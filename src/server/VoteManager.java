package server;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VoteManager {
    private static final Map<String, Integer> voteCounts = new ConcurrentHashMap<>(); // 투표 결과를 저장하는 맵
    private static volatile String lastSender = null; // 마지막 투표 요청자
    private static volatile int lastRequestedTemperature = 24; // 최근 요청된 온도 (기본값: 24)
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 30초 타이머 관리용 스케줄러

    // 투표 요청 처리
    public static void handleVoteRequest(int temperature, ClientHandler sender, List<ClientHandler> clients) {
        lastSender = sender.getClientId();
        lastRequestedTemperature = temperature; // 요청된 온도 저장

        // 투표 결과 초기화
        synchronized (voteCounts) {
            voteCounts.clear();
            voteCounts.put("yes", 0);
            voteCounts.put("no", 0);
        }

        // 투표 메시지 브로드캐스트
        String voteMessage = String.format("%s번 고객님이 %d도로 설정하기를 원합니다. 동의하시면 Yes버튼을, 동의하지 않으시면 No버튼을 눌러주세요. (30초가 지나면 자동으로 동의하는 것으로 간주합니다.)",
                sender.getClientId(), temperature);
        broadcastToAllClients(voteMessage, sender, clients);

        // 30초 타이머 설정: 30초 후 투표 결과를 처리하는 메소드 호출
        scheduler.schedule(() -> finalizeVote(clients), 30, TimeUnit.SECONDS);
    }

    // 투표 처리
    public static void handleVote(String clientId, boolean agree, List<ClientHandler> clients) {
        // 요청자는 투표에 참여할 수 없음
        if (clientId.equals(lastSender)) {
            findClientById(clientId, clients).sendMessage("[알림] 요청자는 투표에 참여할 수 없습니다.");
            return;
        }

        // 투표 결과 카운트
        synchronized (voteCounts) {
            if (agree) {
                voteCounts.merge("yes", 1, Integer::sum);
            } else {
                voteCounts.merge("no", 1, Integer::sum);
            }
        }

        // 투표 완료 여부 확인: 요청자를 제외한 모든 클라이언트가 투표 완료 시 결과 처리
        int totalVotes = voteCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalVotes >= clients.size() - 1) {
            finalizeVote(clients); // 투표 종료 처리
        }
    }

    // 투표 종료 및 결과 처리
    private static void finalizeVote(List<ClientHandler> clients) {
        synchronized (voteCounts) {
            // 다수결로 결과 처리
            boolean isApproved = voteCounts.getOrDefault("yes", 0) > voteCounts.getOrDefault("no", 0);

            // 온도 변경 여부 적용
            if (isApproved) {
                AirConditionerManager.setTemperature(lastRequestedTemperature); // 온도 변경
                AirConditionerManager.applyVoteResult(true); // 변경 승인
            } else {
                AirConditionerManager.applyVoteResult(false); // 변경 거부
            }

            // 투표 결과 메시지 전파
            String resultMessage = "[투표 결과] " + (isApproved ? "찬성 다수로 변경 승인" : "반대 다수로 변경 거부");
            broadcastToAllClients(resultMessage, null, clients);

            // 투표 초기화
            voteCounts.clear();
            lastSender = null;
        }
    }

    // 클라이언트 ID로 클라이언트를 찾는 헬퍼 메소드
    private static ClientHandler findClientById(String clientId, List<ClientHandler> clients) {
        for (ClientHandler client : clients) {
            if (client.getClientId().equals(clientId)) {
                return client;
            }
        }
        return null;
    }

    // 모든 클라이언트에게 메시지를 전파하는 메소드
    private static void broadcastToAllClients(String message, ClientHandler excludeClient, List<ClientHandler> clients) {
        for (ClientHandler client : clients) {
            if (excludeClient == null || !client.getClientId().equals(excludeClient.getClientId())) {
                client.sendMessage(message); // 제외된 클라이언트를 제외하고 메시지 전송
            }
        }
    }
}
