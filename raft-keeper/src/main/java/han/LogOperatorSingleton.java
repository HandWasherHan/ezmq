package han;

import java.util.List;

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

    public static void init(LogOperator logOperator) {
        if (operator != null) {
            return;
        }
        synchronized (lock) {
            if (operator == null) {
                operator = logOperator;
            }
        }
    }

    public static void write(String filename, Log log) {
        init(filename);
        operator.write(log);
    }

    public static void write(Log log) {
        ObjectUtils.assertNotNull(operator);
        operator.write(log);
    }

    public static List<Log> read(String filename) {
        init(filename);
        return operator.read();
    }

    public static List<Log> read() {
        ObjectUtils.assertNotNull(operator);
        return operator.read();
    }

    public static void main(String[] args) {
        init("testFile.log");
        for (int i = 0; i < 10; i++) {
            write(new Log(i, "i am line-" + i));
        }
        System.out.println(read());
    }
}
