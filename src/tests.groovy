import com.eylen.sensordsl.generator.utils.parser.FileParser

String prueba = """
public class Something {
    public void onCreate(Bundle savedInstanceState) {
        //alo alo
        def blablabla = "bubu"
        dalskdalskdjsa()
        for (a=0;aa > 0;aa++) {
        }
        fdsfsdfsd
        if (b < 0){
            fdsfds
            if (fdsf){
            }
        }
    }

    public void otherThing (){
        //blablablabla
    }

    - (void) drawRoundedRect:(NSRect)aRect inView:(NSView *)aView withColor:(NSColor *)color fill:(BOOL)fill {
        objective-c method
    }
    - (void) drawRoundedRect:(NSRect)aRect inView:(NSView *)aView withColor:(NSColor *)color
    {
        objective-c method
    }
}"""


FileParser parser = new FileParser(new File("./com/eylen/sensordsl/generator/utils/parser/FileParser.groovy"))
parser.parseFile()