package com.eylen.sensordsl.generator.camera

import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.parser.FileParser

class CameraCodeGeneratorFactory {

    public static CameraCodeGenerator newInstance(Platform platform, FileParser fileParser){
        switch (platform){
            case Platform.ANDROID:
                return new AndroidCameraCodeGenerator(fileParser)
            case Platform.IOS:
                return new IOSCameraCodeGenerator(fileParser)
        }
    }
}
