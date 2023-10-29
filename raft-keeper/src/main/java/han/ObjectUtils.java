package han;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class ObjectUtils {
    public static void assertNotNull(Object ... args) {
        for (Object obj : args) {
            if (obj == null) {
                throw new IllegalArgumentException("参数为空");
            }
        }
    }
}
