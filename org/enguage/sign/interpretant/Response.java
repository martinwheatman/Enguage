package org.enguage.sign.interpretant;

import java.util.Locale;

import org.enguage.util.strings.Strings;

public class Response {

	public enum Type {
		E_DNU, // DO NOT UNDERSTAND
		E_UDU, // user does not understand
		E_DNK, // NOT KNOWN -- init
		E_SOZ, // SORRY -- -ve
		E_NO,  // FALSE -- -ve
		E_OK,  // TRUE  -- +ve identical to 'yes'
		E_YES, // TRUE  -- +ve identical to 'yes'
		E_CHS; // narrative verdict - meh!
	}
	
	public static final boolean isFelicitous( Response.Type type ) {
		return  Response.Type.E_YES == type ||
				Response.Type.E_OK  == type ||
				Response.Type.E_CHS == type;
	}

	public static final Type typeFromStrings( Strings uttr ) {
		     if (uttr.begins( yes()     )) return Type.E_YES;
		else if (uttr.begins( okay()    )) return Type.E_OK;
		else if (uttr.begins( notOkay() )) return Type.E_SOZ;
		else if (uttr.begins( dnu()     )) return Type.E_DNU;
		else if (uttr.begins( udu()     )) return Type.E_UDU;
		else if (uttr.begins( no()      )) return Type.E_NO;
		else if (uttr.begins( dnk()     )) return Type.E_DNK;
		else return Type.E_CHS;
	}

	private static Strings okay    = new Strings( "ok" );
	public  static void    okay( String s ) {okay = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings okay() {return okay;}

	private static Strings notOkay = new Strings( "sorry" );
	public  static void    notOkay( String s ) {notOkay = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings notOkay() {return notOkay;}
	
	private static Strings dnk = new Strings( "DNK" );
	public  static void    dnk( String s ) {dnk = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings dnk() {return dnk;}
	
	private static Strings no = new Strings( "no" );
	public  static void    no(  String s ) {no = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings no() {return no;}
	
	private static Strings yes    = new Strings( "yes" );
	public  static void    yes( String s ) {yes = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings yes() {return yes;}
	
	private static Strings dnu = new Strings( "DNU" );
	public  static void    dnu( String s ) {dnu = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings dnu(){return dnu;}
	
	private static Strings udu = new Strings( "UDU" );
	public  static void    udu( String s ) {udu = new Strings( s.toLowerCase( Locale.getDefault() ));}
	public  static Strings udu() {return udu;}
}
