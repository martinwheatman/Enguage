package org.enguage.obj.list;

import java.util.ListIterator;

import org.enguage.util.Attribute;
import org.enguage.util.Attributes;
import org.enguage.util.Audit;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.veh.Plural;
import org.enguage.veh.number.Number;
import org.enguage.veh.when.Moment;
import org.enguage.veh.when.When;
import org.enguage.veh.where.Where;

public class Item {

	static private      Audit audit = new Audit("Item" );
	static public final String NAME = "item";
	
	static private Strings format = new Strings(); // e.g. "cake slice", "2 cake slices" or "2 slices of cake"
	static public  void    format( String csv ) { format = new Strings( csv, ',' ); }
	static public  Strings format() { return format; }

	static private Strings groupOn = new Strings();
	static public  void    groupOn( String groups ) { groupOn = new Strings( groups );}
	static public  Strings groupOn() { return groupOn; }
	
	// members: name, desc, attr
	private String  name = new String();
	public  String  name() {return name; }
	public  Item    name( String s ) { name=s; return this; }
	
	private Strings descr = new Strings();
	public  Strings description() { return descr;}
	public  Item    description( Strings s ) { descr=s; return this;}
	
	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Item       attributes( Attributes a ) { attrs=a; return this; }
	public  String     attribute( String name ) { return attrs.get( name ); }
	public  void       replace( String name, String val ) { attrs.replace( name, val );}
	
	public Item() { name( "item" ); }
	public Item( Item item ) { // copy c'tor
		this();
		description( new Strings( item.description() ));
		attributes( new Attributes( item.attributes() ));
	}
	public Item( Strings ss ) { // [ "black", "coffee", "quantity='1'", "unit='cup'" ]
		this();
		Attributes  a = new Attributes();
		Strings descr = new Strings();
		
		for (String s : ss)
			if (Attribute.isAttribute( s ))
				a.add( new Attribute( s ));
			else if (!s.equals("-"))
				descr.add( s );

		description(  descr );
		attributes( a );
	}
	public Item( String s ) {
		// "black coffee quantity='1' unit='cup'
		this( new Strings( s ).contract( "=" ));
	}
	public Item( Strings ss, Attributes as ) {
		// [ "black", "coffee", "quantity='1'"], [unit='cup']
		this( ss );
		attributes().addAll( as );
	}
	
	// -- list helpers
	public long when() {
		long it = -1;
		try {
			it = Long.valueOf( attribute( "WHEN" ));
		} catch (Exception e){}
		return it;
	}
	public int quantity() {
		int quant = 1;
		try {
			quant = Integer.parseInt( attribute( "quantity" ));
		} catch(Exception e) {} // fail silently
		return quant;
	}
	public boolean equals( Item patt ) { // like Tag.equals()
		return Plural.singular( descr ).equals( Plural.singular( patt.description() ))
				&& attrs.matches( patt.attributes());
	}
	public boolean equalsDescription( Item patt ) { // like Tag.equalsContent()
		return Plural.singular( descr ).equals( Plural.singular( patt.description() ));
	}
	public boolean matches( Item patt ) {
		return Plural.singular( descr )
					.contains( Plural.singular( patt.description() )) &&
				attributes().matches( patt.attributes());
	}
	public boolean matchesDescription( Item patt ){
		return Plural.singular( descr )
					.contains( Plural.singular( patt.description() ));
	}
	// -----------------------------------------
	
	public void updateAttributes( Item update ) {
		// update quantity, then replace/add others/all?
		audit.in( "updateAttributes", update.toXml());
		for (Attribute a : update.attrs) {
			String value = a.value(),
					name = a.name();
			if (name.equals( "quantity" )) {
				Number n = new Number( value ),
				       m = new Number( attribute( "quantity" ));
				value = m.combine( n ).toString();
			}
			replace( name, value );
		}
		audit.out();
	}
	public Item removeQuantity( Number removed ) {
		audit.in( "removeQuantity", removed.toString());
		Number quantity = new Number( attribute( "quantity" ));
		removed.magnitude( -removed.magnitude()).relative( true );
		replace( "quantity", quantity.combine( removed ).toString() );
		return (Item) audit.out( this );
	}
	
	// pluralise to the last number... e.g. n cups(s); NaN means no number found yet
	private Float prevNum = Float.NaN;
	private String counted( Float num, String val ) {
		// N.B. val may be "wrong", e.g. num=1 and val="coffees"
		if (val.equals("1")) return "a"; // English-ism!!!
		return Plural.ise( num, val );
	}
	private Float getPrevNum( String val ) {
		ListIterator<String> si = new Strings( val ).listIterator();
		Float prevNum = Number.getNumber( si ).magnitude(); //Integer.valueOf( val );
		return prevNum.isNaN() ? 1.0f : prevNum;
	}
	private Strings getFormatComponentValue( String composite ) { // e.g. from LOCATION
		Strings value = new Strings();
		for (String cmp : new Strings( composite ))
			if ( Strings.isUpperCase( cmp )) { // variable e.g. UNIT
				if (groupOn().contains( cmp )) { // ["LOC"].contains( "LOC" )
					value=null; // IGNORE this component
					audit.debug( "toString(): ignoring:"+ cmp );
					break;
				} else {
					String val = attributes().get( cmp );
					if (val.equals( "" )) {
						value=null; // this component is undefined, IGNORE
						break;
					} else if (cmp.equals("WHEN")) {
						value.add( new When( new Moment( Long.valueOf( val ))).toString() );
					} else if (cmp.equals(Where.LOCTN)
							|| cmp.equals(Where.LOCTR)) {
						value.add( val ); // don't count these!
					} else { // 3 cupS -- pertains to unit/quantity only?
						value.add( counted( prevNum, val ) );  // UNIT='cup(S)'
						prevNum = getPrevNum( val );
				}	}
			} else // lower case -- constant
				value.add( cmp ); // ...of...
		return value;
	}
	public String toXml() { return "<"+name +attrs+">"+descr+"</"+name+">";}
	public String toString() {
		Strings rc = new Strings();
		if (format.size() == 0)
			rc.append( descr.toString() );
		else
			/* Read through the format string: ",from LOCATION"
			 * ADDING attributes: u.c. VARIABLES OR l.c. CONSTANTS),
			 * OR the description if blank string.
			 */
			for (String f : format) // e.g. f="from LOCATION"
				if (f.equals("")) // main item: "black coffee"
					rc.append( Plural.ise( prevNum, descr ));
				else { // attributes: "UNIT of" + unit='cup' => "cups of"
					Strings subrc = getFormatComponentValue( f );
					if (null != subrc) // ignore group name, and undefs
						rc.addAll( subrc );
				}
		return rc.toString();
	}
	private Strings getFormatGroupValue( String f ) {
		boolean found = false;
		Strings value = new Strings();
		for (String cmp : new Strings( f ))
			if ( Strings.isUpperCase( cmp )) { // variable e.g. UNIT
				String val = attributes().get( cmp );
				if (val.equals( "" )) {
					found = false;
					break;
				}
				value.add( val );
				if (groupOn().contains( cmp ))  // ["LOC"].contains( "LOC" )
					found = true;
			} else // lower case -- constant
				value.add( cmp ); // ...of...
		return found ? value : null;
	}
	public String group() { // like toString() but returning group value
		Strings rc = new Strings();
		if (format.size() == 0)
			rc.append( "" );
		else
			/* Read through the format string: ",from LOCATION"
			 * ADDING attributes: u.c. VARIABLES OR l.c. CONSTANTS),
			 * OR the description if blank string.
			 */
			for (String f : format) // e.g. f="from LOCATION"
				// main item: "black coffee" IGNORE
				if (!f.equals("")) { // attributes: "UNIT of" + unit='cup' => "cups of"
					Strings value = getFormatGroupValue( f );
					if (null != value) // ignore group name, and undefs
						rc.addAll( value );
				}
		return rc.toString( Strings.SPACED );
	}
	// ------------------------------------------------------------------------
	static public String interpret( Strings cmd ) {
		String rc = Shell.FAIL;
		if (cmd.size() > 2
				&& cmd.get( 0 ).equals( "set" )
				&& cmd.get( 1 ).equals( "format" ))
		{
			Item.format( Strings.stripQuotes( cmd.get( 2 )));
			rc = Shell.SUCCESS;
		}
		return rc;
	}
	//
	// --- test code ---
	//
	private static Groups groups = new Groups();
	private static void testAdd( String descr ) {
		Item item = new Item( new Strings( descr ).contract( "=" ));
		groups.add( item.group(), item.toString());
	}
	private static void test( String s ) { test( s, null );}
	private static void test( String descr, String expected ) {
		audit.log( ">>>>>>>"+ descr +"<<<<" );
		Item   item  = new Item( new Strings( descr ).contract( "=" ));
		String group = item.group(),
		       ans   = item.toString() + (group.equals("") ? "" : " "+ group);
		if (expected != null && !expected.equals( ans ))
			audit.FATAL( "the item: '" + ans +"'\n  is not the expected: '"+ expected +"'");
		else if (expected == null)
			audit.log( "is ===> "+ ans );
		else
			audit.log( " PASSED: "+ ans );
	}
	public static void main( String args[] ) {
		//Audit.allOn();
		//Audit.traceAll( true );
		Item.format( "QUANTITY,UNIT of,,from FROM,WHEN,"+ Where.LOCTR +" "+ Where.LOCTN );
		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffee from Tesco in London" );
		test( "black coffees quantity='2' unit='cup'", "2 cups of black coffee" );
		test( "black coffees quantity='1'", "a black coffee" );
		test( "black coffee quantity='2'", "2 black coffees" );
		test( "black coffees quantity='5 more' when='20151125074225'" );
		
		audit.title( "grouping" );
		Item.groupOn( Where.LOCTN );
		audit.debug("Using format:"+ Item.format.toString( Strings.CSV )
		          + (Item.groupOn().size() == 0
		        		  ? ", not grouped."
		                  : ", grouping on:"+ Item.groupOn() +"." ));

		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffee from Tesco in London" );
		test( "milk unit='pint' quantity=1 locator='from' location='the dairy aisle'",
				"a pint of milk from the dairy aisle" );
		//
		testAdd( "unit='cup' quantity='1' locator='from' location='Sainsburys' coffee" ); // subrc?
		testAdd( "quantity='1' locator='from' location='Sainsburys' biscuit" );
		testAdd( "locator='from' unit='pint' quantity='1' location='the dairy aisle' milk" );
		testAdd( "locator='from' location='the dairy aisle' cheese" );
		testAdd( "locator='from' location='the dairy aisle' eggs  quantity='6'" );
		testAdd( "toothpaste" );
		
		audit.title( "Groups" );
		audit.log( groups.toString() +"." );
		audit.log( "PASSED" );
}	}