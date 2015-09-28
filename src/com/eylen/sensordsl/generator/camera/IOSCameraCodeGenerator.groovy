package com.eylen.sensordsl.generator.camera

import com.eylen.sensordsl.enums.MediaType
import com.eylen.sensordsl.generator.helpers.CameraGeneratorHelper
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

class IOSCameraCodeGenerator extends CameraCodeGenerator {
    private SimpleTemplateEngine templateEngine

    IOSCameraCodeGenerator(FileParser fileParser) {
        super(fileParser)
        this.templateEngine = new SimpleTemplateEngine()
    }

    @Override
    protected void makeGeneration() {
        String templateDirPath = Constants.TEMPLATES + "/ios/camera/"
        Template startMethodTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "camera_method.template")))
        Template successTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "success_capture.template")))
        Template cancelTemplate = templateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "cancel_capture.template")))

        //TODO imports¿
        ParsedMethod parsedMethod
        String methodLines
        boolean isVideo = false, isCamera = false
        List<String> lines = new ArrayList<>()

        List<String> cameraSuccessMethods = new ArrayList<>()
        List<String> videoSuccessMethods = new ArrayList<>()
        List<String> cameraCancelMethods = new ArrayList<>()
        List<String> videoCancelMethods = new ArrayList<>()
        helpers.each { CameraGeneratorHelper helper ->
            if (helper.mediaType.equals(MediaType.VIDEO)){
                isVideo = true
                if (helper.successCallbackMethod) videoSuccessMethods << helper.successCallbackMethod
                if (helper.cancelCallbackMethod) videoCancelMethods << helper.cancelCallbackMethod
            } else {
                isCamera = true
                if (helper.successCallbackMethod) cameraSuccessMethods << helper.successCallbackMethod
                if (helper.cancelCallbackMethod) cameraCancelMethods << helper.cancelCallbackMethod
            }
            parsedMethod = GeneratorUtils.methodExists(fileParser, helper.methodName, false)
            if (!parsedMethod) parsedMethod = new ParsedMethod()
            methodLines = startMethodTemplate.make(video:isVideo, methodName:helper.methodName).toString()
            lines.clear()
            methodLines.eachLine {lines << it}
            GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, !parsedMethod.methodName, helper.methodName, lines, fileParser)
        }

        methodLines = successTemplate.make(cameraSuccessCallbacks:cameraSuccessMethods, videoSuccessCallbacks:videoSuccessMethods).toString()
        parsedMethod = new ParsedMethod()
        lines.clear()
        methodLines.eachLine {lines << it}
        GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, "imagePickerController", lines, fileParser)

        methodLines = cancelTemplate.make(cameraCancelCallbacks:cameraCancelMethods, videoCancelCallbacks:videoCancelMethods).toString()
        parsedMethod = new ParsedMethod()
        lines.clear()
        methodLines.eachLine {lines << it}
        GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, true, "imagePickerControllerDidCancel", lines, fileParser)
    }
}
