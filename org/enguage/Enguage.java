package org.enguage;

import java.io.File;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Config;
import org.enguage.sign.factory.Spoken;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.interpretant.intentions.Reply;
import org.enguage.sign.object.list.Item;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.audit.Audit;
import org.enguage.util.http.InfoBox;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;
import org.enguage.util.sys.Fs;
import org.enguage.util.sys.Shell;

public class Enguage {
	
	private static final String COPYRIGHT = "Martin Wheatman, 2001-4, 2011-24";
	public  static final String RO_SPACE  = "etc"+ File.separator;
	public  static final String RW_SPACE  = "var"+ File.separator;

	private static Enguage enguage;
	public  static Enguage get() {return enguage;}
	public  static void    set( Enguage e ) {enguage = e;}
	
	private static Audit   audit   = new Audit( "Enguage" );

	private static Shell   shell   = new Shell( "Enguage", COPYRIGHT );
	public  static Shell   shell() {return shell;}
	
	private boolean imagined = false;
	public  boolean imagined() {return imagined;}
	public  void    imagined( boolean img ) {imagined = img;}
		
	public  Enguage() {this( RW_SPACE );}
	public  Enguage( String root ) {
		Audit.suspend();
		Fs.root( root );
		Concept.addNames( Concept.list());
		Config.load( "config.xml" );
		Audit.resume();
		// we should be attached here =b
		Spoken.loadAll();
	}
	
	private Strings mediateSingle( String uid, Strings utterance ) {
		audit.in("mediateSingle", "uid="+ uid +", utt="+ utterance );
		Strings reply;
	
		imagined( false );
		Overlay.attach( uid );
		Where.clearLocation();
		Item.resetFormat();
		Repertoires.signs().firstMatch( true );
		
		if (Utterance.isUnderstood()) // from previous interpretation!
			Overlay.startTxn(); // all work in this new overlay
		
		Reply r = Repertoires.mediate( new Utterance( utterance ));

		// once processed, keep a copy
		Utterance.previous( utterance );

		completeTransaction( r.sayThis() );
		
		// auto-unload here - autoloading() in Repertoire.interpret() 
		// asymmetry: load as we go; tidy-up once finished
		Autoload.unloadAged();

		reply = Reply.say().appendAll( r.sayThis() );
		Reply.say( null );
		Overlay.detach();
			
		// According to Wikipedia, ...  (set in config.xml)
		// If we've picked up a source, attribute it!
		reply = InfoBox.attributeSource( reply );

		audit.out();
		return reply;
	}
	public String mediate( String said ) {return mediate( Overlay.DEFAULT_USERID, said );}
	public String mediate( String uid, String said ) {
		audit.in( "mediate", "uid="+uid+", said="+said );
		Strings rc = new Strings();
		for (Strings single :
				Utterance.conjuntionAlley(
						new Strings( said ), Config.andConjunction()
			)	)
		{
			if (!rc.isEmpty()) rc.add( "and" );
			rc.addAll( mediateSingle( uid, single ));
		}
		audit.out( rc );
		return rc.toString();
	}
	
	// helper methods...
	private void completeTransaction( Strings sayThis ) { // 
		if (imagined()) {
			Overlay.undoTxn();
			if (sayThis.begins( Response.dnu() ))
				Repertoires.signs().ignoreNone();
			
		} else if (Utterance.isUnderstood()) {
			Overlay.commitTxn();
			if (sayThis.begins( Response.dnu() ))
				Repertoires.signs().ignoreNone();
			
		} else {
			// really lost track?
			audit.debug( "utterance is not understood, forgetting to ignore: "
			             +Repertoires.signs().ignore().toString() );
			Repertoires.signs().ignoreNone();
			shell.aloudIs( true ); // sets aloud for whole session if reading from fp
	}	}
	
	/*
	 *  test code....
	 */
	public static void usage() {
		Audit.log( "Usage: java [-jar enguage.jar|org.enguage.Enguage]" );
		Audit.log( "            --help |" );
		Audit.log( "            [-d|--data <path>]" ); 
		Audit.log( "            [-v|--verbose]" ); 
		Audit.log( "            [<utterance>]" );
		Audit.log( "Options are:" );
		Audit.log( "       -h, --help" );
		Audit.log( "          displays this message\n" );
		Audit.log( "       -v, --verbose\n" );
		Audit.log( "       -d, --data <path> specifies the data volume to use\n" );
		Audit.log( "       [<utterance>]" );
		Audit.log( "          with an utterance it runs one-shot;" );
		Audit.log( "          with no utterance it runs as a shell," );
		Audit.log( "             requiring full stops (periods) to" );
		Audit.log( "             terminate utterances." );
	}
	
	public static String doArgs( Strings cmds, String location ) {
		// traverse args and strip switches: -v -d -H -p -s
		String fsys = location;
		String cmd;
		int i = 0;
		while (i < cmds.size()) {
			
			cmd = cmds.get( i );
			
			if (       cmd.equals( "-h" ) || cmd.equals( "--help" )) {
				Enguage.usage();
				System.exit( 0 );
			
			} else if (cmd.equals( "-v" ) || cmd.equals( "--verbose" )) {
				cmds.remove( i );
				audit.debugging( true );
							
			} else if (cmd.equals( "-d" ) || cmd.equals( "--data" )) {
				cmds.remove( i );
				fsys = cmds.isEmpty() ? fsys : cmds.remove( i );
				
			} else
				i++;
		}
		return fsys;
	}
	
	public static void main( String[] args ) {
		
		Strings    cmds = new Strings( args );
		String     fsys = Enguage.doArgs( cmds, RW_SPACE );

		enguage = new Enguage( fsys );
				
		String cmd = cmds.isEmpty() ? "" : cmds.remove( 0 );
		if (cmd.equals( "" )) {
			Overlay.attach( "uid" );
			shell.aloudIs( true ).run();
		
		} else {
			// Command line parameters exists, so...
			
			// reconstruct original commands
			cmds.prepend( cmd );
			cmds = Terminator.stripTerminator( cmds );
			cmds = cmds.toLowerCase();
			
			// ...and interpret
			Audit.log( enguage.mediate( cmds.toString() ));
}	}	}
