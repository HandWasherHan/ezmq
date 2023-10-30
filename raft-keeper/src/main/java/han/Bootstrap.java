package han;

import static han.Constant.DEFAULT_CLUSTER_CONFIG_FILENAME;

import java.util.InvalidPropertiesFormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import han.grpc.HandlerInitializer;
import han.grpc.Sender;

/**
 * used to start the server.
 * the method calls need to keep a sequence like this:
 * @<code> new Bootstrap().initLocalServer().initLogOperator().readClusterCnf().initHandler(); </code>
 * <br/> or <br/>
 * @<code> Bootstrap.batch(id); </code>
 * <br/> to make it easier <br/>
 * this class used a static int state, increases monotonically, so that a server only starts once
 *
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class Bootstrap {
    static final Logger logger = LogManager.getLogger(Bootstrap.class);
    static int state = 0;
    static int me;
    static int port;
    static String logFilename;

    public static void batch(int id) throws InvalidPropertiesFormatException {
        new Bootstrap().initLocalServer(id)
                .initLogOperator().readClusterCnf()
                .initHandler();
    }
    Bootstrap initLocalServer(int id) {
        if (state != 0) {
            throw new IllegalStateException();
        }
        me = id;
        state = 1;
        return this;
    }

    Bootstrap initLogOperator() {
        logFilename = "test" + me + ".log";
        logger.info("使用默认日志文件名{}", logFilename);
        return initLogOperator(logFilename);
    }

    Bootstrap initLogOperator(String filename) {
        if (state != 1) {
            throw new IllegalStateException();
        }
        if (filename == null) {
            filename = "test" + me + ".log";
            logger.info("使用默认日志文件名{}", filename);
        }else if (!filename.matches("\\w+\\.log")) {
            logger.warn("日志文件名不合法:{}, 请使用字母数字组合, 并以[.log]作后缀. " +
                    "已替换为默认日志文件名: {}", filename, filename = "test" + me + ".log");
        }
        logFilename = filename;
        LogOperatorSingleton.init(filename);
        state = 2;
        return this;
    }

    Bootstrap readClusterCnf() throws InvalidPropertiesFormatException {
        if (state != 2) {
            throw new IllegalStateException();
        }
        ServerSingleton.init(me);
        port = Sender.readCnf(DEFAULT_CLUSTER_CONFIG_FILENAME, me);
        state = 3;
        return this;
    }

    void initHandler() {
        if (state != 3) {
            throw new IllegalStateException();
        }
        HandlerInitializer.init(port);
    }
}
