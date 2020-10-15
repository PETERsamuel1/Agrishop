package com.devops.agrishop;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devops.agrishop.utility.VerticalSpacingDecorator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FavoriteFragment extends Fragment {

    private View v;
    private RecyclerView favoriteList;
    private DatabaseReference myFavoriteDatabase,myFavoriteUserDb;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference myUsersDatabase,myProductsDatabase;
    private String TAG = "Userfavorite";

    public FavoriteFragment(){
        //empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_favorite, container, false);

        favoriteList = v.findViewById(R.id.favoriteList);
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        myFavoriteDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Favorite").child(userId);
        myProductsDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Products");

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        favoriteList.setLayoutManager(linearLayoutManager);

        return  v;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Favorite> foptions=new FirebaseRecyclerOptions.Builder<Favorite>()
                .setQuery(myFavoriteDatabase,Favorite.class)
                .build();

        FirebaseRecyclerAdapter<Favorite,FavoriteViewHolder> fadapter=new FirebaseRecyclerAdapter<Favorite, FavoriteViewHolder>(foptions) {
            @Override
            protected void onBindViewHolder(@NonNull final FavoriteViewHolder fholder, int position, @NonNull Favorite fmodel) {

                String postid = fmodel.getPost_id();
                String pname = fmodel.getName();
                fholder.txtFavoriteProductName.setText(fmodel.getName());

                Picasso.get().load(fmodel.getPost_image()).placeholder(R.drawable.ic_android).into(fholder.imgFavoriteProductImage);


                myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");
                myUsersDatabase.child(fmodel.getPoster_id()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String name=dataSnapshot.child("name").getValue().toString();
                        final String phone=dataSnapshot.child("phone").getValue().toString();
                        String county=dataSnapshot.child("county").getValue().toString();
                        String subCounty=dataSnapshot.child("subcounty").getValue().toString();

                       myFavoriteUserDb = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Favorite").child(userId).child(postid);
                       fholder.favoriteProductRemove.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fholder.mProgressbar.setVisibility(View.VISIBLE);
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                Query pQuery = ref.child("ENACTUS UOE").child("Favorite").child(userId).orderByChild("post_id").equalTo(postid);

                                pQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        for (DataSnapshot pSnapshot: dataSnapshot.getChildren()) {
                                            pSnapshot.getRef().removeValue();
                                            myFavoriteUserDb.keepSynced(true);


                                            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference();
                                            Query query = dbref.child("ENACTUS UOE").child("Products").child(postid).child("product_favorites").orderByChild(userId).equalTo(userId);

                                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    snapshot.getRef().removeValue();
                                                    myProductsDatabase.keepSynced(true);

                                                    if(fholder.mProgressbar.getVisibility() == View.VISIBLE){
                                                        fholder.mProgressbar.setVisibility(View.INVISIBLE);
                                                    }
                                                    Snackbar.make(v, "You have removed "+pname+" from your favorites. ", Snackbar.LENGTH_LONG)
                                                            .setAction("Action", null).show();
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Log.e(TAG, "onCancelled", databaseError.toException());
                                    }
                                });

                            }
                        });

                       /*
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

                        */

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




            }

            @NonNull
            @Override
            public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_favorite_listitem,viewGroup,false);
                FavoriteViewHolder viewHolder=new FavoriteViewHolder(view);

                return viewHolder;
            }
        };



        favoriteList.setAdapter(fadapter);
        favoriteList.addItemDecoration(new VerticalSpacingDecorator(10));

        fadapter.startListening();
        fadapter.notifyDataSetChanged();
    }

    public static class FavoriteViewHolder extends RecyclerView.ViewHolder{
        private ImageView imgFavoriteProductImage;
        private TextView txtFavoriteProductName,favoriteProductRemove;
        private ProgressBar mProgressbar;

        public FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);

            mProgressbar = itemView.findViewById(R.id.favoriteprogressBar);
            imgFavoriteProductImage=itemView.findViewById(R.id.imgFavoriteProductImage);
            txtFavoriteProductName=itemView.findViewById(R.id.txtFavoriteProductName);
            favoriteProductRemove = itemView.findViewById(R.id.favoriteProductRemove);

        }
    }

}
