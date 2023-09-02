package org.enguage.util.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.enguage.sign.symbol.reply.Response;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;
import org.enguage.util.sys.Fs;

public class Http implements AutoCloseable {

	public  static final int      ID = 154664;
	private static final Audit audit = new Audit("Http");
	
	private HttpURLConnection connection;
	private HttpURLConnection connection() {return connection;}

	public Http( String url ) {
		try {
			// Create a URL object with the target endpoint
			URL u = new URL( url );
	
			// Open a connection to the URL
			connection = (HttpURLConnection) u.openConnection();
			connection.setRequestMethod("GET");
			
		} catch (IOException e) {
			e.printStackTrace();
	}	}
	
	public void close() {
		connection().disconnect();
	}
	// ************************************************************************
	
	public int responseCode() throws IOException {
		return connection().getResponseCode();
	}
	public String response() {
		StringBuilder response = new StringBuilder();
		// Read the response
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader( connection.getInputStream() )))
		{
			String line;
			while (null != (line = reader.readLine()))
				response.append( line+"\n" );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response.toString();
	}
	
	private static String cache = Fs.root()+"wiki"+File.separator;
	private static String url = "https://en.wikipedia.org/w/index.php?title=";
	public  static void   url( String u ) {url=u;}

	public static Strings interpret( Strings cmds ) {
		audit.in( "interprtet", "cmds="+ cmds.toString(Strings.DQCSV) );
		Strings rc = Response.Fail;
		String cmd = cmds.remove(0);
		
		String location = cmds.remove(cmds.size()-1);
		if (!location.equals("wikipedia"))
			Audit.log("wrong location: "+ location );
		
		String locator = cmds.remove(cmds.size()-1);
		if (!locator.equals("from"))
			Audit.log("wrong locator: "+ locator );
		
		if (cmd.equals( "get" )) {
			// "nelson mandela" => "Nelson_Mandela"
			String title = cmds.normalise()      // => ["nelson", "mandela"]
					.capitalise()                // => ["Nelson", "Mandela"]
					.toString( Strings.UNDERSC );// => "Nelson_Mandela"
			
			if (new File( cache+title ).exists()) {
				rc = new Strings().append( "\""+ cache + title +"\"" );
				audit.debug( "Found: "+ rc );

			} else {
				try (Http http = new Http( url+title )) {
					
					audit.debug( "Downloading: "+ cache+title );
					if (http.responseCode() == 200) {
						Fs.stringToFile(
								cache+title,
								http.response()
						);
						rc = new Strings().append( "\""+ cache + title +"\"" );
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
		}	}
		audit.out( rc.toString() );
		return rc;
	}
	// ************************************************************************
	// Test code...
	//
	public static void main(String[] args) {
		// default url -- SAP graph
		String defaultUrl = "http://localhost:3004/sap.graph/";
		String defaultQuery = "SalesQuote?$top=2";
		
		String query = defaultQuery;
		if (args.length > 0)
			query = args[ 0 ];
		
		String url = defaultUrl + query;
		System.out.println( "Trying: "+ url );
		
		try (Http http = new Http( url )) {
			// Get the response code
			int responseCode = http.responseCode();
			System.out.println("Response Code: " + responseCode);

			System.out.println("Response: " + http.response() );
			
		} catch (IOException e) {
			e.printStackTrace();
		}
}	}
