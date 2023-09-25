package handler.follower;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.EzBroker;
import constant.BrokerConfig;
import handler.event.VoteEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.internal.ConcurrentSet;
import util.NetUtils;

/**
 * activated when a follower find its leader doesn't keep the heartbeat frequency
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class VoteHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(VoteHandler.class);
    private final EzBroker broker;
    // 是否已投过选票
    private boolean voted;
    private final Set<Integer> voteSet = new ConcurrentSet<>();

    public VoteHandler(EzBroker broker) {
        this.broker = broker;
    }

    /**
     * 周期性检测当前leader是否过久无心跳
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (!(evt instanceof IdleStateEvent)) {

        }
        // 当某个follower记录的leader过久没心跳，触发之，给其他followers发送选举通知，表示自己参选
        voted = true;
        voteSet.add(broker.getBrokerMetaData().getId());
        broker.getMemberAddrMap().forEach((k, v) -> {
            broker.getBootstrap().connect(v, BrokerConfig.BROKER_PORT);
            ctx.writeAndFlush(VoteEvent.canvass()).addListener(future -> {
                    logger.info("vote接收到来自{}的回信:{}, 由{}线程处理", k, future, Thread.currentThread());
                    if (future.isSuccess())
                        voteSet.add(k);
                });
        });
        Thread.sleep(BrokerConfig.WAIT_VOTE_TIME);
        if (voteSet.size() * 2 > broker.getMemberAddrMap().size()) {
            // todo 自己当选
        } else {
            // todo 等别人当选 or 选举失败，重新选举
        }
    }

    /**
     * 接收并处理拉票消息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof VoteEvent)) {
            ctx.fireChannelRead(msg);
        }
        assert msg instanceof VoteEvent;
        VoteEvent vote = (VoteEvent) msg;
        logger.info("接收到选举相关消息:{}", vote);
        if (vote.getType() == VoteEvent.CANVASS) {
            logger.debug("新的candidate, 来自于channel:{}", ctx.channel());
            if (voted) {
                ctx.channel().newFailedFuture(new IllegalStateException("已投出选票"));
            } else {
                ctx.writeAndFlush(VoteEvent.elected(broker.getBrokerMetaData().getId()));
                voted = true;
            }
        } else if (vote.getType() > 0) {
          logger.info("接收到当选消息:{}, 来自于channel:{}", vote, ctx.channel());
          broker.getBrokerMetaData().setLeaderId(vote.getType());
          broker.getBrokerMetaData().setLeaderHostAddr(NetUtils.getLocalHostAddr(ctx.channel().remoteAddress()));
          voted = false;
        } else {
            logger.error("错误的voteEvent:{}", vote);
        }

    }
}
