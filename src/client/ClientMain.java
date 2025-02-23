package client;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibrarySeatSelector.main(args); // 자리 선택 UI 실행
        });
    }
}
