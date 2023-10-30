package han.mq;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import han.Cmd;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MQCmd implements Cmd {
    boolean push;
    String topic;
    String msg;

    public MQCmd() {
    }

    public MQCmd(boolean push, String topic, String msg) {
        this.push = push;
        this.topic = topic;
        this.msg = msg;
    }

    @Override
    public Object apply() {
        if (push) {
            if (!TopicQueue.map.containsKey(topic)) {
                TopicQueue.map.put(topic, new ConcurrentLinkedDeque<>());
            }
            TopicQueue.map.get(topic).addLast(msg);
            return null;
        }
        if (!TopicQueue.map.containsKey(topic)) {
            return null;
        }
        Deque<String> queue = TopicQueue.map.get(topic);
        synchronized (TopicQueue.map.get(topic)) {
            if (queue.isEmpty()) {
                return null;
            }
            return queue.removeFirst();
        }
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static void main(String[] args) {
        MQCmd mqCmd = new MQCmd();
        MQCmd mqCmd1 = new MQCmd(true, "hello", "test");
        mqCmd.setPush(false);
        mqCmd.setTopic("test");
        mqCmd.setMsg("hello");
        System.out.println(mqCmd.getTopic().equals(mqCmd1.getMsg()));
        System.out.println(mqCmd.isPush());
    }
}
