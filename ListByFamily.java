import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

public class ListByFamily {

    public static void display( Parser parser, String outFile ) {
    	String[][] grid = new String[parser.generations + 1][parser.persons.size()];
    	int lines = 0;
        for( int r = 0; r < parser.roots.size(); ++r ) {
            if( lines > 0 ) lines += 2;
            lines = _fill( grid, parser.roots.get( r ), lines );
        }

        Writer out = Utils.openOutputFile( outFile );
        final String title = "Family of " + parser.roots.firstElement().name;
        try {
            out.write( Utils.makeHttpHeader( title, true ) );
            out.write( "<table>" );
            for( int g = 0; g < parser.generations; ++g ) {
                out.write( "<col width=160>" );
            }
            out.write( "\r\n" );
            for( int i = 0; i <= lines; ++i ) {
                out.write( "<tr>\r\n" );
                for( int j = 1; j <= parser.generations; ++j ) {
                    out.write( "<td>" );
                    if( grid[j][i] != null ) out.write( "\r\n" + grid[j][i] + "\r\n" );
                    out.write( "</td>\r\n" );
                }
                out.write( "</tr>\r\n" );
            }
            out.write( "</table></body>\r\n</html>\r\n" );
        } catch( Exception e ) {
            System.err.println( "Error writing to " + outFile + ": " + e );
        }
        Utils.closeWriter( out );
    }

    private static int _fill( String[][] grid, Person person, int lines ) {
        String s = person.format( Person.fmtMultiLine | Person.fmtShowAdopted );
        for( Iterator<Person> i = person.partners.values().iterator(); i.hasNext(); ) {
            s += "\r\n<p>" + i.next().format( Person.fmtMultiLine );
        }
       grid[person.generation][lines] = s;
        boolean firstChild = true;
        for( Iterator<Person> i = person.children.values().iterator(); i.hasNext(); ) {
            if( !firstChild ) ++lines;
            lines = _fill( grid, i.next(), lines );
            firstChild = false;
        }
        return lines;
    }

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 2 ) {
            System.err.println( "Usage: ListByFamily person-list(UTF-8) output" );
            System.exit( 2 );
        }

        Parser parser = new Parser();
        if( !parser.parse( args[0] ) ) System.exit( 1 );
        parser.fill();
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons." );

        ListByFamily.display( parser, args[1] );
        System.exit( 0 );
    }
}
