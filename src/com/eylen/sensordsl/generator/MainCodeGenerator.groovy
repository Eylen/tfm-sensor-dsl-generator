package com.eylen.sensordsl.generator

import com.eylen.sensordsl.SensorDSL
import com.eylen.sensordsl.generator.camera.CameraCodeGenerator
import com.eylen.sensordsl.generator.camera.CameraCodeGeneratorFactory
import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.location.LocationCodeGenerator
import com.eylen.sensordsl.generator.location.LocationCodeGeneratorFactory
import com.eylen.sensordsl.generator.motion.MotionSensorsCodeGenerator
import com.eylen.sensordsl.generator.motion.MotionSensorsCodeGeneratorFactory
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class MainCodeGenerator {
    private SensorDSL sensorDSL
    private File codeFile
    private String pathToFile
    private File destDir
    private Logger log

    public MainCodeGenerator(SensorDSL sensorDSL, File codeFile, String pathToFile, File destDir){
        this.sensorDSL = sensorDSL
        this.codeFile = codeFile
        this.pathToFile = pathToFile
        this.destDir = destDir
        log = LogManager.getLogger(MainCodeGenerator.class)
    }

    public void generateCode(Platform platform){
        FileParser fileParser = new FileParser(codeFile)
        fileParser.parseFile()
        if (!codeFile){
            throw new Exception("A codeFile with the same name must exist to be able to generate code")
        }
        CameraCodeGenerator cameraCodeGenerator = CameraCodeGeneratorFactory.newInstance(platform, fileParser)
        cameraCodeGenerator.generateCode(sensorDSL.cameraHandlers)
        MotionSensorsCodeGenerator motionCodeGenerator = MotionSensorsCodeGeneratorFactory.newInstance(platform, fileParser)
        motionCodeGenerator.generateCode(sensorDSL.motionSensorHandlers)
        LocationCodeGenerator locationCodeGenerator = LocationCodeGeneratorFactory.newInstance(platform, fileParser)
        locationCodeGenerator.generateCode(sensorDSL.locationHandlers)

        File fullDestDir = new File(destDir.absolutePath + pathToFile)
        if (!fullDestDir.exists()){
            fullDestDir.mkdirs()
        }

        def out = new File(fullDestDir.absolutePath+"/"+codeFile.name).newWriter(false)
        String beforeImports = fileParser.scriptFile.substring(0, fileParser.importsStart)
        String afterImports = fileParser.scriptFile.substring(fileParser.importsStart, fileParser.classStart + 1)
//        out.writeLine fileParser.scriptFile.substring(0, fileParser.classStart + 1)
        out.write beforeImports
        fileParser.imports.each {
            out.writeLine it
        }
        out.write afterImports
        fileParser.lines.each {
            if (isAddMethodTag(it)){
                ParsedMethod method = fileParser.methods[getMethodNameFromTag(it)]
                if (!method){
                    log.warn("No method found for ${getMethodNameFromTag(it)}")
//                    throw new Exception("Errrrrrooooooooooooooooooooooooooooor")
                } else {
                    method.lines.each {String methodLine-> out.writeLine methodLine}
                }
            } else {
                out.writeLine it
            }
        }
        out.writeLine fileParser.scriptFile.substring(fileParser.classEnd)
        out.flush()

        //Create new classes if needed
        //TODO crear paquetes si tiene
        //TODO add package declaration
        if (fileParser.newClasses.size() > 0){
            fileParser.newClasses.each{String fileName, String classString->
                out = new File(fullDestDir.absolutePath+"/${fileName}").newWriter(false)
                out.write classString
                out.flush()
                out.close()
            }
        }
    }

    private static boolean isAddMethodTag(String line){
        line.indexOf(Constants.ADD_METHOD_TAG) != -1
    }

    private static String getMethodNameFromTag(String line){
        line.substring(line.indexOf(Constants.ADD_METHOD_TAG) + Constants.ADD_METHOD_TAG.length())
    }
}
