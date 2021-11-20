package fr.insalyon.hermes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.hermes.AppState;
import fr.insalyon.hermes.model.*;
import fr.insalyon.hermes.serializer.RuntimeTypeAdapterFactory;
import org.jetbrains.annotations.NotNull;

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

    private static final TypeToken<Message> messageTypeToken = new TypeToken<>() {
    };
    private static final RuntimeTypeAdapterFactory<Message> typeFactory = RuntimeTypeAdapterFactory
            .of(Message.class, "type")
            .registerSubtype(GroupMessage.class)
            .registerSubtype(PrivateMessage.class)
            .registerSubtype(AuthenticationMessage.class)
            .registerSubtype(TextMessage.class)
            .registerSubtype(ConnectionMessage.class)
            .registerSubtype(AlertConnected.class)
            .registerSubtype(DisconnectionMessage.class)
            .registerSubtype(GetChats.class)
            .registerSubtype(CreateChat.class)
            .registerSubtype(AccessChat.class)
            .registerSubtype(GetUsers.class);
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
    private Map<String, Boolean> usersConnected;
    private boolean isConnected;
    private boolean isLoaded;
    private List<Notification> notifications;
    private List<String> currentUserAddable;
    private Date previousConnection;

    private final AppState appState;

    public AppState getAppState() {
        return appState;
    }

    /**
     * HermesClient constructor
     */
    public HermesClient(String username, AppState appState) {
        this.username = username;
        this.chats = new ArrayList<>();
        this.currentChat = null;
        this.usersConnected = new HashMap<>();
        this.isConnected = false;
        this.isLoaded = false;
        this.notifications = new ArrayList<>();
        this.currentUserAddable = new ArrayList<>();
        this.previousConnection = null;
        this.appState = appState;
    }

    public boolean isDesktopAppActive() {
        return appState != null;
    }

    /**
     * @param args 0 => server address 1=> server port 2=> client username
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println("launching hermesClient");
        if (args.length != 3) {
            System.out.println("Usage: java HermesClient <HermesServer host> <HermesServer port> <HermesClient username>");
            System.exit(1);
        }
        HermesClient hClient = new HermesClient(args[2], null);

        try {
            hClient.connect(args[0], Integer.parseInt(args[1]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                            if (alertConnected.getPreviousConnection() != null) {
                                this.previousConnection = alertConnected.getPreviousConnection();
                            }
                        } else {
                            if (currentChat != null && Objects.equals(alertConnected.getSender(), currentChat.getChatName())) {
                                usersConnected.put(alertConnected.getUserConnected(), true);
                                //TODO: list connected update
                            }
                        }
                        break;
                    case "AlertDisconnected":
                        AlertDisconnected alertDisconnected = (AlertDisconnected) receivedMessage;
                        if (Objects.equals(alertDisconnected.getSender(), currentChat.getChatName())) {
                            usersConnected.put(alertDisconnected.getUserDisconnected(), false);
                            //TODO: list connected update
                        }
                        break;
                    case "AddNotification":
                        AddNotification addNotification = (AddNotification) receivedMessage;
                        notifications.add(addNotification);
                        chats.add(addNotification.getChat());
                        //TODO update notification and list chat panel
                        break;
                    case "BanNotification":
                        BanNotification banNotification = (BanNotification) receivedMessage;
                        notifications.add(banNotification);
                        chats.removeIf(chat -> Objects.equals(chat.getName(), banNotification.getSender()));
                        //TODO update notification and list chat panel
                        if (Objects.equals(currentChat.getChatName(), banNotification.getSender())) {
                            accessChat(chats.get(0).getName());
                        }
                        break;
                    case "AddUserChat":
                        AddUserChat addUserChat = (AddUserChat) receivedMessage;
                        break;
                    case "GetNotifications":
                        GetNotifications getNotifications = (GetNotifications) receivedMessage;
                        this.notifications = getNotifications.getNotifications();
                        //todo update
                        break;
                    case "GetUsersAddable":
                        GetUsersAddable getUsersAddable = (GetUsersAddable) receivedMessage;
                        currentUserAddable = getUsersAddable.getUsers();
                        //TODO update
                        break;
                    case "GetChats":
                        GetChats getChats = (GetChats) receivedMessage;
                        chats = getChats.getChats();
                        System.out.println("Chats size = " + chats.size());
                        if (chats.size() != 0) {
                            accessChat(chats.get(0).getName());
                            if(isDesktopAppActive()) {
                                appState.getChats().addAll(chats);
                            }
                        } else {
                            isLoaded = true; //TODO : update page
                        }
                        break;
                    case "AccessChat":
                        AccessChat accessChat = (AccessChat) receivedMessage;
                        currentChat = accessChat;
                        getUsers(currentChat.getChatName());
                        if (isDesktopAppActive()) {
                            appState.getCurrentChat().setValue(accessChat);
                        }
                        break;
                    case "GetUsers":
                        GetUsers getUsers = (GetUsers) receivedMessage;
                        isLoaded = true;
                        usersConnected = getUsers.getUsersConnected();
                        if(isDesktopAppActive()) {
                            appState.getUsersConnected().setValue(getUsers.getUsersConnected());
                        }
                        //TODO : update page
                        break;
                    case "AlertMessage":
                        AlertMessage alertMessage = (AlertMessage) receivedMessage;
                        //TODO display l'alert
                        break;
                    case "CreateChat":
                        CreateChat createChat = (CreateChat) receivedMessage;
                        if (createChat.getState()) {
                            List<String> users = new ArrayList<>();
                            users.add(this.username);
                            LogChat logChat = new LogChat(createChat.getName(), users, new TextMessage("Chat create", createChat.getName(), createChat.getName(), new Date(System.currentTimeMillis())));
                            chats.add(logChat);
                            accessChat(logChat.getName());
                            if(isDesktopAppActive()) {
                                appState.getChats().add(logChat);
                            }
                        } else {
                            System.out.println("ERROR CHAT DUPLICATE");
                            //TODO: display alert
                        }
                        break;
                    case "DisconnectionMessage":
                        DisconnectionMessage disconnectionMessage = (DisconnectionMessage) receivedMessage;
                        this.isConnected = false;
                        //TODO deco and delete this user
                        break;
                    case "LeaveChat":
                        LeaveChat leaveChat = (LeaveChat) receivedMessage;
                        if (currentChat != null && Objects.equals(leaveChat.getName(), this.currentChat.getChatName())) {
                            usersConnected.remove(leaveChat.getSender());
                        }
                        break;
                    case "UpdateChat":
                        UpdateChat updateChat = (UpdateChat) receivedMessage;
                        boolean nameChanged = false;
                        if (!Objects.equals(updateChat.getChatName(), updateChat.getDestination())) {
                            nameChanged = true;
                        }
                        for (LogChat chat : chats) {
                            if (chat.getName().equals(updateChat.getDestination())) {
                                if (nameChanged) {
                                    chat.setName(updateChat.getChatName());
                                }
                            }
                        }
                        if (currentChat != null && Objects.equals(currentChat.getChatName(), updateChat.getDestination())) {
                            if (nameChanged) {
                                currentChat.setName(updateChat.getChatName());
                            }
                            currentChat.setAdmin(updateChat.getAdmin());
                        }
                        //TODO updateChat name if needed in currentChat and list chats
                        //TODO update access if admin have changed
                        break;
                    case "TextMessage":
                        TextMessage textMessage = (TextMessage) receivedMessage;
                        if (currentChat != null && Objects.equals(textMessage.getDestination(), currentChat.getChatName())) {
                            currentChat.add(textMessage);
                            if(isDesktopAppActive()) {
                                appState.getMessages().add(textMessage);
                            }
                            //TODO update
                        } else {
                            for (LogChat chat : chats) {
                                if (chat.getName().equals(textMessage.getSender())) {
                                    chat.setTextMessage(textMessage);
                                }
                            }
                            //TODO update order
                        }
                    default:
                        break;
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
                if (line != null) {
                    if (line.equals("exit")) {
                        sendDisconnection();
                    }
                    sendMessage(line, "channel 3");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
    public void sendMessage(String message, String chatDestination) {
        if (socket != null) {
            TextMessage fullMessage = new TextMessage(message, this.username, chatDestination, new Date(System.currentTimeMillis()));
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
            DisconnectionMessage msg = new DisconnectionMessage(this.username, new Date());
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
            e.printStackTrace();
        }
    }

    public void createChat(@NotNull String chatName) {
        if(socket != null) {
            CreateChat createChat = new CreateChat(this.username, "server", new Date(), chatName);
            outStream.println(gson.toJson(createChat, messageTypeToken.getType()));
        }
    }

}
