package com.devops.agrishop;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class ChooseDeliveryMethodDialog extends DialogFragment {

    private static final String TAG = "ChooseDeliveryMethodDialog";

    private RadioButton rdbHomeDel,rdbPick;
    private TextView txtConfirm;
    private String deliverymethod = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_choose_delivery_method, container, false);

        rdbHomeDel = view.findViewById(R.id.radioBtnHomeDelivery);
        rdbPick = view.findViewById(R.id.radioBtnCollect);
        txtConfirm = view.findViewById(R.id.txtConfirm);

        txtConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rdbHomeDel.isChecked()){
                    deliverymethod = "Home Delivery";
                }

                if (rdbPick.isChecked()){
                    deliverymethod = "Pick from Collection Center";
                }

                if (!TextUtils.isEmpty(deliverymethod)){

                    Intent intent=new Intent(getContext(),ConfirmUserDetails.class);
                    intent.putExtra("deliverymethod",deliverymethod);
                    startActivity(intent);
                    getDialog().dismiss();
                    //Toast.makeText(view.getContext(), "You chose"+deliverymethod, Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(view.getContext(), "Please choose your preferred delivery method", Toast.LENGTH_LONG).show();                }
            }
        });

        TextView cancelDialog = view.findViewById(R.id.txtDMcancel);
        cancelDialog.setOnClickListener(v -> {
            getDialog().dismiss();
        });

        return view;
    }
}
