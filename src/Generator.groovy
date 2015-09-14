import com.eylen.sensordsl.SensorDSL
import com.eylen.sensordsl.SensorDSLScript
import com.eylen.sensordsl.generator.MainCodeGenerator
import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.Constants
import groovy.transform.Field
import groovy.transform.TypeChecked
import org.apache.ivy.util.FileUtil
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer

//Let log be for the entire script
@Field Logger log = LogManager.getLogger(Generator.class)

//Retrieve params and validate
Platform platform
File sourceDirectory, destDirectory
(sourceDirectory, destDirectory, platform) = parseScriptArguments(args)


Binding binding = new Binding()
GroovyShell shell = new GroovyShell(binding)

log.info "Starting Generator for platform ${platform}"
log.info new Date()
log.info "-------------------------------------------------------------------------------------------------------------"
List<String> generatedFiles = new ArrayList<>()
sourceDirectory.eachFileRecurse(groovy.io.FileType.FILES) {File it->
    String pathToFile = it.absolutePath - sourceDirectory.absolutePath - it.name
    if(it.name.endsWith('.dsl.groovy')) {
        log.info "Parsing DSL script for file: " + it.name

        String fileName = it.name.substring(0, it.name.indexOf(".dsl.groovy"))
        generatedFiles << fileName
        /*def codeFiles = new FileNameFinder().getFileNames(it.parent, fileName+".*", it.name)
        File codeFile = null
        if (codeFiles.size() > 0){
            codeFile = new File(codeFiles[0])
        }*/
        SensorDSL sensorDSL = new SensorDSL()
        binding = new Binding(sensorDSL:sensorDSL)
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = SensorDSLScript.class.name
        compilerConfiguration.addCompilationCustomizers(
                new ASTTransformationCustomizer(TypeChecked)
        )

        shell = new GroovyShell(this.class.classLoader, binding, compilerConfiguration)
        shell.evaluate(it)

        if (sensorDSL){
            MainCodeGenerator codeGenerator = new MainCodeGenerator(sensorDSL, binding.codeFile, pathToFile, destDirectory)
            codeGenerator.generateCode(platform)
        }
    } else {
        String fileNameWoExtension = it.name.substring(0, it.name.lastIndexOf("."))
        if (!generatedFiles.contains(fileNameWoExtension)) {
            File fullDestDir = new File(destDirectory.absolutePath + pathToFile)
            if (!fullDestDir.exists()) {
                fullDestDir.mkdirs()
            }
            FileUtil.copy(it, new File(fullDestDir.absolutePath + "/" + it.name), null, true)
        }
    }
}
log.info "-------------------------------------------------------------------------------------------------------------"
log.info "Generator ended"
log.info new Date()
/**
 * Parse parameters and check validity. If some of them are invalid, an error is thrown and the generator exists
 * @param args  script arguments
 * @return  src directory, destination directory and platform
 */
private def parseScriptArguments(args){
    String platformArg = args[0]
    String path = args[1]
    String srcDir, destDir
    if (args.length > 2) {
        srcDir = args[2]
        destDir = args[3]
    } else {
        srcDir = Constants.SRC_PATH
        destDir = Constants.GEN_PATH
    }
    if (!platformArg || !path) {
        log.error "Usage: Generator <ANDROID|IOS> <rootPath> <srcdir>? <destdir>?"
        System.exit(-1)
    }
    Platform platform
    try {
        platform = Platform.valueOf(platformArg.toUpperCase())
    } catch (Exception e){
        log.error "Only ANDROID and IOS platforms are currently supported"
        System.exit(-1)
    }

    File dir = new File(path + "/"+ srcDir)
    if (!dir.exists() || !dir.isDirectory()){
        log.error "The source path does not exist or is not a directory. Ensure that you have set the root dir and that there is a folder called pre-src"
        System.exit(-1)
    }
    File destDirectory = new File(path + "/"+destDir)
    if (!destDirectory.exists()){
        destDirectory.mkdir()
    }
    if (destDirectory.exists() && !destDirectory.isDirectory()){
        log.error "There is a file with the name src and can't overwrite files"
        System.exit(-1)
    }

    return [dir, destDirectory, platform]
}