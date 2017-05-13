import java.io.Writer;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class Utils {

    public static final String charSet =
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";
    public static final String tableStyle =
            "<style>table,th,td { border:1px solid black; border-collapse:collapse; }</style>";

    public static String makeHttpHeader( String title, boolean cfgBorder ) {
        return "<html>\r\n<head>" + charSet + ( cfgBorder ? tableStyle : "" ) +
                "<title>" + title + "</title></head>\r\n<body>\r\n";
    }

    public static Writer openOutputFile( String fileName ) {
        try {
            return new OutputStreamWriter( new FileOutputStream( fileName ), "UTF-8" );
        } catch( Exception e ) {
            System.err.println( "Error writing to " + fileName + ": " + e );
        }
        return null;
    }

    public static void closeWriter( Writer writer ) {
        if( writer == null ) return;
        try {
            writer.close();
        } catch( Exception e ) {}
    }
}
