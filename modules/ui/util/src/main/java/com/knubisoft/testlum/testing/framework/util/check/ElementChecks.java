package com.knubisoft.testlum.testing.framework.util.check;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.knubisoft.testlum.testing.framework.util.check.ElementCheck.*;

public final class ElementChecks {

    public static final Set<ElementCheck> FOR_INTERACTION = Collections.unmodifiableSet(
            EnumSet.of(VISIBILITY, SCROLLED_INTO_VIEW_AND_INTERACTABLE, ENABLED));

    public static final Set<ElementCheck> FOR_WRITING = Collections.unmodifiableSet(
            EnumSet.of(VISIBILITY, SCROLLED_INTO_VIEW_AND_INTERACTABLE, ENABLED, EDITABLE));

    public static final Set<ElementCheck> FOR_POSITIONING = Collections.unmodifiableSet(
            EnumSet.of(VISIBILITY, SCROLLED_INTO_VIEW_AND_INTERACTABLE));

    public static final Set<ElementCheck> FOR_READING = Collections.unmodifiableSet(
            EnumSet.of(VISIBILITY));

    public static final Set<ElementCheck> NONE = Collections.emptySet();

    private ElementChecks() {
    }
}
