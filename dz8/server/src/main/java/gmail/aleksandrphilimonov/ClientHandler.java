package gmail.aleksandrphilimonov;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private String userName;
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public String getUserName() {
        return userName;
    }

    public ClientHandler(Server server, Socket socket) throws IOException {
        this.server = server;
        this.socket = socket;
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        Thread t = new Thread(() -> {
            try {
                //цикл авторизации
                while (true) {
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login ")) {
                            String userNameFromLogin = msg.split("\\s")[1];
                            if (server.isUserOnLine(userNameFromLogin)) {
                                sendMessage("login_failed. Current nickname is already used");
                                continue;
                            }
                            userName = msg.split("\\s")[1];
                            sendMessage("/login_ok " + userName);
                            server.subscribe(this);
                            break;
                        }
                    }

                    //цикл общения с клиентом
                    while (true) {
                        String msg = in.readUTF();

                        if (msg.startsWith("/") && !userName.isEmpty()) {
                            executeCommand(msg);
                            continue;
                        }
                        server.broadCastMessage(userName + ": " + msg);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        });
        t.start();
    }

    private void executeCommand(String cmd){
        if (cmd.startsWith("/w ")) {
            String[] tokens = cmd.split("\\s", 3);
            server.sendPrivateMessage(this, tokens[1], tokens[2]);
        }else if(cmd.equals("/logOut")){
            server.unsubscribe(this);
        }
    }

    public void disconnect() {
        server.unsubscribe(this);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            disconnect();
        }
    }
}












