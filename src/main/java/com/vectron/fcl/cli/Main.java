package com.vectron.fcl.cli;

import com.vectron.fcl.Fcl;
import com.vectron.fcl.FclStack;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

public class Main {
    public static void main(String[] args) throws IOException {
        Fcl fcl = new Fcl(new FclStack(), 524288, new CliTranscript());
        if (args.length >= 1) {
            try (Reader r = new FileReader(args[0])) {
                fcl.eval(r);
            }
        } else {
            Repl repl = new Repl(
                    new StdLib(fcl, System.getProperty("fcl.lib.dir", "")),
                    fcl);
            repl.start();
        }
    }
}
