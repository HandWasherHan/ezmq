package contract;

import java.net.InetAddress;
import java.net.URL;

import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
public class EndPoint {
    private InetAddress addr;

    public EndPoint(InetAddress addr) {
        this.addr = addr;
    }
}
