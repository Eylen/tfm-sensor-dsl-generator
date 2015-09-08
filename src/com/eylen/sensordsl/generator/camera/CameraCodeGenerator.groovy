package com.eylen.sensordsl.generator.camera
import com.eylen.sensordsl.enums.MediaType
import com.eylen.sensordsl.generator.ICodeGenerator
import com.eylen.sensordsl.generator.helpers.CameraGeneratorHelper
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.handlers.CameraHandler

abstract class CameraCodeGenerator implements ICodeGenerator<CameraHandler> {
    protected FileParser fileParser

    protected String defaultCameraMethodName = "startCamera"
    protected String defaultVideoMethodName = "startVideo"

    protected String defaultCameraRequestCodeName = "CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE"
    protected String defaultVideoRequestCodeName = "CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE"
    protected int defaultCameraRequestCodeValue = 100
    protected int defaultVideoRequestCodeValue = 200

    protected List<CameraGeneratorHelper> helpers

    public CameraCodeGenerator(FileParser fileParser){
        this.fileParser = fileParser
        this.helpers = new ArrayList<>()
    }

    public void generateCode(List<CameraHandler> handlers){
        prepareGeneration(handlers)
        makeGeneration()
    }

    private void prepareGeneration(List<CameraHandler> handlers){
        int cameraCounter = 0
        int videoCounter = 0
        handlers.each {CameraHandler handler->
            CameraGeneratorHelper helper = new CameraGeneratorHelper(mediaType:handler.mediaType, fileUri: handler.path)
            if (handler.callbackHandler){
                helper.successCallbackMethod = handler.callbackHandler.successCallback
                helper.cancelCallbackMethod = handler.callbackHandler.cancelCallback
                helper.errorCallbackMethod = handler.callbackHandler.errorCallback
            }

            if (helper.mediaType == MediaType.PICTURE) {
                String counter = (cameraCounter?:"")
                if (!handler.methodName) helper.methodName = defaultCameraMethodName + counter
                else helper.methodName = handler.methodName
                helper.requestCodeName = defaultCameraRequestCodeName + counter
                helper.requestCodeValue = defaultCameraRequestCodeValue + cameraCounter
                cameraCounter++
            }
            else if (helper.mediaType == MediaType.VIDEO) {
                String counter = (videoCounter?:"")
                if (!handler.methodName) helper.methodName = defaultVideoMethodName + counter
                else helper.methodName = handler.methodName
                helper.requestCodeName = defaultVideoRequestCodeName + counter
                helper.requestCodeValue = defaultVideoRequestCodeValue + videoCounter
                if (handler.videoProperties){
                    helper.videoQuality = handler.videoProperties.qualityValue
                    helper.videoSizeLimit = handler.videoProperties.sizeLimit
                    helper.videoDurationLimit = handler.videoProperties.durationLimit
                }
                videoCounter++
            }
            helpers << helper
        }
    }

    protected abstract void makeGeneration();
}
