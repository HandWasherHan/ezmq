package enums;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public enum AckLevelEnum {
    NO_ACK("0"),
    IMMEDIATE("1"),
    ALL("all"),
    PERSISTENCE("persistence")
    ;
    public String code;

    AckLevelEnum(String code) {
        this.code = code;
    }
}
