package org.enguage.util.http;

import java.util.ListIterator;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Autoload;
import org.enguage.sign.factory.Written;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.symbol.when.Date;
import org.enguage.sign.symbol.where.Address;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Attributes;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.token.TokenStream;

public class InfoBox {
	public  static final int      ID = 113445711;
	private static final String NAME = "Infobox";
	private static final Audit audit = new Audit( NAME );

	private static String INFO_BOX_CONCEPT = "InfoBoxGen";
	
	private static String wsrc = "";
	public  static String wikiSource() {return wsrc;}
	public  static void   wikiSource( String s ) {wsrc = s;}
	
	// Set in Config.java/config.xml
	private static Strings attributing = InfoBox.attributing( "according to X," );
	public  static Strings attributing() {return attributing;}
	public  static Strings attributing( String a ) {
		attributing = new Strings( a ).reverse();
		return attributing;
	}	
	private static String decodeSource( String fname ) {
		String[] source = fname.split( "\\." );
		return source[ source.length-1 ];
	}

	private static String topic = "";
	public  static String topic() {return topic;}
	public  static void   topic( String t ) {topic = t;}

	private static String decodeTopic( String fname ) {
		String[] source = fname.split( "\\." );
		String[]   path = source[ source.length - 2 ].split( "/" );
		String    topic = path[ path.length - 1 ].replace( "_", " " );
		return topic;
	}
	private static Attributes listHeaderAttributes( String name, Attributes attrs ) {
		Attributes rc = new Attributes();
		boolean printMe = false;
		// extract attributes for the appropriate header
		ListIterator<Attribute> ni = attrs.listIterator();
		while (ni.hasNext()) {
			Attribute a = ni.next(); 
			if (a.name().equals( "header" ))
				printMe = a.value().equalsIgnoreCase( name );
			else if (printMe)
				rc.add( a );
		}
		return rc;
	}
	private static Strings retrieveValue( Strings name, Attributes attrs ) {
		Strings rc = null;
		// [html retrieve] largest_city_date ["./wiki/Queen_Elizabeth.wikipedia"]
		// attrs => [name="value", name1="value1", ... ]
		
		String  type = name.get( name.size()-1 ); // [value|date|name|address]
		if (type.equals( "date") ||
			type.equals("place") ||
			type.equals( "name") ||
			type.equals("value")   )
			// args is not the attribute name pattern
			type = name.remove( name.size()-1 );
		else
			type = "name";
		
		// Obtain the named value - split into sub-values
		String value = attrs.match( name );
		if (value.equals(""))
			rc = Response.notOkay( topic() +" does not have a '"+ name +"' attribute" );
		
		else {
			//Audit.log( "Value is >"+ value +"<" );
			Strings  subvals = new Strings( value.split( ";" ));
			
			rc = new Strings( "sorry, I don't know" );
			if (type.equals( "date" ))
				rc = new Strings( Date.getDate( subvals, "sorry, I can't find a date" ));
				
			else if (type.equals( "place" ))
				rc = new Strings( Address.getAddress( subvals, "sorry, I can't a place" ));
				
			else if (type.equals( "name" )) {
				for (String s : subvals)
					if (!Date.isDate( s ) &&
					    !Address.isAddress( s ))
					{
						rc = new Strings( s );
						break;
					}
			} else if (type.equals( "value" ))
				rc = new Strings( value );
			rc.stripBrackets( "[", "]" ); // e.g. ref: [1] - on mobile page
		}
		return rc;
	}
	private static String removeJunkBeforeColon( String name ) {
		String tmp = "";
		try {
			TokenStream ts = new TokenStream( name.getBytes("UTF-8") );
			while (ts.hasNext()) {
				String str = ts.getString(); 
				if (str.equals( ";" ))
					tmp = "";
				else
					tmp += (tmp.equals("") ? "" :" ") + str;
			}
			//Audit.log( "tmp is "+ tmp );
			ts.close();
		} catch (Exception x ) {
			Audit.log( "ex:"+ x );
		}
		return tmp;
	}
	public  static Strings perform( Strings args ) {
		audit.in( "interpret", "args="+ args.toString( Strings.DQCSV ));
		Strings rc = new Strings( "sorry, i don't understand" ); // Shell.Fail;
		String cmd = args.remove( 0 );

		// get the filename- last parameter - and set the source && topic
		String fname = Strings.trim( args.remove( args.size()-1 ), '"' );
		// source is now last portion of cached name
		wikiSource( decodeSource( fname )); // e.g. wikipedia
		topic( decodeTopic( fname ));

		Attributes attrs = HtmlTable.doHtml( new HtmlStream( fname ));
		
		if (cmd.equals( "group" )) {

			//Audit.log( "IN GROUP: ["+ args.toString( Strings.DQCSV) +"]" );
			
			boolean checkMe = false;
			String group = args.remove(0);
			String attrName = args.remove(0);
			rc = new Strings( "sorry, attribute "+ attrName +"not found in "+ group );
			ListIterator<Attribute> ai = attrs.listIterator();
			while (ai.hasNext()) {
				Attribute attr = ai.next();
				if (attr.name().equals( "header" ))
					checkMe = attr.value().equalsIgnoreCase(group);
				else if (checkMe && attr.name().contains( attrName ))
					rc = new Strings( attr.value());

				//Audit.log( "name="+ attr.name() +", value="+ attr.value());
			}
			
			
		} else if (cmd.equals( "list" )) {
			String option = args.remove( 0 ); // "values" or "header"

			if (option.equals( "values" )) {
				
				Strings names = attrs.names();
				ListIterator<String> ni = names.listIterator();
				while (ni.hasNext())
					if (ni.next().equals( "header" ))
						ni.remove();
				
				rc = new Strings( ""+ names.toString( Strings.DQCSV ));
				
				
			} else if (option.equals( "header" )) {
				rc = new Strings();
				String header = args.remove( 0 );
				Attributes headerAttrs = listHeaderAttributes( header, attrs );
				
				if (headerAttrs.isEmpty())
					rc = Response.notOkay( "the "+ header +" is not a header" );
				else {
					ListIterator<Attribute> ai = headerAttrs.listIterator();
					while (ai.hasNext()) {
						Attribute adjective = ai.next();
						String name = removeJunkBeforeColon( adjective.name() );
						
						rc.append( name +" "+ header );
						Repertoires.signs().insert(
								new Written( 
											"On \""+ name +" "+ header +"\""
											+ ", according to wikipedia "
											+ "what is the "+ name +" of "+ header 
											+" from "+ topic()
											+" on the cached page"
									).toSign().concept( INFO_BOX_CONCEPT )
						);
					}
					rc = new Strings( rc.toString( "", " or ", "" ));
					// Let Autoload know we've signs to remove...
					Autoload.put( INFO_BOX_CONCEPT );
			}	}
			
			
		} else if (cmd.equals( "retrieve" )) {

			// [html retrieve] largest_city_date ["./wiki/Queen_Elizabeth.wikipedia"]
			// attrs => [name="value", name1="value1", ... ]
			Strings name = new Strings( args.get( 0 ));
			rc = retrieveValue( name, attrs );
			
			
		} else if (cmd.equals( "exists" )) {
			
			Audit.log( "args: ["+ args.toString( Strings.DQCSV ) +"]" );
			if (args.size() != 2 ) 
				rc = Response.notOkay( "exists method needs two parameters, got: "+ args );
			else {
				Strings name  = new Strings( args.get( 0 ));
				Strings value = new Strings( args.get( 1 ));
				
				rc = retrieveValue( name, attrs );
				
				rc.prepend( "is" );
				rc.prepend( "value" );
				for (String s : name )
					rc.add( 0, s );
				rc.prepend( "the" );
				for (String s : InfoBox.attributing())
					rc.add( 0, s.equals( "X" ) ? InfoBox.wikiSource() : s );

				rc.prepend( "," );
				if (rc.equals( value ))
					rc.prepend( "Yes" );
				else
					rc.prepend( "Sorry" ); // No isn't negative enough?
			}
			
		} else if (cmd.equals( "header" )) {

			rc = new Strings();
			String param = args.remove( 0 ); // "value"
			param = args.remove( 0 );        // ATTR
			
			Attributes headerAttrs = listHeaderAttributes( param, attrs );
			if (headerAttrs.size() == 1)
				rc = new Strings(
						" the "
						+ headerAttrs.get( 0 ).name() 
						+ " "
						+ param 
						+ " of "
						+ topic() 
						+ " is "
						+ headerAttrs.get( 0 ).value()
				);
			else
				rc = Response.notOkay( "there is more than one header value" );
		}
		
		audit.out( rc );
		return rc;
	}
	// this is only called directly from Enguage.java - on replying to the user
	public static Strings attributeSource( Strings reply ) {
		// TODO: de-hardcode these prefixes
		if (!wikiSource().equals( "" ) ) {
			// only attribute successful replies...
			if (!(reply.get(0).equalsIgnoreCase( "sorry" ) &&
			      reply.get(1).equals( "," )))
			{
				if (reply.get(0).equalsIgnoreCase( "ok" ) &&
					reply.get(1).equals( "," ))
				{
					reply.remove(0);
					reply.remove(0);
				}
				
				for (String s : attributing)
					reply.add( 0, s.equals( "X" ) ? wikiSource() : s );
			}
			// ...and finally, we want to scrub a source whether or not it was used!
			wikiSource( "" );
		}
		return reply;
}	}
