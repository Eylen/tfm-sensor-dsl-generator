package com.eylen.sensordsl.generator.permission

import com.eylen.sensordsl.Permission
import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.motion.AndroidMotionSensorsCodeGenerator
import com.eylen.sensordsl.generator.motion.IOSMotionSensorsCodeGenerator
import com.eylen.sensordsl.generator.motion.MotionSensorsCodeGenerator
import com.eylen.sensordsl.generator.utils.parser.FileParser

/**
 * Created by Saioa on 16/09/2015.
 */
class PermissionCodeGeneratorFactory {
    public static PermissionCodeGenerator newInstance(Platform platform){
        switch (platform){
            case Platform.ANDROID:
                return new AndroidPermissionCodeGenerator()
            case Platform.IOS:
                return new IOSPermissionCodeGenerator()
        }
    }
}
