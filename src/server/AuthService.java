package server;

public interface AuthService {
    //метод, возвращающий ник-нейм по логину и паролю
    String getNickByLogAndPsw(String login, String password);
}
