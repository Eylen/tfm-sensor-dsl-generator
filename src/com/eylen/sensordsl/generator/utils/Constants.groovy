package com.eylen.sensordsl.generator.utils

/**
 * Created by Saioa on 29/08/2015.
 */
class Constants {
    public static String TEMPLATES = "/templates"
    public static String GEN_PATH = "src/"
    public static String SRC_PATH = "pre-src/"

    public static String ADD_METHOD_TAG = "@@SensorDSL-AddMethod:"
//    public static String METHOD_NAME_ARGS_TAG = "@@SensorDSL-Arguments:"

    public class Android {
        public static String classStart = "{"
        public static String classEnd = "}"
        public static String codeFileExtension = ".*"
        public static String importStatement = "import "
    }
    public class IOS{
        public static String classStart = "@implementation"
        public static String classEnd = "@end"
        public static String codeFileExtension = ".m"
        public static String importStatement = "#import "
    }
}
