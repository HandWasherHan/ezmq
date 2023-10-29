package han;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import han.mock.KVSingleton;
import han.mock.MockCmd;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public interface Cmd extends Serializable {

    static Cmd decode(String str) {
        ObjectMapper objectMapper = new ObjectMapper();
        String[] split = str.split("\\$");
        try {
            return (Cmd) objectMapper.readValue(split[1], Class.forName(split[0]));
        } catch (JsonProcessingException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static String encode(Cmd cmd) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return cmd.getClass().getName() + "$" + objectMapper.writeValueAsString(cmd);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    void apply();

    static void main(String[] args) {
        System.out.println(KVSingleton.map);
        MockCmd mockCmd = new MockCmd(1, "hello");
        String encode = encode(mockCmd);
        System.out.println(encode);
        Cmd decode = decode(encode);
        decode.apply();
        System.out.println(KVSingleton.map);
    }
}
