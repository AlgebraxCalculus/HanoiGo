package com.example.myapplication.controller;
import com.example.myapplication.R;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthFirebaseController {
    private final FirebaseAuth auth;
    private final GoogleSignInClient googleSignInClient;

    public AuthFirebaseController(Context context) {
        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, options);
    }

    public void signOut() {
        googleSignInClient.signOut();
        auth.signOut();
    }

    public void startGoogleSignIn(ActivityResultLauncher<Intent> launcher) {
        Intent intent = googleSignInClient.getSignInIntent();
        launcher.launch(intent);
    }

    public void handleGoogleSignInResult(Intent data, Context context, AuthCallback callback) {
        try {
            GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException.class);
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    auth.getCurrentUser().getIdToken(true).addOnCompleteListener(tokenTask -> {
                        if (tokenTask.isSuccessful()) {
                            callback.onSuccess(tokenTask.getResult().getToken());
                        } else {
                            callback.onFailure("Failed to get Firebase token");
                        }
                    });
                } else {
                    callback.onFailure("Firebase sign-in failed: " + task.getException().getMessage());
                }
            });
        } catch (ApiException e) {
            callback.onFailure("Google sign-in failed: " + e.getMessage());
        }
    }

    public interface AuthCallback {
        void onSuccess(String firebaseToken);
        void onFailure(String errorMessage);
    }
}