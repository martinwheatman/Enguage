package org.enguage.sign.factory;

import java.util.ListIterator;
import java.util.Locale;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Value;
import org.enguage.sign.pattern.Pattern;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Spoken {
	
	private Spoken() {}
	
	public  static final String   NAME = "spoken";
	private static final Audit   audit = new Audit( NAME );
	
	private static final String UNDER_CONSTR = "TBD"; // concept name for signs under construction
	private static final String         THAT = "that"; // always(?) refers to a variable
	public  static final String     CONCEPTS = "spoken";

	private static Sign voiced = null;
	public  static void delete() {voiced = null;}

	private static void removeVoicedValue() {
		// should be removed from a new STATIC_REPERTOIRE
		new Value( CONCEPTS, voiced.pattern().toFilename() ).ignore();
	}
	private static void saveVoicedAsValue() {
		// should be saved to a new STATIC_REPERTOIRE
		new Value( CONCEPTS, voiced.pattern().toFilename()  ).set( voiced.toString() );
	}
	private static Sign rereadVoiceFromValue() {
		// should be reread from a new STATIC_REPERTOIRE
		Repertoires.signs().remove( UNDER_CONSTR );

		// Written uses current concept value...
		String savedConcept = Concept.current();
		Concept.current( UNDER_CONSTR );
		voiced = new Written(
				new Value( CONCEPTS, voiced.pattern().toFilename() ).get()
		).toSign();
		Concept.current( savedConcept );
		
		saveVoicedAsValue();
		
		Repertoires.signs().insert( voiced );
		return voiced;
	}
	
	public static Strings list() {
		audit.in( "list", "");
		Strings files = Overlay.get().list( CONCEPTS );
		audit.out( files );
		return files;
	}
	
	public static void loadAll() {
		Strings files = Overlay.get().list( CONCEPTS );
		for (String file : files)
			Repertoires.signs().insert(
					new Written(
							new Value( CONCEPTS, file ).get()
					).toSign()
			);
	}
	
	/*
	 * Pattern creation routines
	 */
	private static void doCreate( Strings args ) {
		// rename previously voiced sign
		Repertoires.signs().rename( UNDER_CONSTR, Sign.USER_DEFINED );
		if (voiced != null)
			removeVoicedValue();
		
		// create new WIP sign
		voiced = new Sign()
				.pattern( new Pattern( args.toString() ))
				.concept( UNDER_CONSTR );
		Repertoires.signs().insert( voiced );
		
		saveVoicedAsValue();
	}
	private static void doDestroy() {
		// remove last created sign
		Repertoires.signs().remove( UNDER_CONSTR );
		voiced = null;
	}
	private static void doSplit( Strings args ) {
		if (voiced != null) {
		
			voiced = rereadVoiceFromValue();
			removeVoicedValue();
			
			if (args.size() == 1)
				voiced.pattern(
						voiced.pattern().split(args.get( 0 ))
				);
				
			else if (args.size() > 1)
				voiced.pattern(
						voiced.pattern().split(args.get( 0 ), args.get( 1 ))
				);
				
			else
				audit.error( "split: missing parameter(s)" );
			
			// convert existing intentions: "to the phrase ..." already has a reply! 
			if (!voiced.intentions().isEmpty()) {
				for (Intention i : voiced.intentions()) {
					Strings intentions = Strings.markedUppercaser(
							THAT,
							voiced.pattern().names(), 
							i.values()
						);
					i.values( intentions );
			}	}
			
			// splitting will change the pattern complexity
			// therefore it needs to be reinserted...
			Repertoires.signs().remove( UNDER_CONSTR );
			Repertoires.signs().insert( voiced );
			saveVoicedAsValue();
	}	}
	/*
	 * Voiced management routines
	 */
	private static void insertACommaOrPauseAfterFelicityEncoding( Strings reply ) {
		// insert a comma/pause into reply
		if      (reply.size() > Response.okay().size()
				&& reply.begins( Response.okay())
				&& !reply.get( Response.okay().size()).equals(","))
			reply.add( Response.okay().size(), "," );
		
		else if (reply.size() > Response.notOkay().size()
				&& reply.begins( Response.notOkay())
				&& !reply.get( Response.notOkay().size()).equals(","))
			reply.add( Response.notOkay().size(), "," );
		
		else if (reply.size() > Response.yes().size()
				&& reply.begins( Response.yes())
				&& !reply.get( Response.yes().size()).equals(","))
			reply.add( Response.yes().size(), "," );
		
		else if (reply.size() > Response.no().size()
				&& reply.begins( Response.no())
				&& !reply.get( Response.no().size()).equals(","))
			reply.add( Response.no().size(), "," );
	}
	private static Strings derefEmbeddedVars(Strings strs, Strings vars) {
		audit.in( "derefEmbeddedVars", "str="+ strs +", vars=" + vars );
		// "i am phrase variable name" -> "i am NAME"
		// "that is variable" -> "that is variable"
		// "the variable nature of it" -> "the variable nature of it"  
		Strings output = new Strings();
		ListIterator<String> si = strs.listIterator();
		while (si.hasNext()) {
			String tmp = si.next();
			if (vars.contains( tmp ))
				output.add( tmp.toUpperCase( Locale.getDefault() ));
			else
				output.add( tmp );
		}
		audit.out( output );
		return output;
	}
	private static void doIntention( int intent, Strings intents, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromValue();
			
			// takes a marker and replaces that following with upper case...
			// takes a marker and replaces   FOLLOWING    with upper case...
			Strings intentions = Strings.markedUppercaser(
									THAT,
									voiced.pattern().names(), 
									intents
								);
			intentions = derefEmbeddedVars( intentions, voiced.pattern().names());
			
			if (intent == Intention.N_REPLY)
				insertACommaOrPauseAfterFelicityEncoding( intentions );
				
			voiced.insert(
					new Intention(
							Intention.condType( intent, isThen, isElse ),
							intentions
			)		);
			saveVoicedAsValue();
	}	}
	private static void doFinally( Strings args, boolean isThen, boolean isElse ) {
		if (voiced != null) {
			voiced = rereadVoiceFromValue();
			
			// N.B. this method is not currently exercised!!!
			String cmd = args.get( 0 ); // don't remove it
			int base = Intention.N_THINK;
			if (cmd.equals( Intention.DO_HOOK ))
				base = Intention.N_DO;
			else if (cmd.equals( Intention.REPLY_HOOK ))
				base = Intention.N_REPLY;
			
			voiced.append( // all finals at the end :)
					new Intention(
							Intention.condType( base, isThen, isElse ),
							Pattern.toPattern( new Strings( args.toString() ))
					)
			);
			// write out here
			saveVoicedAsValue();
	}	}
	private static Strings doShow() {
		if (voiced != null) {
			voiced = rereadVoiceFromValue();
			// w/o Audit indents!
			Audit.log( voiced.toString( false ));
		} else
			return Response.notOkay( "nothing to see here" );
		return Response.okay();
	}
	private static Strings doList() {
		if (voiced != null) {
			voiced = rereadVoiceFromValue();
			return voiced.intentions().toSpokenList();
		} else
			return Response.notOkay( "nothing to see here" );
	}
	
	private static boolean isElse( String cmd ) {return cmd.equals( "else" );}
	private static boolean isThen( String cmd ) {return cmd.equals( "then" );}

	/*
	 * This will handle:
	 *  	"sign [else|then] create|imply|run|perform|finally <PHRASE-X>"
	 * as found in interpret.txt can_say.txt first+then.txt
	 */
	public static Strings perform( Strings args ) {
		// It simply takes commands to construct a voiced sign, e.g. create, append etc.
		// N.B. the 'payload' (e.g. the intention) is passed as a string.

		args = new Strings( args.toString() );
		
		audit.in( "perform", args.toString( Strings.DQCSV ));
		
		Strings     rc = Response.okay();			
		String     cmd = args.remove( 0 );
		boolean isElse = isElse( cmd );
		boolean isThen = isThen( cmd );
		
		if (isThen || isElse)
			cmd = args.remove( 0 );
			
		if (cmd.equals( "create" ))
			doCreate( args );
			
		else if (cmd.equals( "destroy" ))
			doDestroy();
		
		else if (cmd.equals( "split" ))
			doSplit( args );
			
		else if (cmd.equals( "show" ))
			rc = doShow();
		
		else if (cmd.equals( "list" ))
			rc = doList();
			
		else if (cmd.equals( "perform" ))
			doIntention(  Intention.N_DO,   args, isThen, isElse );
			
		else if (cmd.equals( "think" ))
			doIntention( Intention.N_THINK, args, isThen, isElse );
			
		else if (cmd.equals( "imply" ))
			doIntention( Intention.N_THINK, args, isThen, isElse );
			
		else if (cmd.equals( "run" ))
			doIntention( Intention.N_RUN,   args, isThen, isElse );
			
		else if (cmd.equals( "reply" ))
			doIntention( Intention.N_REPLY, args, isThen, isElse );
			
		else if (cmd.equals( "temporal"))
			voiced.temporalIs( true );
			
		else if (cmd.equals( "finally" ))
			doFinally( args, isThen, isElse );
			
		else {
			rc = Response.notOkay();
			audit.error( "Unknown Sign.interpret() command: "+ cmd );
		}
		audit.out( rc );
		return rc; 
}	}
