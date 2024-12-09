package opt.api.actions.handlers;

import java.util.Map;

import org.enguage.Enguage;
import org.enguage.util.strings.Strings;
import org.enguage.util.strings.Terminator;

import opt.api.actions.ActionHandler;
import opt.api.utils.http.HttpResponse;


public class Interpret extends ActionHandler {

    protected HttpResponse post( Map<String, String> head, Map<String, String> body ) {
        String utterance = body.get("utterance");
        String sessionId = body.get("sessionId");

        String utteranceStrippedOfTerminator = Terminator.stripTerminator(
            new Strings( utterance.split(" ") ).normalise()
        ).toString();

        String reply = Enguage.get().mediateMulti(sessionId, utteranceStrippedOfTerminator);

        return new HttpResponse(reply);
    }
    
}
