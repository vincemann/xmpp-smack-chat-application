package com.github.vincemann.xmpp.client.input;

import java.util.Scanner;

public class InputHandler extends Thread {
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
        this.lineConsumer = lineConsumer;
    }
}
