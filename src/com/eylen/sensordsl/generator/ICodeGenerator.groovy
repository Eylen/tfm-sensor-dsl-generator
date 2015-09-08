package com.eylen.sensordsl.generator

import com.eylen.sensordsl.handlers.AbstractHandler

interface ICodeGenerator<T extends AbstractHandler> {
    public void generateCode(List<T> handlers)
}