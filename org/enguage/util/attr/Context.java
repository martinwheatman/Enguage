package org.enguage.util.attr;

import java.util.ArrayList;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
/** Context: a list of attributes, so a list of list of attribute
 *  [ [ one=>123, two=>456,  thr=>789  ],
 *    [ thr=>123, four=>456, five=>789 ]
 *  ]
 *  We request one and get 123, five and get 789, or thr and get 789.
 */
public class Context {
	static Audit audit = new Audit( "Context" );
	
	public  static final int ID = 42758506; // "context"???
	
	private static ArrayList<Attributes> contexts = new ArrayList<>();
	public  static void         push( Attributes ctx ) {contexts.add( 0, ctx );}
	public  static void         pop() {contexts.remove( 0 );}
	public  static Attributes   context() {
		if (contexts.isEmpty()) push( new Attributes() );
		return contexts.get( 0 );
	}
	private static void append( Attribute a ) {contexts.get( 0 ).add( a );}
	
	// these need to be words? [ "hello", "there" ] -> ["hi", "martin"]
	public  static Strings deref( Strings words ) {return deref( words, false );}
	public  static Strings deref( Strings words, boolean expand ) {
		for (Attributes context : contexts)
			context.deref( words, expand );
		return words;
	}
	public  static String get( String word ) {
		for (Attributes context : contexts) {
			String deref = context.value( word );
			if (!deref.equals(""))
				return deref;
		}
		return "";
	}
	public  static Strings perform( Strings a ) {
		audit.in( "interpret", "a="+ a );
		Strings rc = new Strings( Response.notOkay().toString() );
		if (a.size() > 1) {
			String cmd = a.remove( 0 );
			if (cmd.equals( "add" )) {
				String name = a.remove( 0 );
				append( new Attribute( name, a.toString() ));
				rc = new Strings( Response.okay().toString() );
		}	}
		return audit.out( rc );
	}
	public  static String valueOf() {
		StringBuilder sb = new StringBuilder();
		for (Attributes context : contexts)
			sb.append( context.toString() );
		return sb.toString();
	}
	public static void main( String args[]) {
		audit.debug( "hello there, "+ get( "martin" ) );
		push( new Attributes().add( "martin", "world" ));
		audit.debug( "hello there, "+ get( "martin" ) );
		push( new Attributes().add( "hello", "there" ));
		audit.debug( "ctx>>>"+ valueOf() );
}	}
