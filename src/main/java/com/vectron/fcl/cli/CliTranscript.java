package com.vectron.fcl.cli;

import com.vectron.fcl.Transcript;

public class CliTranscript implements Transcript {
    @Override
    public void show(String str) {
        System.out.print(str);
    }

    @Override
    public void cr() {
        System.out.println();
    }
}
