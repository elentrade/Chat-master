package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.lang.Thread;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

// отработка команд в блоке Initializable после отрисовки всех графических объектов
public class Controller implements Initializable {
    @FXML
    public TextArea txt_area;
    @FXML
    public TextField txt_field;
    @FXML
    public TextField login_field;
    @FXML
    public PasswordField password_field;
    @FXML
    public HBox top_panel;
    @FXML
    public HBox bottom_panel;
    private final String IP_ADRESS = "localhost";
    private final int PORT = 8189;
    private Socket socket;
    DataOutputStream out;
    DataInputStream in;
    private String nickName;
    private boolean authentificated;
    public void setAuthentificated(boolean authentificated){
        this.authentificated = authentificated;
        //при авторизации сделать видимой нижнюю панель, а верхнюю скрыть, никнейм обнулить
        top_panel.setVisible(!authentificated);
        top_panel.setManaged(!authentificated);
        bottom_panel.setVisible(authentificated);
        bottom_panel.setManaged(authentificated);
        if(!authentificated){nickName = "";}


    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //создание сокета клиента на стороне клиента, инициализация входящего и выходящего потоков
        //при создании подключения аудентификация еще не пройдена
        setAuthentificated(false);

    }

    //это отработает перед блоком Initializable
    // отсылка сообщения по нажатию кнопки
    public void send(ActionEvent actionEvent) {
        try {
            //в выходной поток отправить текст, затем очистить поле и вернуть на него фокус
            this.out.writeUTF(txt_field.getText());
            this.txt_field.clear();
            this.txt_field.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //по нажатию кнопки отсыл запроса на аутентификацию на сервер и ожидание результата
    public void tryAuth(ActionEvent actionEvent) {
        //проверка наличия соединения с сервером - если соединения нет - создаем
        if(socket==null || socket.isClosed()){
            connect(); //метод определен ниже
        }
        //если подключение есть - передаем данные аутентификации
        try {
            out.writeUTF(String.format("/auth %s %s", login_field.getText().trim().toLowerCase(), password_field.getText().trim()));
            //trim() - обрезка концевых пробелов toLowerCase() - приведение в строчным буквам
        } catch (IOException e) {
            e.printStackTrace();
        }
        password_field.clear();
    }
    //соединение с сервером
    private void connect() {
        try {
            this.socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    //обработка попыток залогиниться
                    while (true) {
                        String str = in.readUTF();
                        //от сервера поступит ответ "/authok nickName", который надо разбить (split) на 2 части, указав разделитель - пробел
                        // и взять вторую часть [1] из возвращаемого массива. Лимит 2 - чтобы ограничать число знаков
                        if (str.startsWith("/authok")) {
                            nickName = str.split(" ", 2)[1];
                            //если никнейм получен, то закрываем поля аутентификаци, открываем поля сообщений и выходим из цикла
                            setAuthentificated(true);
                            break;
                        }
                        txt_field.appendText(str+"\n");
                    }

                    //обработка входящих сообщений
                    while (true) {
                        String str = in.readUTF();
                        if (!str.equals("/end")) {
                            //txt_area.appendText("Клиент " + str + "\n");
                            txt_area.appendText("Клиент " + str + "\n");
                            continue;
                        }
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Мы отключились от сервера/controller");
                    setAuthentificated(false); //при отключении возвращаем окно в исходное состояние с доступом к полям аутентификации
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
