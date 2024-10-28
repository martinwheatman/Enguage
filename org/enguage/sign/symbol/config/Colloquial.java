package org.enguage.sign.symbol.config;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Colloquial {
	static public  final String NAME = "colloquial";
	static public  final int      ID = 42718199; // Strings.hash( NAME );
	static private       Audit audit = new Audit( "Colloquial" );
	
	private TreeMap<Strings,Strings> intl;
	private TreeMap<Strings,Strings> extl;

	public Colloquial() {
		intl = new TreeMap<Strings,Strings>();
		extl = new TreeMap<Strings,Strings>();
	}

	private void add( Strings ex, Strings in ) {
		intl.put( ex, in );
		extl.put( in, ex );
	}
	// ---
	// this doesn't spot overlapping colloquia...
	// wandered lonely --> moved aimlessly
	// aimlessly as a cloud --> duff analogy
	// lonely as a cloud --> very lonely
	// I wandered lonely as a cloud --> I moved duff analogy (not as required: I moved aimlessly very lonely.)
	public Strings externalise( Strings a ) {
		Set<Map.Entry<Strings,Strings>> set = extl.entrySet();
		Iterator<Map.Entry<Strings,Strings>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Strings,Strings> me = (Map.Entry<Strings,Strings>)i.next();
			//audit.debug("Coll: externalising:"+ me.getValue() +":with:"+ me.getKey() +":");
			a.replace( me.getValue(), me.getKey());
		}
		return a;
	}
	public String toString() {
		String str = "";
		Set<Map.Entry<Strings,Strings>> set = extl.entrySet();
		Iterator<Map.Entry<Strings,Strings>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Strings,Strings> me = (Map.Entry<Strings,Strings>)i.next();
			str += me.getValue() +"/"+ me.getKey() +"; ";
		}
		return str;
	}
	public Strings internalise( Strings a ) {
		Set<Map.Entry<Strings,Strings>> set = intl.entrySet();
		Iterator<Map.Entry<Strings,Strings>> i = set.iterator();
		while(i.hasNext()) {
			Map.Entry<Strings,Strings> me = (Map.Entry<Strings,Strings>)i.next();
			a.replace( me.getValue(), me.getKey());
		}
		return a;
	}
	
	static private Colloquial user = new Colloquial();
	static public  Colloquial user() { return user; }
	
	static private Colloquial host = new Colloquial();
	static public  Colloquial host() {return host;}
	
	static private Colloquial symmetric = new Colloquial();
	static public  Colloquial symmetric() {return symmetric;}
	
	static public String  applyOutgoing( String s ) {return applyOutgoing( new Strings( s )).toString();}
	static public Strings applyOutgoing( Strings s ) {
		return symmetric().externalise(      // 2. [ "You", "do", "not", "know" ] -> [ "You", "don't", "know" ]
				    host().externalise( s ));// 1. [ "_user", "does", "not", "know" ] -> [ "You", "do", "not", "know" ]
	}
	static public String  applyIncoming( String s ) {return applyIncoming( new Strings( s )).toString();}
	static public Strings applyIncoming( Strings s ) {
		/* this is called in Repertoires.interpret(), so that any colloquia
		 * used in the repertoire files are correctly interpreted.
		 */
		return  user().internalise(       // user phrases expand
		   symmetric().internalise( s )); // general expansion
	}
	static public Strings interpret( String  a ) { return perform( new Strings( a )); }
	static public Strings perform( Strings a ) {
		if (null == a) return Response.notOkay();
		//audit.in( "interpret", a.toString( Strings.CSV ));
		
		// expand 2nd and 3rd attribute parameters
		ListIterator<String> ci = a.listIterator();
		if (ci.hasNext()) {
			ci.next();                          // ignore 0
			if (ci.hasNext()) {
				String attr = ci.next();               // 1
				ci.set( Attribute.getValue( attr ));
				if (ci.hasNext()) {
					attr = ci.next();                  // 2
					ci.set( Attribute.getValue( attr ));
		}	}	}

		if (a.size() >= 3) {
			
			Strings intl, extl;
			if (a.size() == 3) {
				intl = new Strings( Strings.trim( a.get( 1 ), Attribute.ALT_QUOTE_CH ));
				extl= new Strings( Strings.trim( a.get( 2 ), Attribute.ALT_QUOTE_CH ));
			} else {
				intl  = new Strings();
				extl = new Strings();
 				boolean doingFirst = true;
				for (int i=1; i<a.size(); i++) {
					String item = a.get( i );
					if ( item.equals( "=" ))
						doingFirst = false;
					else if ( doingFirst )
						intl.add( item );
					else
						extl.add( item );
			}	}
			
			if (a.get( 0 ).equals("both"))
				symmetric().add( intl, extl );
			else if (a.get( 0 ).equals("host"))
				host().add( intl, extl );
			else if (a.get( 0 ).equals("user"))
				user().add( intl, extl );
			else
				audit.error( "Colloquial.interpret(): unknown command: "+ a.toString( Strings.CSV ));
		} else
			audit.error( "Colloquial.interpret(): wrong number of params: "+ a.toString( Strings.CSV ));
		//return audit.out( Shell.Success );
		return Response.okay();
	}
	public static void main( String args[] ) {
		Audit.on();
		Strings a = new Strings( "This test passes" );
		Colloquial c = new Colloquial();
		c.add( new Strings( "This" ), new Strings( "Hello" ));
		c.add( new Strings( "test passes" ), new Strings( "world" ));
		a = c.externalise( a );
		audit.debug( a );
		a = c.internalise( a );
		audit.debug( a );
		
		interpret( "both \"do not\" \"don't\"" );
		interpret( "both \"does not\"  \"doesn't\"" );
		interpret( "both \"cannot\" \"can't\"" );
		interpret( "both \"can not\" \"cannot\"" );
		interpret( "both \"I have\" \"I've\"" );
		interpret( "both \"i am\" \"I'm\"" );
		interpret( "both \"i would\" \"I'd\"" );
		interpret( "both \"i will\" \"I'll\"" );
		interpret( "both \"you are\" \"you're\"" );
		interpret( "both \"you would\" \"you'd\"" );
		interpret( "both \"you will\" \"you'll\"" );
		interpret( "both \"fish'n'chips\" \"fish and chips\"" );

		interpret( "host \"_host's\" \"my\"" );
		interpret( "host \"_user's\" \"your\"" );
		interpret( "host \"_user needs\" \"you need\"" );
		interpret( "host \"_user does not need\" \"you do not need\"" );
		interpret( "host \"_user\" \"you\"" );

		audit.debug( applyIncoming( "I do not need anything" ));
		
		audit.debug(
			applyOutgoing(
				"_user needs: i do not understand, i do not need anything, _user does not need anything."
			));
		audit.debug( "PASSED" );
}	}
