package com.devops.agrishop;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    View v;
    private RecyclerView cartList;
    private DatabaseReference myCartDatabase,myCartUserDb;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference myUsersDatabase;
    private int CALL_PERMISSION_CODE=345;
    private String TAG = "Usercart";
    private Button btnCheckout;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_users, container, false);
        cartList=v.findViewById(R.id.cartList);
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        myCartDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Cart").child(userId);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        cartList.setLayoutManager(linearLayoutManager);

        btnCheckout = v.findViewById(R.id.btnCheckout);
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChooseDeliveryMethodDialog dialog = new ChooseDeliveryMethodDialog();
                dialog.show(getFragmentManager(), getString(R.string.dialog_choose_delivery_method));
                dialog.setCancelable(false);
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Cart> options=new FirebaseRecyclerOptions.Builder<Cart>()
                .setQuery(myCartDatabase,Cart.class)
                .build();

        FirebaseRecyclerAdapter<Cart,CartViewHolder> adapter=new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CartViewHolder holder, int position, @NonNull Cart model) {

                String postid = model.getPost_id();
                String pname = model.getName();
                holder.cart_tvName.setText(model.getName());
                holder.cart_tvQty.setText(model.getQty()+" Kgs ");
                holder.cart_tvPrice.setText("Ksh :"+model.getPrice());

                Picasso.get().load(model.getPost_image()).placeholder(R.drawable.ic_photo_library_black_24dp).into(holder.cart_product_imageView);


                myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");
                myUsersDatabase.child(model.getPoster_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String name=dataSnapshot.child("name").getValue().toString();
                        final String phone=dataSnapshot.child("phone").getValue().toString();
                        String county=dataSnapshot.child("county").getValue().toString();
                        String subCounty=dataSnapshot.child("subcounty").getValue().toString();

                        holder.tvCounty.setText(county);
                        holder.tvSuCounty.setText(subCounty);
                        holder.tvProductUploader.setText("Sold by: "+name);

                        myCartUserDb = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Cart").child(userId).child(postid);
                        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                Query pQuery = ref.child("ENACTUS UOE").child("Cart").child(userId).orderByChild("post_id").equalTo(postid);

                                pQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot pSnapshot: dataSnapshot.getChildren()) {
                                            pSnapshot.getRef().removeValue();
                                            myCartUserDb.keepSynced(true);
                                            //MainActivity mainActivity = new MainActivity();
                                            //mainActivity.updateUsercart(userId);
                                            Snackbar.make(v, "You have removed "+pname+" from your cart. ", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, "onCancelled", databaseError.toException());
                                    }
                                });

                            }
                        });

                        holder.btnCall.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               //Toast.makeText(getContext(), "Call: "+phone, Toast.LENGTH_SHORT).show();

                                if (isCallAllowed()) {

                                    dial(phone);
                                }else {
                                    requestCallPermission();
                                }

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_cart,viewGroup,false);
                CartViewHolder viewHolder=new CartViewHolder(view);

                return viewHolder;
            }
        };



        cartList.setAdapter(adapter);
        adapter.startListening();
        adapter.notifyDataSetChanged();

    }

    private void dial(String phone) {

        Intent callIntent=new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phone));
        startActivity(callIntent);
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder{
        private ImageView cart_product_imageView;
        private TextView cart_tvName,cart_tvPrice,cart_tvQty,tvCounty,tvSuCounty,tvProductUploader;
        private Button btnCall,btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            cart_product_imageView=itemView.findViewById(R.id.cart_product_image_view);
            cart_tvName=itemView.findViewById(R.id.cart_name_tv);
            cart_tvPrice=itemView.findViewById(R.id.cart_price_tv);
            cart_tvQty=itemView.findViewById(R.id.cart_qty_tv);
            tvCounty=itemView.findViewById(R.id.cart_county);
            tvSuCounty=itemView.findViewById(R.id.cart_sub_county);
            btnCall=itemView.findViewById(R.id.btnCall);
            btnRemove=itemView.findViewById(R.id.btnRemove);
            tvProductUploader=itemView.findViewById(R.id.product_cart_uploader);



        }
    }
    private void requestCallPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.CALL_PHONE)){
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CALL_PHONE},CALL_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CALL_PERMISSION_CODE){

            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }else{
                Toast.makeText(getContext(),"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }
    private boolean isCallAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }
}
