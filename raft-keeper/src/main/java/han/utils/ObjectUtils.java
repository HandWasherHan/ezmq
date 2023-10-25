package han.utils;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ObjectUtils {
    public static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("入参为空");
        }
    }
}
