function xPath(node, optimized) {
  if (node.nodeType === Node.DOCUMENT_NODE) return "/";

  const steps = [];

  let contextNode = node;

  while (contextNode) {
    const step = xPathValue(contextNode, optimized);

    if (!step) break; // Error - bail out early.

    steps.push(step);

    if (step.optimized) break;

    contextNode = contextNode.parentNode;
  }

  steps.reverse();

  return (steps.length && steps[0].optimized ? "" : "/") + steps.join("/");
}

function xPathValue(node, optimized) {
  let ownValue;

  const ownIndex = xPathIndex(node);

  if (ownIndex === -1) return null; // Error.

  switch (node.nodeType) {
    case Node.ELEMENT_NODE:
      if (optimized && node.getAttribute("id")) {
        return new Step('//*[@id="' + node.getAttribute("id") + '"]', true);
      }
      ownValue = node.localName;
      break;
    case Node.ATTRIBUTE_NODE:
      ownValue = "@" + node.nodeName;
      break;
    case Node.TEXT_NODE:
    case Node.CDATA_SECTION_NODE:
      ownValue = "text()";
      break;
    case Node.PROCESSING_INSTRUCTION_NODE:
      ownValue = "processing-instruction()";
      break;
    case Node.COMMENT_NODE:
      ownValue = "comment()";
      break;
    case Node.DOCUMENT_NODE:
      ownValue = "";
      break;
    default:
      ownValue = "";
      break;
  }

  if (ownIndex > 0) ownValue += "[" + ownIndex + "]";

  return new Step(ownValue, node.nodeType === Node.DOCUMENT_NODE);
}

function xPathIndex(node) {
  // Returns -1 in case of error, 0 if no siblings matching the same expression, <XPath index among the same expression-matching sibling nodes> otherwise.
  function areNodesSimilar(left, right) {
    if (left === right) return true;

    if (
      left.nodeType === Node.ELEMENT_NODE &&
      right.nodeType === Node.ELEMENT_NODE
    ) {
      return left.localName === right.localName;
    }

    if (left.nodeType === right.nodeType) return true;

    // XPath treats CDATA as text nodes.
    const leftType =
      left.nodeType === Node.CDATA_SECTION_NODE
        ? Node.TEXT_NODE
        : left.nodeType;

    const rightType =
      right.nodeType === Node.CDATA_SECTION_NODE
        ? Node.TEXT_NODE
        : right.nodeType;

    return leftType === rightType;
  }

  const siblings = node.parentNode ? node.parentNode.children : null;

  if (!siblings) return 0; // Root node - no siblings.

  let hasSameNamedElements;

  for (let i = 0; i < siblings.length; ++i) {
    if (areNodesSimilar(node, siblings[i]) && siblings[i] !== node) {
      hasSameNamedElements = true;
      break;
    }
  }

  if (!hasSameNamedElements) return 0;

  let ownIndex = 1; // XPath indices start with 1.

  for (let i = 0; i < siblings.length; ++i) {
    if (areNodesSimilar(node, siblings[i])) {
      if (siblings[i] === node) return ownIndex;

      ++ownIndex;
    }
  }
  return -1; // An error occurred: |node| not found in parent's children.
}

class Step {
  constructor(value, optimized = false) {
    this.value = value;
  }

  toString() {
    return this.value;
  }
}

function getTagsByText(str, tag = "a") {
  const tagsWithText = Array.prototype.slice
    .call(document.getElementsByTagName(tag))
    .filter((el) => {
      if (el.placeholder) {
        return el.placeholder.includes(str) && el.childElementCount === 0;
      }
      return el.textContent.includes(str) && el.childElementCount === 0;
    });
  if (tagsWithText.length != 0) {
    return tagsWithText;
  }
}

const findByText = (text) => {
  let tags = [];
  let tagNames = [];
  for (tag of document.body.getElementsByTagName("*")) {
    if (!tagNames.includes(tag.tagName) && tag.childElementCount === 0) {
      tags.push(tag);
      tagNames.push(tag.tagName);
    }
  }

  let results = [];

  tags.forEach((tag) => {
    const tagsWithText = getTagsByText(text, tag.tagName.toLowerCase());
    if (tagsWithText) results.push(tagsWithText);
  });

  let processingTags = [];

  processingTags = results.flat();
  results = [];

  processingTags.forEach((res) => {
    results.push(`${xPath(res, false)}`);
  });

  return results;
};

findByText("Почати")
