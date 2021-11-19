package fr.insalyon.hermes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.hermes.model.*;
import fr.insalyon.hermes.serializer.RuntimeTypeAdapterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client permettant l'interaction avec le serveur
 */
public class HermesClient {

    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() { };
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class)
            .registerSubtype(TextMessage.class)
            .registerSubtype(ConnectionMessage.class)
            .registerSubtype(AlertConnected.class)
            .registerSubtype(DisconnectionMessage.class)
            .registerSubtype(GetChats.class);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(typeFactory)
            .create();

    private String username;
    /**
     * Socket used to communicate
     * between client and server
     */
    private Socket socket;
    /**
     * Stream to get infos server => client
     */
    private BufferedReader inStream;
    /**
     * Stream to send infos client => server
     */
    private PrintStream outStream;
    /**
     * Thread allowing continuous listening to server
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);


    private List<LogChat> chats;
    private AccessChat currentChat;
    private Map<String, Boolean> userConnected;
    private boolean isConnected;
    private boolean isLoaded;

    /**
     * HermesClient constructor
     */
    public HermesClient(String username) {
        this.username = username;
        this.chats = new ArrayList<>();
        this.currentChat = null;
        this.userConnected = new HashMap<>();
        this.isConnected = false;
        this.isLoaded = false;
    }

//    /**
//     * @param args 0 => server address 1=> server port 2=> client username
//     * @throws IOException
//     */
//    public static void main(String[] args) throws IOException {
//        System.out.println("launching hermesClient");
//        if (args.length != 3) {
//            System.out.println("Usage: java HermesClient <HermesServer host> <HermesServer port> <HermesClient username>");
//            System.exit(1);
//        }
//        HermesClient hClient = new HermesClient(args[2]);
//        try {
//            hClient.connect(args[0], Integer.parseInt(args[1]));
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

    /**
     * Permet de connecter le client au serveur Hermes.
     *
     * @param serverHost IP du serveur
     * @param serverPort Port du serveur
     * @throws IOException
     */
    public void connect(String serverHost, int serverPort) throws IOException {
        socket = new Socket(serverHost, serverPort);
        inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outStream = new PrintStream(socket.getOutputStream());
        executorService.submit(() -> listenerThread(this, inStream));
        executorService.submit(() -> senderThread(this, outStream));
        sendConnection();
    }

    public void listenerThread(HermesClient hClient, BufferedReader inStream) {
        try {
            String message;
            while ((message = inStream.readLine()) != null) {
                Message receivedMessage = gson.fromJson(message, messageTypeToken.getType());

                System.out.println("Message = " + message);
                System.out.println("Deserialized = " + receivedMessage + " name = " + receivedMessage.getClass().getSimpleName());

                switch (receivedMessage.getClass().getSimpleName()) {
                    case "AlertConnected":
                        AlertConnected alertConnected = (AlertConnected) receivedMessage;
                        if (Objects.equals(alertConnected.getUserConnected(), username) && Objects.equals(alertConnected.getSender(), "server")) {
                            isConnected = true;
                            getChats();
                        } else {
                            if (Objects.equals(alertConnected.getSender(), currentChat.getChatName())) {
                                userConnected.put(alertConnected.getUserConnected(), true);
                                //TODO: update
                            }
                        }
                        break;
                    case "GetChats":
                        GetChats getChats = (GetChats) receivedMessage;
                        chats = getChats.getChats();
                        if (chats.size() != 0) {
                            accessChat(chats.get(0).getName());
                        } else {
                            isLoaded = true; //TODO : update page
                        }
                        break;
                    case "AccessChat":
                        AccessChat accessChat = (AccessChat) receivedMessage;
                        currentChat = accessChat;
                        getUsers(currentChat.getChatName());
                        break;
                    case "GetUsers":
                        GetUsers getUsers = (GetUsers) receivedMessage;
                        isLoaded = true;
                        userConnected = getUsers.getUsersConnected();
                        //TODO : update page
                        break;
                    default:
                        break;
                }


            }
        } catch (IOException e) {
            System.out.println(e);
        } catch (Exception e) {
            System.out.println(e);
        }
        //TODO : kill of Connection reset
    }

    private void getUsers(String chatName) {
        if (socket != null) {
            GetUsers getUsers = new GetUsers(this.username, chatName, new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(getUsers, messageTypeToken.getType()));
        }
    }

    private void accessChat(String name) {
        if (socket != null) {
            isLoaded = false;
            AccessChat accessChat = new AccessChat(this.username, "server", new Date(System.currentTimeMillis()), name);
            outStream.println(gson.toJson(accessChat, messageTypeToken.getType()));
        }
    }

    private void getChats() {
        if (socket != null) {
            GetChats getChats = new GetChats(this.username, "server", new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(getChats, messageTypeToken.getType()));
        }
    }

    public void senderThread(HermesClient hClient, PrintStream outStream) {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while (true) {
                line = stdIn.readLine();
                if (line.equals("exit")) {
                    sendDisconnection();
                }
                sendMessage(line);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Event triggered on message received
     * Used in the listening thread
     *
     * @param message
     */
    public void messageReceived(String message) {
        System.out.println("showing message" + message);
    }

    /**
     * Allows the client to send messages to the server
     * Wraps the content of the message with other
     * informations
     *
     * @param message
     */
    public void sendMessage(String message) {
        if (socket != null) {
            TextMessage fullMessage = new TextMessage(message, this.username, "server", new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(fullMessage, messageTypeToken.getType()));
        }
    }

    public void sendConnection() {
        if (socket != null) {
            ConnectionMessage msg = new ConnectionMessage(this.username, "", new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(msg, messageTypeToken.getType()));
        }
    }

    public void sendDisconnection() {
        if (socket != null) {
            DisconnectionMessage msg = new DisconnectionMessage(this.username, new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(msg, messageTypeToken.getType()));
        }
    }

    /**
     * Permet de fermet les flux et de terminer la
     * connexion avec le serveur
     *
     * @throws IOException
     */
    public void closeClient() throws IOException {
        try {
            socket.close();
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
}
