package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class Connection {
    private static Connection instance;
    private Commands commands = new Commands();
    private ResumeStorage resumeStorage = new ResumeStorage();

    private Connection() {
    }

    public static Connection getConnection() {
        if (instance == null) {
            instance = new Connection();
        }
        return instance;
    }

    public void connectWithClient() {
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

                readUniqueId(socket);
                menu(socket, serverSocket);

            }catch(EOFException exc){
                System.out.println("client down!");
            }
            catch (SocketException e) {
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

    public void readUniqueId(Socket socket) {
        try {

            DataInputStream socketInputStream = new DataInputStream(socket.getInputStream());
            resumeStorage.setClientsResumeID(socketInputStream.readUTF());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void menu(Socket socket, ServerSocket serverSocket) throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        String line = "";
        while (close(line, socket, serverSocket)) {
            if (socket.isBound()) {
                line = in.readUTF();
                System.out.println(line);
            }
            System.out.println("received data: " + line);
            switch (line.split(" ")[0]) {
                case "DOWNLOAD": {
                    commands.download(line, out, in, this);
                    break;
                }
                case "ECHO": {
                    commands.echo(line, out);
                    break;
                }
                case "TIME\n": {
                    commands.time(out);
                    break;
                }
                default: {
                    System.out.println("Command not found");
                }
            }

            System.out.println("Waiting...");
            System.out.println();
        }
    }

    private boolean close(String line, Socket socket, ServerSocket ss) throws IOException {
        if (line.equals("CLOSE\n")) {
            resumeStorage.deleteCurrentResume();
            System.out.println("close socket");
            if (socket != null && ss != null) {
                socket.close();
                ss.close();
            }
            return false;
        }
        return true;
    }

    public ResumeStorage getResumeStorage(){
        return  resumeStorage;
    }
}
