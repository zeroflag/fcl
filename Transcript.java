package com.vectron.fcl;

public interface Transcript {
    void show(String str);
    void cr();

    Transcript STDOUT = new Transcript() {
        public void show(String str) {
            System.out.print(str);
        }

        @Override
        public void cr() {
            System.out.println("");
        }
    };
}
