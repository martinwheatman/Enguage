package org.enguage.sign.symbol.pronoun;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.pattern.Pattern;
import org.enguage.sign.symbol.config.Plural;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Pronoun {
	static public  final String NAME = "pronoun";
	static public  final int      ID = 198601099; // Strings.hash( NAME )
	static private final Audit audit = new Audit( NAME );
	
	// hard-coded three dimensions...
	static public final    int SUBJECTIVE  = 0;
	static public final    int  OBJECTIVE  = 1;
	static public final    int POSSESSIVE  = 2;
	
	static public final    int    SINGULAR = 0;
	static public final    int      PLURAL = 1;
	
	static public final String    singular = "singular";
	static public final String      plural = "plural";
	
	static public final String  subjective = "subjective";
	static public final String   objective = "objective";
	static public final String subjectives = "subjectives";
	static public final String  objectives = "objectives";
	static public final String  possessive = "possessive";
		
	static private int plurality( String name ) {
		return !possessive( name ) && !Plural.isSingular( name ) ? PLURAL : SINGULAR;
	}
	// ////////////////////////////////////////////////////////////////////////
	
	static private String[][][] pronouns = { // I, we, us
			{{"i",   "it",  "he",  "she", "he or she"  },   // singular subjective
			 {"we",  "they"                            }},  // plural
			{{"me",  "it",  "him", "her", "him or her" },   // singular objective
			 {"us",  "them"                            }},  // plural
			{{"my",  "its", "his", "her", "his or her" },   // singular possessive
			 {"our", "their"                           }} };// plural
	
	static public String pronoun(int os, int ps, int mf) {
		if (os < SUBJECTIVE   || os > POSSESSIVE)   audit.FATAL( "pronoun - SUBJECTIVE fail" );
		if (ps < SINGULAR     || ps > PLURAL)       audit.FATAL( "pronoun - SNG/PLURAL fail" );
		if (mf < Gendered.MIN || mf > Gendered.MAX) audit.FATAL( "pronoun - GENDERED fail" );
		return pronouns[os][ps][mf];
	}
	static private void set(int os, int ps, int mf, String val) {
		pronouns [os][ps][mf] = val;
		if (ps == PLURAL) {
			if (os == SUBJECTIVE) {
				if (mf == Gendered.PERSONAL)
					Pattern.subjGroup( val );
				else if (mf == Gendered.NEUTRAL)
					Pattern.subjOther( val );
			} else if (os == OBJECTIVE) {
				if (mf == Gendered.PERSONAL)
					Pattern.objGroup( val );
				else if (mf == Gendered.NEUTRAL)
					Pattern.objOther( val );
			} else if (os == POSSESSIVE) {
				if (mf == Gendered.PERSONAL)
					Pattern.possGroup( val );
				else if (mf == Gendered.NEUTRAL)
					Pattern.possOther( val );
	}	}	}
	
	static private String[][][] values = { // initialise to names!
			{{"",   "",    "",   "",    ""  },   // singular subjective
			 {"",   "",    "",   "",    ""  }},  // plural
			{{"",   "",    "",   "",    ""  },   // singular objective
			 {"",   "",    "",   "",    ""  }},  // plural
			{{"",   "",    "",   "",    ""  },   // singular possessive
			 {"",   "",    "",   "",    ""  }} };// plural
	
	static private boolean possessivePn( String s ) {
		for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
			if (s.equals( pronouns[POSSESSIVE][SINGULAR][k])) return true;
		for (int k=Gendered.PERSONAL; k<=Gendered.NEUTRAL; k++) // only two plural pronouns
			if (s.equals( pronouns[POSSESSIVE][PLURAL][k])) return true;
		return false;
	}
	static private int type( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++) {
			for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
				if (s.equals( pronouns[i][SINGULAR][k])) return i;
			for (int k=Gendered.PERSONAL; k<=Gendered.NEUTRAL; k++) // only two plural pronouns
				if (s.equals( pronouns[i][PLURAL][k])) return i;
		}
		return -1;
	}
	static private int snglPlIs( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++) {
			for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
				if (s.equals( pronouns[i][SINGULAR][k])) return SINGULAR;
			for (int k=Gendered.PERSONAL; k<=Gendered.NEUTRAL; k++)
				if (s.equals( pronouns[i][PLURAL][k])) return PLURAL;
		}
		return -1;
	}
	static private int gend( String s ) {
		for (int i=SUBJECTIVE; i<=POSSESSIVE; i++) {
			for (int k=Gendered.PERSONAL; k<=Gendered.UNGENDERED; k++)
				if (s.equals( pronouns[i][SINGULAR][k])) return k;
			for (int k=Gendered.PERSONAL; k<=Gendered.NEUTRAL; k++)
				if (s.equals( pronouns[i][PLURAL][k])) return k;
		}
		return -1;
	}
	static private boolean isPronoun( String s ) { return type( s ) != -1;}

	// ------------------------------------------------------------------------
	static private int nameOs( String name ) {
		// TODO: equals and check pluraled
		return name.startsWith( subject ) ? SUBJECTIVE  :
		       name.equals(     object  ) ||
		       name.equals(     objects ) ? OBJECTIVE   :
		   	   name.equals(  possession ) ? POSSESSIVE  : -1; 
	}
	
	/////////////////// VARIABLE NAMES applicable to pronouns /////////////////
	///
	static private String subject = "subject";
	static public  void   subjective( String nm ) { subject = nm; }
	
	static private String object = "this";
	static public  void   objective( String nm ) { object = nm; }
	
	// These are the names of the parameters to use with plural
	// objective and subjective pronouns ("they" and "them")
	static private String subjects = "subjects";
	static public  void   subjectives( String nm ) { subjects = nm; }
	static public  String subjectives() { return subjects; }
	static private String objects = "these";
	static public  void   objectives( String nm ) { objects = nm; }
	static public  String objectives() { return objects; }
	
	// TODO: needs plural possessive, e.g. "s'"
	static private String possession = "'s";
	static public  void   possession( String nm ) { possession = nm; }
	static public  String possession() { return possession; }
	static public boolean possessive( String word ) {
		return appended ?
				word.endsWith( possession )
				: word.startsWith( possession );
	}
	///
	//// VARIABLE NAMES applicable to pronouns ////////////////////////////////
	static private boolean appended = true;
	static public  boolean appended() { return appended;}
	static public  void    appended(boolean b) { appended = b;}
	
	// ////////////////////////////////////////////////////////////////////////
	// Interaction code...
	//
	static private Strings set( Strings sa ) {
		// the masculine personal objective pronoun is him =>
		//    set([ "objective", "singular", "masculine", "him" ]);
		Strings rc = Response.notOkay();
		if (sa.size() == 4) {
			int so = -1, mfn = -1, sp = -1;
			rc = Response.okay();
			while (sa.size() > 1) {
				String s = sa.remove( 0 );
				if (s.equals( singular ))
					sp = SINGULAR;
				else if (s.equals( plural ))
					sp = PLURAL;
				else if (s.equals( possessive ))
					so = POSSESSIVE;
				else if (s.equals( objective ))
					so = OBJECTIVE;
				else if (s.equals( subjective ))
					so = SUBJECTIVE;
				else if (s.equals( Gendered.personal ))
					mfn = Gendered.PERSONAL;
				else if (s.equals( Gendered.masculine ))
					mfn = Gendered.MASCULINE;
				else if (s.equals( Gendered.feminine ))
					mfn = Gendered.FEMININE;
				else if (s.equals( Gendered.neutral ))
					mfn = Gendered.NEUTRAL;
				else {
					rc = Response.notOkay();
					break;
			}	}
			if (rc.equals( Response.okay() ) && so != -1 && sp != -1 && mfn != -1)
				set( so, sp, mfn, sa.remove( 0 ));
		}
		return rc;
	}
	static private Strings name (String name, String value) {
		// e.g. name subjectives subjects
		Strings rc = Response.okay();
		if (name.equals( subjective ))
			subjective( value );
		
		else if (name.equals( subjectives ))
			subjectives( value );
		
		else if (name.equals( objective ))
			objective( value );
		
		else if (name.equals( objectives ))
			objectives( value );
		
		else if (name.equals( possessive ))
			possessive( value );
		
		else
			rc = Response.notOkay();

		return rc;
	}
	static public Strings perform( Strings sa ) {
		// e.g. (pronoun) add masculine martin
		//      (pronoun) set OBJECTIVE PLURAL MASCULINE him
		//      (pronoun) name subjectives [subjects]
		audit.in( "interpret", ""+ sa );
		Strings rc = Response.notOkay();
		int sz = sa.size();
		if (sz > 0) {
			rc = Response.okay();
			String cmd = sa.remove( 0 );
			if (cmd.equals("set" ))
				rc = set( sa );
			else if (cmd.equals( "add" ))
				rc = Gendered.add( sa );
			else if (cmd.equals( "name" ))
				if (1==sz) // original size!
					rc = Response.notOkay(); // just cmd
				else
					name( sa.get( 0 ), sa.get( 1 ));
			else
				rc = Response.notOkay();
		}
		return audit.out( rc );
	}
	// ////////////////////////////////////////////////////////////////////////
	// get/set
	//
	static private void set( String name, String value ) {
		// set SUBJECT he     -> ignore
		//     SUBJECT martin => values[s][s][m]="martin", SUBJECT="martin"
		//     UNIT    cup    => unit="cup"
		int os = nameOs( name );
		if (!isPronoun( value ) && os != -1)
			values[os][plurality( value )][Gendered.valueMfn( value )] = new String( value );
	}
	static private String get( String name, String value ) {
		// N.B value is only used in get to deref pronoun table!
		//audit.in( "get", name + (isPronoun(value)?" (pronoun="+ value +")":""));
		int os = nameOs( name );
		return os != -1 && isPronoun( value ) ?
				values[os][plurality( value )][Gendered.valueMfn( value )]
				: value;
	}
	static private String update( String name, String value ) {
		String orig = value;
		set( name, value );
		String updated = get( name, value );
		return updated.equals( "" ) ? orig : updated;
	}
	static private void update( Attribute a ) {a.value( update( a.name(), a.value() ));}
	static public Attributes update( Attributes as ) { for (Attribute a : as) update( a ); return as;}
	
	// ////////////////////////////////////////////////////////////////////////
	// -- test code
	//
	static private void possessiveOutbound( String value ) {
		String subj = get( subject, value );
		if (possessive( value ))
			Audit.passed( "passes: "+ value +" => "+
					pronouns[POSSESSIVE][plurality( subj )][Gendered.valueMfn( subj )]);
		else
			audit.debug( "failed: "+ value +" != possessive"  );
	}
	static private void possessiveInbound( String pn ) {
		audit.in(  "possInternalising", pn );
		String ans = "internalising failed";
		// e.g. "his"->SUBJECT/"her"->SUBJECT/"its"->OBJECT/"their"
		if (possessivePn( pn ))
			Audit.passed( "Passed: "+ pn +" => "+
					(ans = values[SUBJECTIVE][snglPlIs( pn )][gend( pn )]));
		else
			Audit.passed( "Test fails: Pronoun type not possessive ("+
					type( pn ) +"!="+ POSSESSIVE +")" );
		audit.out( ans );
	}
	static private void testInterpret( String u ) { perform( new Strings( u ));}
	
	static public void main( String args[]) {
		
		testInterpret( "set singular subjective neutral they" );
		audit.debug( "pronoun: "+ pronouns[SUBJECTIVE][SINGULAR][Gendered.NEUTRAL]);
		
		testInterpret( "add masculine martin" );
		testInterpret( "add masculine jamie" );
		testInterpret( "add feminine  ruth" );
		testInterpret( "name subjective SUBJECT" );
		
		Audit.title( "Possession Test" );
		testInterpret( "add masculine martin" );
		set( subject, "martin" );
		
		Audit.subtl( "Outbound Test" );
		possessiveOutbound( "martin" );
		possessiveOutbound( "ruth's" );
		possessiveOutbound( "martin's" );
		
		Audit.subtl( "Inbound Test" );
		possessiveInbound( "him" );
		possessiveInbound( "her" );
		possessiveInbound( "his" );
		
		audit.debug( subject+"/he => "+ update( subject, "he" ));
		audit.debug( "FRED/he => "+ update( "FRED", "he" ));
		
		Attributes a = new Attributes();
		a.add( new Attribute( subject, "martin" ));
		a.add( new Attribute( subject, "he" ));
		audit.debug( "a="+ a );
		audit.debug( "a="+ update( a ));
		Audit.PASSED();
}	}
