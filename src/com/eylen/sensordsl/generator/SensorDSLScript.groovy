package com.eylen.sensordsl.generator

import com.eylen.sensordsl.SensorDSL
import com.eylen.sensordsl.generator.utils.FileParserUtils
import com.eylen.sensordsl.handlers.CameraHandler
import com.eylen.sensordsl.handlers.LocationHandler
import com.eylen.sensordsl.handlers.MotionSensorHandler

/**
 * The Base Script class for SensorDSL scripts
 */
abstract class SensorDSLScript extends Script{

    private SensorDSL sensorDSL

    /**
     * The script body
     */
    abstract void scriptBody()

    @Override
    def run(){
        this.sensorDSL = this.binding.sensorDSL
        File fileFile = new File(getClass().protectionDomain.codeSource.location.path)
        String dirPath = fileFile.parent
        sensorDSL.scriptFile = fileFile.absolutePath
        String fullName = sensorDSL.scriptFile - dirPath

        String fileName
        if (fullName.startsWith("/") || fullName.startsWith("\\"))
            fullName = fullName.substring(1)
        fileName = fullName.substring(0, fullName.indexOf(".dsl.groovy"))
        def codeFiles = new FileNameByRegexFinder().getFileNames(dirPath, fileName+ FileParserUtils.getCodeFileExtension(this.binding.platform), fullName)
        if (codeFiles.size() > 0){
            this.binding.codeFile = new File(codeFiles[0])
        }
        scriptBody()
    }

    def camera(@DelegatesTo(CameraHandler) Closure closure){
        sensorDSL.camera(closure)
    }

    def accelerometer(@DelegatesTo(MotionSensorHandler) Closure closure){
        sensorDSL.accelerometer(closure)
    }

    def gyroscope(@DelegatesTo(MotionSensorHandler) Closure closure){
        sensorDSL.gyroscope(closure)
    }

    def location(@DelegatesTo(LocationHandler) Closure closure){
        sensorDSL.location(closure)
    }
}
