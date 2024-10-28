package org.enguage.sign.interpretant.intentions;

import org.enguage.sign.interpretant.Intention;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.sofa.Perform;
import org.enguage.sign.symbol.when.Moment;
import org.enguage.sign.symbol.when.When;
import org.enguage.util.attr.Attribute;
import org.enguage.util.strings.Strings;

public class SofaPerform {
	
	private SofaPerform() {}
	
	private static String formatAnswer( String answer ) {
		if (Moment.valid( answer )) // 88888888198888 -> 7pm
			return new When( answer ).rep( Response.dnk() ).toString();
		else if (answer.equals( Response.notOkay().toString() ))
			return Response.notOkay().toString();
		else if (answer.equals( Response.okay().toString() ))
			return Response.okay().toString();
		return answer;
	}
	
	private static final String ARGS = "args=";
	
	public  static void perform( Reply r, Strings values) {perform( r, values, false );}
	public  static void perform( Reply r, Strings values, boolean ignore ) {
		//audit.in( "perform", "value='"+ value +"', ignore="+ (ignore?"yes":"no"))
		Strings cmd = Intention.format( values, r.answer(), true ); // DO expand, UNIT => unit='non-null value'
		
		// In the case of vocal perform, value="args='<commands>'" - expand!
		if (cmd.size()==1 &&
			cmd.get(0).length() > ARGS.length() &&
				cmd.get(0).substring(0,ARGS.length()).equals( ARGS ))
		{
			cmd=new Strings( new Attribute( cmd.get(0) ).value());
		}
		
		Strings rawAnswer = Perform.interpret( new Strings( cmd ));
		
		if (!ignore) {
			String method = cmd.get( 1 );
			if (rawAnswer.isEmpty() &&
				(method.equals( "get" ) ||
				 method.equals( "getAttrVal" )) )
			
				r.toIdk();
				
			else {
				// Methods should return "OK", "Sorry" etc.
				String answer = formatAnswer( rawAnswer.toString());
				r.answer( answer );
				r.type( Response.typeFromStrings(
						new Strings( answer )
				      )                         );
		}	}
}	}
