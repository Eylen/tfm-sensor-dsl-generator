package com.eylen.sensordsl.generator.motion

import com.eylen.sensordsl.enums.SensorType
import com.eylen.sensordsl.generator.ICodeGenerator
import com.eylen.sensordsl.generator.helpers.MotionSensorsGeneratorHelper
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.handlers.MotionSensorHandler

abstract class MotionSensorsCodeGenerator implements ICodeGenerator<MotionSensorHandler>{
    protected FileParser fileParser
    protected MotionSensorsGeneratorHelper foregroundHelper
    protected MotionSensorsGeneratorHelper backgroundHelper

    public MotionSensorsCodeGenerator(FileParser fileParser){
        this.fileParser = fileParser
    }

    public void generateCode(List<MotionSensorHandler> handlers){
        prepareGeneration(handlers)
        makeGeneration()
    }

    private void prepareGeneration(List<MotionSensorHandler> handlers){
        MotionSensorsGeneratorHelper helper
        handlers.each {MotionSensorHandler handler->
            if (handler.trackBackground){
                if (!backgroundHelper){
                    backgroundHelper = new MotionSensorsGeneratorHelper(background: true)
                }
                helper = backgroundHelper
            } else {
                if (!foregroundHelper){
                    foregroundHelper = new MotionSensorsGeneratorHelper(background: false)
                }
                helper = foregroundHelper
            }
            switch (handler.sensorType){
                case SensorType.ACCELEROMETER:
                    helper.accelerometer = true
                    helper.accelerometerCallback = handler.callbackName
                    break
                case SensorType.GYROSCOPE:
                    helper.gyroscope = true
                    helper.gyroscopeCallback = handler.callbackName
                    break
            }
        }
    }

    protected abstract void makeGeneration();
}
