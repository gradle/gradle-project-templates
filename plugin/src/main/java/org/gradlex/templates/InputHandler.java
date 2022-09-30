package org.gradlex.templates;

import javax.annotation.Nullable;
import java.util.Collection;

public interface InputHandler {
    @Nullable
    Boolean askYesNoQuestion(String question);

    boolean askYesNoQuestion(String question, boolean defaultValue);

    <T> T selectOption(String question, Collection<T> options, T defaultOption);

    String askQuestion(String question, String defaultValue);
}
