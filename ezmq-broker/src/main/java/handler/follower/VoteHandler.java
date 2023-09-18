package handler.follower;

import common.EzBroker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * activated when a follower find its leader doesn't keep the heartbeat frequency
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class VoteHandler extends ChannelInboundHandlerAdapter {
    private EzBroker broker;

    public VoteHandler(EzBroker broker) {
        this.broker = broker;
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        ctx.fireUserEventTriggered(evt);
    }
}
