package com.github.vincemann.xmpp.client;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jxmpp.stringprep.XmppStringprepException;

//public class CreateAccount {

//    public static void main(String[] args) throws XmppStringprepException {
//        // Create a connection to the jabber.org server on a specific port.
//        XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
//                .setUsernameAndPassword("myadmin", "password")
//                .setXmppDomain(XMPP_DOMAIN)
//                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) // Do not disable TLS except for test purposes!
////                .setDebuggerEnabled(true)
////                .setHost("earl.jabber.org")
//                .setHost("localhost")
//                .setPort(5222)
//                .build();
//        AbstractXMPPConnection connection = new XMPPTCPConnection(config);
//
//
//        AccountManager accountManager = AccountManager.getInstance(connection);
//        try {
//            if (accountManager.supportsAccountCreation()) {
//                accountManager.sensitiveOperationOverInsecureConnection(true);
//                accountManager.createAccount("userName", "password");
//
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
//}
