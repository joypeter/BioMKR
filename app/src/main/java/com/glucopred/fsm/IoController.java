package com.glucopred.fsm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by peter on 4/6/16.
 */
public class IoController {
    public interface InputEventHandler {
        void onInputEvent(String s);
    }

    private InputEventHandler eventHandler;

    public void setEventHandler(InputEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    public void init() {
        final InputStream in = System.in;

        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = new byte[255];
                try {
                    while (true) {
                        int bytes = in.read(buffer);
                        String s = new String(buffer, 0, bytes, Charset.defaultCharset()).replaceAll("[\\D\\W]", "");
                        eventHandler.onInputEvent(s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        worker.start();
    }

    public void println(String s) {
        System.out.println(s);
    }
}
