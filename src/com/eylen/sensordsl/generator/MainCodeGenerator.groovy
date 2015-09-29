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

/**
 * The Main code generator.
 * This generator will call the rest of generators available
 */
class MainCodeGenerator {
    private SensorDSL sensorDSL
    private File codeFile
    private String pathToFile
    private File destDir
    private Logger log

    /**
     * Constructor
     * @param sensorDSL     SensorDSL   the executed SensorDSL
     * @param codeFile      File        the code file where changes should be applied
     * @param pathToFile    String      the path to the destination code file
     * @param destDir       File        the destination directory
     */
    public MainCodeGenerator(SensorDSL sensorDSL, File codeFile, String pathToFile, File destDir){
        this.sensorDSL = sensorDSL
        this.codeFile = codeFile
        this.pathToFile = pathToFile
        this.destDir = destDir
        log = LogManager.getLogger(MainCodeGenerator.class)
    }

    /**
     * Makes the code generation for the designed platform
     * @param platform  Platform    the platform
     */
    public void generateCode(Platform platform){
        FileParser fileParser = new FileParser(codeFile)
        fileParser.parseFile(platform)
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
        if (fileParser.newClasses.size() > 0){
            fileParser.newClasses.each{String fileName, String classString->
                out = new File(fullDestDir.absolutePath+"/${fileName}").newWriter(false)
                out.write classString
                out.flush()
                out.close()
            }
        }
    }

    /**
     * Detect if the line is a especial line that indicates that a method should be injected
     * @param line  String  the line
     * @return  true if it's a special line, false otherwise
     */
    private static boolean isAddMethodTag(String line){
        line.indexOf(Constants.ADD_METHOD_TAG) != -1
    }

    /**
     * Extract the method name from the special line used to indicate that the method lines of code should be injected
     * @param line  String  the line
     * @return  the method name
     */
    private static String getMethodNameFromTag(String line){
        line.substring(line.indexOf(Constants.ADD_METHOD_TAG) + Constants.ADD_METHOD_TAG.length())
    }
}
