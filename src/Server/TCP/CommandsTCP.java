package Server.TCP;

import util.ClientInfo;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Date;

public class CommandsTCP {

    public void echo(String command, SocketChannel ch) throws IOException {
        if (command.contains("ECHO")) {
            System.out.println("echo");
            command = command.replaceFirst("ECHO ", "");
            ch.write(ByteBuffer.wrap(command.getBytes()));
        }
    }

    public void time(String command, SocketChannel ch) throws IOException {
        if (command.equals("TIME")) {
            Date date = new Date();
            ch.write(ByteBuffer.wrap(date.toString().getBytes()));
        }
    }

    public void isDownload(String line, SocketChannel ch, Selector sel, int index, File file, ArrayList<ClientInfo> list) throws IOException {
        if (line.contains("DOWNLOAD")) {
            System.out.println("The client downloads a file");
            line = line.replace("DOWNLOAD ", "");
            file = list.get(index).file = new File(line);
            if (file.length() > 0) {
                ch.register(sel, SelectionKey.OP_WRITE);
                try {
                    ch.write(ByteBuffer.wrap("true".getBytes()));
                    Thread.sleep(10);
                    ch.write(ByteBuffer.wrap(String.valueOf(file.length()).getBytes()));
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                list.get(index).flag = true;
                list.get(index).reader = new DataInputStream(new FileInputStream(file));
            } else {
                ch.write(ByteBuffer.wrap("false".getBytes()));
            }
        }
    }

    public void download(SocketChannel ch, Selector sel, int index, ArrayList<ClientInfo> list, DataInputStream reader) throws IOException {
        if (list.get(index).flag) {
            reader = list.get(index).reader;
            byte[] b = new byte[1300];
            int length = 0;
            length = reader.read(b);
            if (length <= 0) {
                System.out.println("фсио");
                reader.close();
                ch.register(sel, SelectionKey.OP_READ);
                list.get(index).flag = false;
                return;
            }
            ch.write(ByteBuffer.wrap(b, 0, length));
        }
    }
}
