package org.enguage.sign.object.list;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Transitive {
	
	public  static final String NAME = "transitive";
	public  static final int      ID = 245880631; //Strings.hash( NAME )
	private static       Audit audit = new Audit( NAME );
	
	private static Strings    concepts = new Strings();
	public  static boolean  isConcept(  String   s ) { return concepts.contains( s ); }
	public  static void    addConcept(  String   s ) {if (!concepts.contains( s )) concepts.add( s );}
	public  static void    addConcepts( Strings ss ) {for (String s : ss) addConcept( s );}

	public static String list() { return concepts.toString( Strings.CSV );}
	
	private static Attributes attrs = new Attributes();
	public  static void add( String name, String value ) {
		Attribute a = new Attribute( value, name );
		if (!attrs.contains( a )) {
			a = new Attribute( name, value );
			if (!attrs.contains( a ))
				attrs.add( a );
	}	}
	public  static boolean are( String name, String value ) {
		return attrs.value( name ).equals( value );
	}

	public static Strings perform( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Perform.S_IGNORE;
		if (args.size() > 1) {
			rc = Response.notOkay().toString();
			String cmd = args.remove( 0 );
			if (cmd.equals( "add" )) {
				int sz = args.size();
				while (sz >= 2) {
					// this assumes left-to-right evaluation
					add( args.remove( 0 ), args.remove( 0 ));
					sz -= 2;
				}
				if (sz == 1)
					addConcepts( args );
				rc = Response.okay().toString();
		}	}
		return audit.out( new Strings( rc ));
	}
	public static void main( String[] args ) {
		audit.debug( perform( new Strings( "add cause" )));
		audit.debug( perform( new Strings( "add cause effect" )));
		audit.debug( "cause->effect: "+ are( "cause", "effect" ));
		audit.debug( "effect->cause: "+ are( "effect", "cause" ));
}	}
