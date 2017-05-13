import java.util.TreeMap;

public class ListAll {

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 3 ) {
            System.err.println( "Usage: ListAll person-list(UTF-8) -<format> output" );
            System.err.println( "  Supported formats are" );
            System.err.println( "    age: sorted by age" );
            System.err.println( "    generation: sorted by generation and age" );
            System.err.println( "    descedents: sorted by generation and age" );
            System.err.println( "    solardate: grid list by birthday" );
            System.err.println( "    lunardate: grid list by birthday (lunar)" );
            System.err.println( "    family: grid list by family" );
            System.err.println( "    relation: list by relationship" );
            System.exit( 2 );
        }

        Parser parser = new Parser();
        if( !parser.parse( args[0] ) ) System.exit( 1 );
        parser.fill();
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons." );

        for( int i = 1; i < args.length - 1; ++i ) {
            final String format = args[i].toLowerCase();
            if( format.charAt( 0 ) != '-' ) {
                System.err.println( "Invalid parameter: " + format );
                continue;
            }
            final String outFile = args[++i];
            ListByAge age = new ListByAge( parser );
            switch( format.charAt( 1 ) ) {
            case 'a':
                age.display( outFile, false );
                break;
            case 'd':
                ( new ListDescendents( parser ) ).display( outFile );
                break;
            case 'g':
                age.display( outFile, true );
                break;
            case 's':
                ListByDate.display( persons, outFile, false );
                break;
            case 'l':
                ListByDate.display( persons, outFile, true );
                break;
            case 'f':
                ( new ListByFamily() ).display( parser, outFile );
                break;
            case 'r':
                ( new ListByRelation() ).display( parser, outFile );
                break;
            default:
                System.err.println( "Unsupported format: " + format );
            }
        }
        System.exit( 0 );
    }
}
