//package com.github.vincemann.xmpp.client.register;
////
//import org.jivesoftware.smack.AbstractXMPPConnection;
//import org.jivesoftware.smack.ConnectionConfiguration;
//import org.jivesoftware.smack.SmackException;
//import org.jivesoftware.smack.XMPPException;
//import org.jivesoftware.smack.tcp.XMPPTCPConnection;
//import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
//import org.jivesoftware.smackx.iqregister.AccountManager;
//import org.jxmpp.jid.parts.Localpart;
//import org.jxmpp.stringprep.XmppStringprepException;
//
//import java.io.IOException;
//
//public class CreateAccountApplication {
//
//    public static void main(String[] args) throws IOException, InterruptedException, XMPPException, SmackException {
//        int index = 0;
//        String username =   args[index++];
//        String password =   args[index++];
//
//        AbstractXMPPConnection connection = connectAndLogin("admin", "admin", "debian.local", "127.0.0.1", "5222");
//
//
//        AccountManager accountManager = AccountManager.getInstance(connection);
//        try {
//            if (accountManager.supportsAccountCreation()) {
//                accountManager.sensitiveOperationOverInsecureConnection(true);
//                Localpart usernameLocalPart = Localpart.from(username);
//                accountManager.createAccount(usernameLocalPart, password);
//            }else {
//                System.out.println("Does not support account creation");
//            }
//        } catch (SmackException.NoResponseException e) {
//            e.printStackTrace();
//        } catch (XMPPException.XMPPErrorException e) {
//            e.printStackTrace();
//        } catch (SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static AbstractXMPPConnection connectAndLogin(String username, String password, String domain, String host, String port) throws IOException, InterruptedException, XMPPException, SmackException {
//        // Create a connection to the jabber.org server on a specific port.
//        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
//                .setUsernameAndPassword(username, password)
//                .setXmppDomain(domain)
//                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) // Do not disable TLS except for test purposes!
//                .setHost(host)
//                .setPort(Integer.valueOf(port))
////                .setResource(device)
//                .build();
//
//        // SASL is already configured here for user from config
//        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
////        connection.addAsyncStanzaListener(new MyStanzaListener(), new StanzaFilter() {
////            public boolean accept(Stanza stanza) {
////                return true;
////            }
////        });
//        System.out.println("connecting to server: " + host + " : " + port);
//        connection.connect();
//        connection.login();
//        System.out.println("Logged in to server as: " + connection.getUser().toString());
//        if (connection.isAuthenticated()) {
//            System.out.println("Auth done");
//        }
//        return connection;
//    }
//}
