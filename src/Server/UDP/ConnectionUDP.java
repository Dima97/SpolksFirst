package Server.UDP;

import util.PacketsScope;

import java.io.*;
import java.net.*;
import java.util.Date;

public class ConnectionUDP {
    int counter = 0;
    private InetAddress address = null;
    private int port = 7001;
    private int sizeBuffer = 32001;
    byte[] buffer = new byte[sizeBuffer];
    private int totalSize = 0;
    byte num = 0;
    private int bufferStringSize = 256;
    private byte[] bufferforString = new byte[bufferStringSize];
    private PacketsScope packetsScope = new PacketsScope();


    public ConnectionUDP() {
        try {
            address = InetAddress.getByName("127.0.0.1");
            DatagramSocket socket = new DatagramSocket(7000);

            while (waitString(socket).equals("notOk")) ;

            System.out.println(address + " " + port);

            menu(socket);
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }

    public void menu(DatagramSocket socket) {
        String line = "";
        while (true) {
            try {
                while ((line = waitString(socket)).equals("notOk")) ;
                if (line.contains("ECHO")) {
                    echo(line, socket);
                }
                if (line.contains("TIME")) {
                    sendTime(socket);
                }
                if (line.contains("DOWNLOAD")) {
                    download(line, socket);
                }
                if (line.contains("CLOSE")) {
                    break;
                }
            }catch(IOException ex){
                System.out.println("suka");
            }
        }
    }

    public void sendTime(DatagramSocket socket) throws IOException {
        Date date = new Date();
        sendString(date.toString(), socket);
    }

    public void echo(String line, DatagramSocket socket) throws IOException {
        System.out.println(line);
        sendString(line.split(" ")[1], socket);
    }

    private void download(String line, DatagramSocket socket) throws IOException {
        packetsScope.clearScope();
        buffer = new byte[sizeBuffer];
        bufferforString = new byte[sizeBuffer];
        totalSize = 0;
        String length = null;
        System.out.println("The client downloads a file");
        line = line.replace("DOWNLOAD ", "");
        File file = new File(line);
        byte[] b = new byte[sizeBuffer];
        if (file.length() > 0) {
            length = String.valueOf(file.length());
            System.out.println(length);
            sendString("true", socket);
            DataInputStream reader = new DataInputStream(new FileInputStream(file));
            sendString(length, socket);
            String progress;
            while ((progress = waitString(socket)).equals("notOk")) ;
            reader.skipBytes(Integer.parseInt(progress));
            System.out.println("skipnul: " + progress);
            while (totalSize < file.length()) {
                fillScope(reader, file.length());

                String answer;
                while (!(answer = waitString(socket)).equals("kiday")){
                    if(answer.equals("Handshake")) {
                        return;
                    }
                }
                sendScope(socket);
            }
            reader.close();
        } else {
            sendString("false", socket);
        }
    }

    public void fillScope(DataInputStream reader, long fileSize) throws IOException {
        byte[] b = new byte[sizeBuffer - 1];
        byte[] pack = new byte[1];
        while (true) {

            if (packetsScope.isFull() || (totalSize == fileSize)) {
                num = 0;
                break;
            }

            totalSize += reader.read(b);
            pack[0] = num;


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(pack);
            baos.write(b);
            b = baos.toByteArray();

            if (packetsScope.setValue(num, b)) {
                pack = new byte[1];
                b = new byte[sizeBuffer - 1];

                num++;
            } else {
                num = 0;
                break;
            }
        }
        if (!packetsScope.isFull()) {
            System.out.println(totalSize);
        }
    }

    public void sendScope(DatagramSocket socket) throws IOException {
        for (int i = 0; i < packetsScope.getScope().size(); i++) {
            if (packetsScope.getScope().get(i) == null) break;
            sendBytes(packetsScope.getScope().get(i), packetsScope.getScope().get(i).length, socket);
            counter++;
        }

        sendBytes(new byte[]{-1}, 2, socket);
        buffer = new byte[sizeBuffer - 1];
        while (waitString(socket).equals("notOk")) {
            String number = receiveString(socket);
            if (packetsScope.getScope().get(Integer.parseInt(number)) == null) {
                sendBytes(new byte[]{-2}, 1, socket);
                break;
            }
            if (number == "all") break;
            if (number.length() > 0) {
                sendBytes(packetsScope.getScope().get(Integer.parseInt(number)),
                        packetsScope.getScope().get(Integer.parseInt(number)).length, socket);
            }
        }
        packetsScope.clearScope();
    }

    public DatagramPacket receive(DatagramSocket socket) throws IOException {
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        socket.receive(reply);
        return reply;
    }

    public String waitString(DatagramSocket socket) {
        DatagramPacket reply = new DatagramPacket(bufferforString, bufferforString.length);
        try {
            socket.setSoTimeout(100);
            socket.receive(reply);
        }
        catch (IOException e) {
            System.out.println(getMyString(reply));
            return "notOk";
        }
        return getMyString(reply);
    }

    public String receiveString(DatagramSocket socket) throws IOException {
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        socket.receive(reply);
        return getMyString(reply);
    }

    public String getMyString(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }

    public void sendString(String string, DatagramSocket socket) throws IOException {
        byte[] b = string.getBytes();
        DatagramPacket dp = new DatagramPacket(
                b, b.length, address, port);
        socket.send(dp);
    }

    public void sendBytes(byte[] bytes, int length, DatagramSocket socket) throws IOException {
        DatagramPacket dp = new DatagramPacket(
                bytes, bytes.length, address, port);
        socket.send(dp);
    }
}