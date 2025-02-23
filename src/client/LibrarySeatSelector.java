package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LibrarySeatSelector {
    public static void main(String[] args) {
        // JFrame 설정: 도서관 자리 선택 UI
        JFrame frame = new JFrame("도서관 자리 선택");
        frame.setSize(300, 150);  // 프레임 크기 설정
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // 창을 닫을 때 종료되도록 설정
        frame.setLayout(new GridLayout(3, 1));  // 3행 1열 레이아웃 설정

        // 자리 번호 입력 라벨
        JLabel label = new JLabel("자리 번호를 입력하세요 (1~99):", SwingConstants.CENTER);
        frame.add(label);

        // 자리 번호를 입력받는 텍스트 필드
        JTextField seatField = new JTextField();
        frame.add(seatField);

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        frame.add(confirmButton);

        // 확인 버튼 클릭 시 동작
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 입력된 자리 번호를 가져옴
                String input = seatField.getText().trim();

                // 자리 번호가 비어있으면 경고 메시지
                if (input.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "자리 번호를 입력해주세요!");
                }
                // admin 입력 시 관리자 UI 실행
                else if (input.equalsIgnoreCase("admin")) {
                    frame.dispose();  // 현재 창을 닫고
                    AdminClientUI.launch();  // 관리자 UI 실행
                } else {
                    // 자리 번호가 숫자이고 1~99 범위인지 확인
                    try {
                        int seatNumber = Integer.parseInt(input);  // 자리 번호를 숫자로 변환
                        if (seatNumber < 1 || seatNumber > 99) {  // 유효하지 않은 번호
                            throw new NumberFormatException("자리 번호는 1부터 99 사이여야 합니다.");
                        }
                        frame.dispose();  // 현재 창을 닫고
                        MainClientUI.launch(String.valueOf(seatNumber));  // 자리 번호를 MainClientUI로 전달하여 실행
                    } catch (NumberFormatException ex) {
                        // 유효하지 않은 입력에 대해 경고 메시지
                        JOptionPane.showMessageDialog(frame, "유효한 자리 번호를 입력하세요 (1~99).", "입력 오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        // 창을 보이도록 설정
        frame.setVisible(true);
    }
}
