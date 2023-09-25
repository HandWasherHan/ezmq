package handler.follower;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmd.Connect;
import common.Broker;
import common.EzBroker;
import constant.BrokerConfig;
import contract.BrokerMetaData;
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

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.channel().writeAndFlush(new Connect<>().getConnect());
//        ctx.fireChannelActive();
//    }

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Connect)) {
            ctx.fireChannelRead(msg);
            return;
        }
        Connect connect = (Connect) msg;
        logger.info("读取到来自leader的新连接响应:{}", connect);
        Object data = connect.getData();
        switch (connect.getType()) {
            case REDIRECT: {
                String hostname = (String) data;
                logger.info("重定向到:{}", hostname);
                InetSocketAddress inetAddr = new InetSocketAddress(hostname, BrokerConfig.BROKER_PORT);
                ctx.channel().connect(inetAddr);
                ctx.channel().writeAndFlush(new Connect<Broker>(broker).getConnect());
                return;
            }
            case WELCOME: {
                BrokerMetaData metaData = (BrokerMetaData) data;
                logger.info("获取到来自leader的集群信息:{}", metaData);
                broker.getBrokerMetaData().update(metaData);
                return;
            }
            default: {
                throw new IllegalAccessException();
            }
        }
    }
}
