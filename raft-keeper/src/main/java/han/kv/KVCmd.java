package han.kv;

import han.Cmd;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class KVCmd implements Cmd {
    int id;
    String msg;

    public KVCmd() {
    }

    public KVCmd(int id, String msg) {
        this.id = id;
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public Object apply() {
        KVSingleton.map.put(id, msg);
        return null;
    }

    /**
     * used for suppress the "unused" warnings.
     * these methods are actually used by ObjectMapper in Jackson.
     * but my ide - idea, doesn't know that.
     */
    public static void main(String[] args) {
        KVCmd KVCmd = new KVCmd();
        KVCmd.setId(1);
        KVCmd.setMsg("hello");
        System.out.println(KVCmd.getMsg() + KVCmd.getId());
    }
}
