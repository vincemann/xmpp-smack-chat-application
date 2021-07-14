package com.github.vincemann.xmpp.client;

import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;

public class LoggingIncomingMsgListener implements IncomingChatMessageListener {
    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
        System.err.println("Received message: "
                + (message != null ? message.getBody() : "NULL" + " | from " + entityBareJid));
    }
}
