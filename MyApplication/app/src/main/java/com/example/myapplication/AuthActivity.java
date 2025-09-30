package com.example.myapplication;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaos.view.PinView;
import com.example.myapplication.controller.AuthFirebaseController;
import com.example.myapplication.api.AuthApi;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

public class AuthActivity extends AppCompatActivity {
    private AuthFirebaseController authFirebaseController;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    authFirebaseController.handleGoogleSignInResult(result.getData(), this, new AuthFirebaseController.AuthCallback() {
                        @Override
                        public void onSuccess(String firebaseToken) {
                            AuthApi.loginWithFirebase(firebaseToken, AuthActivity.this, new AuthApi.AuthApiCallback() {
                                @Override
                                public void onSuccess(String jwtToken, JSONObject userObj) {
                                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login successful!", Toast.LENGTH_SHORT).show());
                                    System.out.println("JWT token:" + jwtToken);
                                    System.out.println("user information: " + userObj.toString());
                                }

                                @Override
                                public void onSuccess(JSONObject userObj) {}

                                @Override
                                public void onFailure(String errorMessage) {
                                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show());
                                }
                            });
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(AuthActivity.this, "Google sign-in failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
    );

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        authFirebaseController = new AuthFirebaseController(this);
        authFirebaseController.signOut();

        PinView pinView = findViewById(R.id.pinViewOtp);
        pinView.requestFocus();




        View bottomPanel = findViewById(R.id.bottomPanel);
        View groupIntro = findViewById(R.id.groupIntro);
        View groupLogin = findViewById(R.id.groupLogin);
        View groupSignUp = findViewById(R.id.groupSignUp);   // thêm Sign Up panel
        View groupForgotPass = findViewById(R.id.groupForgotPass);
        View groupVerifyEmail = findViewById(R.id.groupVerifyEmail);
        View groupResetPass = findViewById(R.id.groupResetPass);

        MaterialButton btnLetsGo = findViewById(R.id.btnLetsGo);
        View tvSignUp = findViewById(R.id.tvSignUp);         // link "Sign up now"
        View tvLoginNow = findViewById(R.id.tvLoginNow);     // link "Login now" trong Sign Up
        View tvForgotPass = findViewById(R.id.tvForgot);
        MaterialButton btnBackToLogin = findViewById(R.id.btnBackToLogin);
        MaterialButton btnVerifyBack = findViewById(R.id.btnVerifyBack);
        MaterialButton btnResetBack = findViewById(R.id.btnResetBack);


        // Khi bấm "Let’s Go" → Intro -> Login
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

            // intro -> login
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

        // Khi bấm "Sign up now" → Login -> Sign Up
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

        // Khi bấm "Login now" → Sign Up -> Login
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

        // Khi bấm "Forgot password?" → Login -> Forgot Password
        tvForgotPass.setOnClickListener(v -> {
            groupLogin.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupLogin.setVisibility(View.GONE);
                        groupForgotPass.setAlpha(0f);
                        groupForgotPass.setVisibility(View.VISIBLE);
                        groupForgotPass.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        // Khi bấm "Back to Login" => Forgot password -> Login
        btnBackToLogin.setOnClickListener(v -> {
            groupForgotPass.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupForgotPass.setVisibility(View.GONE);
                        groupLogin.setAlpha(0f);
                        groupLogin.setVisibility(View.VISIBLE);
                        groupLogin.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        // Khi bấm "Back" trong Verify your email => Verify your email -> Forgot password
        btnVerifyBack.setOnClickListener(v -> {
            groupVerifyEmail.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupVerifyEmail.setVisibility(View.GONE);
                        groupForgotPass.setAlpha(0f);
                        groupForgotPass.setVisibility(View.VISIBLE);
                        groupForgotPass.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        // Khi bấm "Back" trong Reset password => Reset password -> Verify your email
        btnResetBack.setOnClickListener(v -> {
            groupResetPass.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupResetPass.setVisibility(View.GONE);
                        groupVerifyEmail.setAlpha(0f);
                        groupVerifyEmail.setVisibility(View.VISIBLE);
                        groupVerifyEmail.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });



        // Xử lý event cho các nút chức năng chính
        //NÚT btnGoogle
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> authFirebaseController.startGoogleSignIn(activityResultLauncher));

        //NÚT btnLogin
        MaterialButton btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            String username = ((EditText)findViewById(R.id.edtUsername)).getText().toString();
            String password = ((EditText)findViewById(R.id.edtPassword)).getText().toString();

            AuthApi.login(username, password, AuthActivity.this, new AuthApi.AuthApiCallback() {
                @Override
                public void onSuccess(String jwtToken, JSONObject userObj) {
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login successful!", Toast.LENGTH_SHORT).show());
                    System.out.println("JWT token:" + jwtToken);
                    System.out.println("user information: " + userObj.toString());
                }

                @Override
                public void onSuccess(JSONObject userObj) {}

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        });

        //NÚT btnSignup
        MaterialButton btnSignup = findViewById(R.id.btnSignUp);
        btnSignup.setOnClickListener(v -> {
            String username = ((EditText)findViewById(R.id.edtSignUpUsername)).getText().toString();
            String email = ((EditText)findViewById(R.id.edtSignUpEmail)).getText().toString();
            String password = ((EditText)findViewById(R.id.edtSignUpPassword)).getText().toString();

            AuthApi.register(username, email, password, AuthActivity.this, new AuthApi.AuthApiCallback() {
                @Override
                public void onSuccess(String jwtToken,JSONObject userObj) {}

                @Override
                public void onSuccess(JSONObject userObj) {
                    runOnUiThread(() -> {
                        Toast.makeText(AuthActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                        System.out.println("user information: " + userObj.toString());

                        // Chuyển từ Sign Up sang Login
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
                    //TODO: chuyển sang màn hình login

                }

                @Override
                public void onFailure(String errorMessage) {
                    runOnUiThread(() -> Toast.makeText(AuthActivity.this, "Signup failed: " + errorMessage, Toast.LENGTH_SHORT).show());
                }
            });
        });

        //NÚT btnSend
        MaterialButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            groupForgotPass.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupForgotPass.setVisibility(View.GONE);
                        groupVerifyEmail.setAlpha(0f);
                        groupVerifyEmail.setVisibility(View.VISIBLE);
                        groupVerifyEmail.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        //NÚT btnVerify
        MaterialButton btnVerify = findViewById(R.id.btnVerify);
        btnVerify.setOnClickListener(v -> {
            groupVerifyEmail.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupVerifyEmail.setVisibility(View.GONE);
                        groupResetPass.setAlpha(0f);
                        groupResetPass.setVisibility(View.VISIBLE);
                        groupResetPass.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        //NÚT btnSave
        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            groupResetPass.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        groupResetPass.setVisibility(View.GONE);
                        groupLogin.setAlpha(0f);
                        groupLogin.setVisibility(View.VISIBLE);
                        groupLogin.animate().alpha(1f).setDuration(300).start();
                    })
                    .start();
        });

        TextView tvResendOtp = findViewById(R.id.tvResendOtp);
        tvResendOtp.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
    }

    
}