package com.vectron.fcl;

public class RamTranscript implements Transcript {
    private final StringBuilder content = new StringBuilder();

    @Override
    public void show(String str) {
        content.append(str);
    }

    @Override
    public void cr() {
        content.append("\n");
    }

    public String content() {
        return content.toString();
    }
}
