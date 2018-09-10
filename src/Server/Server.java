package Server;

import java.io.*;
import java.net.*;
import java.util.Date;

public class Server {
    public static void main(String[] argv) {
        Connection connection = Connection.getConnection();
        connection.connectWithClient();
    }
}
