package com.github.vincemann.xmpp.client.input;

import com.github.vincemann.xmpp.client.BiDirChatManager;

public class MenuLineConsumer  implements LineConsumer {
    private static final String OPEN_CHAT_MENU_OPTION = "chat";
    private BiDirChatManager biDirChatManager;

    public MenuLineConsumer(BiDirChatManager biDirChatManager) {
        this.biDirChatManager = biDirChatManager;
        printMenu();
    }

    public MenuLineConsumer() {
        printMenu();
    }

    public void printMenu() {
        StringBuilder sb = new StringBuilder();
        sb.append("Options:").append(System.lineSeparator());
        sb.append(OPEN_CHAT_MENU_OPTION).append(" user");
        System.out.println(sb.toString());
    }

    @Override
    public void consume(String menuInput) {
        if (menuInput.startsWith(OPEN_CHAT_MENU_OPTION)) {
            String chatPartner = menuInput.split(" ")[1];
            biDirChatManager.startChat(chatPartner);
        } else {
            System.out.println("Invalid input");
        }
    }

    public void setBiDirChatManager(BiDirChatManager biDirChatManager) {
        this.biDirChatManager = biDirChatManager;
    }
}