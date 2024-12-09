package org.enguage.sign.object.sofa;

import java.io.File;
import java.util.Iterator;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.fs.Path;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Overlay {
	
	public  static final String NAME = "overlay";
	private static final Audit audit = new Audit( NAME ); //.tracing( true ).logging( true )
	public  static final int      ID = 188374473; //Strings.hash( "overlay" )
	
	/* root/uid/   (uid="series")
	 *  +----------+ - 0/   <--+read here
	 *  |+----------+ - 1/  <--+
	 *  +|+----------+ - 2/ <--+ 
	 *   +| entity   |      <---write here
	 *   +| !deleted |
	 *    | ^from^to | #rename (not implemented!)
	 *    +----------+
	 */
	public static  final String DEFAULT_USERID = "uid";
	public static  final String        DEFAULT = "Enguage";
	public static  final int       MODE_READ   = 0; // "r"
	public static  final int       MODE_WRITE  = 1; // "w"
	public static  final int       MODE_APPEND = 2; // "a"
	public static  final int       MODE_DELETE = 3; // "d"
	//public static final int      MODE_RENAME = 4; // "m" 

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- overlay singleton
	// ---
	
	private Overlay() {p = new Path( System.getProperty( "user.dir" ));}
	
	private static Overlay overlay = new Overlay();
	public  static Overlay get() {return overlay;}
	public  static void    set( Overlay o ) {overlay = o;}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- naming conventions
	// ---
	private static final char   DELETE_CH  = '!';  // RENAME_CH = "^"
	private static final String DELETE_STR = ""+DELETE_CH;
	public  static boolean isDeleteName( String name ) {
		return new File( name ).getName().charAt( 0 ) == DELETE_CH;
	}
	public static String nonDeleteName( String name ) {
		if (isDeleteName( name )) {
			File f = new File( name );
			name = f.getParent() +"/"+ f.getName().substring( 1 );
		}
		return name;
	}
	public static String deleteName( String name ) {
		if (!isDeleteName( name )) {
			File f = new File( name );
			return f.getParent() +"/"+ DELETE_STR + f.getName();
		}
		return name;
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- object space - just write directly into Fs.root()
	// ---
	private static String root = "";
	private static void   root( String uid ) {
		root = Fs.root()+ uid +File.separator;
		new File( root ).mkdirs();
	}
	
	
	private Path p;
	private String getPath( String dname ) {
		String absName = ".";
		try {
			if (null == dname || dname.isEmpty())
				absName =  ".";
			else if (dname.charAt( 0 ) == '/')
				absName = new File( dname ).getCanonicalPath();
			else
				absName = new File( p.pwd() + File.separator + dname ).getCanonicalPath();
		} catch( Exception e ) {
			audit.error( "cannonical file error: "+ e.toString() );
		}
		return absName;
	}
	public Strings list( String dname ) {
		Strings rc = new Strings();

		p.pathListDelete();
		
		String absName = getPath( dname );
		
		// add underlaid-most files
		p.insertDir( absName, "" );
		
		if (absName != null && p.pwd().length() <= absName.length() && isOverlaid( p.pwd() )) {
			int     n = -1;
			int count = highest+1;
			String suffix = absName.substring( p.pwd().length());
			while (++n < count)
				p.insertDir( root + n + suffix, "" );
		}
		
		Iterator<Path.Pent> pi = p.iterator(); 
		while ( pi.hasNext() )
			rc.add( pi.next().name() );

		return rc;
	}
 	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- Version management
	// ---
	private static final String DETACHED = null;
	private static       String   series = DETACHED;
	
	private static void  series( String nm ) {if (nm != null) series = nm; }
	
	// version numbers...
	// private static final int lowest() {return 1
	private static final int  LOWEST = 1;
	
	private static final int  DEFAULT_HIGHEST = -1; // 0, 1, ..., n => 1+n; -1 == detached
	private static       int  highest = DEFAULT_HIGHEST; // 0, 1, ..., n => 1+n; -1 == detached
	private static final int  highest() {return highest;}
	private static final void highest( int n ) {highest = n;}
	
	private static final void countOverlays() {
		highest( DEFAULT_HIGHEST );
		String[] files = new File( root ).list();
		if (files != null)
			for (String file : files)
				try {	int tmp = Integer.parseInt( file );
						if (tmp > highest) highest( tmp );
				} catch (Exception ex) {/*ignore*/}
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- create/delete versions
	// ---
	public  static boolean exists() {
		return Fs.exists( root + series + Link.EXT );
	}
	public  static void    append() {
		new File( nth( ++highest )).mkdirs();
	}
	public  static void    remove() {
		Fs.destroy( nth( highest-- ));
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// -- - attach / detach
	// ---
	private static boolean attached = false;
	public  static boolean attached() {return attached;}
	private static void    attached(boolean b) {attached = b;}
	
	private static String  nth( int vn ) {return root + vn;}
	
	public  static Strings attach( String userId ) {
		Strings rc = Response.okay();
		if (!attached()) {
			attached( true );
			//
			root( userId );
			set( get()); // set singleton
			String cwd = System.getProperty( "user.dir" );
			series( new File( cwd ).getName() );
			
			//audit.debug( "attaching to "+ new File( cwd ).getName() )
			Link.fromString( root + series, cwd );
			//audit.debug( "Linking: "+ root +"=+="+ series +" to"+ cwd )
			//
			countOverlays();
			while (highest() < LOWEST)
				append(); // side effect - increments highest
		}
		if (highest < 0) {
			audit.debug( "No such series: "+ userId );
			rc = Response.notOkay();
		}
		return rc;
	}
	public static  void    detach() {
		if (attached()) {
			series( DETACHED );
			attached( false );
	}	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- combining versions
	// ---
	private static void combineWithLower( int overlay ) {
		if (overlay > 0) {
			moveFiles(
				new File( nth( overlay   )),
				new File( nth( overlay-1 ))
			);
	}	}
	private static void commit() {
		// We always want a top overly we can remove next time
		// so consolidate the lower overlays to one and rename the 
		// top one to "1"
		while (highest > 0) {
			// combine two highest overlays
			//audit.debug( "commit(): "+ highest +" => "+ (highest-1))
			combineWithLower( highest );
			//showVersions()
			highest--;
		}
		append();
	}
	private static void consolidate() {
		if (attached()) {
			int penultimate = highest-1;
			while (penultimate > LOWEST)
				combineWithLower( penultimate-- );
			
			if (highest > LOWEST+1) {
				// then _rename_ top overlay - down to "1"
				File src = new File( nth( highest ));
				File dst = new File( nth( LOWEST +1 ));
				src.renameTo( dst );
			}
			
			// reset overlay count
			countOverlays();
		}
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- Legacy methods
	// ---
	private String nthCandidate( String nm, int vn ) {
		if (vn < 0)
			vn = 0;
		else if (vn > highest)
			vn = highest;
		return nth( vn ) + File.separator + nm;
	}
	private String topCandidate( String name ) {
		return nthCandidate( name, highest );
	}
	private String delCandidate( String name, int n ) {
		File f = new File( nthCandidate( name, n ));
		return f.getParent() +"/"+ DELETE_STR + f.getName(); 
	}
	private String find( String vfname ) {
		int vn = highest+1; // number=3 ==> 0,1,2, so...
		while (--vn >= 0) { // ...initially decr vn
			String fsname = nthCandidate( vfname, vn );
			if (Fs.exists( fsname ))
				return fsname;
			else if (Fs.exists( delCandidate( vfname, vn ) )) 
				return topCandidate( vfname ); // look no further - return top (non-existing) file
		}
		return vfname;
	}
	private boolean isOverlaid( String vfname ) {
		if (vfname == null) return false;
		if (vfname.startsWith( root )) return false;
		if (!vfname.startsWith( "/" )) return true; // assumes were at head of overlaid fs!
		return vfname.startsWith( Link.content( root + series )); // NAME="xyz/abc" base="xyz" => true
	}
	
	//  virtual to "real" filename mapping
	// /home/martin/person/martin/name.txt -> /home/martin/sofa/sofa.0/martin/name.txt
	// if write - top overlay NAME is returned, simple.
	// if read  - overlay space is searched for an existing file.
	//          - if not found, or if the file has been deleted, the (non-existing) write filename is returned
	//          - if rename found (e.g. old^new), change return the old NAME.
	public static String fname( String vfname, int modeChs ) {
		//
		// one-off hack! To help remove the _user and _host 3rd-Party virt. users.
		if (vfname.equals ( "me" ))
			vfname = "i";
		else if (vfname.startsWith( "me/" )) {
			vfname = "i/" + vfname.substring( 3 );
		} else if (vfname.equals ( "my" )) 
			vfname = "i";
		else if (vfname.startsWith( "my/" )) {
			vfname = "i/" + vfname.substring( 3 );
		}
		//
		//
		if (attached() && overlay.isOverlaid( vfname ))
			switch (modeChs) {
				case MODE_READ   : return overlay.find( vfname ); 
				case MODE_DELETE : return overlay.delCandidate( vfname, highest );
				default          : return overlay.topCandidate( vfname ); // MODE_WRITE | _APPEND
			}
		else
			return vfname;
	}
	
	// ===
	
	/*
	 * implements transactions -- but isn't ACID :(
	 */
	// --- Compact overlays
	private static void moveFiles( File src, File dest ) {
		if (src.isDirectory()) {
			if (!dest.exists()) dest.mkdir();
			String[] files = src.list();
			if (files != null)
				for (String file : files)
					moveFiles( new File( src, file ), new File( dest, file ));
			src.delete();
			
		} else {  // move file across
			if (isDeleteName( src.toString())) // We have !filename...
				// remove filename
				new File( nonDeleteName( dest.toString() )).delete();
			src.renameTo( dest );
	}	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- Transactions
	// ---
	public  static void startTxn() { append();}
	public  static void undoTxn()  {
		if (highest > LOWEST) {
			remove(); // this (empty!)
			remove(); // last/aborted
			append(); // replace current
	}	}
	public  static void commitTxn() { consolidate();}
	public  static void destroyTxns() {
		while (highest>=LOWEST) remove(); // this adjusts 'highest'
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- perform "overlay [attach|detach|...] <args>"
	// ---
	private static Strings doCommitHelper( String value ) {
		Strings rc = Response.okay();
		if (value.equals( "" ))
			commitTxn();
		else if (value.equals( "full" ))
			commit();
		else
			rc = Response.notOkay();
		return rc;
	}
	public static Strings perform( Strings argv ) {
		
		Strings rc = Response.okay();
		int argc = argv.size();
		
		String  cmd    = argv.remove( 0 );
		
		if (cmd.equals( "attach" )) {
			rc = attach( argv.toString() );
				
		} else if (cmd.equals( "detach" ) && (1 == argc)) {
			detach();
			
		} else if (cmd.equals( "count" ) && (1 == argc)) {
			// Remember, 0 => 1, 0,1 => 2: should be consecutive!
			rc = Response.okay(""+(1 + highest));
			
		} else if (cmd.equals( "start"  ) && (1 == argc) ) {
			startTxn();
			
		} else if (cmd.equals( "undo"  ) && (1 == argc) ) {
			undoTxn();
			
		} else if (cmd.equals( "destroy"  ) && (1 == argc) ) {
			destroyTxns();
			
		} else if (cmd.equals( "commit" )) {
			rc = doCommitHelper( argv.toString() );
			
		} else {
			rc = Response.notOkay();
			audit.debug( "Usage: attach <series>\n"
			                 +"     : detach\n"
			                 +"     : save\n"
			                 +"     : count\n"
			                 +"given: "+ argv.toString( Strings.CSV ));
		}
		return rc;
}	}
