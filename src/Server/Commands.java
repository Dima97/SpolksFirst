package Server;

import java.io.*;
import java.util.Date;

public class Commands {

    public void download(String line, DataOutputStream out, DataInputStream in, Connection connection) throws IOException {
        System.out.println("The client downloads a file");
        line = line.replace("DOWNLOAD ", "");
        File file = new File(line);
        byte[] b = new byte[1];//65536
        if (file.length() > 0) {
            out.writeUTF("true");
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            out.writeUTF(String.valueOf(file.length()));
            out.flush();
            int length = 0, progress = connection.getResumeStorage().getProgress(line);
            reader.skipBytes(progress);
            out.writeUTF(String.valueOf(progress));
            out.flush();
            while (true) {
                length = reader.read(b);
                if (length <= 0) {
//                    connection.getResumeStorage().removeCurrentFile(line);
                    break;
                }

                try {
                    Thread.sleep(10);
                    out.write(b, 0, length);
                    out.flush();
                    progress += b.length;
                    connection.getResumeStorage().refreshResume(line, progress);
                } catch (InterruptedException e) {
                    e.printStackTrace();
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
