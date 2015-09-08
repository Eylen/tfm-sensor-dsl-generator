package com.eylen.sensordsl.generator.helpers

import com.eylen.sensordsl.enums.LocationPriorityType
import com.eylen.sensordsl.enums.TrackType

class LocationGeneratorHelper {
    String startMethod
    String stopMethod
    String callbackMethod
    TrackType trackType

    int interval
    int fastestInterval
    LocationPriorityType priorityType
}
