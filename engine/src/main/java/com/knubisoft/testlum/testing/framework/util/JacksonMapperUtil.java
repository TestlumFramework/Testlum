package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knubisoft.testlum.testing.framework.vault.model.VaultDto;
import com.knubisoft.testlum.testing.framework.vault.model.VaultDtoDeserializer;
import lombok.SneakyThrows;

public final class JacksonMapperUtil {

    private static final ObjectMapper MAPPER = buildObjectMapper();
    private static final ObjectMapper COPY_MAPPER = createObjectMapperForDeepCopy();
    private static final ObjectMapper VAULT_MAPPER = buildObjectToVaultMapper();

    @SneakyThrows
    public static <T> T readValue(final String content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public static VaultDto readVaultValue(final String content, final Class<VaultDto> vaultDto) {
        return VAULT_MAPPER.readValue(content, vaultDto);
    }

    @SneakyThrows
    public static String writeValueAsString(final Object value) {
        return MAPPER.writeValueAsString(value);
    }

    @SneakyThrows
    public static <T> T readCopiedValue(final String content, final Class<T> valueType) {
        JavaType javaType = COPY_MAPPER.getTypeFactory().constructType(valueType);
        return COPY_MAPPER.readValue(content, javaType);
    }

    @SneakyThrows
    public static String writeValueToCopiedString(final Object value) {
        return COPY_MAPPER.writeValueAsString(value);
    }

    private static ObjectMapper buildObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private static ObjectMapper createObjectMapperForDeepCopy() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.scenario.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.global_config.")
                .build();

        return JsonMapper.builder()
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE)
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS)
                .build();
    }

    private static ObjectMapper buildObjectToVaultMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(VaultDto.class, new VaultDtoDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
