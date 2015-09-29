package com.eylen.sensordsl.generator

import com.eylen.sensordsl.handlers.AbstractHandler

/**
 * Abstract class that all sensor generator should implement
 * @param < T > extends AbstractHandler
 */
interface ICodeGenerator<T extends AbstractHandler> {
    /**
     * The method that will make the code generation
     * @param handlers  List<T> an array of handlers needed for code generation
     */
    public void generateCode(List<T> handlers)
}