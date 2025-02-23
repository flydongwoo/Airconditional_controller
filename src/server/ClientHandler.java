package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;  // 클라이언트 소켓
    private BufferedReader in;    // 입력 스트림 (클라이언트에서 보내는 데이터 받기)
    private PrintWriter out;     // 출력 스트림 (서버에서 클라이언트로 데이터 보내기)
    private String clientId;     // 클라이언트의 아이디 (자리 번호)
    private List<ClientHandler> clients;  // 클라이언트 목록

    public ClientHandler(Socket socket, List<ClientHandler> clients, String seatNumber) {
        this.clientSocket = socket;  // 소켓 초기화
        this.clients = clients;      // 클라이언트 리스트 초기화
        this.clientId = seatNumber;  // 클라이언트 아이디(자리 번호) 설정
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));  // 입력 스트림 설정
            out = new PrintWriter(clientSocket.getOutputStream(), true);  // 출력 스트림 설정

            // 클라이언트 입장 메시지 처리
            System.out.println("[INFO] 클라이언트 연결: " + clientId);
            broadcastMessage(clientId + "번 고객님이 입장하셨습니다.");
            sendToAdmin(clientId + "번 고객님이 입장하셨습니다."); // 관리자에게 입장 알림

            String input;
            while ((input = in.readLine()) != null) {  // 클라이언트로부터 메시지 수신
                System.out.println("[INFO] 메시지 수신 (" + clientId + "): " + input);

                // 메시지 타입에 따른 처리
                if (input.startsWith("관리자 호출:")) {  // 관리자 호출 처리
                    sendToAdmin(input);
                } else if (input.startsWith("[공지]")) {
                    broadcastMessage(input);  // 공지 처리
                } else if (input.startsWith("온도 설정:")) {
                    handleTemperatureRequest(input);  // 온도 설정 요청 처리
                } else if (input.startsWith("투표:")) {
                    handleVote(input);  // 투표 처리
                } else if (input.startsWith("모드 변경:")) {
                    handleModeChangeRequest(input);  // 모드 변경 요청 처리
                } else if (input.startsWith("바람 세기:")) {
                    handleWindSpeedRequest(input);  // 바람 세기 설정 요청 처리
                } else {
                    broadcastMessage(input);  // 일반 메시지 브로드캐스트
                }
            }
        } catch (IOException e) {
            System.err.println("[오류] 클라이언트 통신 오류: " + clientId);  // 연결 오류 처리
        } finally {
            disconnectClient();  // 연결 종료 후 클라이언트 처리
        }
    }

    // 관리자 호출 메시지 처리
    private void sendToAdmin(String message) {
        String[] parts = message.split(":");  // 메시지를 ':'로 분리

        if (parts.length > 1) {
            for (ClientHandler client : clients) {
                if (client.getClientId().equalsIgnoreCase("admin")) {
                    client.sendMessage("[관리자 호출 요청] " + parts[1].trim());  // 관리자에게 호출 메시지 전송
                }
            }
        } else {
            System.err.println("[ERROR] 잘못된 메시지 포맷: " + message);  // 잘못된 포맷 처리
        }
    }

    // 투표 메시지 처리
    private void handleVote(String input) {
        boolean agree = input.split(":")[1].trim().equalsIgnoreCase("yes");  // 동의 여부 확인

        String resultMessage = clientId + "가 " + (agree ? "찬성" : "반대") + "합니다.";  // 투표 결과 메시지
        broadcastMessage(resultMessage);  // 다른 클라이언트에게 투표 결과 브로드캐스트

        // 투표 결과 처리
        VoteManager.handleVote(clientId, agree, clients);
    }

    // 온도 요청 처리
    private void handleTemperatureRequest(String input) {
        try {
            int temperature = Integer.parseInt(input.split(":")[1].trim());  // 온도 요청 처리

            // 모드에 따라 처리
            if (AirConditionerManager.getModeChoice().equals("average")) {
                AirConditionerManager.addTemperatureRequest(temperature);  // 평균 모드에서 온도 요청 추가
                sendMessage("[알림] 평균 모드로 요청이 반영되었습니다.");
            } else if (AirConditionerManager.getModeChoice().equals("majority")) {
                VoteManager.handleVoteRequest(temperature, this, clients);  // 다수결 모드에서 투표 요청
                sendMessage("[알림] 다수결 모드로 요청이 반영되었습니다.");
            } else {
                sendMessage("[알림] 유효하지 않은 모드입니다.");  // 잘못된 모드 처리
            }
        } catch (NumberFormatException e) {
            sendMessage("[오류] 잘못된 온도 값입니다.");  // 온도 값 오류 처리
        }
    }

    // 모드 변경 요청 처리
    private void handleModeChangeRequest(String input) {
        String requestedMode = input.split(":")[1].trim();  // 요청된 모드 확인
        if (!AirConditionerManager.isModeChangeAllowed(requestedMode)) {
            sendMessage("[알림] 현재 " + AirConditionerManager.getSeason() +
                    "이므로 '" + requestedMode + "' 모드로 변경이 불가능합니다.");
        } else {
            AirConditionerManager.changeMode(requestedMode);  // 모드 변경 처리
            broadcastMessage("[알림] " + clientId + "번 사용자가 " + requestedMode + " 모드로 변경하였습니다.");
        }
    }

    // 바람 세기 요청 처리
    private void handleWindSpeedRequest(String input) {
        try {
            int delta = Integer.parseInt(input.split(":")[1].trim());  // 바람 세기 조정 값 처리
            int currentSpeed = AirConditionerManager.getWindSpeed();
            int newSpeed = currentSpeed + delta;

            if (newSpeed < 1 || newSpeed > 3) {
                sendMessage("[알림] 바람 세기는 1단에서 3단 사이로 설정 가능합니다.");
            } else {
                AirConditionerManager.adjustWindSpeed(delta);  // 바람 세기 조정
            }
        } catch (NumberFormatException e) {
            sendMessage("[오류] 잘못된 바람 세기 값입니다.");  // 바람 세기 값 오류 처리
        }
    }

    // 클라이언트 연결 해제 처리
    private void disconnectClient() {
        try {
            clientSocket.close();  // 소켓 닫기
            clients.remove(this);  // 클라이언트 목록에서 제거
            broadcastMessage(clientId + "번 고객님이 퇴장하셨습니다.");  // 퇴장 메시지 전송
            System.out.println("[INFO] 클라이언트 연결 해제: " + clientId);
        } catch (IOException e) {
            System.err.println("[오류] 클라이언트 소켓 닫기 실패: " + clientId);
        }
    }

    // 메시지 전송
    public void sendMessage(String message) {
        out.println(message);  // 클라이언트로 메시지 전송
    }

    // 메시지 브로드캐스트 (모든 클라이언트에게 전파)
    private void broadcastMessage(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);  // 모든 클라이언트에게 메시지 전송
            }
        }

        // 관리자에게는 별도로 입장 정보를 전달
        for (ClientHandler client : clients) {
            if (client.getClientId().equalsIgnoreCase("admin")) {
                // "관리자"에게만 입장 정보를 따로 전달
                if (message.contains("입장하셨습니다")) {
                    System.out.println("[로그] 관리자에게 메시지 전송: " + message);  // 로그에 출력
                    client.sendMessage("[고객님이 입장하셨습니다.] " + message);  // 관리자에게 입장 메시지 전송
                }
            }
        }
    }

    public String getClientId() {
        return clientId;  // 클라이언트 아이디 반환
    }
}
