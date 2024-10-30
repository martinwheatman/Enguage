package org.enguage.repertoires.concepts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.sign.Assets;
import org.enguage.sign.Sign;
import org.enguage.sign.factory.Spoken;
import org.enguage.sign.factory.Written;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.Utterance;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;
import org.enguage.util.sys.Fs;

/** Concepts is: the list of concept names; and,
 *               a name-to-concept 'match' function.
 */
public class Concept {
	
	// this is a static class - a 'module'
	private Concept() {}
	
	public  static final String     NAME = "concept";
	public  static final int          ID = 42747006; // Strings.hash( "Concept" )
	private static final Audit     audit = new Audit( NAME ).tracing( true );
	
	public  static final String     RPTS = "rpts";
	public  static final String     DICT = "dict";
	
	private static final String      DOT = ".";
	private static final String      TXT = "txt";
	private static final String      RPT = "rpt";
	private static final String    ENTRY = "entry";
	private static final String      DEL = "del";
	
	public  static final String  TEXT_EXT = DOT + TXT;
	public  static final String  REPT_EXT = DOT + RPT;
	public  static final String ENTRY_EXT = DOT + ENTRY;
	public  static final String  DELT_EXT = DOT + DEL;
	
	private static String current = "";
	public  static void   current( String name ) {current = name;}
	public  static String current() {return current;}

	private static TreeSet<String> names = new TreeSet<>();
	public  static void  remove( String name ) {names.remove( name );}
	public  static void     add( String name ) {names.add(    name );}
	public  static void  addAll( Strings nms ) {names.addAll(  nms );}
	
	private static final String rwRpts() {
		return Fs.root() +RPTS+ File.separator;
	}
	private static final String ro( String loc ) {
		return Enguage.RO_SPACE+ loc +File.separator;
	}
	
	// Unit tests refer to 'concept', in the dictionary they are in 'c/concept'
	public  static String dictName( String cname ) {
		return cname.charAt( 0 ) +File.separator+ cname;
	}
	// concepts are read in a tree
	public  static String dictionary(     String name ) {return ro(DICT)+ name +ENTRY_EXT;}
	private static String writtenName(    String name ) {return ro(RPTS)+ name +TEXT_EXT;}
	private static String writtenRepName( String name ) {return ro(RPTS)+ name +REPT_EXT;}
	
	public  static String spokenName(    String s ) {return rwRpts()+ s +TEXT_EXT;}
	private static String spokenRepName( String s ) {return rwRpts()+ s +REPT_EXT;}
	
	private static String deleteName(    String s ) {return rwRpts()+ s +DELT_EXT;}
		
	private static Strings treeAdd( String base, String location ) {
		Strings  names = new Strings();
		String[] files = Assets.list( base + location );
		if (files != null)
			for (String file : files)
				if (file.endsWith( TEXT_EXT ) ||
				    file.endsWith( REPT_EXT ) ||
				    file.endsWith( ENTRY_EXT )   )
					names.add( (!location.equals(".") ? location+"/" : "")+ file );
				else if (!file.contains( "." ))
					names.addAll( treeAdd( base, file ));
		return names;
	}

	public static String[] list() {
		Strings names = new Strings();
		names.addAll( treeAdd( ro(RPTS), "." ));
		names.addAll( treeAdd( ro(DICT), "." ));

		String[] array = new String[ names.size() ];
		return names.toArray( array );
	}
	
	public static void addNames( String[] names ) {
		for (String name : names) { // e.g. name="rpt/hello.txt"
			// e.g.: w/we.entry, config/arithmetic.txt, object/set+get+unset.txt
			String[] components = name.split( "\\." );
			if (components.length==2 && !name.startsWith( "config/" ))
				add( components[ 0 ]);
	}	}
	public static void addNames( Strings names ) {
		for (String name : names) { // e.g. name="-is_the-.txt"
			audit.debug( "Adding name: "+ name );
			add( name );
	}	}
	
	private static boolean matchAnyBoilerplate( Strings utt, Strings bplt, char sep ) {
		Iterator<String> ui  = utt.iterator();
		Iterator<String> bi = bplt.iterator();
		boolean first = true;
		while (ui.hasNext() && bi.hasNext()) { // read through the utterance & pattern
			Iterator<String> pwi = new Strings( bi.next(), sep ).iterator();
			if (pwi.hasNext()) {
				
				String pw = pwi.next();
				String ut = ui.next();
				
				// find the first matching token
				if (!first)
					while (!ut.equals(pw) && ui.hasNext())
						ut = ui.next();
				
				//now go thru' matching tokens
				while (ut.equals(pw) && ui.hasNext() && pwi.hasNext()) {
					ut = ui.next();
					pw = pwi.next();
				}
				
				// return if we have a mismatch
				if (!pw.equals(ut)) return false;
				
				// hyphen represents at least 1 utterance string, just read over it
				if (bi.hasNext()) {
					if (ui.hasNext())
						ui.next();
					else
						return false; // hyphen but no text...
				} else
					return !ui.hasNext();
				first = false;
		}	}
		return true;
	}
	private static boolean matchSign( Strings utt, String sign ) {
		Strings allBplate = new Strings( sign, '-' ); // ["to_the_phrase", "reply", ""]
		switch (allBplate.size()) {
		case 0: return true;
		case 1: return utt.contains( new Strings( allBplate.iterator().next(), '_' ));
		default:return matchAnyBoilerplate( utt, allBplate, '_' );
	}	}
	private static boolean matchConceptNames( Strings utterance, String cfname ) {
		// matching: "to my name is martin reply hello martin" with "to-reply-"
		for (String sign : new Strings( cfname, '+' )) // e.g. c="to_the_phrase-reply-"
			if (matchSign(utterance, sign))
				return true;
		return false;
	}
	private static boolean matchConcepts( Strings utterance, String concept ) {
		// Strip dir name from concept filename
		String[] tmp = concept.split( "/" ); // OR closeAirSupport/egress
		return matchConceptNames( utterance, tmp[ tmp.length-1 ]);
	}
	public static Strings match( Strings utterance ) {
		/* match(["martin", "is", "a", "hero"]):
		 *     names=[ "is_a+has_a" ] 
		 * } => ( is_a+has_a )
		 */
		Strings matches = new Strings();
		for (String concept : names ) // e.g."is_a+has_a" OR "to_the_phrase-reply_with"
			if (matchConcepts( utterance, concept ))
				matches.add( concept );
		return matches;
	}
	
	// -- Load begin
	// --
	/* This is the STATIC loading of concepts at startup -- read
	 * from the config.xml file.
	 */
	private static TreeSet<String>   loaded = new TreeSet<>();
	public  static SortedSet<String> loaded() {return loaded;}
	public  static void              loaded(String name) {loaded.add(name);}

	private static FileInputStream getFile( String name ) {
		FileInputStream is = null;
		try {is = new FileInputStream( name );
		} catch (IOException ignore) {/* returning null is okay */}
		return is;
	}

	public  static void delete( String cname ) {
		if (cname != null) {
			File oldFile = new File( spokenName( cname ));
			File newFile = new File( deleteName( cname ));
			if (oldFile.exists() && !oldFile.renameTo( newFile ))
				audit.error( "renaming "+ oldFile +" to "+ newFile );
	}	}

	private static String preprocessLine( String line, String from, String to ) {
		//remove Byte order mark...
		if (line.startsWith("\uFEFF"))
			line = line.substring(1);

		// truncate comment -- only in real files
		int i = line.indexOf( '#' );
		if (-1 != i)
			line = line.substring( 0, i );

		// if we're converting on the fly, e.g. want -> need
		if (from != null)
			line = line.replace( from, to );
		
		return line;
	}
	private static Strings preprocessFile( InputStream fp, String from, String to ) {
		Strings content = new Strings();
		Scanner br = new Scanner( new InputStreamReader( fp ));
			while (br.hasNextLine()) 
				content.addAll( new Strings( preprocessLine( br.nextLine(), from, to )));
		br.close();
		return content;
	}
	private static void loadFileContent( InputStream fp, String from, String to ) {
		// adds signs and interprets utterances
		Strings content = preprocessFile( fp, from, to );
		ArrayList<Strings> utterances = content.divide( Terminator.terminators(), false );
		for (Strings utterance : utterances) {
			Sign sign = new Written( utterance ).toSign();
			if (sign != null)
				Repertoires.signs().insert( sign );
			else // if we find, e.g. "this concept is spatial".
				Repertoires.mediate( new Utterance( utterance ));
		}
	}
	public static String loadConceptFile( String name ) {
		return loadConceptFile( name, null, null );
	}
	public static String loadConceptFile( String name, String from, String to ) {
		boolean wasLoaded   = true;
		String  loadedName = to==null ? name : name.replace( from, to );
		
		Variable.set( Assets.NAME, name );
		Concept.current( loadedName );
		Audit.suspend();
		
		InputStream  is = null;
		// should be found on one of these places... in this order(!)
		if ((null != (is = getFile( spokenName( name )             ))) ||
		    (null != (is = getFile( spokenRepName( name )          ))) ||
		    (null != (is = Assets.getStream( dictionary( name )    ))) ||
		    (null != (is = Assets.getStream( writtenName( name )   ))) ||
		    (null != (is = Assets.getStream( writtenRepName( name ))))   )
		{	
			loadFileContent( is, from, to );
			try{is.close();} catch(IOException ignore) {/*ignore me!*/}
			
		} else
			wasLoaded = false;
		
		Audit.resume();
		Variable.unset( Assets.NAME );
		
		return wasLoaded ? loadedName : "";
	}
	public static boolean load( String name ) {
		boolean rc = true;
		if (!loaded().contains( name )) {
			String conceptName = loadConceptFile( name );
			if (!conceptName.equals( "" ))
				loaded( conceptName );
			else {
				rc = false;
				Audit.log( "error loading "+ name );
		}	}
		return rc;
	}
	// --
	// -- Load end

	public static Strings perform( Strings cmds ) {
		
		Strings rc = Response.okay();
		String cmd = cmds.remove( 0 );
		
		if (cmd.equals( "saveAs" )) {
			String name = cmds.toString( Strings.UNDERSC );
			audit.debug( "Saving concepts as "+ name );
			add( name );
			rc = Repertoires.signs().saveAs(
								Sign.USER_DEFINED,
								name
				 ) ? Response.okay() : Response.notOkay();

			
		} else if (cmd.equals( "delete" )) {
			String concept = cmds.toString( Strings.UNDERSC );
			audit.debug( "Deleting "+ concept +" concept");
			remove( concept ); // remove the concept name
			delete( concept ); // delete the concept file
			Repertoires.signs().remove( concept ); // remove the loaded concept
			Spoken.delete(); // remove sign under construction
			
			
		} else if (cmd.equals( "load" ))
			/* load is used by create, delete, ignore and restore to
			 * support their interpretation
			 */
			for (String file : cmds)
				load( file );
			 
		else if (cmd.equals( "unload" ))
			for (String file : cmds)
				Autoload.unloadNamed( file );
			
		else if (cmd.equals( "exists" ))
			// concept count "i can say"
			rc = cmds.isEmpty() || match( new Strings( cmds.remove( 0 )) ).isEmpty()
					? Response.no() : Response.yes();
			
		else
			rc = Response.notOkay();
		
		return rc;
}	}
