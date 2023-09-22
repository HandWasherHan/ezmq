package handler.follower;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd.Connect;
import common.Broker;
import common.EzBroker;
import constant.BrokerConfig;
import dto.ClusterDTO;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ClientConnectHandler extends ChannelInboundHandlerAdapter {
    public static final Logger logger = LoggerFactory.getLogger(ClientConnectHandler.class);
    private EzBroker broker;

    public ClientConnectHandler(EzBroker broker) {
        this.broker = broker;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(new Connect<Broker>(broker).getConnect());
        ctx.fireChannelActive();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Connect)) {
            ctx.fireChannelRead(msg);
            return;
        }
        Connect connect = (Connect) msg;
        Object data = connect.getData();
        switch (connect.getType()) {
            case REDIRECT: {
                EzBroker leader = (EzBroker) data;
                logger.info("重定向到:{}", leader);
                String hostname = leader.getEndPoint().getHostAddr();
                InetSocketAddress inetAddr = new InetSocketAddress(hostname, BrokerConfig.INTER_PORT);
                ctx.channel().connect(inetAddr);
                ctx.channel().writeAndFlush(new Connect<Broker>(broker).getConnect());
                return;
            }
            case WELCOME: {
                ClusterDTO cluster = (ClusterDTO) data;
                logger.info("获取到来自leader的集群信息:{}", cluster.getFollowers());
                broker.setLeader(cluster.getYou().getLeader());
                broker.setId(cluster.getYou().getId());
                logger.info("本机编号:{}", broker.getId());
                broker.setFollowers(cluster.getFollowers());
                return;
            }
            default: {
                throw new IllegalAccessException();
            }
        }
    }
}
