package han.grpc;

import static han.Constant.REQUEST_EXPIRE;

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

import com.google.protobuf.GeneratedMessageV3;

import han.Constant;
import han.MsgFactory;
import han.Server;
import han.ServerSingleton;
import han.StateVisitor;
import han.grpc.MQService.AppendEntry;
import han.grpc.MQService.Ack;
import han.grpc.MQService.RequestVote;
import han.state.LeaderState;
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

    public synchronized static void init(boolean multi) {
        int me = 0;
        if (multi) {
            System.out.println("本机的id是:");
            me = new Scanner(System.in).nextInt();
        }
        init(multi, me);
    }

    /**
     * 本机id
     * @param multi
     * @param me
     */
    public synchronized static void init(boolean multi, int me) {
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
            int port = Integer.parseInt(serversCnf[2]);
            int id = Integer.parseInt(serversCnf[0]);

            if (hostname.equals("me") || (multi && id == me)) {
                ServerSingleton.init(id);
                HandlerSingleton.init(port);
                ServerSingleton.getServer().setId(id);
            } else {
                senderList.add(new Sender(hostname, port));
            }
        }
        logger.info("初始化完成，我是:{}", ServerSingleton.getServer());
    }

    public synchronized static void init() {
        init(false);
    }

    public static boolean send(GeneratedMessageV3 msg) {
        init();
        Map<Sender, Ack> ackMap = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(Constant.clusterSize / 2 + 1);
        Server server = ServerSingleton.getServer();
        server.setVoteFor(server.getId());
        latch.countDown();
        boolean isVoteRequest = msg instanceof RequestVote;
        for (Sender sender : senderList) {
            if (isVoteRequest) {
                RequestVote requestVote = (RequestVote) msg;
                executor.submit(new SendMsgTask().msg(requestVote).sender(sender).ackMap(ackMap).latch(latch));
            } else {
                AppendEntry appendEntry = (AppendEntry) msg;
                executor.submit(new SendMsgTask().msg(appendEntry).sender(sender).ackMap(ackMap).latch(latch));
            }
        }
        logger.debug("向{}个目标发送请求", senderList.size());
        boolean await = false;
        try {
            await = latch.await(REQUEST_EXPIRE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("请求结果:{}, 接收到{}个响应， 其中成功数目为{}", await, ackMap.size(), ackMap.values());
        return await;
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
        ServerSingleton.init(1);
        init();
        senderList.get(2).send(MsgFactory.requestVote());
    }
}
