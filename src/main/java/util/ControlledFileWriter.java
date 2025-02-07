package util;

import java.io.FileWriter;
import java.io.IOException;

public class ControlledFileWriter {
    private FileWriter fileWriter;
    private boolean enableLogging;

    public ControlledFileWriter(String fileName, boolean enableLogging) throws IOException {
        this.enableLogging = enableLogging;
        if (enableLogging) {
            fileWriter = new FileWriter(fileName,false);
        }
    }

    public void write(String str) throws IOException {
        if (enableLogging && fileWriter != null) {
            fileWriter.write(str);
        }
    }

    public void close() throws IOException {
        if (enableLogging && fileWriter != null) {
            fileWriter.close();
        }
    }

}