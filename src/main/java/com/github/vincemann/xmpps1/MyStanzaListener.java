package com.github.vincemann.xmpps1;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;

public class MyStanzaListener implements StanzaListener {


    public void processStanza(Stanza packet) throws SmackException.NotConnectedException, InterruptedException, SmackException.NotLoggedInException {
        System.err.println("New Stanza coming in: "+ packet.toString());
    }
}
