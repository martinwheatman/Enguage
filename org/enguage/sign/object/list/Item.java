package org.enguage.sign.object.list;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.symbol.config.Plural;
import org.enguage.sign.symbol.number.Number;
import org.enguage.sign.symbol.when.Moment;
import org.enguage.sign.symbol.when.When;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Item {

	private static      Audit audit = new Audit("Item" );
	public static final String NAME = "item";
	public static final int      ID = 171847; //Strings.hash( NAME );
	
	/* The format has a default value "QUANTITY,UNIT of,THIS,LOCATOR LOCATION"
	 * The reasoning is from a Radio4 piece a year or two back, that there is a
	 * 'natural' order to qualifiers (adjectives?):-
	 *    Why we say, "a big yellow taxi", and not, "a yellow big taxi" :-)
	 */
	private static String  defFormat = "QUANTITY,UNIT of,THIS,LOCATOR LOCATION,WHEN";
	public  static void    resetFormat() { format( defFormat ); }
	
	private static Strings format = new Strings();
	public  static void    format( String csv ) { format = new Strings( csv, ',' );}
	public  static Strings format() { return format; }

	private static Strings groupOn = new Strings();
	public  static void    groupOn( String groups ) {groupOn = new Strings( groups );}
	public  static Strings groupOn() {return groupOn;}
	
	// members: name, desc, attr
	private String name = new String( NAME );
	public  String name() { return name; }
	public  Item   name( String s ) { name=s; return this; }
	
	private Strings descr = new Strings();
	public  Strings description() { return descr;}
	public  Item    description( Strings s ) { descr=s; return this;}
	
	private static ArrayList<Strings> isStuff = new ArrayList<Strings>();
	private static boolean isStuff( Strings s ) {return isStuff.contains( Plural.singular( s ));}
	private static void    stuffIs( Strings s ) {
		areThings.remove( Plural.plural( s ));
		isStuff.add( Plural.singular( s ));
	}
	
	private static ArrayList<Strings> areThings = new ArrayList<Strings>();
	private static boolean areThings( Strings s ) {return areThings.contains( Plural.plural( s ));}
	private static void    thingsAre( Strings s ) {
		isStuff.remove( Plural.singular( s ));
		areThings.add( Plural.plural( s ));
	}
	
	private Attributes attrs = new Attributes();
	public  Attributes attributes() { return attrs; }
	public  Item       attributes( Attributes a ) { attrs=a; return this; }
	public  String     attribute( String name ) { return attrs.value( name ); }
	public  void       replace( String name, String val ) { attrs.replace( name, val );}
	
	public Item() {};
	public Item( Item item ) { // copy c'tor
		name( item.name() );
		description( new Strings( item.description() ));
		attributes( new Attributes( item.attributes() ));
	}
	public Item( Strings ss ) { // [ "black", "coffee", "quantity='1'", "unit='cup'" ]
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
	public static Item next( ListIterator<String> si ) {
		Item it = null;
		if (si.hasNext() && si.next().equals( "<" ) &&
			si.hasNext() && si.next().equals( "item" )) // will be "/" on end list
		{
			it = new Item();
			it.attributes( new Attributes( si ));
			si.next(); // consume ">"
			it.description( Strings.copyUntil( si, "<" ));
			if (!si.hasNext() || !si.next().equals(    "/" ) &&
				!si.hasNext() || !si.next().equals( "item" ) &&
				!si.hasNext() || !si.next().equals(    ">" )   )
			{
				audit.error( "Missing end-item tag");
		}	}
		return it;
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
	public boolean equalsDescription( Item patt ) { // like Tag.equalsContent()
		return Plural.singular( descr ).equals( Plural.singular( patt.description() ));
	}
	public boolean matchesDescription( Item patt ){
		return Plural.singular( descr ).contains( Plural.singular( patt.description() ));
	}
	public boolean matchesAttributes( Item patt ){
		return attributes().matches( patt.attributes());
	}
	public boolean firstEquals( Item patt ){
		return attributes().first().equalsIgnoreCase( patt.attributes().first() );
	}
	public boolean equals( Item patt ) {
		return equalsDescription( patt )
				&& matchesAttributes( patt );
	}
	public boolean matches( Item patt ) {
		return matchesDescription( patt )
				&& matchesAttributes( patt );
	}
	// -----------------------------------------
	
	public void updateAttributes( Attributes newValues ) {
		// replace values with newValues, combining quantity
		audit.in( "updateAttributes", newValues.toString() );
		for (Attribute nval : newValues) {
			String value = nval.value(),
					name = nval.name();
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
		removed.magnitude( -removed.magnitude()).isRelative( true );
		replace( "quantity", quantity.combine( removed ).toString() );
		return (Item) audit.out( this );
	}
	
	// pluralise to the last number... e.g. n cups(s); NaN means no number found yet
	private Float  prevNum = Float.NaN;
	private boolean united = false;
	private String counted( Float num, String val ) {
		// N.B. val may be "wrong", e.g. num=1 and val="coffees"
		if (val.equals("1")) return "a"; // English-ism!!!
		return Plural.ise( num, val );
	}
	private final Strings unitary = new Strings( "1" );
	private String counted( Float num, Strings val ) {
		// N.B. val may be "wrong", e.g. num=1 and val="coffees"
		if (val.equals( unitary )) return "a"; // English-ism!!!
		return Plural.ise( num, val );
	}
	private Float getPrevNum( String val ) {
		ListIterator<String> si = new Strings( val ).listIterator();
		Float prevNum = new Number( si ).magnitude(); //Integer.valueOf( val );
		return prevNum.isNaN() ? 1.0f : prevNum;
	}
	private Strings getFormatComponentValue( String composite ) { // e.g. "from LOCATION"
		//audit.in( "getFormatComponentValue", "composite="+ composite )
		Strings value = new Strings();
		for (String cmp : new Strings( composite ))
			if ( Strings.isUpperCase( cmp )) { // variable e.g. "UNIT"
				if (groupOn().contains( cmp )) { // ["LOC"].contains( "LOC" )
					value=null; // IGNORE this component, and finish
					audit.debug( "toString(): ignoring:"+ cmp );
					break;
				} else { // cmp = "UNIT"
					String val = attributes().value( cmp );
					if (val.equals( "" )) {
						value=null; // this component is undefined, IGNORE
						break;
					} else if (cmp.equals(When.ID)) {
						When w = Moment.valid( val ) ?
								new When( new Moment( Long.valueOf( val ))) : // e.g. 2020012888888
								When.getWhen( new When(), new Strings( val )); //e.g. 'yesterday'
						value.add( w.toString() );
					} else if (cmp.equals(Where.LOCTN)
							|| cmp.equals(Where.LOCTR)) {
						value.add( val ); // don't count these!
					} else if (cmp.equals("QUANTITY")) {
						value.add( counted( prevNum, val ));
						prevNum = getPrevNum( val );
					} else if (cmp.equals("UNIT")) { // UNIT='cup(S)'
						value.add( counted( prevNum, val ));
						prevNum = Float.NaN;
						united = true;
					} else { // something else?
						value.add( counted( prevNum, val ));
						prevNum = Float.NaN;
				}	}
			} else // lower case -- constant
				value.add( cmp ); // e.g. "of"
		//audit.out( value )
		return value;
	}
	public String toXml() { return "<"+name+attrs+">"+descr+"</"+name+">";}
	public String toString() {
		Strings rc = new Strings();
		if (format.size() == 0)
			rc.appendAll( descr.size()>0? descr : new Strings( attributes().first().value() ));
		else {
			united = false;
			/* Read through the format string: ",from LOCATION"
			 * ADDING attributes: u.c. VARIABLES OR l.c. CONSTANTS),
			 * OR the description if blank string.
			 */
			for (String f : format) // e.g. f="from LOCATION"
				if (f.equals("")) { // main item: "black coffee", "crisps"
					if (united)
						if (isStuff( descr )) // coffee
							rc.appendAll( Plural.singular( descr ));
						else if ( areThings( descr )) // biscuits
							rc.appendAll( Plural.plural( descr ));
						else
							rc.appendAll( descr ); // get it wrong and hope user says no, X is stuff
						
					else
						rc.append( counted( prevNum, descr )); // 2 beers
					prevNum = Float.NaN;
				} else { // component, f, e.g. "UNIT of"
					Strings subrc = getFormatComponentValue( f );
					if (null != subrc) // ignore group name, and undefs
						rc.addAll( subrc );
				}
		}
		return rc.toString();
	}
	private Strings getFormatGroupValue( String f ) {
		boolean found = false;
		Strings value = new Strings();
		for (String cmp : new Strings( f ))
			if ( Strings.isUpperCase( cmp )) { // variable e.g. UNIT
				String val = attributes().value( cmp );
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
		if (format.isEmpty())
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
	public static Strings perform( Strings cmds ) {
		audit.in( "interpret", "cmds="+ cmds );
		Strings rc = Response.notOkay();
		if (!cmds.isEmpty()) {
			
			rc = Response.okay();
			String one = cmds.remove( 0 );
			
			if (one.equals( "ungroup" ))
				if (cmds.isEmpty())
					groupOn( "" );
				else
					rc = Response.notOkay();

			else if (one.equals( "groupby" ))
				if (cmds.isEmpty())
					rc = Response.notOkay();
				else
					groupOn( ""+cmds );
					
			else if (cmds.size() == 2) {

				String two = cmds.remove( 0 );
				String thr = cmds.remove( 0 );
			
				if (one.equals( "set" )
				 && two.equals( "format" ))
					format( Strings.stripQuotes( Attribute.getValue( thr )));
					
				else if (one.equals( "things" )
				      && two.equals( "include" ))
					thingsAre( new Strings( Strings.stripQuotes( thr )));
					
				else if (one.equals( "stuff" )
				      && two.equals( "includes" ))
					stuffIs( new Strings( Strings.stripQuotes( thr )));
					
				else
					rc = Response.notOkay();
			} else
				rc = Response.notOkay();
		}
		audit.out( rc );
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
		audit.debug( ">>>>>>>"+ descr +"<<<<" );
		Item   item  = new Item( new Strings( descr ).contract( "=" ));
		String group = item.group(),
		       ans   = item.toString() + (group.equals("") ? "" : " "+ group);
		if (expected != null && !expected.equals( ans ))
			audit.FATAL( "the item: '" + ans +"'\n  is not the expected: '"+ expected +"'");
		else if (expected == null)
			Audit.passed( " is ===> "+ ans );
		else
			Audit.passed( " PASSED: "+ ans );
	}
	public  static void main( String args[] ) {
		//Audit.on();
		//Audit.traceAll( true );
		Item.format( "QUANTITY,UNIT of,,from FROM,WHEN,"+ Where.LOCTR +" "+ Where.LOCTN );
		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffees from Tesco in London" );
		audit.debug( "adding b/c: "+ perform( new Strings( "stuff includes 'black coffee'" )));
		test( "black coffees quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffee from Tesco in London" );
		
		test( "black coffee quantity='2' unit='cup'", "2 cups of black coffee" );
		test( "black coffee quantity='1'", "a black coffee" );
		test( "black coffee quantity='2'", "2 black coffees" );
		test( "black coffee quantity='5 more' when='20151125074225'" );

		Item.format( "QUANTITY,UNIT of,,from FROM,WHEN,"+ Where.LOCTR +" "+ Where.LOCTN );
		test( "crisp quantity=2 unit='packet' from='Tesco' locator='in' location='London'",
				"2 packets of crisp from Tesco in London" );
		audit.debug( "adding crisps: "+ perform( new Strings( "things include crisps" )));
		test( "crisp quantity=2 unit='packet' from='Tesco' locator='in' location='London'",
				"2 packets of crisps from Tesco in London" );
		

		Audit.title( "grouping" );
		Item.groupOn( Where.LOCTN );
		audit.debug("Using format:"+ Item.format.toString( Strings.CSV )
		          + (Item.groupOn().size() == 0
		        		  ? ", not grouped."
		                  : ", grouping on:"+ Item.groupOn() +"." ));

		test( "black coffee quantity=1 unit='cup' from='Tesco' locator='in' location='London'",
				"a cup of black coffee from Tesco in London" );
		test( "milk unit='pint' quantity=2 locator='from' location='the dairy aisle'",
				"2 pints of milk from the dairy aisle" );
		test( "crisps unit='packets' quantity=2 locator='from' location='the dairy aisle'",
				"2 packets of crisps from the dairy aisle" );
		//
		testAdd( "unit='cup' quantity='1' locator='from' location='Sainsburys' coffee" ); // subrc?
		testAdd( "quantity='1' locator='from' location='Sainsburys' biscuit" );
		testAdd( "locator='from' unit='pint' quantity='1' location='the dairy aisle' milk" );
		testAdd( "locator='from' location='the dairy aisle' cheese" );
		testAdd( "locator='from' location='the dairy aisle' eggs  quantity='6'" );
		testAdd( "toothpaste" );
		
		Audit.title( "Groups" );
		audit.debug( groups.toString() +"." );
		
		Audit.PASSED();
}	}
