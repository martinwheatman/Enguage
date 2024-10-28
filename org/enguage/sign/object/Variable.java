package org.enguage.sign.object;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.object.sofa.Value;
import org.enguage.sign.pattern.Pattern;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Variable {
	/* As part of the Sofa library, variable manages persistent values:
	 * /a/b/c/_NAME -> value, which is the persistent equivalent of NAME="value".
	 * Because $ has special significance in the filesystem/shell
	 * prefix variables with '_'
	 */
	public  static  final String NAME = "variable";
	public  static  final int      ID = 262169728; //Strings.hash( NAME )
	private static       Audit audit = new Audit( "Variable" );
	
	private static TreeMap<String,String> cache = encache();

	// If overlays are modified, variables need to be encached...
	public  static TreeMap<String,String> encache() {return encache( Overlay.get() );}

	private static TreeMap<String,String> encache( Overlay o ) {
		//audit.in( "encache", Ospace.location())
		cache = new TreeMap<>();
		for( String name : o.list( NAME ))
			if (name.equals( name.toUpperCase( Locale.getDefault()) )) // if valid variable name
				cache.put( name, new Value( NAME, name ).get());
		//audit.out()
		return cache;
	}
	private static void printCache() {
		Audit.title( "Printing cache" );
		Set<Map.Entry<String,String>> entries = cache.entrySet();
		Iterator<Map.Entry<String,String>> ei = entries.iterator();
		Audit.incr();
		while (ei.hasNext()) {
			Map.Entry<String,String> e = ei.next();
			audit.debug( e.getKey()  +"='"+  e.getValue()  +"'" );
		}
		Audit.decr();
	}

	String name;
	Value  value;
	
	public Variable( String nm ) {
		name = nm.toUpperCase( Locale.getDefault());
		value = new Value( NAME, name );
		cache.put( name, value.get());
	}
	public Variable( String nm, String val ) {
		name = nm.toUpperCase( Locale.getDefault());
		value = new Value( NAME, name );
		set( val );
	}
	public String     get() { return cache.get( name ); }
	public boolean  isSet( String value ) { return cache.get( name ).equals( value );}
	public Variable   set( String val ) {
		cache.put( name, val );
		value.set( val );
		return this;
	}
	public Variable unset() {
		cache.remove( name );
		value.ignore();
		return this;
	}
	
	// TODO: need to have lowercase variables too - so concept scripts can set them
	// 		 Can't be UPPERCASE, unless TAGs passes through u/c unmatches vars.
	// 		 May match - then difficult bugs to find!
	// for backward compatibility, keeping these statics 
	public  static String set( String name, String val ) { new Variable( name ).set( val ); return val;}
	public  static void unset( String name ) { new Variable( name ).unset(); }
	public  static String get( String name ) { return cache.get( name.toUpperCase( Locale.getDefault()) ); } // raw name
	public  static String get( String name, String def ) {
		boolean reflect = name.startsWith( Pattern.EXTERN_PREFIX );
		if (reflect)
			name = name.substring( Pattern.EXTERN_PREFIX.length() );
		
		boolean first = name.startsWith( Pattern.FIRST_PREFIX );
		if (first)
			name = name.substring( Pattern.FIRST_PREFIX.length() );
		
		boolean rest = name.startsWith( Pattern.REST_PREFIX );
		if (rest)
			name = name.substring( Pattern.REST_PREFIX.length() );
		
		String value = cache.get( name );
		if (reflect)
			value = Attributes.reflect( new Strings( value )).toString();
		
		if (first) { // "martin and ruth" => "martin"
			int old = value.length();
			value = new Strings( value ).before( "and" ).toString();
			if (old == value.length())
				value = new Strings( value ).before( "or" ).toString();
			
		} else if (rest) { // "martin or ruth" => "ruth"
			String newValue = new Strings( value ).after( "and" ).toString();
			if (newValue.length() > 0)
				value = newValue;
			else
				value = new Strings( value ).after( "or" ).toString();
		}
		
		return value==null || value.equals("") ? def : value;
	}
	public  static boolean isSet( String name, String value ) {
		String val = new Variable( name ).get();
		return  val != null &&
				((value == null && !val.equals( "" )  ) ||
				 (value != null && val.equals( value ))   );
	}
	public  static String derefUc( String in ) {
		String       word = "";
		StringBuilder out = new StringBuilder();
		char[] buffer = in.toCharArray();
		for (char ch : buffer) {
			if (Character.isUpperCase( ch ))
				word += ch;
			else {
				if (!word.equals( "" )) {
					word = Variable.get( word );
					if (word == null) word = "";
					out.append( word );
					word = "";
				}
				out.append( ch );
		}	}
		return out.toString();
	}
	
	private static Strings exceptions = new Strings();
	public  static  void exceptionAdd( Strings excepts ) {
		for (String s : excepts ) if (!exceptions.contains( s )) exceptions.add( s );
	}
	private static void exceptionRemove( Strings excepts ) {
		for (String s : excepts ) exceptions.remove( s );
	}
	
	private static Strings deref( String name ) {
		// must return strings for case where variable value is 'hello world'
		// must contract( "=" ) for case where 'name' is "SUBJECT='fred'"
		return (!exceptions.contains( name ) && Strings.isUCwHyphUs( name )
				 ?	new Strings( get( name, name )).replace( ",", "and" )
				 :	new Strings( name )).contract( "=" );
	}
	public  static Strings derefOrPop( ListIterator<String> ai ) { return derefOrPop( ai, false ); }
	public  static Strings derefOrPop( ListIterator<String> ai, boolean internal ) {
		// "QUANTITY='2' UNITS='cup' of" => "2 cups of"
		// "LOCATION='here' LOCATOR=''"  => ""
		Strings b = new Strings();
		while (ai.hasNext()) {
			String next = ai.next();
			if (next.equals( "[" ))
				b.appendAll( derefOrPop( ai, true ));
			else if (next.equals( "]" ))
				break;
			else {
				Strings c = deref( next ); // deref ...
				if (internal)
					for (String d : c)
						if (!exceptions.contains( d )
								&& Strings.isUCwHyphUs( d )) { // ... POP!
							//audit.debug( "popping on "+ d )
							while (ai.hasNext() && !ai.next().equals( "]" ));
							return new Strings();  // or null?
						}
				b.appendAll( c );
		}	}
		return b;
	}
	public  static Strings deref( Strings in ) {
		Strings out = new Strings();
		Iterator<String> it = in.iterator();
		while (it.hasNext())
			out.appendAll( deref( it.next() )); // preserve name='value'
		return out;
	}
	
	public  static Strings perform( Strings args ) {
		audit.in( "interpret", args.toString() );
		String  rc = Response.okay().toString();
		String cmd = args.remove( 0 );
		int sz = args.size();
		if (sz > 0) {
			String name = args.remove( 0 );
			if (sz > 1)
				if (cmd.equals( "set" ))
					rc = set( name, args.toString() );

				else if (cmd.equals( "equals" ))
					rc = isSet( name, args.toString()) ? Response.okay().toString() : Response.notOkay().toString();
			
				else if (cmd.equals( "exception" )) {
					
					String direction = args.remove( 0 );
					if (direction.equals( "add" ))
						exceptionAdd( args );
					else if (direction.equals( "remove" ))
						exceptionRemove( args );
					else
						rc = Response.notOkay().toString();
				} else
					rc = Response.notOkay().toString();
				
			else { // sz == 1, name and no params
				if (cmd.equals( "exists" ))
					rc = isSet( name, null ) ? Response.okay().toString() : Response.notOkay().toString();
				else if (cmd.equals( "unset" ))
					unset( name );
				else if (cmd.equals( "get" ))
					rc = get( name.toUpperCase( Locale.getDefault() ));
				else
					rc = Response.notOkay().toString();
			}
		} else if (cmd.equals( "show" )) {
			audit.debug( "printing cache" );
			printCache();
			audit.debug( "printed" );
		} else
			rc = Response.notOkay().toString();
		rc = rc==null?"":rc;
		audit.out( rc );
		return new Strings( rc );
	}
	
	// --
	public  static void test( String cmd, String expected ) {
		Strings actual = perform( new Strings( cmd ));
		if (actual.equals( new Strings( expected ))) {
			if (actual.isEmpty())
				audit.debug(   "PASS: "+ cmd );
			else
				audit.debug(   "PASS: "+ cmd +" = '"+ actual +"'" );
		} else
			audit.FATAL( "FAIL: "+ cmd +" = '"+ actual +"' (expected: "+ expected +")" );
	}
	public  static void main( String[] args ) {
		Overlay.attach( NAME );
		Audit.on();
		
		printCache();
		Variable spk = new Variable( "NAME" );
		String tmp = spk.get();
		audit.debug( "was="+ (tmp==null?"<null>":tmp));
		if ( tmp == null || tmp.equals( "fred" ))
			perform( new Strings( "set NAME billy boy" ));
		else
			spk.set( "fred" );
		tmp = spk.get();
		audit.debug( "now="+ (tmp==null?"<null>":tmp));
		if ( spk.isSet( "fred" ))
			audit.debug( "spk set to Fred" );
		else
			audit.debug( "spk is set to Bill" );
		printCache();
		
		//*		Static test, backwards compat...
		test( "set hello there", "there" );
		test( "get HELLO", "there" );
		test( "equals HELLO there", Response.okay().toString() );
		audit.debug( "deref: HELLO hello there="+ deref( new Strings( "HELLO hello there" )));
		test( "unset HELLO", Response.okay().toString() );
		test( "get HELLO", "" );
		{	// derefOrPop test...
			Variable.unset( "LOCATOR" );
			Variable.set( "LOCATION", "Sainsburys" );
			Strings b = new Strings( "where [LOCATOR LOCATION] to LOCATION" );
			
			b = derefOrPop( b.listIterator() );

			audit.debug( "b is now '"+ b +"' (should be 'where to Sainsburys')");
		}
		audit.debug( "PASSED" );
}	}
