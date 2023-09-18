package template;

import java.util.function.Supplier;

import io.netty.channel.EventLoopGroup;
import response.Response;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public abstract class NetTransportTemplate {
    public Response<Object> send(Object obj, EventLoopGroup sender){
        return null;
    }
}
