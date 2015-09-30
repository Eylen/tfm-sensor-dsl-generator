package com.eylen.sensordsl.generator.utils

import com.eylen.sensordsl.generator.enums.Platform

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

    public static int getClassStart(Platform platform, String fileName, String scriptFile, String fileExtension){
        String startIndex = getPlatformProperty(platform, "classStart")
        switch (platform){
            case Platform.ANDROID:
                return scriptFile.indexOf(startIndex)
            case Platform.IOS:
                if (fileExtension == ".m"){
                    String line = startIndex + " " + fileName
                    return scriptFile.indexOf(line) + line.length()
                } else {
                    return 0
                }

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
