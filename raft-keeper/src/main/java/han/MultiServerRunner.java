package han;

import java.util.Scanner;

import han.grpc.SenderListSingleton;

/**
 * @author han <handwasherhan@gmail.com>
 * Created on 2023
 */
public class MultiServerRunner {

    public static void main(String[] args) {
        SenderListSingleton.init(true);
        System.out.println("running...input [quit] to quit");
        Scanner scanner = new Scanner(System.in);
        while (!scanner.nextLine().equals("quit")) {

        }
    }
}
