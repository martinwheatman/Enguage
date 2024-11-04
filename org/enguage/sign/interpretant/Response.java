package org.enguage.sign.interpretant;

import java.util.Locale;

import org.enguage.util.strings.Strings;

public class Response {

	public enum Type {
		DNU( "DNU"   ), // DO NOT UNDERSTAND
		UDU( "UDU"   ), // user does not understand
		DNK( "DNK"   ), // NOT KNOWN -- init
		SOZ( "sorry" ), // SORRY -- -ve
		NO(  "no"    ), // FALSE -- -ve
		OK(  "ok"    ), // TRUE  -- +ve identical to 'yes'
		YES( "yes"   ), // TRUE  -- +ve identical to 'yes'
		CHS( "chs"   ); // narrative verdict - meh!
		
		private Strings value;
		public  void    value( String s ) {value = new Strings( s.toLowerCase( Locale.getDefault() ));}
		public  Strings value() {return value;}
		
		private Type( String s ) {value = new Strings( s );}
	}
	
	public static final boolean isFelicitous( Response.Type type ) {
		return  Response.Type.YES == type ||
				Response.Type.OK  == type ||
				Response.Type.CHS == type;
	}

	public static final Type typeFromStrings( Strings uttr ) {
		     if (uttr.begins( yes()     )) return Type.YES;
		else if (uttr.begins( okay()    )) return Type.OK;
		else if (uttr.begins( notOkay() )) return Type.SOZ;
		else if (uttr.begins( dnu()     )) return Type.DNU; // sorry, i don't think...
		else if (uttr.begins( udu()     )) return Type.UDU;
		else if (uttr.begins( no()      )) return Type.NO;
		else if (uttr.begins( dnk()     )) return Type.DNK;
		else return Type.CHS;
	}

	private static Strings build( Type t, String s ) {
		Strings rc = new Strings( t.value() );
		rc.add( "," );
		for (String str : new Strings( s.toLowerCase( Locale.getDefault() )))
			rc. add( str );
		return rc;
	}
	private static Strings build( Type t, Strings s ) {
		Strings rc = new Strings( t.value() );
		rc.add( "," );
		for (String str : s )
			rc. add( str );
		return rc;
	}

	public  static Strings okay() {return Type.OK.value();}
	public  static Strings okay( String s ) {return build( Type.OK, s );}

	public  static Strings notOkay() {return Type.SOZ.value();}
	public  static Strings notOkay( String s ) {return build( Type.SOZ, s );}
	
	public  static Strings dnk() {return Type.DNK.value();}
	public  static Strings dnk( String s ) {return build( Type.DNK, s );}
	
	public  static Strings no() {return Type.NO.value();}
	public  static Strings no(  String s ) {return build( Type.NO,  s );}
	
	public  static Strings yes() {return Type.YES.value();}
	public  static Strings yes( String s ) {return build( Type.YES, s );}
	
	public  static Strings dnu(){return Type.DNU.value();}
	public  static Strings dnu( String s ) {return build( Type.SOZ, build( Type.DNU, s ));}
	
	public  static Strings udu() {return Type.UDU.value();}
	public  static Strings udu( String s ) {return build( Type.UDU, s );}
}
