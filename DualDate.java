import java.io.Writer;

public class DualDate {

    public final int solar;
    public final int lunar;
    public final int lunarCalc;

    // the offset of the Sexagenary cycle
    public static final int offset = 4;

    public static final char leap = 0x95F0;
    public static final char year = 0x5E74;
    public static final char month = 0x6708;
    public static final char[] dates = new char[] {
            0x521D, 0x4E00, 0x4E8C, 0x4E09, 0x56DB, 0x4E94,
            0x516D, 0x4E03, 0x516B, 0x4E5D, 0x5341, 0x5EFF
    };
    public static final char[] heavenlyStem = new char[] {
            0x7532, 0x4E59, 0x4E19, 0x4E01, 0x620A,
            0x5DF1, 0x5E9A, 0x8F9B, 0x58EC, 0x7678
    };
    public static final char[] earthlyBranch = new char[] {
            0x5B50, 0x4E11, 0x5BC5, 0x536F, 0x8FB0, 0x5DF3,
            0x5348, 0x672A, 0x7533, 0x9149, 0x620C, 0x4EA5
    };
    public static final char[] zodiac = new char[] {
            0x9F20, 0x725B, 0x864E, 0x5154, 0x9F99, 0x86C7,
            0x9A6C, 0x7F8A, 0x7334, 0x9E21, 0x72D7, 0x732A
    };

    public DualDate( int origSolar, int origLunar ) {
        solar = origSolar;
        lunarCalc = calcLunar();
        lunar = origLunar == 0 ? lunarCalc : origLunar;
    }

    public boolean isSet() {
        return solar == 0 && lunar == 0 || solar > 10100 && ( lunar > 100 || lunar == 0 );
    }

    public int getSexagenary() {
        return solar / 10000 - offset - ( ( solar % 10000 ) < ( lunar % 10000 ) ? 1 : 0 );
    }

    public String getChineseYear() {
        final int y = getSexagenary();
        return y < 0 ? "" : "" + heavenlyStem[y % 10] + earthlyBranch[y % 12];
    }

    public char getZodiac() {
        final int y = getSexagenary();
        return y < 0 ? 0 : zodiac[y % 12];
    }

    public int calcLunar() {
        if( solar < 10100 ) return 0;
        final int ms = ( solar / 100 ) % 100;
        final int ds = solar % 100;
        if( ms == 0 || ds == 0 || ms > 12 || ds > 31 ) return 0;
        ChineseCalendar cc = new ChineseCalendar();
        cc.setGregorian( solar / 10000, ms, ds );
        if( cc.computeChineseFields() != 0 ) return 0;
        if( cc.computeSolarTerms() != 0 ) return 0;
        final int ml = cc.getChineseMonth();
        final int dl = cc.getChineseDate();
        return ( ml > 0 ? ml : 100 - ml ) * 100 + dl;
    }

    public int customLunar() {
        return lunar > 0 && lunar != lunarCalc ? lunar : 0;
    }

    public String formatSolar() {
        if( solar < 10101 ) return "?";
        final String y = "" + ( solar / 10000 );
        final int md = solar % 10000;
        if( md < 101 || md > 1231 ) return y;
        return y + '/' + ( solar / 100 % 100 ) + '/' + ( solar % 100 );
    }

    public String formatLunar() {
        if( lunar < 101 ) return "?";
        final int m = ( lunar / 100 ) % 100;
        String s = getChineseYear() + year;
        if( lunar > 10000 ) s += leap;
        if( m > 9 ) s += dates[10];
        if( m != 10 ) s += dates[m % 10];
        s += month;
        s += formatLunarDay( lunar % 100 );
        return s;
    }

    public static String formatLunarDay( int d ) {
        String s = "";
        if( d < 11 ) s += dates[0];
        else if( d < 20 ) s += dates[10];
        else if( d > 20 && d < 30 ) s += dates[11];
        if( d == 20 || d == 30 ) s += dates[d / 10];
        if( d % 10 == 0 ) s += dates[10];
        else s += dates[d % 10];
        return s;
    }

    public String format() {
        if( solar == 0 ) return "";
        String s = formatSolar();
        return lunar == 0 ? s : s + " (" + formatLunar() + ')';
    }

    public static String testLunarFormat() throws Exception {
        final int[] dates = new int[] {
                19470202, 19430125, 19390922, 19410724, 19451231, 19360520
        };
        StringBuffer buf = new StringBuffer();
        for( int i = 0; i < dates.length; ++i ) {
            final DualDate date = new DualDate( dates[i], 0 );
            buf.append( "# test date " + dates[i] + " -> " + date.formatLunar() + "\r\n" );
        }
        for( int d = 1; d <= 30; ++d ) {
            buf.append( "# test day " + d + " -> " + DualDate.formatLunarDay( d ) + "\r\n" );
        }
        return buf.toString();
    }

    public static void main( String args[] ) throws InterruptedException {
        Writer out = Utils.openOutputFile( args[0] );
        try {
            out.write( testLunarFormat() );
        } catch( Exception e ) {
            System.err.println( "Exception: " + e );
        }
        if( args.length > 1 ) {
            for( int i = 1; i < args.length; ++i ) {
                try {
                    final int ns = Integer.parseInt( args[i] );
                    final int nc = ns - offset;
                    out.write( "Year " + ns + ": " + heavenlyStem[nc % 10] + earthlyBranch[nc % 12] + "\r\n" );
                } catch( Exception e ) {
                    System.err.println( "Error converting year " + args[i] + ": " + e );
                }
            }
        }
        Utils.closeWriter( out );
        System.exit( 0 );
    }
}
