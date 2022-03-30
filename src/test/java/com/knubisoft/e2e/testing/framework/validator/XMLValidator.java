package com.knubisoft.e2e.testing.framework.validator;

import java.io.File;

public interface XMLValidator<E> {

    void validate(E e, File xmlFile);
}
