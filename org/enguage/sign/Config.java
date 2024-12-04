package org.enguage.sign;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Locale;

import org.enguage.Enguage;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.interpretant.intentions.Commands;
import org.enguage.sign.interpretant.intentions.Reply;
import org.enguage.sign.object.Variable;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.http.Http;
import org.enguage.util.http.InfoBox;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;
import org.enguage.util.sys.Fs;
import org.enguage.util.tag.Tag;

public class Config {

	private Config() {}
	
	private static final Audit audit = new Audit( "Config" );
	private static final String NAME = "config";
	
	private static boolean complete = false;
	public  static boolean complete() {return complete;}
	
	private static boolean setValues( String name, String value ) {
		     if (name.equals("ACCUMULATECOMMAND")) accumulateCmds( new Strings( value ));
		else if (name.equals("ANDCONJUNCTIONS")) andConjunction( value );
		else if (name.equals("ORCONJUNCTIONS"))   orConjunction( value );
		else if (name.equals("PROPAGATEREPLY")) propagateReplys( new Strings( value ));
		else if (name.equals("ANDLISTFORMAT" )) andListFormat( value);
		else if (name.equals("LISTFORMATSEP" )) listSep(       value);
		else if (name.equals( "ORLISTFORMAT" )) orListFormat(  value );
		else if (name.equals( "REPEATFORMAT" )) repeatFormat(  value );
		else if (name.equals( "ATTRIBUTING" )) InfoBox.attributing(  value );
		else if (name.equals( "REFERENCERS" )) referencers( new Strings( value ));
		else if (name.equals( "CONDFORMAT" )) Intention.conditionalFormat( new Strings( value, ':' ));
		else if (name.equals( "CLASSPATH" )) Commands.classpath( value );
		else if (name.equals( "CONDPOS" )) Intention.soPrefix( value );
		else if (name.equals( "CONDNEG" )) Intention.noPrefix( value );
		else if (name.equals( "SUCCESS" )) Response.Type.OK.value( value );
		else if (name.equals( "FAILURE" )) Response.Type.SOZ.value( value );
		else if (name.equals(  "ANSWER" )) placeholder( new Strings( value ));
		else if (name.equals(   "SHELL" )) Commands.shell( value );
		else if (name.equals(   "TERMS" )) Terminator.terminators( new Strings( value ));
		else if (name.equals(    "SOFA" )) Commands.java( value );
		else if (name.equals(     "URL" )) Http.url( value );
		else if (name.equals(     "TTL" )) Autoload.ttl( value );
		else if (name.equals(     "DNU" )) Response.Type.DNU.value( value );
		else if (name.equals(     "DNK" )) Response.Type.DNK.value( value );
		else if (name.equals(     "UDU" )) Response.Type.UDU.value( value );
		else if (name.equals(     "YES" )) Response.Type.YES.value( value );
		else if (name.equals(      "NO" )) Response.Type.NO.value(  value );
		else
			return false;
		return true;
	}
	private static void setContext( ArrayList<Attribute> aa ) {
		ListIterator<Attribute> pi = aa.listIterator();
		while (pi.hasNext()) {
			Attribute  a = pi.next();
			String  name = a.name().toUpperCase( Locale.getDefault() );
			String value = a.value();
			if (!setValues( name, value))
				Variable.set( name,  value );
	}	}	
	private static void loadTag( Tag concepts ) {
		for (Tag t : concepts.children())
			if ( t.name().equals( "concept" ) &&
			    !t.attributes().value( "op" ).equals( "ignore" ))
				Concept.load( t.attributes().value( "id" ));
	}
	public static int load( String fname ) {
		int rc = -1;
		String content = Fs.stringFromStream(
			Assets.getStream( Enguage.RO_SPACE+ fname )
		);
		
		Tag t = new Tag( new Strings( content ).listIterator());
		if ((t = t.findByName( NAME )) != null) {
			setContext( t.attributes() );
			loadTag( t.findByName( "concepts" ));
			rc = content.length();
		}

		complete = true;
		return audit.out( rc );
	}

	private static String  repeatFormat = "i said, ... .";
	public  static void    repeatFormat( String s ) {repeatFormat = s.toLowerCase( Locale.getDefault() );}
	public  static String  repeatFormat() {return repeatFormat;}

	private static String  andConjunction = "and";
	public  static void    andConjunction( String s ) {andConjunction = s.toLowerCase( Locale.getDefault() );}
	public  static String  andConjunction() {return andConjunction;}

	private static Strings andListFormat = new Strings( ", /, and ", '/' );
	public  static void    andListFormat( String s ) {andListFormat = new Strings( s, Config.listSep().charAt( 0 ));}
	public  static Strings andListFormat() {return andListFormat;}

	private static String  orConjunction = "or";
	public  static void    orConjunction( String sa ) {orConjunction = sa;}
	public  static String  orConjunction() {return orConjunction;}

	private static Strings orListFormat = new Strings( ", /, or ", '/' );
	public  static void    orListFormat( String s ) {orListFormat = new Strings( s, Config.listSep().charAt( 0 ));}
	public  static Strings orListFormat() {return orListFormat;}

	private static Strings referencers = new Strings( "the" );
	public  static void    referencers( Strings sa ) {referencers = sa;}
	public  static Strings referencers() {return referencers;}

	private static String  listSep = "/";
	public  static void    listSep( String s ) {listSep = s;}
	public  static String  listSep() {return listSep;}

	private static Strings propagateReplys = new Strings( "say so" );
	public  static Strings propagateReplys() {return propagateReplys;}
	public  static void    propagateReplys( Strings pr ) {propagateReplys = pr;}

	// to interact with the 'say' list - as String or Strings
	private static Strings accumulateCmds = new Strings( "say this now" );
	public  static Strings accumulateCmds() {return accumulateCmds;}
	public  static void    accumulateCmds( Strings ac ) {accumulateCmds = ac;}
	
	// Format answer placeholder
	private static Strings placeholder = new Strings( Reply.ANSWER_PH );
	public  static Strings placeholder() {return placeholder;}
	public  static void    placeholder( Strings ph ) {placeholder = ph;}
}