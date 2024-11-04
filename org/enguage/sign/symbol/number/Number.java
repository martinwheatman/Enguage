package org.enguage.sign.symbol.number;

import java.util.ListIterator;

import org.enguage.sign.object.expr.Function;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.symbol.config.Englishisms;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Number {
	/*
	 * This class is too long, and needs reimplementing!
	 * Look at When.java (to/from value/string) for a better spec!
	 * 
	 */
			
	static private Audit audit = new Audit( "Number" );
	/* a number is obtained from a string of characters, and can be:
	 * relative/absolute, exact/vague, positive/negative, e.g
	 * i need  /another 3/  cups of coffee
	 * i need /another few/ cups of coffee
	 * i need     /some/    cups of coffee
	 * i need       /a/     cup  of coffee
	 * i need   /1 plus 2/  cups of coffee
	 */
	/* NEED TO DEFINE (non-)Numerics, e.g.:
	 * <xml>
	 * 	<nonnumeric quantity='1'  relative='no'  exact='no'  multiplier='no'>some</nonnumeric>
	 * 	<nonnumeric quantity='2'  relative='no'  exact='yes' multiplier='no'>a pair</nonnumeric>
	 * 	<nonnumeric quantity='~2' relative='no'  exact='no'  multiplier='no'>a couple</nonnumeric>
	 * 	<nonnumeric quantity='~3' relative='no'  exact='no'  multiplier='no'>a few</nonnumeric>
	 * 	<nonnumeric quantity='1 more' relative='yes' exact='yes' multiplier='yes'>another</nonnumeric>
	 * 	<nonnumeric quantity='1'  relative='no'  exact='yes' multiplier='yes'>one</nonnumeric>
	 * </xml>
	 */
	/* 
	 * Much as this project is about CONNOTATIONALISM, numbers (and their descriptions)
	 * have a denotational dimension. Something like:
	 * a [few|couple of|] |
	 * about              } [<numeric: digits[{OP digits}]               { [more|less|]
	 * another            }          [[all|]squared|cubed][OP numeric]>]
	 * 
	 *                      **** THUS, THIS IS LANGUAGE SPECIFIC. ****
	 */
	static public  final String NOT_A_NUMBER = "not a number";
	static public  final String         MORE = "more";
	static public  final String        FEWER = "less";
	static public  final String      ANOTHER = "another";
	
	// ===== getNumber(): a Number representamen Factory
	static private final Strings        All = new Strings(             "all" );
	static private final Strings      Cubed = new Strings(           "cubed" );
	static private final Strings    Squared = new Strings(         "squared" );
	static private final Strings    PowerOf = new Strings( "to the power of" );
	static public  final Strings NotANumber = new Strings(    "not a number" );
	

	static private boolean aImpliesNumeric = true;
	static public  void    aImpliesNumeric( boolean b ) {aImpliesNumeric = b;}
	static public  boolean aImpliesNumeric() { return aImpliesNumeric;}
	
	// properties of a number...
	private boolean isRelative = false;
	public  boolean isRelative() { return isRelative; }
	public  Number  isRelative( boolean b ) { isRelative = b; return this; }
	
	private boolean isAscending = true;
	public  boolean isAscending() { return isAscending; }
	public  Number  isAscending( boolean b ) { isAscending = b; return this; }
	
	public  boolean isInteger() { return representamen.isInteger(); }
	public  Number  isInteger( boolean b ) { representamen.isInteger( b ); return this; }
	
	private boolean isExact = true;
	public  boolean isExact() { return isExact; }
	public  Number  isExact( boolean b ) { isExact = b; return this; }
	
	private Float   magnitude = Float.NaN;
	public  Float   magnitude() { return magnitude; }
	public  Number  magnitude( Float valueToSet ) {
		if (!valueToSet.isNaN()) // <<< TODO: take this out?
			magnitude = isRelative() ? Math.abs( valueToSet ) : valueToSet;
		return this;
	}
	
	private Representamen representamen = new Representamen();
	public  Representamen representamen() { return representamen; }
	public  void    representamen( Representamen s ) { representamen=s; }
	public  Number  append( String s ) {
		representamen.add( s );
		return this;
	}
	private Number  append( Strings sa ) {
		representamen.addAll( sa );
		return this;
	}
	private void remove( ListIterator<String> si, int n ) {
		int sz = representamen.size();
		int req = sz - n;
		while (req<sz) {
			si.previous();
			representamen.remove( --sz );
	}	}
	public static String floatToString( Float f ) {
		//audit.in( "floatToString", f.toString() );
		String value;
		if (Float.isNaN( f ))
			value = Number.NOT_A_NUMBER;
		else {
			// 3.0 => "3" -- remove ".0"
			// 3.25 => "3 point 2 5" -- replace "." with "point", .nn should be spelled out
			int i;
			value = Float.toString( f );
			if (value.substring( value.length()-2 ).equals( ".0" ))
				value = value.substring( 0, value.length()-2 );
			if (-1 != (i = value.indexOf( "." )))
				value = value.substring( 0, i ) + " point " + Englishisms.spell( value.substring( i+1 )); 
		}
		//return audit.out( value );
		return value;
	}
	public Number( String s ) {  // e.g. +=6
		if (s != null && s.length() > 0) {
			char sign;
			if (s.length() > 1 && ((sign = s.charAt( 1 )) == '=' || sign == '~')) {
				//audit.debug( "number from value" );
				isRelative( true );
				isAscending( s.charAt( 0 ) == '+' );
				isExact( sign == '=' );
				magnitude( Float.valueOf( s.substring( 2 )));
				// create representamen
				if (!isExact()) append( "about" );
				append( s.substring( 2 ));
				append( isAscending() ? MORE : FEWER );
			} else {
				//audit.debug( "number from string" );
				Number n = new Number( new Strings( s ).listIterator()); // getNumber( s );
				isRelative(  n.isRelative );
				isAscending( n.isAscending );
				magnitude( n.magnitude );
				isExact(     n.isExact );
				isInteger(   n.isInteger() );
				representamen( n.representamen );
	}	}	}

	public Number combine( Number n ) {
		audit.in( "combine", "'"+ toString() + "' with '" + n.toString() +"'" );
		if (n.isRelative()) {
			audit.debug( "rel="+ isRelative +" && n.rel="+ n.isRelative );
			// relative 
			isRelative( isRelative && n.isRelative ); // need to set rel before magnitude!
			isExact(     isExact &&   n.isExact ); // lowest common denominator
			isInteger( isInteger() && n.isInteger() ); // lowest common denominator
			Float valueToSet = (isAscending ? magnitude : -magnitude) + (n.isAscending ? n.magnitude() : -n.magnitude()); 
			isAscending( valueToSet >= 0.0F );
			magnitude( valueToSet );
			
			// this = 3 && n -= 6,  this = -3
			// recreate representamen
			representamen = new Representamen();
			if (!isExact()) append( "about" );
			String tmp = String.valueOf( magnitude() );
			if (isInteger()) tmp = tmp.substring( 0, tmp.length() - 2 );
			append( tmp );
			if (isRelative) append( isAscending ? MORE : FEWER );

		} else {
			audit.debug( "n is abs" );
			// replace this with n
			isRelative(  false ); // need to set rel before magnitude!
			isAscending(  n.isAscending );
			magnitude( n.magnitude );
			isExact(     n.isExact );
			isInteger(   n.isInteger() );
			representamen( n.representamen );
		}
		return (Number) audit.out( this );
	}
		
	public String toString() {
		// e.g. "another 10"
		return representamen.size() == 0 ? NOT_A_NUMBER : representamen.toString();
	}
	public Strings valueOf() {
		//audit.in( "valueOf" );
		// value = a|[about] N [more/fewer]
		String rc;
		//idx = 0; // initialise dx for f2Str()
		representamen.resetIdx();
		if (representamen.size() == 0)
			rc = Number.NOT_A_NUMBER;
		else {
			rc = floatToString( magnitude() );
			if (!rc.equals(Number.NOT_A_NUMBER))
				rc = (isRelative ? (isAscending ? "+" : "-" ) + (isExact ? "=" : "~") : "") + rc;
		}	
		//return audit.out( rc );
		return new Strings().append( rc );
	}
	public static Float value( String s ) {
		if (s.equalsIgnoreCase(   "zero" )) return  0f;
		if (s.equalsIgnoreCase(    "one" )) return  1f;
		if (s.equalsIgnoreCase(    "two" )) return  2f;
		if (s.equalsIgnoreCase(  "three" )) return  3f;
		if (s.equalsIgnoreCase(   "four" )) return  4f;
		if (s.equalsIgnoreCase(   "five" )) return  5f;
		if (s.equalsIgnoreCase(    "six" )) return  6f;
		if (s.equalsIgnoreCase(  "seven" )) return  7f;
		if (s.equalsIgnoreCase(  "eight" )) return  8f;
		if (s.equalsIgnoreCase(   "nine" )) return  9f;
		if (s.equalsIgnoreCase(    "ten" )) return 10f;
		if (s.equalsIgnoreCase( "eleven" )) return 11f;
		if (s.equalsIgnoreCase( "twelve" )) return 12f;
		return Float.NaN;
	}
	private void appendPostOp( ListIterator<String> si ) {
		int oplen, x=0;
		String power = "";
		do {
			oplen = 0;
			if (si.hasNext()) {
				int    n = 0;
				if (   0 != (x = All.peekwals( si )))
				{
					for (int j=0; j<x; j++) si.next();
					n+=x; // can only be 1
				}
				if (   0 != (x =   Cubed.peekwals( si ))
				    || 0 != (x = Squared.peekwals( si )))
				{
					Strings.next( si, x );
					oplen = (n+=x); // success!
					
				} else if ( 0 != (x = PowerOf.peekwals( si ))) { // we can say "all to the ..."
					Strings.next( si, x ); // skip past "to the power of"
					n+=x;  // add len "to the..."
					
					if (si.hasNext())
						if (!Strings.isNumeric( power = si.next() )) {
							si.previous();
							power = ""; // reset tmp!
						} else
							oplen = ++n; // success!
				}
				Strings.previous( si, n );      // now, put them all back!
				if (n > 0) representamen.append( si, oplen ); // might go back 2 and tx none, or 2.
				if (!power.equals( "" )) representamen.append( si, 1 ); // in case there is a power to add
			}
		} while (++x<10 && oplen > 0);
	}
	private int appendOp( ListIterator<String> si) {
		// creating representamen - appending an op, e.g. "plus"|"all multiplied by"
		int len = 0;
		if (si.hasNext()) {
			String op = si.next();
			int n = 1;
			if (op.equals( "all" ) && si.hasNext()) {
				op = si.next();
				n++;
			}
			if (   op.equals( "times"      )
			    || op.equals( "multiplied" )
			    || op.equals( "divided"    ))
			{
				if ( op.equals( "times" ))
					len = n; // times is ok on its own

				if (si.hasNext()) {
					op = si.next();
					n++;
					if (op.equals( "by" )) {
						len = n;
				}	}

			} else if ( op.equals( "+" ) || op.equals( "plus"  ) ||
						op.equals( "-" ) || op.equals( "minus" ) ||
						op.equals( "x" ) || //op.equals( "over" ) ||
						op.equals( "/" )                            )
				len = n;
			
			// n >= len
			Strings.previous( si, n );
			if (n > 0) representamen.append( si, len );
		}
		return len;
	}
	private boolean appendNumeral( ListIterator<String> si ) {
		//audit.in( "doNumeral", Strings.peek( si ));
		boolean rc = false;
		if (si.hasNext()) {
			String token = si.next();
			if (rc = Strings.isNumeric( token ))
				append( token );
			else
				si.previous(); // not numeric and not "the" and not "the."
		}
		return rc; //audit.out( rc );
	}
	private boolean appendNum( ListIterator<String> si ) {
		//audit.in( "doNum", Strings.peek( si ));
		boolean rc = false;
		if (si.hasNext()) {
			String token = si.next();
			if (rc = Strings.isNumeric( token )) {
				magnitude( Strings.valueOf( token ));
				append( token );
			} else 
				si.previous();
		}
		//return audit.out( rc );
		return rc;
	}
	/* getNumber() identifies how many items in the array, from the index are numeric
	 *   [..., "68",    "guns", ...]         => 1 //  9
	 *   [..., "17", "x",  "4", "guns", ...] => 3 //  9
	 *   [..., "another", "68", "guns", ...] => 2 // +6
	 *   [..., "68",    "more", "guns", ...] => 2 // +6
	 *   [..., "some",  "guns", ...]         => 1 // <undefined few|many>
	 *   [..., "some",   "jam", ...]         => 0 -- jam is not plural!
	 *   [..., "a",      "gun", ...]         => 1 // 1
	 */
	private boolean doA( ListIterator<String> si ) {
		if (si.hasNext() && aImpliesNumeric()) {
			if (si.next().equals( "a" )) { // ascending only valid if relative!
				// need to set rel before magnitude!
				isRelative( false )/*.ascending( true )*/.magnitude( 1F ).append( "a" );
				
				if (si.hasNext()) {
					String tmp = si.next();
					if (tmp.equals( "few"))
						append( "few" ).isExact( false ).magnitude( 3.0f );
						
					else if (tmp.equals( "couple")) {
						append( "couple" ).isExact( false ).magnitude( 2.0f );
						if (si.hasNext())
							if (si.next().equals( "of" ))
								append( "of" );
							else
								si.previous(); // put !'of' back
						
					} else // something
						si.previous();
					return true; // a few, a couple, a something
					
				} else
					si.previous(); // put 'a' back
				// 'a' on its own -- fall through to false
			} else
				si.previous(); // put 'a' back
		}
		return false;
	}
	private boolean doAbout( ListIterator<String> si ) {
		//audit.in( "doAbout", Strings.peek( si ));;
		boolean rc = false;
		if (si.hasNext())
			if (rc = si.next().equals( "about"))
				isExact( false ).append( "about" );
			else
				si.previous();
		//return audit.out( false );
		return rc;
	}
	private boolean doAnother( ListIterator<String> si ) {
		//audit.in( "doAnother", Strings.peek( si ));
		boolean rc = false;
		if (si.hasNext())
			if (rc = si.next().equals( ANOTHER )) {
				audit.debug( "found "+ ANOTHER );
				isRelative( true );  // need to set rel before magnitude!
				isAscending( true );
				isExact( true );
				magnitude( 1.0f );
				append( ANOTHER );
			} else
				si.previous();
		//return audit.out( rc );
		return rc;
	}
	private void doMoreOrLess( ListIterator<String> si ) {
		// POST-numeric- deal with 'more' OR 'less'...  following numbers
		//audit.in( "doPostExpr", ">>>"+ Strings.peek( si ));
		if (si.hasNext()) {
			String token = si.next();
			if (token.equals( MORE )) {
				audit.debug( "found "+ MORE );
				isRelative( true ).isAscending(  true );
				append( MORE );
			} else if (token.equals( FEWER )) {
				audit.debug( "found "+ FEWER );
				isRelative( true ).isAscending( false );
				append( FEWER );
			} else
				si.previous();
		}
		//audit.out();
	}
	private static Strings getActualParams( ListIterator<String> si ) {
		//audit.in( "getActualParams", Strings.peek( si ));
		Strings params = null;
		boolean andRead = false;
		
		while (si.hasNext()) {
			Number n = new Number( si );
			if (n.toString().equals(NOT_A_NUMBER))
				break;
			else {
				if (null == params) params = new Strings();
				params.add( n.toString());
				if (Strings.peek( si ).equals( "and" )) {
					andRead = true;
					params.add( si.next());
				} else {
					andRead = false;
					break;
		}	}	}
		if (andRead) si.previous();
		return params; // audit.out( params ); // returns null or param list
	}
	private boolean appendFunction( ListIterator<String> si ) {
		//audit.in( "doFunction", Strings.peek( si ));
		boolean rc = false;
		if (si.hasNext()) {
			String token = si.next();
			if (token.equals( "the" ) && si.hasNext()) {
				// could this be: the <FUNCTION/> of <PARAMS/> ?
				String fn = si.next();
				if (si.hasNext()) {
					Strings params = null;
					if (si.next().equals( "of" ) && 
						null != (params = getActualParams( si )) //&&      // ["2",  "3"] <= getActualParams( "2 and 3" );
						//null != (token  = funcEval( fn, params ))) // "5" <= numEval("sum", ["2",  "3"] )
					){	
						rc = true;
						append( "the" ).append(fn).append("of").append( params );
					} else
						Strings.previous( si, 3 ); // "the fname of ... and ...".
				} else
					Strings.previous( si, 2 ); // "the fname." 
			} else
				si.previous(); // "the." 
		}
		//return audit.out( rc );
		return rc;
	}
	private boolean appendExpr( ListIterator<String> si ) {
		//audit.in( "appendExpr", Strings.peek( si ));
		boolean rc = false;
		if (appendNum( si ) || appendFunction( si )) {
			rc = true;
			int done;
			do {
				// optional, so ignore any return code
				appendPostOp( si ); // [all] squared|cubed|to the power of
				if (   0 < (done = appendOp( si )) // ... times
					&& !(appendNumeral( si ) || appendFunction( si )))
				{ // if done op but not numeral remove op..
					remove( si, done );
					done = 0;
				}
			} while (si.hasNext() && done > 0);
			
			magnitude( representamen.doTerms()); // need to do this before more or less
			doMoreOrLess( si );
		}		
		//audit.out( rc );
		return rc;
	}
	public Number( ListIterator<String> si ) {
		if (si.hasNext()) {
			if (doA(       si ) ||
				doAbout(   si ) ||
				doAnother( si )   );
			
			appendExpr( si );
	}	}

	//* ===== test code ====
	static private void evaluationTest( String term, String ans ) {
		ListIterator<String> si = new Strings( term ).listIterator();
		Number n = new Number( si );
		audit.debug( "n is '"+ n.toString() +"' ( '"+ ans +"' == '"+ n.valueOf() +"' ) sz="+ n.representamen().size() );
		if (!ans .equals( n.valueOf().toString() ))
			audit.FATAL( "Doh!" );
	}
	static private void getNumberTest( String s ) { getNumberTest( s, "" ); }
	static private void getNumberTest( String s, String expected ) {
		//audit.in( "getNumberTest", s );
		Strings orig = new Strings( s );
		ListIterator<String> si = orig.listIterator();
		Number n = new Number( si );
		Strings val = n.valueOf(), strg = n.valueOf();
		audit.debug(
				s +": toString=>"+ strg +"<"
				+" rep=>"+ n.representamen() +"<"
				+" valueOf=>"+ val +"<"
				+" mag="+ n.magnitude()
				+ (si.hasNext() ? ", nxt token is >>>"+ si.next() + "<<<" : "")
		);
		if (!expected.equals( "" ) && !expected.equals( n.valueOf().toString() ))
			audit.FATAL( "getNumberTest(): "+ s +": "+  val +" != expected("+ expected +")");
		//audit.out();
	}
	static private void combineTest( String number, String with, String expected, String expValue ) {
		Number m = new Number( number );
		m.combine( new Number( with ));
		audit.debug( "m="+ m.toString() +"("+ m.valueOf() +")");
		if (!expValue.equals( "" ) && !expValue.equals( m.valueOf().toString()))
			audit.FATAL( "Values not equal: "+ expValue +" != "+ m.valueOf() );
		if (!expected.equals( "" ) && !expected.equals( m.toString()))
			audit.FATAL( "Strings not equal: '"+ expected +"' != '"+ m.toString() +"'" );
	}
	public static void main( String[] args ) {
		//Audit.on();
		//audit.on();
	
		Number n = new Number( "+=6" );
		audit.debug( "n="+ n.toString() +"("+ n.valueOf() +")" );
		
		audit.debug( "3.0  -> "+ floatToString( 3.0f  ));
		audit.debug( "3.25 -> "+ floatToString( 3.25f ));
		
		Audit.title( "evaluation test:");
		evaluationTest(  "this is not a number",  NOT_A_NUMBER );
		evaluationTest(  "3 plus 2",              "5" );
		evaluationTest(  "3 x    2",              "6" );
		evaluationTest(  "3 squared",             "9" );
		evaluationTest(  "3 squared plus 2",     "11" );
		evaluationTest(  "3 plus 1 all squared", "16" );
		evaluationTest(  "3 times y",             "3" );
		// -- */	
		Audit.title( "get number test:" );
		Audit.incr();
		getNumberTest( "another",                  "+=1" );
		getNumberTest( "another   cup  of coffee", "+=1" );
		getNumberTest( "another 2 cups of coffee", "+=2" );
		getNumberTest( "some coffee",         NOT_A_NUMBER );
		Audit.decr();
		// -- */	
		
		Audit.title( "more/less test:");
		Audit.incr();
		getNumberTest( "about 6 more cups of coffee", "+~6" );
		getNumberTest( "6 more cups of coffee",       "+=6" );
		getNumberTest( "6 less cups of coffee",       "-=6" );
		getNumberTest( "5 more" );
		getNumberTest( "a coffee",                      "1" );
		getNumberTest( "another",                     "+=1" );
		getNumberTest( "another coffee",              "+=1" );
		getNumberTest( "a few 1000 more" );
		getNumberTest( "a couple of 100" );
		getNumberTest( "a couple" );
		getNumberTest( "5 less",                      "-=5" );
		getNumberTest( "another 6",                   "+=6" );
		getNumberTest( "3 times 4   factorial",        "72" );
		getNumberTest( "3 times 4.2 factorial",         "3" );
		getNumberTest( "2 to the power of",             "2" );
		getNumberTest( "2 to the power of 5",          "32" );
		Audit.decr();
		
		Audit.title( "Number combine test:");
		Audit.incr();
		combineTest(   "3",   "6",            "6",   "6" );
		combineTest( "+~3", "-~6", "about 3 less", "-~3" );
		combineTest( "+~3", "+~3", "about 6 more", "+~6" );
		combineTest( "-~3", "+~6", "about 3 more", "+~3" );
		combineTest( "-~3", "-~3", "about 6 less", "-~6" );
		Audit.decr();
		
		Overlay.set( Overlay.get());
		Overlay.attach( "Number" );
		
		/* factorial( n ): n times the factorial of n minus 1.
		 * Mersenne number( n ): 2^n ALL minus 1  -- not a definition???
		 * Mersenne prime(  n ): iff 2^n ALL minus 1 is prime --nad!
		 */
			
		Audit.title( "Function test:");
		Audit.incr();
		// Function within function:
		Function.interpret( "create sum x y / "+ new Attribute( "body", "x + y" ));
		getNumberTest( "2 times the sum of 2 and 3",  "10" );
		
		Function.interpret( "create addition x y / "+ new Attribute( "body", "the sum of x and y" ));
		getNumberTest( "2 times the addition of 2 and 3",  "10" );
		
		Function.interpret( "create product x y / "+ new Attribute( "body", "x times y" ));
		getNumberTest( "2 times the product of 2 and 3",  "12" );
		
		Function.interpret( "create square x / "+ new Attribute( "body", "the product of x and x" ));
		getNumberTest( "2 times the square of 2",  "8" );
		//TODO:
		//getNumberTest( "the square of x",  "the product of x and x" );
		
		Function.interpret( "create factorial n / "+ new Attribute( "body", "n times the factorial of n - 1" ));
		getNumberTest( "2 times the factorial of 4",   "48" );
		Audit.decr();
		
		audit.debug( "PASSED." );
}	}
