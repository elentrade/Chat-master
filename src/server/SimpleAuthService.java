package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService {
    //внутренний класс, содержащий поля для пользователя
    private class UserData{
        String login;
        String password;
        String nickname;
        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }
    //конструктор класса с созданием списка пользователей с  записями
    List<UserData> users;
    public  SimpleAuthService(){
        users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(new UserData("log"+i, "psw"+i, "nickname"+i));
        }
        users.add(new UserData("qwe","qwe","qwe"));
        users.add(new UserData("asd","asd","asd"));
        users.add(new UserData("zxc","zxc","zxc"));
    }
    @Override
    //реализация ИФ - метод сопоставляет входящие логин и пароль с данными в списке пользователей и возвращает ник или nukk
    public String getNickByLogAndPsw(String login, String password) {
        for (UserData user: users) {
            if(user.login.equals(login)&&user.password.equals(password)){
                return user.nickname;
            }
        }
        return null;
    }
}