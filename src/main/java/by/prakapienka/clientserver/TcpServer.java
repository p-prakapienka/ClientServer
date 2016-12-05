package by.prakapienka.clientserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(TcpServer.class);

    private static final String HANDSHAKE = "handshake";
    private static final String EXIT_COMMAND = "Exit";
    private static final String AVAILABLE_COMMANDS = "1. Say Hello. 2. Server Date. 3. My Address. 0. Exit.";

    private final int port;
    private final ExecutorService executor;
    private ServerSocket serverSocket;
    private static boolean isRunning;

    private class Handler implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String request, response;

                serverHandshake();
                out.println(AVAILABLE_COMMANDS);
                while (isRunning) {
                    request = in.readLine();
                    LOG.info("Processing request for {}.", socket.getRemoteSocketAddress());
                    response = processRequest(request);
                    out.println(response);
                    if (response.equalsIgnoreCase(EXIT_COMMAND)) {
                        LOG.info("Client disconnected {}.", socket.getRemoteSocketAddress());
                        break;
                    }
                }
            } catch (IOException e) {
                LOG.error("Exception caught when trying to listen to remote connection "
                        + socket.getRemoteSocketAddress() + ".");
                System.out.println(e.getMessage());
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

        private void serverHandshake() throws IOException {
            out.println(HANDSHAKE);
            String response = in.readLine();
            if (!response.equals(HANDSHAKE)) {
                throw new IOException("Failed to establish connection.");
            }
            LOG.info("Successfully connected to {}.", socket.getRemoteSocketAddress());
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

        public Handler(Socket socket) {
            this.socket = socket;
        }
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            new ServerConsole().start();
            isRunning = true;
            LOG.info("Server started. Listening on port: {}", this.port);

            while (true) {
                Socket socket = serverSocket.accept();
                if (socket != null) {
                    executor.execute(new Handler(socket));
                    LOG.info("Establishing connection with {}.",
                            socket.getRemoteSocketAddress());
                    continue;
                }
            }
        } catch (IOException e) {
            LOG.error("Exception caught when trying to listen on port "
                    + port + " or listening for a connection.");
            System.out.println(e.getMessage());
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                    serverSocket = null;
                }
                executor.shutdown();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public TcpServer(int port) {
        this.port = port;
        executor = Executors.newFixedThreadPool(10);
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

    private static class ServerConsole extends Thread {

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
                LOG.info("Server console is running.");
                while (isRunning) {
                    String command = in.readLine();
                    if (command.equalsIgnoreCase(EXIT_COMMAND)) {
                        isRunning = false;
                    }
                }
            } catch (IOException e) {
                LOG.error("Failed to start server console.");
                System.err.print(e.getMessage());
            }

        }
    }
}
