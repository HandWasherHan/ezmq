package handler.leader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.Broker;
import common.EzBroker;
import contract.PublishEvent;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * used by leader to solve publish events
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class PublisherHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PublisherHandler.class);
    private EzBroker broker;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof PublishEvent)) {
            ctx.fireChannelRead(msg);
            return;
        }
        int members = broker.getMembers();
        int successCount = 0;
        Map<Integer, Broker> followers = broker.getFollowers();
        List<Integer> successList = new ArrayList<>();
        List<Integer> failList = new ArrayList<>();
        followers.forEach((k, v) -> {
            EzBroker ezBroker = (EzBroker) v;
            ChannelFuture channelFuture = ezBroker.getChannel().writeAndFlush(msg);
            channelFuture.addListener((f) -> {
                if (f.isSuccess()) {
                    successList.add(k);
                } else {
                    failList.add(k);
                }
            });
        });
        PublishEvent<?> event = (PublishEvent<?>) msg;
        if (successList.size() * 2 > members) {
            ctx.writeAndFlush(PublishEvent.ok(event.getId()));
        } else {
            ctx.writeAndFlush(PublishEvent.fail(event.getId(), "集群不可用，半数以上的集群未响应"));
        }


    }
}
