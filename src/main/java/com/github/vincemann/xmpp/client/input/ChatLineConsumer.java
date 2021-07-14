package com.github.vincemann.xmpp.client.input;

import com.github.vincemann.xmpp.client.BiDirChatManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;

public class ChatLineConsumer implements LineConsumer {
    private BiDirChatManager biDirChatManager;

    public ChatLineConsumer(BiDirChatManager biDirChatManager) {
        this.biDirChatManager = biDirChatManager;
    }

    @Override
    public void consume(String line) {
        if (line.equals("q")) {
            biDirChatManager.closeChat(true);
            return;
        }
        System.out.println("Sending msg: " + line);
        biDirChatManager.send(line);
    }
}