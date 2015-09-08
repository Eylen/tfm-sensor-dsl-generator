import com.eylen.sensordsl.SensorDSL
import com.eylen.sensordsl.generator.MainCodeGenerator
import com.eylen.sensordsl.generator.enums.Platform
import com.eylen.sensordsl.generator.utils.Constants
import org.apache.ivy.util.FileUtil

String platform = this.args[0]
String path = this.args[1]
String srcDir, destDir
if (args.length > 2) {
    srcDir = this.args[2]
    destDir = this.args[3]
} else {
    srcDir = Constants.SRC_PATH
    destDir = Constants.GEN_PATH
}
if (!platform || !path) {
    System.err.println "Usage: Generator <ANDROID|IOS> <rootPath> <srcdir>? <destdir>?"
    return
}

try {
    Platform.valueOf(platform.toUpperCase())
} catch (Exception e){
    System.err.println "Only ANDROID and IOS platforms are currently supported"
    return
}

File dir = new File(path + "/"+ srcDir)
if (!dir.exists() || !dir.isDirectory()){
    System.err.println "The source path does not exist or is not a directory. Ensure that you have set the root dir and that there is a folder called pre-src"
    return
}
File destDirectory = new File(path + "/"+destDir)
if (!destDirectory.exists()){
    destDirectory.mkdir()
}
if (destDirectory.exists() && !destDirectory.isDirectory()){
    System.err.println "There is a file with the name src and can't overwrite files"
    return
}

Binding binding = new Binding()
GroovyShell shell = new GroovyShell(binding)

List<String> generatedFiles = new ArrayList<>()
dir.eachFileRecurse(groovy.io.FileType.FILES) {File it->
    String pathToFile = it.absolutePath - dir.absolutePath - it.name
    if(it.name.endsWith('.dsl.groovy')) {
        println "Parsing dsl of file-->" + it.name
        String fileName = it.name.substring(0, it.name.indexOf(".dsl.groovy"))
        generatedFiles << fileName
        def codeFiles = new FileNameFinder().getFileNames(it.parent, fileName+".*", it.name)
        File codeFile = null
        if (codeFiles.size() > 0){
            codeFile = new File(codeFiles[0])
        }

        Script dslScript = shell.parse(it)
        SensorDSL sensorDSL = (SensorDSL) dslScript.run()
        if (sensorDSL){
            MainCodeGenerator codeGenerator = new MainCodeGenerator(sensorDSL, codeFile, pathToFile, destDirectory)
            codeGenerator.generateCode(Platform.ANDROID)
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