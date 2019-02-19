package org.enguage.objects.list;

import org.enguage.util.Audit;
import org.enguage.util.Strings;
import org.enguage.util.sys.Shell;

public class Transitive {
	
	public final static  String NAME = "transitive";
	static public  final long     id = Strings.hash( NAME );
	static Audit audit = new Audit( NAME );
	
	private static Strings    concepts = new Strings();
	public  static boolean  isConcept(  String   s ) { return concepts.contains( s ); }
	public  static void    addConcept(  String   s ) {if (!concepts.contains( s )) concepts.add( s );}
	public  static void    addConcepts( Strings ss ) {for (String s : ss) addConcept( s );}

	public static String list() { return concepts.toString( Strings.CSV );}
	
	static public Strings interpret( Strings args ) {
		audit.in( "interpret", args.toString() );
		String rc = Shell.IGNORE;
		if (args.size() > 1) {
			rc = Shell.FAIL;
			String cmd = args.remove( 0 );
			if (cmd.equals( "add" )) {
				addConcepts( args );
				rc = Shell.SUCCESS;
		}	}
		return audit.out( new Strings( rc ));
	}
	public static void main( String args[] ) {
		audit.log( interpret( new Strings( "add cause" )));
}	}