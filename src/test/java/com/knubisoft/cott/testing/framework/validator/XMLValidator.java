package com.knubisoft.cott.testing.framework.validator;

import java.io.File;

public interface XMLValidator<E> {

    void validate(E e, File xmlFile);
}
