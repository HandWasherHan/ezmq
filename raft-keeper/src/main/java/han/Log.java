package han;


/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class Log {
    int term;
    String cmd;

    public Log(int term, String cmd) {
        this.term = term;
        this.cmd = cmd;
    }

    public Log(int term, Cmd cmd) {
        this.term = term;

    }

    public int getTerm() {
        return term;
    }

    public String getCmd() {
        return cmd;
    }

    @Override
    public String toString() {
        return "Log{" +
                "term=" + term +
                ", cmd='" + cmd + '\'' +
                '}';
    }
}
