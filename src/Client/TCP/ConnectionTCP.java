package Client.TCP;

import Client.Client;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.UUID;

import static java.lang.System.exit;

public class ConnectionTCP {

    int serverPort = 6666;
    String address = null;
    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    public void sendUnigueID(Socket socket) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF(createKey());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String createKey() {
        File storeID = new File("uniqueID.txt");
        if (storeID.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(storeID);
                byte[] data = new byte[(int) storeID.length()];
                inputStream.read(data);
                inputStream.close();
                return new String(data, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            String uniqueID = UUID.randomUUID().toString();
            try {
                storeID.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(storeID);
                PrintStream outKey = new PrintStream(outputStream);
                outKey.print(uniqueID);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return uniqueID;
        }
        return null;
    }

    public Socket connect() {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        int serverPort = 9876;
        String address = null;
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
            for (int i = 1; i <= 3; i++) {
                try {
                    socket = new Socket(ipAddress, serverPort);
                    break;
                } catch (ConnectException e) {
                    System.out.println("Attempt " + i);
                    socket = null;
                    Thread.sleep(500);
                }
            }
            if (socket == null) {
                System.out.println("not connected");
                exit(1);
            }
            System.out.println("Connect");
            return socket;
        } catch (Exception x) {
            x.printStackTrace();
        }
        return null;
    }

    public void removeUniqueIDFile(){
        File storeID = new File("uniqueID.txt");
        storeID.delete();
    }

    public void close(String line, Socket socket) throws IOException, InterruptedException {
        if (line.equals("CLOSE\n")) {
            removeUniqueIDFile();
            System.out.println("close socket");
            Thread.sleep(10);
            socket.close();
            socket = null;
            exit(0);
        }
    }
}
