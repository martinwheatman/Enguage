package com.yagadi;

import org.enguage.Enguage;

public class Interpreter implements Runnable {

    private MainActivity context;
    private String utterance;

    private Callback callback;

    public Interpreter(MainActivity m, String u, Callback cb) {
        context = m;
        utterance = u;
        callback = cb;
    }

    public void run() {
        String location = context.getExternalFilesDir( null ).getPath();
        Enguage interpreter = new Enguage( location );
        String reply = interpreter.mediate( utterance );
        callback.callback( reply );
    }
}
