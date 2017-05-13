import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;

public class ListByDate {

    public static void display( TreeMap<Integer, Person> persons, String outFile, boolean lunar ) {
        final int days = lunar ? 30 : 31;
        TreeMap<Integer, Person>[][] grid = new TreeMap[13][days + 1];
        for( int i = 1; i < 13; ++i ) {
            for( int j = 1; j <= days; ++j ) {
                grid[i][j] = new TreeMap<Integer, Person>();
            }
        }
        int count = 0;
        for( Iterator<Person> i = persons.values().iterator(); i.hasNext(); ) {
            final Person person = i.next();
            if( person.birth.lunar == 0 ) {
                System.err.println( "Skip " + person.id + ": invalid date of birth." );
                continue;
            }
            final int day = lunar ? person.birth.lunar : person.birth.solar;
            final int m = ( day % 10000 ) / 100;
            final int d = day % 100;
            if( Parser.insertByBirthday( grid[m][d], person ) ) ++count;
            else System.err.println( "Skip " + person.id + ": date of birth full." );
        }
        System.out.println( "Displaying " + count + " persons." );
        Writer out = Utils.openOutputFile( outFile );
        final String title = "Family of " + persons.firstEntry().getValue().name
                + " by Birthday" + ( lunar ? " (\u9634\u5386)" : "" );
        final String leap = " (" + DualDate.leap + ")";
        try {
            out.write( Utils.makeHttpHeader( title, true ) );
            out.write( "<h3><center>" + title + "</center></h3><p>" );
            out.write( "<table><col width=30>" );
            for( int k = 1; k < 13; ++k ) {
                out.write( "<col width=120>" );
            }
            out.write( "\r\n<tr><td></td>" );
            for( int m = 1; m < 13; ++m ) {
                String s = "";
                if( m > 9 ) s += DualDate.dates[10];
                if( m != 10 ) s += DualDate.dates[m % 10];
                out.write( "<td>" + ( lunar ? s + DualDate.month : m ) + "</td>" );
            }
            out.write( "</tr>\r\n" );
            for( int d = 1; d <= days; ++d ) {
                out.write( "<tr><td>" + ( lunar ? DualDate.formatLunarDay( d ) : d ) + "</td>" );
                for( int m = 1; m < 13; ++m ) {
                    out.write( "<td>\r\n" );
                    for( Iterator<Person> itr = grid[m][d].values().iterator(); itr.hasNext(); ) {
                        final Person person = itr.next();
                        out.write( person.formatName( "<br>" ) );
                        out.write( "<br>" + ( person.birth.solar / 10000 ) );
                        if( lunar ) {
                            out.write( " " + person.birth.getChineseYear() );
                            if( person.birth.lunar > 10000 ) out.write( leap );
                        }
                        out.write( "<p>\r\n" );
                    }
                    out.write( "</td>\r\n" );
                }
                out.write( "</tr>\r\n" );
            }
            out.write( "</table>\r\n<p>Showing " + count + '/' + persons.size() +
                    " persons.\r\n</body>\r\n</html>\r\n" );
        } catch( Exception e ) {
            System.err.println( "Error writing to " + outFile + ": " + e );
        }
        Utils.closeWriter( out );
    }

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 2 ) {
            System.err.println( "Usage: ListByDate [-t] person-list(UTF-8) output" );
            System.exit( 2 );
        }

        int argi = 0;
        boolean traditional = false;
        if( args[argi].charAt( 0 ) == '-' ) {
            if( args[argi].charAt( 1 ) == 't' ) traditional = true;
            ++argi;
        }
        final String inFile = args[argi++];
        final String outFile = args[argi++];

        Parser parser = new Parser();
        if( !parser.parse( inFile ) ) System.exit( 1 );
        parser.fill();
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons." );

        display( persons, outFile, traditional );
        System.exit( 0 );
    }
}
