package by.prakapienka.clientserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpClient {

    private static final String HANDSHAKE = "handshake";
    private static final String EXIT_COMMAND = "Exit";

    private final String host;
    private final int port;
    private DatagramSocket socket;
    private InetAddress address;

    public void run() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName(host);
            byte[] buf = new byte[256];

            DatagramPacket packet = new DatagramPacket(
                    HANDSHAKE.getBytes(),
                    HANDSHAKE.getBytes().length,
                    address, port);
            socket.send(packet);

            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            System.out.println(new String(packet.getData()));

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));
            String serverResponse;
            String message;

            while (true) {
                message = reader.readLine();
                System.out.println("Client: " + message);
                packet = new DatagramPacket(
                        message.getBytes(),
                        message.getBytes().length,
                        address, port);
                socket.send(packet);

                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                serverResponse = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Server: " + serverResponse);

                if (EXIT_COMMAND.equals(serverResponse)) {
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public UdpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java UdpClient <hostname> <port number>");
            return;
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        UdpClient udpClient = new UdpClient(hostName, portNumber);
        udpClient.run();

    }

}
