package com.yagadi;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.enguage.util.audit.Audit;

// locally defined
import org.enguage.sign.Assets;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private static final int   REQUEST_SPEECH = 1;
    private static final Audit          audit = new Audit( "Enguage" );

    Interpreter thinker = null;

    public TextToSpeech tts = null;
    private boolean ttsInitialised = false;
    private boolean vocalised() {
        AudioManager am = (AudioManager)this.getSystemService( Activity.AUDIO_SERVICE );
        return ttsInitialised && AudioManager.RINGER_MODE_NORMAL == am.getRingerMode();
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    tts.setLanguage( Locale.getDefault() );
                }
            }
        });

        ImageButton btn_mic = findViewById(R.id.mic);
        btn_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        Assets.context( this );
    }

    public void onInit(int code) {
        if (TextToSpeech.SUCCESS == code) {
            ttsInitialised = true;
    }   }

    @Override
    public void onResume() {super.onResume();}

    private void promptSpeechInput() {
        Intent intent = new Intent( RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
//                "org.enguage.demo");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));

        try {
            startActivityForResult( intent, REQUEST_SPEECH );
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }    }

    private TextView createTv( String msg, int colour, boolean user ) {
        TextView tv = new TextView(getBaseContext());
        tv.setText( msg );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
                (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(10,10,10,10);
        params.gravity = Gravity.TOP | (user ? Gravity.START : Gravity.END);
        tv.setLayoutParams(params);

        tv.setTextColor(getResources().getColor(R.color.white));
        tv.setBackgroundColor( colour );
        tv.setPadding(40, 40, 40, 40);

        return tv;
    }

    public void think( String utterance ) {
        //display messages in IM format in linear layout
        final LinearLayout ll_messages = findViewById( R.id.ll_messages );
        ll_messages.addView( createTv( utterance,    getResources().getColor(R.color.colorPrimary), true ), 0);
        Log.e( ">>>>>>>>>>UTTERANCE>>> ", utterance);

        thinker = new Interpreter(this, utterance, new Callback() {
            public void callback( String r ) {
                runOnUiThread( new Runnable() {
                    @Override
                    public void run() {
                        reply(r);
                    }
                });
            }
        } );
        Thread thread = new Thread( thinker );
        thread.start();
    }
    public void reply( String reply ) {

        //Toast.makeText( getApplicationContext(), toSpeak, Toast.LENGTH_SHORT ).show();
        Log.e ( ">>>>>>>>>>>REPLY>>> ", reply );
        Audit.log("tts is NULL!" );

        //display messages in IM format in linear layout
        final LinearLayout ll_messages = findViewById( R.id.ll_messages );
        ll_messages.addView( createTv( reply, getResources().getColor(R.color.colorPrimaryDark), false), 0);

        if (null != tts) {
            tts.stop();
            tts.speak( reply, TextToSpeech.QUEUE_FLUSH, null );
        } else {
            Audit.log("tts is NULL!" );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_SPEECH:
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> saidArray =
                            data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
                    think( saidArray.get( 0 ));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }    }

    @Override
    public void onPause() {
        if (null != tts) tts.stop();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        if (null != tts) tts.shutdown();
        super.onDestroy();
    }
}
