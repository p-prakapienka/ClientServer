package by.prakapienka.clientserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SelectorClient {

    public static void main(String[] args) {
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 5454);

        try (SocketChannel client = SocketChannel.open(hostAddress);
             BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                String message = reader.readLine();
                ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                client.write(buffer);
                buffer.clear();

                if ("0".equals(message)) break;

                buffer = ByteBuffer.allocate(256);
                client.read(buffer);
                System.out.println("Server: " + new String(buffer.array()).trim());
            }

            Thread.sleep(3000);
        } catch (IOException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }
}
