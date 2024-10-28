package org.enguage.sign.object;

import java.util.ListIterator;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Value;
import org.enguage.sign.symbol.number.Number;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Numeric extends Value {
	public static  final String NAME = "numeric";
	static public  final int      ID = 176168105; //Strings.hash( NAME );
	static private       Audit audit = new Audit( "Numeric" );
	
	public Numeric( String e, String a ) { super( e, a ); }
	
	public void  set( Float val ) {
		Fs.stringToFile(
				name( ent, attr, Overlay.MODE_WRITE ),
				Float.toString( val ));
	}
	public Float get( Float def ) {
		Float val = def;
		try {
			val = Float.valueOf( 
					Fs.stringFromFile( name( ent, attr, Overlay.MODE_READ ))
			);
		} catch (Exception e) {
			set( def ); // set a default
		}
		return val;
	}
	public boolean increase( String value ) {
		boolean rc = true;
		Float v = get( 0f );
		if (Float.isNaN( v )) {
			rc = false;
			audit.error("Numeric.increase(): NaN found in "+ ent +"/"+ attr );
		} else {
			v += Float.valueOf( value );
			set( v );
		}
		return rc;
	}
	public boolean decrease( String value ) {
		boolean rc = true;
		Float v = get( 0f );
		if (Float.isNaN( v )) {
			rc = false;
			audit.error("Numeric.decrease(): NaN found in "+ ent +"/"+ attr );
		} else {
			v -= Float.valueOf( value );
			set( v );
		}
		return rc;
	}
	static private String deref( String s ){
		try {
			s = Number.floatToString( Float.parseFloat( s ));
		} catch ( Exception e ) {
			if (s.equals( "-" )) s = "minus";
			else if (s.equals( "+" )) s = "plus";
			else if (s.equals( "*" )) s = "times";
			else if (s.equals( "/" )) s = "divided by";
		} // otherwise fail silently!
		return s;
	}
	static public Strings deref( Strings sa ){
		ListIterator<String> i = sa.listIterator();
		while (i.hasNext())
			i.set( deref( i.next()));
		return sa;
	}
	static private String usage( Strings a ) {
		System.out.println(
				"Usage: numeric [set|get|remove|increase|decrease|exists|equals|delete] <ent> <attr>[ / <attr> ...] [<values>...]\n"+
				"given: "+ a.toString( Strings.CSV ));
		return Response.notOkay().toString();
	}
	static public Strings perform( Strings a ) {
		// interpret( ["increase", "device", "textSize", "4"] )
		audit.in( "interpret", a.toString( Strings.DQCSV ));
		String rc = Response.okay().toString();
		if (a.size() > 1) {
			String cmd = a.get( 0 );
			
			if (cmd.equals("isAbs")) { // => Numeric.java?
				char firstChar = a.get( 1 ).charAt( 0 ); 
				rc = firstChar == '-' || firstChar == '+' ? Response.notOkay().toString() : Response.okay().toString();
				
			} else if (cmd.equals( "evaluate" )) {
				// parameters no longer expanded in sofa...!
				int i = 0;
				for (String s : a) {
					if (Attribute.isAttribute( s ))
						a.set( i, new Attribute( s ).value()) ;
					i++;
				}
				ListIterator<String> ai = a.normalise().listIterator();
				if (ai.hasNext()) {
					ai.next(); // read over command
					Number number = new Number( ai );
					rc = number.valueOf() + a.copyAfter( 1 + number.representamen().size()).toString();
				} else
					rc = Response.notOkay().toString();
				
			} else if (a.size() > 2) {
				int i = 2;
				String entity = a.get( 1 ), attribute = null;
				if (i<a.size()) { // components? martin car / body / paint / colour red
					attribute = a.get( i );
					while (++i < a.size() && a.get( i ).equals( "/" ))
						attribute += ( "/"+ a.get( i ));
				}
				Numeric n = new Numeric( entity, attribute );

				// [ "4", "+", "3" ] => [ "7" ] ???? at some point!
				ListIterator<String> ai = a.listIterator();
				String value = new Number( ai ).valueOf().toString(); /* <<<< this could 
				 * have "another", coz it has context. In practice, this is a single figure for 
				 * increase or decrease.
				 */
				
				if (cmd.equals( "increase" )) 
					rc = n.increase( value ) ? Response.okay().toString() : Response.notOkay().toString();
				else if (cmd.equals( "decrease" ))
					rc = n.decrease( value ) ? Response.okay().toString() : Response.notOkay().toString();
				else
					rc = usage( a );
			}
		} else
			rc = usage( a );
		audit.out( rc );
		return new Strings( rc );
}	}