package com.ccaroni.kreasport.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.BaseCallback;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.LockCallback;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;
import com.ccaroni.kreasport.other.CredentialsManager;

public class NewLogin extends AppCompatActivity {

    private static final String LOG = NewLogin.class.getSimpleName();

    private Lock mLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Auth0 auth0 = new Auth0("0FlTsCGzAeTXuCOeJmAutqEJyuBKAhzU", "caroni.eu.auth0.com");
        auth0.setOIDCConformant(true);

        mLock = Lock.newBuilder(auth0, mCallback)
                .withScope("openid offline_access read:races update:races create:races")
                .withAudience("kreasport-jwt-api")
                .build(this);

        String accessToken = CredentialsManager.getCredentials(this).getAccessToken();
        if (accessToken == null) {
            Log.d(LOG, "access token is null, showing login");
            startLockWidget();
        } else {
            final AuthenticationAPIClient aClient = new AuthenticationAPIClient(auth0);
            aClient.userInfo(accessToken)
                    .start(new BaseCallback<UserProfile, AuthenticationException>() {
                        @Override
                        public void onSuccess(final UserProfile payload) {
                            Log.d(LOG, "access token validation: SUCCESS");
                            autoLoginRedirect();
                        }

                        @Override
                        public void onFailure(AuthenticationException error) {
                            Log.d(LOG, "access token validation: FAIL");
                            attemptRefresh(aClient);
                        }
                    });
        }
    }

    private void attemptRefresh(AuthenticationAPIClient aClient) {
        String refreshToken = CredentialsManager.getCredentials(this).getRefreshToken();
        if (refreshToken == null) {
            startLockWidget();
        } else {
            aClient.renewAuth(refreshToken)

                    .start(new BaseCallback<Credentials, AuthenticationException>() {

                        @Override
                        public void onSuccess(Credentials credentials) {
                            Log.d(LOG, "refresh token validation: SUCCESS");
                            CredentialsManager.saveCredentials(NewLogin.this, credentials);
                            autoLoginRedirect();
                        }

                        @Override
                        public void onFailure(AuthenticationException error) {
                            Log.d(LOG, "refresh token validation: SUCCESS");
                            startLockWidget();
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Your own Activity code
        mLock.onDestroy(this);
        mLock = null;
    }

    private final LockCallback mCallback = new AuthenticationCallback() {
        @Override
        public void onAuthentication(Credentials credentials) {
            Log.d(LOG, "Login - Success");
            CredentialsManager.saveCredentials(NewLogin.this, credentials);
            startActivity(new Intent(NewLogin.this, HomeActivity.class));
            finish();
        }

        @Override
        public void onCanceled() {
            Log.d(LOG, "Login - Cancelled");
        }

        @Override
        public void onError(LockException error) {
            Log.d(LOG, "Login - Error:" + error.toString());
        }
    };

    private void startLockWidget() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(NewLogin.this, "Session Expired, please Log In", Toast.LENGTH_SHORT).show();
            }
        });

        CredentialsManager.deleteCredentials(this);
        startActivity(mLock.newIntent(this));
    }

    /**
     * Finishes this activity and goes to {@link HomeActivity}.
     */
    private void autoLoginRedirect() {
        runOnUiThread(new Runnable() {
            public void run() {
                Log.d(LOG, "Login - Automatic login success");
            }
        });
        startActivity(new Intent(NewLogin.this, HomeActivity.class));
        finish();
    }

}