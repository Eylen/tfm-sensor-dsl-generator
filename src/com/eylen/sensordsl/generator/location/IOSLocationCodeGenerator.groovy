package com.eylen.sensordsl.generator.location

import com.eylen.sensordsl.enums.TrackType
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

class IOSLocationCodeGenerator extends  LocationCodeGenerator{
    private SimpleTemplateEngine templateEngine

    IOSLocationCodeGenerator(FileParser fileParser) {
        super(fileParser)
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    void makeGeneration() {
        if (lastHelper || trackerHelper) {
            String templateDirPath = Constants.TEMPLATES + "/ios/location/"
            //TODO implements....
            //TODO appdelegate
            //TODO variables de interfaz
            Template importsTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "imports.template")))
            Template locationManagerTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "locationmanager.template")))
            Template viewLoadTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "viewDidLoad.template")))
            Template viewUnloadTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "viewDidDisappear.template")))
            Template startMethodTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "start_method.template")))
            Template stopMethodTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "stop_method.template")))
            Template lastLocationTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "last_location.template")))
            Template locationChangedTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "location_changed.template")))

            String templateContent = importsTemplate.make().toString()
            templateContent.eachLine { fileParser.imports << it }

            List<String> lines = new ArrayList<>()
            ParsedMethod parsedMethod = GeneratorUtils.methodExists(fileParser, "viewDidLoad", true)
            boolean createMethod = !parsedMethod
            if (!parsedMethod) parsedMethod = new ParsedMethod()
            templateContent = viewLoadTemplate.make(createMethod: createMethod, automatic: trackerHelper?.trackType == TrackType.AUTO).toString()
            templateContent.eachLine { lines << it }
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "viewDidLoad", lines, fileParser)

            if (trackerHelper) {
                if (trackerHelper.trackType == TrackType.MANUAL) {
                    templateContent = startMethodTemplate.make(methodName: trackerHelper.startMethod).toString()
                    lines.clear()
                    templateContent.eachLine { lines << it }
                    parsedMethod = new ParsedMethod()
                    GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, trackerHelper.startMethod, lines, fileParser)
                    templateContent = stopMethodTemplate.make(methodName: trackerHelper.stopMethod).toString()
                    lines.clear()
                    templateContent.eachLine { lines << it }
                    parsedMethod = new ParsedMethod()
                    GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, trackerHelper.stopMethod, lines, fileParser)
                } else {
                    parsedMethod = GeneratorUtils.methodExists(fileParser, "viewDidDisappear", true)
                    createMethod = !parsedMethod
                    if (!parsedMethod) parsedMethod = new ParsedMethod()
                    templateContent = viewUnloadTemplate.make(createMethod: createMethod, automatic: true).toString()
                    lines.clear()
                    templateContent.eachLine { lines << it }
                    GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "viewDidDisappear", lines, fileParser)
                }
                templateContent = locationChangedTemplate.make(methodName: trackerHelper.callbackMethod).toString()
                lines.clear()
                templateContent.eachLine { lines << it }
                parsedMethod = new ParsedMethod()
                GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, trackerHelper.callbackMethod, lines, fileParser)
            }
            if (lastHelper) {
                templateContent = lastLocationTemplate.make(methodName: lastHelper.lastLocationCallback).toString()
                lines.clear()
                templateContent.eachLine { lines << it }
                parsedMethod = new ParsedMethod()
                GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, lastHelper.lastLocationCallback, lines, fileParser)
            }


        }
    }
}
