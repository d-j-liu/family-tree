import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.Writer;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Vector;

public class Parser {

    public interface IExtraFieldParser {
        /**
         * Parse extra fields
         * @param person  the person to parse for
         * @param fields  the list of fields to parse  
         * @param offset  the index at which to start parsing
         * @return the index where processing ended
         */
        public int parse( Person person, String[] fields, int offset );
    }

    // the maximum number of persons that can share the same date of birth
    final static int dup = 10;

    final static int invalid = 0x80000000;

    private IExtraFieldParser _extraParser = null;

    public TreeMap<Integer, Person> persons = new TreeMap<Integer, Person>();
    public Vector<Person> roots = new Vector<Person>();
    public int generations = 0;

    public StringBuffer comments = new StringBuffer();

    public Parser() {}

    public Parser( IExtraFieldParser parser ) {
        _extraParser = parser;
    }

    public boolean parse( String fileName ) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader( new InputStreamReader( new FileInputStream( fileName ), "UTF-8" ) );
        } catch( Exception e ) {
            System.err.println( "Error reading '" + fileName + "': " + e );
            return false;
        }

        int maxId = 0;
        for( int i = 1;; ++i ) {
            String line = null;
            try {
                line = reader.readLine();
            } catch( Exception e ) {
                break;
            }
            if( line == null ) break;
            if( line.charAt( 0 ) == '#' ) {
                comments.append( line ).append( "\r\n" );
                continue;
            }
            final String[] tokens = line.split( "\t|," );
            if( tokens.length < 10 ) {
                System.err.println( "Skip line " + i + ": " + tokens.length + " fields." );
                continue;
            }
            // parse standard fields
            int field = 0;
            final int id = _parseInt( tokens, field++ );
            if( id == invalid ) {
                System.err.println( "Invalid ID " + tokens[0] + ", skipping " + line );
                continue;
            }
            if( maxId < id ) maxId = id;
            Person person = new Person();
            person.id = id;
            person.name = tokens[field++];
            person.altName = tokens[field++];
            person.gender = _parseGender( tokens[field++] );
            final int bs = _parseInt( tokens, field++ );
            final int bl = _parseInt( tokens, field++ );
            person.birth = new DualDate( bs, bl );
            final int ds = _parseInt( tokens, field++ );
            final int dl = _parseInt( tokens, field++ );
            person.death = new DualDate( ds, dl );
            person.father = _parseInt( tokens, field++ );
            person.mother = _parseInt( tokens, field++ );
            person.spouse = _parseInt( tokens, field++ );
            person.sibling = _parseInt( tokens, field++ );
            // extract information from additional fields
            if( _extraParser != null ) _extraParser.parse( person, tokens, field );

            // first validation
            if( person.birth.isSet() && person.birth.solar > 0 ) {
                if( person.birth.customLunar() > 0 )
                    System.err.println( "Custom lunar birth date for person " + id );
            } else {
                System.err.println( "Invalid or missing date of birth for person " + id );
            }
            if( person.death.isSet() ) {
                if( person.death.customLunar() > 0 )
                    System.err.println( "Custom lunar death date for person " + id );
            } else {
                System.err.println( "Invalid date of death for person " + id );
            }
            if( person.father == invalid || person.mother == invalid || person.spouse == invalid ) {
                System.err.println( "Invalid relation for person " + id );
                continue;
            }
            final Integer key = Integer.valueOf( id );
            if( persons.containsKey( key ) ) {
                System.err.println( "Duplicate person [" + id + "]: " + person.name );
                continue;
            }
            persons.put( key, person );
        }
        try {
            if( reader != null ) reader.close();
        } catch( Exception e ) {}
        System.err.println( "Max ID " + maxId );
        return true;
    }

    private static int _parseInt( String[] fields, int index ) {
        if( index >= fields.length ) return 0;
        String s = fields[index];
        if( s == null ) return 0;
        s = s.trim();
        try {
            return s.length() == 0 ? 0 : Integer.parseInt( s );
        } catch( Exception e ) {
            return invalid;
        }
    }

    private static int _parseGender( String s ) {
        if( s.length() == 0 ) return 0;
        final char c = s.toUpperCase().charAt( 0 );
        return c == 'M' ? Person.male : c == 'F' ? Person.female : 0;
    }

    public static boolean insertByBirthday( TreeMap<Integer, Person> list, Person person ) {
        for( int k = 0; k < dup; ++k ) {
            final Integer key = Integer.valueOf( person.birth.solar * dup + k );
            if( list.containsKey( key ) ) continue;
            list.put( key, person );
            return true;
        }
        return false;
    }

    public void fill() {
        // external links to track persons related to spouses of descendants
        Vector<Person> extLinks = new Vector<Person>();
        // validate relationship and fill-in missing values
        for( Iterator<Person> i = persons.values().iterator(); i.hasNext(); ) {
            final Person me = i.next();
            if( me.father < 0 || me.mother < 0 ) extLinks.add( me );
            if( me.spouse != 0 && me.father <= 0 && me.mother <= 0 ) {
                final Integer sid = Integer.valueOf( me.spouse > 0 ? me.spouse : -me.spouse );
                final Person spouse = persons.get( sid );
                if( spouse != null ) {
                    if( me.gender == 0 && spouse.gender != 0 ) me.gender = spouse.gender ^ 3;
                    int sortId = me.id;
                    if( spouse.spouse != me.id ) sortId += 10000;
                    if( me.spouse < 0 ) sortId += 10000;
                    spouse.partners.put( Integer.valueOf( sortId ), me );
                    me.partners.put( sid, spouse );
                }
            }
            final Person father = persons.get( Integer.valueOf( me.father ) );
            if( father != null ) {
                insertByBirthday( father.children, me );
                if( father.gender == Person.female ) System.err.println( "Gender error " + father.id );
                father.gender = Person.male;
            }
            final Person mother = persons.get( Integer.valueOf( me.mother ) );
            if( mother != null ) {
                insertByBirthday( mother.children, me );
                if( mother.gender == Person.male ) System.err.println( "Gender error " + mother.id );
                mother.gender = Person.female;
            }
            if( ( me.father == 0 ) != ( me.mother == 0 ) ) System.err.println( "Person[" + me.id + "] has single parent" );
            final Person sibling = persons.get( Integer.valueOf( me.sibling ) );
            if( sibling != null ) {
                insertByBirthday( sibling.siblings, me );
                insertByBirthday( me.siblings, sibling );
            }
        }
        final Person root = persons.firstEntry().getValue();
        roots.add( root );
        root.generation = 1;
        generations = setRelation( root );
        // add external root members -- must appear after their internal links
        for( int m = 0; m < extLinks.size(); ++m ) {
            final Person src = extLinks.get( m );
            if( src.generation == 0 ) continue;
            for( int n = m + 1; n < extLinks.size(); ++n ) {
                final Person dest = extLinks.get( n );
                if( dest.generation > 0 ) continue;
                if( src.father != dest.father || src.mother != dest.mother ) continue;
                roots.add( dest );
                dest.generation = src.generation;
                final int g = setRelation( dest );
                if( g > generations ) generations = g;
            }
        }
    }

    public static int setRelation( Person person ) {
        int gen = person.generation;
        final int spn = person.partners.size();
        final int cLen = person.children.size();
        if( spn > 0 || cLen > 0 ) person.isNode = true;
        final int nextGen = person.generation + 1;
        for( Iterator<Person> i = person.partners.values().iterator(); i.hasNext(); ) {
            final Person partner = i.next();
            if( partner.generation == 0 ) partner.generation = gen;
            final int pcLen = partner.children.size();
            if( pcLen > 0 && ( spn > 1 || !partner.children.equals( person.children ) ) ) partner.isNode = true;
            for( Iterator<Person> j = partner.siblings.values().iterator(); j.hasNext(); ) {
                partner.isNode = true;
                final Person sibling = j.next();
                if( sibling.generation > 0 ) continue;
                sibling.generation = gen;
                setRelation( sibling );
            }
            for( Iterator<Person> j = partner.children.values().iterator(); j.hasNext(); ) {
                final Person child = j.next();
                if( child.generation == 0 ) child.generation = nextGen;
            }
        }
        for( Iterator<Person> i = person.children.values().iterator(); i.hasNext(); ) {
            final Person child = i.next();
            if( child.generation == 0 ) child.generation = nextGen;
            final int g = setRelation( child );
            if( gen < g ) gen = g;
        }
        return gen;
    }

    public static void export( Writer out, Person root ) throws Exception {
        out.write( root.export() + "\r\n" );
        for( Iterator<Person> i = root.partners.values().iterator(); i.hasNext(); ) {
            out.write( i.next().export() + "\r\n" );
        }
        for( Iterator<Person> j = root.children.values().iterator(); j.hasNext(); ) {
            export( out, j.next() );
        }
        for( Iterator<Person> k = root.siblings.values().iterator(); k.hasNext(); ) {
            export( out, k.next() );
        }
    }

    public static void main( String args[] ) throws InterruptedException {
        if( args.length < 2 ) {
            System.err.println( "Usage: Parser person-list(UTF-8) output" );
            System.exit( 2 );
        }

        Parser parser = new Parser();
        if( !parser.parse( args[0] ) ) System.exit( 1 );

        parser.fill();
        Writer out = Utils.openOutputFile( args[1] );
        TreeMap<Integer, Person> persons = parser.persons;
        System.out.println( "Total " + persons.size() + " persons in " +
                parser.generations + " generations." );
        int max = 0;
        for( Iterator<Person> i = persons.values().iterator(); i.hasNext(); ) {
            final Person person = i.next();
            if( max < person.id ) max = person.id;
            System.out.println( "person[" + person.id + "]: " + person );
        }
        System.err.println( "Maximum ID=" + max );
        for( int i = 1; i < max; ++i ) {
            if( persons.get( Integer.valueOf( i ) ) == null ) System.err.println( "Gap: " + i );
        }
        if( out == null ) {
            System.err.println( "Unable to open output file." );
            System.exit( 1 );
        }
        try {
            if( parser.comments.length() > 2 ) out.write( parser.comments.toString() );
            else out.write( "# id\tName\tAltName\tGender\tBirth\tBirthC\tDeath\tDeathC\tFather\tMother\tSpouse\r\n" );
            for( int i = 0; i < parser.roots.size(); ++i ) {
                export( out, parser.roots.get( i ) );
            }
        } catch( Exception e ) {}
        Utils.closeWriter( out );
        System.exit( 0 );
    }
}
