package com.eylen.sensordsl.generator.motion

import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

class AndroidMotionSensorsCodeGenerator extends MotionSensorsCodeGenerator{
    private static String DEFAULT_ACCELEROMETER_VARIABLE = "mAccelerometerSensor"
    private static String DEFAULT_GYROSCOPE_VARIABLE = "mGyroscopeSensor"
    private static String DEFAULT_SENSOR_MANAGER_VARIABLE = "mSensorManager"

    private SimpleTemplateEngine templateEngine

    AndroidMotionSensorsCodeGenerator(FileParser fileParser) {
        super(fileParser)
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    protected void makeGeneration() {
        String templateDirPath = Constants.TEMPLATES + "/android/motion/"
        Template sensorVariableTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "sensor_variable.template")))
        Template sensorInitializerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "sensor_initialize.template")))
        Template registerListenerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "register_listener.template")))

        List<String> variables = new ArrayList<>()
        if (foregroundHelper){
            //TODO controlar nombres de variables
            Template listenerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "motion_listener.template")))
            Template sensorListenerDeclarationTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "sensor_listener_declaration.template")))
            Template unregisterListenerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "unregister_listener.template")))
            Template sensorManagerVariableTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "sensor_manager_variable.template")))
            Template importsTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "imports.template")))

            String importLines = importsTemplate.make().toString()
            importLines.eachLine {String line ->
                if (!fileParser.scriptFile.contains(line.trim())){
                    fileParser.imports << line
                }
            }

            //Create variables for sensorManager and sensors
            variables << sensorManagerVariableTemplate.make().toString()
            variables << sensorListenerDeclarationTemplate.make().toString()
            if (foregroundHelper.accelerometer) {
                variables << sensorVariableTemplate.make(varName:DEFAULT_ACCELEROMETER_VARIABLE).toString()
            }
            if (foregroundHelper.gyroscope){
                variables << sensorVariableTemplate.make(varName:DEFAULT_GYROSCOPE_VARIABLE).toString()
            }

            //Initialize sensorManager and sensors
            List<String> lines = new ArrayList<>()
            ParsedMethod parsedMethod = GeneratorUtils.methodExists(fileParser, "onCreate", true)
            boolean createMethod = !parsedMethod
            if (createMethod) parsedMethod = new ParsedMethod()

            String sensorInitializer = sensorInitializerTemplate.make(createMethod:createMethod, accelerometer:foregroundHelper.accelerometer, gyroscope:foregroundHelper.gyroscope, context:"mContext", sensorManager:DEFAULT_SENSOR_MANAGER_VARIABLE).toString()
            sensorInitializer.eachLine {lines << it}
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod,createMethod,"public void onCreate(Bundle savedInstanceState)", lines, fileParser)

            lines.clear()

            //Add listener registration
            parsedMethod = GeneratorUtils.methodExists(fileParser, "onResume", true)
            createMethod = !parsedMethod
            if (createMethod) parsedMethod = new ParsedMethod()

            String registerListener = registerListenerTemplate.make(createMethod:createMethod, accelerometer:foregroundHelper.accelerometer, gyroscope:foregroundHelper.gyroscope, listener:"sensorDSLMotionListener").toString()
            registerListener.eachLine {lines << it}
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "public void onResume()", lines, fileParser)

            lines.clear()

            //Add listener unregistration
            parsedMethod = GeneratorUtils.methodExists(fileParser, "onPause", true)
            createMethod = !parsedMethod
            if (createMethod) parsedMethod = new ParsedMethod()

            String unregisterListener = unregisterListenerTemplate.make(createMethod:createMethod, sensorManager:DEFAULT_SENSOR_MANAGER_VARIABLE).toString()
            unregisterListener.eachLine {lines << it}
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "public void onPause()", lines, fileParser)

            //Add the sensor listener class
            String motionListenerClass = listenerTemplate.make([accelerometer:foregroundHelper.accelerometer, accelerometerCallback:foregroundHelper.accelerometerCallback, gyroscope:foregroundHelper.gyroscope, gyroscopeCallback:foregroundHelper.gyroscopeCallback]).toString()
            motionListenerClass.eachLine {
                fileParser.lines << it
            }

        }
        if (backgroundHelper){
            Template sensorServiceTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "sensor_service.template")))
            Template broadcastReceiverTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "broadcast_receiver.template")))
            Template broadcastRegisterTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "broadcast_register.template")))

            ParsedMethod parsedMethod = GeneratorUtils.methodExists(fileParser, "onCreate", true)
            boolean createMethod = !parsedMethod
            if (createMethod) parsedMethod = new ParsedMethod()

            String broadcastReceiver = broadcastReceiverTemplate.make(accelerometer:backgroundHelper.accelerometer, accelerometerCallback:backgroundHelper.accelerometerCallback, gyroscope:backgroundHelper.gyroscope, gyroscopeCallback:backgroundHelper.gyroscopeCallback).toString()
            String broadcastRegister = broadcastRegisterTemplate.make().toString()
            List<String> lines = new ArrayList<>()
            broadcastReceiver.eachLine {lines << it}
            lines << broadcastRegister
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "public void onCreate(Bundle savedInstanceState", lines, fileParser)

            if (backgroundHelper.accelerometer) {
                variables << sensorVariableTemplate.make(varName:DEFAULT_ACCELEROMETER_VARIABLE).toString()
            }
            if (backgroundHelper.gyroscope){
                variables << sensorVariableTemplate.make(varName:DEFAULT_GYROSCOPE_VARIABLE).toString()
            }

            String sensorsInitialization = sensorInitializerTemplate.make(createMethod:false, sensorManager:"mSensorManager", accelerometer:backgroundHelper.accelerometer, gyroscope:backgroundHelper.gyroscope, context:null).toString()
            String registerListeners = registerListenerTemplate.make(createMethod:false, accelerometer:backgroundHelper.accelerometer, gyroscope:backgroundHelper.gyroscope, listener:"this").toString()

            String sensorService = sensorServiceTemplate.make(variables:variables, sensorsInitialization:sensorsInitialization, registerListeners:registerListeners, listener:"this").toString()
            fileParser.newClasses["MotionSensorDSLService.java"] = sensorService
        }

        fileParser.lines.addAll(0, variables)
        fileParser.firstMethod += variables.size()
    }


}
