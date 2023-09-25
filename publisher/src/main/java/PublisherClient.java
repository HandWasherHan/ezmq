import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import constant.BrokerConfig;
import contract.PublishEvent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class PublisherClient {
    private String hostAddr;
    private int state;
    private volatile AtomicBoolean initialized = new AtomicBoolean();
    private Channel channel;

    public void init() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }
        System.out.println("输入要连接的broker地址");
        Scanner sc = new Scanner(System.in);
        this.hostAddr = sc.nextLine();
        sc.close();
        channel = new Bootstrap()
                .remoteAddress(hostAddr, BrokerConfig.BROKER_PORT)
                .channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new ObjectEncoder())
                                .addLast(new ObjectDecoder(1024 * 1024,
                                        ClassResolvers.weakCachingResolver(this.getClass().getClassLoader())))
                                ;
                    }
                })
                .connect()
                .channel();
    }

    public void write(Object obj) {
        assertInitialized();
        channel.writeAndFlush(new PublishEvent<>(1, obj, "test", 0, 0));
    }


    private void assertInitialized() {
        if (!initialized.get()) {
            throw new IllegalStateException("未初始化");
        }
    }

    public static void main(String[] args) {
        PublisherClient publisherClient = new PublisherClient();
        publisherClient.init();
        Scanner sc = new Scanner(System.in);
        String str;
        while (!Objects.equals(str = sc.nextLine(), "quit")) {
            publisherClient.write(str);
        }
    }
}
