package util;

import java.io.DataInputStream;
import java.io.File;
import java.nio.channels.SocketChannel;

public class ClientInfo {
    public boolean flag = false;
    public File file = null;
    public DataInputStream reader = null;
    public SocketChannel ch = null;

    public ClientInfo(SocketChannel channel) {
        this.ch = channel;
    }
}
