package org.enguage.sign.object.sofa;

import java.io.File;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Value {
	private static Audit audit = new Audit( "Value" );
	
	public  static final String   NAME = "value";
	public  static final int        ID = 10079711; //Strings.hash( NAME )
	private static       boolean debug = false;   // Enguage.runtimeDebugging
	
	protected String ent, attr;
	public String name() {return attr;}

	// constructor
	public Value( String e, String a ) {
		ent  = e;
		attr = a;
	}

	// statics
	public static  String name( String entity, String attr, int rw ) {
		return Overlay.fname( entity +(attr==null?"":"/"+ attr), rw );
	}
	
	// members
	// TODO: cache items[]? Lock file - this code is not suitable for IPC? Exists in constructor?
	// set() methods return change in number of items
	public boolean exists() {        return Fs.exists(         name( ent, attr, Overlay.MODE_READ ));}
	public boolean set( String val ){return Fs.stringToFile(   name( ent, attr, Overlay.MODE_WRITE ), val );}
	public void    unset() {                Fs.destroyEntity(  name( ent, attr, Overlay.MODE_WRITE ));}
	public String  get(){            return Fs.stringFromFile( name( ent, attr, Overlay.MODE_READ ));}
	public boolean isSet() {return !get().equals("");}
	
	public  boolean equals( String val ) { return get().equals( val ); }
	private boolean contains( String val ) { return get().contains( val ); }
	
	// this works..
	private static final String MARKER = "this is a marker file";
	public  boolean  ignore() {
		boolean rc = false;
		if (Fs.exists( name( ent, attr, Overlay.MODE_READ ))) {
			rc = true;
			String writeName = name( ent, attr, Overlay.MODE_WRITE );
			String deleteName = Overlay.deleteName( writeName );
			if ( Fs.exists( writeName )) { // rename
				File oldFile = new File(  writeName );
				File newFile = new File( deleteName );
				rc = oldFile.renameTo( newFile );
			} else { // create
				Fs.stringToFile( deleteName, MARKER );
		}	}
		return rc;
	}
	public boolean restore() {
		String writeName = name( ent, attr, Overlay.MODE_WRITE ); // if not overlayed - simply delete!?!
		String deletedName = Overlay.deleteName( writeName ); //dir/file => dir/!file
		File deletedFile = new File( deletedName );
		String content = Fs.stringFromFile( deletedName );
		if (content.equals( MARKER )) {
			if (debug) audit.debug( "Value.restore(): Deleting marker file "+ deletedFile.toString());
			return deletedFile.delete();
		} else {
		    File restoredFile = new File( writeName );
		    if (debug) audit.debug( "Value.restore(): Moving "+ deletedFile.toString() +" to "+ restoredFile.toString());
			return deletedFile.renameTo( restoredFile );
	}	}
	
	private static String usage( String cmd, String entity, String attr, Strings a ) {
		return usage( cmd +" "+ entity +" "+ attr +" "+ a.toString() );
	}
	private static String usage( String a ) {
		audit.debug( "Usage: [set|get|add|exists|equals|delete] <ent> <attr>{/<attr>} [<values>...]\n"+
				   "given: "+ a );
		return Response.notOkay().toString();
	}
	public static  Strings perform( Strings a ) {
		// sa might be: [ "add", "_user", "need", "some", "beer", "+", "some crisps" ]
		audit.in( "interpret", a.toString( Strings.CSV ));
		a = a.normalise();
		String rc = Response.okay().toString();
		if (a.size() > 2) {
			
			String       cmd = a.remove( 0 ); 
			String    entity = a.remove( 0 );
			String attribute = a.remove( 0 );
			
			// components? martin car / body / paint / colour red
			while (a.size()>1 && a.get( 0 ).equals( "/" )) {
				a.remove( 0 );
				attribute += ( "/"+ a.remove( 0 ));
			}
			Value v = new Value( entity, attribute ); //attribute needs to be composite: dir dir dir file values
			
			if (!a.isEmpty()) {
				// [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
				String value = a.contract("=").toString();
				if (Attribute.isAttribute( value ))
					value = new Attribute( value ).value();
				
				if (cmd.equals( "set" ))
					v.set( value );
				
				else if (cmd.equals( "equals" )) 
					rc = v.equals( value ) ? Response.okay().toString() : Response.notOkay().toString();
				
				else if (cmd.equals( "contains" ))
					rc = v.contains( value ) ? Response.okay().toString() : Response.notOkay().toString();
				
				else
					usage( cmd, entity, attribute, a );
				
			} else {
				if (cmd.equals( "get" ))
					rc = v.get();
				
				else if (cmd.equals( "unset" ))
					v.unset();
				
				else if (cmd.equals( "isSet" ))
					rc = v.isSet() ? Response.okay().toString() : Response.notOkay().toString();
				
				else if (cmd.equals( "delete" ))
					v.ignore();
				
				else if (cmd.equals( "undelete" ))
					rc = v.restore() ? Response.okay().toString() : Response.notOkay().toString() ;
				
				else if (cmd.equals( "exists" ))
					// could check to see if it contains <attribute>?
					rc = v.exists()  ? Response.okay().toString() : Response.notOkay().toString() ;
				
				else 
					rc = usage( cmd, entity, attribute, a );
			}
		} else
			rc = usage( a.toString() );
		audit.out( rc );
		return new Strings( rc );
	}
	private static void test( String cmd, String expected ) {
		Strings answer = perform( new Strings( cmd ));
		if (!answer.equals( new Strings( expected )))
			audit.error( "expecting:"+ expected +", but got: "+ answer );
		else
			Audit.passed();
	}
 	public static void main( String[] args ) {
		Overlay.set( Overlay.get());
		Overlay.attach( NAME );
		test( "set martin name martin j wheatman", "TRUE" );
		test( "get martin name",      "martin j wheatman" );
		Audit.PASSED();
}	}
