package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345; // 서버가 연결을 수신할 포트 번호
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>()); // 연결된 클라이언트 리스트

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // 서버 소켓을 생성하여 포트 12345에서 클라이언트 연결 대기
            System.out.println("서버가 시작되었습니다...");
            ExecutorService threadPool = Executors.newFixedThreadPool(10); // 10개의 스레드를 동시에 처리할 수 있는 스레드 풀

            while (true) { // 무한 루프, 클라이언트 연결 대기
                try {
                    Socket clientSocket = serverSocket.accept(); // 클라이언트의 연결 요청을 받음
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                    // 클라이언트 자리 번호 수신
                    String seatNumber = in.readLine();
                    System.out.println("[INFO] 자리 번호 수신: " + seatNumber);

                    // 새로운 ClientHandler 객체 생성
                    ClientHandler clientHandler = new ClientHandler(clientSocket, clients, seatNumber);

                    // 중복 클라이언트 처리: 동일 자리 번호가 있는지 확인하고, 없다면 리스트에 추가
                    synchronized (clients) {
                        boolean isDuplicate = clients.stream().anyMatch(client -> client.getClientId().equals(seatNumber));
                        if (!isDuplicate) {
                            clients.add(clientHandler); // 중복되지 않으면 추가
                            System.out.println("[INFO] 클라이언트가 리스트에 추가되었습니다: " + seatNumber);
                        } else {
                            System.out.println("[WARNING] 이미 연결된 클라이언트입니다: " + seatNumber); // 중복된 클라이언트
                        }
                    }

                    // 클라이언트를 쓰레드 풀에서 실행
                    threadPool.execute(clientHandler);

                } catch (IOException e) {
                    System.err.println("[ERROR] 클라이언트 처리 중 오류 발생: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] 서버 오류: " + e.getMessage()); // 서버 소켓 생성 실패
        }
    }

    // 모든 클라이언트에게 메시지 전송
    public static void sendMessageToAllClients(String message) {
        for (ClientHandler writer : clients) {
            writer.sendMessage(message);  // 모든 클라이언트에게 상태 변경 메시지 전송
        }
    }
}
