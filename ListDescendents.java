import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

public class ListDescendents {

    public final String rootName;
    public final int total;
    private TreeMap<Integer, Person>[] _sorted = null;

    public ListDescendents( Parser parser ) {
        TreeMap<Integer, Person> persons = parser.persons;
        final Person root = parser.roots.get( 0 );
        rootName = root.name;
        total = persons.size();
        _sorted = new TreeMap[parser.generations + 1];
        for( int g = 1; g < _sorted.length; ++g ) {
            _sorted[g] = new TreeMap<Integer, Person>();
        }
        _sorted[1].put( 1, root );
        _insert( root );
    }

    private void _insert( Person person ) {
        for( Iterator<Person> i = person.children.values().iterator(); i.hasNext(); ) {
            final Person child = i.next();
            _insert( child );
            if( !child.birth.isSet() || child.birth.solar == 0 ) {
                System.err.println( "Skip " + child.id + ": invalid date of birth." );
                continue;
            }
            if( !Parser.insertByBirthday( _sorted[child.generation], child ) )
                System.err.println( "Skip " + child.id + ": date of birth full." );
        }
    }

    public void display( String outFile ) {
        final String title = "Descendents of " + rootName;
        Writer out = Utils.openOutputFile( outFile );
        try {
            out.write( Utils.makeHttpHeader( title, false ) );
            int shown = 0;
            for( int g = 1; g < _sorted.length; ++g ) {
                for( Iterator<Person> i = _sorted[g].values().iterator(); i.hasNext(); ) {
                    out.write( i.next().format( 0 ) + "<br>\r\n" );
                    ++shown;
                }
                out.write( "<p>\r\n" );
            }
            out.write( "<p>Showing " + shown + '/' + total + " persons.\r\n" );
            out.write( "</body>\r\n</html>\r\n" );
        } catch( Exception e ) {
            System.err.println( "Error writing to " + outFile + ": " + e );
        }
        Utils.closeWriter( out );
    }

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 2 ) {
            System.err.println( "Usage: ListDescendents person-list(UTF-8) output" );
            System.exit( 2 );
        }

        int argi = 0;
        final String inFile = args[argi++];
        final String outFile = args[argi++];

        Parser parser = new Parser();
        if( !parser.parse( inFile ) ) System.exit( 1 );
        parser.fill();
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons." );

        ListDescendents lister = new ListDescendents( parser );
        lister.display( outFile );
        System.exit( 0 );
    }
}
