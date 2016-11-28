package by.prakapienka.clientserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TcpClient {

    private static final String HANDSHAKE = "handshake";
    private static final String EXIT_COMMAND = "Exit";

    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void run() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));
            String serverResponse;
            String message;

            handshake();
            System.out.println("Connection established.");

            while (true) {
                serverResponse = in.readLine();
                System.out.println("Server: " + serverResponse);
                if (serverResponse.equals(EXIT_COMMAND))
                    break;

                message = reader.readLine();
                if (message != null) {
                    System.out.println("Client: " + message);
                    out.println(message);
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void handshake() throws IOException {
        String handshake = in.readLine();
        if (!handshake.equals(HANDSHAKE)) {
            throw new IOException("Failed to establish connection.");
        }
        out.println(HANDSHAKE);
    }

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
            System.err.println(
                    "Usage: java TcpClient <host name> <port number>");
            return;
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        TcpClient tcpClient = new TcpClient(hostName, portNumber);
        tcpClient.run();

    }
}
