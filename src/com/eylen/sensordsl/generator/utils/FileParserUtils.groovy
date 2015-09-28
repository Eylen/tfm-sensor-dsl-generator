package com.eylen.sensordsl.generator.utils

import com.eylen.sensordsl.generator.enums.Platform

/**
 * Created by Saioa on 17/09/2015.
 */
class FileParserUtils {
    private static getPlatformProperty(Platform platform, String property){
        switch (platform){
            case Platform.ANDROID:
                return Constants.Android[property]
                break
            case Platform.IOS:
                return Constants.IOS[property]
                break
        }
    }

    public static int getClassStart(Platform platform, String fileName, String scriptFile){
        String startIndex = getPlatformProperty(platform, "classStart")
        switch (platform){
            case Platform.ANDROID:
                return scriptFile.indexOf(startIndex)
            case Platform.IOS:
                String line = startIndex + " " + fileName
                return scriptFile.indexOf(line) + line.length()
        }
    }

    public static String getClassEnd(Platform platform){
        getPlatformProperty(platform, "classEnd")
    }

    public static String getImportStatement(Platform platform){
        getPlatformProperty(platform, "importStatement")
    }

    public static String getCodeFileExtension(Platform platform){
        getPlatformProperty(platform, "codeFileExtension")
    }
}
