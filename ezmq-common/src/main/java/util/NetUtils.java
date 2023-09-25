package util;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class NetUtils {

    public static String getLocalHostAddr() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalHostAddr(SocketAddress socketAddress) {
        InetSocketAddress inet = (InetSocketAddress) socketAddress;
        return inet.getAddress().getHostAddress();
    }
}
