package com.github.vincemann.xmpps1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.Scanner;

public class Application {

    //    public static final String XMPP_DOMAIN = "debian.local";
    private static final String OPEN_CHAT_MENU_OPTION = "chat";
    private static final String CLOSE_CHAT_MSG = "user closed chat";
    private static ChatManager chatManager;
    private static String domain;
    private static Boolean startedChat = Boolean.FALSE;
    private static EntityBareJid chatPartner = null;
    private static InputHandler inputHandler;

    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException {
        // username password partnerJID host port connect
        int index = 0;
        String username =   args[index++];
        String password =   args[index++];
        domain =            args[index++];
        String host =       args[index++];
        String port =       args[index++];

        AbstractXMPPConnection connection = connectAndLogin(username, password, domain, host, port);
        chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new LoggingIncomingMsgListener());
        inputHandler = new InputHandler(new MenuLineConsumer());
        inputHandler.start();
        addChatListener();
    }

    public static void startChat(String chatPartner) {
        String jidString = chatPartner + "@" + domain;
        EntityBareJid jid;
        try {
            jid = JidCreate.entityBareFrom(jidString);
        } catch (XmppStringprepException e) {
            throw new RuntimeException(e);
        }
        System.err.println("Creating Chat with: " + jid.toString());
        startedChat = Boolean.TRUE;
        Chat chat = chatManager.chatWith(jid);
//        enterChatLoop(chat);
        inputHandler.switchLineConsumer(new ChatLineConsumer(chat));
    }

    public static void printMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("Options:").append(System.lineSeparator());
        sb.append(OPEN_CHAT_MENU_OPTION).append(" user");
        System.out.println(sb.toString());
    }

    public static void addChatListener() throws InterruptedException {

        chatManager.addIncomingListener(new IncomingChatMessageListener() {
            @Override
            public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
                if (startedChat) {
//                    System.err.println("Ignoring msg bc started chat and already logged by other listener");
                    return;
                } else {
                    // todo implement chatting boolean and waiting queue if another user tries to chat with user, while
                    if (chatPartner == null || !chatPartner.equals(entityBareJid)) {
                        System.err.println("New Chat Partner: " + entityBareJid);
                        chatPartner = entityBareJid;
//                        waitForFreeChat();
                        inputHandler.switchLineConsumer(new ChatLineConsumer(chat));
                    }
                    if (message.getBody().contains(CLOSE_CHAT_MSG)){
                        closeChat();
                        return;
                    }
                }
            }
        });
    }

    public static AbstractXMPPConnection connectAndLogin(String username, String password, String domain, String host, String port) throws IOException, InterruptedException, XMPPException, SmackException {
        // Create a connection to the jabber.org server on a specific port.
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setXmppDomain(domain)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) // Do not disable TLS except for test purposes!
                .setHost(host)
                .setPort(Integer.valueOf(port))
//                .setResource(device)
                .build();

        // SASL is already configured here for user from config
        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
//        connection.addAsyncStanzaListener(new MyStanzaListener(), new StanzaFilter() {
//            public boolean accept(Stanza stanza) {
//                return true;
//            }
//        });
        System.err.println("connecting to server: " + host + " : " + port);
        connection.connect();
        connection.login();
        System.err.println("Logged in to server as: " + connection.getUser().toString());
        if (connection.isAuthenticated()) {
            System.err.println("Auth done");
        }
        return connection;
    }



    static interface LineConsumer {
        public void consume(String line);
    }

    static class MenuLineConsumer implements LineConsumer {

        public MenuLineConsumer() {
            printMenu();
        }

        @Override
        public void consume(String menuInput) {
            if (menuInput.startsWith(OPEN_CHAT_MENU_OPTION)) {
                String chatPartner = menuInput.split(" ")[1];
                startChat(chatPartner);
            } else {
                System.out.println("Invalid input");
            }
        }
    }


    static class ChatLineConsumer implements LineConsumer {
        private Chat chat;

        public ChatLineConsumer(Chat chat) {
            this.chat = chat;
        }

        @Override
        public void consume(String line) {
            try {
                if (line.equals("q")) {
                    chat.send(CLOSE_CHAT_MSG);
                    closeChat();
                    return;
                }
                System.err.println("Sending msg: " + line);
                chat.send(line);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class InputHandler extends Thread {
        private LineConsumer lineConsumer;
        private Scanner scanner;

        public InputHandler(LineConsumer lineConsumer) {
            this.lineConsumer = lineConsumer;
            this.scanner = new Scanner(System.in);
        }

        @Override
        public void run() {
            while (true) {
                String line = scanner.nextLine();
                this.lineConsumer.consume(line);
            }
        }

        public synchronized void switchLineConsumer(LineConsumer lineConsumer) {
//            this.interrupt();
            this.lineConsumer = lineConsumer;
        }
    }

    public static synchronized void closeChat() {
        System.out.println("chat closed");
        inputHandler.switchLineConsumer(new MenuLineConsumer());
        chatPartner = null;
        startedChat = Boolean.FALSE;
    }
//
//    public static synchronized void waitForFreeChat() {
//        while (chatting) {
//            try {
//                System.out.println("Waiting for ending chat...");
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        System.out.println("start chatting");
//        chatting = Boolean.TRUE;
//    }

    static class LoggingIncomingMsgListener implements IncomingChatMessageListener {
        @Override
        public void newIncomingMessage(EntityBareJid entityBareJid, Message message, Chat chat) {
            System.out.println("Received message: "
                    + (message != null ? message.getBody() : "NULL" + " | from " + entityBareJid));
        }
    }

//    /**
//     * Displays users (entries) in the roster
//     */
//    public static void displayBuddyList(XMPPTCPConnection connection) {
//        Roster roster = Roster.getInstanceFor(connection);
//        Collection<RosterEntry> entries = roster.getEntries();
//
//        System.out.println("\n\n" + entries.size() + " buddy(ies):");
//        for (RosterEntry r : entries) {
//            BareJid userJid = r.getJid();
//            Presence.Type presenceType = roster.getPresence(userJid).getType();
//            System.out.println(userJid + ":" + presenceType);
//        }
//    }


}

