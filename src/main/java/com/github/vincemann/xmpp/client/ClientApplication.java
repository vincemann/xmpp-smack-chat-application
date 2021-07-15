package com.github.vincemann.xmpp.client;

import com.github.vincemann.xmpp.client.input.InputHandler;
import com.github.vincemann.xmpp.client.input.MenuLineConsumer;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

public class ClientApplication {

    //    public static final String XMPP_DOMAIN = "debian.local";

    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException {
        // username password partnerJID host port connect
        int index = 0;
        String username =   args[index++];
        String password =   args[index++];
        String domain =     args[index++];
        String host =       args[index++];
        String port =       args[index++];

        AbstractXMPPConnection connection = connectAndLogin(username, password, domain, host, port);
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        chatManager.addIncomingListener(new LoggingIncomingMsgListener());
        MenuLineConsumer menuLineConsumer = new MenuLineConsumer();
        InputHandler inputHandler = new InputHandler(menuLineConsumer);
        BiDirChatManager biDirChatManager = new BiDirChatManager(chatManager, domain, inputHandler);
        menuLineConsumer.setBiDirChatManager(biDirChatManager);
        chatManager.addIncomingListener(biDirChatManager);
        inputHandler.start();
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
        System.out.println("connecting to server: " + host + " : " + port);
        connection.connect();
        connection.login();
        System.out.println("Logged in to server as: " + connection.getUser().toString());
        if (connection.isAuthenticated()) {
            System.out.println("Auth done");
        }
        return connection;
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

