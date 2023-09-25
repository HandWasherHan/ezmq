package handler.leader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import common.EzBroker;
import contract.PublishEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.AllArgsConstructor;

/**
 * used by leader to solve publish events
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@AllArgsConstructor
public class PublisherHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(PublisherHandler.class);
    private EzBroker broker;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof PublishEvent)) {
            ctx.fireChannelRead(msg);
            return;
        }
        logger.info("接收到写入请求:{}", msg);
        int members = broker.getMemberAddrMap().size();
        Map<Integer, Channel> followers = broker.getFollowers();
        List<Integer> successList = new ArrayList<>();
        List<Integer> failList = new ArrayList<>();
        followers.forEach((k, v) -> {
            v.writeAndFlush(msg).addListener(f -> {
                if (f.isSuccess()) {
                    successList.add(k);
                } else {
                    failList.add(k);
                }
            });
        });
        PublishEvent<?> event = (PublishEvent<?>) msg;
        logger.debug("写入完成，成功写入了{}个副本", successList.size());
        if (successList.size() * 2 > members) {
            ctx.writeAndFlush(PublishEvent.ok(event.getId()));
        } else {
            ctx.writeAndFlush(PublishEvent.fail(event.getId(), "集群不可用，半数以上的集群未响应"));
        }
        // 写入失败的followers视为已死
        for (int id : failList) {
            broker.getDeadFollowers().put(id, followers.remove(id));
        }
    }
}
