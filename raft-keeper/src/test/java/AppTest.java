
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import han.dal.LogMapper;
import jakarta.annotation.Resource;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@SpringBootTest
public class AppTest {
    @Resource
    LogMapper logMapper;
    @Test
    public void fun() {
        logMapper.insert(1, "test");
        System.out.println(logMapper.selectAll());
        logMapper.insert(1, "test");
        System.out.println(logMapper.selectAll());
    }
}
