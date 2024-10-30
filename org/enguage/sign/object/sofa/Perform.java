package org.enguage.sign.object.sofa;

import org.enguage.repertoires.Repertoires;
import org.enguage.repertoires.concepts.Concept;
import org.enguage.repertoires.concepts.Similarity;
import org.enguage.sign.Assets;
import org.enguage.sign.Sign;
import org.enguage.sign.interpretant.Response;
import org.enguage.sign.object.Numeric;
import org.enguage.sign.object.Temporal;
import org.enguage.sign.object.Variable;
import org.enguage.sign.object.expr.Function;
import org.enguage.sign.object.list.Item;
import org.enguage.sign.object.list.Items;
import org.enguage.sign.object.list.Transitive;
import org.enguage.sign.symbol.config.Colloquial;
import org.enguage.sign.symbol.config.Plural;
import org.enguage.sign.symbol.pronoun.Pronoun;
import org.enguage.sign.symbol.where.Where;
import org.enguage.util.attr.Attribute;
import org.enguage.util.attr.Context;
import org.enguage.util.audit.Audit;
import org.enguage.util.http.Http;
import org.enguage.util.http.InfoBox;
import org.enguage.util.http.SapGraph;
import org.enguage.util.strings.Strings;

public class Perform {
	
	public  static final String  S_IGNORE  = "";
	
	private Perform() {}
	
	private static final Audit audit = new Audit( "Sofa" );
	
	public  static Strings interpret( Strings a ) {
		if (a.isEmpty()) {
			audit.error("perform interpret: not enough params: "+ a.toString());
		} else {
			/* Tags.matchValues() now produces:
			 * 		["a", "b", "c='d'", "e", "f='g'"]
			 * Perform.interpret() typically deals with:
			 * 		["entity", "get", "martin", "name"]
			 * 		["colloquial", "both", "'I have'", "'I've'"]
			 */
			String  type = a.remove( 0 );
			switch (Strings.hash( type )) {
				case Item       .ID: return        Item.perform( Attribute.expand23( a ));
				case Link       .ID: return        Link.perform(                     a  );
				case Http       .ID: return        Http.perform( Attribute.expand(   a ));
				case Sign       .ID: return        Sign.perform( Attribute.expand(   a ));
				case Audit      .ID: return       Audit.perform( Attribute.expand(   a ));
				case Items      .ID: return       Items.perform(                     a  );
				case Value      .ID: return       Value.perform( Attribute.expand23( a ));
				case Where      .ID: return       Where.perform( Attribute.expand23( a ));
				case Plural     .ID: return      Plural.perform( Attribute.expand23( a ));
				case Assets     .ID: return      Assets.perform( Attribute.expand(   a ));
				case Entity     .ID: return      Entity.perform( Attribute.expand23( a ));
				case Concept    .ID: return     Concept.perform( Attribute.expand23( a ));
				case InfoBox    .ID: return     InfoBox.perform( Attribute.expand(   a ));
				case Pronoun    .ID: return     Pronoun.perform( Attribute.expand23( a ));
				case Context    .ID: return     Context.perform( Attribute.expand23( a ));
				case Numeric    .ID: return     Numeric.perform( Attribute.expand23( a ));
				case Overlay    .ID: return     Overlay.perform( Attribute.expand23( a ));
				case Temporal   .ID: return    Temporal.perform( Attribute.expand23( a ));
				case Function   .ID: return    Function.perform( Attribute.expand23( a ));
				case Variable   .ID: return    Variable.perform( Attribute.expand23( a ));
				case SapGraph   .ID: return    SapGraph.perform( Attribute.expand23( a ));
				case Similarity .ID: return  Similarity.perform( Attribute.expand23( a ));
				case Colloquial .ID: return  Colloquial.perform(                     a  );
				case Transitive .ID: return  Transitive.perform( Attribute.expand23( a ));
				case Repertoires.ID: return Repertoires.perform(                     a  );
				default :
					audit.error( "Perform: "+ type +".ID should be set to "+ Strings.hash( type ));
					return Response.notOkay();
		}	}
		return Response.notOkay();
}	}
