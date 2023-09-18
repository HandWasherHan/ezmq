package response;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public interface Response<T> {
    boolean isSuccess();

    void setData(T data);


}
