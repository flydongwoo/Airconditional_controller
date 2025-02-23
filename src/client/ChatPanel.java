package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChatPanel extends JPanel {
    private JTextArea chatArea; // 채팅 메시지를 표시하는 텍스트 영역
    private JTextField inputField; // 사용자 입력을 받는 텍스트 필드
    private JButton sendButton; // 메시지 전송 버튼
    private ClientManager clientManager; // 서버와의 통신을 관리하는 클라이언트 매니저

    // 채팅 패널의 UI를 설정
    public ChatPanel(String seatNumber) {
        setLayout(new BorderLayout());  // 레이아웃 설정

        // 채팅창
        chatArea = new JTextArea();  // 채팅 내용을 표시할 텍스트 영역
        chatArea.setEditable(false); // 채팅 영역은 읽기 전용
        add(new JScrollPane(chatArea), BorderLayout.CENTER); // 텍스트 영역을 스크롤 가능하게 추가

        // 입력창 및 버튼
        inputField = new JTextField(); // 메시지 입력을 위한 텍스트 필드
        sendButton = new JButton("보내기"); // 메시지 전송 버튼
        JPanel inputPanel = new JPanel(new BorderLayout());  // 입력 필드를 담을 패널
        inputPanel.add(inputField, BorderLayout.CENTER);  // 입력 필드를 중앙에 추가
        inputPanel.add(sendButton, BorderLayout.EAST);  // 전송 버튼을 오른쪽에 추가
        add(inputPanel, BorderLayout.SOUTH); // 입력 패널을 하단에 추가

        // 메시지 전송 버튼 클릭 시 동작
        sendButton.addActionListener((ActionEvent e) -> {
            String message = inputField.getText().trim();  // 입력된 메시지 가져오기
            if (!message.isEmpty()) {
                sendMessageToServer(seatNumber + ": " + message);  // 서버로 메시지 전송
                inputField.setText(""); // 입력 필드 초기화
            }
        });
    }

    // 새로운 메시지를 채팅 창에 추가하는 메소드
    public void appendMessage(String message) {
        chatArea.append(message + "\n");  // 채팅창에 메시지 추가
        chatArea.setCaretPosition(chatArea.getDocument().getLength()); // 스크롤을 맨 아래로 이동
    }

    // 클라이언트 매니저 설정 메소드
    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    // 서버로 메시지를 전송하는 메소드
    private void sendMessageToServer(String message) {
        if (clientManager != null) {
            clientManager.sendMessage(message); // 메시지를 서버로 전송
        }
    }

    // 채팅 영역을 반환하는 메소드
    public JTextArea getChatArea() {
        return chatArea;
    }
}
