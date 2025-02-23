package client;

import javax.swing.*;
import java.io.*;
import java.net.*;

public class ClientManager {
    private Socket socket; // 서버와의 연결을 위한 소켓
    private PrintWriter out; // 서버로 메시지를 전송할 PrintWriter
    private BufferedReader in; // 서버에서 메시지를 받을 BufferedReader
    private JTextArea messageDisplayArea; // 사용자에게 메시지를 표시하는 영역
    private ChatPanel chatPanel; // 채팅 메시지를 표시하는 패널
    private ACControlPanel acControlPanel; // 에어컨 제어 패널

    // 추가된 변수: 메시지 수신 스레드가 실행 중인지 확인하는 변수
    private boolean isReceiverThreadRunning = false;

    // 생성자: 클라이언트 소켓을 열고, 메시지를 수신하는 스레드를 시작
    public ClientManager(String host, int port, JTextArea messageDisplayArea, String seatNumber, ChatPanel chatPanel, ACControlPanel acControlPanel) throws IOException {
        this.messageDisplayArea = messageDisplayArea;
        this.chatPanel = chatPanel;
        this.acControlPanel = acControlPanel;

        socket = new Socket(host, port);  // 서버에 연결
        out = new PrintWriter(socket.getOutputStream(), true);  // 서버로 메시지를 보낼 준비
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));  // 서버에서 메시지를 읽을 준비

        out.println(seatNumber);  // 서버에 자리 번호를 전송

        startMessageReceiver();  // 서버에서 오는 메시지를 실시간으로 받기 위한 수신 스레드 시작
    }

    // 메시지를 수신하고 처리하는 메소드
    public void startMessageReceiver() {
        // 메시지 수신 스레드가 실행되지 않으면 실행
        if (!isReceiverThreadRunning) {
            isReceiverThreadRunning = true;
            new Thread(() -> {
                try {
                    String incomingMessage;
                    while ((incomingMessage = in.readLine()) != null) {  // 서버로부터 메시지를 계속해서 읽음
                        final String message = incomingMessage;  // 수신된 메시지를 final로 선언하여 람다에서 사용할 수 있게 함
                        SwingUtilities.invokeLater(() -> {  // UI 스레드에서 처리하도록 SwingUtilities.invokeLater 사용
                            // 채팅 메시지 수신 처리
                            if (this.chatPanel != null) {
                                this.chatPanel.appendMessage(message);  // 채팅창에 메시지를 추가
                            }

                            // 온도, 모드, 바람 세기 관련 메시지 처리
                            if (message.startsWith("온도 변경:")) {
                                String newTemp = message.split(":")[1].trim();
                                acControlPanel.updateCurrentTemperature(Integer.parseInt(newTemp));  // 에어컨 온도 업데이트
                            } else if (message.startsWith("모드 변경:")) {
                                String newMode = message.split(":")[1].trim();
                                acControlPanel.updateMode(newMode);  // 에어컨 모드 업데이트
                            } else if (message.startsWith("바람 세기 변경:")) {
                                String newWindSpeed = message.split(":")[1].trim();
                                acControlPanel.updateWindSpeed(Integer.parseInt(newWindSpeed));  // 바람 세기 업데이트
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();  // 예외 발생 시 스택 트레이스를 출력
                }
            }).start();
        }
    }

    // 서버로 메시지를 전송하는 메소드
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    // 소켓을 반환하는 메소드
    public Socket getSocket() {
        return socket;
    }

    // 소켓을 닫는 메소드
    public void close() throws IOException {
        if (socket != null) {
            socket.close();
            isReceiverThreadRunning = false;
        }
    }
}
