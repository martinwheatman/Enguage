package opt.web.actions;

import org.enguage.Enguage;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

import opt.web.WebRequest;

public class Eng {
	public static final String engPage( String username ) {
		return	"<fieldset>\n"
				+ "		<legend>Say:</legend>\n"
				+ "		<form action='/enguage'>\n"
				+ "			<label for='utterance'>Utterance:</label>\n"
				+ "			<input type='text' id='utterance' placeholder='utterance'>&nbsp;\n"
				+ "			<input type='button' id='say' onclick='i_say()' value='Say'>\n" 
				+ "		</form>\n"
				+ " 	<p id='reply'></p>\n"
				+ "	</fieldset>\n"
				+ "<script>\n"
				+ "	function i_say() {\n"
				+ "		//alert( 'hello' );\n"
				+ "		var request = '/enguage'"
				+ "					+ '?uid=" + username +"'"
				+ "					+ '&utterance='+ document.getElementById('utterance').value;\n"
				+ "		var xhttp = new XMLHttpRequest();\n"
				+ "		xhttp.onreadystatechange = function() {\n"
				+ "			if (this.readyState == 4 && this.status == 200) {\n"
				+ "				document.getElementById('utterance').value = '';\n"
				+ "				document.getElementById('reply').innerHTML = this.responseText;\n"
				+ "			}\n"
				+ "		};\n"
				+ "		xhttp.open('GET', request, true);\n"
				+ "		xhttp.send();\n"
				+ "	}\n"
				+ "</script><br>\n";
	}
	private static String viaForm( String[] params ) {
		return "<center><strong>"
				+ Enguage.get().mediateMulti(
						params[ 0 ].split( "=" )[ 1 ],
						Terminator.stripTerminator(
								new Strings( params[ 1 ].split( "=" )[ 1 ].split( "%20" ))
										.normalise() // this will separate.the terminator
						).toString()
				  )
				+ "</strong></center></P>";
	}
	private static String viaUrl( String uid, String[] params ) {
		String reply =  Enguage.get().mediateMulti(
							uid,
							new Strings(
									params[ 1 ].split( "=" )[ 1 ].split( "%20" )
							).toString()
						);
		return reply;
	}
	public static String getReply( WebRequest r, String cmd, String[] params ) {
		String reply = "";
		if (cmd.equals( "enguage" )) {
			//URLDecoder.decode( in.nextLine(), "UTF-8" )
			reply = WebRequest.validAttrs( params, 2 ) ?
						Eng.viaForm( params )
						: ("<center>"
							+ "(Try setting the value of utterance to something)"
							+ "</center>");
				
		} else if (cmd.equals( "Enguage" )) {
			
			if (WebRequest.validAttrs( params, 2 ) && r.uid().length() > 0)
				reply = Eng.viaUrl( r.uid(), params );
		}
		return reply;
}	}
