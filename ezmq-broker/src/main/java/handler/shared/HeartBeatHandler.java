package handler.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.Broker;
import common.EzBroker;
import constant.BrokerConfig;
import constructure.MetaData;
import enums.MetaDataTypeEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * for broker client and server.
 * server sends the heartbeats and client read them
 *
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    private EzBroker broker;

    public HeartBeatHandler(EzBroker broker) {
        this.broker = broker;
    }

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    /**
     * 目前用于定时心跳发送，
     * 期望被设计成唯一可能探测到死broker复活的地方以防止deadFollowers的并发冲突，
     * 同时也是唯一leader broker发心跳的方法
     *
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 仅leader broker需要发送心跳
        if (!broker.getBrokerMetaData().isLeader()) {
            super.userEventTriggered(ctx, evt);
            return;
        }
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
                logger.info("heart beat sent");
                //向所有follower发送心跳消息，并在发送失败时关闭该连接
                Map<Integer, Channel> followers = broker.getFollowers();
                Map<Integer, Channel> deadFollowers = broker.getDeadFollowers();
                followers.forEach((k, v) -> {
                    v.writeAndFlush(MetaData.heartBeat()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                });
                // 死broker是否重新活了过来
                List<Integer> recovers = new ArrayList<>();
                deadFollowers.forEach((k, v) -> {
                    v.writeAndFlush(MetaData.heartBeat()).addListener((future) -> {
                        if (future.isSuccess()) {
                            recovers.add(k);
                        }
                    });
                });
                for (int id : recovers) {
                    // todo 复活的follower进行数据同步
                    followers.put(id, deadFollowers.remove(id));
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if ((msg instanceof MetaData) && ((MetaData<?>) msg).getType() == MetaDataTypeEnum.HEART_BEAT) {
            Long beatTime = ((MetaData<Long>) msg).getPayload();
            if (beatTime - broker.getLastTrans() > BrokerConfig.HEART_BEAT_BEAR) {
                // todo 心跳发晚了，follower以为leader已死，此时可能已经在选举新leader
            }
            broker.setLastTrans(beatTime);
            logger.debug("本次更新的心跳时间:{}", beatTime);
            return;
        }
        ctx.fireChannelRead(msg);
    }
}
