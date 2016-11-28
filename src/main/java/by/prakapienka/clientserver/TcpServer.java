package by.prakapienka.clientserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TcpServer {

    private static final String HANDSHAKE = "handshake";
    private static final String EXIT_COMMAND = "Exit";
    private static final String AVAILABLE_COMMANDS = "1. Say Hello. 2. Server Date. 3. My Address. 0. Exit.";

    private final int port;
    private ServerSocket serverSocket;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public void run() {

        try {
            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String request, response;

            serverHandshake();
            out.println(AVAILABLE_COMMANDS);
            while (true) {
                request = in.readLine();
                response = processRequest(request);
                out.println(response);
                if (response.equalsIgnoreCase(EXIT_COMMAND))
                    break;
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + port + " or listening for a connection");
            System.out.println(e.getMessage());
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
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void serverHandshake() throws IOException {
        out.println(HANDSHAKE);
        String response = in.readLine();
        if (!response.equals(HANDSHAKE)) {
            throw new IOException("Failed to establish connection.");
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
        return socket.getRemoteSocketAddress().toString();
    }

    private String getCurrentTime() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("d.MM.YYYY H:mm");
        return String.format("Current server time: %s", dateFormat.format(date));
    }

    public TcpServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java TcpServer <port number>");
            return;
        }

        int portNumber = Integer.parseInt(args[0]);

        TcpServer server = new TcpServer(portNumber);
        server.run();

    }
}
