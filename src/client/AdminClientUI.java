package client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import server.AirConditionerManager;

public class AdminClientUI {

    // 관리자 클라이언트 UI를 시작하는 메소드
    public static void launch() {
        // JFrame 설정
        JFrame frame = new JFrame("관리자 클라이언트");
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // 메시지 로그 화면 초기화
        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);  // 읽기 전용
        JScrollPane scrollPane = new JScrollPane(logArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 관리 기능 버튼 패널 설정
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(5, 1));

        // 버튼 생성
        JButton announceButton = new JButton("공지 전송");
        JButton setModeButton = new JButton("모드 변경 (다수결/평균)");
        JButton setSeasonButton = new JButton("계절 변경 (여름/겨울)");
        JButton setTemperatureButton = new JButton("온도 직접 설정");
        JButton sendMessageButton = new JButton("투표 중재 메시지");

        buttonPanel.add(announceButton);
        buttonPanel.add(setModeButton);
        buttonPanel.add(setSeasonButton);
        buttonPanel.add(setTemperatureButton);
        buttonPanel.add(sendMessageButton);

        frame.add(buttonPanel, BorderLayout.EAST);

        // 서버와 연결 및 메시지 수신 설정
        try {
            ChatPanel chatPanel = new ChatPanel("admin");  // 채팅 패널 생성
            ACControlPanel acControlPanel = new ACControlPanel("admin");  // 에어컨 컨트롤 패널 생성

            ClientManager clientManager = new ClientManager("localhost", 12345, logArea, "admin", chatPanel, acControlPanel);  // 클라이언트 관리자 생성

            // 메시지 수신 쓰레드
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientManager.getSocket().getInputStream()));
                    String input;
                    while ((input = in.readLine()) != null) {
                        final String message = input;
                        // 메시지에 따라 다르게 처리
                        if (message.startsWith("[관리자 호출 요청]")) {
                            SwingUtilities.invokeLater(() -> {
                                synchronized (logArea) {
                                    logArea.append(message + "\n");
                                    logArea.setCaretPosition(logArea.getDocument().getLength());
                                }
                            });
                        } else if (message.startsWith("온도 변경:")) {
                            // 온도 변경 메시지 처리
                            SwingUtilities.invokeLater(() -> {
                                String newTemp = message.split(":")[1].trim();
                                acControlPanel.updateCurrentTemperature(Integer.parseInt(newTemp));
                            });
                        } else {
                            // 채팅 메시지 처리
                            SwingUtilities.invokeLater(() -> {
                                chatPanel.appendMessage(message);  // 채팅창에 메시지 추가
                                synchronized (logArea) {
                                    logArea.append(message + "\n");
                                    logArea.setCaretPosition(logArea.getDocument().getLength());
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(frame, "서버 연결이 종료되었습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                }
            }).start();

            // === 메시지 수신 쓰레드 끝 ===

            // 공지 전송
            announceButton.addActionListener(e -> {
                String message = JOptionPane.showInputDialog(frame, "공지 내용을 입력하세요:", "공지 전송", JOptionPane.PLAIN_MESSAGE);
                if (message != null && !message.trim().isEmpty()) {
                    clientManager.sendMessage("[공지] " + message);  // 공지 메시지 전송
                } else {
                    JOptionPane.showMessageDialog(frame, "공지 내용을 입력해주세요!", "입력 오류", JOptionPane.WARNING_MESSAGE);
                }
            });

            // 모드 변경 (다수결/평균)
            setModeButton.addActionListener(e -> {
                String[] options = {"다수결", "평균"};
                int choice = JOptionPane.showOptionDialog(
                        frame,
                        "에어컨 모드를 선택하세요:",
                        "모드 변경",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                if (choice != -1) {
                    String selectedMode = options[choice].equals("다수결") ? "다수결" : "평균";

                    // 모드 변경 시 서버에 반영
                    AirConditionerManager.setModeChoice(selectedMode);
                    clientManager.sendMessage("관리자 모드 변경: " + selectedMode);
                }
            });

            // 계절 변경 (여름/겨울)
            setSeasonButton.addActionListener(e -> {
                String[] options = {"여름", "겨울"};
                int choice = JOptionPane.showOptionDialog(
                        frame,
                        "계절을 선택하세요:",
                        "계절 변경",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[0]
                );
                if (choice != -1) {
                    String selectedSeason = options[choice].equals("여름") ? "summer" : "winter";
                    AirConditionerManager.changeSeason(selectedSeason);
                    clientManager.sendMessage("[관리자] 계절 변경: " + (selectedSeason.equals("summer") ? "여름" : "겨울"));
                }
            });

            // 온도 직접 설정
            setTemperatureButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(frame, "설정할 온도를 입력하세요 (18~26°C):", "온도 설정", JOptionPane.PLAIN_MESSAGE);
                if (input != null) {
                    try {
                        int temperature = Integer.parseInt(input.trim());
                        if (temperature < 18 || temperature > 26) {
                            JOptionPane.showMessageDialog(frame, "온도는 18~26°C 사이여야 합니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                        } else {
                            clientManager.sendMessage("관리자 온도 설정: " + temperature);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frame, "유효한 숫자를 입력하세요.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            // 투표 중재 메시지
            sendMessageButton.addActionListener(e -> {
                String input = JOptionPane.showInputDialog(frame, "투표 중재 메시지를 입력하세요:", "투표 중재 메시지", JOptionPane.PLAIN_MESSAGE);
                if (input != null && !input.trim().isEmpty()) {
                    clientManager.sendMessage("[관리자] " + input);
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "서버 연결 실패: " + e.getMessage(), "에러", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }

        frame.setVisible(true);  // 프레임을 화면에 표시
    }
}
