package com.knubisoft.testlum.testing.framework.xml;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
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

    @SneakyThrows
    @Override
    public LSInput resolveResource(final String type, final String namespaceURI,
                                   final String publicId, final String systemId, final String baseURI) {
        if (!schemaNames.contains(systemId)) {
            InputStream resourceAsStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream(getPathToSchema(systemId));
            if (resourceAsStream == null) {
                throw new RuntimeException("Incorrect jar structure. Unable to locate schema for " + systemId);
            }
            byte[] content = readContent(resourceAsStream);
            schemaNames.add(systemId);
            String charset = StandardCharsets.UTF_8.name();
            return new DOMInputImpl(publicId, systemId, baseURI, new ByteArrayInputStream(content), charset);
        }
        return null;
    }

    private static byte[] readContent(final InputStream resourceAsStream) throws IOException {
        try {
            return IOUtils.toByteArray(resourceAsStream);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

    private String getPathToSchema(final String schemaName) {
        return String.format("%s%s%s", schemaBasePath, File.separator, schemaName);
    }
}
