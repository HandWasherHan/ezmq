package han.mock;

import han.Cmd;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MockCmd implements Cmd {
    int id;
    String msg;

    public MockCmd() {
    }

    public MockCmd(int id, String msg) {
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
    public void apply() {
        KVSingleton.map.put(id, msg);
    }

    /**
     * used for suppress the "unused" warnings.
     * these methods are actually used by ObjectMapper in Jackson.
     * but my ide - idea, doesn't know that.
     */
    public static void main(String[] args) {
        MockCmd mockCmd = new MockCmd();
        mockCmd.setId(1);
        mockCmd.setMsg("hello");
        System.out.println(mockCmd.getMsg() + mockCmd.getId());
    }
}
