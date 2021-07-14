package com.github.vincemann.xmpp.client;

import com.github.vincemann.xmpp.client.input.ChatLineConsumer;
import com.github.vincemann.xmpp.client.input.InputHandler;
import com.github.vincemann.xmpp.client.input.MenuLineConsumer;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

public class BiDirChatManager  implements IncomingChatMessageListener {
    private static final String CLOSE_CHAT_MSG = "user closed chat";

    private Boolean startedChat = Boolean.FALSE;
    private EntityBareJid chatPartner = null;
    private Chat chat = null;
    private ChatManager chatManager;
    private String domain;
    private InputHandler inputHandler;

    BiDirChatManager(ChatManager chatManager, String domain, InputHandler inputHandler) {
        this.chatManager = chatManager;
        this.domain = domain;
        this.inputHandler = inputHandler;
    }

    public void startChat(String user){
        String jidString = user + "@" + domain;
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(jidString);
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
        this.chatPartner = jid;
        System.out.println("Creating Chat with: " + jid.toString());
        this.startedChat = Boolean.TRUE;
        this.chat = chatManager.chatWith(jid);
        inputHandler.switchLineConsumer(new ChatLineConsumer(this));
    }

    public void send(String msg){
        try {
            chat.send(msg);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void closeChat(boolean initiated){
        if (initiated){
            try {
                chat.send(CLOSE_CHAT_MSG);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("chat closed");
        inputHandler.switchLineConsumer(new MenuLineConsumer(this));
        chatPartner = null;
        startedChat = Boolean.FALSE;
    }

    @Override
    public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
        if (startedChat) {
//                    System.out.println("Ignoring msg bc started chat and already logged by other listener");
            return;
        } else {
            // todo implement chatting boolean and waiting queue if another user tries to chat with user, while
            if (chatPartner == null || !chatPartner.equals(entityBareJid)) {
                System.out.println("New Chat Partner: " + entityBareJid);
                this.chatPartner = entityBareJid;
                this.chat=chat;
//                        waitForFreeChat();
                inputHandler.switchLineConsumer(new ChatLineConsumer(this));
            }
            if (message.getBody().contains(CLOSE_CHAT_MSG)){
                closeChat(false);
                return;
            }
        }
    }


    public Boolean getStartedChat() {
        return startedChat;
    }

    public EntityBareJid getChatPartner() {
        return chatPartner;
    }

    public Chat getChat() {
        return chat;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public String getDomain() {
        return domain;
    }

}