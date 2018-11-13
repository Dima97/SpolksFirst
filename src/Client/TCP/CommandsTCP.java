package Client.TCP;

import Client.Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.System.exit;

public class CommandsTCP {
    private static long progress = 0;
    private static long oldProgress = 0;
    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    public void startListen(Socket socket, ConnectionTCP connection) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String line = null;
            while (true) {
                System.out.print(">");
                line = keyboard.readLine();
                //out.writeUTF(line);
                out.write(line.getBytes());
                out.flush();
                echo(line, in);
                time(line, in);
                close(line, socket);
                download(line, in, out, socket);
            }
        } catch (Exception x) {
            System.out.println("server down!");
        }
    }

    public static void download(String line, DataInputStream in, DataOutputStream out, Socket socket) throws IOException {
        if (line.contains("DOWNLOAD")) {
            if (readString(in).equals("true")) {
                line = line.replace("DOWNLOAD ", "");
                File file = new File(line);
                DataOutputStream writer = new DataOutputStream(new FileOutputStream(file, true));
                byte[] b = new byte[1300];
                String s = readString(in);
                System.out.println(s);
                long size = Long.parseLong(s.replaceAll("[^0-9]",""));
                System.out.println(size);
                int length = 0;
                long fileLength = 0;
                long start = System.currentTimeMillis();
                while ( file.length() < size) {
                    length = in.read(b);
                    fileLength = fileLength + length;
                    writer.write(b, 0, length);
                    progress(file.length(), size);
                }
                long end = (System.currentTimeMillis() - start) / 1000;
                if(end != 0) {
                    System.out.println("\nspeed: " + (file.length() / 1048576) / end + " Mb/s");
                }else System.out.println("\nspeed: 100500 Mb/s");
                writer.close();
            } else {
                System.out.println("there is no such file");
            }
        }
    }

    public static void progress(long length, long size) {
        progress = (length * 100 / size);
        if (progress % 1 == 0 && progress != oldProgress) {
            System.out.print('\r');
            System.out.print("Progress: " + progress + "%");
            oldProgress = progress;
        }
    }

    private static void close(String line, Socket socket) throws IOException, InterruptedException {
        if (line.equals("CLOSE")) {
            System.out.println("close socket");
            Thread.sleep(10);
            socket.close();
            socket = null;
            exit(0);
        }
    }


    public static void time(String line, DataInputStream in) throws IOException {
        if (line.equals("TIME")) {
            System.out.println("Time: " + readString(in));
        }
    }

    public static void echo(String line, DataInputStream in) throws IOException {
        if (line.contains("ECHO")) {
            System.out.println("Echo: " +readString(in));
        }
    }
    public static String readString(DataInputStream in) {
        byte [] b = new byte[1300];
        int length;
        try {
            length = in.read(b);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return new String(b,0,length);
    }

    private String command(String line) {
        if (line.contains("ECHO") || line.equals("TIME") || line.equals("CLOSE")) {
            return new String(line + "\n");
        }
        return line;
    }
}
