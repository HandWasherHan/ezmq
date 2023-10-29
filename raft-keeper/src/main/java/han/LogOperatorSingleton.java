package han;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class LogOperatorSingleton {
    final static Integer lock = 0;
    static LogOperator operator;

    public static void init(String filename) {
        if (operator != null) {
            return;
        }
        synchronized (lock) {
            if (operator == null) {
                try {
                    operator = new LogOperator(filename);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void write(Log log) {
        ObjectUtils.assertNotNull(operator);
        operator.write(log);
    }

    public static List<Log> read() {
        ObjectUtils.assertNotNull(operator);
        return operator.read();
    }

    static class LogOperator {
        String filename;

        File file;
        FileOutputStream fo;

        /**
         * 建议使用单例的LogOperator
         *
         * @param filename 文件名
         * @throws IllegalAccessException 访问/创建文件失败
         */
        LogOperator(String filename) throws IllegalAccessException {
            this.filename = filename;
            this.file = new File(filename);
            try {
                if (!file.exists()) {
                    if (!file.createNewFile()) {
                        throw new IllegalAccessException("创建文件失败: " + filename);
                    }
                    this.fo = new FileOutputStream(file);
                } else {
                    this.fo = new FileOutputStream(file, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new IllegalAccessException("访问文件失败: " + filename);
            }
        }

        public synchronized List<Log> read() {
            FileInputStream fi;
            try {
                fi = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            Scanner sc = new Scanner(fi);
            List<Log> res = new ArrayList<>();
            while (sc.hasNext()) {
                res.add(new Log(Integer.parseInt(sc.nextLine()), sc.nextLine()));
            }
            return res;
        }

        public void write(Log log) {
            try {
                fo.write((log.term + "\r\n").getBytes(StandardCharsets.UTF_8));
                fo.write((log.cmd + "\r\n").getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings("deprecated")
        public static void main(String[] args) throws IOException, IllegalAccessException {
            LogOperator test = new LogOperator("test");
            test.fo.write("hello\n".getBytes(StandardCharsets.UTF_8));
            test.fo.write("world\n".getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) {
        init("testFile.log");
        for (int i = 0; i < 10; i++) {
            write(new Log(i, "i am line-" + i));
        }
        System.out.println(read());
    }
}
