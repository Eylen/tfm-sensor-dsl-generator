package com.eylen.sensordsl.generator.motion

import com.eylen.sensordsl.generator.helpers.MotionSensorsGeneratorHelper
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

/**
 * Created by Saioa on 02/09/2015.
 */
class IOSMotionSensorsCodeGenerator extends MotionSensorsCodeGenerator{
    private SimpleTemplateEngine templateEngine

    IOSMotionSensorsCodeGenerator(FileParser fileParser) {
        super(fileParser)
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    protected void makeGeneration() {
        String templateDirPath = Constants.TEMPLATES + "/ios/motion/"
        //TODO en AppDelegate podría ser cualquier clase...
        Template delegateMotionManagerTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "appdelegate/cmotionmanager_declaration.template")))
        Template delegateMotionManagerMethodTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "appdelegate/cmotionmanager_method.template")))

        List<String> appDelegateLines = new ArrayList<>()
        String appContent = delegateMotionManagerTemplate.make().toString()
        appContent = delegateMotionManagerMethodTemplate.make().toString()

        Template importsTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath +"imports.template")))
        Template motionManagerTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "cmotionmanager.template")))

        Template viewLoadTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "viewDidLoad.template")))

        List<String> accelerometerCallbacks = new ArrayList<>()
        List<String> gyroscopeCallbacks = new ArrayList<>()
        if (foregroundHelper){
            if (foregroundHelper.accelerometer) accelerometerCallbacks << foregroundHelper.accelerometerCallback
            if (foregroundHelper.gyroscope) gyroscopeCallbacks << foregroundHelper.gyroscopeCallback
        }
        if (backgroundHelper){
            if (backgroundHelper.accelerometer) accelerometerCallbacks << backgroundHelper.accelerometerCallback
            if (backgroundHelper.gyroscope) gyroscopeCallbacks << backgroundHelper.gyroscopeCallback
        }

        String templateContent = importsTemplate.make().toString()
        templateContent.eachLine {fileParser.imports << it}

        List<String> lines = new ArrayList<>()
        ParsedMethod parsedMethod = GeneratorUtils.methodExists(fileParser, "(void)viewDidLoad", false)
        boolean methodExists = parsedMethod!=null
        if (!parsedMethod) parsedMethod = new ParsedMethod()
        templateContent = viewLoadTemplate.make(createMethod:!methodExists, accelerometer: foregroundHelper?.accelerometer || backgroundHelper?.accelerometer, accelerometerCallbacks: accelerometerCallbacks,
            gyroscope: foregroundHelper?.gyroscope || backgroundHelper?.gyroscope, gyroscopeCallbacks:gyroscopeCallbacks).toString()
        templateContent.eachLine {lines << it}
        GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, !methodExists, "(void)viewDidLoad", lines, fileParser)

        templateContent = motionManagerTemplate.make().toString()
        lines.clear()
        parsedMethod = GeneratorUtils.methodExists(fileParser, "-(CMMotionManager *)motionManager", false)
        if (!parsedMethod) {
            parsedMethod = new ParsedMethod()
            templateContent.eachLine {lines << it}
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, "-(CMMotionManager *)motionManager", lines, fileParser)
        }

    }
}
