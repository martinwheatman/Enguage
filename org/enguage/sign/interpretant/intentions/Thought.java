package org.enguage.sign.interpretant.intentions;

import org.enguage.repertoires.Repertoires;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.symbol.Utterance;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Thought {
	
	private Thought() {}
	
	private static final String NAME = "Thought";
	private static final Audit audit = new Audit( NAME );

	public static  Reply think( Strings values, String prevAnswer ) {

		// Don't expand, UNIT => cup NOT unit='cup'
		Strings thought = Intention.format( values, prevAnswer, false );
		audit.debug( "Thinking: "+ thought.toString( Strings.CSV ));
		
		// think it...
		Reply r = Repertoires.mediate( new Utterance( thought ));

		if (Response.Type.DNU == r.type()) {
			// put this into reply via Reply.strangeThought()
			audit.debug( "Strange thought: I don't understand: '"+ thought +"'" );
			
			// Construct the DNU format
			r.toDnu( thought );
		}
		
		r.doneIs(
				r.type() == Response.Type.SOZ   &&
				Strings.isUCwHyphUs( values.toString() ) // critical!
		);
		
		return r;
}	}
