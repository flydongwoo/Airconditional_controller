package server;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AirConditionerManager {
    private static String season = "summer"; // 현재 계절 (여름/겨울)
    private static String modeChoice = "average"; // 에어컨 모드: "average" (평균 모드), "majority" (다수결 모드)
    private static int currentTemperature = 24; // 현재 온도
    private static final List<Integer> temperatures = new ArrayList<>(); // 사용자 요청 온도 저장 리스트
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 스케줄러: 평균 온도 계산을 위한 대기 시간 설정
    private static Runnable notifyClientsCallback; // 클라이언트 UI 업데이트를 위한 콜백
    private static int windSpeed = 1; // 바람 세기

    // 모드 변경
    public static void changeMode(String mode) {
        modeChoice = mode; // 모드 변경
        notifyClients("[알림] 모드가 변경되었습니다: " + mode); // 모드 변경 알림
    }

    // **현재 모드 반환 (추가된 메서드)**
    public static String getModeChoice() {
        return modeChoice; // 현재 모드 반환
    }

    // 계절 변경
    public static void changeSeason(String newSeason) {
        season = newSeason; // 계절 변경
        notifyClients("[알림] 계절이 변경되었습니다: " + (newSeason.equals("summer") ? "여름" : "겨울")); // 계절 변경 알림
    }

    // 모드 변경 허용 여부 확인
    public static boolean isModeChangeAllowed(String requestedMode) {
        if ((season.equals("summer") && requestedMode.equals("난방")) ||
                (season.equals("winter") && requestedMode.equals("냉방"))) {
            return false; // 계절에 따라 변경 불가능한 모드가 있음
        }
        return true;
    }

    // 계절 반환
    public static String getSeason() {
        return season; // 현재 계절 반환
    }

    // 온도 직접 설정
    public static void setTemperature(int temperature) {
        if (temperature < 18 || temperature > 26) {
            System.out.println("[오류] 설정 가능한 온도 범위는 18°C에서 26°C입니다."); // 온도 범위 초과 오류 처리
        } else {
            currentTemperature = temperature; // 온도 설정
            notifyClients("[알림] 관리자가 온도를 " + temperature + "°C로 설정하였습니다."); // 설정된 온도 알림
        }
    }

    // 사용자 요청 온도 추가
    public static synchronized void addTemperatureRequest(int temperature) {
        temperatures.add(temperature); // 사용자 요청 온도 리스트에 추가
        System.out.println("[INFO] 사용자 요청 온도 추가: " + temperature + "°C");

        // 5초 뒤 평균 온도 계산
        scheduler.schedule(AirConditionerManager::calculateAverageTemperature, 5, TimeUnit.SECONDS);
    }

    // 평균 온도 계산
    private static synchronized void calculateAverageTemperature() {
        if (!temperatures.isEmpty()) {
            int sum = temperatures.stream().mapToInt(Integer::intValue).sum(); // 온도 합산
            int average = (int) Math.round((double) sum / temperatures.size()); // 평균 계산
            currentTemperature = average; // 평균 온도 설정
            temperatures.clear(); // 리스트 초기화

            notifyClients("[알림] 평균 온도로 설정되었습니다: " + currentTemperature + "°C"); // 평균 온도 알림
            updateClientsTemperatureUI(); // 클라이언트 UI 업데이트
        }
    }

    private static void updateClientsTemperatureUI() {
        if (notifyClientsCallback != null) {
            notifyClientsCallback.run(); // 클라이언트 UI를 업데이트하는 콜백 호출
        }
    }

    // 현재 온도 반환
    public static int getCurrentTemperature() {
        return currentTemperature; // 현재 온도 반환
    }

    // 클라이언트 알림 콜백 설정
    public static void setNotifyClientsCallback(Runnable callback) {
        notifyClientsCallback = callback; // 클라이언트 UI 업데이트를 위한 콜백 설정
    }

    // 클라이언트에 메시지 전송
    private static void notifyClients(String message) {
        if (notifyClientsCallback != null) {
            notifyClientsCallback.run(); // 메시지를 클라이언트에게 전달하는 콜백 호출
        }
    }

    // 투표 결과 적용
    public static void applyVoteResult(boolean isApproved) {
        if (isApproved) {
            notifyClients("[알림] 투표에 따라 온도가 " + currentTemperature + "°C로 설정되었습니다."); // 투표 승인 시 온도 설정
            updateClientsTemperatureUI(); // UI 업데이트
        } else {
            notifyClients("[알림] 투표 결과 온도 변경이 거부되었습니다."); // 투표 거부 시
        }
    }

    // 모드 설정
    public static void setModeChoice(String newMode) {
        modeChoice = newMode; // 모드 변경
        notifyClients("[알림] 모드가 '" + newMode + "'로 변경되었습니다."); // 변경된 모드 알림
    }

    // ====== 바람 세기 관련 메서드 ======
    public static void adjustWindSpeed(int delta) {
        int newSpeed = windSpeed + delta; // 바람 세기 조정
        if (newSpeed >= 1 && newSpeed <= 3) {
            windSpeed = newSpeed; // 세기 설정
            notifyClients("[알림] 바람 세기가 " + windSpeed + "단으로 변경되었습니다."); // 바람 세기 변경 알림
            updateClientsWindSpeedUI(); // UI 업데이트
        }
    }

    private static void updateClientsWindSpeedUI() {
        if (notifyClientsCallback != null) {
            notifyClientsCallback.run(); // 바람 세기 변경 시 UI 업데이트
        }
    }

    // 바람 세기 반환
    public static int getWindSpeed() {
        return windSpeed; // 바람 세기 반환
    }
}
