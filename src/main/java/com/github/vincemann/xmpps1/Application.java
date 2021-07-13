package com.github.vincemann.xmpps1;

import org.apache.maven.plugin.logging.Log;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.util.stringencoder.Base64;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;

import java.io.IOException;
import java.util.Collection;
import java.util.Scanner;

public class Application {

    public static final String XMPP_DOMAIN = "debian.local";

    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException {
        // username password partnerJID host port
        int index = 0;
        String username = args[index++];
        String password = args[index++];
        String device = args[index++];
        String partnerJid = args[index++];
        String host = args[index++];
        String port = args[index++];
        String writeMsg = args[index++];

        System.err.println("Write msg:" + writeMsg);



        // Create a connection to the jabber.org server on a specific port.
        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword(username, password)
                .setXmppDomain(XMPP_DOMAIN)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) // Do not disable TLS except for test purposes!
//                .setDebuggerEnabled(true)
//                .setHost("earl.jabber.org")
                .setHost(host)
                .setPort(Integer.valueOf(port))
                .setResource(device)
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
        System.err.println("Logging in to server as: " + connection.getUser().toString());


        ChatManager chatManager = ChatManager.getInstanceFor(connection);


        if(connection.isAuthenticated() )
        {
            System.err.println("Auth done");
        }

        if (writeMsg.equals("true")){
            System.err.println("Creating chat");
            EntityBareJid jid = JidCreate.entityBareFrom(partnerJid);
            System.err.println("Creating Chat with: " + jid.toString());
            Chat chat = chatManager.createChat(jid, new ChatMessageListener() {
                public void processMessage(Chat chat, Message message) {
                    System.err.println("New message from " + chat.getParticipant().toString()
                            + ": " + message.getBody());

                }
            });

            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = scanner.next();
                if (msg.equals("q")) {
                    break;
                }
                System.err.println("Sending msg: " + msg);
                chat.sendMessage(msg);
            }
        }else {
            System.err.println("Waiting for chat");
            chatManager.addChatListener(
                    new ChatManagerListener() {
                        @Override
                        public void chatCreated(Chat chat, boolean createdLocally)
                        {
                            System.err.println("New Chat created");
                            chat.addMessageListener(new ChatMessageListener()
                            {
                                @Override
                                public void processMessage(Chat chat, Message message) {
                                    System.out.println("Received message: "
                                            + (message != null ? message.getBody() : "NULL"));
                                }
                            });

                            System.err.println(chat.toString());
                        }
                    });
        }

    }

    /**
     * Displays users (entries) in the roster
     */
    public static void displayBuddyList(XMPPTCPConnection connection) {
        Roster roster = Roster.getInstanceFor(connection);
        Collection<RosterEntry> entries = roster.getEntries();

        System.out.println("\n\n" + entries.size() + " buddy(ies):");
        for (RosterEntry r : entries) {
            BareJid userJid = r.getJid();
            Presence.Type presenceType = roster.getPresence(userJid).getType();
            System.out.println(userJid + ":" + presenceType);
        }
    }


}

