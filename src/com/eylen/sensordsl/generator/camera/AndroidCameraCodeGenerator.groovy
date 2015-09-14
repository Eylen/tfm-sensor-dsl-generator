package com.eylen.sensordsl.generator.camera

import com.eylen.sensordsl.generator.helpers.CameraGeneratorHelper
import com.eylen.sensordsl.generator.utils.AndroidConstants
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

class AndroidCameraCodeGenerator extends CameraCodeGenerator{
    private SimpleTemplateEngine templateEngine

    public AndroidCameraCodeGenerator(FileParser fileParser) {
        super(fileParser)
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    protected void makeGeneration() {
        String templateDirPath = Constants.TEMPLATES + "/android/camera/"
        Template cameraMethodTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + AndroidConstants.Camera.Template.CAMERA_METHOD)))
        Template activityResultTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + AndroidConstants.Camera.Template.ACTIVITY_RESULT)))
        Template requestCodeTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + AndroidConstants.Camera.Template.ACTIVITY_CODE_VAR)))
        Template importsTemplate = templateEngine.createTemplate(new InputStreamReader(this.getClass().getResourceAsStream(templateDirPath + "imports.template")))
        boolean createOnActivityResultMethod = !fileParser.methods.containsKey("onActivityResult")

        List<String> variables = new ArrayList<>()
        List<String> methods = new ArrayList<>()
        String activityResult
        String method
        ParsedMethod parsedMethod
        if (helpers.size() > 0){
            String importLines = importsTemplate.make().toString()
            importLines.eachLine {String line ->
                if (!fileParser.scriptFile.contains(line.trim())){
                    fileParser.imports << line
                }
            }
        }

        helpers.each {CameraGeneratorHelper helper ->
            variables << requestCodeTemplate.make([requestCodeName:helper.requestCodeName, requestCodeValue:helper.requestCodeValue]).toString()

            method = cameraMethodTemplate.make([methodName:helper.methodName, mediaType: helper.mediaType, fileUri: helper.fileUri, requestCodeName:helper.requestCodeName, videoQuality:helper.videoQuality, videoDurationLimit:helper.videoDurationLimit, videoSizeLimit:helper.videoSizeLimit])
            parsedMethod = new ParsedMethod()
            parsedMethod.methodName = helper.methodName
//            parsedMethod.linePosition = fileParser.lines.size()
            method.eachLine {parsedMethod.lines << it}
            fileParser.methods[helper.methodName] = parsedMethod
            fileParser.lines << "${Constants.ADD_METHOD_TAG}${helper.methodName}"
            methods << method
            activityResult = activityResultTemplate.make([createMethod:createOnActivityResultMethod, requestCodeName:helper.requestCodeName, successCallback:helper.successCallbackMethod, cancelCallback:helper.cancelCallbackMethod, errorCallback:helper.errorCallbackMethod]).toString()
            if (createOnActivityResultMethod) {
                parsedMethod = new ParsedMethod()
                parsedMethod.methodName = "onActivityResult"
//                parsedMethod.linePosition = fileParser.lines.size()
                activityResult.eachLine {parsedMethod.lines << it}
                parsedMethod.startPosition = parsedMethod.lines[0].indexOf("{")
                parsedMethod.endPosition = parsedMethod.lines[parsedMethod.lines.size() - 1].lastIndexOf("}")
                fileParser.methods["onActivityResult"] = parsedMethod
                fileParser.lines << "${Constants.ADD_METHOD_TAG}onActivityResult"
                createOnActivityResultMethod = false
            } else {
                parsedMethod = fileParser.methods["onActivityResult"]
                String lastLine = parsedMethod.lines[parsedMethod.lines.size() - 1]
                if (lastLine.trim() == "}"){
                    parsedMethod.lines.addAll(parsedMethod.lines.size() - 2, activityResult)
                } else {
                    String lastCodeLine = lastLine.substring(0,lastLine.lastIndexOf("}"))
                    parsedMethod.lines.putAt(parsedMethod.lines.size() -1, lastCodeLine)
                    activityResult.eachLine {parsedMethod.lines << it}
                    parsedMethod << "}"
                }
            }
        }

        fileParser.lines.addAll(0, variables)
        fileParser.firstMethod += variables.size()

    }
}
