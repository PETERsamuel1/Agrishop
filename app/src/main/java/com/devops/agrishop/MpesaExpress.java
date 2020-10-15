package com.devops.agrishop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidstudy.daraja.Daraja;
import com.androidstudy.daraja.DarajaListener;
import com.androidstudy.daraja.model.AccessToken;
import com.androidstudy.daraja.model.LNMExpress;
import com.androidstudy.daraja.model.LNMResult;
import com.androidstudy.daraja.util.TransactionType;

public class MpesaExpress extends AppCompatActivity {

    //Declare Daraja :: Global Variable
    private Daraja daraja;

    private String phoneNumber,amount,fullnumber;
    private Button sendButton;
    private EditText editTextPhoneNumber;
    private String code = "254";
    private ProgressBar mProgressbar;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa_express);

        sendButton = findViewById(R.id.sendButton);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        progressText = findViewById(R.id.txtProgress);

        phoneNumber = getIntent().getStringExtra("phonenumber");
        fullnumber = code+phoneNumber;
        amount = getIntent().getStringExtra("amount");
        editTextPhoneNumber.setText(phoneNumber);

        mProgressbar = findViewById(R.id.mpesaprogressBar);

        //Init Daraja
        //TODO :: REPLACE WITH YOUR OWN CREDENTIALS  :: THIS IS SANDBOX DEMO
        showDialog();
        daraja = Daraja.with("Wg9NW21CHaxl4xd8lfvuK5RPABEVGwHB", "G6IteK4XpIqHK6xX", new DarajaListener<AccessToken>() {
            @Override
            public void onResult(@NonNull AccessToken accessToken) {
                hideDialog();
                Log.i(MpesaExpress.this.getClass().getSimpleName(), accessToken.getAccess_token());
                //Toast.makeText(MpesaExpress.this, "TOKEN : " + accessToken.getAccess_token(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MpesaExpress.this, "Authenticating your details", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String error) {
                hideDialog();
                Log.e(MpesaExpress.this.getClass().getSimpleName(), error);
                Toast.makeText(MpesaExpress.this ,"Error authenticating your details, Please try again", Toast.LENGTH_LONG).show();
            }
        });

        //TODO :: THIS IS A SIMPLE WAY TO DO ALL THINGS AT ONCE!!! DON'T DO THIS :)
        sendButton.setOnClickListener(v -> {

            showDialog();
            //Get Phone Number from User Input
            phoneNumber = editTextPhoneNumber.getText().toString().trim();

            if (TextUtils.isEmpty(phoneNumber)) {
                editTextPhoneNumber.setError("Please Provide a Phone Number");
                return;
            }

            //TODO :: REPLACE WITH YOUR OWN CREDENTIALS  :: THIS IS SANDBOX DEMO
            LNMExpress lnmExpress = new LNMExpress(
                    "174379",
                    "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919",  //https://developer.safaricom.co.ke/test_credentials
                    TransactionType.CustomerBuyGoodsOnline, // TransactionType.CustomerPayBillOnline  <- Apply any of these two
                    amount,
                    fullnumber,
                    "174379",
                    phoneNumber,
                    "http://mycallbackurl.com/checkout.php",
                    "001ABC",
                    "Goods Payment"
            );

            //This is the
            daraja.requestMPESAExpress(lnmExpress,
                    new DarajaListener<LNMResult>() {
                        @Override
                        public void onResult(@NonNull LNMResult lnmResult) {
                            hideDialog();
                            Log.i(MpesaExpress.this.getClass().getSimpleName(), lnmResult.ResponseDescription);
                            Toast.makeText(MpesaExpress.this, "Contacting Safaricom....almost there.", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onError(String error) {
                            hideDialog();
                            Log.i(MpesaExpress.this.getClass().getSimpleName(), error);
                        }
                    }
            );
        });
    }

    private void showDialog(){
        mProgressbar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
    }

    private void hideDialog(){
        if(mProgressbar.getVisibility() == View.VISIBLE && progressText.getVisibility() == View.VISIBLE){
            mProgressbar.setVisibility(View.INVISIBLE);
            progressText.setVisibility(View.INVISIBLE);
            sendButton.setEnabled(true);
        }
    }
}
