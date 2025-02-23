package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener; // ActionListener 추가

public class ACControlPanel extends JPanel {
    // UI에 표시되는 컴포넌트들
    private JLabel currentTempLabel; // 현재 온도 표시
    private JLabel targetTempLabel;  // 설정 온도 표시
    private JLabel modeLabel;        // 에어컨 모드 표시
    private JLabel windSpeedLabel;   // 바람 세기 표시

    private JButton increaseTempButton;  // 온도 상승 버튼
    private JButton decreaseTempButton;  // 온도 하강 버튼
    private JButton changeModeButton;   // 모드 변경 버튼
    private JButton callAdminButton;    // 관리자 호출 버튼
    private JButton voteYesButton;      // 투표 "Yes" 버튼
    private JButton voteNoButton;       // 투표 "No" 버튼
    private JButton increaseWindSpeedButton; // 바람 세기 증가 버튼
    private JButton decreaseWindSpeedButton; // 바람 세기 감소 버튼

    private int targetTemp = 25;        // 설정 온도 (초기값: 25°C)
    private final int MAX_TEMP = 26;    // 설정 가능한 최대 온도
    private final int MIN_TEMP = 18;    // 설정 가능한 최소 온도
    private String mode = "냉방";       // 현재 모드 (초기값: 냉방)
    private int windSpeed = 1;          // 바람 세기 (초기값: 1단)
    private String seatNumber;          // 클라이언트의 자리 번호
    private ClientManager clientManager; // 클라이언트 관리 객체


    public ACControlPanel(String seatNumber) {
        this.seatNumber = seatNumber;
        setLayout(new GridLayout(9, 1, 10, 10));  // 레이아웃 설정

        // UI 요소 초기화 및 추가
        currentTempLabel = new JLabel("현재 온도: 25°C", SwingConstants.CENTER); // 현재 온도 라벨
        add(currentTempLabel);

        targetTempLabel = new JLabel("설정 온도: " + targetTemp + "°C", SwingConstants.CENTER); // 설정 온도 라벨
        add(targetTempLabel);

        modeLabel = new JLabel("모드: " + mode, SwingConstants.CENTER); // 모드 라벨
        add(modeLabel);

        windSpeedLabel = new JLabel("바람 세기: " + windSpeed + "단", SwingConstants.CENTER); // 바람 세기 라벨
        add(windSpeedLabel);

        // 버튼 생성 및 이벤트 처리
        increaseTempButton = createButton("온도 상승", e -> adjustTemperature(1)); // 온도 상승 버튼
        decreaseTempButton = createButton("온도 하강", e -> adjustTemperature(-1)); // 온도 하강 버튼
        changeModeButton = createButton("모드 변경", e -> toggleMode());  // 모드 변경 버튼
        callAdminButton = createButton("관리자 호출", e -> callAdmin());  // 관리자 호출 버튼
        voteYesButton = createButton("투표: Yes", e -> vote(true));  // 투표 "Yes" 버튼
        voteNoButton = createButton("투표: No", e -> vote(false));   // 투표 "No" 버튼
        increaseWindSpeedButton = createButton("바람 세기 증가", e -> adjustWindSpeed(1)); // 바람 세기 증가 버튼
        decreaseWindSpeedButton = createButton("바람 세기 감소", e -> adjustWindSpeed(-1)); // 바람 세기 감소 버튼

        // 버튼들을 패널에 추가
        add(increaseTempButton);
        add(decreaseTempButton);
        add(changeModeButton);
        add(callAdminButton);
        add(voteYesButton);
        add(voteNoButton);
        add(increaseWindSpeedButton);
        add(decreaseWindSpeedButton);
    }

    // 버튼 생성 메소드 (텍스트, 동작을 받아 버튼을 생성)
    private JButton createButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);  // 버튼 클릭 시 실행할 동작 연결
        return button;
    }

    // 온도 조정 메소드 (온도 변경)
    private void adjustTemperature(int delta) {
        int newTemp = targetTemp + delta;
        if (newTemp >= MIN_TEMP && newTemp <= MAX_TEMP) {
            targetTemp = newTemp;
            targetTempLabel.setText("설정 온도: " + targetTemp + "°C");  // 설정 온도 UI 업데이트
            sendMessageToServer("온도 설정: " + targetTemp);  // 서버로 변경된 온도 전송
        } else {
            showErrorMessage("설정할 수 없는 온도입니다.");  // 유효하지 않은 온도일 때 오류 메시지
        }
    }

    // 모드 변경 메소드
    private void toggleMode() {
        mode = mode.equals("냉방") ? "난방" : "냉방";  // 모드를 '냉방' ↔ '난방'으로 토글
        modeLabel.setText("모드: " + mode);  // 모드 UI 업데이트
        sendMessageToServer("모드 변경: " + mode);  // 서버로 변경된 모드 전송
    }


    // 바람 세기 조정 메소드
    private void adjustWindSpeed(int delta) {
        int newSpeed = windSpeed + delta;
        if (newSpeed >= 1 && newSpeed <= 3) {
            windSpeed = newSpeed;
            windSpeedLabel.setText("바람 세기: " + windSpeed + "단");  // 바람 세기 UI 업데이트
            sendMessageToServer("바람 세기: " + windSpeed);  // 서버로 바람 세기 전송
        } else {
            showErrorMessage("바람 세기는 1단에서 3단까지 설정 가능합니다.");  // 유효하지 않은 바람 세기일 때 오류 메시지
        }
    }

    // 관리자 호출 메소드
    private void callAdmin() {
        sendMessageToServer("관리자 호출: " + seatNumber + "번 사용자가 관리자를 호출했습니다.");  // 관리자에게 호출 메시지 전송
    }

    // 투표 메소드
    private void vote(boolean isYes) {
        sendMessageToServer("투표: " + (isYes ? "yes" : "no"));  // 서버로 투표 결과 전송
    }

    // 서버로 메시지를 전송하는 메소드
    private void sendMessageToServer(String message) {
        if (clientManager != null) {
            clientManager.sendMessage(message);  // 클라이언트 관리자를 통해 메시지 전송
        }
    }

    // 오류 메시지 표시
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "오류", JOptionPane.ERROR_MESSAGE);  // 오류 메시지 팝업
    }

    // 클라이언트 관리자 설정 메소드
    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;  // 클라이언트 관리자를 설정
    }

    // 설정 온도 및 현재 온도 UI 업데이트 메소드
    public void updateCurrentTemperature(int newTemp) {
        targetTempLabel.setText("설정 온도: " + newTemp + "°C");  // 설정 온도 UI 업데이트
        currentTempLabel.setText("현재 온도: " + newTemp + "°C");  // 현재 온도 UI 업데이트
    }

    // 모드 업데이트 메소드
    public void updateMode(String newMode) {
        modeLabel.setText("모드: " + newMode);  // 모드 UI 업데이트
    }

    // 바람 세기 업데이트 메소드
    public void updateWindSpeed(int newWindSpeed) {
        windSpeedLabel.setText("바람 세기: " + newWindSpeed + "단");  // 바람 세기 UI 업데이트
    }
}