package com.example.myapplication;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

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
        View groupSignUp = findViewById(R.id.groupSignUp);

        MaterialButton btnLetsGo = findViewById(R.id.btnLetsGo);
        View tvSignUp = findViewById(R.id.tvSignUp);
        View tvLoginNow = findViewById(R.id.tvLoginNow);

        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        TextInputEditText edtUsername = findViewById(R.id.edtUsername);
        TextInputEditText edtPassword = findViewById(R.id.edtPassword);

        btnLetsGo.setOnClickListener(v -> {
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

        tvSignUp.setOnClickListener(v -> {
            groupLogin.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupLogin.setVisibility(View.GONE);
                        groupSignUp.setAlpha(0f);
                        groupSignUp.setVisibility(View.VISIBLE);
                        groupSignUp.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        tvLoginNow.setOnClickListener(v -> {
            groupSignUp.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupSignUp.setVisibility(View.GONE);
                        groupLogin.setAlpha(0f);
                        groupLogin.setVisibility(View.VISIBLE);
                        groupLogin.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        btnLogin.setOnClickListener(v -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this,
                        "Please enter username and password",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }
}

