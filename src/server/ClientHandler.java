package server;
//обработчик, который в отдельном потоке обрабатывает входящие и исходящие данные с каждого подключившегося клиента
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickName;

    public ClientHandler(final Server server, final Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            //выделеление обработки исходящего и входящего потока клиента в отдельный от графики поток
            new Thread(()-> {
                try {
//цикл аутентификации - пока не будет введен правильный логин пароль
                    while (true){
                        String str = in.readUTF();
                        //от сервера поступит auth login password
                        //если поступивший пакет начинается с auth, разбираем его на части  \\s = любое количество пробелов
                        if (str.startsWith("/auth")){
                            String []token = str.split("\\s"); //получаем массив из которого будем брать логин и пароль
                            //полученные логин и паорль передаем в метод для проверки наличия в списке пользователей
                            //если есть - вернет никнейм
                            String newNickName = server.getAuthService().getNickByLogAndPsw(token[1],token[2]);
                            //если получен никнейм, то присваиваем его значение никнейму и отправляем на сервер сообщение,
                            // что пользователь аутентифицирован и выходим из цикла
                            if (newNickName!=null){
                                nickName=newNickName;
                                sendMsg("/authok "+nickName);
                                //добавлем подключенного клиента в список
                                server.subscribe(this);
                                System.out.println("Клиент "+nickName+ " подключился");
                                break;
                            }
                            else {
                                System.out.println("Неверный логин/пароль");}
                        }

                    }


                    //цикл обработки входящего сообщения с сервера
                    while (true) {
                        String str = ClientHandler.this.in.readUTF();
                        //рассылка всем клиентам исходящего сообщения
                        //не понятно - str здесь входящее сообщение и оно же передается в исходящий поток????
                        if (!str.equals("/end")) {
                            server.broadcast(this,str);
                            continue;
                        }
                        return;
                    }
                    //System.out.println("Клиент отключился"); //кажись это лишнее
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //удаление данного клиента из списка. ссылка this не работает в runnable, поэтому поток запускаем через лямбду
                    server.unsubscribe(this);
                    System.out.println("Связь с сервером потеряна/handler");
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    //метод для передачи исходящей информации от клиента
    void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public String getNickName(){
        return nickName;
    }
}
