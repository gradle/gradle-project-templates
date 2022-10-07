package org.gradlex.templates;

import org.gradle.api.logging.Logger;
import templates.TemplateLogger;

public class GradleLogger implements TemplateLogger {

    private final Logger delegate;

    public GradleLogger(Logger delegate) {
        this.delegate = delegate;
    }
    @Override
    public void info(String message) {
        delegate.info(message);
    }

    @Override
    public void warn(String msg, Throwable t) {
        delegate.warn(msg, t);
    }
}
