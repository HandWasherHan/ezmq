package constructure;

import java.io.Serializable;
import java.util.Map;

import enums.MetaDataTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 这个类写的不好，需要用到broker的地方太多了
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@Data
@AllArgsConstructor
public class MetaData<T> implements Serializable {
    private T payload;
    private int version;
    private MetaDataTypeEnum type;

    public static <T> MetaData<T> leaderData(T data) {
        return new MetaData<>(data, 0, MetaDataTypeEnum.LEADER_INFO);
    }

    public static <T> MetaData<T> acceptData(T data) {
        return new MetaData<>(data, 0, MetaDataTypeEnum.ACCEPT);
    }
    public static MetaData<Long> heartBeat() {
        return new MetaData<>(System.currentTimeMillis(), 0, MetaDataTypeEnum.HEART_BEAT);
    }

    /**
     * 建立连接时，leader将自己与所有followers,deadFollowers的信息打包发给client
     * 注意，希望follower并不保存其他followers的状态，它们是死是活不是一个follower该管的
     * @param cluster 完整的broker集群
     * @return 打包后的metadata
     */
    public static <T> MetaData<Map<Integer, T>> clusterData(Map<Integer, T> cluster) {
        return new MetaData<>(cluster, 0, MetaDataTypeEnum.FOLLOWER_INFO);
    }

}
