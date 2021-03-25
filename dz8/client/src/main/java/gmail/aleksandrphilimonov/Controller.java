package gmail.aleksandrphilimonov;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    TextArea msgArea;
    @FXML
    TextField msgField, userNameField;
    @FXML
    HBox loginPanel, messagePanel;
    @FXML
    ListView<String> clientsList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String userName;

    public void setUserName(String userName) {
        this.userName = userName;
        if (userName != null) {
            loginPanel.setVisible(false);
            loginPanel.setManaged(false);
            messagePanel.setVisible(true);
            messagePanel.setManaged(true);
            clientsList.setVisible(true);
            clientsList.setManaged(true);
        } else {
            loginPanel.setVisible(true);
            loginPanel.setManaged(true);
            messagePanel.setVisible(false);
            messagePanel.setManaged(false);
            clientsList.setVisible(false);
            clientsList.setManaged(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setUserName(null);
    }

    public void login() {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        if (userNameField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Имя пользователя не может быть пустым");
            alert.showAndWait();
            return;
        }

        try {
            out.writeUTF("/login " + userNameField.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 8189);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            Thread t1 = new Thread(() -> {
                try {
                    //цикл авторизации
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/login_ok ")) {
                            setUserName(msg.split("\\s")[1]);
                            break;
                        }

                        if (msg.startsWith("/login_failed ")) {
                            setUserName(msg.split("\\s")[1]);
                            String cause = msg.split("\\s", 2)[1];
                            msgArea.appendText(cause + "\n");
                        }
                    }
                    //цикл общения
                    while (true) {
                        String msg = in.readUTF();
                        if (msg.startsWith("/")) {
                            if (msg.startsWith("/clients_list ")) {
                                String[] tokens = msg.split("\\s");
                                Platform.runLater(() -> {
                                            clientsList.getItems().clear();
                                            for (int i = 1; i < tokens.length; i++) {
                                                clientsList.getItems().add(tokens[i]);
                                            }
                                        }
                                );

                            }
                            continue;
                        }
                        msgArea.appendText(msg + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    disconnect();
                }
            });
            t1.start();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно подключиться к серверу", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void sendMsg() {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно отправить сообщение", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void logOut() throws IOException {

        msgField.requestFocus();
        userNameField.clear();
        msgArea.clear();
        loginPanel.setVisible(true);
        loginPanel.setManaged(true);
        messagePanel.setVisible(false);
        messagePanel.setManaged(false);
        clientsList.setVisible(false);
        clientsList.setManaged(false);
        out.writeUTF("/logOut");
    }

    public void disconnect() {
        setUserName(null);
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
















