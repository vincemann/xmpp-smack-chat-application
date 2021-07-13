package com.github.vincemann.xmpps1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
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
    private static final String OPEN_CHAT_MENU_OPTION = "open-chat";
    private static AbstractXMPPConnection connection;
    private static ChatManager chatManager;
    private static String domain;
    private static Boolean chatting = Boolean.FALSE;
    private static Boolean started_chat = Boolean.FALSE;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException {
        // username password partnerJID host port connect
        int index = 0;
        String username = args[index++];
        String password = args[index++];
        domain = args[index++];
//        String partnerJid = args[index++];
        String host = args[index++];
        String port = args[index++];
//        String writeMsg = args[index++];

        connection = connectAndLogin(username, password, domain, host, port);
        chatManager = ChatManager.getInstanceFor(connection);
        // start new thread waiting for incoming chats
        scanner = new Scanner(System.in);
        waitForChat();
        printMenu();
        handleMenuInput();
    }

    public static Thread startChat(String chatPartner) {
        Thread thread = new Thread(() -> {
            String jidString  = chatPartner+"@"+domain;
            EntityBareJid jid;
            try {
                jid = JidCreate.entityBareFrom(jidString);
            } catch (XmppStringprepException e) {
                throw new RuntimeException(e);
            }
            System.err.println("----- Creating Chat with: " + jid.toString());
            started_chat = Boolean.TRUE;
            Chat chat = chatManager.createChat(jid, new LoggingMsgListener());
            enterChatLoop(chat);
        });
        thread.start();
        return thread;
    }

    public static void handleMenuInput() throws InterruptedException {
//        scanner = new Scanner(System.in);
        while (true) {
            String menuInput = scanner.nextLine();
            if (menuInput.startsWith(OPEN_CHAT_MENU_OPTION)){
                String chatPartner = menuInput.split(" ")[1];
                Thread thread = startChat(chatPartner);
//                // wait for chat to finish
                thread.join();
                System.err.println("done chatting with initialized chat");
//                printMenu();
            }else {
                System.out.println("Invalid input");
            }
        }
    }

    public static void printMenu(){
        StringBuilder sb = new StringBuilder();
        sb.append("Options:").append(System.lineSeparator());
        sb.append(OPEN_CHAT_MENU_OPTION).append(" user").append(System.lineSeparator());
        System.out.println(sb.toString());
    }

    public static Thread waitForChat() throws InterruptedException {
        Thread thread = new Thread(() -> {
            final Chat[] gChat = {null};
            final Boolean[] chatCreated = {Boolean.FALSE};
            System.err.println("Waiting for chat");
            chatManager.addChatListener(
                    new ChatManagerListener() {
                        @Override
                        public void chatCreated(Chat chat, boolean createdLocally) {
                            if (started_chat){
                                System.out.println("Started chat, ignoring handler");
                                started_chat=Boolean.FALSE;
                                return;
                            }
                            gChat[0] = chat;
                            System.err.println("-----New Chat created");
                            chat.addMessageListener(new LoggingMsgListener());
                            chatCreated[0] = Boolean.TRUE;
                            System.err.println("-----" + chat.toString());

                        }
                    });
            // this should stop as soon as new chat is created
            while (!chatCreated[0]) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            enterChatLoop(gChat[0]);
        });
        thread.start();
        Thread.sleep(100);
        return thread;
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
        if(connection.isAuthenticated() )
        {
            System.err.println("Auth done");
        }
        return connection;
    }

    static class LoggingMsgListener implements ChatMessageListener{
        @Override
        public void processMessage(Chat chat, Message message) {
            System.out.println("Received message: "
                    + (message != null ? message.getBody() : "NULL" + " | from " + chat.getParticipant().toString()));
        }
    }

    // replace with chat window opening
    public static void enterChatLoop(Chat chat)  {
        waitForFreeChat();
        System.err.println("Entering chat loop");
//        scanner = new Scanner(System.in);
        while (true) {
            String msg = scanner.nextLine();
            if (msg.equals("q")) {
                chat.close();
                doneChatting();
                break;
            }
            System.err.println("Sending msg: " + msg);
            try {
                chat.sendMessage(msg);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static synchronized void doneChatting(){
        System.out.println("done chatting");
        chatting = Boolean.FALSE;
    }

    public static synchronized void waitForFreeChat(){
        while (chatting){
            try {
                System.out.println("Waiting for ending chat...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("start chatting");
        chatting = Boolean.TRUE;
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

