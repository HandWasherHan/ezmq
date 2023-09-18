package response;

import enums.AckLevelEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@AllArgsConstructor
@Data
public class ProduceResponse<T> implements Response<T>{
//    private AckLevelEnum ackLevel;
    private T data;
    /**
     * 0：成功
     * 1：失败
     * -1： 未知
     */
    private int status;
    private String msg;
    public static <T> Response<T> ok(T data) {
        return new ProduceResponse<T>(data, 0, null);
    }
    public static <T> Response<T> fail(T data, String msg) {
        return new ProduceResponse<T>(data, 1, msg);
    }

    public static <T> Response<T> timeout(T data, String msg) {
        return new ProduceResponse<T>(data, -1, msg);
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public void setData(T data) {
        this.data = data;
    }
}
