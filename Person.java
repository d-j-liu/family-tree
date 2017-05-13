import java.util.TreeMap;

public class Person {

    public static final int male = 1;
    public static final int female = 2;
    public static final char[] genderLabel = new char[] { '?', 0x7537, 0x5973 };

    public static final char shu = 0x5C5E;
    public static final char shuTr = 0x5C6C;
    public static final String divorce = String.valueOf( new char[] { '[', 0x79BB, ']' } );
    public static final String adopted = String.valueOf( new char[] { '[', 0x8FC7, 0x623F, ']' } );

    public static final int fmtMultiLine = 1;
    public static final int fmtShowAdopted = 2;

    public int id = 0;
    public String name = "";
    public String altName = "";
    public int gender = 0;
    public DualDate birth = null;
    public DualDate death = null;
    public int father = 0;
    public int mother = 0;
    public int spouse = 0;
    // to indicate the person being a sibling of a spouse (rare)
    public int sibling = 0;

    public boolean isNode = false;
    public int generation = 0;
    public TreeMap<Integer, Person> partners = new TreeMap<Integer, Person>();
    public TreeMap<Integer, Person> children = new TreeMap<Integer, Person>();
    public TreeMap<Integer, Person> siblings = new TreeMap<Integer, Person>();

    public String formatName( String delim ) {
        final String s = spouse < 0 ? divorce + name : sibling < 0 ? name + adopted : name;
        return altName.isEmpty() ? s : s + delim + "(" + altName + ')';
    }

    public String format( int format ) {
        return format( format, null );
    }

    public String format( int format, String nameDelim ) {
    	final boolean multiLine = ( format & fmtMultiLine ) > 0;
        final String delim = multiLine ? "<br>" : " ";
        if( nameDelim == null || nameDelim.isEmpty() ) nameDelim = delim;
        String s = formatName( delim ) + nameDelim + genderLabel[gender];
        final char z = birth.getZodiac();
        if( z != 0 ) s = ( s + shu ) + z;
        if( !multiLine ) return s + ", " + birth.format() + " - " + death.format();
        s += "<br>" + birth.formatSolar() + " - ";
        if( death.solar > 0 ) s += death.formatSolar();
        s += delim + '(';
        s += birth.formatLunar() + " - ";
        if( death.solar > 0 ) s += death.formatLunar();
        s += ')';
        return s;
    }

    public String export() {
        final char T = '\t';
        final char[] gLabel = new char[] { '?', 'M', 'F' };
        return "" + id + T + name + T + altName + T + gLabel[gender] + T +
                birth.solar + T + birth.customLunar() + T +
                death.solar + T + death.customLunar() + T +
                father + T + mother + T + spouse;
    }

    public String toString() {
        return formatName( " " ) + ' ' + genderLabel[gender] + ' ' +
                ( birth.isSet() ? birth.format() : "?" ) + " - " + death.format() +
                " father: " + father + " mother: " + mother + " spouse: " + spouse +
                " generation: " + generation + " children: " + children.size();
    }
}
