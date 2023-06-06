package com.knubisoft.testlum.testing.framework.util;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Serializers;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import com.knubisoft.testlum.testing.model.scenario.WebsocketStomp;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSubscribe;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.boot.jackson.JsonComponentModule;

import java.util.ArrayList;

@UtilityClass
public class InjectionUtil {

    private static final ObjectMapper MAPPER = buildObjectMapper();

    @SuppressWarnings("unchecked")
    public <T> T injectObject(final T t, final ScenarioContext scenarioContext) {
        return injectObject(t, (Class<T>) t.getClass(), scenarioContext);
    }

    @SneakyThrows
    public <T> T injectObject(final T t, final Class<T> clazz, final ScenarioContext scenarioContext) {
        String asJson = MAPPER.writeValueAsString(t);
        String injected = scenarioContext.inject(asJson);
        JavaType javaType = MAPPER.getTypeFactory().constructType(clazz);
        return MAPPER.readValue(injected, javaType);
    }

    private ObjectMapper buildObjectMapper() {
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
//                .allowIfSubType(AbstractUiCommand.class)
//                .allowIfSubType(AbstractCommand.class)
//                .allowIfSubType(WebsocketStomp.class)
//                .allowIfSubType(WebsocketSend.class)
//                .allowIfSubType(WebsocketReceive.class)
//                .allowIfSubType(WebsocketSubscribe.class)
//                .allowIfSubType(ArrayList.class)
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.util.")
                .allowIfSubType("com.knubisoft.testlum.testing.model.scenario.")
                .build();

        return JsonMapper.builder()
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS )
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.WRAPPER_ARRAY)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.WRAPPER_OBJECT)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.EXISTING_PROPERTY)
//                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS, JsonTypeInfo.As.PROPERTY)
//                .findAndAddModules()
//                .addModule(new SimpleModule())
//                .addModule(new JavaTimeModule())
//                .addModule(new ParameterNamesModule())
//                .addModule(new JacksonXmlModule())
//                .addModule(new JsonComponentModule())
//                .addModule(new JaxbAnnotationModule())
//                .addModule(new Jdk8Module())
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
}
