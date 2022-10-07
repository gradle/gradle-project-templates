package templates;

public interface TemplateLogger {

    void info(String message);

    void warn(String msg, Throwable t);
}
