package handler.leader;

import java.util.HashMap;
import java.util.Map;

import cmd.Connect;
import common.Broker;
import common.EzBroker;
import constructure.MetaData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 用于broker之间的连接
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ConnectHandler extends ChannelInboundHandlerAdapter {
    private EzBroker broker;

    public ConnectHandler(EzBroker broker) {
        this.broker = broker;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof Connect)) {
            ctx.fireChannelRead(msg);
            return;
        }
        Connect connect = (Connect) msg;
        switch (connect.getType()) {
            // server接收到client的连接
            case NEW_CONNECT: {
                // 当前broker已不是leader，则发送重定向消息，将真正的leader发回去
                if (broker.getLeader() != null) {
                    ctx.channel().writeAndFlush(new Connect<Broker>(broker.getLeader()).redirect());
                    ctx.fireChannelRead(msg);
                    return;
                }
                // 将整个集群信息发回去
                EzBroker comer = (EzBroker)(((Connect<?>) msg).getData());
                broker.acceptBroker(comer);
                Map<Integer, Broker> clusterMap = new HashMap<>(broker.getFollowers());
                clusterMap.putAll(broker.getDeadFollowers());
                ctx.channel().writeAndFlush(Connect.welcome(comer, clusterMap));
                return;
            }

        }
    }


}
