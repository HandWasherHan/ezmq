package dto;

import java.io.Serializable;
import java.util.Map;

import common.Broker;
import common.EzBroker;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
@AllArgsConstructor
@Data
public class ClusterDTO implements Serializable {
    private EzBroker you;
    private Map<Integer, EzBroker> followers;
}
