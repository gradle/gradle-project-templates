package org.gradlex.templates.standalone;

import org.gradlex.templates.TemplateLogger;

public class StandaloneLogger implements TemplateLogger {
    @Override
    public void info(String message) {
        System.out.println(message);
    }
}
