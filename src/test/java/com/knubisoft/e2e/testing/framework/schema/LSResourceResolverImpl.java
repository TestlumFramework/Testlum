package com.knubisoft.e2e.testing.framework.schema;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;
import static org.apache.commons.codec.CharEncoding.UTF_8;

public class LSResourceResolverImpl implements LSResourceResolver {

    private final String schemaBasePath;

    private final Set<String> schemaNames = new HashSet<>();

    public LSResourceResolverImpl(String basePath) {
        this.schemaBasePath = basePath;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI,
                                   String publicId, String systemId, String baseURI) {
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
        return format("%s/%s", schemaBasePath, schemaName);
    }
}
