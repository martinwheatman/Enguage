package org.enguage.sign.interpretant.intentions;

import org.enguage.Enguage;
import org.enguage.repertoires.Repertoires;
import org.enguage.sign.Config;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.config.Englishisms;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public final class Engine {
	
	private Engine() {}
	
	public  static final String NAME = "engine";
	private static final Audit audit = new Audit( NAME );

	public  static final String IGNORE_STR = "ignore";
	public  static final String UNDO_STR   = "undo";

	public  static final Sign[] commands() {return commands;}
	private static final Sign[] commands = {
			/* These could be accompanied in a repertoire, but they have special 
			 * interpretations and so are built here alongside those interpretations.
			 */
			new Sign()
					.pattern( "this is all imagined" )
					.append( Intention.N_ALLOP, "imagined" )
					.concept( NAME ),
			new Sign()
					.pattern( "help", "" )
					.append( Intention.N_ALLOP, "help" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "say PHRASE-SAID" )
					.append( Intention.N_ALLOP, "say SAID")
			  		.concept( NAME ),
			new Sign()
					.pattern( "say again" )
					.append( Intention.N_ALLOP, "repeat"       )
			  		.concept( NAME ),
			new Sign()
					.pattern( "spell X" )
					.append( Intention.N_ALLOP, "spell X"      )
			  		.concept( NAME ),
			new Sign()
					.pattern( "this is false" )
					.append( Intention.N_ALLOP, "undo" )
			  		.concept( NAME ),
			new Sign()
					.pattern( "this sentence is false" )
					.append( Intention.N_ALLOP, "undo" )
			  		.concept( NAME ),
			/* 
			 * it is possible to arrive at the following construct:   think="reply 'I know'"
			 * e.g. "if X, Y", if the instance is "if already exists, reply 'I know'"
			 * here reply is thought. Should be rewritten:
			 * representamen: "if X, reply Y", then Y is just the quoted string.
			 * However, the following should deal with this situation.
			 */
			new Sign()
					.pattern( "reply PHRASE-X" )
					.append( Intention.N_THEN_REPLY, "X" )
					.concept( NAME ),
					
			new Sign() // for vocal description of concepts... autopoiesis!
					.pattern( "perform PHRASE-ARGS" )
					.append( Intention.N_THEN_DO, "ARGS" )
					.concept( NAME ),
			/* 
			 * REDO: undo and do again, or disambiguate
			 */
			new Sign()
					.pattern( UNDO_STR )
					.append( Intention.N_ALLOP, UNDO_STR )
			  		.concept( NAME ),
			new Sign()
					.pattern( "No PHRASE-X" )
					// first remove/restart transaction...
					.append( Intention.N_ALLOP, UNDO_STR )
					.append( Intention.N_ELSE_REPLY, "undo is not available" )
					// then ignore last matched complexity
					.append( Intention.N_ALLOP, IGNORE_STR +" X" )
					// then re-think X
					.append( Intention.N_THEN_THINK, "X" )
					.concept( NAME )
		 };
	
	public static Reply interp( Intention in, Reply r ) {
		r.answer( Response.okay().toString() ); // bland default reply to stop debug output look worrying
		r.type(   Response.typeFromStrings( Response.okay() ));

		Strings cmds = Context.deref( new Strings( in.value() )).normalise();
		String  cmd  = cmds.remove( 0 );

		if (cmd.equals( UNDO_STR )) {
			// remove top overlay...
			Overlay.undoTxn();
			// ...so, reload variables
			Variable.encache();
				
		} else if (cmd.equals( IGNORE_STR )) {
			Repertoires.signs().ignore( cmds );
		
		} else if (cmd.equals( "imagined" )) {
			Enguage.get().imagined( true );
			r.format( new Strings( "ok, this is all imagined" ));
			
		} else if (cmd.equals( "spell" )) {
			r.format( new Strings( Englishisms.spell( cmds.get( 0 ), true )));
			
		} else if (cmd.equals( "iknow" )) {
			String tmp = Repertoires.mediate( new Utterance( cmds )).toString();
			if (tmp.charAt( tmp.length() - 1) == '.')
				tmp = tmp.substring( 0, tmp.length() - 1 );
			r.answer( tmp );
			r.type( Response.typeFromStrings( new Strings( tmp ) ));
			
		} else if ( in.value().equals( "repeat" )) {
			if (Reply.previous() == null) {
				audit.debug("Allop:repeating dnu");
				r.format( Response.dnu());
			} else {
				audit.debug("Allop:repeating: "+ Reply.previous());
				r.repeated( true );
				r.format( new Strings( Config.repeatFormat()));
				r.answer( Reply.previous().toString());
				r.type( Response.typeFromStrings( Reply.previous() ));
			}
			
		} else if (cmd.equals( "say" )) {
			// 'say' IS: 'say "what";' OR: 'say egress is back to the wheel;'
			// so we need to trim the quoted speech...
			if (cmds.size() == 1)
				Reply.say( Variable.deref(
					new Strings( Strings.trim( cmds.get( 0 ), Strings.DOUBLE_QUOTE ))
				)                        );
			else
				Reply.say( Variable.deref( new Strings( cmds )));
			
		} else
			r.format( Response.dnu() +":"+ cmd +" "+ cmds );
		
		return r;
}	}
