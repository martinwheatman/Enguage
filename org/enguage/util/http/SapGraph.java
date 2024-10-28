package org.enguage.util.http;

import java.io.IOException;

import org.enguage.sign.interpretant.Response;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class SapGraph {

	public  static final int      ID = 27;
	private static final Audit audit = new Audit("SapGraph");
	
	private static String defaultUrl   = "http://localhost:3004/sap.graph/";
	private static String defaultQuery = "SalesQuote?$top=2";
	
	public  static Strings perform( Strings cmds ) {
		audit.in( "interprtet", "cmds="+ cmds.toString(Strings.DQCSV) );
		Strings rc = Response.notOkay();
		String cmd = cmds.remove(0);
		
		if (cmd.equals( "query" )) {
			try (Http http = new Http( defaultUrl )) {
				
				audit.debug( "creating: "+ defaultQuery );
				if (http.responseCode() == 200) {
					Fs.stringToFile(
							defaultQuery,
							http.response()
					);
					rc = new Strings().append( defaultQuery );
				}
				
			} catch (IOException e) {
				e.printStackTrace();
		}	}
		audit.out( rc.toString() );
		return rc;
	}
	public static void main( String[] args ) {
		Strings cmds = new Strings( "query nelson mandela" );
		Audit.log( perform( cmds ));
}	}
