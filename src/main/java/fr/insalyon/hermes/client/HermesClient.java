package fr.insalyon.hermes.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fr.insalyon.hermes.AppState;
import fr.insalyon.hermes.model.*;
import fr.insalyon.hermes.serializer.RuntimeTypeAdapterFactory;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static kotlin.io.ConsoleKt.readLine;

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
            .registerSubtype(GetNotifications.class)
            .registerSubtype(GetUsersAddable.class)
            .registerSubtype(AddUserChat.class)
            .registerSubtype(AddNotification.class)
            .registerSubtype(BanNotification.class)
            .registerSubtype(UpdateChat.class)
            .registerSubtype(BanUserChat.class)
            .registerSubtype(LeaveChat.class)
            .registerSubtype(AlertDisconnected.class)
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
        //if (args.length != 3) {
        //    System.out.println("Usage: java HermesClient <HermesServer host> <HermesServer port> <HermesClient username>");
        //    System.exit(1);
        //}
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Username :");
        String userName = stdIn.readLine();
        while(userName.contains(" ") || userName.equals("all")){
            System.err.println("Wrong format, the username is only one word and can't be equal tu all");
            System.out.println("UserName :");
            userName = stdIn.readLine();
        }
        System.out.println("Host IP :");
        String host = stdIn.readLine();
        System.out.println("Server Port :");
        String port = stdIn.readLine();

        HermesClient hClient = new HermesClient(userName, null);

        try {
            hClient.connect(host, Integer.parseInt(port));
            hClient.getNotifications();
        }  catch (IOException ex) {
            System.err.println("Error : could not connect to server " + host + " on port " + Integer.valueOf(port));
        } catch (NumberFormatException ex) {
            System.err.println("Error : you must set a correct port number");
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
        System.out.println("Connecting...");
        socket = new Socket(serverHost, serverPort);
        inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outStream = new PrintStream(socket.getOutputStream());
        executorService.submit(() -> listenerThread(this, inStream));
        executorService.submit(() -> senderThread(this, outStream));
        sendConnection();
        isConnected = true;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void listenerThread(HermesClient hClient, BufferedReader inStream) {
        try {
            String message;
            while ((message = inStream.readLine()) != null) {
                Message receivedMessage = gson.fromJson(message, messageTypeToken.getType());

                //System.out.println("Message = " + message);
                //System.out.println("Deserialized = " + receivedMessage + " name = " + receivedMessage.getClass().getSimpleName());

                switch (receivedMessage.getClass().getSimpleName()) {
                    case "AlertConnected":
                        AlertConnected alertConnected = (AlertConnected) receivedMessage;
                        if (Objects.equals(alertConnected.getUserConnected(), username) && Objects.equals(alertConnected.getSender(), "server")) {
                            isConnected = true;
                            getChats();
                            if (alertConnected.getPreviousConnection() != null) {
                                this.previousConnection = alertConnected.getPreviousConnection();
                            }
                            if(!isDesktopAppActive()){
                                displayAlert("Connection success");
                            }
                        } else {
                            if (currentChat != null && Objects.equals(alertConnected.getSender(), currentChat.getChatName())) {
                                usersConnected.put(alertConnected.getUserConnected(), true);
                                if(isDesktopAppActive()) {
                                    Map<String, Boolean> newUsersConnected = new HashMap<>(usersConnected);
                                    appState.getUsersConnected().setValue(newUsersConnected);
                                }
                                //TODO: list connected update
                                if(!isDesktopAppActive()){
                                    displayAlert(alertConnected.getUserConnected()+" connected in the chat");
                                }
                            }
                        }
                        break;
                    case "AlertDisconnected":
                        AlertDisconnected alertDisconnected = (AlertDisconnected) receivedMessage;
                        if (Objects.equals(alertDisconnected.getSender(), currentChat.getChatName())) {
                            usersConnected.put(alertDisconnected.getUserDisconnected(), false);
                            if(isDesktopAppActive()) {
                                Map<String, Boolean> newUsersConnected = new HashMap<>(usersConnected);
                                appState.getUsersConnected().setValue(newUsersConnected);
                            }

                            //TODO: list connected update
                            if(!isDesktopAppActive()){
                                displayAlert(alertDisconnected.getUserDisconnected()+" disconnecting from the chat");
                            }
                        }
                        break;
                    case "AddNotification":
                        AddNotification addNotification = (AddNotification) receivedMessage;
                        notifications.add(addNotification);
                        chats.add(addNotification.getChat());
                        //TODO update notification and list chat panel
                        if(!isDesktopAppActive()){
                            displayAlert(addNotification.getContent());
                        } else {
                            //Add the new chat in app state (because the user has been added to a new chat)
                            appState.getChats().add(addNotification.getChat());
                        }
                        break;
                    case "BanNotification":
                        BanNotification banNotification = (BanNotification) receivedMessage;
                        notifications.add(banNotification);
                        chats.removeIf(chat -> Objects.equals(chat.getName(), banNotification.getSender()));
                        //TODO update notification and list chat panel
                        if (Objects.equals(currentChat.getChatName(), banNotification.getSender())) {
                            if(chats.size()>0){
                                accessChat(chats.get(0).getName());
                            } else {
                                if(!isDesktopAppActive()){
                                    System.out.println("Chats list is empty");
                                }
                            }
                        }
                        if(isDesktopAppActive()) {
                            appState.getNotification().setValue(new Pair<>(banNotification.getContent(), true));
                            //Update the available chats
                            appState.getChats().removeIf(chat -> Objects.equals(chat.getName(), banNotification.getSender()));
                        }
                        if(!isDesktopAppActive()){
                            displayAlert(banNotification.getContent());
                        }
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
                        if(!isDesktopAppActive()){
                            displayAddable();
                        } else {
                            appState.getUsersAddable().clear();
                            appState.getUsersAddable().addAll(currentUserAddable);
                        }
                        break;
                    case "GetChats":
                        GetChats getChats = (GetChats) receivedMessage;
                        chats = getChats.getChats();
                        if (chats.size() != 0) {
                            if(isDesktopAppActive()) {
                                appState.getChats().clear();
                                appState.getChats().addAll(chats);
                            } else {
                                displayChats();
                            }
                            accessChat(chats.get(0).getName());
                        } else {
                            isLoaded = true; //TODO : update page
                            if(isDesktopAppActive()) {
                                appState.getChats().clear();
                                appState.getCurrentChat().setValue(null);
                                appState.getMessages().clear();
                            }
                            if(!isDesktopAppActive()){
                                System.out.println("Chats list is empty");
                            }
                        }
                        break;
                    case "AccessChat":
                        currentChat = (AccessChat) receivedMessage;
                        getUsers(currentChat.getChatName());
                        if (isDesktopAppActive()) {
                            appState.getCurrentChat().setValue(currentChat);
                            appState.getMessages().clear();
                            appState.getMessages().addAll(currentChat.getMessages());
                        } else {
                            System.out.println("Current position : "+currentChat.getChatName());
                            displayMessages();
                        }
                        break;
                    case "GetUsers":
                        GetUsers getUsers = (GetUsers) receivedMessage;
                        isLoaded = true;
                        usersConnected = getUsers.getUsersConnected();
                        if(isDesktopAppActive()) {
                            appState.getUsersConnected().setValue(getUsers.getUsersConnected());
                        } else {
                            displayConnected();
                        }
                        //TODO : update page
                        break;
                    case "AlertMessage":
                        AlertMessage alertMessage = (AlertMessage) receivedMessage;
                        if(!isDesktopAppActive()) {
                            displayAlert(alertMessage.getContent());
                        } else {
                            appState.getNotification().setValue(new Pair<>(alertMessage.getContent(), true));
                        }
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
                            } else {
                                System.out.println("Chat created");
                            }
                        } else {
                            if(isDesktopAppActive()) {

                            } else {
                                System.out.println("Chat already used, change the name !");
                            }
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
                        if(!isDesktopAppActive()){
                            System.out.println("You have left the chat :" + leaveChat.getName());
                        }
                        //Update the chats
                        getChats();
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

                        if(!isDesktopAppActive()) {
                            System.out.println("Chat updated :");
                            System.out.println(updateChat.getDestination() +" rename to "+updateChat.getChatName());
                            System.out.println("Admin : "+updateChat.getAdmin());
                        }
                        //TODO updateChat name if needed in currentChat and list chats
                        //TODO update access if admin have changed
                        break;
                    case "TextMessage":
                        TextMessage textMessage = (TextMessage) receivedMessage;

                        if(!isDesktopAppActive()){
                            System.out.println("New message in chat :"+ textMessage.getDestination());
                        }

                        for (LogChat chat : chats) {
                            if (chat.getName().equals(textMessage.getDestination())) {
                                chat.setTextMessage(textMessage);

                                if(Objects.equals(textMessage.getDestination(), textMessage.getSender())){
                                    String [] content = textMessage.getContent().split(" ");
                                    if ("added".equals(content[1])) {
                                        chat.addUser(content[0]);
                                    } else {
                                        chat.removeUser(content[0]);
//                                        appState.getUsersConnected().getValue().remove(content[0]);
                                    }
                                }

                            }
                        }
                        if (currentChat != null && Objects.equals(textMessage.getDestination(), currentChat.getChatName())) {
                            currentChat.add(textMessage);
                            if(Objects.equals(textMessage.getDestination(), textMessage.getSender())){
                                String [] content = textMessage.getContent().split(" ");
                                //Text message that a user has been added
                                if ("added".equals(content[1])) {
                                    currentChat.setUsers(currentChat.getUsers() + 1);
                                    getUsers(currentChat.getChatName());
                                } else { //Text message that a user has been removed
                                    if(isDesktopAppActive()) {
                                        HashMap<String, Boolean> newUsers = new HashMap<>(appState.getUsersConnected().getValue());
                                        newUsers.remove(content[0]);
                                        appState.getUsersConnected().setValue(newUsers);
                                    }
                                    usersConnected.remove(content[0]);
                                    currentChat.setUsers(currentChat.getUsers() - 1);
                                }
                            }

                            if(!isDesktopAppActive()){
                                displayMessage(textMessage);
                            } else {
                                appState.getMessages().add(textMessage);
                            }

                        }
                            //TODO update order
                        break;
                    default:
                        break;

                }



            }
        } catch (Exception e) {
            if(Objects.equals(e.getMessage(), "Socket closed")){
                if(!isDesktopAppActive()){
                    System.out.println("Disconnected");
                    System.exit(0);
                }
            } else if(Objects.equals(e.getMessage(), "Connection reset")){
                closeClient();
                if(!isDesktopAppActive()){
                    System.out.println("Connection with the server lost");
                    System.exit(0);
                }
            } else {
                e.printStackTrace();
            }
        }

    }




    private void getUsers(String chatName) {
        if (socket != null) {
            GetUsers getUsers = new GetUsers(this.username, chatName, new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(getUsers, messageTypeToken.getType()));
        }
    }

    public void accessChat(String name) {
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

    private void getNotifications() {
        if(socket != null) {
            GetNotifications getNotifications = new GetNotifications(this.username, "server", new Date(System.currentTimeMillis()));
            outStream.println(gson.toJson(getNotifications, messageTypeToken.getType()));
        }
    }

    public void senderThread(HermesClient hClient, PrintStream outStream) {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String line;
        try {
            while (true) {
                line = stdIn.readLine();
                if (line != null) {
                    if(line.charAt(0) == '?'){
                        line = line.substring(1);
                        switch(line){
                            case "chats":
                                displayChats();
                                break;
                            case "connected":
                                displayConnected();
                                break;
                            case "notifications":
                                displayNotifications();
                                break;
                            case "messages":
                                displayMessages();
                                break;
                            case "position":
                                if(currentChat==null){
                                    System.out.println("Main Menu");
                                } else {
                                    System.out.println(currentChat.getChatName());
                                }
                                break;
                            case "addable":
                                if(currentChat==null){
                                    System.out.println("Error, no active chat");
                                } else {
                                    getAddable();
                                }
                                break;
                            default :
                                System.out.println("Command Unknown");
                                break;
                        }
                    } else if (line.charAt(0)== '-'){
                        line = line.substring(1);
                        String[] args = line.split(" ");
                        if(args.length<1){
                            System.out.println("Command Unknown");
                        } else {
                            switch(args[0]){
                                case "create":
                                    if(args.length<2){
                                        System.out.println("Error, Usage : -create <Chat Name>");
                                    } else {
                                        args[0]="";
                                        String name = String.join(" ", args);
                                        createChat(name.substring(1));
                                    }
                                    break;
                                case "leave":
                                    if(currentChat != null){
                                        if(Objects.equals(currentChat.getAdmin(), username)){
                                            System.out.println("Error, you can't leave a chat where you are the admin");
                                        } else if(Objects.equals(currentChat.getAdmin(), "all") && usersConnected.size() == 1){
                                            System.out.println("Error, you can't leave a chat where you are the admin");
                                        } else {
                                            leaveChat(currentChat.getChatName());
                                        }
                                    } else {
                                        System.out.println("Error, no active chat");
                                    }
                                    break;
                                case "update":
                                    if(currentChat!=null){
                                        if(Objects.equals(currentChat.getAdmin(), "all") || Objects.equals(currentChat.getAdmin(), username)) {
                                            System.out.println("New name :");
                                            String chatName = stdIn.readLine();
                                            System.out.println("List of admin (all | userName):");
                                            String admin = stdIn.readLine();
                                            if (Objects.equals(admin, "all") || usersConnected.containsKey(admin)) {
                                                updateChat(currentChat.getChatName(), chatName, admin);
                                            } else {
                                                System.out.println("Error, this user isn't in the chat");
                                            }
                                        } else {
                                            System.out.println("Error, you are not allowed to do that");
                                        }
                                    } else {
                                        System.out.println("Error, no active chat");
                                    }
                                    break;
                                case "add":
                                    if(currentChat != null && currentUserAddable !=null){
                                        if(Objects.equals(currentChat.getAdmin(), "all") || Objects.equals(currentChat.getAdmin(), username)){
                                            if(args.length!=2){
                                                System.out.println("Error, Usage : -add <name1;name2;name3>");
                                            }
                                            List<String> adds= new ArrayList<String>();
                                            boolean valid = true;
                                            for(String user : args[1].split(";")){
                                                if(currentUserAddable.contains(user)){
                                                    adds.add(user);
                                                } else {
                                                    valid = false;
                                                }
                                            }
                                            if(valid){
                                                addUsers(adds);
                                            } else {
                                                System.out.println("Error, parse error");
                                            }
                                        } else {
                                            System.out.println("Error, you are not allowed to do that");
                                        }

                                    } else {
                                        System.out.println("Error, no active chat");
                                    }
                                    break;
                                case "ban":
                                    if(currentChat != null){
                                        if(Objects.equals(currentChat.getAdmin(), "all") || Objects.equals(currentChat.getAdmin(), username)){
                                            if(args.length!=2){
                                                System.out.println("Error, Usage : -ban <name>");
                                            }
                                            if(Objects.equals(args[1], username)){
                                                System.out.println("Error, you can't ban yourself");
                                            } else {
                                                if(usersConnected.containsKey(args[1])){
                                                    banUser(args[1]);
                                                } else {
                                                    System.out.println("Error, user doesn't exist");
                                                }
                                            }
                                        } else {
                                            System.out.println("Error, you are not allowed to do that");
                                        }

                                    } else {
                                        System.out.println("Error, no active chat");
                                    }
                                    break;
                                case "access":
                                    args[0]="";
                                    String name = String.join(" ", args);
                                    name = name.substring(1);
                                    boolean valid = false;
                                    for(LogChat chat :  chats){
                                        if(chat.getName().equals(name)){
                                            valid = true;
                                        }
                                    }
                                    if(valid){
                                        accessChat(name);
                                    } else {
                                        System.out.println("Error, chat doesn't exist");
                                    }
                                    break;
                                case "exit":
                                    sendDisconnection();
                                    closeClient();
                                    break;
                                default:
                                    System.out.println("Command Unknown");
                                    break;
                            }
                        }

                    } else {
                        if(currentChat != null){
                            sendMessage(line, currentChat.getChatName());
                        } else {
                            sendMessage(line, "server");
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayMessage(TextMessage message) {
        System.out.println("Message received :");
        if(Objects.equals(message.getSender(), username)){
            System.out.println("From : me");
        } else {
            System.out.println("From : "+ message.getSender());
        }
        System.out.println(message.getContent());
        System.out.println(message.getTime());
        System.out.println(" ");
    }


    private void displayMessages() {
        System.out.println("*****************");
        if(currentChat == null){
            System.out.println("no current chat");
        } else {
            for(TextMessage textMessage : currentChat.getMessages()) {
                if(Objects.equals(textMessage.getSender(), username)){
                    System.out.println("From : me");
                } else {
                    System.out.println("From : "+ textMessage.getSender());
                }
                System.out.println(textMessage.getContent());
                System.out.println(textMessage.getTime());
                System.out.println(" ");
            }
        }
        System.out.println("*****************");
    }

    private void displayNotifications() {
        System.out.println("*****************");
        for(Notification notification : notifications) {
            System.out.println("Notifications :" );
            System.out.println(notification.getContent());
            System.out.println(notification.getTime());
        }
        if(notifications.size()==0){
            System.out.println("Notifications list empty");
        }
        System.out.println("*****************");
    }

    private void displayConnected() {
        System.out.println("*****************");
        if(currentChat == null){
            System.out.println("no chat");
        } else {
            System.out.println("Admin : "+ currentChat.getAdmin());
            System.out.println("Members : ");
            for (Map.Entry<String, Boolean> mapentry : usersConnected.entrySet()) {
                System.out.println(mapentry.getKey() +": "+ (mapentry.getValue()?"connected":"disconnected"));
            }
        }
        System.out.println("*****************");
    }

    private void displayChats() {
        System.out.println("*****************");
        for (LogChat chat : chats) {

            if(chat.getUsersNumber()==2){
                System.out.println("Conversation avec " +chat.getOtherName(username));
            } else {
                System.out.println("Chat : " +chat.getName());
            }
            System.out.println(chat.getMessage().getSender() +": "+chat.getMessage().getContent());
            System.out.println(chat.getMessage().getTime());
        }
        if(chats.size()==0){
            System.out.println("Chats list empty");
        }
        System.out.println("*****************");
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
     */
    public void closeClient() {
        try {
            socket.close();
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

    public void leaveChat(String chatName) {
        if(socket != null) {
            LeaveChat leaveChat = new LeaveChat(this.username, chatName, new Date(), chatName);
            outStream.println(gson.toJson(leaveChat, messageTypeToken.getType()));
        }
    }

    private void updateChat(String chatName, String name, String admin) {
        if(socket != null) {
            UpdateChat updateChat = new UpdateChat(this.username, chatName, new Date(), name, admin);
            outStream.println(gson.toJson(updateChat, messageTypeToken.getType()));
        }
    }

    public void getAddable() {
        if(socket != null) {
            GetUsersAddable getUsersAddable = new GetUsersAddable(this.username, currentChat.getChatName(), new Date());
            outStream.println(gson.toJson(getUsersAddable, messageTypeToken.getType()));
        }
    }

    public void addUsers(List<String> users) {
        if(socket != null) {
            AddUserChat addUserChat = new AddUserChat(this.username,currentChat.getChatName(),new Date(),currentChat.getChatName(),users);
            outStream.println(gson.toJson(addUserChat, messageTypeToken.getType()));
        }
    }

    public void banUser(String userName) {
        if(socket != null) {
            BanUserChat banUserChat = new BanUserChat(this.username,currentChat.getChatName(),new Date(),currentChat.getChatName(),userName);
            outStream.println(gson.toJson(banUserChat, messageTypeToken.getType()));
        }
    }

    private void getUsersAddable() {
        if(socket != null) {
            GetUsersAddable getUsersAddable = new GetUsersAddable(this.username,currentChat.getChatName(),new Date());
            outStream.println(gson.toJson(getUsersAddable, messageTypeToken.getType()));
        }
    }

    private void displayAlert(String msg) {
        System.out.println("********Alert*********");
        System.out.println(msg);
        System.out.println("**********************");
    }

    private void displayAddable() {
        System.out.println("**********************");
        System.out.println("Addable :");
        for(String user : currentUserAddable){
            System.out.println(user);
        }
        System.out.println("**********************");
    }


}
