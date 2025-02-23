package server;

public class ServerMain {
    public static void main(String[] args) {
        try {
            Server.main(args); // Server 클래스 실행
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
