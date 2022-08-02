package com.knubisoft.cott.testing.framework.parser;

import com.knubisoft.cott.testing.framework.validator.XMLValidator;
import com.knubisoft.cott.testing.framework.validator.XSDValidator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import java.io.File;

@RequiredArgsConstructor
@Slf4j
public final class XMLParser<E> {

    private final Schema schema;
    private final Class<E> cls;
    private final Class<?> objectFactory;

    private static <E> E deserializeXmlTo(final File file,
                                          final Class<E> cls,
                                          final Class<?> objectFactory) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(objectFactory);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StreamSource streamSource = new StreamSource(file);
        JAXBElement<E> element = unmarshaller.unmarshal(streamSource, cls);
        return element.getValue();
    }

    @SneakyThrows
    public E process(final File file, final XMLValidator<E> validator) {
        XSDValidator.validateBySchema(file, this.schema);
        E obj = deserializeXmlTo(file, this.cls, this.objectFactory);
        validator.validate(obj, file);
        return obj;
    }

    @SneakyThrows
    public E process(final File file) {
        XSDValidator.validateBySchema(file, this.schema);
        return deserializeXmlTo(file, this.cls, this.objectFactory);
    }
}
