package org.enguage.sign.symbol.pronoun;

import java.util.ArrayList;
import java.util.ListIterator;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.strings.Strings;

// ////////////////////////////////////////////////////////////////////////
// gender...
//
public class Gendered {
	// neutral is second to hold all plural pronouns...
	static public final    int         PERSONAL = 0;
	static public final    int MIN =   PERSONAL;
	static public final    int          NEUTRAL = 1;
	static public final    int        MASCULINE = 2;
	static public final    int         FEMININE = 3;
	static public final    int       UNGENDERED = 4;
	static public final    int MAX = UNGENDERED;
	
	static public final String    neutral = "neutral";
	static public final String   feminine = "feminine";
	static public final String  masculine = "masculine";
	static public final String   personal = "personal";
	
	static class Genders extends ArrayList<Gendered> {
		public static final long serialVersionUID = 0l;
		public Genders append( Gendered g ) { add( g ); return this;}
	}
		
	public Gendered( String nm, int gdr ) { name = nm; gender = gdr;}

	int gender = -1;
	
	private String name;
	public  String name() { return name;}
	
	static Strings add (Strings sa) {
		// e.g. add masculine martin feminine ruth
		Strings rc = Response.okay();
		while (rc.equals( Response.okay() ) && sa.size() > 1) {
			String gender = sa.remove( 0 );
			if (gender.equals( masculine ))
				masculineIs( sa.remove( 0 ));
			else if (gender.equals( feminine ))
				feminineIs( sa.remove( 0 ));
			else if (gender.equals( personal ))
				personalIs( sa.remove( 0 ));
			else if (gender.equals( neutral ))
				neutralIs( sa.remove( 0 ));
			else
				rc = Response.notOkay();
		}
		return rc;
	}

	static Genders gendered = new Genders()
		.append( new Gendered( "he",  Gendered.MASCULINE ))
		.append( new Gendered( "his", Gendered.MASCULINE ))
		.append( new Gendered( "him", Gendered.MASCULINE ))
		.append( new Gendered( "she", Gendered.FEMININE  ))
		.append( new Gendered( "her", Gendered.FEMININE  ))
		.append( new Gendered( "she", Gendered.FEMININE  ))
		.append( new Gendered( "i",   Gendered.PERSONAL  ))
		.append( new Gendered( "me",  Gendered.PERSONAL  ))
		.append( new Gendered( "my",  Gendered.PERSONAL  ))
		.append( new Gendered( "it",   Gendered.UNGENDERED  ))
		.append( new Gendered( "they",  Gendered.UNGENDERED  ))
		.append( new Gendered( "them",  Gendered.UNGENDERED  ))
		.append( new Gendered( "he or she",  Gendered.NEUTRAL  ))
		.append( new Gendered( "him or her",  Gendered.NEUTRAL  ))
		.append( new Gendered( "his or her",  Gendered.NEUTRAL  ))
		.append( new Gendered( "their",  Gendered.NEUTRAL  ));
	
	
	static int valueMfn( String s ) {
		if (Pronoun.possessive( s )) s = s.substring( 0, s.length()-Pronoun.possession().length());
		return isNeutral(      s ) ? Gendered.UNGENDERED
				: isPersonal(  s ) ? PERSONAL
				: isMasculine( s ) ? Gendered.MASCULINE
				: isFeminine(  s ) ? Gendered.FEMININE : Gendered.NEUTRAL ;
	}
	static void    neutralIs(   String wd ) {
		removeAll( wd );
		gendered.add( new Gendered( wd, Gendered.NEUTRAL ));
	}
	static boolean isNeutral(   String wd ) { return isGendered( wd, Gendered.NEUTRAL );}
	static void    feminineIs(  String wd ) {
		removeAll( wd );
		gendered.add( new Gendered( wd, Gendered.FEMININE ));
	}
	static boolean isFeminine(  String wd ) { return isGendered( wd, Gendered.FEMININE );}
	static void    masculineIs( String wd ) {
		removeAll( wd );
		gendered.add( new Gendered( wd, Gendered.MASCULINE ));
	}
	static boolean isMasculine( String wd ) { return isGendered( wd, Gendered.MASCULINE );}
	static void    personalIs( String wd ) {
		removeAll( wd );
		gendered.add( new Gendered( wd, Gendered.PERSONAL ));
	}
	static boolean isPersonal( String wd ) { return isGendered( wd, Gendered.PERSONAL );}
	static void removeAll( String name ) {
		ListIterator<Gendered> gi = gendered.listIterator();
		while (gi.hasNext())
			if (gi.next().name().equals( name )) 
				gi.remove();
	}
	static boolean isGendered( String name, int gender ) {
		ListIterator<Gendered> gi = gendered.listIterator();
		while (gi.hasNext()) {
			Gendered g = gi.next();
			if (g.name().equals( name ) && g.gender == gender )
				return true;
		}
		return false;
}	}
