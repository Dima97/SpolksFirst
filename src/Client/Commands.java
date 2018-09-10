package Client;

import java.io.*;
import java.net.Socket;

public class Commands {
    private static long progress = 0;
    private static long oldProgress = 0;
    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

    public void startListen(Socket socket, Connection connection) {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            String line = null;
            while (true) {
                System.out.print(">");
                line = keyboard.readLine();
                line = command(line);
                out.writeUTF(line);
                out.flush();
                echo(line, in);
                time(line, in);
                connection.close(line, socket);
                download(line, in, socket);
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

    private void download(String line, DataInputStream in, Socket socket) throws IOException {
        if(line.contains("DOWNLOAD")) {
            if (in.readUTF().equals("true")) {
                line = line.replace("DOWNLOAD ", "");
                File file = new File("1" + line);
                DataOutputStream writer = new DataOutputStream(new FileOutputStream(file));
                byte[] b = new byte[65536];
                long size = Long.parseLong(in.readUTF());
                int length = 0;
                while (file.length() < size) {
                    length = in.read(b);
                    writer.write(b, 0, length);
                    progress(file.length(), size);
                }
                writer.close();
            }else {
                System.out.println("there is no such file");
            }
        }
    }

    private void progress(long length, long size){
        progress = (length*100/size);
        if(progress % 1 == 0 && progress != oldProgress) {
            System.out.print('\r');
            System.out.print("Progress: " + progress + "%");
            oldProgress = progress;
        }
    }

    private String command(String line) {
        if(line.contains("ECHO") || line.equals("TIME") || line.equals("CLOSE")) {
            return new String(line + "\n");
        }
        return line;
    }

    private void time(String line, DataInputStream in) throws IOException {
        if(line.equals("TIME\n")) {
            line = in.readUTF();
            System.out.println("Time: " + line);
        }
    }

    private void echo(String line, DataInputStream in) throws IOException {
        if(line.contains("ECHO")) {
            line = in.readUTF();
            System.out.println("Echo: " + line.split(" ")[1]);
        }
    }
}
