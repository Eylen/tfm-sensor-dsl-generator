import com.eylen.sensordsl.Permission
import com.eylen.sensordsl.SensorDSL
import com.eylen.sensordsl.generator.SensorDSLScript
import com.eylen.sensordsl.generator.MainCodeGenerator
import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.permission.PermissionCodeGenerator
import com.eylen.sensordsl.generator.permission.PermissionCodeGeneratorFactory
import com.eylen.sensordsl.generator.utils.Constants
import com.eylen.sensordsl.generator.utils.GeneratorUtils
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

File permissionFile = null
Set<Permission> permissions = new HashSet<>()
// Loop through all files from source directory
sourceDirectory.eachFileRecurse(groovy.io.FileType.FILES) {File it->
    String pathToFile = it.absolutePath - sourceDirectory.absolutePath - it.name

	// Execute Sensor DSL Scripts
    if(it.name.endsWith('.dsl.groovy')) {
        log.info "Parsing DSL script for file: " + it.name

        String fileName = it.name.substring(0, it.name.indexOf(".dsl.groovy"))
        generatedFiles << fileName

        SensorDSL sensorDSL = new SensorDSL()
        binding = new Binding(sensorDSL: sensorDSL, codeFile: null, platform:platform, fileName: null)
        def compilerConfiguration = new CompilerConfiguration()
        compilerConfiguration.scriptBaseClass = SensorDSLScript.class.name //Custom base script
        compilerConfiguration.addCompilationCustomizers(
                new ASTTransformationCustomizer(TypeChecked) //make type checking of the script before execute
        )

        shell = new GroovyShell(this.class.classLoader, binding, compilerConfiguration)
        shell.evaluate(it)

        if (sensorDSL) {
            permissions.addAll sensorDSL.permissionList
            MainCodeGenerator codeGenerator = new MainCodeGenerator(sensorDSL, binding.codeFile, pathToFile, destDirectory)
            codeGenerator.generateCode(platform)
        }
    } else if (GeneratorUtils.isPermissionFile(it, platform)) {
		//if it's a permission file, we store it for later use
       	permissionFile = it
    } else {
		// If it's not a sensorDSL script neither permission file, we just copy it to destination folder
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

// Add permission lines identified during code generation
if (permissionFile){
    log.info "Adding permissions in file: " + permissionFile.name
    PermissionCodeGenerator permissionCodeGenerator = PermissionCodeGeneratorFactory.newInstance(platform)
    List<String> permissionsString = permissionCodeGenerator.generateCode(permissions, permissionFile)

    String pathToFile = permissionFile.absolutePath - sourceDirectory.absolutePath - permissionFile.name
    File fullDestDir = new File(destDirectory.absolutePath + pathToFile)
    if (!fullDestDir.exists()){
        fullDestDir.mkdirs()
    }

    def before = permissionFile.text.substring(0, permissionCodeGenerator.getPermissionsStart(permissionFile))
    def after = permissionFile.text.substring(permissionCodeGenerator.getPermissionsStart(permissionFile))
    def out = new File(fullDestDir.absolutePath+"/"+permissionFile.name).newWriter(false)
    out.write before
    permissionsString.each {
        out.writeLine it
    }
    out.write after
    out.flush()
    out.close()
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
    Platform platform = null
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