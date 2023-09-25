package handler.follower;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.EzBroker;
import contract.PublishEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@AllArgsConstructor
public class PublishSynchronizer extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PublishSynchronizer.class);
    private EzBroker broker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof PublishEvent)) {
            ctx.fireChannelRead(msg);
            return;
        }
        PublishEvent event = (PublishEvent) msg;
        broker.getBuffer().add(event.getPayload());
        logger.debug("follower同步完成，新增数据为{}", event.getPayload());
    }
}
