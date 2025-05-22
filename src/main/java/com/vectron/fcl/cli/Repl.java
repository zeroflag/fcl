package com.vectron.fcl.cli;

import com.vectron.fcl.Fcl;

import java.util.Scanner;

public class Repl {
    private final Fcl fcl;

    public Repl(StdLib lib, Fcl fcl) {
        this.fcl = fcl;
        // TODO add missing primitive
        fcl.addPrimitive("exchange", () -> { throw new RuntimeException("not implemented"); }, false);
        fcl.addPrimitive("aux>", () -> { throw new RuntimeException("not implemented"); }, false);
        fcl.addPrimitive(">aux", () -> { throw new RuntimeException("not implemented"); }, false);
        lib.load();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to FCL REPL (type 'bye' to quit)");
        while (true) {
            System.out.print("% ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")
                    || input.equalsIgnoreCase("bye")) {
                break;
            } else {
                try {
                    fcl.eval(input);
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }
        }
        scanner.close();
    }
}
