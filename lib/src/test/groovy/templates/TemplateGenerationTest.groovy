package templates

import spock.lang.Specification
import spock.lang.TempDir

class TemplateGenerationTest extends Specification {

    @TempDir
    File tmpDir

    File cloneDir
    File targetDir
    Map<String, Object> data
    TemplateEngine templateEngine
    TemplateLogger logger

    def setup() {
        cloneDir = new File(tmpDir, 'clone')
        cloneDir.mkdir()
        targetDir = new File(tmpDir, 'target')
        targetDir.mkdir()

        data = [:]
        templateEngine = new FreemarkerTemplateEngine()
        logger = Mock(TemplateLogger)
    }

    def 'clone dir does not exist'() {
        setup:
        cloneDir = new File(tmpDir, 'nonexistent')

        when:
        processTemplates()

        then:
        TemplateGenerationException e = thrown(TemplateGenerationException)
        e.message.startsWith('Template project not found at')
    }

    def 'empty clone dir'() {
        when:
        processTemplates()

        then:
        targetDir.listFiles().size() == 0
    }

    def 'template project contains plain file'() {
        setup:
        File f = new File(cloneDir, 'f')
        f << 'content'

        when:
        processTemplates()

        then:
        new File(targetDir, 'f').text ==  'content'
    }

    def 'template project contains template file'() {
        setup:
        File f = new File(cloneDir, 'f')
        f << '''<#GradleTemplate>
</#GradleTemplate>
content'''

        when:
        processTemplates()

        then:
        new File(targetDir, 'f').text == 'content'
    }


    def processTemplates() {
        new TemplateGeneration().processTemplates(cloneDir, targetDir, data, templateEngine, logger)
    }

}