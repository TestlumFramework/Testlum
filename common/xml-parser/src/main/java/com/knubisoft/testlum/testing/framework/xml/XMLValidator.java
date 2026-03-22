package com.knubisoft.testlum.testing.framework.xml;

import java.io.File;

public interface XMLValidator<E> {

    void validate(E e, File xmlFile);
}
