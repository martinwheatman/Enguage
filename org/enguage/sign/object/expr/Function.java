package org.enguage.sign.object.expr;

import java.util.ListIterator;

import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.sofa.Overlay;
import org.enguage.sign.symbol.number.Number;
import org.enguage.util.attr.Attribute;
import org.enguage.util.audit.Audit;
import org.enguage.util.strings.Strings;

public class Function {
	
	public static  final String NAME = "function";
	public static  final int      ID = 81133373; //Strings.hash( NAME );
	private static       Audit audit = new Audit( "Function" );
	
	public Function( String nm ) { name = nm; } // find
	public Function( String nm, Strings params, String body ) {
		this( nm );
		lambda = new Lambda( this, params, body );
	}
	
	private String   name = "";
	public  String   name() { return name; }
	
	private Lambda lambda = null;
	
	private static Strings create( String name, Strings args ) {
		// args=[ "x and y", "/", "body='x + y'" ]
		audit.in( "create", "name="+ name +", args="+ args );
		Strings params = new Strings();
		ListIterator<String> si = args.listIterator();
		
		Strings.getWords( si, "/", params ); // added in perform call
		params = params.normalise(); // "x and y" => "x", "and", "y"
		params.remove( params.size()-1 ); // remove '/'
		if (params.size() > 2)
			params.remove( params.size()-2 ); // remove 'and'
		
		new Function(
				name,
				params,
				new Attribute( si.hasNext() ? si.next() : "" ).value()
		);
		
		audit.out();
		return Response.okay();
	}
	public String toString() {return name + (lambda==null ? "<noLambda/>" : lambda.toString());}
	
	private static Function getFunction( String name, Strings actuals ) {
		//audit.in( "getFunction", name +", "+ actuals.toString("[", ", ", "]"));
		Function fn = new Function( name );
		fn.lambda = new Lambda( fn, actuals ); // this is a 'find', body="" == !found
		if (fn.lambda.body().equals( "" )) {
			audit.debug( "FUNCTION: no body found for "+ actuals +"/"+ name );
			fn = null;
		}
		return fn; //(Function) audit.out( fn );
	}
	private static Strings substitute( String function, Strings actuals ) {
		//audit.in( "substitute", "Function="+ function +", argv="+ actuals.toString( Strings.DQCSV ));
		Strings ss = null;
		
		// resolve actual expressions into value
		Strings tmp = new Strings();
		for (String a : actuals) {
			String b = ""+new Number( a ).valueOf();
			if (b.equals("not a number"))
				tmp.add( a );
			else
				tmp.add( b );
		}
		actuals = tmp;
		
		Function f = getFunction( function, actuals );
		if (f != null)
			ss = new Strings( f.lambda.body() )
					.substitute(
						new Strings( f.lambda.signature() ), // formals
						actuals.derefVariables() );
		return ss; //audit.out( ss );
	}
	private static Strings evaluate( String name, Strings argv ) {
		//audit.in( "evaluate", "name="+ name +", args='"+ argv +"'" );
		Strings  rc = Response.dnk();
		Strings ss = substitute( name, argv.divvy( "and" ));
		if (ss != null) {
			rc = new Number( ss.listIterator() ).valueOf();
			if (rc.equals( Number.NotANumber ))
				rc = Response.dnk();
		}
		return rc; //audit.out( rc );
	}
	public static Strings interpret( String arg ) { return perform( new Strings( arg ));}
	public static Strings perform( Strings argv ) {
		//audit.in( "interpret", argv.toString( Strings.DQCSV ));
		Strings rc = Response.notOkay();
		if (argv.size() >= 2) {
			String      cmd = argv.remove( 0 ),
			       function = argv.remove( 0 );
			argv = argv.normalise().contract( "=" );
	
			if (cmd.equals( "create" ))
				// [function] "create", "sum", "a", "b", "/", "body='a + b'"
				rc = create( function, argv );
				
			else if (cmd.equals( "evaluate" ))
				// [function] "evaluate", "sum", "3", "and", "4"
				rc = evaluate( function, argv );
			
			else
				audit.error( "Unknown "+ NAME +".interpret() command: "+ cmd );
		}
		return rc; //(Strings) audit.out( rc );
	}
	// === test code below! ===
	private static void testCreate( String fn, String formals, String body ) {
		audit.debug( "The "+ fn +" of "+ formals +" is "+ body );
		perform(	new Strings( "create "+ fn +" "+ formals +" / "+
							     Attribute.asString( "body", body )
				 )			   );
	}
	private static void testQuery( String fn, String actuals ) {
		audit.debug( "What is the "+ fn +" of "+ actuals );
		Strings eval = perform( new Strings("evaluate "+ fn +" "+ actuals ));
		audit.debug( eval.equals( new Strings( Response.dnk())) ?
			eval.toString() : "The "+ fn +" of "+ actuals +" is "+ eval.toString() +"\n" );
	}
	public static void main( String args[]) {
		Audit.resume();
		Overlay.set( Overlay.get());
		Overlay.attach( NAME );
		
		Response.dnk( "I do not know\n" );
		testQuery(  "sum", "1 , 1" ); // error!
		
		testCreate( "sum", "a and b", "a + b" );
		testQuery(  "sum", "3 and 2" );
		
		testCreate( "sum", "a b c and d", "a + b + c + d" );
		testQuery(  "sum", "4 and 3 and 2 and 1" );
		
		audit.debug( "setting x to 1" );
		Variable.set( "x", "1" );
		audit.debug( "setting y to 2" );
		Variable.set( "y", "2" );
		testQuery(  "sum", "x and y" );
		
		testCreate( "factorial", "1", "1" );
		testQuery(  "factorial", "1" );
		//testQuery(  "factorial", "4" );
		audit.debug( "PASSED" );
}	}
