package gmail.aleksandrphilimonov;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ClientHandler> clients;

    public Server(int port) {
        this.port = port;
        clients = new ArrayList<>();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер запущен на порту " + port);

            while (true) {
                System.out.println("Ожидание нового клиента");
                Socket socket = serverSocket.accept();
                System.out.println("Новый клиент подключился");
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadCastMessage("Клиент " + clientHandler.getUserName() + " вошёл в чат.");
        broadcastClientsList();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadCastMessage("Клиент " + clientHandler.getUserName() + " вышел из чата.");
        broadcastClientsList();
    }

    public synchronized void broadCastMessage(String message){
        for (ClientHandler clientHandler : clients) {
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void sendPrivateMessage(ClientHandler sender, String receiverUsername, String message){
        for (ClientHandler c : clients) {
            if (c.getUserName().equals(receiverUsername)) {
                c.sendMessage("От: " + sender.getUserName() + " Сообщение: " + message);
                sender.sendMessage("Пользователь: " + receiverUsername + " Сообщение: " + message);
                return;
            }
        }
        sender.sendMessage("невозможно отправить сообщение пользователя: " + receiverUsername + ". Такого пользователя нет в сети.");

    }

    public synchronized boolean isUserOnLine(String userName) {
        for (ClientHandler clientHandler : clients) {
            if (clientHandler.getUserName().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastClientsList(){
        StringBuilder sb = new StringBuilder("/clients_list ");
        for (ClientHandler c : clients) {
            sb.append(c.getUserName()).append(" ");
        }
        sb.setLength(sb.length() - 1);
        String clientsList = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMessage(clientsList);
        }
    }
}









































