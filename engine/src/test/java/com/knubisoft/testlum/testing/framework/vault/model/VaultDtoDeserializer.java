package com.knubisoft.testlum.testing.framework.vault.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class VaultDtoDeserializer extends StdDeserializer<VaultDto> {
    private static final long serialVersionUID = 1;

    public VaultDtoDeserializer() {
        this(null);
    }

    public VaultDtoDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public VaultDto deserialize(final JsonParser jsonParser,
                                             final DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String key = node.fieldNames().next();
        String value = node.get(key).asText();

        return new VaultDto(key, value);
    }
}
