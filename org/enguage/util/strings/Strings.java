package org.enguage.util.strings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TreeSet;

import org.enguage.sign.Sign;
import org.enguage.sign.object.Numeric;
import org.enguage.sign.object.Temporal;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.expr.Function;
import org.enguage.sign.object.list.Item;
import org.enguage.sign.object.list.Items;
import org.enguage.sign.object.list.Transitive;
import org.enguage.sign.object.sofa.Entity;
import org.enguage.sign.object.sofa.Link;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Value;
import org.enguage.sign.symbol.Utterance;
import org.enguage.sign.symbol.config.Colloquial;
import org.enguage.sign.symbol.config.Englishisms;
import org.enguage.sign.symbol.config.Plural;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;

public class Strings extends ArrayList<String> implements Comparable<Strings> {
	
	public  static final long serialVersionUID = 0;
	private static Audit audit = new Audit( "Strings" );
	
	public  static final int MAXWORD = 1024;
	
	public static final int     CSV = 0;
	public static final int   SQCSV = 1;
	public static final int   DQCSV = 2;
	public static final int  SPACED = 3;
	public static final int    PATH = 4;
	public static final int   LINES = 5;
	public static final int  CONCAT = 6;
	public static final int ABSPATH = 7;
	public static final int OUTERSP = 8;
	public static final int UNDERSC = 9;
		
	public static final String      lineTerm = "\n";
	public static final String           AND = "&&";
	public static final String            OR = "||";
	public static final String    PLUS_ABOUT = "+~";
	public static final String   MINUS_ABOUT = "-~";
	public static final String   PLUS_EQUALS = "+=";
	public static final String  MINUS_EQUALS = "-=";
	public static final String      ELLIPSIS = "...";
	public static final Strings ellipsis = new Strings( ELLIPSIS, '/' );
	
	public static final char    SINGLE_QUOTE = '\'';
	public static final char    DOUBLE_QUOTE = '"';
	
	private String[] tokens = {
			ELLIPSIS,    AND,  OR,
			PLUS_EQUALS, MINUS_EQUALS,
			PLUS_ABOUT,  MINUS_ABOUT };
	
	public Strings() { super(); }
	
	public Strings( Strings orig ) {
		super();
		Iterator<String> i = orig.iterator();
		while (i.hasNext())
			add( i.next());
	}
	public Strings( String[] sa ) {
		super();
		if (null != sa)
			for (int i=0; i<sa.length; i++)
				add( sa[ i ]);
	}
	
	public Strings( TreeSet<String> sa ) {
		super();
		Iterator<String> i = sa.iterator();
		while (i.hasNext())
			add( i.next());
	}
	public Strings( String buf, char sep ) {
		super();
		if (null != buf) {
			int sz = buf.length();
			if (0 < sz) {
				int cp = 0;
				String word = null;
				while( cp<sz ) {
					word="";
					while( cp<sz && (sep != buf.charAt(cp)))
						word += Character.toString( buf.charAt( cp++ )); // *cp++ = *buf++;
					add( new String( word ));
					if ( cp<sz && sep == buf.charAt(cp) ) { // not finished
						cp++;         // avoid separator
						if (cp>=sz) // now finished!
							add( new String( "" )); // add trailing blank string!
	}	}	}	}	}
	private static boolean tokenMatch( String token, String buf, int i, int sz ) {
		int tsz = token.length();
		return (i+tsz <= sz) && token.equals( buf.substring( i, i+tsz ));
	}
	public Strings( String s ) {
		super();
		if (s != null && !s.equals( "" )) { // NB this doesn't tie up with parsing in Attributes.c!!!!
			char[] buffer = s.toCharArray();
			int  i = 0, sz = buffer.length;
			while (i<sz) {
				if (Character.isWhitespace( buffer[ i ]))
					i++;
				else {
					StringBuilder word = new StringBuilder( MAXWORD );
					if (Character.isLetter( buffer[ i ])
						|| (   ('_' == buffer[ i ] || '$' == buffer[ i ])
							&& 1+i<sz && Character.isLetter( buffer[ 1+i ])))
					{	word.append( buffer[ i++ ]);
						while (i<sz && (
							Character.isLetter( buffer[ i ])
							|| Character.isDigit(  buffer[ i ])
							||	(( '-'  == buffer[ i ]
								||	SINGLE_QUOTE == buffer[ i ]
								||	'_'  == buffer[ i ]
								||  '.'  == buffer[ i ])
									&& 1+i < sz && 
									(Character.isLetter( buffer[ 1+i ])
									|| Character.isDigit( buffer[ 1+i ]))
								)
							))
							word.append( buffer[ i++ ]);
						
					} else if (Character.isDigit( buffer[ i ])
							 ||	(	i+1<sz
								 && Character.isDigit( buffer[ 1+i ])
								 && (	buffer[ i ] =='-'   // -ve numbers
								 	 || buffer[ i ] =='+')) // +ve numbers
							)
					{	word.append( buffer[ i++ ]);
						boolean pointDone = false;
						while (i<sz
								&& (Character.isDigit( buffer[ i ])
									|| (  !pointDone && buffer[ i ] =='.'
								        && i+1<sz
								        && Character.isDigit( buffer[ 1+i ]))
							  )    )
						{
							if (buffer[ i ] == '.') {
								pointDone = true;
								word.append( buffer[ i++ ]); // point,
								word.append( buffer[ i++ ]); // first decimal
							} else
								word.append( buffer[ i++ ]);
						}
						
					} else if (SINGLE_QUOTE == buffer[ i ] ) {
						// first check for stand-alone apostrophe e.g. ENT''s
						if (i+1<sz && buffer[ i+1 ] == SINGLE_QUOTE) {
							i+=2;
							append( word.toString() );
							word = new StringBuilder( MAXWORD );
							word.append( "'" );
						} else {
							// embedded apostrophes: check "def'def", " 'def" or "...def'[ ,.?!]" 
							// quoted string with embedded apostrophes 'no don't'
							word.append( buffer[ i++ ]);
							while( i<sz &&
							      !(SINGLE_QUOTE == buffer[ i ] && // ' followed by WS OR embedded
							        (1+i==sz || //Character.isWhitespace( buffer[ 1+i ]))
							        		(   !Character.isLetter( buffer[ i+1 ])
											 && !Character.isDigit(  buffer[ i+1 ])))
							     ) ) 
								word.append( buffer[ i++ ]);
							word.append( "'" );
							i++;
						}
						
					} else if (DOUBLE_QUOTE == buffer[ i ]) {
						//audit.audit("DQ string")
						word.append( buffer[ i++ ]);
						while( i<sz && DOUBLE_QUOTE != buffer[ i ])
							word.append( buffer[ i++ ]);
						word.append( DOUBLE_QUOTE ); // always terminate string
						i++;
						
					} else {
						boolean found = false;
						//audit.audit("TOKEN")
						for (int ti=0; ti<tokens.length && !found; ti++)
							if (tokenMatch( tokens[ ti ],  s,  i,  sz )) {
								found=true;
								word.append( tokens[ ti ]);
								i += tokens[ ti ].length();
							}
						if (!found)
							word.append( buffer[ i++ ]);

					}
					String tmp = word.toString();
					if (!tmp.equals( "" )) {
						add( tmp );
						word = new StringBuilder( MAXWORD );
					}
		}	}	}
	}
	public static Strings getStrings( String s ) {
		Strings ss = new Strings();
		if (s != null && !s.equals( "" )) { // NB this doesn't tie up with parsing in Attributes.c!!!!
			char[] buffer = s.toCharArray();
			int  i = 0, sz = buffer.length;
			while (i<sz) {
				while (i < sz && Character.isWhitespace( buffer[ i ])) i++;
				StringBuilder word = new StringBuilder( MAXWORD );
				while (i < sz && !Character.isWhitespace( buffer[ i ]))
					word.append( buffer[ i++ ]);
				if (!word.toString().equals( "" )) ss.add( word.toString() );
		}	}
		return ss;
	}
	public String toString( String fore, String mid, String aft ) {
		StringBuilder as = new StringBuilder();
		int i = 0;
		as.append( fore );
		for (String s : this)
			as.append((i++ == 0 ? "" : mid) + s);
		as.append( aft );
		return as.toString();
	}
	public String toString( int n ) {
		return
			( n == OUTERSP ) ? toString(  " ",      " ",  " " ) :
			( n ==  SPACED ) ? toString(   "",      " ",   "" ) :
			( n ==  CONCAT ) ? toString(   "",       "",   "" ) :
			( n ==   DQCSV ) ? toString( "\"", "\", \"", "\"" ) :
			( n ==   SQCSV ) ? toString(  "'",   "', '",  "'" ) :
			( n ==     CSV ) ? toString(   "",      ",",   "" ) :
			( n ==    PATH ) ? toString(   "",      "/",   "" ) :
			( n ==   LINES ) ? toString(   "",     "\n",   "" ) :
			( n == ABSPATH ) ? toString(   "/",     "/",   "" ) :
			( n == UNDERSC ) ? toString(   "",      "_",   "" ) :
			"Strings.toString( "+ toString( CSV ) +", n="+ n +"? )";
	}
	@Override
	public String toString() { return toString( SPACED ); }
	public String toString( Strings seps ) {
		if (size() == 0)
			return "";
		else if (null == seps)
			return toString( SPACED );
		else if (seps.size() == 1)
			return toString( "", seps.get( 0 ), "" );
		else if (seps.size() == 2) { // oxford comma: ", ", ", and "
			StringBuilder rc = new StringBuilder();
			ListIterator<String> li = listIterator();
			if (li.hasNext()) {
				rc.append( li.next() );
				String first = seps.get( 0 ),
				       last = seps.get( 1 );
				while (li.hasNext()) {
					String tmp = li.next();
					rc.append((li.hasNext() ? first : last) + tmp);
			}  }
			return rc.toString();
		} else if (seps.size() == 4) {
			Strings tmp = new Strings();
			tmp.add( seps.get( 1 ));
			tmp.add( seps.get( 2 ));
			return seps.get( 0 ) + toString( tmp ) + seps.get( 3 );
		} else 
			return toString( seps.get( 0 ), seps.get( 1 ), seps.get( 2 ));
	} // don't use traceOutStrings here -- it calls Strings.toString()!
	// --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- --- 
	public static boolean isAlphabetic( String s ) {
		int sz = s.length();
		for (int i=0; i<sz; i++) {
			char ch = s.charAt( i ); 
			if (   Character.getType( ch ) != Character.LOWERCASE_LETTER
				&& Character.getType( ch ) != Character.UPPERCASE_LETTER 
				&& ch != '_')
				return false;
		}
		return true;
	}
	public static boolean isNumeric( String s ) {
		try {
			return !Float.isNaN( Float.parseFloat( s ));
		} catch (NumberFormatException nfe) {
			return false;
	}	}
	public static Float valueOf( String s ) {
		try {
			return Float.parseFloat( s );
		} catch (NumberFormatException nfe) {
			// this will return Float.NaN is not successful 
			return org.enguage.sign.symbol.number.Number.value( s );
	}	}

	public int peekwals( ListIterator<String> si ) {
		boolean rc = true;
		ListIterator<String> sai = listIterator();
		int i = si.nextIndex();
		while (rc && sai.hasNext() && si.hasNext())
			if (!sai.next().equals( si.next()))
				rc = false;
		// if not put si back!
		while (si.nextIndex() > i) si.previous();
		return rc && !sai.hasNext() ? size() : 0; // we haven't failed AND got to end of strings
	}
	public static String getString( ListIterator<String> si, int n ) {
		Strings sa = new Strings();
		for (int i=0; i<n; i++)
			if (si.hasNext())
				sa.add( si.next());
		return sa.toString( Strings.SPACED );
	}
	public Strings getUntil( String term ) {
		Strings from = new Strings();
		String tmp;
		while (!(tmp = remove( 0 )).equals( term ))
			from.add( tmp );
		return from;
	}
	public Strings filter() {
		// remove any [ superfluous ] stuff
		Strings filtered = new Strings();
		ListIterator<String> li = listIterator();
		while (li.hasNext()) {
			String item=li.next();
			if (item.equals( "[" )) // skip to closing ]
				while (li.hasNext() && !item.equals( "]" ))
					item=li.next();
			else
				filtered.add( item );
		}
		return filtered;
	}
	public Strings removeAll( String val ) {
		Iterator<String> ai = iterator();
		while (ai.hasNext())
			if (ai.next().equals( val ))
				ai.remove();
		return this;
	}
	public boolean containsMatched( Strings inner ) {
		boolean rc = false;
		if (size() == 0 && 0 == inner.size())
			return true;
		else if (size() >= inner.size())
			// this loop goes thru outer in a chunk size of inner
			for (int o=0; rc == false && o<=size()-inner.size(); o++) {
				// see if the inner chunk matches from posn o
				rc = true; // lets assume it does
				for (int i=0; rc == true && i<inner.size(); i++)
					if (!get( o + i ).equals( inner.get( i ))) // if one doesn't match
						rc = false;
			}
		return rc;
	}
	// ...OR: -------------------------------------------
	// a=[ "One Two Three", "Aye Bee Cee", "Alpha Beta" ], val= "Bee" => b = [ "One Two Three", "Alpha Beta" ];
	public Strings removeAllMatched( String val ) {
		Strings b = new Strings();
		Strings valItems = new Strings( val );
		for (String ai : this) 
			if (!new Strings( ai ).containsMatched( valItems ))
				b.add( ai );
		return b;
	}
	// ---------------------------------------------
	public Strings removeFirst( String val ) {
		Iterator<String> si = iterator();
		while (si.hasNext())
			if (si.next().equals( val )) {
				si.remove();
				break;
			}
		return this;
	}
	public Strings remove( int i, int n ) {
		Strings strs = new Strings();
		for (int j=0; j<n; j++ ) 
			strs.add( remove( i ));
		return strs;
	}
	public Strings linuxSwitches() {
		// [..., "-", "d", ...] =>[..., "-d", ...]
		boolean doNext = false;
		ListIterator<String> si = listIterator();
		while (si.hasNext()) {
			String tmp;
			if ((tmp = si.next()).equals( "--" ))
				break;
			else if (tmp.equals( "-" )) {
				si.remove();
				doNext = true;
			} else if (doNext) {
				si.set( "-"+tmp );
				doNext = false;
		}	}
		return this;
	}
	public Strings contract( String item ) {
		// from: [ ..., "name",   "=",   "'value'", ... ]
		//   to: [ ...,       "name='value'",       ... ]
		int sz=size()-1;
		for( int i=1; i<sz; i++ )
			if (get( i ).equals( item )) {
				// from: [ ..., i-1:'name',   i:'=',   i+1:'value', ... ]
				//   to: [ ..., i-1:'name="value"', ... ]
				set( i-1, get(i-1) + item + remove(i+1) );
				remove(i); // remove this second!
				sz -= 2;
				i--; // move back to i-1: dir / dir /dir / file
			}
		return this;
	}
	public Strings replace( int i, String s ) {
		if (null != s) set( i, s );
		return this;
	}
	public Strings replaceIgnoreCase( String s1, String s2 ) {
		int i=0;
		for (String s : this) {
			if (s.equalsIgnoreCase( s1 ))
				set( i, s2 );
			i++;
		}
		return this;
	}
	public Strings replace( String s1, String s2 ) {
		int i=0;
		for (String s : this) {
			if (s.equals( s1 ))
				set( i, s2 );
			i++;
		}
		return this;
	}
	public Strings appendAll( Strings sa ) {
		if (null != sa)
			for( String s : sa )
				add( s );
		return this;
	}
	public Strings append( String s ) {
		if (null != s && !s.equals( "" ))
			add( s );
		return this;
	}
	public void append( ListIterator<String> si, int n ) {
		for (int j=0; j<n; j++)
			if (si.hasNext())
				append( si.next() );
	}
	public Strings prepend( String str ) {
		if (null != str && !str.equals( "" ))
			add( 0, str );
		return this;
	}
	public Strings copyFrom( int n ) {
		Strings b = new Strings();
		for (int i=n, sz = size(); i<sz; i++)
			b.add( get( i ));
		return b;
	}
	public Strings copyAfter( int n ) {
		Strings b = new Strings();
		for (int i=n+1, sz = size(); i<sz; i++)
			b.add( get( i ));
		return b;
	}
	public Strings copyBefore( String word ) {
		Strings b = new Strings();
		for (String a : this)
			if (a.equals( word ))
				break;
			else
				b.add( a );
		return b;
	}
	public Strings copyAfter( String word ) {
		Strings b = new Strings();
		boolean found = false;
		for (String a : this)
			if (found)
				b.add( a );
			else if (a.equals( word ))
				found = true;
		return b;
	}
	public Strings copyFromUntil( int n, String until ) {
		Strings b = new Strings();
		for (int i=n, sz = size(); i<sz; i++) {
			String item = get( i );
			if (item.equals( until ))
				break;
			else
				b.add( item );
		}
		return b;
	}
	public static Strings copyUntil( ListIterator<String> si, String until ) {
		// until is separator, it is consumed
		String tmp;
		Strings sa = new Strings();
		while (si.hasNext() &&
		       !(tmp = si.next()).equals( until ))
			sa.append( tmp );
		return sa;
	}
	public static Strings fromNonWS( String buf ) {
		Strings a = new Strings();
		if (buf != null) {
			char ch;
			for (int i=0, sz=buf.length(); i<sz; i++ ) {
				StringBuffer word = new StringBuffer();
				while( i<sz &&  Character.isWhitespace( buf.charAt( i++ )));
				while( i<sz && !Character.isWhitespace( ch = buf.charAt( i++ )))
					word.append( ch );
				a.add( word.toString());
		}	}
		return a;
	}
	public String camelise( Strings strs ) {
		StringBuilder rc = new StringBuilder();
		for( String s : strs )
			rc.append( Character.toUpperCase( s.charAt( 0 )) +  s.substring( 1 ));
		return rc.toString();
	}
	public Strings decamelise( String s ) {
		Strings strs = new Strings();
		String tmp = "";
		char ch;
		for (int i=0; i < s.length(); i++) {
			ch = s.charAt( i );
			if (Character.isUpperCase( ch )) {
				if (!tmp.equals( "" )) strs.add( tmp );
				tmp = "" + Character.toLowerCase( ch );
			} else
				tmp += ch;
		}
		if (!tmp.equals( "" )) strs.append( tmp );
		return strs;
	}
	public Strings reverse() {
		Strings b = new Strings();
		for (int sz=size(), i=sz-1; i>=0; i--)
			b.add( get( i ));
		return b;
	}
	//      [ "hello", "martin", "!" ].replace([ "martin" ], [ "to", "you" ]) => [ "hello", "to", "you", "!" ]
	// err: [ "hello", "martin", "!" ].replace([ "martin" ], [ "to", "martin" ]) => [ "hello", "martin", "!" ]
	public Strings replace( Strings b, Strings c ) {
		//audit.traceIn("replace", b.toString() +" with "+ c.toString() +" in "+ toString());
		int len = size(), blen = b.size(), clen = c.size();
		for (int i=0; i <= len - blen; i++) {
			boolean found = true;
			int j=0;
			for (j=0; j<blen && found; j++)
				if (!get( i+j ).equalsIgnoreCase( b.get( j ))) found=false;
			if (found) {
				for (j=0; j<blen; j++) remove( i );
				for (j=0; j<clen; j++) add( i+j, c.get( j ));
				i += clen;    // advance counter over replaced Strings
				len = size(); // ...reset len since we've messed with a	
		}	}
		//audit.traceOut( toString());
		return this;
	}
	public Strings replace( Strings a, String b ) { return replace( a, new Strings( b ));}
	public boolean contains( Strings a ) {
		
		boolean found = false;
		int n = 1 + size() - a.size();
		ListIterator<String> ti = this.listIterator();
		while (n-- > 0 && !found) {
			
			int i=0;
			found = true;
			Iterator<String> ai = a.iterator();
			while (ai.hasNext() && found) {
				i++;
				found = ti.next().equalsIgnoreCase( ai.next());
			}
			
			if (!found) while (--i>0) ti.previous();
		}
		return found;
	}

	public static void removes( ListIterator<String> si, int n ) {
		while (n-->0) si.remove();
	}
	public static void previous( ListIterator<String> si, int n ) {
		while (n-->0) si.previous();
	}
	public static void next( ListIterator<String> si, int n ) {
		while (n-->0) si.next();
	}
	
	// count the number of matching strings - due for Strings class!!!
	public int matches( ListIterator<String> li ) {
		int n = 0;
		ListIterator<String> pi = listIterator();
		while (li.hasNext() && pi.hasNext()) {
			if (li.next().equals( pi.next() ))
				n++;
			else
				break;
		}
		while (n-- > 0) li.previous();

		return pi.hasNext() ? 0 : n;
	}
	
	// deals with matched and unmatched values:
	// [ "a", "$matched", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "a",  "MATCHED", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "a", "$unmatch", ".",  "b" ] => [ "a", "_USER", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
	// [ "we are holding hands", "."  ] => [ "we", "are", "holding", "hands", "." ] -- jik - just in case!
	// matches are from tags, *ap contains mixed case - any UPPERCASE items should match matches OR envvars.
	// [ 'some', 'bread', '+', 'fish'n'chips', '+', 'some', 'milk' ], "+"
	//                                => [  'some bread', 'fish and chips', 'some milk' ]
	private Strings normalise( String ipSep, String opSep ) {
		// remember, if sep='+', 'fish', '+', 'chips' => 'fish+chips' (i.e. NOT 'fish + chips')
		// here: some coffee + fish + chips => some coffee + fish and chips
		Strings values = new Strings();
		if (size() > 0) {
			int i = 0;
			String value = get( 0 ); //
			String localSep = opSep; // ""; // only use op sep on appending subsequent strings
			while (++i < size()) {
				String tmp = get( i );
				if (tmp.equals( ipSep )) {
					values.add( value );
					value = "";
					localSep = "";
				} else {
					value += ( localSep + tmp );
					localSep = opSep;
			}	}
			values.add( value );
		}
		return values;
	}
	/*
	 * normalise with a parameter uses that param as a user defined separator, rather than whitespace
	 * normalise([ "one", "two", "+", "three four" ], "+") => [ "one two", "three four" ]
	 */
	public Strings normalise( String sep ) { return normalise( sep, " " ); }
	// normalise([ "one", "two three" ]) => [ "one", "two", "three" ]
	public Strings normalise() {
		Strings a = new Strings();
		for (String s1 : this)
			for (String s2 : new Strings( s1 ))
				a.add( s2 );
		return a;
	}
	// TODO: expand input, and apply each thought...
	// I need to go to the gym and the jewellers =>
	// (I need to go to the gym and I need to go to the jewellers =>)
	// I need to go to the gym. I need to go to the jewellers.
	/* [ [ "to", "go", "to", "the", "gym" ], [ "the", "jewellers" ] ] 
	 * => [ [ "to", "go", "to", "the", "gym" ], [ "to", "go", "to", "the", "jewellers" ] ]
	 */
	// [ "THIS", "is", "Martin" ] => [ "THIS", "is", "martin" ]
	public Strings decap() {
		int i = -1;
		//remove all capitalisation... we can re-capitalise on output.
		while (size() > ++i) {
			String tmp = get( i );
			if (isCapitalised( tmp ))
				set( i, Character.toLowerCase( tmp.charAt( 0 )) + tmp.substring( 1 ));
		}
		return this;
	}

	public Strings capitalise() {
		int n=0;
		for(String s : this)
			set( n++, Character.toUpperCase(s.charAt(0)) + s.substring(1));
		return this;
	}
	private static boolean isCapitalised( String str ) {
		if (null != str) {
			int len = str.length();
			if (len > 1 && Character.isUpperCase( str.charAt( 0 ))) {
				int i = 0;
				while (len > ++i && Character.isLowerCase( str.charAt( i )))
					;
				return str.length() == i; // capitalised if we're at the end of the string
		}	}
		return false;
	}
	public static boolean isUpperCase( String a ) {
		for (int i=0, sz=a.length(); i<sz; i++)
			if (!Character.isUpperCase( a.charAt( i )) )
				return false;
		return true;
	}
	public boolean areLowerCase() {
		for (String a : this )
			if (isUpperCase( a ))
				return false;
		return true;
	}
	public static boolean isUCwHyphUs( String a ) {
		int len=a.length();
		if (len >= 2 && a.charAt( len-2 ) == Englishisms.APOSTROPHE_CH)
			return a.endsWith( Englishisms.apostrophed() );
		for (int i=0; i<len; i++) {
			char ch = a.charAt( i );
			// TODO: l'eau
			if (!Character.isUpperCase( ch ) && ch != '-' && ch !='_' )
				return false;
		}
		return true;
	}
	public Strings before( String word ) {
		Strings before = new Strings();
		for (String s : this)
			if (s.equals( word ))
				break;
			else
				before.add( s );
		return before;
	}
	public Strings after( String word ) {
		Strings after = new Strings();
		boolean found = false;
		for (String s : this)
			if (found)
				after.add( s );
			else if (s.equals( word ))
				found = true;
		return after;
	}
	public Strings trimAll( char ch ) {
		int i=0;
		for( String s : this )
			set( i++, trim( s, ch ));
		return this;
	}
	public static String trim( String a, char ch ) {return triml( a, a.length(), ch );}
	public static String triml( String a, int asz, char ch ) {
		// (a="\"hello\"", ch='"') => "hello"; ( "ohio", 'o' ) => "hi"
		if (asz > 1) {
			char ch0 = a.charAt( 0 );
			if (asz == 2 && ch0 == ch && a.charAt( 1 ) == ch)
				return "";
			else if (asz > 2 && ch0 == ch && a.charAt( asz-1 ) == ch)
				return a.substring( 1, asz-1 );
		}
		return a;
	}
	public static boolean isQuoted(String s) {
		if (null==s||s.length()==0) return false;
		char ch = s.charAt(0);
		return ch == '"' || ch == '\''; // assume ending with same char
	}
	public static String stripQuotes( String s ) {
		int sz = s.length();
		if (sz>1) {
			char ch = s.charAt( 0 );
			     if (ch == SINGLE_QUOTE) s = Strings.triml( s, sz, SINGLE_QUOTE );
			else if (ch == DOUBLE_QUOTE) s = Strings.triml( s, sz, DOUBLE_QUOTE );
		}
		return s; 
	}
	public static String stripAttrQuotes( String str ) {
		char quoteCh = str.charAt( 0 );
		if (quoteCh == str.charAt( str.length() - 1) &&
			(	quoteCh == Attribute.ALT_QUOTE_CH
			 ||	quoteCh == Attribute.DEF_QUOTE_CH   
			)	)
			str = Strings.trim( str, quoteCh );
		return str;
	}
	public Strings stripBrackets( String open, String close ) {
		boolean deleting = false;
		ListIterator<String> ri = this.listIterator();
		while (ri.hasNext()) {
			String s = ri.next();
			if (s.equals( open )) {
				ri.remove();
				deleting = true;
			} else if (s.equals( close )) {
				ri.remove();
				deleting = false;
			} else if (deleting)
				ri.remove();
		}
		return this;
	}
	public  void toUpperCase() {
		ListIterator<String> li = this.listIterator();
		while (li.hasNext())
			li.set( li.next().toUpperCase());
	}
	public Strings strip( String from, String to ) {
		// this {one} and {two} is => one two
		boolean adding = false;
		Strings rc = new Strings();
		for (String s : this) {
			if (s.equals( from ))
				adding = true;
			else if (s.equals( to ))
				adding = false;
			else if (adding)
				rc.add( s );
		}
		return rc;
	}
	public Strings reinsert( Attributes as, String from, String to ) {
		// "{ONE} and {TWO}".reinsert( as=[one="martin", two="ruth"], "{", "}" ) => martin and ruth
		int i = 0;
		boolean adding = true;
		Strings rc = new Strings();
		for (String s : this) {
			if (s.equals( from ))
				adding = false;
			else if (s.equals( to )) {
				adding = true;
				rc.add( as.get( i++ ).value());
			} else if (adding)
				rc.add( s );
		}
		return rc;
	}

	
	// ---------------------------------------------------------
	// ---------------------------------------------------------
	/* 
	 * combine and divide --
	 * if a single separator, don't need to store that separator, combine just adds it
	 * if a combination of separators, we need to remember which one it is so it can be added!
	 * 
	 */
	// backwards compatibility -- include terminators
	public ArrayList<Strings> divide( Strings separators ) {return divide( separators, false );}
	public ArrayList<Strings> divide( Strings terminators, boolean inclusive ) {
		// [ "o", "t", ".", "t", "?", "f", "f" ]( ".?!" ) => [["o", "t", "."], ["t", "?"], ["f", "f"]]
		ArrayList<Strings> divisions = new ArrayList<Strings>();
		Strings division = new Strings();
		for (String s : this) {
			if (inclusive || !terminators.contains( s )) division.add( s );
			if (terminators.contains( s )) {
				divisions.add( division );
				division = new Strings();
		}	}
		divisions.add( division );
		return divisions;
	}
	public ArrayList<Strings> divide( String terminator, boolean inclusive ) {
		// [ "o", "t", ".", "t", "?", "f", "f" ]( ".?!" ) => [["o", "t", "."], ["t", "?"], ["f", "f"]]
		ArrayList<Strings> divisions = new ArrayList<Strings>();
		Strings division = new Strings();
		for (String s : this) {
			if (inclusive || !terminator.equals( s )) division.add( s );
			if (terminator.equals( s )) {
				divisions.add( division );
				division = new Strings();
		}	}
		divisions.add( division );
		return divisions;
	}
	public static Strings combine( ArrayList<Strings> as ) {
		// [["o", "t". "."], ["t", "?"], ["f", "f"]] => [ "o", "t", ".", "t", "?", "f", "f" ]
		Strings sa = new Strings();
		for (Strings tmp : as)
			sa.addAll( tmp );
		return sa;
	}
	// ---------------------------------------------------------
	public ArrayList<Strings> divide( String sep ) {
		// [ "o", "t", "&", "t", "?", "&", "f", "f" ]( "&" ) => [["o", "t", "."], ["t", "?"], ["f", "f"]]
		ArrayList<Strings> divisions = new ArrayList<Strings>();
		Strings division = new Strings();
		for (String s : this) {
			if (sep.equals( s )) {
				divisions.add( division );
				division = new Strings();
			} else {
				division.add( s );
		}	}
		divisions.add( division );
		return divisions;
	}
	static Strings combine( ArrayList<Strings> as, String sep ) {
		// [["o", "t"], ["t", "?"], ["f", "f"]] => [ "o", "t", "&", "t", "?", "&", "f", "f" ]
		Strings sa = new Strings();
		boolean first = true;
		for (Strings tmp : as) {
			if (first)
				first = false;
			else
				sa.add( sep );
			sa.addAll( tmp );
		}
		return sa;
	}
	
	public int compareTo( Strings sa ) {
		/* This compareTo() will put the longer strings first so:
		 * "user", "does", "not"  matches before  "user", "does"
		 */
		int rc = 0;
		Iterator<String> i = iterator(),
		               sai = sa.iterator();
		while (rc==0 && i.hasNext() && sai.hasNext())
			rc = sai.next().compareTo( i.next() );
		
		if (rc==0 && (i.hasNext() || sai.hasNext()))
			rc = i.hasNext() ? -1 : 1 ;
			
		return rc;
	}
	@Override 
	public boolean equals( Object sa ) {		
		if (sa instanceof Strings) {
			Iterator<String>   i = iterator();
			Iterator<String> sai = ((Strings)sa).iterator();
			
			while (i.hasNext() && sai.hasNext())
				if (!sai.next().equalsIgnoreCase( i.next() ))
					return false;
			
			return !i.hasNext() && !sai.hasNext();
		}
		return false;
	}
	public boolean begins( Strings sa ) {
		Iterator<String> i = iterator(),
		               sai = sa.iterator();
		while (i.hasNext() && sai.hasNext())
			if (!sai.next().equalsIgnoreCase( i.next() ))
				return false;
		return !sai.hasNext();
	}

	public static boolean doString( String val, ListIterator<String> si ) {
		if (si.hasNext()) {
			if (si.next().equalsIgnoreCase( val ))
				return true;
			si.previous();
		}
		return false;
	}

	public static ListIterator<String> resetList( Strings sa, int start, ListIterator<String> si) {
		si = sa.listIterator();
		while (si.hasNext() && si.nextIndex() != start) si.next();
		return si;
	}
	public Strings derefVariables() {
		Strings actuals = new Strings();
		for (String a : this )  //  why isNumeric + getVar = a if not found???
			actuals.add( isNumeric( a ) ? a:Variable.get( a ));
		return actuals;
	}
	// -- static Algorithm helpers here...
	public Strings substitute( Strings formals, Strings actuals ) {
		audit.in( toString()+".substitute",
				      "["+ formals.toString( Strings.DQCSV )
				+"] => ["+ actuals.toString( Strings.DQCSV ) +"]" );
		if (actuals.size() == formals.size()) {
			int i = 0;
			ListIterator<String> bi = listIterator();
			while (bi.hasNext()) {
				int index;
				String token = bi.next();
				if (-1   != (index = formals.indexOf( token )) &&
				    null != (token = actuals.get(     index ))    )
					set( i, token );
				i++;
			}
		} else {
			audit.out( "null" );
			return null;
		}
		return audit.out( this );
	}
	public static String peek( ListIterator<String> li ) {
		String s = "";
		if (li.hasNext()) {
			s = li.next();
			li.previous();
		}
		return s;
	}
	public static void unload( ListIterator<String> li, Strings sa ) {
		// this assumes all things got have been added to sa
		int sz=sa.size();
		while (0 != sz--) {
			sa.remove( 0 );
			li.previous();
	}	}
	public static boolean getWord( ListIterator<String> si, String word, Strings rep ) {
		//audit.in( "getWord", peek( si )+", word="+word )
		if (si.hasNext()) {
			if (si.next().equals( word )) {
				audit.debug( "found: + word ");
				rep.add( word );
				// audit.out( true )
				return true;
			} else {
				si.previous();
		}	}
		//audit.out( false )
		return false;
	}
	public static String getName( ListIterator<String> si, Strings rep ) {
		String s = si.hasNext() ? si.next() : null;
		if (s != null)
			rep.add( s );
		else
			unload( si, rep );
		return s;
	}
	public static String getLetter( ListIterator<String> si, Strings rep ) {
		String s = si.hasNext() ? si.next() : null;
		if (s != null)
			rep.add( s );
		else
			unload( si, rep );
		return s;
	}
	public static Strings getWords( ListIterator<String> li, String term, Strings rep ) {
		return getWords( li, 99, term, rep );
	}
	public static Strings getWords( ListIterator<String> li, int sanity, String term, Strings rep ) {
		Strings sa = new Strings();
		String  s  = "";
		
		while (--sanity>=0
				&& li.hasNext()
				&& !(s=li.next()).equals( term ))
			sa.add( s );
		
		if (sanity < 0 || !s.equals( term )) {
			unload( li, rep );
			sa = null;
		} else {
			rep.addAll( sa );
			rep.add( term );
		}
		return sa;
	}
	public Strings toLowerCase() {
		Strings lc = new Strings();
		for( String s : this )
			lc.add( s.toLowerCase( Locale.getDefault()));
		return lc;
	}
	public static String toUnderscored( String in ) {
		return new Strings( in ).toString( Strings.UNDERSC );
	}
	public static String fromUnderscored( String in ) {
		if (in.charAt( 0 ) == '"') return in;
		Strings ss = new Strings( in, '_' );
		return ss.toString();
	}
	public static boolean isUnderscored( String in ) {
		return in.contains( "_" );
	}
	public Strings extract( ListIterator<String> ui ) {
		ListIterator<String> loci = listIterator();
		Strings rc = new Strings();
		String tmp;
		while (ui.hasNext() && loci.hasNext())
			if ((tmp = ui.next()).equals( loci.next() ))
				rc.add( tmp );
			else { // not matched...
				ui.previous(); // ...put this one back!
				break;
			}
		if (loci.hasNext()) { // we've failed!
			int n = rc.size();
			while (n-- > 0) ui.previous(); //previous( ui, rc.size());
			rc = new Strings();
		}
		return rc;
	}
	public Strings divvy( String sep ) {
		// ["a", "b", "and", "c"].split( "and" ) => [ ["a", "b"], ["c"] ]
		Strings output = new Strings(),
				tmp    = new Strings();
		for (String s : this)
			if (s.equals( sep )) {
				if (!tmp.isEmpty()) output.add( tmp.toString());
				tmp = new Strings();
			} else 
				tmp.add( s );
		if (!tmp.isEmpty()) output.add( tmp.toString());
		return output;
	}
	public static Strings markedUppercaser( String marker, Strings vars, Strings implication ) {
		// takes a marker and replaces that following with upper case...
		// takes a marker and replaces   FOLLOWING    with upper case...
		Strings uc = new Strings();
		
		for (int i=0; i<implication.size(); i++)
			if (i<implication.size()-1 &&
					implication.get( i ).equals( marker ) &&
					vars.contains( implication.get( i + 1 )))
				
				// increment counter to skip marker
				uc.append( implication.get( ++i ).toUpperCase());
		
			else
				uc.add( implication.get( i ));
		
		return uc;
	}

	public static long lash( String s ) {
		final char upper = 'z', lower = 'a';
		long lhsh  = 0;
		char ch;
		int rng = upper - lower + 1,
		    len = s.length();
		for (int i=0; i<len; i++)
			if ((ch = s.charAt( i ))>=lower && ch<=upper)
				lhsh = lhsh*rng + ch - lower + 1;
		return lhsh;
	}
	public static int hash( String s ) {
		//final int MAXINT = 2147483647
		final char upper = 'z', lower = 'a';
		int ihsh  = 0;
		char ch;
		int rng = upper - lower + 1;
		int len = s.length();
		for (int i=0; i<len && i<6; i++)
			if ((ch = Character.toLowerCase( s.charAt( i )))>=lower &&
			     ch                                         <=upper    )
				ihsh = ihsh*rng + ch - lower + 1;
		return ihsh;
	}
	// -- static Algorithm helpers ABOVE
	// ---------------------------------------------------------
	
	public static void main( String args[]) {
		Audit.on(); //main()
		
//		Audit.traceAll( true );
//		new Strings( "a + b" ).substitute( new Strings("a b"), new Strings( "1 2"));
//		System.exit( 0 );
		


		
		Strings a = new Strings( "hello there" ),
				b = new Strings( "hello world" ),
		        c = new Strings( "hello there martin" );
		
		audit.debug( "comparing "+ a +" to "+ b +" = "+ (a.compareTo( b ) > 0 ? "pass" : "fail" ));
		audit.debug( "comparing "+ a +" to "+ c +" = "+ (a.compareTo( c ) > 0 ? "pass" : "fail" ));
		
		b = new Strings( c );
		b.remove( 2 );
		audit.debug( "remove from a copy: b is "+ b.toString( Strings.SPACED ) +", c is "+ c.toString( Strings.SPACED ) );
		
		
		
		audit.debug( "a: ["+ new Strings( "martin''s" ).toString( DQCSV ) +"]" );
		audit.debug( "b: ["+ new Strings( "failure won't 'do' 'do n't'" ).toString( DQCSV ) +"]" );
		audit.debug( "c: "+ new Strings( "..........." ));
		audit.debug( "d: "+ new Strings( "+2.0" ));
		audit.debug( "e: "+ new Strings( "quantity+=2.0" ));
		
		a = new Strings("hello failure");
		b = new Strings( "failure" );
		c = new Strings( "world" );
		audit.debug( "e: ["+ a.replace( b, c ).toString( "'", "', '", "'" ) +"]" );
		String tmp = "+=6";
		audit.debug( "tmp: "+ tmp.substring( 0, 1 ) + tmp.substring( 2 ));
	
		audit.debug("tma:"+(tokenMatch( ELLIPSIS, ELLIPSIS, 0, ELLIPSIS.length() )?"true":"false")+"=>true");
		audit.debug("tma:"+(tokenMatch( ELLIPSIS, ELLIPSIS, 1, ELLIPSIS.length() )?"true":"false")+"=>false");
		audit.debug("tma:"+(tokenMatch( ELLIPSIS,     "..", 0,     "..".length() )?"true":"false")+"=>false");
		
		a = new Strings( "this is a test sentence. And half a" );
		ArrayList<Strings> as = a.divide( Terminator.terminators() );
		// as should be of length 2...
		b = as.remove( 0 );
		audit.debug( "b is '"+ b.toString() +"'. as is len "+ as.size() );
		a = Strings.combine( as ); // needs blank last item to add terminating "."
		audit.debug( "a is '"+ a.toString() +"'. a is len "+ a.size() );
		a.addAll( b );
		audit.debug( "a is now '"+ a.toString() +"'." );
		
		audit.debug( "begins:"+ ( new Strings("to be or not").begins(new Strings("to be"))? "pass":"fail" ));
		audit.debug( "begins:"+ ( new Strings("to be or not").begins(new Strings("to be"))? "pass":"fail" ));
		audit.debug( "begins:"+ ( new Strings("to be").begins(new Strings("to be or"))? "fail":"pass" ));
		
		a = new Strings( "17:45:30:90" );
		audit.debug( "the time is "+ a.toString( SPACED ));

		a = new Strings( "this is a test" );
		Strings seps = new Strings( ", / and ", '/' );
		audit.debug( a.toString( seps ) );
// */
		/* /
		String s = "this test should pass";
		Strings sa1 = new Strings( s, ' ' );
		//Strings sa2 = new Strings( s, " " );
		//String[] sa3 = Strings.fromLines( "this\ntest\nshould\npass" );
		//audit.audit( "equals test "+ (sa1.equals( sa2 ) ? "passes" : "fails" ));
		audit.audit( "===> ["+ sa1.toString( "'", "', '", "'" ) +"] <===" );
		//audit.audit( "===> ["+ sa2.toString( Strings.SQCSV ) +"] <===" );
	
		
		//public static String[] removeAt( String[] a, int n ) ;
		//public static String[] removeAll( String[] a, String val ) ;
		// EITHER:
		//String[] a = new String[] {"One Two Three", "this test passes", "Alpha Beta" };
		//String  val= "passes";
		//audit.audit( "getContext test: "+ getContext( a, val ));
		
		Strings outer = new Strings( "a strong beer" );
		Strings inner = new Strings( "strong beer" );
		audit.audit( "containsStrings test "+ (outer.containsMatched( inner ) ? "passes" : "fails" ));
		// ...OR: -------------------------------------------
		// a=[ "One Two Three", "Aye Bee Cee", "Alpha Beta" ], val= "Bee" => b = [ "One Two Three", "Alpha Beta" ];
		//public static String[] removeAllMatched( String[] a, String val ) ;
		// ---------------------------------------------
		//public static String[] removeFirst( String[] a, String val ) ;
		//audit.audit( toString( removeFirst( Strings.fromString( "this test passes" ), "test" ), SPACED ));
		//public static String[] append( String[] a, String str ) ;
		//audit.audit( toString( append( Strings.fromString( "this test " ), "passes" ), SPACED ));
		//public static String[] append( String[] a, String sa[] ) ;
		//audit.audit( toString( append( fromString( "this test " ), fromString( "passes" )), SPACED ));
		//public static String[] prepend( String[] a, String str ) ;
		//audit.audit( toString( prepend( fromString( "test passes" ), "this" ), Strings.SPACED ));
		//public static String[] copyAfter( String[] a, int n ) ;
		//audit.audit( toString( copyAfter( fromString( "error this test passes" ), 0 ), SPACED ));
		//public static String[] copyFromUntil( String[] a, int n, String until ) ;
		//Strings xxx = new Strings( "error this test passes error" );
		//audit.audit( toString( xxx.copyFromUntil( 1, "passes" ), SPACED ));
		//public static String[] fromNonWS( String buf ) ;
	/*	audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//public static String[] insertAt( String[] a, int pos, String str ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//public static String[] reverse( String[] a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		// replace( [ "hello", "martin", "!" ], [ "martin" ], [ "to", "you" ]) => [ "hello", "to", "you", "!" ]
		//public static String[] replace( String[] a, String[] b, String[] c ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//public static int indexOf( String[] a, String s ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//public static boolean contain( String[] a, String s ) ; return -1 != indexOf( a, s ); }
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		
		// deals with matched and unmatched values:
		// [ "a", "$matched", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
		// [ "a",  "MATCHED", ".",  "b" ] => [ "a", "martin", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
		// [ "a", "$unmatch", ".",  "b" ] => [ "a", "_USER", "." ] += [ "b", "." ] -- add period like Tag.c::newTagFromDesc...()
		// [ "we are holding hands", "."  ] => [ "we", "are", "holding", "hands", "." ] -- jik - just in case!
		// matches are from tags, *ap contains mixed case - any UPPERCASE items should match matches OR envvars.
		//public static String[][] split( String[] a, String[] terminators ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));

		// [ 'some', 'bread', '+', 'fish'n'chips', '+', 'some', 'milk' ], "+"  => [  'some bread', 'fish and chips', 'some milk' ]
		//public static String[] rejig( String[] a, String ipSep, String opSep ) ;
		//public static String[] rejig( String[] a, String sep ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		// todo: remove rejig, above? Or, combine with expand() and normalise()/
		// NO: Need Stringses to Strings & vv [ "some", "beer", "+", "some crisps" ] => "some beer", "some crisps" ]
		// [ "some beer", "some crisps" ] => [ "some", "beer", "+", "some", "crisps" ]
		// todo: expand input, and apply each thought...
		// I need to go to the gym and the jewellers =>
		// (I need to go to the gym and I need to go to the jewellers =>)
		// I need to go to the gym. I need to go to the jewellers.
		//public static String[][] expand( String[][] a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));

		// [ "one", "two three" ] => [ "one", "two", "three" ]
		// todo: a bit like re-jig aove???
		//public static String[] normalise( String[] sa ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//public static boolean isUpperCase( String a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		//public static boolean isUpperCaseWithHyphens( String a ) ;
		audit.audit( toString( ( Strings.fromString( "this test " ), "passes" ), Strings.SPACED ));
		 *
		 */
		// [ "THIS", "is Martin" ] => [ "THIS", "is", "martin" ]
		//audit.audit( Strings.toString( decap( Strings.fromString( "THIS is Martin" )), Strings.DQCSV ) +" should equal [ \"THIS\", \"is\", \"martin\" ]" );
		//audit.audit( trim( "\"hello\"", '"' ) +" there == "+ trim( "ohio", 'o' ) +" there! Ok?" );
		
		//audit.audit( Strings.toString( Strings.fromString( "failure won't 'do' 'don't'" ), Strings.DQCSV ));
		//audit.audit( Strings.toString( Strings.insertAt( Strings.fromString( "is the greatest" ), -1, "martin" ), Strings.SPACED ));
		//audit.audit( "" );
		audit.debug( "Item:       "+ Strings.hash(      Item.NAME));
		audit.debug( "Link:       "+ Strings.hash(      Link.NAME));
		audit.debug( "Sign:       "+ Strings.hash(      Sign.NAME));
		audit.debug( "Items:      "+ Strings.hash(     Items.NAME));
		audit.debug( "Value:      "+ Strings.hash(     Value.NAME));
		audit.debug( "Where:      "+ Strings.hash(     Where.NAME));
		audit.debug( "Entity:     "+ Strings.hash(    Entity.NAME));
		audit.debug( "Plural:     "+ Strings.hash(    Plural.NAME));
		audit.debug( "Numeric:    "+ Strings.hash(   Numeric.NAME));
		audit.debug( "Overlay:    "+ Strings.hash(   Overlay.NAME));
		audit.debug( "Function:   "+ Strings.hash(  Function.NAME));
		audit.debug( "Temporal:   "+ Strings.hash(  Temporal.NAME));
		audit.debug( "Variable:   "+ Strings.hash(  Variable.NAME));
		audit.debug( "Colloquial: "+ Strings.hash(Colloquial.NAME));
		audit.debug( "Transitive: "+ Strings.hash(Transitive.NAME));
		
		Variable.set( "ENT",  "martin" );
		Variable.set( "ATTR", "height" );
		
		Strings tmp2 = new Strings( "..., ENT''s ATTR is ..." );
		audit.debug( "tmp2: "+ tmp2.toString( DQCSV ));
		tmp2 = Utterance.externalise( tmp2, false );
		audit.debug( "tmp2: "+ tmp2.toString( DQCSV ));
}	}
