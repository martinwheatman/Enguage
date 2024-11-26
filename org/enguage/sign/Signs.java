package org.enguage.sign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.interpretant.intentions.Reply;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.pronoun.Pronoun;
import org.enguage.util.attr.Attributes;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Signs extends TreeMap<Integer,Sign> {
	
	public  static final long serialVersionUID = 0l;
	private static       Audit           audit = new Audit( "Signs" );
	
	private final String name;
	
	public Signs( String nm ) {super(); name=nm;}

	public Signs add( Sign[] signs ) {
		for (Sign sign: signs)
			insert( sign );
		return this;
	}
	
	public  Signs insert( Sign insertMe ) {
		int i = 0;
		int c = insertMe.complexity();
		// Crikey! Descending order to put newest first!
		// From old C coding!!
		while (i > -99 && containsKey( c + i )) i--;
		if (i < 99) // Arbitrary limit...
			put( c + i, insertMe );
		else // not tested...
			audit.error( "failed to find place for sign:" );
		return this;
	}
	public Signs rename( String from, String to ) {
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			if (from.equals( me.getValue().concept()))
				me.getValue().concept( to );
		}
		return this;
	}
	public void remove( String id ) {
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while (i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			if (id.equals( me.getValue().concept()))
				i.remove();
	}	}
	public boolean saveAs( String simpleFilter, String cname ) {
		boolean rc = false;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			Sign s = me.getValue();
			if (s.concept().equals(simpleFilter)) {
				String fname = cname==null ? s.pattern().toFilename() : cname;
				if (s.toFile( Concept.spokenName( fname ))) {
					s.concept( fname );
					rc = true;
		}	}	}
		return rc;
	}	

	// -----------------------------
	// -----------------------------
	// SKIP SIGNS Begin 
	// remember which sign we interpreted last
	private int       posn = Integer.MAX_VALUE;
	public  void    foundAt( int i ) { posn = i; }
	public  int lastFoundAt() { return posn; }
	
	// used to save the positions of signs to ignore - now keys of signs to ignore
	// think: might different matches have the same complexity? See: "no, this is a test"
	private ArrayList<Integer> ignore = new ArrayList<>();
	public       List<Integer> ignore() {return ignore;}
	public  void               ignore( int i ) {
		audit.debug("Ignoring: "+ i );
		ignore.add( i );
	}
	public  void               ignoreNone() {ignore.clear();}
	
	private boolean firstMatch = true;
	public  void    firstMatch( boolean b ) {firstMatch = b;}
	public  boolean firstMatch() {return firstMatch;}

	public  void    ignore( Strings cmd ) { // called from Engine.mediate("disamb")!
		audit.in( "ignore", "cmd="+ cmd );
		
		if (       Utterance.previous()               .equals( cmd  ) // first time around
			|| (   Utterance.previous().get(       0 ).equals( "no" ) // subsequent times
			   	&& Utterance.previous().copyAfter( 0 ).equals( cmd  ) // no, ...
			)
			&& Repertoires.signs().lastFoundAt() != Integer.MAX_VALUE)
		{
			firstMatch( true );
			Repertoires.signs().ignore( Repertoires.signs().lastFoundAt() );
		}
		audit.out( "Now avoiding: "+ Repertoires.signs().ignore() );
	}
	
	private void saveForIgnore( int key ) {
		if (firstMatch()) {
			audit.debug( "REDO: Remembering interpreted sign: "+ key );
			foundAt( key );
			firstMatch( false );
			
		} else
			audit.debug( "REDO: Nominal operation" );
	}
	// SKIP SIGNS End
	// -----------------------------
	// -----------------------------

	// -----------------------------
	// -- Audit begin
	private void auditIn( Utterance u ) {
		if (Audit.allAreOn()) {
			audit.in( "mediate",
				"("+ name +"="+ size() +") "
				+ "'"+ u.toString() +"' "
		 		+ (ignore.isEmpty() ? "" : "avoiding "+ignore ));
			audit.debug( "concepts: ["+ Autoload.loaded().toString(Strings.CSV) +"]");
	}	}
	private void auditMatch( int key, Sign s, Attributes match ) {
		if (Audit.allAreOn()) {
			// here: match=[ x="a", y="b+c+d", z="e+f" ]
			audit.debug( "matched: "+ key +"\n"+ s.toStringIndented( true ));
			audit.debug( "Concept: "+s.concept() +"," );
			if (!match.isEmpty())
				audit.debug( "   with: "+ match.toString() +"," );
			if (!Context.context().isEmpty())
				audit.debug( "    and: "+ Context.valueOf());
	}	}
	private void auditOut( String answer ) {
		if (Audit.allAreOn())
			audit.out( answer );
	}
	// --- Audit end
	// -----------------------------
	
	private Reply contextualMediate( Attributes match, Sign s ) {
		Context.push( match );
		Reply r = s.intentions().mediate(); // may pass back DNU
		Context.pop();
		return r;
	}
	
	private Reply doMatch( Sign s, Attributes match ) {
		audit.in( "doMatch", "sign="+ s.pattern() +", vals="+ match );
		
		Pronoun.update( match );
		match.toVariables();
		
		Reply r = contextualMediate( match, s );
		
		audit.out( "Signs.interpretation() returned "+ r.type() );
		return r;
	}

	public Reply mediate( Utterance u ) {
		Reply r = new Reply();
		auditIn( u );
		Set<Map.Entry<Integer,Sign>> entries = entrySet();
		Iterator<Map.Entry<Integer,Sign>> ei = entries.iterator();
		
		while (ei.hasNext()) {
			
			Map.Entry<Integer,Sign> e = ei.next();
			int key = e.getKey(); // based on complexity

			if (ignore.contains( key ))
				audit.debug( "Skipping ignored: "+ key );
			
			else {
				Sign s = e.getValue(); // s knows if it is temporal!	
				// do we need to check if we're repeating ourselves?
				Attributes match = u.match( s );
				if (null == match) {
					//audit.debug( "NO match: "+ s.pattern() +"("+ s.pattern().notMatched()+")" )
					// ; // java doesn't have a 'null' operator!
				} else {
					saveForIgnore( key );
					auditMatch( key, s, match );
					r = doMatch( s, match );
					// if reply is DNU, this meaning is not appropriate!
					if (r.type() != Response.Type.DNU)
						break;
		}	}	}
		auditOut( "reply="+ r.toString() );
		return r;
	}
	
	public Strings find( Strings utterance ) {
		Strings rc = Response.notOkay( utterance +": not found" );
		Utterance u = new Utterance( utterance );
		audit.in( "find", "u="+ u +", utterance="+ utterance );
		Set<Map.Entry<Integer,Sign>> entries = entrySet();
		Iterator<Map.Entry<Integer,Sign>> ei = entries.iterator();
		
		while (ei.hasNext()) {
			
			Map.Entry<Integer,Sign> e = ei.next();
			int key = e.getKey(); // based on complexity

			if (ignore.contains( key ))
				audit.debug( "Skipping ignored: "+ key );
			
			else {
				Sign s = e.getValue(); // s knows if it is temporal!	
				// do we need to check if we're repeating ourselves?
				Attributes match = u.match( s );
				if (null == match) {
					audit.debug( "NO match: "+ s.pattern() +"("+ s.pattern().notMatched()+")" )
					; // java doesn't have a 'null' operator!
				} else {
					saveForIgnore( key );
					auditMatch( key, s, match );
					rc  = s.intentions().toSpokenList( match );
					break;
		}	}	}
		audit.out( "reply="+ rc );
		return rc;
	}
	
	// UNUSED ---
	@Override
	public boolean equals(Object o) {return false;}
	@Override
	public int hashCode() {return 0;}
	// UNUSED ---
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			str.append( me.getValue().toString());
			if (i.hasNext()) str.append( "\n" );
		}
		return str.toString();
	}
	
	public  void show() {show( null );}
	private void show( String simpleFilter ) {
		int n=0;
		Set<Map.Entry<Integer,Sign>> set = entrySet();
		Iterator<Map.Entry<Integer,Sign>> i = set.iterator();
		while( i.hasNext()) {
			Map.Entry<Integer,Sign> me = i.next();
			Sign s = me.getValue();
			if (simpleFilter == null || s.concept().contains(simpleFilter))
				Audit.log( s.toXml( n++, me.getKey() ));
	}	}
}
