package com.devops.agrishop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChangePhoneNumberFragment extends BottomSheetDialogFragment {

    View v;
    private TextView txtProfPhonenumber;
    private EditText editTextProfPhonenumber;
    private String userId;
    private Button btnProfPhonenumberSave;
    private FirebaseAuth mAuth;
    private DatabaseReference myUsersDatabase;
    private ProgressBar mProgressBar;

    public ChangePhoneNumberFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_bottom_edit_accountphonenumber, container, false);

        txtProfPhonenumber = v.findViewById(R.id.txtProfPhoneNumber);
        editTextProfPhonenumber = v.findViewById(R.id.editTextProfphonenumber);
        btnProfPhonenumberSave = v.findViewById(R.id.btnProfPhoneSave);
        mProgressBar = v.findViewById(R.id.changephonenoprogressBar);

        mAuth= FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");

        btnProfPhonenumberSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeprofphonenumber();
            }
        });

        myUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String phone = dataSnapshot.child("phone").getValue().toString();

                    txtProfPhonenumber.setText(phone);

                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

    private  void changeprofphonenumber(){
        if(editTextProfPhonenumber.getText().toString().equals("")){
            editTextProfPhonenumber.setError("Please Provide a Phone Number");
            return;
        }else{
            showDialog();
            myUsersDatabase
                    .child(userId)
                    .child("phone")
                    .setValue(editTextProfPhonenumber.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    hideDialog();
                    Toast.makeText(v.getContext(), "Successfully changed your phone number", Toast.LENGTH_LONG).show();
                    getDialog().dismiss();
                }
            });
        }

    }

    private void showDialog(){
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void hideDialog(){
        if(mProgressBar.getVisibility() == View.VISIBLE){
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}
