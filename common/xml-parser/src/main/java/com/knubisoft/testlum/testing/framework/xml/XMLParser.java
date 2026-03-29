package com.knubisoft.testlum.testing.framework.xml;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public E process(final File file, final XMLValidator<E> validator) {
        XSDValidator.validateBySchema(file, this.schema);
        try {
            E obj = deserializeXmlTo(file, this.cls, this.objectFactory);
            validator.validate(obj, file);
            return obj;
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public E process(final File file) {
        XSDValidator.validateBySchema(file, this.schema);
        try {
            return deserializeXmlTo(file, this.cls, this.objectFactory);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
