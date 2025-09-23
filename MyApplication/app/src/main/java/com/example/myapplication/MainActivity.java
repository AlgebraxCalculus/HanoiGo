package com.example.myapplication;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View bottomPanel = findViewById(R.id.bottomPanel);
        View groupIntro = findViewById(R.id.groupIntro);
        View groupLogin = findViewById(R.id.groupLogin);
        MaterialButton btnLetsGo = findViewById(R.id.btnLetsGo);

        btnLetsGo.setOnClickListener(v -> {
            // 1. Animate tăng chiều cao panel
            int startH = bottomPanel.getHeight();
            int targetH = (int) (getResources().getDisplayMetrics().heightPixels * 0.78f);
            targetH = Math.max(targetH, dpToPx(560));

            ValueAnimator anim = ValueAnimator.ofInt(startH, targetH);
            anim.setDuration(450);
            anim.addUpdateListener(va -> {
                ViewGroup.LayoutParams lp = bottomPanel.getLayoutParams();
                lp.height = (int) va.getAnimatedValue();
                bottomPanel.setLayoutParams(lp);
            });
            anim.start();

            // 2. Chuyển Intro -> Login (crossfade)
            groupIntro.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupIntro.setVisibility(View.GONE);
                        groupLogin.setAlpha(0f);
                        groupLogin.setVisibility(View.VISIBLE);
                        groupLogin.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });
    }
}
