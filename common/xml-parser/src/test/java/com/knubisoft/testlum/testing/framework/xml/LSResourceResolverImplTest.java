package com.knubisoft.testlum.testing.framework.xml;

import org.junit.jupiter.api.Test;
import org.w3c.dom.ls.LSInput;

import static org.junit.jupiter.api.Assertions.*;

class LSResourceResolverImplTest {

    @Test
    void resolveResourceReturnsLSInputForExistingSchema() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        LSInput input = resolver.resolveResource(null, null, null, "sub-schema.xsd", null);
        assertNotNull(input);
    }

    @Test
    void resolveResourceReturnsNullForAlreadyLoadedSchema() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        resolver.resolveResource(null, null, null, "sub-schema.xsd", null);
        LSInput secondCall = resolver.resolveResource(null, null, null, "sub-schema.xsd", null);
        assertNull(secondCall);
    }

    @Test
    void resolveResourceThrowsForMissingSchema() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        assertThrows(RuntimeException.class,
                () -> resolver.resolveResource(null, null, null, "nonexistent.xsd", null));
    }

    @Test
    void resolveResourceThrowsWithDescriptiveMessage() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> resolver.resolveResource(null, null, null, "missing.xsd", null));
        assertEquals("Incorrect jar structure. Unable to locate schema for missing.xsd",
                exception.getMessage());
    }

    @Test
    void resolveResourceHandlesMultipleDifferentSchemas() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        LSInput first = resolver.resolveResource(null, null, null, "sub-schema.xsd", null);
        LSInput second = resolver.resolveResource(null, null, null, "test-item.xsd", null);
        assertNotNull(first);
        assertNotNull(second);
    }

    @Test
    void resolveResourceSetsPublicIdAndSystemId() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        LSInput input = resolver.resolveResource(null, null, "pubId", "sub-schema.xsd", "baseURI");
        assertNotNull(input);
        assertEquals("pubId", input.getPublicId());
        assertEquals("sub-schema.xsd", input.getSystemId());
        assertEquals("baseURI", input.getBaseURI());
    }

    @Test
    void resolveResourceWithNullPublicIdAndBaseUri() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        LSInput input = resolver.resolveResource(null, null, null, "sub-schema.xsd", null);
        assertNotNull(input);
        assertNull(input.getPublicId());
        assertNull(input.getBaseURI());
    }

    @Test
    void resolveResourceReturnsInputWithContent() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("test-schema");
        LSInput input = resolver.resolveResource(null, null, null, "sub-schema.xsd", null);
        assertNotNull(input);
        assertNotNull(input.getCharacterStream() != null || input.getByteStream() != null
                || input.getStringData() != null);
    }

    @Test
    void resolveResourceWithEmptyBasePath() {
        LSResourceResolverImpl resolver = new LSResourceResolverImpl("");
        assertThrows(RuntimeException.class,
                () -> resolver.resolveResource(null, null, null, "sub-schema.xsd", null));
    }
}
