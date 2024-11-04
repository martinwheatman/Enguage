package org.enguage.sign.object.sofa;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

class EntityShell extends Shell {
	EntityShell() { super( "Entity" );}
	public Strings interpret( Strings argv ) { return Entity.perform( argv ); }
}

public class Entity {
	public  static  final String NAME = "entity";
	private static        Audit audit = new Audit( "Entity" );
	public  static  final int      ID = 66162693; // Strings.hash of "entity" 
	
	public static boolean exists( String name ) {
		return Fs.exists( Overlay.fname( name, Overlay.MODE_READ ));
	}

	public static boolean create( String name ) {
		audit.in( "create", "name='"+ name +"' ("+ Overlay.fname( name, Overlay.MODE_WRITE ) +")" );
		boolean rc = Fs.create( Overlay.fname( name, Overlay.MODE_WRITE ));
		audit.out( rc );
		return rc;
	}
	
	// really should be in a corresponding Component.c module!
	public static boolean createComponent( Strings a ) {
		boolean rc = false;
		StringBuilder name = new StringBuilder();
		for (int i=0, sz=a.size(); i<sz; i++) { // ignore all initial unsuccessful creates
			name.append( a.get( i ));
			rc = Fs.create( name.toString() );
			name.append( "/" );
		}
		return rc;	
	}
	public static boolean delete( String name ) {
		boolean rc = true;
		String readName  = Overlay.fname( name, Overlay.MODE_READ );
		if (Fs.exists( readName )) {
			String writeName = Overlay.fname( name, Overlay.MODE_WRITE );
			String dname = Overlay.deleteName( writeName );
			if (!Fs.destroy( writeName )) {
				// haven't managed to remove top overlay entity -- either not empty or not there
				rc = Fs.exists( writeName ) ?
					Fs.rename( writeName, dname ) : // ...it is there, so rename it!
					Fs.create( dname ); //...not there, so put in a placeholder!
			} else if (Fs.exists( readName )) // successfully removed entity but prev version still exists...
				rc = Fs.create( dname );
		}
		return rc;
	}
	public static boolean ignore( String name ) {
		boolean status = false;
		String    actual = Overlay.fname( name, Overlay.MODE_READ );
		String potential = Overlay.fname( name, Overlay.MODE_WRITE );
		String   ignored = Overlay.deleteName( potential );
		if (Fs.exists( actual )) {
			if (Fs.exists( potential )) 
				status = Fs.rename( potential, ignored );
			else
				status = Fs.create( ignored );
		}
		return status;
	}
	
	public static boolean restore( String entity ) {
		boolean status = false;
		String restored = Overlay.fname( entity, Overlay.MODE_WRITE );
		String  ignored = Overlay.deleteName( restored );
		if (!exists( entity ))
			status = Fs.rename( ignored, restored );
		return status;
	}
	
	public static Strings perform( Strings argv ) {
		// N.B. argv[ 0 ]="create", argv[ 1 ]="martin wheatman"
		Strings rc = Response.dnu( argv.toString() );
		if (argv.size() > 1) {
			String cmd = argv.remove( 0 );
			String ent = argv.remove( 0 );
			if ( (cmd.equals(    "create" ) &&  create( ent )) ||
				 (cmd.equals( "component" ) && createComponent( argv )) ||
				 (cmd.equals(    "delete" ) &&  delete( ent )) ||
				 (cmd.equals(    "exists" ) &&  exists( ent )) ||
				 (cmd.equals(    "ignore" ) &&  ignore( ent )) ||
				 (cmd.equals(   "restore" ) && restore( ent ))    )
			{
				rc = Response.okay();
				
			} else {
				rc = Response.notOkay();
				Audit.log(
					"Usage: entity [create|exists|ignore|delete] <entityName>\n"+
					"Given: entity "+ argv.toString( Strings.SPACED )
				);
		}	}
		return rc;
	}
	
	public static void main (String[] args) {
		Overlay.attach( "Entity" );
		new EntityShell().run();
}	}
