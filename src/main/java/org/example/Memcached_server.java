package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Memcached_server {
    private static final int DEFAULT_PORT = 11211;
    private final Map<String, Item> store = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("Hello welcome to Memcached_server");

        System.out.println("Instructions :: ");
        System.out.println("set <key> :  Stores a value associated with a key ");
        System.out.println("get <key> : Retrieves the value associated with a key.");
        System.out.println("add <key>: Stores a value only if the key does not already exist.");
        System.out.println("replace <key>: Stores a value only if the key already exists.");
        System.out.println("append <key>: Appends data to an existing key’s value.");
        System.out.println("prepend <key>: Prepends data to the beginning of an existing key’s value.");


        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter port number (default 11211): ");
        int port = DEFAULT_PORT;

        if (scanner.hasNextInt()) {
            port = scanner.nextInt();
        } else {
            System.out.println("Invalid port number. Using default port " + DEFAULT_PORT);
            scanner.next();
        }
        scanner.close();

        new Memcached_server().startServer(port);
    }

    private void startServer(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(new ClientHandler(clientSocket)).start();
                } catch (IOException e) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            System.exit(1);
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }

                    String[] command = line.split(" ", 5);
                    if (command.length < 2) {
                        out.println("ERROR");
                        continue;
                    }

                    switch (command[0].toLowerCase()) {
                        case "set":
                            handleSetCommand(command, in, out);
                            break;
                        case "get":
                            handleGetCommand(command, out);
                            break;
                        case "add":
                            handleAddCommand(command, in, out);
                            break;
                        case "replace":
                            handleReplaceCommand(command, in, out);
                            break;
                        case "append":
                            handleAppendCommand(command, in, out);
                            break;
                        case "prepend":
                            handlePrependCommand(command, in, out);
                            break;
                        default:
                            out.println("ERROR");
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client: " + e.getMessage());
            }
        }

        private void handleSetCommand(String[] command, BufferedReader in, PrintWriter out) throws IOException {
            if (command.length < 5) {
                out.println("ERROR");
                return;
            }

            String key = command[1];
            int flags = Integer.parseInt(command[2]);
            int exptime = Integer.parseInt(command[3]);
            int byteCount = Integer.parseInt(command[4].split(" ")[0]);


            boolean noreply = command[4].contains("noreply");

            StringBuilder data = new StringBuilder();
            for (int i = 0; i < byteCount; i++) {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                data.append((char) c);
            }
            in.readLine();

            Instant expirationTime = Instant.now();
            if (exptime > 0) {
                expirationTime = expirationTime.plusSeconds(exptime);
            } else if (exptime < 0) {
                expirationTime = Instant.now().minusSeconds(1);
            }

            store.put(key, new Item(key, flags, data.toString(), expirationTime));

            if (!noreply) {
                out.println("STORED");
            }
        }

        private void handleGetCommand(String[] command, PrintWriter out) {
            if (command.length < 2) {
                out.println("ERROR");
                return;
            }

            String key = command[1];
            Item item = store.get(key);

            if (item == null || item.isExpired()) {
                store.remove(key);
                out.println("END");
            } else {
                out.printf("VALUE %s %d %d\r\n%s\r\n", item.key, item.flags, item.data.length(), item.data);
                out.println("END");
            }
        }

        private void handleAddCommand(String[] command, BufferedReader in, PrintWriter out) throws IOException {
            if (command.length < 5) {
                out.println("ERROR");
                return;
            }

            String key = command[1];
            int flags = Integer.parseInt(command[2]);
            int exptime = Integer.parseInt(command[3]);
            int byteCount = Integer.parseInt(command[4].split(" ")[0]);


            boolean noreply = command[4].contains("noreply");

            if (store.containsKey(key)) {
                out.println("NOT_STORED");
                in.readLine();
                return;
            }

            StringBuilder data = new StringBuilder();
            for (int i = 0; i < byteCount; i++) {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                data.append((char) c);
            }
            in.readLine();
            Instant expirationTime = Instant.now();
            if (exptime > 0) {
                expirationTime = expirationTime.plusSeconds(exptime);
            } else if (exptime < 0) {

                expirationTime = Instant.now().minusSeconds(1);
            }

            store.put(key, new Item(key, flags, data.toString(), expirationTime));

            if (!noreply) {
                out.println("STORED");
            }
        }

        private void handleReplaceCommand(String[] command, BufferedReader in, PrintWriter out) throws IOException {
            if (command.length < 5) {
                out.println("ERROR");
                return;
            }

            String key = command[1];
            int flags = Integer.parseInt(command[2]);
            int exptime = Integer.parseInt(command[3]);
            int byteCount = Integer.parseInt(command[4].split(" ")[0]);


            boolean noreply = command[4].contains("noreply");

            if (!store.containsKey(key)) {
                out.println("NOT_STORED");
                in.readLine();
                return;
            }

            StringBuilder data = new StringBuilder();
            for (int i = 0; i < byteCount; i++) {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                data.append((char) c);
            }
            in.readLine();
            Instant expirationTime = Instant.now();
            if (exptime > 0) {
                expirationTime = expirationTime.plusSeconds(exptime);
            } else if (exptime < 0) {

                expirationTime = Instant.now().minusSeconds(1);
            }

            store.put(key, new Item(key, flags, data.toString(), expirationTime));

            if (!noreply) {
                out.println("STORED");
            }
        }

        private void handleAppendCommand(String[] command, BufferedReader in, PrintWriter out) throws IOException {
            if (command.length < 5) {
                out.println("ERROR");
                return;
            }

            String key = command[1];
            int flags = Integer.parseInt(command[2]);
            int exptime = Integer.parseInt(command[3]);
            int byteCount = Integer.parseInt(command[4].split(" ")[0]);
            boolean noreply = command[4].contains("noreply");

            Item item = store.get(key);
            if (item == null || item.isExpired()) {
                store.remove(key);
                out.println("NOT_STORED");
                in.readLine();
                return;
            }

            StringBuilder data = new StringBuilder();
            for (int i = 0; i < byteCount; i++) {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                data.append((char) c);
            }
            in.readLine();


            item.data += data.toString();
            item.expirationTime = Instant.now().plusSeconds(exptime);

            if (!noreply) {
                out.println("STORED");
            }
        }

        private void handlePrependCommand(String[] command, BufferedReader in, PrintWriter out) throws IOException {
            if (command.length < 5) {
                out.println("ERROR");
                return;
            }

            String key = command[1];
            int flags = Integer.parseInt(command[2]);
            int exptime = Integer.parseInt(command[3]);
            int byteCount = Integer.parseInt(command[4].split(" ")[0]);


            boolean noreply = command[4].contains("noreply");

            Item item = store.get(key);
            if (item == null || item.isExpired()) {
                store.remove(key);
                out.println("NOT_STORED");
                in.readLine();
                return;
            }

            StringBuilder data = new StringBuilder();
            for (int i = 0; i < byteCount; i++) {
                int c = in.read();
                if (c == -1) {
                    break;
                }
                data.append((char) c);
            }
            in.readLine();
            item.data = data.toString() + item.data;
            item.expirationTime = Instant.now().plusSeconds(exptime);

            if (!noreply) {
                out.println("STORED");
            }
        }
    }

    private static class Item {
        private final String key;
        private final int flags;
        private String data;
        private Instant expirationTime;

        Item(String key, int flags, String data, Instant expirationTime) {
            this.key = key;
            this.flags = flags;
            this.data = data;
            this.expirationTime = expirationTime;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expirationTime);
        }
    }
}
