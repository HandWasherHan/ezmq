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
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd.Connect;
import constant.BrokerConfig;
import contract.EndPoint;
import handler.follower.ClientConnectHandler;
import handler.leader.ConnectHandler;
import handler.shared.HeartBeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.BootstrapConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.bootstrap.ServerBootstrapConfig;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class EzBroker implements Broker{
    private static final Logger logger = LoggerFactory.getLogger(EzBroker.class);

    private EndPoint endPoint;
    private EzBroker leader;
    private int id;
    private int members;
    private long lastTrans; // 上次发心跳的时间
    private volatile boolean initialized;
    private int term;

    private Map<Integer, EzBroker> followers;
    private Map<Integer, EzBroker> deadFollowers;

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
    public EzBroker(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    public EzBroker(String hostname) {
        this.endPoint = new EndPoint(hostname);
    }

    public EzBroker(SocketAddress addr) {
        InetSocketAddress inet = (InetSocketAddress) addr;
        this.endPoint = new EndPoint(inet.getHostName());
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
                                .addLast(new HeartBeatHandler(broker));
                    }
                    public ChannelInitializer<NioSocketChannel> accept(EzBroker broker) {
                        this.broker = broker;
                        return this;
                    }
                }.accept(this));
        serverBootstrap.bind(BrokerConfig.INTER_PORT);
        logger.info("leader启动完成");
    }

    public void runFollower(String leaderAddr) {
        assertInitialized();
        SocketAddress socketAddress = new InetSocketAddress(leaderAddr, BrokerConfig.INTER_PORT);
        bootstrap = new Bootstrap();
        this.channel = bootstrap.group(new NioEventLoopGroup())
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
                                .addLast(new HeartBeatHandler(broker));
                    }
                    public ChannelInitializer<NioSocketChannel> accept(EzBroker broker) {
                        this.broker = broker;
                        return this;
                    }
                })
                .connect(socketAddress)
                .channel();
        channel.writeAndFlush(new Connect<Broker>(this).getConnect()).addListener(future -> {
            if (future.isSuccess()) {
                logger.info("follower启动完成，连接至:{}", leader);
            } else {
                logger.error("连接失败, {}", future);
            }
        });
    }

    public void init() {
        logger.info("broker开始初始化");
        if (initialized) {
            return;
        }
        try {
            initialized = true;
            endPoint.setHostAddr(InetAddress.getLocalHost().getHostAddress());
            followers = new ConcurrentHashMap<>();
            deadFollowers = new ConcurrentHashMap<>();
            buffer = new ArrayList<>();
        } catch (UnknownHostException uhe) {
            logger.error("", uhe);
        } catch (Exception e){
            initialized = false;
            logger.error("broker初始化失败:{}", this);
            throw e;
        }
        logger.info("broker初始化完成, 部署在:{}", endPoint.getHostAddr());

    }

    public synchronized EzBroker acceptBroker(Channel channel) {
        EzBroker comer = new EzBroker();
        comer.setChannel(channel);
        comer.setBuffer(new ArrayList<>());
        int id = members + 1;
        comer.setId(id);
        comer.setLeader(this);
        followers.put(id, comer);
        members++;
        comer.getBuffer().addAll(buffer);
        logger.info("数据同步到到follower:{}", comer);
        return comer;
    }

    public void writeReplica(Object data) {
        int successCount;
        followers.forEach((k, v) -> {

        });
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new IllegalStateException("broker未初始化，需要先调用init方法");
        }
    }

    private void assertFollower() {
        if (!initialized) {
            throw new IllegalStateException("broker未初始化，需要先调用init方法");
        }
        if (leader == null || leader.endPoint == null) {
            throw new IllegalStateException("leader信息缺失");
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        EzBroker ezBroker = new EzBroker(new InetSocketAddress(InetAddress.getLocalHost(), BrokerConfig.INTER_PORT));
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
                    if (ezBroker.leader == null) {
                        System.out.println("Yes");
                    } else {
                        System.out.println("No, your leader is");
                        System.out.println(ezBroker.leader);
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
     * @return
     */
    @Override
    public String toString() {
        return "EzBroker{" +
                "endPoint=" + endPoint +
                ", id=" + id +
                ", members=" + members +
                ", lastTrans=" + lastTrans +
                ", initialized=" + initialized +
                ", term=" + term +
                ", serverBootstrap=" + serverBootstrap +
                ", bootstrap=" + bootstrap +
                ", channel=" + channel +
                ", buffer=" + buffer +
                '}';
    }
}
