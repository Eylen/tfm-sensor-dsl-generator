package com.eylen.sensordsl.generator.utils.parser

import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.FileParserUtils


class FileParser {
    Map<String, ParsedMethod> methods
    String scriptFile
    String className
    int classStart
    int classEnd
    int firstMethod
    int importsStart

    String beforeInnerClass
    List<String> lines
    Map<String, String> newClasses
    List<String> imports

    Map<String, String> specialFiles

    public FileParser(File file){
        String scriptFile = file.readLines().join("\n")
        this.className = file.name.substring(0, file.name.lastIndexOf("."))
        this.methods = new HashMap<>()
        this.classEnd = -1
        this.classStart = -1
        this.importsStart = -1
        this.scriptFile = scriptFile
        this.firstMethod = -1
        this.lines = new ArrayList<>()
        this.newClasses = new HashMap<>()
        this.imports = new ArrayList<>()
        this.specialFiles = new HashMap<>()
    }

    public void parseFile(Platform platform){

        classStart = FileParserUtils.getClassStart(platform, className, scriptFile)
        classEnd = scriptFile.lastIndexOf(FileParserUtils.getClassEnd(platform))
        importsStart = scriptFile.indexOf(FileParserUtils.getImportStatement(platform))

        beforeInnerClass = scriptFile.substring(0, classStart)

        if (classStart==-1 || classEnd == -1){
            //TODO cambiar los errores
            throw new Exception("The given file is not a class.")
        }
        String innerClass = scriptFile.substring(classStart + 1, classEnd)

        boolean inMethod = false
        boolean inComment = false
        ParsedMethod parsedMethod
        int openedBrackets = 0
        int lineIndex = 1
        int methodStart
        innerClass.eachLine{String line->
            if (!inMethod && line.trim().startsWith("/*")){
                inComment = true
            }

            if (!inMethod && isLineComment(line)){
                lines << line
            } else {
                if (!inMethod && !inComment && line.indexOf("{") != -1) { //new method!
                    if (this.firstMethod == -1) {
                        this.firstMethod = lineIndex
                    }
                    inMethod = true
                    parsedMethod = new ParsedMethod()
                    methodStart = lineIndex
                    parsedMethod.startPosition = line.indexOf("{")
                    parsedMethod.methodName = extractMethodName(line, parsedMethod.startPosition)
                    if (isSingleLineMethod(line)) {
                        closeMethod(parsedMethod, line, lineIndex, methodStart)
                        inMethod = false
                        openedBrackets = 0
                    } else {
                        openedBrackets = line.count("{") - line.count("}")
                        if (parsedMethod && openedBrackets == 0) {
                            closeMethod(parsedMethod, line, lineIndex, methodStart)
                            inMethod = false
                        }
                    }
                    parsedMethod.lines << line
                    methods[parsedMethod.methodName] = parsedMethod

                    //Add a false line for the method
                    lines << "${Constants.ADD_METHOD_TAG}${parsedMethod.methodName}"
                } else if (inMethod) {
                    parsedMethod.lines << line
                    openedBrackets = openedBrackets + line.count("{") - line.count("}")
                    if (parsedMethod && openedBrackets == 0) {
                        closeMethod(parsedMethod, line, lineIndex, methodStart)
                        inMethod = false
                    }
                } else {
                    lines << line
                }
            }

            if (inComment){
                if (line.trim().endsWith("*/")){
                    inComment = false
                }
            }

            lineIndex++
        }
//        println lines
//        println methods
    }

    private String extractMethodName(String line, int methodStart) {
        String lineAux = line.substring(0, methodStart).trim()
        if (lineAux.indexOf("{") == -1){
            return lineAux
        } else {
            lineAux.substring(lineAux.lastIndexOf("{")).trim()
        }
    }

    private boolean isSingleLineMethod(String line){
        return line.count("{")==line.count("}")
    }

    private void closeMethod(ParsedMethod parsedMethod, String line, int lineIndex, int methodStart){
        parsedMethod.endPosition = line.lastIndexOf("}")
    }

    private boolean isLineComment(String line){
        return line.startsWith("//")
    }
}
