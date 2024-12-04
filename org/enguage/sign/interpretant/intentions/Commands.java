package org.enguage.sign.interpretant.intentions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.enguage.sign.Assets;
import org.enguage.sign.Config;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Variable;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Commands {
	private static Audit audit = new Audit( "Commands" );
	
	private String   command = "";
	public  Commands command( String c ) {
		command = Variable.deref( Strings.getStrings( c ))
				.replace( new Strings( "SOFA" ), new Strings( java() ))
				.contract( "/" ) // this gets undone!
				.linuxSwitches()
				.toString();
		return this;
	}
	
	private static String classpath = "";
	public  static String classpath() { return classpath; }
	public  static void   classpath(String cp) { classpath = cp; }
	
	private static String java = "";
	public  static String java() { return java; }
	public  static void   java(String cp) { java = cp; }
	
	private static String shell = "/bin/bash";
	public  static void   shell( String sh ) { shell = sh; }
	public  static String shell() { return shell; }
	
	private Reply runResult( int rc, Strings results ) {
		//audit.in( "runresult", "rc="+ rc +", result=["+ results +"]")
		Reply r = new Reply();
		
		for (String result : results)
	 		r.answer( result );
		/*
	 	 * We have no control over what text the command sends back.
	 	 * A zero result is success.
	 	 * Passing back a non-zero result is a failure.
	 	 * No room for other response types
	 	 */
	 	r.type( rc == 0 ? Response.Type.OK : Response.Type.SOZ );
	 	r.format( rc == 0 ? "ok, ..." : "sorry, ..." );
		
		//audit.out( "run result: "+ r )
	 	return r;
	}
	
	public  Commands injectParameter( String runningAns ) {
		command = new Strings( command )
				.replace( Strings.ellipsis, runningAns )
				.replace( Config.placeholder(), runningAns )
				.toString();
		return this;
	}
	
	public Reply run() {
		Reply r = new Reply();
		Strings results = new Strings();

		// somehow '/' seem to get separated!!! 
		command = new Strings( command ).contract( "/" ).toString();	
		// benign for non-Android...
		if (Assets.context() != null && command.startsWith( "sbin/" ))
			command = Assets.path() + command;
				
		audit.debug( "running: "+ command );
		ProcessBuilder pb = new ProcessBuilder( "bash", "-c", command );
		
		try {
			Process p = pb.start();
			try (
				BufferedReader reader =
						new BufferedReader(
							new InputStreamReader(
									p.getInputStream()
						)	);
				BufferedReader  error =
						new BufferedReader(
								new InputStreamReader(
										p.getErrorStream()
						)		);
			) {
		
				String line;
				while ((line = reader.readLine()) != null)
					results.append( line );
				
				if (results.isEmpty())
					while ((line = error.readLine()) != null)
						results.append( line );
				
				r = runResult( p.waitFor(), results );
				
			} catch (Exception e) {
				Strings errString = new Strings();
				errString.add( "Command failed: "+ command );
				r = runResult( 1, errString  );
			}
		} catch (IOException iox) {
			Strings errString = new Strings();
			errString.add( "I can't run: "+ command );
			r = runResult( 1, errString );
		}
		return r;
	}
	
	public static Reply run( String cmd, String answer ) {
		return new Commands()
				.command( cmd )
				.injectParameter( answer )
				.run();
}	}
