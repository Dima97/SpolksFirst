package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.System.exit;

public class Connection {

    int serverPort = 6666;
    String address = null;
    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
    public Socket connect() {
        System.out.print("input Server ip \n>");
        while (true) {
            try {
                address = keyboard.readLine();
                break;
            } catch (IOException e) {
                System.out.println("uncorrected ip");
            }
        }
        try {
            InetAddress ipAddress = InetAddress.getByName(address);
            Socket socket = null;
            for (int i = 1; i <= 3; i++){
                try {
                    socket = new Socket(ipAddress, serverPort);
                    socket.setKeepAlive(true);
                    break;
                }catch (ConnectException e){
                    System.out.println("Attempt " + i);
                    socket = null;
                    Thread.sleep(500);
                }
            }
            if(socket == null){
                System.out.println("not connected");
                exit(1);
            }
            System.out.println("Connect");
            return socket;
        }  catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    public void close(String line, Socket socket) throws IOException, InterruptedException {
        if(line.equals("CLOSE\n")) {
            System.out.println("close socket");
            Thread.sleep(10);
            socket.close();
            socket = null;
            exit(0);
        }
    }
}
