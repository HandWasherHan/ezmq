package contract;

import java.net.InetSocketAddress;

import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class EndPoint {
    private String hostAddr;

    public EndPoint(InetSocketAddress addr) {
        this.hostAddr = addr.getHostName();
    }

    public EndPoint(String hostAddr) {
        this.hostAddr = hostAddr;
    }
}
