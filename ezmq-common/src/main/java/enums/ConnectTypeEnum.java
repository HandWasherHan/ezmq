package enums;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public enum ConnectTypeEnum {
    NEW_CONNECT("join"),
    REDIRECT("redirect"),
    WELCOME("welcome")
    ;
    private String type;

    ConnectTypeEnum(String type) {
        this.type = type;
    }
}
