package eu.alehem.tempserver.remote;

import java.util.Set;

public class TempSender implements Runnable {

    private TempQueue queue = TempQueue.getInstance();
    private final int MAX_TO_SEND = 100;

    @Override
    public void run() {
        //Strategy: Empty Queue, then start to empty database
        //Get up to 100 entries from queue
        //Send. If success, pop from queue. If fail write to db
    }

    boolean sendToServer(Set<Temperature> temperatures) {
        return false;
    }

}
