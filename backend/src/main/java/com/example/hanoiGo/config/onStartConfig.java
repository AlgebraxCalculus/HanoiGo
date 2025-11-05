package com.example.hanoiGo.config;

import com.example.hanoiGo.service.UserService;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Component
public class onStartConfig {

    @Autowired
    private UserService UserService;

    private final Firestore db = FirestoreClient.getFirestore();

    // chạy mỗi 30 phút để kiểm tra
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void checkAndUpdateDailyChart() {
        try {
            System.out.println("Trigger chartUpdate running...");
            LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            DocumentReference metaRef = db.collection("meta").document("chartUpdate");
            DocumentSnapshot metaSnap = metaRef.get().get();

            String lastUpdated = metaSnap.exists() ? metaSnap.getString("lastUpdated") : null;

            if (lastUpdated == null || !lastUpdated.equals(today.toString())) {
                System.out.println("⚙️ Running chart data update for " + today);
                UserService.updateChartData();

                // cập nhật lại trạng thái trong Firestore
                metaRef.set(Map.of("lastUpdated", today.toString()));
                System.out.println("✅ Chart data updated for today. "+today);
            } else {
                System.out.println("⏸ Already updated today (" + today + "), skipping.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
