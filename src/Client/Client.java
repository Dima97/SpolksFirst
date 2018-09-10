package Client;

import java.net.Socket;

public class Client {
    public static void main(String[] argv) {
        Connection connection = new Connection();
        Socket socket = connection.connect();
    }
}
