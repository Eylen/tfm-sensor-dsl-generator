package com.eylen.sensordsl.generator.location

import com.eylen.sensordsl.enums.LocationPriorityType
import com.eylen.sensordsl.enums.TrackType
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

class AndroidLocationCodeGenerator extends LocationCodeGenerator{
    private SimpleTemplateEngine templateEngine

    public AndroidLocationCodeGenerator(FileParser fileParser){
        super(fileParser)
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    void makeGeneration() {
        if (lastHelper || trackerHelper) {
            String templateDirPath = Constants.TEMPLATES + "/android/location/"
            Template listenerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "location_listener.template")))
            Template listenerDeclarationTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "location_listener_declaration.template")))
            Template listenerInitTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "location_listener_init.template")))
            Template startListenerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "start_listener.template")))
            Template stopListenerTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "stop_listener.template")))
            Template importsTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "imports.template")))

            int packageStart = fileParser.beforeInnerClass.indexOf("package")
            String packageDeclaration = null
            if (packageStart != -1) {
                packageDeclaration = fileParser.beforeInnerClass.substring(packageStart)
                packageDeclaration = packageDeclaration.substring(0, packageDeclaration.indexOf(";") + 1)
            }

            String importLines = importsTemplate.make().toString()
            importLines.eachLine { String line ->
                if (!fileParser.scriptFile.contains(line.trim())) {
                    fileParser.imports << line
                }
            }

            String priorityType = getPriorityType(trackerHelper?.priorityType)
            String listenerClass = listenerTemplate.make(packageName: packageDeclaration, lastLocationCallback: lastHelper?.lastLocationCallback, locationTrackCallback: trackerHelper?.callbackMethod, startMethod: trackerHelper?.startMethod ?: "startTracking", stopMethod: trackerHelper?.stopMethod ?: "stopMethod", interval: trackerHelper?.interval, fastestInterval: trackerHelper?.fastestInterval, locationPriority: priorityType).toString()
            listenerClass.eachLine {
                fileParser.lines << it
            }
            fileParser.lines.add(0, listenerDeclarationTemplate.make().toString())

            //Initialize sensorManager and sensors
            List<String> lines = new ArrayList<>()
            ParsedMethod parsedMethod = GeneratorUtils.methodExists(fileParser, "onCreate", true)
            boolean createMethod = !parsedMethod
            if (createMethod) parsedMethod = new ParsedMethod()

            String sensorInitializer = listenerInitTemplate.make(createMethod: createMethod).toString()
            sensorInitializer.eachLine { lines << it }
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "public void onCreate(Bundle savedInstanceState)", lines, fileParser)

            if (trackerHelper?.trackType == TrackType.AUTO) {
                lines.clear()

                parsedMethod = GeneratorUtils.methodExists(fileParser, "onResume", true)
                createMethod = !parsedMethod
                if (createMethod) parsedMethod = new ParsedMethod()

                String startListener = startListenerTemplate.make(createMethod: createMethod).toString()
                startListener.eachLine { lines << it }
                GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "public void onResume()", lines, fileParser)

                lines.clear()
                parsedMethod = GeneratorUtils.methodExists(fileParser, "onPause", true)
                createMethod = !parsedMethod
                if (createMethod) parsedMethod = new ParsedMethod()

                String stopListener = stopListenerTemplate.make(createMethod: createMethod).toString()
                stopListener.eachLine { lines << it }
                GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "public void onPause()", lines, fileParser)
            }
        }
//        fileParser.newClasses["SensorDSLLocationListener.java"]=listenerClass
    }

    private String getPriorityType(LocationPriorityType priorityType){
        switch (priorityType){
            case LocationPriorityType.LOW_POWER:
                return "PRIORITY_LOW_POWER"
            case LocationPriorityType.NO_POWER:
                return "PRIORITY_NO_POWER"
            case LocationPriorityType.HIGH_ACCURACY:
                return "PRIORITY_HIGH_ACCURACY"
            case LocationPriorityType.BALANCED:
                return "PRIORITY_BALANCED_POWER_ACCURACY"
        }
        return null
    }
}
