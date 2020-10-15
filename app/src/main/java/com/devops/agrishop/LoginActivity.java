package com.devops.agrishop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText etAdmNo,etPass;
    private Button btnLogin;
    private FirebaseAuth mFirebaseAuth;

    private ProgressDialog pd;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private static final int RC_SIGN_IN = 1;
    public static boolean isActivityRunning;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etAdmNo=findViewById(R.id.editTxtEmail);
        etPass=findViewById(R.id.editTxtPassword);
        btnLogin=findViewById(R.id.loginButton);
        mFirebaseAuth=FirebaseAuth.getInstance();
        pd=new ProgressDialog(this);
        pd.setTitle("Logging in");
        pd.setMessage("Just a moment...");

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.googleBtn);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(v -> {
            signIn();
        });

        TextView resetPassword = findViewById(R.id.forgot_password);
        resetPassword.setOnClickListener(v -> {
            PasswordResetDialog dialog = new PasswordResetDialog();
            dialog.show(getSupportFragmentManager(), "dialog_password_reset");
            overridePendingTransition(R.anim.slide_out_up,R.anim.stay);
            dialog.setCancelable(false);
        });


        btnLogin.setOnClickListener(v -> {
            pd.show();
            String adm=etAdmNo.getText().toString().trim();
            String pass=etPass.getText().toString().trim();
            if (!TextUtils.isEmpty(adm) && !TextUtils.isEmpty(pass))
            {
                mFirebaseAuth.signInWithEmailAndPassword(adm,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            pd.dismiss();
                            startActivity(new Intent(LoginActivity.this,MainActivity.class));
                            finish();


                        }else{
                            pd.dismiss();

                            toast(task.getException().getMessage());
                        }
                    }
                });
            }else {
                pd.dismiss();

                toast("You left a blank !");
            }
        });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        
    }

    private void toast(String s) {
        Toast.makeText(this, "Message: "+s, Toast.LENGTH_SHORT).show();
    }


    public void goToRegister(View view) {
        startActivity(new Intent(this,RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        finish();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

}
