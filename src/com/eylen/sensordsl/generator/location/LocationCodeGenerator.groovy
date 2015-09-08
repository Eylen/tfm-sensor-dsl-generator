package com.eylen.sensordsl.generator.location

import com.eylen.sensordsl.enums.LocationPriorityType
import com.eylen.sensordsl.generator.ICodeGenerator
import com.eylen.sensordsl.generator.helpers.LastLocationGeneratorHelper
import com.eylen.sensordsl.generator.helpers.LocationGeneratorHelper
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.handlers.LocationHandler

abstract class LocationCodeGenerator implements ICodeGenerator<LocationHandler>{
    protected LastLocationGeneratorHelper lastHelper
    protected LocationGeneratorHelper trackerHelper
    protected FileParser fileParser

    public LocationCodeGenerator(FileParser fileParser){
        this.fileParser = fileParser
    }

    @Override
    public void generateCode(List<LocationHandler> handlers){
        prepareGeneration(handlers)
        makeGeneration()
    }

    public void prepareGeneration(List<LocationHandler> handlers){
        //TODO versión con pendingintent
        handlers.each {LocationHandler handler->
            if (handler.lastKnownLocationHandler){
                if (!lastHelper) lastHelper = new LastLocationGeneratorHelper()
                lastHelper.lastLocationCallback = handler.lastKnownLocationHandler.varName
            }
            if (handler.locationTrackingHandler){
                def lastHandler = handler.locationTrackingHandler
                if (!trackerHelper) trackerHelper = new LocationGeneratorHelper()
                trackerHelper.callbackMethod = lastHandler.callbackMethod
                trackerHelper.startMethod = lastHandler.startMethod
                trackerHelper.stopMethod = lastHandler.stopMethod
                trackerHelper.interval = lastHandler.locationTrackingProperties?.interval?:60*1000
                trackerHelper.fastestInterval = lastHandler.locationTrackingProperties?.fastestInterval?:10*1000
                trackerHelper.priorityType = lastHandler.locationTrackingProperties?.priorityType?:LocationPriorityType.BALANCED
            }
        }
    }

    public abstract void makeGeneration();
}
