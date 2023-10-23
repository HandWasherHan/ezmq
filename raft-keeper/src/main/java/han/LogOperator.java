package han;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class LogOperator {
    String filename;

    File file;
    FileOutputStream fo;
    public LogOperator(String filename) throws IllegalAccessException {
        this.filename = filename;
        this.file = new File(filename);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new IllegalAccessException("文件已存在: " + filename);
                }
                this.fo = new FileOutputStream(file);
            } else {
                this.fo = new FileOutputStream(file, true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void write(Log log) {
        try {
            fo.write((log.term + "\r\n").getBytes(StandardCharsets.UTF_8));
            fo.write((log.cmd + "\r\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, IllegalAccessException {
        LogOperator test = new LogOperator("test");
        test.fo.write("hello\n".getBytes(StandardCharsets.UTF_8));

        LogOperator test2 = new LogOperator("test");

        test.fo.write("world\n".getBytes(StandardCharsets.UTF_8));


    }
}