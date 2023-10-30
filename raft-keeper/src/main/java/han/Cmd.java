package han;

import java.io.Serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import han.kv.KVSingleton;
import han.kv.KVCmd;

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

    Object apply();

    static void main(String[] args) {
        System.out.println(KVSingleton.map);
        KVCmd KVCmd = new KVCmd(1, "hello");
        String encode = encode(KVCmd);
        System.out.println(encode);
        Cmd decode = decode(encode);
        System.out.println(decode.apply());
        System.out.println(KVSingleton.map);
    }
}
