package han.mq;

import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class TopicQueue {
    static Map<String, Deque<String>> map = new ConcurrentHashMap<>();

}
