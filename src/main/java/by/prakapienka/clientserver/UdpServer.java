package by.prakapienka.clientserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class UdpServer {

    private static final String HANDSHAKE = "handshake";
    private static final String AVAILABLE_COMMANDS = "1. Say Hello. 2. Server Date. 3. My Address. 0. Exit.";
    private static final String EXIT_COMMAND = "Exit";

    private final int port;
    private DatagramSocket socket;
    private InetAddress address;

    public void run() {
        try {
            socket = new DatagramSocket(port);

            while (true) {
                byte[] buf = new byte[256];

                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String request = new String(packet.getData(), 0, packet.getLength());
                if (request.equals(HANDSHAKE)) {
                    System.out.println("Connection established.");
                    address = packet.getAddress();
                    int port = packet.getPort();
                    packet = new DatagramPacket(
                            AVAILABLE_COMMANDS.getBytes(),
                            AVAILABLE_COMMANDS.getBytes().length,
                            address, port);
                    socket.send(packet);
                    continue;
                }

                String response = processRequest(request);
                address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(
                        response.getBytes(),
                        response.getBytes().length,
                        address, port);
                socket.send(packet);

                if (EXIT_COMMAND.equals(response)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + port + " or listening for a connection");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private String processRequest(String request) {
        if (request.equalsIgnoreCase("say hello") || request.equals("1")) {
            return  "Hello!";
        } else  if (request.equalsIgnoreCase("server date") || request.equals("2")) {
            return getCurrentTime();
        } else if (request.equalsIgnoreCase("my address") || request.equals("3")) {
            return getRemoteSocketAddress();
        } else if (request.equalsIgnoreCase(EXIT_COMMAND) || request.equals("0")) {
            return EXIT_COMMAND;
        } else {
            return "Unknown command.";
        }
    }

    private String getRemoteSocketAddress() {
        return address.toString();
    }

    private String getCurrentTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.YYYY H:mm");
        return String.format("Current server time: %s", dateFormat.format(date));
    }

    public UdpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java UdpServer <port number>");
            return;
        }

        int portNumber = Integer.parseInt(args[0]);

        UdpServer server = new UdpServer(portNumber);
        server.run();

    }

}
