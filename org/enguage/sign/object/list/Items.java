package org.enguage.sign.object.list;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.sign.Config;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.Value;
import org.enguage.sign.symbol.number.Number;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Items extends ArrayList<Item> {
	static final long serialVersionUID = 0L;
	static final public  String   NAME = "items";
	static       private Audit   audit = new Audit( NAME );
	static final public  int        ID = 4468041; //Strings.hash( NAME );
	
	Value value;
	public void ignore() {value.ignore();}
	public void restore() {value.restore();}
	
	Attributes attrs = new Attributes();
	
	public Items( String ent, String attr ) {
		value = new Value( ent, attr );
		fromXml(
			new Strings( value.get() ).listIterator()	
		);
	}
	
	// --- to/from XML ---
	private void fromXml( ListIterator<String> si ) {
		if (si.hasNext() && si.next().equals(     "<"      ) &&
			si.hasNext() && si.next().equals( value.name() ) &&
			si.hasNext() )
		{
			Item it;
			attrs = new Attributes( si );
			String tmp = si.next();
			if (tmp.equals( ">" ))
				while (null != (it = Item.next( si ))) add( it );
			else if (!tmp.equals( "/" ))
				audit.error( "unknown token in Xml file: "+ tmp );
	}	}
	private String toXml() {
		String list = "";
		for (Item item : this)
			list += item.toXml()+"\n      ";
		return "<"+value.name()+ attrs.toString() +(list.equals("") ? "/" : ">"+ list +"</"+value.name()) +">";
	}
	public  String toString() { return toString( null ); }
	private String toString( Item pattern ) { // to Items class!
		audit.in( "get", "item="+ (pattern==null?"ALL":pattern.toXml()));
		Groups g=new Groups();
		for (Item item : this)
			if (pattern == null || item.matches( pattern ))
				g.add( item.group(), item.toString());
		return audit.out( g.toString());
	}
	// -------------------------------
	
	// --- List Processing Methods ---
	private int index( Item item, boolean exact ) {
		//audit.in( "find", "lookingFor="+ item.toXml() +" f/p="+ (exact ? "FULL":"partial"));
		
		String ilocr = item.attribute( Where.LOCTR );
		String ilocn = item.attribute( Where.LOCTN );
		long   iwhen = item.when();
		boolean desc = item.description().size() > 0;
		
		int pos = -1;
		for (Item li : this) {
			pos++;
			String tlocr = li.attribute( Where.LOCTR );
			String tlocn = li.attribute( Where.LOCTN );
			long   tt    = li.when();
			if ( (iwhen == -1 || iwhen == tt) // if tt == -1 && it != -i fail!
				&& (!exact || (
					   (ilocr.equals( "" ) || ilocr.equals( tlocr ))
					&& (ilocn.equals( "" ) || ilocn.equals( tlocn )))
				)
				&&     (( exact && li.equals( item ))
			         || (!exact && (desc ? li.matchesDescription( item ):li.firstEquals( item ))))
			 )
				return pos; //audit.out( pos );
		}
		return -1; //audit.out( -1 );
	}
	private boolean exists(Item item, Strings params) {
		String lastParam = params.get( params.size() - 1 );
		return index( item,
				!(  lastParam.equals( "quantity='some'" )
				  ||lastParam.equals( "quantity='any'"  ))) != -1;
	}
	private int matches( Item item ) {
		audit.in( "matches", "item="+ item.toXml());
		int pos = -1;
		for (Item li : this) {
			audit.debug( "matching: "+ li.toXml() );
			pos++;
			if (li.matches( item ))
				return audit.out( pos ); // pos;
		}
		return audit.out( -1 ); // -1;
	}
	private String quantity( Item item, boolean exact ) { // e.g. ["cake slices","2"]
		audit.in( "quantity", "Item="+item.toString() + ", exact="+ (exact?"T":"F"));
		Integer count = 0;
		boolean desc = item.description().size() > 0;
		for (Item t : this ) // go though the file
			if (   ( exact && t.equals( item ))
			    || (!exact && (desc ? t.matchesDescription( item ):t.firstEquals( item ))))
				count += t.quantity();
		audit.out( count );
		return count.toString();
	}
	private String append( Item item ) { // adjusts attributes, e.g. quantity
		String rc = item.toString()+ " "+ item.group(); // return what we've just said
		audit.in( "append", item.toXml() +" ("+ rc +")" );
		int n = index( item, false ); // exact match? No!
		if (-1 == n) { 
			String quantity = item.attribute( "quantity" );
			Number number = new Number( quantity );
			if (quantity.equals( "" ) || number.magnitude() != 0.0f) {
				// combining nothing with a relative number
				// in case: quantity='+= 37' => set it to '37'
				if (number.isRelative()) {
					number.isRelative( false );
					item.replace( "quantity", number.toString() );
				}
				add( item );
			}
		} else { // found so update item...
			Item removedItemTag = remove( n );
			removedItemTag.updateAttributes( item.attributes() );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || new Number( quantity ).magnitude() != 0.0f)
				add( n, removedItemTag );
		}
		value.set( toXml() );
		return audit.out( rc );
	}
	private String addItem( Item item ) { 
		String rc = item.toString()+ " "+ item.group(); // return what we've just said
		audit.in( "addItem", item.toXml() +" ("+ rc +")" );
		int n = index( item, true ); // exact match? Yes, don't add duplicates!
		if (-1 == n) { 
			add( item );
		}
		value.set( toXml() );
		return audit.out( rc );
	}
	private String update( Item item ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		int n = index( item, false ); // exact match? No!
		if (-1 != n) { 
			Item removedItemTag = remove( n );
			removedItemTag.updateAttributes( item.attributes() );
			String quantity = removedItemTag.attribute( "quantity" );
			if (quantity.equals( "" ) || new Number( quantity ).magnitude() != 0.0F)
				add( n, removedItemTag );
			value.set( toXml() );
		}
		return audit.out( rc );
	}
	private String delAttr( Item item, String name ) { // adjusts attributes, e.g. quantity
		String rc = item.toString(); // return what we've just said
		audit.in( "delAttr", "item:"+ item.toXml() +", name="+ name );
		int n = index( item, false ); // exact match? No!
		if (-1 != n) {
			Item tmp = remove( n );
			tmp.attributes().remove( name );
			add( n, tmp );
			value.set( toXml() ); // was set( lines );
		} else
			audit.error("not found "+ item.toXml());
		return audit.out( rc );
	}
	private Strings getAttrVal( Item item, String name ) {
		Strings   rc = new Strings();
		audit.in( "getAttrVal", "item='"+ item.toXml() +"', name="+ name );
		boolean desc = item.description().size() > 0;
		for (Item t : this) 
			if ((item == null || (desc ? t.matchesDescription( item ):t.firstEquals( item )))
				&& t.attributes().hasName( name ))
					rc.add( t.attributes().value( name ));
		return audit.out( rc );
	}
	private boolean isLinked( Attribute from, Attribute to) {
		/*  isLinked( cause="X", effect="Z" )
		 * it.item contains attrs: [cause='X', effect='Y'], [cause='Y', effect='Z']
		 * if trans, swap cause value to effect and re-search
		 */
		audit.in( "isLinked", "CHECKING: from: "+ from +", to: '"+ to +"'");

		// sanity check
		if (to.value().equals("") || from.value().equals("")) {
			audit.debug( "bailing out" );
			return audit.out( false );
		}

		for (Item li : this)
			if (li.attributes().contains( from, to ))
				return audit.out( true );

		for (Item li : this) 
			if (  !li.attributes().value(   to.name() ).equalsIgnoreCase(   to.value()) // loop check - same dest
				&& li.attributes().value( from.name() ).equalsIgnoreCase( from.value()) // a='x' A='X'
				&& isLinked( new Attribute( from.name(), li.attributes().value( to.name() )), to ))
						return audit.out( true );

		return audit.out( false );
	}
	/* this needs to include adjusting quantity downwards, as above in add()
	 * i need coffee + I have 3 coffees = ???
	 * Returns number removed: existing - to be removed.
	 */
	private String removeQuantity( Item tbr, boolean exact ) {
		String rc = Response.notOkay().toString();
		audit.in("removeQuantity", "item="+ tbr.toXml() +", exact="+ (exact?"T":"F"));
		
		int n;
		if (-1 != (n = index( tbr, exact ))) {
			Item tmp = remove( n );
			
			if (   tbr.attributes().hasName( "quantity" )
			    && tmp.attributes().hasName( "quantity" ))
			{
				String maxRemoval = new Number( tmp.attribute( "quantity" )).toString();
				
				tmp.removeQuantity( new Number( tbr.attribute( "quantity" )));
				tmp.description( tbr.description() ); // will replace crisp with crisps...
				
				if (new Number( tmp.attribute( "quantity" )).magnitude() <= 0) {
					tbr.replace( "quantity", maxRemoval );
					tmp.replace( "quantity", "0" );
				} else
					add( n, tmp );
				rc = tbr.toString(); // prepare return value
				
			} else if (   ( exact && tmp.equals( tbr ))
			           || (!exact && tmp.matches( tbr )))
				/* ...or, as before, remove whole item or all items...
				 *	// i need milk/i have milk
				 *	// i need milk/i have 37 milks
				 *	// i need 37 milks/i have milk
				 */
				rc = tbr.toString(); // prepare return value
				
		}
		if (!rc.equals( Response.notOkay().toString() ))
			value.set( toXml() ); // put list back...
		return audit.out( rc );
	}
	private String removeAll( Item tbr ) {
		audit.in( "removeAll", tbr==null?"<ALL>":tbr.toXml());
		ArrayList<Item> reprieve = new ArrayList<Item>();
		while (size() > 0) {
			Item itm = remove( 0 );
			if (tbr != null && !itm.attributes().matches( tbr.attributes() ))
				reprieve.add( itm );
		}
		for (Item itm : reprieve) add( itm );
		value.set( toXml() );
		return audit.out( Response.okay().toString() );
	}
	static public Strings perform( Strings sa ) {
		
		// first dereference 2nd and 3rd parameters
		sa = Attribute.expand23( sa );

		{ // Then append tempro/spatial awareness if it has been added. 
			String when = Context.get( "when" );
			if (!when.equals(""))
				sa.append( Attribute.asString( "WHEN", when ) );
			String locator = Context.get( Where.LOCTR );
			if (!locator.equals("")) {
				String location = Context.get( Where.LOCTN );
				if (!location.equals("")) {
					sa.append( Attribute.asString( Where.LOCTR, locator  ));
					sa.append( Attribute.asString( Where.LOCTN, location ));
		}	}	}


		
		/* An item may be <item>black coffee</item>, or
		 * <item unit="cup" quantity="1">black coffee</item>
		 */
		/* What we're doing here is to process the parameters provided in 
		 * the repertoire as processed by Intention.class (attributes are 
		 * expanded) i.e. X ==> x='xvalue'
		 * At this point the "list" is stripped from that conceptualisation.
		 * We need to ensure that the first 5 parameters are re-expanded 
		 * (so black coffee is one value/parameter):
		 * (list) get martin needs black coffee quantity='1', or perhaps...
		 * (list) get martin needs  quantity='1' black coffee
		 * This is complicated by the fact that a phrase may have an "and"
		 * in it which means the first (or last) param of each component 
		 * needs to be converted, and the operation called for each.
		 */
		Strings rc = Response.notOkay();
		audit.in( "perform", sa.toString());
		
		String	cmd = sa.remove( 0 ),
				ent = sa.remove( 0 ), 
				atr = sa.remove( 0 ),
				attrName = cmd.equals( "getAttrVal" ) ? sa.remove( 0 ) : "";
		
		Items list = new Items( ent, atr );
		
		if (cmd.equals( "delete" )) {
			list.ignore();
			rc = Response.okay();
			
		} else if (cmd.equals( "undelete" )) {
			list.restore();
			rc = Response.okay();
				
		} else if (sa.size() == 0) {
			if (cmd.equals("get"))
				rc = new Strings( list.toString());
			
			else if (cmd.equals( "removeAll" )) {
				list.removeAll( (Item)null );
				rc = Response.okay();
			}
		
		} else if (cmd.equals( "isLinked" )) {
			
			Attribute from = new Attribute( sa.remove( 0 ));
			Attribute   to = new Attribute( sa.remove( 0 ));

			if (Transitive.are( from.name(), to.name() ))
				rc = list.isLinked( from, to )
						? Response.okay() : Response.notOkay();
			else {
				rc = Response.notOkay();
				for (Item li : list)
					if (li.attributes().contains( from, to )) {
						rc = Response.okay();
						break;
			}		}

		} else {
			Strings paramsList = new Strings( sa ),
			        rca        = new Strings();
			
			for (Strings params : paramsList.divide( "and" )) {
				
				Item item = new Item( params );
				
				if (cmd.equals( "exists" )) {
					if (list.exists( item, params )) {
						if (rca.size() == 0) rca.add( Response.okay().toString() );
					} else {
						rca = new Strings();
						rca.add( Response.notOkay().toString() );
						break;
					}
					
				} else if (cmd.equals( "notExists" )) {
					if (!list.exists( item, params )) {
						if (rca.size() == 0) rca.add( Response.okay().toString() );
					} else {
						rca = new Strings();
						rca.add( Response.notOkay().toString() );
						break;
					}
					
				} else if (cmd.equals( "matches" )) {
						if (list.matches( item ) != -1) {
							if (rca.size() == 0) rca.add( Response.okay().toString() );
						} else {
							rca = new Strings();
							rca.add( Response.notOkay().toString() );
							break;
						}
						
				} else if (cmd.equals( "delAttr" )) {
					// Typically: delAttr SUBJECT LIST THIS NAME
					list.delAttr(
							item,
							new Attribute( sa.get( 1 )).value()
					);
					rca.add( Response.okay().toString() );
					
				} else if (cmd.equals( "getAttrVal" )) {
					
					// Typically: getAttrVal SUBJECT LIST NAME THIS
					rca.add( list.getAttrVal(
								item,
								Attribute.getValue( attrName ) // Expand: n='v' => v
							).toString( Config.andListFormat())
					);

				} else if (cmd.equals( "quantity" )) {
					rca.add( list.quantity( item, false ));
					
				} else if (cmd.equals( "remove" )) {
					// must return those left... so need 10, have 6, return 4.
					rca.add( list.removeQuantity( item, false ));
					
				} else if (cmd.equals( "removeItem" )) {
					rca.add( list.removeQuantity( item, true ));
					
				} else if (cmd.equals( "removeAny" )) {
					while (-1 != list.index( item, false ))
						rca.add( list.removeQuantity( item, false ));
					
				} else if (cmd.equals( "removeAll" )) {
					rca.add( list.removeAll( item ));
					
				} else if (cmd.equals( "add" )) {
					rca.add( list.append( item ));
					
				} else if (cmd.equals( "addItem" )) {
					rca.add( list.addItem( item ));
					
				} else if (cmd.equals( "update" )) {
					rca.add( list.update( item ));
					
				} else if (cmd.equals( "get" ))
					rca.add( list.toString( item ));
			}
			// some (e.g. get) may have m-values, some (e.g. exists) only one
			rc = rca.size() == 0 ?
					Response.notOkay() : new Strings( rca.toString( Config.andListFormat()));
		}
		return audit.out( rc );
	}
	// ----------------------------------------------------------------------------------
	// params( "one two = three" ]) => [ "one", "two=three" ]
	static public Strings params( String s ) {
		return new Strings( s ).contract( "+=" ).contract( "-=" ).contract( "=" );
	}
	static public void test( int id, String cmd, String result ) {
		Strings s = Items.perform( params( cmd ));
		if (!result.equals( "" ) && !s.equals( new Strings( result )))
			audit.FATAL(
					(id != -1 ? id +": " : "")+
					cmd +", returns:\n"
					+ "\t'"+ s +"'\n"
					+ "but should return:\n"
					+ "\t'"+ result +"'"
			);
		else
			Audit.passed( result );
	}
	static public void test( int id, String cmd ) { test( id, cmd, "" );}
	static public void test( String cmd ) { test( -1, cmd, "" );}
	static public void test( String cmd, String result ) {test( -1, cmd, result );}
	
	public static void main( String[] argv ) {
		
		Items l = new Items( "martin", "needs" );
		l.append( new Item( new Strings( "coffee   unit='cup' quantity='1' locator='from' location='Sainsburys'"   )));
		l.append( new Item( new Strings( "biscuits            quantity='1' locator='from' location='Sainsburys'"   )));
		l.append( new Item( new Strings( "locator='from' unit='pint' quantity='1' location='the dairy aisle' milk" )));
		audit.debug( "martin needs: "+ l.toString());
		
		Item.format( "QUANTITY,UNIT of,,"+ Where.LOCTR +" "+ Where.LOCTN );
		Item.groupOn( Where.LOCTN );
		audit.debug( "get martin needs: "+ perform( new Strings( "get martin needs" )));
		
		Audit.title( "SHOPPING LIST TESTS..." );
		Item.format( "QUANTITY,UNIT of,THIS,from FROM" );
		test( 51, "delete martin needs", "TRUE" );
		test( 52, "add martin needs object='coffee' quantity='1'", "a coffee" );
		test( 53, "add martin needs object='coffee' quantity='another'", "another coffee" );
		test( 55, "get martin needs", "2 coffees" );

		Audit.title( "SHOPPING LIST TESTS..." );
		Item.format( "QUANTITY,UNIT of,,from FROM" );
		test( 101, "delete martin needs", "TRUE" );
		test( 102, "add martin needs coffee quantity='1'", "a coffee" );
		test( 103, "add martin needs coffee quantity='8 more'", "8 more coffees" );
		test( 104, "add martin needs milk quantity='6' unit='pint'",   "6 pints of milk" );
		test( 105, "get martin needs", "9 coffees, and 6 pints of milk" );

		// remove more milk than we've got, and not all coffees
		test( 106, "remove martin needs milk quantity='10' unit='pint'", "6 pints of milk" ); 
		test( 107, "remove martin needs coffees quantity='6'", "6 coffees" );
		test( 108, "get martin needs", "3 coffees");

		test( 109, "exists martin needs coffees quantity='3'",   "TRUE" );
		test( 110, "exists martin needs coffees quantity='any'", "TRUE" );
		test( 111, "exists martin needs coffees quantity='1'",  "FALSE" );
		
		test( 112, "removeAny martin needs coffee", "coffee" );
		test( 113, "get martin needs", "");
		
		Audit.title( "Calendar list tests..." );
		Item.groupOn( "" ); // reset
		Item.format( ","+Where.LOCTR +" "+ Where.LOCTN+",WHEN" );
		test( 201, "add _user meeting fred locator='at' location='the pub' when='20151225190000'",
				  "fred at the pub at 7 pm on the 25th of December , 2015" );
		test( 202, "add _user meeting fred locator='at' location='the pub' when='20151225193000'",
				  "fred at the pub at 7 30 pm on the 25th of December , 2015" );
		
		Audit.title( "why" );
		l = new Items( "martin", "causal" );
		
		// two linked cause-effects
		Attributes one = new Attributes();
		one.add( new Attribute( "cause",  "i am baking a cake" ));
		one.add( new Attribute( "effect", "i need 3 eggs" ));
		
		Attributes two = new Attributes();
		two.add( new Attribute( "cause",  "i need 3 eggs" ));
		two.add( new Attribute( "effect", "i need to go to the shop" ));
		
		// non-linked cause
		Attributes ten = new Attributes();
		ten.add( new Attribute( "cause",  "sophie is very fashionable" ));
		ten.add( new Attribute( "effect", "sophie needs dr martens" ));
		
		l.append( new Item().attributes( one ));
		l.append( new Item().attributes( two ));
		l.append( new Item().attributes( ten ));
		l.value.set( l.toXml() );
		
		
		//test( 300, "getAttrVal martin why name='cause' i need 3 eggs", "i am baking a cake" );
		audit.debug( l.toXml());
		//Audit.on();
		Transitive.add( "cause", "effect" );
		test( 301, "isLinked martin causal  cause='i am baking a cake' effect='i need to go to the shop'", "TRUE" );
		test( 302, "isLinked martin causal effect='i am baking a cake'  cause='i need to go to the shop'", "FALSE" ); //not trans
		test( 303, "isLinked martin causal  cause='i am baking a cake' effect='sophie is so fashionable'", "FALSE" );
		
		Audit.PASSED();
}	}
