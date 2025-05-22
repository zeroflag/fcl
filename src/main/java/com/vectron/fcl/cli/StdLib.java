package com.vectron.fcl.cli;

import com.vectron.fcl.Fcl;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class StdLib {
    private final Fcl fcl;
    private final String libDir;

    public StdLib(Fcl fcl, String libDir) {
        this.fcl = fcl;
        this.libDir = libDir;
    }

    public void load() {
        fcl.addPrimitive("exchange", () -> { throw new RuntimeException("not implemented"); }, false);
        fcl.addPrimitive("aux>", () -> { throw new RuntimeException("not implemented"); }, false);
        fcl.addPrimitive(">aux", () -> { throw new RuntimeException("not implemented"); }, false);
        loadFile("core.forth");
        loadFile("ops.forth");
        loadFile("locals.forth");
        loadFile("quotations.forth");
        loadFile("collections.forth");
        loadFile("http.forth");
        loadFile("misc.forth");
    }

    private void loadFile(final String fileName) {
        FileReader reader = null;
        try {
            reader = new FileReader(Paths.get(libDir, fileName).toFile());
            fcl.eval(reader);
        } catch (IOException e) {
            System.out.println("Cannot load " + fileName
                    + " in " + libDir + " reason: " + e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
