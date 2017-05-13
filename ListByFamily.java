import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

public class ListByFamily {

    private String[][] _grid = null;
    private int _lines = 0;

    public void display( Parser parser, String outFile ) {
        _grid = new String[parser.generations + 1][parser.persons.size()];
        _lines = 0;
        for( int r = 0; r < parser.roots.size(); ++r ) {
            if( _lines > 0 ) _lines += 2;
            _fill( parser.roots.get( r ) );
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
            for( int i = 0; i <= _lines; ++i ) {
                out.write( "<tr>\r\n" );
                for( int j = 1; j <= parser.generations; ++j ) {
                    out.write( "<td>" );
                    if( _grid[j][i] != null ) out.write( "\r\n" + _grid[j][i] + "\r\n" );
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

    private void _fill( Person person ) {
        String s = person.format( Person.fmtMultiLine | Person.fmtShowAdopted );
        for( Iterator<Person> i = person.partners.values().iterator(); i.hasNext(); ) {
            s += "\r\n<p>" + i.next().format( Person.fmtMultiLine );
        }
        _grid[person.generation][_lines] = s;
        boolean firstChild = true;
        for( Iterator<Person> i = person.children.values().iterator(); i.hasNext(); ) {
            if( !firstChild ) ++_lines;
            _fill( i.next() );
            firstChild = false;
        }
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

        ListByFamily lister = new ListByFamily();
        lister.display( parser, args[1] );
        System.exit( 0 );
    }
}
