package by.prakapienka.clientserver.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TestTcpClient extends Thread {

    private static final String HANDSHAKE = "handshake";
    private static final String EXIT_COMMAND = "Exit";

    private final String host;
    private final int port;
    private final String name;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private String message;

    public void run() {
        try {
            socket = new Socket(host, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String serverResponse;

            handshake();
            System.out.println(name + ": Connection established.");

            while (true) {
                serverResponse = in.readLine();
                System.out.println("Server for " + name + ": " + serverResponse);
                if (serverResponse.equals(EXIT_COMMAND))
                    break;

                while (message == null) {}
                synchronized (this) {
                    if (message != null) {
                        System.out.println(name + ": " + message);
                        out.println(message);
                        message = null;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
                if (out != null) {
                    out.close();
                    out = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public synchronized void sendMessage(String message) {
        this.message = message;
    }

    private void handshake() throws IOException {
        String handshake = in.readLine();
        if (!handshake.equals(HANDSHAKE)) {
            throw new IOException(name + ": Failed to establish connection.");
        }
        out.println(HANDSHAKE);
    }

    public TestTcpClient(String host, int port, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
    }
}
