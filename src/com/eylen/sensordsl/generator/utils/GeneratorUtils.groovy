package com.eylen.sensordsl.generator.utils

import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod

class GeneratorUtils {
    /**
     * Adds or updates the parsedMethod to the parsed code file.
     * @param parsedMethod  ParsedMethod    the parsedMethod to add or update
     * @param createMethod  boolean         if the method must be added or updates
     * @param fullName      String          full method name (with arguments)
     * @param lines         List<String>    an array with the method lines
     * @param fileParser    FileParser      the parsed code file
     */
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

    /**
     * Checks if a method exists in the parsed code file
     * @param fileParser    FileParser  the parsed file
     * @param methodName    String      the method name to search for
     * @param like          boolean     if a 'like' search should be performed
     * @return  the corresponding ParsedMethod if it's found or null if not
     */
    public static ParsedMethod methodExists(FileParser fileParser, String methodName, boolean like){
        if (like){
            fileParser.methods.find{key,value->key.indexOf(" "+methodName+"(")!=-1}?.value
        } else {
            fileParser.methods[methodName]
        }
    }

    /**
     * Check if the file is the permission file for the OS
     * @param file      File        The file to check
     * @param platform  Platform    Destination Platform
     * @return  true if it's a permission file
     */
    public static boolean isPermissionFile(File file, Platform platform){
        return (platform == Platform.ANDROID && file.name == 'AndroidManifest.xml') || (platform == Platform.IOS && file.name == 'Info.plist')
    }
}
