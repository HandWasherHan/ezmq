package han.grpc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import han.Constant;
import han.Server;
import han.ServerSingleton;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.Ack;
import han.grpc.MQService.RequestVote;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class SenderListSingleton {
    final static Logger logger = LogManager.getLogger(ServerSingleton.class);
    final static List<Sender> senderList = new ArrayList<>();
    final static Pattern numberPattern = Pattern.compile("^\\d+$");
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 16,
            1000, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(1000),
            new DefaultThreadFactory(""),
            new ThreadPoolExecutor.AbortPolicy());

    synchronized static void init() {
        if (!senderList.isEmpty()) {
            return;
        }
        File file = new File("cluster.cnf");
        Scanner scanner;
        try {
            scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            Constant.clusterSize++;
            String[] serversCnf = scanner.nextLine().split(" ");
            isCnfLegal(serversCnf);
            String hostname = serversCnf[1];
            String port = serversCnf[2];
            String id = serversCnf[0];

            if (hostname.equals("me")) {
                HandlerSingleton.init(Integer.parseInt(port));
                ServerSingleton.getServer().setId(Integer.parseInt(id));
            } else {
                senderList.add(new Sender(hostname, Integer.parseInt(port)));
            }
        }
        logger.info("初始化完成，我是:{}", ServerSingleton.getServer());
    }

    public static void send(AppendEntry appendEntry) {
        init();
        Map<Sender, Ack> ackMap = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(Constant.clusterSize / 2 + 1);
        for (Sender sender : senderList) {
            executor.submit(new SendMsgTask().msg(appendEntry).sender(sender).ackMap(ackMap));
        }
        try {
            if (latch.await(500, TimeUnit.MILLISECONDS)) {
                Server server = ServerSingleton.getServer();
                server.setCommitIndex(server.getCommitIndex() + 1);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    static void isCnfLegal(String[] cnf) throws RuntimeException {
        try {
            if (cnf.length != 3) {
                String message = "请使用以单个空格分割的三个属性：id, hostname, port";
                throw new InvalidPropertiesFormatException(message);
            }
            if (!numberPattern.matcher(cnf[0]).matches()) {
                throw new InvalidPropertiesFormatException("id必须为非负32位数字");
            }
            if (!numberPattern.matcher(cnf[2]).matches()) {
                throw new InvalidPropertiesFormatException("port必须为非负数字");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class SendMsgTask implements Callable<Ack> {
        Sender sender;
        AppendEntry appendEntry;
        RequestVote requestVote;
        Map<Sender, Ack> ackMap;
        CountDownLatch latch;
        SendMsgTask sender(Sender sender) {
            this.sender = sender;
            return this;
        }
        SendMsgTask msg(AppendEntry msg) {
            this.appendEntry = msg;
            return this;
        }
        SendMsgTask msg(RequestVote msg) {
            this.requestVote = msg;
            return this;
        }
        SendMsgTask ackMap(Map<Sender, Ack> ackMap) {
            this.ackMap = ackMap;
            return this;
        }
        SendMsgTask latch(CountDownLatch latch) {
            this.latch = latch;
            return this;
        }

        @Override
        public Ack call() throws Exception {
            stateCheck();
            Ack ack;
            if (requestVote != null) {
                ack = sender.send(requestVote);
            } else {
                ack = sender.send(appendEntry);
            }
            if (ackMap != null) {
                ackMap.put(sender, ack);
            }
            if (ack.getSuccess()) {
                if (latch != null) {
                    latch.countDown();
                }
            }
            return ack;
        }

        void stateCheck() {
            if (requestVote != null && appendEntry != null) {
                throw new IllegalStateException("一次只能发一种消息");
            }
            if (requestVote == null && appendEntry == null) {
                throw new IllegalStateException("没有消息可发");
            }
            if (sender == null) {
                throw new IllegalStateException("sender未设置");
            }
        }
    }

    public static void main(String[] args) {

        init();
    }
}
