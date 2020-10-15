package com.devops.agrishop;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TopProductsBottomSheet extends BottomSheetDialogFragment {

    View v;
    RecyclerView topProductsList;
    private FirebaseAuth mAuth;
    private String userId;
    private DatabaseReference myProductsDatabase;

    public TopProductsBottomSheet(){
        //Empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v= inflater.inflate(R.layout.fragment_top_products, container, false);
        topProductsList=v.findViewById(R.id.topList);

        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();
        myProductsDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Products");

        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        layoutManager.setReverseLayout(false);
        //layoutManager.setStackFromEnd(true);

        topProductsList.setLayoutManager(layoutManager);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        myProductsDatabase.keepSynced(true);

        FirebaseRecyclerOptions<Products> options=new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(myProductsDatabase,Products.class)
                .build();

        final FirebaseRecyclerAdapter<Products,topproductsViewHolder> adapter = new FirebaseRecyclerAdapter<Products, topproductsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull topproductsViewHolder topproductsViewHolder, int i, @NonNull Products products) {
                topproductsViewHolder.toptvName.setText(products.getProduct_name());
                topproductsViewHolder.toptvQty.setText(products.getProduct_quantity()+" Kgs");
                topproductsViewHolder.toptvPrice.setText("Ksh:"+products.getProduct_price());
                Picasso.get().load(products.getPost_image()).resize(96,96).placeholder(R.drawable.ic_photo_library_black_24dp).into(topproductsViewHolder.top_product_imageView);

                topproductsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent in=new Intent(getContext(),DetailActivity.class);
                        in.putExtra("name",products.getProduct_name());
                        in.putExtra("qty",products.getProduct_quantity());
                        in.putExtra("price",products.getProduct_price());
                        in.putExtra("image",products.getPost_image());
                        in.putExtra("poster",products.getProduct_poster());
                        in.putExtra("time",products.getPost_time());
                        in.putExtra("postId",products.getPost_id());
                        in.putExtra("category",products.getProduct_category());
                        startActivity(in);

                    }
                });

            }

            @NonNull
            @Override
            public topproductsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_top_product,viewGroup,false);
                topproductsViewHolder viewHolder=new topproductsViewHolder(view);

                return viewHolder;
            }
        };

        topProductsList.setAdapter(adapter);
        adapter.startListening();

        topProductsList.smoothScrollToPosition(adapter.getItemCount()+1);
    }

    public static class topproductsViewHolder extends RecyclerView.ViewHolder{
        private CircleImageView top_product_imageView;
        private TextView toptvName,toptvPrice,toptvQty;


        public topproductsViewHolder(@NonNull View itemView) {
            super(itemView);
            top_product_imageView=itemView.findViewById(R.id.top_product_image_view);
            toptvName=itemView.findViewById(R.id.top_name_tv);
            toptvPrice=itemView.findViewById(R.id.top_price_tv);
            toptvQty=itemView.findViewById(R.id.top_qty_tv);

        }
    }
}
