public class ExtraFieldParser implements Parser.IExtraFieldParser {

    public int parse( Person person, String[] fields, int offset ) {
        for( ; offset < fields.length; ++offset ) {
            int shu = fields[offset].indexOf( Person.shu );
            if( shu < 1 ) shu = fields[offset].indexOf( Person.shuTr );
            if( shu < 1 ) continue;
            final char MF = fields[offset].charAt( shu - 1 );
            final int gender = Person.genderLabel[Person.male] == MF ? Person.male :
                        Person.genderLabel[Person.female] == MF ? Person.female : 0;
            if( gender == 0 && person.gender == 0 )
                    System.err.println( "Unknown gender " + MF + " for person " + person.id );
            if( person.gender == 0 ) person.gender = gender;
            else if( person.gender != gender )
                    System.err.println( "Conflicting gender for person " + person.id );
            break;
        }
        return offset;
    }
}
