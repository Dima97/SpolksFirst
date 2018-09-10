package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Connection {
    private static Connection instance;

    private Connection() {
    }

    public static Connection getConnection(){
        if(instance == null){
            instance = new Connection();
        }
        return instance;
    }

    public void connectWithClient(){
        int port = 6666;
        ServerSocket serverSocket = null;
        while (true) {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
                serverSocket = new ServerSocket(port);
                System.out.println("Waiting for a client...");

                Socket socket = null;
                socket = serverSocket.accept();
                socket.setKeepAlive(true);
                System.out.println("Client connected");

                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                String line = "";
                while (close(line, socket, serverSocket)) {
                    if (socket.isBound()) {
                        line = in.readUTF();
                    }

                    System.out.println("Waiting...");
                    System.out.println();
                }
            } catch (SocketException e) {
                if (serverSocket != null) {
                    try {
                        serverSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }

    private static boolean close(String line, Socket socket, ServerSocket ss) throws IOException {
        if (line.equals("CLOSE\n")) {
            System.out.println("close socket");
            if (socket != null && ss != null) {
                socket.close();
                ss.close();
            }
            return false;
        }
        return true;
    }
}
