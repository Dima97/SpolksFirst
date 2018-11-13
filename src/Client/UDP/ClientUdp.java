package Client.UDP;

import util.PacketsScope;

import java.io.*;
import java.net.*;
import java.util.Arrays;


public class ClientUdp {
    private int counter = 0;
    private long progress;
    private byte[] buffer = null;
    private byte[] bufferforString = null;
    private InetAddress address = null;
    private int port = 7000;
    private int bufferSize = 32001;
    private int bufferStringSize = 256;
    private long oldProgress;
    long totalSize = 0;
    DatagramPacket receive = null;
    private int num = 127;
    BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
    private PacketsScope packetsScope = new PacketsScope();

    ClientUdp() {
        DatagramSocket socket = null;
        buffer = new byte[bufferSize];
        bufferforString = new byte[bufferStringSize];
        try {
            address = InetAddress.getByName("127.0.0.1");
            socket = new DatagramSocket(7001);
            sendString("Handshake", socket);
            menu(socket);
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }

    public void menu(DatagramSocket socket) {
        try {
            String line = null;
            while (true) {
                System.out.print(">");

                line = keyboard.readLine();

                if (line.contains("ECHO")) {
                    echo(line, socket);
                }
                if (line.equals("TIME")) {
                    getTime(socket);
                }
                if (line.contains("DOWNLOAD")) {
                    download(line, socket);
                }
                if (line.equals("CLOSE")) {
                    close(socket);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(DatagramSocket socket) throws IOException {
        sendString("CLOSE", socket);
    }

    public void getTime(DatagramSocket socket) throws IOException {
        sendString("TIME", socket);
        System.out.println("Time from server: " + receiveString(socket));
    }

    public void echo(String line, DatagramSocket socket) throws IOException {
        sendString(line, socket);
        System.out.println("received echo:" + receiveString(socket));
    }

    public void download(String line, DatagramSocket socket) throws IOException {
        packetsScope.clearScope();
        buffer = new byte[bufferSize];
        bufferforString = new byte[bufferSize];
        sendString(line, socket);
        if (line.contains("DOWNLOAD")) {
            if (receiveString(socket).equals("true")) {
                Arrays.fill(buffer, (byte) 0);
                line = line.replace("DOWNLOAD ", "");
                File file = new File(line);
                DataOutputStream writer = new DataOutputStream(new FileOutputStream(file,true));
                long size = Long.parseLong(receiveString(socket));
                System.out.println(size);
                Arrays.fill(buffer, (byte) 0);
                long progress = file.length();
                System.out.println(progress);
                sendString(String.valueOf(progress), socket);
                long start = System.currentTimeMillis();
                while (file.length() < size) {
                    sendString("kiday", socket);

                    receiveScope(socket, size);

                    writeInFile(writer, (int) size, file);

                    sendString("ok", socket);
                    progress(file.length(), size);
                }
                long end = (System.currentTimeMillis() - start) ;
                String speed = (end/1000 != 0)?String.valueOf((size/1000000)/(end/1000)):
                        new String(String.valueOf(Character.toString('\u221E')).getBytes("UTF-8"),
                                "UTF-8");
                System.out.println("\nMb/s: " + speed);
                writer.close();
            } else {
                System.out.println("there is no such file");
            }
        }
    }

    public void writeInFile(DataOutputStream writer, int size, File file) throws IOException {
        for(int i = 0; i< packetsScope.getScope().size(); i++) {
            writer.write(packetsScope.getScope().get(i), 0,
                    (size - totalSize > bufferSize)?packetsScope.getScope().get(i).length : (int) (size - totalSize));
            totalSize = file.length();
            counter++;
        }
        packetsScope.clearScope();
    }

    public void receiveScope(DatagramSocket socket, long size) throws IOException {
        while(totalSize < size) {
            if (receive(socket) != null ) {
                if(buffer[0] == -1)break;
                packetsScope.setValue(buffer[0], Arrays.copyOfRange(buffer, 1, receive.getLength()));
                buffer = new byte[bufferSize];
            }
        }
        while(!checkScope(socket));
        sendString("all", socket);
    }

    public boolean checkScope(DatagramSocket socket) throws IOException {
        buffer = new byte[bufferSize];
        if(packetsScope.getFirstNullPosition() != -1){
            sendString("notOk", socket);
            sendString(String.valueOf(packetsScope.getFirstNullPosition()), socket);
            while(true){
                if(receive(socket) != null){
                    if(buffer[0] == -2){
                        System.out.println("-2");
                        return true;
                    }
                    if(buffer[0] == -1)break;
                    packetsScope.setValue(buffer[0], Arrays.copyOfRange(buffer, 1, receive.getLength()));
                    break;
                }
            }
        }else {

            return true;
        }
        return packetsScope.isFull();
    }

    public DatagramPacket receive(DatagramSocket socket) {
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        try {
            socket.setSoTimeout(200);
            socket.receive(reply);
        } catch (IOException e) {
            return null;
        }
        receive = reply;
        return reply;
    }

    public String receiveString(DatagramSocket socket) throws IOException {
        DatagramPacket reply = new DatagramPacket(bufferforString, bufferforString.length);
        try {
            socket.setSoTimeout(10000);
            socket.receive(reply);
        } catch (IOException e) {
            System.out.println("notOk");
            return "notOK";
        }
        return getMyString(reply);
    }

    public String waitString(DatagramSocket socket) throws IOException {
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

    public void progress(long length, long size) {
        progress = (length * 100 / size);
        if (progress % 1 == 0 && progress != oldProgress) {
            System.out.print('\r');
            System.out.print("Progress: " + progress + "%");
            oldProgress = progress;
        }
    }

}