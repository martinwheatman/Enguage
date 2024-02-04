package org.enguage.sign.interpretant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.enguage.repertoires.Repertoires;
import org.enguage.sign.Config;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.intentions.Commands;
import org.enguage.sign.interpretant.intentions.Engine;
import org.enguage.sign.interpretant.intentions.SofaPerform;
import org.enguage.sign.interpretant.intentions.Reply;
import org.enguage.sign.interpretant.intentions.Thought;
import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.pattern.Frags;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Intention {
	
	private static final String NAME = "Intention";
	private static       Audit audit = new Audit( NAME );

	public  static final String  AUTOP_STR = "autopoiesis";
	
	public  static final String   RUN_HOOK = "run";
	public  static final String    DO_HOOK = "perform";
	public  static final String REPLY_HOOK = "reply";
	public  static final String FNLLY_HOOK = "finally";

	public  static final String      UNDEF = "u";
	public  static final String        NEW = "w";
	public  static final String     APPEND = "a";
	public  static final String    PREPEND = "p";
	
	public  static final String        POS = "+";
	public  static final String        NEG = "-";
	public  static final String        NEU = "";
	
	public  static final String      THINK = "t";
	public  static final String THEN_THINK = THINK + POS;
	public  static final String ELSE_THINK = THINK + NEG;
	
	public  static final String         DO = "d";
	public  static final String    THEN_DO = DO    + POS;
	public  static final String    ELSE_DO = DO    + NEG;

	public  static final String        RUN = "n";
	public  static final String   THEN_RUN = RUN   + POS;
	public  static final String   ELSE_RUN = RUN   + NEG;
	
	public  static final String      REPLY = "r";
	public  static final String THEN_REPLY = REPLY + POS;
	public  static final String ELSE_REPLY = REPLY + NEG;
	
	// 'finally' intentions are run irrespective of outcome
	public  static final String   FINALLY = "f";
	
	public  static final int N_THEN       = 0x01; // 0000 0001
	public  static final int N_ELSE       = 0x03; // 0000 0011
	
	public  static final int N_THINK      = 0x00; // 0000 00xx
	public  static final int N_DO         = 0x04; // 0000 01xx
	public  static final int N_RUN        = 0x08; // 0000 10xx
	public  static final int N_REPLY      = 0x0c; // 0000 11xx
	
	// written types
	public  static final int UNDEFINED     = -1;

	public  static final int N_THEN_THINK  = N_THEN | N_THINK; // =   1
	public  static final int N_ELSE_THINK  = N_ELSE | N_THINK; // =   3
	public  static final int N_THEN_DO     = N_THEN | N_DO;    // =   4
	public  static final int N_ELSE_DO     = N_ELSE | N_DO;    // =   7
	public  static final int N_THEN_RUN    = N_THEN | N_RUN;   // =   9
	public  static final int N_ELSE_RUN    = N_ELSE | N_RUN;   // =  11
	public  static final int N_THEN_REPLY  = N_THEN | N_REPLY; // =  13
	public  static final int N_ELSE_REPLY  = N_ELSE | N_REPLY; // =  15
	
	public  static final int N_ALLOP       = 0x10;             // =  16
	public  static final int N_THEN_ALLOP  = N_THEN | N_ALLOP; // =  17
	public  static final int N_ELSE_ALLOP  = N_ELSE | N_ALLOP; // =  19
	private static final int N_AUTOP       = 0x14;             // =  20
	public  static final int N_FINALLY     = 0xff;             // = 255
	
	public  static final int condType( int base, boolean isThen, boolean isElse ) {
		if (isThen) return base | N_THEN;
		if (isElse) return base | N_ELSE;
		return base;
	}
	public static int condTypeFromHookStr( String hook, boolean pos, boolean neg ) {
		if (hook.equals(DO_HOOK))
			return condType( N_DO,    pos, neg );
		
		else  if (hook.equals(   RUN_HOOK ))
			return condType( N_RUN,   pos, neg );
		
		else  if (hook.equals( REPLY_HOOK ))
			return condType( N_REPLY, pos, neg );
			
		else  if (hook.equals( FNLLY_HOOK ))
			return N_FINALLY;
		
		return UNDEFINED;
	}
	public static String getCond( Strings sa ) {
		if (sa.begins( Config.soPrefix() )) {
			sa.remove( 0, Config.soPrefix().size()); // e.g. 'if', 'so', ','
			return POS;
		} else if (sa.begins( Config.noPrefix() )) {
			sa.remove( 0, Config.noPrefix().size()); // e.g.'if', 'so', ','
			return NEG;
		}
		return NEU;
	}
	public static int getType(Strings sa) {
		// [ "if", "so", ",", "think", "something" ] => 
		//   N_THEN_THINK + [ "think", "something" ]
		// [ "if", "not", ",", "say", "so" ] =>
		//   N_ELSE_REPLY + []
		int rc = UNDEFINED;
		String cond = getCond( sa );
		boolean neg = cond.equals( NEG );
		boolean pos = cond.equals( POS );
		
		
		if (sa.equals( Config.propagateReplys()) ||
			sa.equals( Config.accumulateCmds()) )
		{
			// don't remove these 'specials', pass on & define in config.xml
			rc = condType( N_REPLY, pos, neg );
			
		} else { // reply "xyz"
			int len = sa.size();
			String hook  = len>0?sa.get(0):"";
			String param = len>1?sa.get(1):"";
			
			if (Strings.isQuoted( param )) {
				// one should be "reply", "perform", etc.
				rc = condTypeFromHookStr( hook, pos, neg );
				if (rc != UNDEFINED) {
					sa.remove(0); // ["perform" | "run" | "reply" | "finally" ]
					sa.set( 0, Strings.trim( sa.get(0), '"' ));
		}	}	}
		
		if (rc == UNDEFINED)
			rc = condType( N_THINK, pos, neg );
		
		return rc;
	}



	public Intention( int t, Strings vals ) {this( t, vals.toString());}
	public Intention( int t, String v ) {type=t; value=v; values=new Strings(v);}
	public Intention( Intention in, boolean temp, boolean spatial ) {
		this( in.type(), in.value() );
		temporalIs( temp );
		spatialIs( spatial );
	}
	
	private final int    type;
	public  final int    type() {return type;}
	
	private final String value;
	public  final String value() {return value;}
	
	private final Strings values;
	public  final Strings values() {return values;}

	private boolean   temporal = false;
	public  boolean   isTemporal() {return temporal;}
	public  Intention temporalIs( boolean b ) {temporal = b; return this;}

	private boolean   spatial = false;
	public  boolean   isSpatial() {return spatial;}
	public  Intention spatialIs( boolean s ) {spatial = s; return this;}

	private static String concept = "";
	public  static void   concept( String name ) { concept = name; }
	public  static String concept() { return concept; }

	// "ok, PERSON needs ..." => "ok martin needs a coffee"
	public  static Strings format( Strings values, String answer, boolean expand ) {
		return 	Variable.deref( // $BEVERAGE + _BEVERAGE -> ../coffee => coffee
					Context.deref( // X => "coffee", singular-x="80s" -> "80"
						new Strings( values )
								.replace( Strings.ellipsis, answer ),
						expand
				)	);
	}
	
	/*
	 * Perform works at the code level to obtain/set an answer.
	 * This was initially the object model, but now includes any code.
	 */
	public  void andFinally( Reply r ) {SofaPerform.perform( r, values );}
	
	private boolean skip( Reply r ) {return type != N_FINALLY && r.isDone();}
	
	public Reply mediate( Reply r ) {
		if (Audit.allAreOn())
			audit.in( "mediate", typeToString( type ) +"='"+ value +"'"+(skip( r )?" >skipping<":"" ));
		
		switch (type) {
 			case N_THINK: r = Thought.think( values,  r.answer() ); break;
 			case N_DO: 	  SofaPerform.perform( r, values );         break;
 			case N_RUN:   r = Commands.run(
 			                       format( values, r.answer(), false ).toString(),
 			                       r.answer()
 			              );                                        break;
 			case N_REPLY: r.reply( values );                        break;
 			case N_ALLOP: r = Engine.interp( this, r );             break;
 			default:
 				if (r.isFelicitous()) {
		 			switch (type) {
						case N_THEN_THINK: r = Thought.think( values, r.answer() ); break;
						case N_THEN_DO:    SofaPerform.perform( r, values );        break;
						case N_THEN_RUN:   r = Commands.run(
								                  format( values, r.answer(), false ).toString(),
								                  r.answer()
								           );                                       break;
						case N_THEN_REPLY: r.reply( values );                       break;
						case N_THEN_ALLOP: r = Engine.interp( this, r );            break;
			 			default: break;
		 			}
	 			} else { // check for is not meh! ?
					switch (type) {
						case N_ELSE_THINK: r = Thought.think( values, r.answer() ); break;
						case N_ELSE_DO:	   SofaPerform.perform( r, values );        break;
						case N_ELSE_RUN:   r = Commands.run(
								                   format( values, r.answer(), false ).toString(),
								                   r.answer()
								           );                                       break;
						case N_ELSE_REPLY: r.reply( values );                       break;
						case N_ELSE_ALLOP: r = Engine.interp( this, r );            break;
			 			default:                                                    break;
		}		}	}

		if (Audit.allAreOn())
			audit.out( r );
		return r;
	}
	/* ------------------------------------------------------------------------
	 * Printy bits
	 */
	public static String typeToString( int type ) {
		switch (type) {
			case N_REPLY      : return REPLY;
			case N_THEN_REPLY : return THEN_REPLY;
			case N_ELSE_REPLY : return ELSE_REPLY;
			case N_THINK      : return THINK;
			case N_THEN_THINK : return THEN_THINK;
			case N_ELSE_THINK : return ELSE_THINK;
			case N_DO         : return DO;
			case N_THEN_DO    : return THEN_DO;
			case N_ELSE_DO    : return ELSE_DO;
			case N_RUN        : return RUN;
			case N_THEN_RUN   : return THEN_RUN;
			case N_ELSE_RUN   : return ELSE_RUN;
			case N_ALLOP      : return Engine.NAME;
			case N_AUTOP      : return AUTOP_STR;
			case N_FINALLY    : return FINALLY;
			default:
				audit.FATAL( "Intention: returning undefined for: "+ type );
				return UNDEF;
	}	}
	public String toString() {
		switch (type) {
			case N_THINK      : return value;
			case N_DO         : return "perform \""+ value +"\"";
			case N_RUN        : return "run \""+ value +"\"";
			case N_REPLY      : return "reply \""+ value +"\"";
			case N_THEN_THINK : return "if so, "+  value;
			case N_THEN_DO    : return "if so, perform \""+ value +"\"";
			case N_THEN_RUN   : return "if so, run \""+ value +"\"";
			case N_THEN_REPLY : return "if so, reply \""+ value +"\"";
			case N_ELSE_THINK : return "if not, "+ value;
			case N_ELSE_DO    : return "if not, perform \""+ value +"\"";
			case N_ELSE_RUN   : return "if not, run \""+ value +"\"";
			case N_ELSE_REPLY : return "if not, reply \""+ value +"\"";
			case N_FINALLY    : return "finally \""+ value +"\"";
			default : return Attribute.asString( typeToString( type ), value() );
	}	}
	/*
	 * Test code...
	 */
	public static Reply test(Reply r, List<Intention> intents) {
		Iterator<Intention> ins = intents.iterator();
		while (!r.isDone() && ins.hasNext()) {
			Intention in = ins.next();
			audit.debug( typeToString( in.type )  +"='"+ in.value +"'" );
		}
		return r;
	}
	public static void main( String[] argv ) {
		Reply r = new Reply().answer( "world" );
		r.type( Response.typeFromStrings( new Strings( "world" )));
		audit.debug( new Intention( N_THEN_REPLY, "hello ..." ).mediate( r ).toString() );
		
		Audit.title( "trad autopoiesis... add to a list and then add that list" );
		r = new Reply();
		ArrayList<Intention> a = new ArrayList<>();
//		a.add( new Intention( N_CREATE, THINK      +" \"a PATTERN z\" \"one two three four\""   ));
//		a.add( new Intention( N_APPEND, ELSE_REPLY +" \"two three four\""   ));
//		a.add( new Intention( N_APPEND, REPLY      +" \"three four\"" ));
		test( r, a );
		audit.debug( Repertoires.signs().toString() );
		audit.debug( r.toString());
		
		Audit.title( "sign self-build II... add pairs of attributes" );
		// now built like this...
		// To PATTERN reply TYPICAL REPLY
		r = new Reply();
		Sign.latest(
				new Sign()
					.pattern( new Frags( "c variable pattern z" ))
					.concept( concept() )
		);
		String reply = "three four";
		Sign.latest().append( Intention.N_THEN_REPLY, reply );
		// ...This implies COND
		Sign.latest().insert( Intentions.Insertion.PREPEND, new Intention( N_THEN_THINK, "one two three four" ));
		// ...if not reply EXECP REPLY
		Sign.latest().insert( Intentions.Insertion.HEADER, new Intention( N_ELSE_REPLY, "two three four" ));
		
		Repertoires.signs().insert( Sign.latest() );
		r.answer( Config.yes().toString() );

		
		audit.debug( Repertoires.signs().toString() );
		audit.debug( r.toString());
}	}
