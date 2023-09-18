package constant;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class BrokerConfig {
    // 多久没心跳会认为leader已死
    public static final long HEART_BEAT_BEAR = 30 * 1000;
    public static final int INTER_PORT = 9908;
    public static final int PUBLIC_PORT = 9907;
}
