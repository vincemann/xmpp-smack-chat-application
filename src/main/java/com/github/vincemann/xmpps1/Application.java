package com.github.vincemann.xmpps1;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.Chat;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.StreamOpen;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Application {

    //    public static final String XMPP_DOMAIN = "debian.local";
    private static final String OPEN_CHAT_MENU_OPTION = "c";
    private static AbstractXMPPConnection connection;
    private static ChatManager chatManager;
    private static String domain;
    private static Boolean chatting = Boolean.FALSE;
    private static Boolean startedChat = Boolean.FALSE;
//    private static volatile Scanner menuScanner;

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
        chatManager.addIncomingListener(new LoggingIncomingMsgListener());
        // start new thread waiting for incoming chats
        waitForChat();
        printMenu();
        handleMenuInput();
    }

    public static Thread startChat(String chatPartner, Scanner scanner) {
        Thread thread = new Thread(() -> {
            String jidString = chatPartner + "@" + domain;
            EntityBareJid jid;
            try {
                jid = JidCreate.entityBareFrom(jidString);
            } catch (XmppStringprepException e) {
                throw new RuntimeException(e);
            }
            System.err.println("----- Creating Chat with: " + jid.toString());
            startedChat = Boolean.TRUE;
            Chat chat = chatManager.chatWith(jid);
            enterChatLoop(chat);
        });
        thread.start();
        return thread;
    }

    public static void handleMenuInput() throws InterruptedException {
        while (true) {
            Scanner scanner = new Scanner(System.in);
//            menuScanner = scanner;
            String menuInput = scanner.nextLine();
            if (menuInput.startsWith(OPEN_CHAT_MENU_OPTION)) {
                String chatPartner = menuInput.split(" ")[1];
                Thread thread = startChat(chatPartner, scanner);
                scanner.reset();
//                // wait for chat to finish
                thread.join();
                System.err.println("done chatting with initialized chat");
//                printMenu();
            } else {
                System.out.println("Invalid input");
            }
        }
    }

    public static void printMenu() {
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
            chatManager.addIncomingListener(new IncomingChatMessageListener() {
                @Override
                public void newIncomingMessage(EntityBareJid entityBareJid, Message message, org.jivesoftware.smack.chat2.Chat chat) {
                    if (startedChat){
//                        System.out.println("Started chat, ignoring handler");
                        startedChat=Boolean.FALSE;
                        return;
                    }
                    gChat[0] = chat;
                    chatCreated[0] = Boolean.TRUE;
//                    System.err.println("-----" + chat.toString());
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
        if (connection.isAuthenticated()) {
            System.err.println("Auth done");
        }
        return connection;
    }

    // replace with chat window opening
    public static void enterChatLoop(Chat chat) {
        waitForFreeChat();
        System.err.println("Entering chat loop");
//        menuScanner.reset();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Scanner scanner = new Scanner(System.in);
        scanner.reset();
        while (true) {
            try {
                String msg = scanner.nextLine();
                if (msg.equals("q")) {
                    doneChatting();
                    break;
                }
                System.err.println("Sending msg: " + msg);
                chat.send(msg);
            }/* catch (IOException e) {
                e.printStackTrace();
            } */catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static synchronized void doneChatting() {
        System.out.println("done chatting");
        chatting = Boolean.FALSE;
    }

    public static synchronized void waitForFreeChat() {
        while (chatting) {
            try {
                System.out.println("Waiting for ending chat...");
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("start chatting");
        chatting = Boolean.TRUE;
    }

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

