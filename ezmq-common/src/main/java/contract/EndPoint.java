package contract;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;

import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class EndPoint {
    private String hostname;

    public EndPoint(InetSocketAddress addr) {
        this.hostname = addr.getHostName();
    }

    public EndPoint(String hostname) {
        this.hostname = hostname;
    }
}
