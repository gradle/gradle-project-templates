package org.gradlex.templates;


import org.gradle.api.internal.tasks.userinput.UserInputHandler;

import javax.annotation.Nullable;
import java.util.Collection;

public class GradleInputHandler implements InputHandler {

    private final UserInputHandler delegate;

    public GradleInputHandler(UserInputHandler delegate) {
        this.delegate = delegate;
    }

    @Nullable
    @Override
    public Boolean askYesNoQuestion(String question) {
        return delegate.askYesNoQuestion(question);
    }

    @Override
    public boolean askYesNoQuestion(String question, boolean defaultValue) {
        return delegate.askYesNoQuestion(question, defaultValue);
    }

    @Override
    public <T> T selectOption(String question, Collection<T> options, T defaultOption) {
        return delegate.selectOption(question, options, defaultOption);
    }

    @Override
    public String askQuestion(String question, String defaultValue) {
        return delegate.askQuestion(question, defaultValue);
    }
}
