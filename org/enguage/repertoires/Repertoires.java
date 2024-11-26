package org.enguage.repertoires;

import java.util.TreeSet;

import org.enguage.repertoires.concepts.Autoload;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.sign.Config;
import org.enguage.sign.Sign;
import org.enguage.sign.Signs;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.interpretant.intentions.Engine;
import org.enguage.sign.interpretant.intentions.Reply;
import org.enguage.sign.object.Variable;
import org.enguage.sign.symbol.Utterance;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Repertoires {
	
	private Repertoires() {}
	
	public  static final String NAME = "repertoires";
	public  static final int      ID = 216434732;
	private static final Audit audit = new Audit( NAME );
		
	private static Signs signs = new Signs( "user" );
	public  static Signs signs() {return signs;}
	
	private static Signs engine = new Signs( Engine.NAME ).add( Engine.commands() );
	private static Signs engine() {return engine;}
	
	// entry point for Enguage, re-entry point for Intentions
	public static Reply mediate( Utterance u ) {
		
		// not sure why blank mediations are being requested?
		if ( "".equals( u.toString() )) return new Reply();
		
		audit.in( "mediate", "\""+ u.toString() +"\"" );
		
		Autoload.autoload( u.representamen() ); // unloaded up in Enguage.interpret()
		
		/* At this point we need to rebuild utterance with the (auto)loaded concept,
		 * with any colloquialisms it may have loaded...
		 * Needs to be expanded in case we've expanded any parameters (e.g. whatever)
		 */
		u = new Utterance( u.expanded() );
		
		Reply r = signs.mediate( u );
		if (Response.Type.DNU == r.type())
			r = engine().mediate( u );
	
		audit.out( r );
		return r;
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	private static Strings doShow( Strings cmds ) {
		Strings rc = Response.okay();
		
		String name = cmds.remove( 0 );
		if (name.equals("signs") ||
			name.equals("user"))
			
			signs.show();
			
		else if (name.equals( Engine.NAME ))
			engine().show();
			
		else if (name.equals( "all" )) {
			engine.show();
			signs.show();
			
		} else
			rc = Response.notOkay();
		
		return rc;
	}
	
	private static Strings doFind( Strings cmds ) {
		Audit.log( "in find: "+ cmds.toString( Strings.SQCSV ));
		String utterance = Attribute.getValue( ""+cmds );
		return signs.find( new Strings( utterance ));
	}
	
	public static Strings perform( Strings cmds ) {
		Strings rc = Response.notOkay();
		audit.in( "perform", "cmds="+ cmds );
		if (!cmds.isEmpty()) {
			String cmd = cmds.remove( 0 );
			
			if (cmd.equals("show"))
				rc = doShow( cmds );
				
			else if (cmd.equals( "variable" ))
				rc = Variable.perform( new Strings( "show" ));
				
			else if (cmd.equals( "find" ))
				rc = doFind( cmds );
				
			else if (cmd.equals( "list" ))
				/* This becomes less important as the interesting stuff 
				 * becomes auto loaded.  Don't want to list all repertoire
				 * once the repertoire base begins to grow?
				 * May want to ask "is there a repertoire for needs" ?
				 */
				rc = new Strings(
						"loaded repertoires include "+
						new Strings( 
								(TreeSet<String>)Concept.loaded()
						).toString( Config.andListFormat() )
				);
		}
		return audit.out( rc );
}	}
