import org.gradlex.templates.FreemarkerTemplateEngine;
import org.gradlex.templates.TemplateEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

class FreemarkerTemplateEngineTest {

    @TempDir
    File cloneDir;

    @TempDir
    File targetDir;

    @BeforeEach
    void setup(TestInfo testInfo) throws IOException {
        System.err.println(cloneDir);
        System.err.println(targetDir);
    }

    @Test
    @DisplayName("Test 1")
    void test1() throws IOException {
        TemplateEngine engine = new FreemarkerTemplateEngine();
//        engine.processTemplate();
    }
//
//    private void writeString(File file, String string) throws IOException {
//        try (Writer writer = new FileWriter(file)) {
//            writer.write(string);
//        }
//    }
}
