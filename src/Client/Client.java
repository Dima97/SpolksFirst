package Client;

import Client.TCP.CommandsTCP;
import Client.TCP.ConnectionTCP;
import Server.UDP.ConnectionUDP;

import java.net.Socket;

public class Client {
    public static void main(String[] argv) {
        ConnectionTCP connectionTCP = new ConnectionTCP();
        Socket socket = connectionTCP.connect();
        CommandsTCP commandsTCP = new CommandsTCP();
        commandsTCP.startListen(socket, connectionTCP);
    }
}
