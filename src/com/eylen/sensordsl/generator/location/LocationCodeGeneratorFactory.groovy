package com.eylen.sensordsl.generator.location

import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.parser.FileParser

/**
 * Created by Saioa on 01/09/2015.
 */
class LocationCodeGeneratorFactory {
    public static LocationCodeGenerator newInstance(Platform platform, FileParser fileParser){
        switch (platform){
            case Platform.ANDROID:
                return new AndroidLocationCodeGenerator(fileParser)
            case Platform.IOS:
                return new IOSLocationCodeGenerator(fileParser)
        }
    }
}
