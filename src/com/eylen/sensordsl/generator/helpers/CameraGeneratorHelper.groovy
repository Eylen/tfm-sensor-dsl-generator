package com.eylen.sensordsl.generator.helpers

import com.eylen.sensordsl.enums.MediaType
import groovy.transform.TupleConstructor

@TupleConstructor
class CameraGeneratorHelper {
    String methodName
    MediaType mediaType
    String fileUri
    String requestCodeName
    String requestCodeValue

    String successCallbackMethod
    String cancelCallbackMethod
    String errorCallbackMethod

    Integer videoQuality
    Integer videoDurationLimit
    Integer videoSizeLimit
}
