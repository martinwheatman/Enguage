package org.enguage.sign.symbol.where;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.enguage.sign.Assets;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;


public class Where {
	/** e.g. i need milk locator='from' location="the dairy aisle"
	 */

	public  static final String  NAME = "where";
	public  static final int       ID = 10654909; //Strings.hash( NAME )
	public  static       Audit  audit = new Audit( NAME );
	
	public  static final String LOCTR = "LOCATOR";
	public  static final String LOCTN = "LOCATION";
	public  static void clearLocation() {
		Variable.unset( Where.LOCTN );
		Variable.unset( Where.LOCTR );
	}

	private Where( Strings locr, Strings locn ) {
		addLocator( locr );
		addLocation( locn );
		assigned( locr != null && locn != null );
	}
	// all possible locators: spatially something can be ... .
	// e.g. [ ["in"], ["at"], ["in", "front", "of"], ...
	private  static ArrayList<Strings> locators = new ArrayList<>();
	private  static Strings isLocator( ListIterator<String> li ) {
		Strings rc;
		for (Strings locator : locators)
			if (!(rc = locator.extract( li )).isEmpty())
				return rc;
		return new Strings();
	}
	private  static void locatorIs( String l ) { locatorIs( new Strings( l )); }
	public  static  void locatorIs( Strings l ){ if (!l.isEmpty()) locators.add( l ); }

	private boolean assigned = false;
	public  boolean assigned() { return assigned; }
	public  Where   assigned( boolean l ) { assigned = l; return this; }

	private static Strings   concepts = new Strings();
	public  static boolean isConcept(  String s) {return concepts.contains( s );}
	public  static void   addConcept(  String s ) {if (s != null && !concepts.contains( s )) concepts.add( s );}
	private static void   addConcepts( Strings ss) {for (String s : ss) Where.addConcept( s );}
	
	// Was: location=["the", "pub"]
	// Now: location=[ ["the", pub"] ]
	private ArrayList<Strings> location = new ArrayList<>();
	public  List<Strings>      location() { return location; }
	private Where           addLocation( Strings l ) { location.add( l ); return this; }
	public  String             locationAsString( int n ) {return location.get( n ).toString();}
	
	// Was: locator="at" -- not "in front of"!!!
	// 2be: locator=[ ["at"] ]
	private ArrayList<Strings> locator = new ArrayList<Strings>(); //-- e.g. "in", "at", "in front of"
	public  ArrayList<Strings> locator() { return locator; }
	private Where           addLocator( Strings l ) { locator.add( l ); return this; }
	public  String             locatorAsString( int n ) {return locator.get( n ).toString();}

	// --
	public static Where getWhere( ListIterator<String> said, String terminator ) {
		Where w = null;
		if (said.hasNext()) {
			Strings locr = Where.isLocator( said );
			if (!locr.isEmpty()) {
				if (said.hasNext()) {
					Strings locn = new Strings();
					String word = said.next(); // typically "the"
					locn.add( word );
					
					if (null == terminator) {
						while (said.hasNext()) locn.add( said.next() );
						w = new Where( new Strings( locr ), locn ).assigned( true );
					} else {
						while (said.hasNext()) {
							word = said.next();
							if (!word.equals( terminator ))
								locn.add( word );
							else {
								// put unmacthed back
								said.previous();
								break;
						}	}
						
						if (said.hasNext())
							w = new Where( new Strings( locr ), locn ).assigned( true );
						else
							Strings.previous( said, locn.size() + locr.size());
					}
				} else
					Strings.previous( said, locr.size() );
		}	}
		return w;
	}

	// --
	public String toString() {
		return assigned() ? locatorAsString( 0 ) +" "+ locationAsString( 0 ) : "";
	}
	public static String list() { return concepts.toString( Strings.CSV );}

	public  static Strings perform( Strings args ) {
		//audit.in( "interpret", args.toString() )
		Strings rc = new Strings( Perform.S_IGNORE );
		if (!args.isEmpty()) {
			String cmd = args.remove( 0 );
			rc = Response.okay();
			if (cmd.equals( "add" ))
				addConcepts( args );
			else if (cmd.equals( "addCurrent" ))
				addConcept( Variable.get( Assets.NAME ));
			else if (cmd.equals( "locator" ))
				locatorIs( Attribute.value( args ));
			else
				rc = Response.notOkay();
		}
		//audit.out( rc )
		return rc;
	}

	public  static void doLocators( String locators ) {
		Strings locs = new Strings( locators, '/' );
		for (String l : locs) 
			locatorIs( l );
	}
	//
	// -- test code
	//
	private  static void testDoLocators() {
		// locators need to be in decreasing length...
		locatorIs( "to the left of" );
		locatorIs( "to the right of" );
		locatorIs( "in front of" );
		locatorIs( "on top of" );
		locatorIs( "behind" );
		locatorIs( "in" );
		locatorIs( "on" );
		locatorIs( "from" );
		locatorIs( "under" );
		locatorIs( "underneath" );
		locatorIs( "over" );
		locatorIs( "at" );
	}
	public static void main( String[] args) {
		Overlay.set( Overlay.get());
		Overlay.attach( NAME );
		testDoLocators();
}	}
