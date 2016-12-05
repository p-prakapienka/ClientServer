package by.prakapienka.clientserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class SelectorServer {

    public static void main(String[] args) {

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open();
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5454);
            serverSocket.bind(hostAddress);
            serverSocket.configureBlocking(false);
            int ops = serverSocket.validOps();
            SelectionKey selectionKey = serverSocket.register(selector, ops, null);

            out:
            while (true) {
                int noOfKeys = selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();

                for (SelectionKey key : selectedKeys) {
                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocket.accept();
                        if (client != null) {
                            client.configureBlocking(false);
                            client.register(selector, SelectionKey.OP_READ);
                            System.out.println("Accepted new connection from client: " + client);

                        }

                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel)key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        client.read(buffer);
                        String request = new String(buffer.array()).trim();

                        if ("0".equals(request)) {
                            client.close();
                            System.out.println("Client disconnected.");
                            break out;
                        } else {
                            System.out.println(request);
                            buffer = ByteBuffer.wrap("Hello".getBytes());
                            client.write(buffer);
                            buffer.clear();
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


}
