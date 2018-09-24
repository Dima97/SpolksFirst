package Server;

import java.io.*;
import java.util.Date;

public class Commands {

    public void download(String line, DataOutputStream out, DataInputStream in, Connection connection) throws IOException {
        System.out.println("The client downloads a file");
        line = line.replace("DOWNLOAD ", "");
        File file = new File(line);
        byte[] b = new byte[256];//65536
        if (file.length() > 0) {
            out.writeUTF("true");
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            out.writeUTF(String.valueOf(file.length()));
            out.flush();

            int progress = Integer.parseInt(in.readUTF());
            int length = 0;
            reader.skipBytes(progress);
            while (true) {
                length = reader.read(b);
                if (length <= 0) {
                    break;
                }

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(in.readBoolean()) {
                    out.write(b, 0, length);
                    out.flush();
                    progress += b.length;
                    connection.getResumeStorage().refreshResume(line, progress);
                }
            }
            reader.close();
        }
    }

    public String echo(String line, DataOutputStream out) throws IOException {
        System.out.println(line);
        out.writeUTF(line);
        out.flush();
        return line;
    }

    public void time( DataOutputStream out) throws IOException {
        Date date = new Date();
        out.writeUTF(date.toString());
        out.flush();
    }
}
