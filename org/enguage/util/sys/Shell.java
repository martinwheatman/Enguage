package org.enguage.util.sys;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import org.enguage.Enguage;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

public class Shell {

	static  Audit audit = new Audit( "Shell" );
	
	private String  prompt;
	public  String  prompt() { return prompt; }
	public  Shell   prompt( String p ) { prompt = p; return this; }
	
	private boolean aloud = true;
	public  boolean isAloud() { return aloud; }
	public  Shell   aloudIs( boolean is ) { aloud = is; return this; }
	
	private String prog;
	public  String name() { return prog; }
	public  Shell  name( String nm ) { prog = nm; return this; }
	
	private String who;
	private String dates;
	private String copyright;
	public  String copyright() { return prog +" (c) "+ (copyright!=null? copyright : who +", "+ dates); }
	public  Shell  copyright( String wh, String dts ) { who = wh; dates = dts; return this; }
	public  Shell  copyright( String statement ) { copyright = statement; return this; }

	private static long then = new GregorianCalendar().getTimeInMillis();
	public  static  long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	public Shell( String name, String copyright ) {
		name( name ).prompt( "> " ).copyright( copyright );
	}
	public Shell( String name ) {
		name( name ).prompt( "> " ).copyright( "Martin Wheatman", "2001-4, 2011-20" );
	}
	public Shell( String name, Strings args ) {this( name );}
	
	private void doLine( String line, String from, String to ) {
		//remove Byte order mark...
		if (line.startsWith("\uFEFF")) { line = line.substring(1); }

		if (!line.equals("\n")) {
			Strings stream = new Strings();
			// truncate comment -- only in real files
			int i = line.indexOf( '#' );
			if (-1 != i) line = line.substring( 0, i );
			
			// if we're converting on the fly, e.g. want -> need
			if (from != null) line = line.replace( from, to );
			
			// will return "cd .." as ["cd", ".", "."], not ["cd" ".."] -- "cd.." is meaningless!
			// need new stage of non-sentence sign processing
			stream.addAll( new Strings( line ));
			ArrayList<Strings> sentences = stream.divide( Terminator.terminators() );
			if ( sentences.size() > 1 ) {
				Strings sentence = sentences.remove( 0 );
				stream = Strings.combine( sentences );
				if (!sentence.isEmpty()) {
					Audit.log(
						Enguage.get().mediateDefaultDebug( ""+sentence )
					);
		}	}	}
	}
	public void interpret( InputStream fp, String from, String to ) { // reads file stream and "interpret()"s it
		if (fp==System.in) System.err.print( prompt() );
		
		try (BufferedReader br = new BufferedReader( new InputStreamReader( fp ))) {
			String line;
			while ((line = br.readLine()) != null) {
				doLine( line, from, to );
				if (fp==System.in) System.err.print( prompt() );
			}
		} catch (java.io.IOException e ) {
			audit.error( "IO error in Shell::interpret(stdin);" );
	}	}
	public void run() { interpret( System.in, null, null ); } // we're not converting on-the-fly!
}
