package common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd.Connect;
import constant.BrokerConfig;
import contract.BrokerMetaData;
import handler.follower.ClientConnectHandler;
import handler.follower.PublishSynchronizer;
import handler.leader.ConnectHandler;
import handler.leader.PublisherHandler;
import handler.shared.HeartBeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.BootstrapConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.internal.ConcurrentSet;
import lombok.Data;
import util.NetUtils;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class EzBroker {
    private static final Logger logger = LoggerFactory.getLogger(EzBroker.class);

    private BrokerMetaData brokerMetaData;

    private long lastTrans; // 上次发心跳的时间
    private volatile AtomicBoolean initialized = new AtomicBoolean();


    private Map<Integer, Channel> followers;
    private Map<Integer, Channel> deadFollowers;
    private Set<String> memberSet;
    private Map<Integer, String> memberAddrMap;

    private ServerBootstrap serverBootstrap;
    private Bootstrap bootstrap;
    /**
     * this channel refers to the server when the broker is a follower
     * else, null
     */
    private Channel channel;
    private List<Object> buffer;

    public EzBroker() {

    }

    public void runLeader() {
        logger.info("尝试启动leader");
        assertInitialized();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    private EzBroker broker;

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ObjectDecoder(1024 * 1024,
                                        ClassResolvers.weakCachingResolver(this.getClass().getClassLoader())))
                                .addLast(new ObjectEncoder())
                                .addLast(new ConnectHandler(broker))
                                .addLast(new IdleStateHandler(0, 10, 0))
                                .addLast(new HeartBeatHandler(broker))
                                .addLast(new PublisherHandler(broker))
                        ;
                    }
                    public ChannelInitializer<NioSocketChannel> accept(EzBroker broker) {
                        this.broker = broker;
                        return this;
                    }
                }.accept(this));
        serverBootstrap.bind(BrokerConfig.BROKER_PORT);
        logger.info("leader启动完成");
    }

    public void runFollower(String leaderAddr) {
        assertInitialized();
        SocketAddress socketAddress = new InetSocketAddress(leaderAddr, BrokerConfig.BROKER_PORT);
        bootstrap = new Bootstrap();
        ChannelFuture connect = bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    private EzBroker broker;


                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ObjectDecoder(1024 * 1024,
                                        ClassResolvers.weakCachingResolver(this.getClass().getClassLoader())))
                                .addLast(new ObjectEncoder())
                                .addLast(new ClientConnectHandler(broker))
                                .addLast(new HeartBeatHandler(broker))
                                .addLast(new PublishSynchronizer(broker));
                    }

                    public ChannelInitializer<NioSocketChannel> accept(EzBroker broker) {
                        this.broker = broker;
                        return this;
                    }
                }.accept(this))
                .connect(socketAddress);
        connect.awaitUninterruptibly();
        this.channel = connect.channel();
        logger.info("channel连接完成:{}", channel);
        channel.writeAndFlush(new Connect<>().getConnect()).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("follower启动完成");
            } else {
                logger.error("连接失败, {}", future);
            }
        });
    }

    public void init() {
        logger.info("broker开始初始化");
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        try {
            followers = new ConcurrentHashMap<>();
            deadFollowers = new ConcurrentHashMap<>();
            buffer = new ArrayList<>();
            memberAddrMap = new ConcurrentHashMap<>();
            memberSet = new ConcurrentSet<>();
            brokerMetaData = new BrokerMetaData();
        } catch (Exception e){
            initialized.compareAndSet(true, false);
            logger.error("broker初始化失败:{}", this);
            throw e;
        }
        logger.info("broker初始化完成, 部署在:{}", NetUtils.getLocalHostAddr());

    }

    public synchronized BrokerMetaData acceptBroker(Channel channel) {
        int id = brokerMetaData.getMembers().size() + 1;
        InetSocketAddress socketAddress = (InetSocketAddress)channel.remoteAddress();
        String hostAddress = socketAddress.getAddress().getHostAddress();
        if (memberSet.contains(hostAddress)) {
            return new BrokerMetaData();
        }
        brokerMetaData.getMembers().add(hostAddress);
        BrokerMetaData result = BrokerMetaData.builder()
                .id(id)
                .term(brokerMetaData.getTerm())
                .leaderId(brokerMetaData.getId())
                .members(brokerMetaData.getMembers())
                .build();
        followers.put(id, channel);
        logger.info("数据同步到到follower:{}", result);
        return result;
    }

    public void writeReplica(Object data) {
        int successCount;
        followers.forEach((k, v) -> {

        });
    }

    private void assertInitialized() {
        if (!initialized.get()) {
            throw new IllegalStateException("broker未初始化，需要先调用init方法");
        }
    }

    private void assertFollower() {
        if (!initialized.get()) {
            throw new IllegalStateException("broker未初始化，需要先调用init方法");
        }
        if (brokerMetaData == null) {
            throw new IllegalStateException("leader信息缺失");
        }
        if (brokerMetaData.getLeaderId() != null) {
            throw new IllegalStateException("这个broker不是follower");
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        EzBroker ezBroker = new EzBroker();
        Scanner sc = new Scanner(System.in);
        ezBroker.init();
        if (args.length != 0){
            if (args[0].equals("leader")) {
                ezBroker.runLeader();
            }
            if (args[0].equals("follower")) {
                ezBroker.runFollower(args[1]);
            }
        } else {
            System.out.println("start leader or follower?");
            String str = sc.nextLine();
            if (str.equals("leader")) {
                ezBroker.runLeader();
            } else {
                System.out.println("input your leader's hostname");
                ezBroker.runFollower(sc.nextLine());
            }
        }
        String str;
        while (!(str = sc.nextLine()).equals("quit")) {
            switch (str) {
                case "show followers": {
                    System.out.println("followers如下");
                    System.out.println(ezBroker.getFollowers());
                    System.out.println("无响应followers如下");
                    System.out.println(ezBroker.getDeadFollowers());
                    break;
                }
                case "am i leader": {
                    if (ezBroker.brokerMetaData.isLeader()) {
                        System.out.println("Yes");
                    } else {
                        System.out.println("No, your leader is");
                        System.out.println(ezBroker.brokerMetaData.getLeaderHostAddr());
                    }
                }
            }
        }
        Optional.of(ezBroker)
                .map(EzBroker::getServerBootstrap)
                .map(ServerBootstrap::config)
                .map(ServerBootstrapConfig::group)
                .ifPresent(EventExecutorGroup::shutdownGracefully);
        Optional.of(ezBroker)
                .map(EzBroker::getServerBootstrap)
                .map(ServerBootstrap::config)
                .map(ServerBootstrapConfig::childGroup)
                .ifPresent(EventExecutorGroup::shutdownGracefully);
        Optional.of(ezBroker)
                .map(EzBroker::getBootstrap)
                .map(Bootstrap::config)
                .map(BootstrapConfig::group)
                .ifPresent(EventExecutorGroup::shutdownGracefully);
    }


    /**
     * 防stackoverflow，只打部分数据
     */
    @Override
    public String toString() {
        return "EzBroker{" +
                "brokerMetaData=" + brokerMetaData +
                ", lastTrans=" + lastTrans +
                ", initialized=" + initialized +
                ", followers=" + followers +
                ", deadFollowers=" + deadFollowers +
                ", serverBootstrap=" + serverBootstrap +
                ", bootstrap=" + bootstrap +
                ", channel=" + channel +
                ", buffer=" + buffer +
                '}';
    }
}
