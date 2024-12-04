package org.enguage.sign.symbol;

import java.util.ArrayList;
import java.util.List;

import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Sign;
import org.enguage.sign.object.Numeric;
import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.config.Colloquial;
import org.enguage.sign.symbol.config.Englishisms;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.strings.Strings;

public class Utterance {
	//private static Audit   audit      = new Audit( "Utterance" );
		
	// members
	private static Strings previous   = new Strings();
	public  static Strings previous() { return previous; }
	public  static Strings previous( Strings sa ) { return previous = sa; }

	private static boolean understood = true;
	public  static boolean understoodIs( boolean was ) {return understood = was;}
	public  static boolean isUnderstood() {return understood;}

	// versions
	private Strings representamen;
	public  Strings representamen() { return representamen; }
	private Strings temporal;
	private Strings expanded;
	public  Strings expanded() { return expanded; }
	private When    when;
	private Attributes whenAttrs;
	// ... extensible?

	/* Utterance stores all the versions of what has been said, recognising snippets:
	 * - typically learnt in primary, are ctx free: e.g. in/at/on/from/until <time>
	 * - extensible - at runtime. See sgai 2016 paper...
	 * Further, taught word phrases to determine tense: will ~, ~s, ~ed; plurality?
	 */
	public Utterance( Strings orig ) {
		representamen = orig;
		expanded  =	Colloquial.applyIncoming(        // I'm => I am => _user is
						Variable.deref( orig.normalise()) // "i am/." => "i/am/."
					).contract( "=" );      // [ nm, =, "val" ] => [ nm="val" ]
		temporal  = new Strings( expanded );
		when      = When.getWhen( temporal ); // removes temporal content
		whenAttrs = when.toAttributes();
	}
	
	public Attributes match( Sign s ) {
		// This is a high value method so could do with being fast!
		Attributes match = null;

		if (s.isTemporal()) {
			Context.push( whenAttrs );
			if (!when.isUnassigned() &&
			    (null != (match = s.pattern().matchValues( temporal, s.isSpatial() ))))
				match.addAll( whenAttrs ); // add it, don't not pop it.
			Context.pop();  // do pop it! Keep context clean!
		}
		// if no qualified match, attempt an expanded match
		return match == null ? s.pattern().matchValues( expanded, s.isSpatial() ) : match;
	}
		
	public String toString( int layout ) { return expanded.toString( layout );}
	public String toString() { return toString( Strings.SPACED );}
	
	public  static  Strings  externalise( Strings reply, boolean verbatim ) {
		
		reply = Variable.derefOrPop( reply.listIterator());
		
		// outbound and general colloquials
		if (!verbatim)
			reply = Colloquial.applyOutgoing( reply );

		// ...deref any context...
		
		// English-dependent processing...
		reply = Englishisms.indefiniteArticleVowelSwap( reply );
		
		// TODO: to Strings, or move up into derefOrPop?
		Strings tmp = new Strings();
		for (String r : reply)
			tmp.add( Strings.fromUnderscored( r ));
		reply = tmp;
		
		return Englishisms.asStrings( Numeric.deref( reply ))
				.contract( Englishisms.APOSTROPHE );
	}
	
	// --
	// -- Conjunction - Begin
	// --
	/*
	 * Leaving this for now. Spent several days allowing an 'and'
	 * at the beginning of a sentence XD. Leaving the issue of:
	 *    "i need a coffee and james and ruth need a tea."
	 * Can solve this by type change - coffee being a 'supermarket 
	 * commodity', whereas ruth and james are 'known people'?
	 * There are larger fish to fry!!!
	 *   
	 */
	private static List<Strings> spllit( Strings ths, String sep ) {
		/* we don't want empty strings in the list.
		 * Only split if we've collected 'something'.
		 */
		ArrayList<Strings> output = new ArrayList<>();
		Strings sentence = new Strings();
		for (String str : ths) {
			if (str.equals( sep ) && !sentence.isEmpty()) {
				output.add( sentence );
				sentence = new Strings();
			} else // will add a second splitter in a row
				sentence.add( str );			
		}
		if (!sentence.isEmpty()) output.add( sentence );
		return output;
	}
	public static List<Strings> conjuntionAlley( Strings s, String conj ) {
		ArrayList<Strings> rc = new ArrayList<>();
		boolean found = false;
		Strings frag = new Strings();
		List<Strings> listOfStrings = spllit( s, conj );
		for (Strings strings : listOfStrings) {
			if (!found) {
				if (!frag.isEmpty()) frag.add( conj );
				frag.addAll( strings );
				found = !Concept.match(strings).isEmpty();
				
			} else {
				Strings matched = Concept.match(strings);
				if (matched.isEmpty()) {
					frag.add( conj );
					frag.addAll( strings );
					
				} else {
					rc.add( frag );
					frag = strings;
					found = false;
				}
		}	}
		if (!frag.isEmpty()) rc.add( frag );
		return rc;
}	}
