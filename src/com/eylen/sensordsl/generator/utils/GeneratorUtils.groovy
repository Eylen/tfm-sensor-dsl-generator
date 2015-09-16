package com.eylen.sensordsl.generator.utils

import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod

class GeneratorUtils {

    public static void createOrUpdateParsedMethod(ParsedMethod parsedMethod, boolean createMethod, String fullName, List<String> lines, FileParser fileParser){
        if (createMethod){
            parsedMethod.methodName = fullName
            parsedMethod.lines.addAll lines
            parsedMethod.startPosition = parsedMethod.lines[0].indexOf("{")
            parsedMethod.endPosition = parsedMethod.lines[parsedMethod.lines.size() - 1].lastIndexOf("}")
            fileParser.lines << Constants.ADD_METHOD_TAG + parsedMethod.methodName
            fileParser.methods[parsedMethod.methodName] = parsedMethod
        } else {
            String lastLine = parsedMethod.lines.last().trim()
            if (lastLine != "}"){
                lastLine = lastLine.substring(0, parsedMethod.endPosition)
                parsedMethod.lines[parsedMethod.lines.size() - 1] = lastLine
            } else {
                parsedMethod.lines.pop()
            }
            parsedMethod.lines.addAll lines
            parsedMethod.lines << "\t}"
        }
    }

    public static ParsedMethod methodExists(FileParser fileParser, String methodName, boolean like){
        if (like){
            fileParser.methods.find{key,value->key.indexOf(" "+methodName+"(")!=-1}?.value
        } else {
            fileParser.methods[methodName]
        }
    }

    public static boolean isPermissionFile(File file, Platform platform){
        return (platform == Platform.ANDROID && file.name == 'AndroidManifest.xml')
    }
}
