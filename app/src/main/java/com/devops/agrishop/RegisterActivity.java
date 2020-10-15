package com.devops.agrishop;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAdmNo,etPass,etPassOne;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private ProgressDialog pd;
    public static boolean isActivityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etAdmNo=findViewById(R.id.input_email);
        etPass=findViewById(R.id.input_password);
        etPassOne=findViewById(R.id.input_confirm_password);

        pd=new ProgressDialog(this);
        pd.setTitle("Creating Account");
        pd.setMessage("Just a moment...");
        mAuth=FirebaseAuth.getInstance();
        btnRegister=findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pd.show();
                String adm=etAdmNo.getText().toString().trim();
                String pass=etPass.getText().toString().trim();
                String pass1=etPassOne.getText().toString().trim();
                if (!TextUtils.isEmpty(adm) && !TextUtils.isEmpty(pass) &&!TextUtils.isEmpty(pass1))
                {
                    if (pass.equals(pass1)) {
                        //toast("Data ready for upload....");
                        mAuth.createUserWithEmailAndPassword(adm,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful())
                                {
                                    toast("Account created Successfully, Login to verify");
                                    pd.dismiss();
                                    mAuth.signOut();
                                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                    finish();
                                }else {
                                    pd.dismiss();
                                    toast(task.getException().getMessage());
                                }
                            }
                        });



                    }else {
                        pd.dismiss();

                        toast("Passwords dont match....");
                    }
                }else {
                    pd.dismiss();

                    toast("You left a blank !");
                }
            }
        });
    }

    private void toast(String s) {
        Toast.makeText(this, "Message: "+s, Toast.LENGTH_SHORT).show();
    }

    public void onLoginClick(View view) {
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left,android.R.anim.slide_out_right);
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
