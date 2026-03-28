package com.knubisoft.testlum.testing.framework.xml;

import org.apache.commons.io.IOUtils;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class LSResourceResolverImpl implements LSResourceResolver {

    private final Set<String> schemaNames = new HashSet<>();
    private final String schemaBasePath;

    public LSResourceResolverImpl(final String basePath) {
        this.schemaBasePath = basePath;
    }

    @Override
    public LSInput resolveResource(final String type, final String namespaceURI,
                                   final String publicId, final String systemId, final String baseURI) {
        if (!schemaNames.contains(systemId)) {
            return buildInput(publicId, systemId, baseURI);
        }
        return null;
    }

    private LSInput buildInput(final String publicId, final String systemId, final String baseURI) {
        InputStream resourceAsStream = loadSchemaStream(systemId);
        try {
            byte[] content = readContent(resourceAsStream);
            schemaNames.add(systemId);
            return new DOMInputImpl(publicId, systemId, baseURI,
                    new ByteArrayInputStream(content), StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream loadSchemaStream(final String systemId) {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(getPathToSchema(systemId));
        if (stream == null) {
            throw new RuntimeException("Incorrect jar structure. Unable to locate schema for " + systemId);
        }
        return stream;
    }

    private static byte[] readContent(final InputStream resourceAsStream) throws IOException {
        try {
            return IOUtils.toByteArray(resourceAsStream);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

    private String getPathToSchema(final String schemaName) {
        return String.join("/", schemaBasePath, schemaName);
    }
}
