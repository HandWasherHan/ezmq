package han.dq;

import java.io.Serializable;
import java.util.Objects;

import han.Cmd;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class DQCmd implements Cmd, Serializable {
    boolean push;
    String msg;

    public DQCmd() {
    }

    public DQCmd(boolean push, String msg) {
        this.push = push;
        this.msg = msg;
    }

    @Override
    public void apply() {
        if (push) {
            DequeSingleton.queue.addLast(msg);
        } else {
            String s = DequeSingleton.queue.removeFirst();
            System.out.println(s);
        }
    }

    public boolean isPush() {
        return push;
    }

    public void setPush(boolean push) {
        this.push = push;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static void main(String[] args) {
        DQCmd dqCmd = new DQCmd();
        dqCmd.apply();
        dqCmd.setPush(true);
        dqCmd.setMsg("");
        if (Objects.equals(dqCmd.getMsg(), "")) {
            System.out.println(dqCmd.isPush());
        }
    }
}
