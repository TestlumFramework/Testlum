package com.knubisoft.testlum.testing.framework.schema;

import org.apache.xerces.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.codec.CharEncoding.UTF_8;

public class LSResourceResolverImpl implements LSResourceResolver {

    private final String schemaBasePath;

    private final Set<String> schemaNames = new HashSet<>();

    public LSResourceResolverImpl(final String basePath) {
        this.schemaBasePath = basePath;
    }

    @Override
    public LSInput resolveResource(final String type, final String namespaceURI,
                                   final String publicId, final String systemId, final String baseURI) {
        if (!schemaNames.contains(systemId)) {
            InputStream resourceAsStream = this.getClass()
                    .getClassLoader()
                    .getResourceAsStream(getPathToSchema(systemId));
            schemaNames.add(systemId);
            return new DOMInputImpl(publicId, systemId, baseURI, resourceAsStream, UTF_8);
        }
        return null;
    }

    private String getPathToSchema(final String schemaName) {
        return format("%s%s%s", schemaBasePath, File.separator, schemaName);
    }
}
