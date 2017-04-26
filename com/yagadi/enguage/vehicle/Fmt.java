package com.yagadi.enguage.vehicle;

import com.yagadi.enguage.object.Variable;
import com.yagadi.enguage.util.Audit;
import com.yagadi.enguage.util.Strings;

public class Fmt {
	/** Fmt:
	 * e.g. ["Ok", ",", "you", "need", Strings.ELLIPSIS"]
	 */
	
	static private Audit audit = new Audit( "Fmt" );
	
	private boolean shrt = false;
	public  boolean shrt() { return shrt; }
	public  void    shrt( boolean b ) { shrt = b; }
	
	private boolean variable = false;
	public  boolean variable() {return variable;}
	
	private Strings ormat = new Strings();
	public  Strings ormat() {
		/*if (shrt) {
			if (ormat().size() > 1 && ormat().get( 1 ).equals( "," ))
				if (ormat().get( 0 ).equalsIgnoreCase( yes ) || // yes is set to "OK", when yes is "yes", this fails...
					ormat().get( 0 ).equalsIgnoreCase(  no ) ||
					ormat().get( 0 ).equalsIgnoreCase( success )) {
					return ormat().get( 0 ));
				} else if (ormat().get( 0 ).equalsIgnoreCase( failure )) {
					return ormat().copyAfter( 1 ).filter(); // forget 1st & 2nd
				}
			return ormat().filter();
		}*/
		return ormat;
	}
	public  void    ormat(String s) {
		ormat = Colloquial.applyOutgoing(
					Context.deref(
						Variable.deref(
							new Strings( s )
				)	)	);
		if (ormat.contains( Strings.ELLIPSIS )) variable = true;
	}
	static public void main( String args[] ) {
		Fmt f = new Fmt();
		f.ormat( "SUBJECT needs "+ Strings.ELLIPSIS );
		audit.log( "fmt: "+ f.ormat());
}	}
