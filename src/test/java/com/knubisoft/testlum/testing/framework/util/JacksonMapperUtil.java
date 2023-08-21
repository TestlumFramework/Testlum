package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.knubisoft.testlum.testing.framework.vault.model.VaultDto;
import com.knubisoft.testlum.testing.framework.vault.model.VaultDtoDeserializer;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.File;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.CLOSE_BRACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.CLOSE_SQUARE_BRACKET;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_BRACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.OPEN_SQUARE_BRACKET;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public final class JacksonMapperUtil {

    private static final ObjectMapper MAPPER = buildObjectMapper();
    private static final ObjectMapper DYNAMODB_MAPPER = createObjectMapperWithFieldVisibility();
    private static final ObjectMapper COPY_MAPPER = createObjectMapperForDeepCopy();
    private static final ObjectMapper VAULT_MAPPER = buildObjectToVaultMapper();


    @SneakyThrows
    public <T> T readValue(final String content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readValue(final byte[] content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public <T> T readValue(final File content, final Class<T> valueType) {
        return MAPPER.readValue(content, valueType);
    }

    @SneakyThrows
    public VaultDto readVaultValue(final String content, final Class<VaultDto> vaultDto) {
        return VAULT_MAPPER.readValue(content, vaultDto);
    }

    @SneakyThrows
    public String writeValueAsString(final Object value) {
        return MAPPER.writeValueAsString(value);
    }

    @SneakyThrows
    public String writeValueAsStringWithDefaultPrettyPrinter(final Object value) {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
    }

    @SneakyThrows
    public String writeAsStringForDynamoDbOnly(final Object value) {
        return DYNAMODB_MAPPER.writeValueAsString(value);
    }

    @SneakyThrows
    public <T> T readCopiedValue(final String content, final Class<T> valueType) {
        JavaType javaType = COPY_MAPPER.getTypeFactory().constructType(valueType);
        return COPY_MAPPER.readValue(content, javaType);
    }

    @SneakyThrows
    public String writeValueToCopiedString(final Object value) {
        return COPY_MAPPER.writeValueAsString(value);
    }

    public Object toJsonObject(final String content) {
        if (isNotBlank(content)
                && ((content.startsWith(OPEN_BRACE) && content.endsWith(CLOSE_BRACE))
                || (content.startsWith(OPEN_SQUARE_BRACKET) && content.endsWith(CLOSE_SQUARE_BRACKET)))) {
            return JacksonMapperUtil.readValue(content, Object.class);
        }
        return content;
    }

    private ObjectMapper buildObjectMapper() {
        return JsonMapper.builder()
                .findAndAddModules()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private ObjectMapper createObjectMapperWithFieldVisibility() {
        ObjectMapper mapper = new ObjectMapper();
        VisibilityChecker<?> config = configMapper(mapper);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setVisibility(config);
        return mapper;
    }

    private VisibilityChecker<?> configMapper(final ObjectMapper mapper) {
        return mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE);
    }

    private ObjectMapper createObjectMapperForDeepCopy() {
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

    private ObjectMapper buildObjectToVaultMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(VaultDto.class, new VaultDtoDeserializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }
}
