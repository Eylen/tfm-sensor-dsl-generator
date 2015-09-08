package com.eylen.sensordsl.generator.helpers

import com.eylen.sensordsl.enums.SensorType
import groovy.transform.TupleConstructor

@TupleConstructor
class MotionSensorsGeneratorHelper {
    boolean background = false
    boolean gyroscope = false
    boolean accelerometer = false
    String gyroscopeCallback
    String accelerometerCallback
}
