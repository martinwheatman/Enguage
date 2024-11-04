package org.enguage.util.audit;

import java.util.GregorianCalendar;

import org.enguage.repertoires.Repertoires;
import org.enguage.sign.interpretant.Response;
import org.enguage.util.strings.Strings;

public class Audit {
	private static final Audit audit = new Audit( "Audit" );
	
	public  static final int            ID = 829030;
	private static       Strings funcNames = new Strings( "zeroStack" );

	// === auditing on/off API
	private static  boolean auditOn = false;
	public  static  void    off() {auditOn = false; indent.reset();}
	public  static  void    on() {auditOn = true;}
	public  static  boolean isOn() {return auditOn;}
	
	private boolean tracing = false; // per object
	public  Audit   tracing( boolean on ) {tracing=on; return this;}
	private boolean debugging = false; // per object
	public  Audit   debugging( boolean on ) {debugging=on; return this;}

    // === indent
 	private static  Indentation indent = new Indentation();
 	public  static  void   incr() {indent.incr();}
 	public  static  void   decr() {indent.decr();}
 	public  static  String indent() {return indent.toString();}
 	
	private static boolean showSignsOnFatal = false; // on fatal
	private static void    showSignsOnFatal() {showSignsOnFatal=true;}
	private static void    hideSignsOnFatal() {showSignsOnFatal=false;}
	
	public Audit( String nm ) {className = Character.toUpperCase( nm.charAt(0)) + nm.substring(1);}
	
	private              String   className = "";

	// === global DEBUG switches...
	public  static final boolean  numericDebug = false;
	
	// === timestamp
	private static  long then = new GregorianCalendar().getTimeInMillis();
	public  static  long interval() {
		long now = new GregorianCalendar().getTimeInMillis();
		long rc = now - then;
		then = now;
		return rc;
	}
	
	// === debug and detail - "auditing"
	private static  int     suspended = 0;
	public  static  void    suspend() {suspended++;}
	public  static  void    resume() {if (suspended > 0) suspended--;}
	public  static  boolean suspended() {return suspended>0;}
	
	// test count
	private static int  numberOfTests = 0;
	public  static int  numberOfTests() {return numberOfTests;}
	public  static void passed() {numberOfTests++;}
	public  static void passed( String msg ) {log( msg ); passed();}
	public  static void PASSED() {log( "+++ PASSED "+ numberOfTests +" tests in "+ interval()+"ms +++" );}
	
	// === allOn - tracing AND debug
	// allOn vs. auditOn - turning auditOn when allOn, suppresses for this level
	public  static  boolean allAreOn() {return auditOn;}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- LOGGING - LOG, debug, info, error, FATAL
	// ---
	public static String log( String info ) {
		System.out.println( indent.toString() + info );
		return info;
	}
	public static  String log( Strings info ) {return log( info.toString() );}
	
	public static String timestamp( String info ) {log( info+ " -- "+interval()+"ms\n");return info;}
	
	public void   debug( String info ) {
		if (suspended==0 && (auditOn || debugging)) log( info );
	}
	public void   debug( Strings info ) {debug( info.toString() );}
	public Object  info(  String fn, String in, Object out ) {// out may be null!
		if (auditOn && (out!=null && !out.equals("")))
			debug( className +"."+ fn +"( "+ in +" ) => "+ out.toString() );
		return out;
	}
	public void   FATAL( String msg ) {
		log( "FATAL: "+ className +": "+ msg );
		if (showSignsOnFatal) Repertoires.signs().show();
		System.exit( 1 );
	}
	public void   FATAL( String phrase, String msg ) {FATAL( phrase +": "+ msg );}
	public void   error( String info ) {
		log("ERROR: "+ className +(funcNames.size()>1?"."+ funcNames.get( 0 ) +"()" : "")+": "+ info);
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// --- Tracing - IN/OUT
	// ---
    public  void in( String fn ) {in( fn, "" );}
    public  void in( String fn, String info ) {if (auditOn || tracing) IN( fn, info );}
    public  void IN( String fn, String info ) {
		// sometimes this is tested at call time - preventing the string processing
		// in the traceIn() call being performed at runtime.
		funcNames.prepend( fn );
		log( "IN  "+ className +"."+ fn +"("+ (info==null?"":" "+ info +" ") +")");
		indent.incr();
	}

    public String OUT() {return OUT( (String) null );}
    public String OUT( String result ) {
    	indent.decr();
		log( "OUT "+ className
				+ (funcNames.size()>1?"."+ funcNames.remove( 0 ) +"()" : "")
				+ (result==null ? "" : " => '"+ result +"'" )
			);
		return result;
    }
	public String  out( String result ) {return (auditOn || tracing) ? OUT( result ) : result;}
	public void    out() {out( (String)null );}
	public Strings out( Strings s ) {out( s!=null?"["+s.toString(Strings.DQCSV)+"]":"<null>"); return s;}
	public boolean out( boolean b ) {out( Boolean.toString( b )); return b;}
	public int     out( int     n ) {out( Integer.toString( n )); return n;}
	public Float   out( Float   f ) {out( Float.toString(   f )); return f;}
	public Float   OUT( Float   f ) {OUT( Float.toString(   f )); return f;}
	public long    out( long    l ) {out( Long.toString(    l )); return l;}
	public Object  out( Object  o ) {out( o==null ? "null" : o.toString()); return o;}
	public Object  OUT( Object  o ) {OUT( o==null ? "null" : o.toString()); return o;}
	public Strings OUT( Strings o ) {OUT( o==null ? "null" : o.toString()); return o;}
	public boolean OUT( boolean b ) {OUT( Boolean.toString( b )); return b;}
	
	public static Strings perform(Strings cmds) {
		audit.in( "perform", ""+ cmds );
		
		Strings rc = Response.okay();
		String cmd = cmds.remove( 0 );
		
		if (cmd.equals( "entitle" )) {
			cmds.toUpperCase();
			Audit.title( cmds.toString() );
			
		} else if (cmd.equals( "subtitle" )) {
			Audit.subtl( cmds.toString() );
			
		} else if (cmd.equals( "echo" )) {
			Audit.log( cmds.toString() );
			
		} else {
			String param = cmds.isEmpty() ? "" : cmds.remove( 0 );
			
			if (cmd.equals( "timing" ) || cmd.equals( "tracing" )) {
				if (param.equals("off"))
					off();
				else
					on();
				
			} else if (cmd.equals( "detailed" )) {
				if (param.equals("off"))
					off();
				else
					on();
			
			} else if (cmd.equals( "debug" )) {
				if (param.equals( "off" ))
					off();
				else
					on();
				
			} else if (cmd.equals( "show" )) {
				if (param.equals( "signs" ))
					showSignsOnFatal();
				
			} else if (cmd.equals( "hide" )) {
				if (param.equals( "signs" ))
					hideSignsOnFatal();				
				
			} else
				rc = Response.notOkay();
		}			

		return audit.out( rc) ;
	}
	
	// === title/underline
	private static boolean firstTitle = true;
	private static void bracket( String title, String chs ) {
		if (!firstTitle) log( "\n" );
		log( chs +" "+ title +" "+ chs );
		firstTitle = false;
	}
	public  static void underline( String title ) {underline( title, '-' );}
	public  static void underline( String title, char ch ) {
		log( title );
		StringBuilder underline = new StringBuilder();
		for (int i = 0; i < title.length(); i++) underline.append( ch );
		log( underline.toString() );
	}
	private static void title( String title, char ch ) {
		if (!firstTitle) log( "\n" );
		underline( title, ch );
		firstTitle = false;
	}
	public  static void title( String title ) {title( title, '=' );}
	public  static void subtl( String title ) {bracket( title, "**" );}

	public static void main( String[] args ) {
		Audit audit = new Audit( "Audit" ); // <= needs setting as $DEBUG to test
		Audit.on();
		audit.in( "main", "this='is', a='test'" );
		audit.in( "inn", "this='is', again='aTest'" );
		audit.in( "inner", "this='is', again='aTest'" );
		audit.debug( "Hello, martin" );
		audit.out();
		audit.out();
		audit.out( "passed" );
}	}
