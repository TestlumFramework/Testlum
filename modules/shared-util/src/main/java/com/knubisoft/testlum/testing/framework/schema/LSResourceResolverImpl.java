package com.knubisoft.testlum.testing.framework.schema;

import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.lang3.CharEncoding.UTF_8;

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
            return new DOMInputImpl(publicId, systemId, baseURI, new ByteArrayInputStream(content), UTF_8);
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
        return format("%s%s%s", schemaBasePath, File.separator, schemaName);
    }
}
