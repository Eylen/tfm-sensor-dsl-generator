package com.eylen.sensordsl.generator.utils.parser

import groovy.transform.ToString

@ToString(excludes = ["lines"])
class ParsedMethod {
    String methodName
    int startPosition
    int endPosition

    List<String> lines

    public ParsedMethod(){
        this.lines = new ArrayList<>()
    }
}
