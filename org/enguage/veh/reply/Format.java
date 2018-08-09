package org.enguage.veh.reply;

import org.enguage.obj.Variable;
import org.enguage.sgn.ctx.Context;
import org.enguage.util.Audit;
import org.enguage.util.Strings;

public class Format {
	/** Format:
	 * e.g. ["Ok", ",", "you", "need", Strings.ELLIPSIS"]
	 */
	
	static private Audit audit = new Audit( "Fmt" );
	
	private boolean shrt = false;
	public  boolean shrt() { return shrt; }
	public  void    shrt( boolean b ) { shrt = b; }
	
	private boolean variable = false;
	public  boolean variable() {return variable;}
	
	private Strings format = new Strings();
	public  Strings ormat() { return format; }
	public  void    ormat(String s) {
		format = new Strings( s );
		if (format.size() > 0 && format.get(0).equals( Strings.ELLIPSIS )) variable = true;
		// Decontextualise format, while we're in the moment!
		format = Context.deref( format );
	}
	static public void main( String args[] ) {
		Format f = new Format();
		Variable.set( "FMTEST", "martin" );
		f.ormat( "FMTEST needs "+ Strings.ELLIPSIS );
		audit.log( "fmt: "+ f.ormat());
}	}