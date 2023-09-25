package cmd;

import java.io.Serializable;
import java.util.Map;

import common.Broker;
import common.EzBroker;
import contract.BrokerMetaData;
import dto.ClusterDTO;
import enums.ConnectTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class Connect<T> implements Serializable {
    private T data;
    private ConnectTypeEnum type;

    public Connect(T data) {
        this.data = data;
    }

    public Connect<T> getConnect() {
        this.type = ConnectTypeEnum.NEW_CONNECT;
        return this;
    }

    public Connect<T> redirect() {
        this.type = ConnectTypeEnum.REDIRECT;
        return this;
    }

    public static Connect<BrokerMetaData> welcome(BrokerMetaData metaData) {
        return new Connect<>(metaData, ConnectTypeEnum.WELCOME);
    }


}
