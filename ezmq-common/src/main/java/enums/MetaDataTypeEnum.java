package enums;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public enum MetaDataTypeEnum {
    LEADER_INFO("leader"),
    ACCEPT("accept"),
    HEART_BEAT("beat"),
    FOLLOWER_INFO("followers");
    private String code;

    MetaDataTypeEnum(String code) {
        this.code = code;
    }
}
