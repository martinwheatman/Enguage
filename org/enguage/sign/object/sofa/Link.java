package org.enguage.sign.object.sofa;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Link {
	static public  final String NAME = "link";
	static public  final int      ID = 217371; //Strings.hash( "link" );
	static private       Audit audit = new Audit( "Link" );
	
	/* Need to support:
	 *   Composites : martin/hand holding ruth/hand: martin/hand/holding = "ruth/hand"
	 *   Relatives  : martin/lhand holding ../rhand: martin/lhand/holding = "../rhand"
	 */

	// **************
	// ************** FS Helpers - java fs model is s...  symlink-less!
	// **************
	static public final String EXT = ".symlink" ;
	static public boolean isLink( String s ) {
		return	s.length() > EXT.length()
				&& s.substring( s.length() - EXT.length()).equals( EXT );
	}
	static public String linkName( String name ) {return isLink( name ) ? name : name + EXT;}
	static public boolean fromString( String nm, String val ) {
		return Fs.stringToFile( Overlay.fname( linkName( nm ), Overlay.MODE_WRITE ), val );
	}
	static public String content( String nm ) { return Fs.stringFromFile( linkName( nm ));}
	
//	static private String extrd( String e, String a ) {return Overlay.fsname( e +"/"+ a, Overlay.MODE_READ );}
//	static private String extwr( String e, String a ) {return Overlay.fsname( e +"/"+ a, Overlay.MODE_WRITE );}

	// **************
	// ************** two recursive Link commands:
	// **************

	static private boolean exists(String entity, String attr, String value ) {
		Value  v   = new Value( entity, attr );
		String val = v.get();
		return v.exists() && (val.equals( value ) || exists( val, attr, value ));
	}
	static private boolean attribute(String e, String l, String a, String val ) {
		Value v;
		return !e.equals( "" ) &&
				(((v = new Value( e, a     )).exists() && v.equals( val )) ||
				 ((v = new Value( e, l+EXT )).exists() && attribute( v.get(), l, a, val )));
	}
	//---
	static private void usage( Strings a ) { usage( a.toString());}
	static private void usage( String a ) {
		audit.error(
				"Usage: link: [set|get|exists|attribute|destroy|delete] <ent> <link> [<value>]\n"+
				"given: "+ a );
	}
	static public Strings perform( Strings args ) {
		audit.in( "interpret", "["+ args.toString( Strings.CSV ) +"]" ); 
		Strings rc = Response.notOkay();
		int argc = args.size();
		if (argc >= 3 || argc <= 5) {
			rc = Response.okay();
			String	cmd    = args.remove( 0 ),
					entity = args.remove( 0 ),
					attr   = args.remove( 0 ),
					target = argc > 3 ? args.remove( 0 ) : "",
					value  = argc > 4 ? args.remove( 0 ) : "";
			
			// We now get passed and un-stripped attribute...
			if (Attribute.isAttribute( entity )) entity = new Attribute( entity ).value();
			if (Attribute.isAttribute( attr   )) attr   = new Attribute( attr   ).value();
			if (Attribute.isAttribute( target )) target = new Attribute( target ).value();
					
			if (cmd.equals("set") || cmd.equals( "create" ))
				rc = new Value( entity, attr+EXT ).set( target ) ? Response.okay() : Response.notOkay();
				
			else if (cmd.equals("get"))
				rc = new Strings( new Value( entity, attr+EXT ).get());
				
			else if (cmd.equals("exists"))
				rc = (target.equals( "" ) ?
						new Value( entity, attr+EXT ).exists() ? Response.yes() : Response.no()
						: exists( entity, attr+EXT, target ) ? Response.yes() : Response.no());
				
			else if (cmd.equals("delete"))
				if (target.equals( "" ))
					new Value( entity, attr+EXT ).ignore();
				else if (exists( entity, attr+EXT, target ))
					new Value( entity, attr+EXT ).ignore();
				else
					rc =  Response.notOkay();
				
			else if (cmd.equals("attribute"))
				rc = attribute( entity, attr, target, value ) ? Response.okay(): Response.notOkay();
			
			else
				usage( "cmd="+ cmd +", ent="+ entity +", attr="+ attr +", [ "+ args +" ]" );
		} else
			usage( args );
		return audit.out( rc );
	}
	private static Strings test( String cmd, String expected ) {
		Strings reply = perform( new Strings( cmd ).contract( "/" ));
		if (expected != null && !reply.equals( new Strings( expected )))
			audit.FATAL( cmd + "\nExpecting: "+ expected + "\n  but got: "+ reply );
		else
			Audit.passed();
		return reply;
	}
	public static void main( String args[] ) {
		
		Fs.root( null );
		
		Overlay.attach( "Link" );
		
		test( "create martin loves ruth",          Response.okay().toString() );
		test( "create martin hates name=\"ruth\"", Response.okay().toString() );
		test( "delete martin hates ruth",          Response.okay().toString() );
		test( "exists martin hates",        "no" );
		test( "exists martin hates ruth",   "no" );
		test( "exists martin loves",        "yes" );
		test( "exists martin loves ruth",   "yes" );
		test( "create engineer isa person", Response.okay().toString() );
		test( "create martin isa engineer", Response.okay().toString() );
		test( "exists martin isa",          "yes" );
		test( "exists martin isa person",   "yes" );
		test( "exists person isa martin",   "no" );
		
		new Value( "person", "age" ).set( "42" );
		test( "attribute martin isa age 42",     Response.okay().toString() );
		test( "attribute martin isa age 55",     Response.notOkay().toString() );
			
		Audit.PASSED();
}	}
