package com.devops.agrishop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;

public class ConfirmUserDetails extends AppCompatActivity {
    
    private EditText txtUname,txtUphone,txtUstreetname,txtUboxno,txtUemail,txtUamount,txtUdate,txtUdeliverymethod;
    private EditText txtUpaymentmethod;
    private String deliverymethod,pno;
    private String home_delivery = "Home Delivery";
    private String mpesa= "M-pesa";
    private String amount = "";
    private DatePickerDialog datePickerDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference myUsersDatabase;
    private Button btnFinishCheckout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_user_details);
        
        txtUname = findViewById(R.id.txtUname);
        txtUphone = findViewById(R.id.txtUphone);
        txtUstreetname = findViewById(R.id.txtUstreetname);
        txtUboxno = findViewById(R.id.txtUBoxno);
        txtUemail = findViewById(R.id.txtUemail);
        txtUamount = findViewById(R.id.txtUamount);
        txtUpaymentmethod = findViewById(R.id.txtUpayementMethod);
        txtUdate = findViewById(R.id.txtUdate);
        txtUdeliverymethod = findViewById(R.id.txtUdeliverymethod);
        btnFinishCheckout = findViewById(R.id.btn_finish_checkout);

        mAuth=FirebaseAuth.getInstance();
        myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");

        deliverymethod=getIntent().getStringExtra("deliverymethod");
        
        if (deliverymethod.equals(home_delivery)){
            amount = "200";
        }
        
        txtUdeliverymethod.setText(deliverymethod);
        
        if (amount != null){
            txtUamount.setText(amount);
            txtUamount.setEnabled(false);
        }

        init();

        btnFinishCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoPayment();
            }
        });
    }

    private void init() {
        String userId=mAuth.getCurrentUser().getUid();
        String email= mAuth.getCurrentUser().getEmail();

        txtUemail.setText(email);
        txtUemail.setEnabled(false);

        myUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();
                    pno = dataSnapshot.child("phone").getValue().toString();

                    txtUname.setText(name);
                    txtUname.setEnabled(false);
                    txtUphone.setText(pno);
                    txtUphone.setEnabled(false);

                }else {
                    startActivity(new Intent(   ConfirmUserDetails.this,UserDetailsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        txtUdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calender class's instance and get current date , month and year from calender
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR); // current year
                int mMonth = c.get(Calendar.MONTH); // current month
                int mDay = c.get(Calendar.DAY_OF_MONTH); // current day
                // date picker dialog
                datePickerDialog = new DatePickerDialog(ConfirmUserDetails.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                txtUdate.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        txtUpaymentmethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // setup the alert builder
                AlertDialog.Builder builder = new AlertDialog.Builder(ConfirmUserDetails.this);
                builder.setTitle("Choose a payment method");
// add a checkbox list
                String[] payments = {"M-pesa", "Paypal", "Credit Card"};
                boolean[] checkedItems = {false, false, false};
                builder.setMultiChoiceItems(payments, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        // user checked or unchecked a box
                        switch (which) {
                            case 0:
                                txtUpaymentmethod.setText("M-pesa");
                                break;
                            case 1:
                                txtUpaymentmethod.setText("Paypal");
                                break;
                            case 2:
                                txtUpaymentmethod.setText("Credit card");
                                break;
                            default:
                                break;
                        }
                    }
                });
// add OK and Cancel buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // user clicked OK
                    }
                });
                builder.setNegativeButton("Cancel", null);
// create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void gotoPayment(){
        String streetname = txtUstreetname.getText().toString().trim();
        String boxno = txtUboxno.getText().toString().trim();
        String paymentmethod = txtUpaymentmethod.getText().toString().trim();
        String date = txtUdate.getText().toString().trim();
        if (!TextUtils.isEmpty(streetname) && !TextUtils.isEmpty(boxno) && !TextUtils.isEmpty(paymentmethod)
        && !TextUtils.isEmpty(date)){
            if (paymentmethod.equals(mpesa)){
                Intent intent=new Intent(ConfirmUserDetails.this, MpesaExpress.class);
                intent.putExtra("phonenumber",pno);
                intent.putExtra("amount" ,amount);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
            }
        }else{
            Toast.makeText(ConfirmUserDetails.this, "Please Ensure all Fields are set", Toast.LENGTH_LONG).show();
        }
    }

}
