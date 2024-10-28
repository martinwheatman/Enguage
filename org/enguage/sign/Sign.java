package org.enguage.sign;

import org.enguage.sign.factory.Spoken;
import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Intentions;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Temporal;
import org.enguage.sign.pattern.Frag;
import org.enguage.sign.pattern.Pattern;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Sign {
	
	public  static final String         NAME = "sign";
	public  static final int              ID = 340224; //Strings.hash( NAME )
	public  static final String USER_DEFINED = "OTF"; // concept name for signs created on-the-fly

	public Sign() {super();}
	public Sign( Frag  patte  ) {this(); pattern( patte );}
	public Sign( String prefix ) {this( new Frag( prefix ));}
	public Sign( String prefix, Frag variable, String postfix ) {
		this( variable.prefix( prefix ).postfix( postfix ));
	}
	public Sign( String prefix1, Frag variable1,
	             String prefix2, Frag variable2 )
	{	this();
		pattern( variable1.prefix( prefix1 ));
		pattern( variable2.prefix( prefix2 ));
	}
	
	/** ------------------------------------------------------------------------
	 *  Member - Pattern - SYMBOL
	 */
	private Pattern pattern = new Pattern();
	public  Pattern pattern() {return pattern;}
	public  Sign    pattern( String s ) {pattern = new Pattern(s); return this;}
	public  Sign    pattern( Pattern ta ) {pattern = ta; return this;}
	public  Sign    pattern( Frag child ) {
		if (!child.isEmpty())
			pattern.add( child );
		return this;
	}
	public  Sign   pattern( String prefix, String name ) {
		pattern( new Frag( prefix, name ));
		return this;
	}
	public  Sign   pattern( String prefix, String name, String postfix ) {
		pattern( new Frag( prefix, name, postfix ));
		return this;
	}
	public  Sign   split( String word ) {pattern( pattern.split( word )); return this;} 

	/**
	 * Sign Complexity, used in sign ordering, is the complexity of its pattern.
	 */
	public int complexity() {
		return pattern.cplex(
					concept().equals( USER_DEFINED )
			   );
	}
	
	/* ------------------------------------------------------------------------
	 * Member - Intentions - THOUGHTS and REFERENCES
	 */
	private Intentions intentions = new Intentions();
	public  Intentions intentions() {return intentions;}
	public  Sign       intentions( Intentions is ) {intentions = is; return this;}
	public  Sign       insert( Intention intent ) {intentions.insert( intent ); return this;}
	public  Sign       append( Intention intent ) {intentions.add( intent ); return this;}
	public  Sign       append( int type, String pattern ) {intentions.append( new Intention( type, pattern )); return this;}
	
	/* ------------------------------------------------------------------------
	 *  Member Concept - name of its repertoire
	 */
	private String  concept = "";
	public  String  concept() { return concept; }
	public  Sign    concept( String name ) { concept = name; return this; }
	
	/* ------------------------------------------------------------------------
	 * Members - spatio-temporal booleans
	 */
	private boolean temporalSet = false;
	private boolean temporal = false;
	public  void    temporalIs( boolean b ) {temporal = b; temporalSet = true;}
	public  boolean isTemporal() {
		if (!temporalSet && !concept.equals( "" )) {
			temporal = Temporal.isConcept( concept );
			temporalSet = true;
		}
		return temporal;
	}
	private boolean spatialSet = false;
	private boolean spatial = false;
	public  boolean isSpatial() {
		if (!spatialSet && !concept.equals( "" )) {
			spatial = Where.isConcept( concept );
			spatialSet = true;
		}
		return spatial;
	}

	/* ------------------------------------------------------------------------
	 * Print routines --- in various formats ----------------------------------
	 */
	public String toXml( int n, long complexity ) {
		String ind = Audit.indent();
		return "<"+ NAME
				+" "+ Attribute.asString( "nth" , ""+n )
				+" "+ Attribute.asString( "complexity", ""+complexity )
				+" "+ Attribute.asString( "repertoire", concept())
				+    (isTemporal()?" "+Attribute.asString( "temporal", "true"):"")
				+ intentions.toXml() +">\n"
				+ ind + ind + pattern().toString() + "</"+ NAME +">";
	}
	public String toStringIndented( boolean auditIndents ) {
		return Audit.indent() + toString(auditIndents);
	}
	public String toString( boolean auditIndents ) {
		return "On \""+ pattern().toString()+ "\""
				+ intentions.toStringIndented( auditIndents ) +".";
	}
	public String toString() {return toString( false );}
	
	public String  toLine() {return toString( false ) + "\n";}
	public boolean toFile( String fname ){return Fs.stringAppendFile( fname, toLine());}
	public void    toFile() {Fs.stringToFile( pattern.toFilename(), toLine());}

	/*  -----------------------------------------------------------------------
	 *  'Sign' commands are fielded by 'spoken' SignBuilder factory, to
	 *  work upon the sign under construction.
	 */
	public static Strings perform( Strings args ) {
		
		if (args.isEmpty())
			return new Strings( Response.notOkay().toString());
		
		return Spoken.perform( args );
}	}
