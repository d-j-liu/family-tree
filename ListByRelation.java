import java.io.Writer;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.HashSet;

public class ListByRelation {

    private TreeMap<Integer, Person> _persons = null;
    private HashSet<Person> _siblings = new HashSet<Person>();

    private void _showPerson( Writer out, String type, Person person ) throws Exception {
        out.write( "<tr><td>" + type + "</td><td>" );
        if( person.isNode ) out.write( "<a href=#" + person.id + '>' );
        out.write( person.format( Person.fmtShowAdopted ) );
        if( person.isNode ) out.write( "</a>" );
        out.write( "</td></tr>\r\n" );
    }

    private void _showPerson( Writer out, String type, int id ) throws Exception {
        _showPerson( out, type, _persons.get( new Integer( id ) ) );
    }

    private void _showFamily( Writer out, Person person, int spouseOf ) throws Exception {
        final String vspace = "<tr><td><br></td></tr>\r\n";
        out.write( "<h2 id=" + person.id + ">" + person.format( Person.fmtShowAdopted, "</h2>" ) );
        out.write( "\r\n<table>\r\n" );
        if( person.father > 0 || person.mother > 0 ) out.write( vspace );
        if( person.father > 0 ) _showPerson( out, "Father", person.father );
        if( person.mother > 0 ) _showPerson( out, "Mother", person.mother );
        if( person.partners.size() > 0 ) out.write( vspace );
        String label = person.partners.size() > 1 ? "spouses" : "spouse";
        for( Iterator<Person> i = person.partners.values().iterator(); i.hasNext(); ) {
            _showPerson( out, label, i.next() );
            label = "";
        }
        if( person.children.size() > 0 ) out.write( vspace );
        label = person.children.size() > 1 ? "children" : "child";
        for( Iterator<Person> i = person.children.values().iterator(); i.hasNext(); ) {
            final Person child = i.next();
            if( spouseOf == 0 || child.father == 0 || child.mother == 0 ||
                    child.father == spouseOf || child.mother == spouseOf ) {
                _showPerson( out, label, child );
                label = "";
            }
        }
        if( person.siblings.size() > 0 ) out.write( vspace );
        label = person.siblings.size() > 1 ? "siblings" : "sibling";
        for( Iterator<Person> i = person.siblings.values().iterator(); i.hasNext(); ) {
            _showPerson( out, label, i.next() );
            label = "";
        }
        out.write( vspace + "<tr><td colspan=3><hr>\r\n" );
        out.write( "</table><p>\r\n" );

        if( person.sibling == 0 ) {
            for( Iterator<Person> i = person.siblings.values().iterator(); i.hasNext(); ) {
                _siblings.add( i.next() );
            }
        }
        if( spouseOf > 0 ) return;
        for( Iterator<Person> i = person.partners.values().iterator(); i.hasNext(); ) {
            final Person partner = i.next();
            if( partner.isNode ) _showFamily( out, partner, person.id );
        }
        for( Iterator<Person> i = person.children.values().iterator(); i.hasNext(); ) {
            final Person child = i.next();
            if( child.isNode ) _showFamily( out, child, 0 );
        }
    }

    public void display( Parser parser, String outFile ) {
        final String title = "Family of " + parser.roots.firstElement().name + " by Relationship";
        _persons = parser.persons;
        Writer out = Utils.openOutputFile( outFile );
        try {
            out.write( Utils.makeHttpHeader( title, false ) );
            for( int r = 0; r < parser.roots.size(); ++r ) {
                _siblings.clear();
                Person person = parser.roots.elementAt( r );
                person.isNode = true;
                _showFamily( out, person, 0 );
                for( Iterator<Person> itr = _siblings.iterator(); itr.hasNext(); ) {
                    _showFamily( out, itr.next(), 0 );
                }
            }
            out.write( "</body>\r\n</html>\r\n" );
        } catch( Exception e ) {
            System.err.println( "Error writing to " + outFile + ": " + e );
        }
        Utils.closeWriter( out );
    }

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 2 ) {
            System.err.println( "Usage: ListByRelation person-list(UTF-8) output" );
            System.exit( 2 );
        }

        Parser parser = new Parser();
        if( !parser.parse( args[0] ) ) System.exit( 1 );
        parser.fill();
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons." );

        ListByRelation lister = new ListByRelation();
        lister.display( parser, args[1] );
        System.exit( 0 );
    }
}
