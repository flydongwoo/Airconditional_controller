package client;

import javax.swing.*;
import java.awt.*;

public class MainClientUI {
    public static void launch(String seatNumber) {
        // JFrame 설정: 사용자 UI 창 생성
        JFrame frame = new JFrame("사용자 자리: " + seatNumber);
        frame.setSize(800, 600);  // 창 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 창을 닫을 때 종료되도록 설정
        frame.setLayout(new GridLayout(1, 2));  // 1행 2열 레이아웃 설정

        // 채팅 UI (ChatPanel) 생성
        ChatPanel chatPanel = new ChatPanel(seatNumber);  // 사용자 자리 번호를 기반으로 ChatPanel 생성

        // 에어컨 조절기 UI (ACControlPanel) 생성
        ACControlPanel acControlPanel = new ACControlPanel(seatNumber);  // 사용자 자리 번호를 기반으로 ACControlPanel 생성

        // 채팅 패널과 에어컨 조절기 패널을 JFrame에 추가
        frame.add(chatPanel);
        frame.add(acControlPanel);

        // JFrame 표시
        frame.setVisible(true);

        // 클라이언트 소켓 관리
        try {
            // ClientManager 객체 생성, 채팅과 에어컨 컨트롤 패널을 전달하여 클라이언트 기능 초기화
            ClientManager clientManager = new ClientManager("localhost", 12345, chatPanel.getChatArea(), seatNumber, chatPanel, acControlPanel);
            chatPanel.setClientManager(clientManager);  // ChatPanel에 ClientManager 설정
            acControlPanel.setClientManager(clientManager);  // ACControlPanel에 ClientManager 설정
        } catch (Exception e) {
            // 서버 연결 실패 시 오류 메시지 표시
            JOptionPane.showMessageDialog(frame, "서버 연결 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
            System.exit(0);  // 오류 발생 시 프로그램 종료
        }
    }
}
