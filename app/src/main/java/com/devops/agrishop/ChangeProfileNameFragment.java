package com.devops.agrishop;

import android.content.Intent;
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
import com.squareup.picasso.Picasso;

public class ChangeProfileNameFragment extends BottomSheetDialogFragment {

    View v;
    public TextView txtProfName;
    private EditText editTextProfName;
    private String userId;
    private Button btnProfNameSave;
    private FirebaseAuth mAuth;
    private DatabaseReference myUsersDatabase;
    private ProgressBar mProgressBar;

    public  ChangeProfileNameFragment(){
        //Empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_bottom_edit_accountname, container, false);

        txtProfName = v.findViewById(R.id.txtProfName);
        editTextProfName = v.findViewById(R.id.editTextProfName);
        btnProfNameSave = v.findViewById(R.id.btnProfNameSave);
        mProgressBar = v.findViewById(R.id.changeprofnameprogressBar);

        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");

        btnProfNameSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeprofname();
            }
        });

        myUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue().toString();

                    txtProfName.setText(name);

                }else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

    private  void changeprofname(){
        if(editTextProfName.getText().toString().equals("")){
            editTextProfName.setError("Please Provide your full name");
            return;
        }else{
            showDialog();
            myUsersDatabase
                    .child(userId)
                    .child("name")
                    .setValue(editTextProfName.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    hideDialog();
                    Toast.makeText(v.getContext(), "Successfully changed your profile name", Toast.LENGTH_LONG).show();
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
