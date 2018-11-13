package Server;

import Server.TCP.ConnectionTCP;
import Server.UDP.ConnectionUDP;

import java.io.IOException;

public class Server {
    public static void main(String[] argv) {
        ConnectionTCP connectionTCP = ConnectionTCP.getConnection();
        try {
            connectionTCP.connectWithClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
