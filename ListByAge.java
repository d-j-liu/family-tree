import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

public class ListByAge {

    public final String rootName;
    public final int total;
    private TreeMap<Integer, Person> _sorted = new TreeMap<Integer, Person>();

    public ListByAge( Parser parser ) {
        TreeMap<Integer, Person> persons = parser.persons;
        total = persons.size();
        rootName = parser.roots.get( 0 ).name;
        for( Iterator<Person> i = persons.values().iterator(); i.hasNext(); ) {
            final Person person = i.next();
            if( !person.birth.isSet() || person.birth.solar == 0 ) {
                System.err.println( "Skip " + person.id + ": invalid date of birth." );
                continue;
            }
            if( !Parser.insertByBirthday( _sorted, person ) )
                System.err.println( "Skip " + person.id + ": date of birth full." );
        }
        System.out.println( "Displaying " + _sorted.size() + " persons." );
    }

    public void display( String outFile, boolean byGeneration ) {
        final String title = "Family of " + rootName +
                ( byGeneration ? " by generation" : " by age" );
        Writer out = Utils.openOutputFile( outFile );
        try {
            out.write( Utils.makeHttpHeader( title, false ) );
            if( byGeneration ) displayGeneration( out );
            else displayFlat( out );
            out.write( "<p>Showing " + _sorted.size() + '/' + total + " persons.\r\n" );
            out.write( "</body>\r\n</html>\r\n" );
        } catch( Exception e ) {
            System.err.println( "Error writing to " + outFile + ": " + e );
        }
        Utils.closeWriter( out );
    }

    public void displayFlat( Writer out ) throws Exception {
        for( Iterator<Person> i = _sorted.values().iterator(); i.hasNext(); ) {
            out.write( i.next().format( 0 ) + "<br>\r\n" );
        }
    }

    public void displayGeneration( Writer out ) throws Exception {
        for( int g = 1;; ++g ) {
            boolean written = false;
            for( Iterator<Person> i = _sorted.values().iterator(); i.hasNext(); ) {
                final Person person = i.next();
                if( person.generation != g ) continue;
                out.write( person.format( 0 ) + "<br>\r\n" );
                written = true;
            }
            if( written ) out.write( "<p>\r\n" );
            else break;
        }
    }

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 2 ) {
            System.err.println( "Usage: ListByAge [-g] person-list(UTF-8) output" );
            System.exit( 2 );
        }

        int argi = 0;
        boolean byGeneration = false;
        if( args[argi].charAt( 0 ) == '-' ) {
            if( args[argi].charAt( 1 ) == 'g' ) byGeneration = true;
            ++argi;
        }
        final String inFile = args[argi++];
        final String outFile = args[argi++];

        Parser parser = new Parser();
        if( !parser.parse( inFile ) ) System.exit( 1 );
        parser.fill();
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons." );

        ListByAge lister = new ListByAge( parser );
        lister.display( outFile, byGeneration );
        System.exit( 0 );
    }
}
