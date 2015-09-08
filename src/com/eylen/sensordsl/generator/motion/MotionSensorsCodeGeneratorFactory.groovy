package com.eylen.sensordsl.generator.motion

import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.parser.FileParser

/**
 * Created by Saioa on 02/09/2015.
 */
class MotionSensorsCodeGeneratorFactory {
    public static MotionSensorsCodeGenerator newInstance(Platform platform, FileParser fileParser){
        switch (platform){
            case Platform.ANDROID:
                return new AndroidMotionSensorsCodeGenerator(fileParser)
            case Platform.IOS:
                return new IOSMotionSensorsCodeGenerator(fileParser)
        }
    }
}
