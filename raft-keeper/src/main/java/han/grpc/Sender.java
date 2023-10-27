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

import han.Constant;
import han.MsgFactory;
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
public class Sender {
    final static Logger logger = LogManager.getLogger(ServerSingleton.class);
    final static List<ServerStubContext> serverStubList = new ArrayList<>();
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
     * @param multi 是否多实例
     * @param me 若多实例，需给出本机id
     */
    public synchronized static void init(boolean multi, int me) {
        if (!serverStubList.isEmpty()) {
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
                HandlerInitializer.init(port);
                ServerSingleton.getServer().setId(id);
            }
            if (id != serverStubList.size() + 1) {
                throw new RuntimeException("id与实际不符, 请使用从1开始、连续的id");
            }
            serverStubList.add(new ServerStubContext(hostname, port));
        }
        logger.info("初始化完成，共有{}个server，我是:{}", Constant.clusterSize, ServerSingleton.getServer());
    }

    public synchronized static void init() {
        init(false);
    }

    /**
     * 向所有follower发RequestVote消息
     * @param msg 要发送的消息
     * @return 发送成功返回true，失败或超时返回false
     */
    public static boolean send(RequestVote msg) {
        init();
        CountDownLatch latch = new CountDownLatch(Constant.clusterSize / 2 + 1);
        latch.countDown();
        for (ServerStubContext serverStubContext : serverStubList) {
            executor.submit(new SendMsgTask().msg(msg).sender(serverStubContext).latch(latch));
        }
        logger.debug("向{}个目标发送请求", serverStubList.size());
        try {
            return latch.await(REQUEST_EXPIRE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean send(AppendEntry appendEntry, boolean carryEntry) {
        init();
        Map<ServerStubContext, Ack> ackMap = new ConcurrentHashMap<>();
        CountDownLatch latch = new CountDownLatch(Constant.clusterSize / 2 + 1);
        latch.countDown();
        Server server = ServerSingleton.getServer();
        for (int i = 0; i < serverStubList.size(); i++) {
            ServerStubContext serverStubContext = serverStubList.get(i);
            if (carryEntry) {
                // fixme 疑似nextIndex不对
                Integer nextIndex = server.getNextIndex().get(i);
                if (nextIndex != null && nextIndex < server.getLogs().size()) {
                    han.grpc.MQService.Log log = MsgFactory.log(server.getLogs().get(nextIndex));
                    appendEntry = appendEntry.toBuilder().addEntry(log).build();
                }
            }
            SendMsgTask sendMsgTask = new SendMsgTask().msg(appendEntry).sender(serverStubContext);
            executor.submit(sendMsgTask.ackMap(ackMap).latch(latch).targetServerId(i));
        }
        logger.debug("向{}个目标发送请求", serverStubList.size());
        boolean await = false;
        try {
            await = latch.await(REQUEST_EXPIRE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.debug("请求结果:{}, 接收到{}个响应， 其中成功数目为{}", await, ackMap.size(), ackMap.values());
        return await;
    }

    public static boolean send(AppendEntry msg) {
        return send(msg, false);
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
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static class SendMsgTask implements Callable<Ack> {
        ServerStubContext serverStubContext;
        AppendEntry appendEntry;
        RequestVote requestVote;
        Map<ServerStubContext, Ack> ackMap;
        CountDownLatch latch;
        Integer targetServerId;
        SendMsgTask sender(ServerStubContext serverStubContext) {
            this.serverStubContext = serverStubContext;
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
        SendMsgTask ackMap(Map<ServerStubContext, Ack> ackMap) {
            this.ackMap = ackMap;
            return this;
        }
        SendMsgTask latch(CountDownLatch latch) {
            this.latch = latch;
            return this;
        }
        SendMsgTask targetServerId(int id) {
            this.targetServerId = id;
            return this;
        }
        @Override
        public Ack call() {
            stateCheck();
            Ack ack;
            if (requestVote != null) {
                ack = serverStubContext.send(requestVote);
            } else {
                ack = serverStubContext.send(appendEntry);
            }
            if (ackMap != null) {
                ackMap.put(serverStubContext, ack);
            }
            if (ack.getSuccess()) {
                if (latch != null) {
                    latch.countDown();
                }
                if (appendEntry != null && !appendEntry.getEntryList().isEmpty()) {
                    List<Integer> nextIndex = ServerSingleton.getServer().getNextIndex();
                    int index = targetServerId - 1;
                    nextIndex.set(index, nextIndex.get(index) + 1);
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
            if (serverStubContext == null) {
                throw new IllegalStateException("sender未设置");
            }
        }
    }

    public static void main(String[] args) {
        ServerSingleton.init(1);
        init();
        serverStubList.get(2).send(MsgFactory.requestVote());
    }
}
