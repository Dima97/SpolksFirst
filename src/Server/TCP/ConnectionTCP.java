package Server.TCP;

import util.ClientInfo;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

public class ConnectionTCP {
    private static ConnectionTCP instance;
    private CommandsTCP commandsTCP = new CommandsTCP();

    public static final int PORT = 9876;
    public static int index = 0;
    private static ByteBuffer buffer = ByteBuffer.allocate(4000);
    private static File file;
    private static DataInputStream reader;
    static ArrayList<ClientInfo> list = new ArrayList<ClientInfo>();
    private ConnectionTCP() {
    }

    public static ConnectionTCP getConnection() {
        if (instance == null) {
            instance = new ConnectionTCP();
        }
        return instance;
    }

    public void connectWithClient() throws IOException {
        String encoding = System.getProperty("file.encoding");
        Charset cs = Charset.forName(encoding);
        SocketChannel ch = null;
        ServerSocketChannel ssc = ServerSocketChannel.open();
        Selector sel = SelectorProvider.provider().openSelector();
        try {
            ssc.configureBlocking(false);
            ssc.socket().bind(new InetSocketAddress(PORT));
            ssc.register(sel, ssc.validOps());
            System.out.println("Server on port: " + PORT);
            while (sel.select() > -1) {
                Iterator it = sel.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey skey = (SelectionKey) it.next();
                    it.remove();
                    connectNewClient(ch, sel, ssc, skey);
                    if (skey.isReadable() || skey.isWritable()) {
                        ch = (SocketChannel) skey.channel();
                        searchChannel(ch);
                        String str = readString(ch);
                        if(str.length() > 0) {
                            System.out.println("received data: " + str);
                        }
                        menu(str, ch, sel);
                        buffer.clear();
                    }
                }
            }
        } finally {
            if (ch != null) {
                ch.close();
            }
            ssc.close();
            sel.close();
        }
    }

    public void connectNewClient(SocketChannel ch, Selector sel, ServerSocketChannel ssc, SelectionKey skey) {
        if (skey.isAcceptable()) {
            try {
                ch = ssc.accept();
                System.out.println("Accepted connection from:"
                        + ch.socket());
                ch.configureBlocking(false);
                ch.register(sel, SelectionKey.OP_READ);
                ClientInfo cl = new ClientInfo(ch);
                list.add(cl);
            } catch (ClosedChannelException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void menu(String str, SocketChannel ch, Selector sel) throws IOException {
        close(str, ch);
        commandsTCP.time(str, ch);
        commandsTCP.echo(str, ch);
        commandsTCP.isDownload(str, ch, sel, index, file, list);
        if(list.get(index).flag){
            ch.register(sel, SelectionKey.OP_WRITE);
            commandsTCP.download(ch,sel, index, list, reader);
        }
    }

    private void close(String line, SocketChannel ch) {
        if (line.equals("CLOSE")) {
            System.out.println("close channel");
            try {
                ch.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void searchChannel(SocketChannel ch) {
        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).ch == ch) {
                index = i;
            }
        }
    }

    private String readString(SocketChannel ch) {
        String encoding = System.getProperty("file.encoding");
        Charset charset = Charset.forName(encoding);
        try {
            ch.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        CharBuffer cb = charset.decode((ByteBuffer) buffer.flip());
        return cb.toString();
    }
}
