package com.eylen.sensordsl.generator.ios

import com.eylen.sensordsl.SensorDSL
import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
import com.eylen.sensordsl.generator.utils.parser.FileParser
import com.eylen.sensordsl.generator.utils.parser.ParsedMethod
import groovy.text.SimpleTemplateEngine
import groovy.text.Template

class AppDelegateGenerator {
	private SensorDSL sensorDSL
	private SimpleTemplateEngine simpleTemplateEngine

	private File srcDir
	private File destDir

	public AppDelegateGenerator(SensorDSL sensorDSL, File srcDir, File destDir){
		simpleTemplateEngine = new SimpleTemplateEngine()
		this.sensorDSL = sensorDSL
		this.srcDir = srcDir
		this.destDir = destDir
	}

	public void generate(){
		def appDelegateFile = new FileNameFinder().getFileNames(srcDir.absolutePath, "*AppDelegate.m", "")
		println appDelegateFile
		FileParser fileParser = new FileParser(new File(appDelegateFile[0]))
		fileParser.parseFile(Platform.IOS)
		def appDelegateInterface = new FileNameFinder().getFileNames(srcDir.absolutePath, "*AppDelegate.h", "")
		FileParser fileParserInterface = new FileParser(new File(appDelegateInterface[0]))
		fileParserInterface.parseFile(Platform.IOS)
		println fileParserInterface.methods

		if (sensorDSL.motionSensorHandlers?.size() > 0){
			String templateDirPath = Constants.TEMPLATES + "/ios/motion/"
			//TODO en AppDelegate podría ser cualquier clase...
			Template delegateMotionManagerTemplate = simpleTemplateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "appdelegate/cmotionmanager_declaration.template")))
			Template delegateMotionManagerMethodTemplate = simpleTemplateEngine.createTemplate(new InputStreamReader(getClass().getResourceAsStream(templateDirPath + "appdelegate/cmotionmanager_method.template")))



			List<String> appDelegateLines = new ArrayList<>()
			String appContent = delegateMotionManagerTemplate.make().toString()
			appContent = delegateMotionManagerMethodTemplate.make().toString()
			ParsedMethod parsedMethod = GeneratorUtils.methodExists(fileParser, "(CMMotionManager *)motionManager", false)
			boolean createMethod = !parsedMethod
			if (!parsedMethod) parsedMethod = new ParsedMethod()
			List<String> lines = new ArrayList<>()
			appContent.eachLine {lines << it}
			GeneratorUtils.createOrUpdateParsedMethod(parsedMethod, createMethod, "(CMMotionManager *)motionManager", lines, fileParser)
			println fileParser.methods

			def out = new File(destDir.absolutePath+"/"+new File(appDelegateFile[0]).name).newWriter(false)
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
//						log.warn("No method found for ${getMethodNameFromTag(it)}")
					} else {
						method.lines.each {String methodLine-> out.writeLine methodLine}
					}
				} else {
					out.writeLine it
				}
			}
			out.writeLine fileParser.scriptFile.substring(fileParser.classEnd)
			out.flush()
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
