package org.enguage.object.numeric;

import java.util.ListIterator;

import org.enguage.Enguage;
import org.enguage.object.Attribute;
import org.enguage.object.Variable;
import org.enguage.object.space.Overlay;
import org.enguage.util.Audit;
import org.enguage.util.Number;
import org.enguage.util.Shell;
import org.enguage.util.Strings;
import org.enguage.vehicle.Reply;

public class Function {
	
	static public  String  NAME = "function";
	static private Audit  audit = new Audit( "Function" );
	
	public Function( String nm ) { name = nm; } // find
	public Function( String nm, Strings params, String body ) {
		this( nm );
		lambda = new Lambda( nm, params, body );
	}
	
	private String   name;
	public  String   name() { return name; }
	
	private Lambda lambda = null;
	
	public  String toString() { return "FUNCTION "+ name + lambda==null ? "<null>" : lambda.toString();}
	
	static public String create( String name, Strings args ) {
		// args=[ "x and y", "/", "body='x + y'" ]
		audit.in( "create", "name="+ name +", args="+ args.toString( Strings.DQCSV ));
		Strings params = new Strings();
		ListIterator<String> si = args.listIterator();
		Strings.getWords( si, "/", params ); // added in perform call
		params = params.normalise(); // "x and y" => "x", "and", "y"
		params.remove( params.size()-1 ); // remove '/'
		if (params.size() > 2)
			params.remove( params.size()-2 ); // remove 'and'
		
		new Function( name, params, Attribute.getAttribute( si ).value() );
		
		return audit.out( Shell.SUCCESS );
	}
	private static Function getFunction( String name, Strings values ) {
		// ("area", ["height", "width"])
		audit.in( "getFunction", name +", "+ values.toString("[", ", ", "]"));
		Function fn = new Function( name );
		fn.lambda = new Lambda( name, values );
		if (fn.lambda.body().equals( "" ))
			fn = null;
		return (Function) audit.out( fn );
	}
	static private Strings substitute( String function, Strings argv ) {
		// takes: "sum", ["3", "4"]
		audit.in( "substitue", "Function="+ function +", argv="+ argv.toString( "[",",","]") );
		Strings ss = null;
		Function f = getFunction( function, argv ); // e.g. sum 2 => sum/x,y.txt
		if (f != null)
			ss = new Strings( f.lambda.body() )
							.substitute(
									new Strings( f.lambda.sig() ), // formals
									argv );
		return audit.out( ss );
	}
	static public String evaluate( String name, Strings argv ) {
		audit.in(  "evaluate", argv.toString( Strings.DQCSV ));
		String  rc = Reply.dnk(); //argv.toString();
		Strings ss = substitute( name, argv.divvy( "and" ));
		if (ss != null) {
			rc = Number.getNumber( ss.listIterator()).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = argv.toString();
		}
		return audit.out( rc );
	}
	static public String interpret( Strings argv ) {
		audit.in( "interpret", argv.toString( Strings.DQCSV ));
		String  rc = Shell.FAIL;
		if (argv.size() >= 2) {
			String      cmd = argv.remove( 0 ),
			       function = argv.remove( 0 );
			argv = argv.normalise().contract( "=" );
	
			if (cmd.equals( "create" )) {
				// [function] "create", "sum", "a", "b", "/", "body='a + b'"
				rc = create( function, argv );
				
			} else if (cmd.equals( "evaluate" )) {
				// [function] "evaluate", "sum", "3", "and", "4"
				rc = evaluate( function, argv );
				
			} else
				audit.ERROR( "Unknown "+ NAME +".interpret() command: "+ cmd );
		}
		return audit.out( rc );
	}
	//crate + query
	static private void test( String fn, String formals, String body, String actuals ) {
		audit.log( "The "+ fn +" of "+ formals +" is "+ body );
		interpret( new Strings( "create "+ fn +" "+ formals +" / body='"+ body +"'" ));
		
		audit.log( "What is the "+ fn +" of "+ actuals );
		
		String eval = interpret( new Strings("evaluate "+ fn +" "+ actuals ));
		audit.log( eval.equals( Reply.dnk()) ?
			eval :
			"The "+ fn +" of "+ actuals +" is "+ eval +"\n" );
	}
	static public void main( String args[]) {
		Enguage.e = new Enguage();
		Overlay.Set( Overlay.Get());
		if (!Overlay.autoAttach())
			audit.ERROR( "Ouch!" );
		else {
			Variable.set( "x", "1" );
			Variable.set( "y", "2" );
			//Audit.traceAll( true );
			audit.debug( "matching passes!" );
			test( "sum", "a and b", "a + b", "3 and 2" );
			//Audit.traceAll( false );
			test( "sum", "a b c and d", "a + b + c + d", "4 and 3 and 2 and 1" );
			test( "sum", "1", "1", "6" );
			test( "factorial", "1", "1", "6" );
}	}	}
