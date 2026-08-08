package bgu.spl.mics.application.services;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.messages.TickBroadcast;

public class Open implements Runnable {
    @Override
    public void run() {
        (MessageBusImpl.getInstance()).sendBroadcast(new TickBroadcast());
    }
}
