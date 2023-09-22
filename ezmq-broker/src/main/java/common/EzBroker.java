package common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constant.BrokerConfig;
import contract.EndPoint;
import handler.follower.ClientConnectHandler;
import handler.leader.ConnectHandler;
import handler.shared.HeartBeatHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
     * this channel refers to the leader when the broker is a follower
     * else, null
     */
    private Channel channel;
    private List<Object> buffer;


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
    public void runServer() {
        logger.info("尝试启动server");
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
                                .addLast(new ConnectHandler(broker))
                                .addLast(new HeartBeatHandler(broker));
                    }
                    public ChannelInitializer<NioSocketChannel> accept(EzBroker broker) {
                        this.broker = broker;
                        return this;
                    }
                }.accept(this));
        serverBootstrap.bind(BrokerConfig.INTER_PORT);
        logger.info("server启动完成");
    }

    public void runClient(String leaderHostname) {
        assertInitialized();
        SocketAddress socketAddress = new InetSocketAddress(leaderHostname, BrokerConfig.INTER_PORT);
        bootstrap = new Bootstrap();
        this.channel = bootstrap.group(new NioEventLoopGroup())
                .remoteAddress(socketAddress)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    private EzBroker broker;
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline()
                                .addLast(new ClientConnectHandler(broker))
                                .addLast(new HeartBeatHandler(broker));
                    }
                    public ChannelInitializer<NioSocketChannel> accept(EzBroker broker) {
                        this.broker = broker;
                        return this;
                    }
                })
                .connect()
                .channel();
    }

    public void init() {
        logger.info("broker开始初始化");
        if (initialized) {
            return;
        }
        try {
            initialized = true;
            endPoint.setHostname(InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException uhe) {
            logger.error("", uhe);
        } catch (Exception e){
            initialized = false;
            logger.error("broker初始化失败:{}", this);
            throw e;
        }
        logger.info("broker初始化完成, 部署在:{}", endPoint.getHostname());

    }

    public synchronized void acceptBroker(EzBroker broker) {
        int id = members + 1;
        EzBroker ezBroker = (EzBroker) broker;
        ezBroker.setId(id);
        ezBroker.setLeader(this);
        followers.put(id, broker);
        members++;
        ezBroker.getBuffer().addAll(buffer);
        logger.info("数据同步到到follower:{}", broker);
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
        ezBroker.init();
        if (args[0].equals("server")) {
            ezBroker.runServer();
        }
        if (args[0].equals("client")) {
            ezBroker.runClient(args[1]);
        }
    }



}
