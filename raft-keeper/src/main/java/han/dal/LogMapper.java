package han.dal;


import java.util.List;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import han.Log;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Mapper
public interface LogMapper {
    void insert(@Param("term") int term, @Param("cmd") String cmd);
    List<Log> selectAll();

}
