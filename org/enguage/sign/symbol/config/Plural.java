package org.enguage.sign.symbol.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.TreeMap;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Plural {
	static public  final String NAME = "plural";
	static public  final int      ID = 195967030; //Strings.hash( NAME );
	static private       Audit audit = new Audit( "Plural" );
	
	// these hold full exceptions, not endings e.g. "colloquial" -> "colloquia"
	private static TreeMap<String,String> singularExceptions = new TreeMap<String,String>();
	private static TreeMap<String,String>   pluralExceptions = new TreeMap<String,String>();
	static public void addException( String s, String p ) {
		singularExceptions.put( s, p );
		pluralExceptions.put( p, s );
	}
	
	private static ArrayList<String> singularEndings = new ArrayList<String>();
	private static ArrayList<String> pluralEndings   = new ArrayList<String>();
	static public void addRule( String s, String p ) {
		singularEndings.add( s );
		pluralEndings.add( p );
	}
	
	static private boolean isPlural( String p ) {
		// first check exceptions
		if (pluralExceptions.containsKey( p )) return true;
		
		// then the rules
		int len = p.length();
		Iterator<String> pi = pluralEndings.iterator();
		while (pi.hasNext()) {
			String pluralEnding = pi.next();
			int engingLen = pluralEnding.length();
			if (len>=engingLen && p.substring( len-engingLen ).equals( pluralEnding ))
				return true;
		}
		
		// then plain old singular s -- should this be a rule?
		if (  len>=1 && p.substring( len-1 ).equals(  "s" )
		&&  !(len>=2 && p.substring( len-2 ).equals( "ss" ))) return true;
		
		// its not a plural
		return false;
	}
	static public boolean isSingular( String p ) {
		return singularExceptions.containsKey( p ) || !isPlural( p );
	}
	static public String singular( String p ) {
		// check if already singular
		if (!isPlural( p )) return p;
		
		// check if it is an exception
		String tmp;
		if (null != (tmp = pluralExceptions.get( p ))) return tmp;
		
		// check endings
		int len = p.length();
		Iterator<String> pi =   pluralEndings.iterator(),
		                 si = singularEndings.iterator();
		while (pi.hasNext()) { // pi and si are of equal length
			String   pluralEnding = pi.next(),
			       singularEnding = si.next();
			int engingLen = pluralEnding.length();
			if (len>=engingLen && p.substring( len-engingLen ).equals( pluralEnding )) 
				return p.substring( 0, len-engingLen ) + singularEnding;
		}
		// single s as plural -- can this be a rule too?
		if (  len>=1 && p.substring( len-1 ).equals(  "s" )
		&&  !(len>=2 && p.substring( len-2 ).equals( "ss" )))
			return p.substring( 0, len-1 );
		
		// doesn't get here
		return p; // already singular
	}
	static public Strings singular( Strings s ) {
		return new Strings( singular( s.toString()));
	}
	static public Strings plural( Strings s ) {
		return new Strings( plural( s.toString()));
	}
	static public String plural( String s ) {
		// already plural
		if (isPlural( s )) return s;
		
		// check if it is an exception
		String tmp;
		if (null != (tmp = singularExceptions.get( s ))) return tmp;
		
		// check endings
		int len = s.length();
		Iterator<String> pi =   pluralEndings.iterator(),
		                 si = singularEndings.iterator();
		while (pi.hasNext()) {
			String   pluralEnding = pi.next(),
			       singularEnding = si.next();
			int engingLen = singularEnding.length();
			if (len>=engingLen && s.substring( len-engingLen ).equals( singularEnding )) 
				return s.substring( 0, len-engingLen ) + pluralEnding;
		}
		if (s.length() == 1) return s;
		
		// added for concept file names
		Strings sa = new Strings( s, '_' );
		if (sa.size() == 2 && sa.get( 1 ).equals( "to" )) return plural( sa.get( 0 )) + "_to";
		
		return s + "s"; // rough and ready!
	}
	public static String ise( int i, String s ) {
		return i==1 ? Plural.singular( s ) : Plural.plural( s );
	}
	public static String ise( Float f, String s ) {
		return (s==null || s.equals( "" )) ?
				null
				: f.isNaN() ?
						s
						: f==1.0 ?
								Plural.singular( s )
								: Plural.plural( s );
	}
	public static String ise( Float f, Strings strs ) {
		return ise( f, strs.toString() );
	}
	public static Strings ise( Strings s ) {
		boolean pl = false;
		int prev = 1;
		ListIterator<String> si = s.listIterator();
		while (si.hasNext()) {
			String value = si.next();
			if (pl) si.set( ise( prev, value ));
			try {
				prev = Integer.valueOf( value );
				pl = true;
			} catch (Exception e) {pl = false;} // fail silently
		}
		return s;
	}
	public static Strings perform( Strings a ) {
		if (null == a)
			return Response.notOkay();
		else if (a.get( 0 ).equals("exception") && a.size() == 3)
			addException( a.get( 1 ), a.get( 2 ));
		else if (a.get( 0 ).equals("rule") && a.size() == 3)
			addRule( a.get( 1 ), a.get( 2 ));
		return Response.okay();
	/* Plurals:
	 * colloquial <=> colloquia  : because it is an exception
	 * princess   <=> princesses : *ss adds 'es'
	 * prince     <=> princes    : just adds 's'
	 */
	}
	public static void stest( String s ) {
		System.out.println( "the singular of "+ s +" is "+ singular( s ));
	}
	public static void ptest( String s ) {
		System.out.println( "the plural of "+ s +" is "+ plural( s ));
	}
	public static void main(String[] args) {
		Audit.on();
		Plural.addRule(  "y",  "ies" );
		Plural.addRule( "ss", "sses" );
		Plural.addException( "colloquial", "colloquia" ); // the plural of X is Y
		Plural.addException( "james",      "jameses" );   // the plural of X is Y
		Plural.addException( "this",       "these" );

		System.out.println( " ==== Plural tests:" );
		ptest( "colloquial" );
		ptest( "kings" );
		ptest( "queeny" );
		ptest( "princess" );
		ptest( "prince" );
		ptest( "james" );
		ptest( "these" );
		
		System.out.println( " ==== Singular tests:" );
		stest( "colloquia" );
		stest( "king" );
		stest( "queenies" );
		stest( "princesses" );
		stest( "princes" );
		stest( "james" );
		stest( "this" );
		
		Strings s = new Strings("4 , you need 4 cup of black coffee" );
		s = ise( s );
		audit.debug( "s is "+ s.toString());

		s = new Strings("you/need/2/yellow coffee", '/' );
		s = ise( s );
		audit.debug( "s is "+ s.toString());
}	}
