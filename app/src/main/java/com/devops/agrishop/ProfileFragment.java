package com.devops.agrishop;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devops.agrishop.utility.HorizontalSpacingDecorator;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {
    private static final int GALLERY_REQUEST_CODE = 2345;
    private FloatingActionButton btnUpdate;
    private CircleImageView postDialogClose;
    private EditText productName,productCategory,productQuantity,productPrice;
    private RecyclerView productsList,mostViewedProductList;
    private FirebaseAuth mAuth;
    private String userId;

    private View v;
    private Dialog d;
    private Uri imageUri=null;
    private ImageView profileImage;
    private DatabaseReference myProductsDatabase;
    private StorageReference mStorageReference;
    private ProgressDialog pd;
    private FirebaseRecyclerAdapter mAdapter;
    private int position =0;
    private boolean end;


    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_profile, container, false);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mAuth=FirebaseAuth.getInstance();
        myProductsDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Products");

        mStorageReference= FirebaseStorage.getInstance().getReference().child("ENACTUS UOE").child("Product Images");

        btnUpdate=v.findViewById(R.id.fb);
        d=new Dialog(getContext());
        d.setContentView(R.layout.post_dialog);

        profileImage=d.findViewById(R.id.productImage);
        productName=d.findViewById(R.id.product_title);
        productCategory=d.findViewById(R.id.product_category);
        productQuantity=d.findViewById(R.id.product_quantity);
        productPrice=d.findViewById(R.id.product_price);

        productsList=v.findViewById(R.id.productsList);
        mostViewedProductList = v.findViewById(R.id.mostViewedProductList);

        LinearLayoutManager layoutManager= new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        layoutManager.setReverseLayout(false);
        //layoutManager.setStackFromEnd(true);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new AutoScrollTask(), 2000, 5000);

        mostViewedProductList.setLayoutManager(layoutManager);

        GridLayoutManager gridLayoutManager=new GridLayoutManager(getContext(),2);
        productsList.setLayoutManager(gridLayoutManager);
        userId=mAuth.getCurrentUser().getUid();
        pd=new ProgressDialog(getContext());
        pd.setTitle("Uploading Product");
        pd.setMessage("Please wait...");
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bringPostDialog();
            }
        });

        return v;
    }

    private void bringPostDialog() {
        Button btnPost=d.findViewById(R.id.btnUpload);
        postDialogClose = d.findViewById(R.id.postDialogClose);

        postDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,GALLERY_REQUEST_CODE);
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name=productName.getText().toString().trim().trim();
                String category=productCategory.getText().toString().trim();
                String quantity=productQuantity.getText().toString().trim();
                String price=productPrice.getText().toString().trim();

                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(category) && !TextUtils.isEmpty(quantity) &&!TextUtils.isEmpty(price))
                {

                    if (imageUri!=null)
                    {

                        //toast("Data is ready");
                        pd.show();
                        postData(name,category,quantity,price,imageUri);
                    }else {
                        toast("Tap the avatar to select product Image...");
                    }
                }else {
                    toast("Check your Product Inputs...some fileds are blank...");
                }
            }
        });
        d.show();
    }

    private void postData(final String name, final String category, final String quantity, final String price, final Uri myUri) {

       final StorageReference filePath=mStorageReference.child(myUri.getLastPathSegment()+".jpg");
       filePath.putFile(myUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
           @Override
           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

               if (task.isSuccessful())
               {
                   filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                       @Override
                       public void onSuccess(Uri uri) {
                           String downloadLink=uri.toString();

                           DateFormat dateFormat=new SimpleDateFormat("HH:mm:ss ");
                           Date date=new Date();
                           String time=dateFormat.format(date);
                           String productKey=myProductsDatabase.push().getKey();
                           DatabaseReference newProduct=myProductsDatabase.child(productKey);
                           HashMap<String,Object> myMap=new HashMap<>();
                           myMap.put("post_id",productKey);
                           myMap.put("product_name",name);
                           myMap.put("product_category",category);
                           myMap.put("product_quantity",quantity);
                           myMap.put("product_price",price);
                           myMap.put("post_time",time);
                           myMap.put("product_poster",userId);
                           myMap.put("product_views","0");
                           myMap.put("product_likes","0");

                           myMap.put("post_image",downloadLink);

                           newProduct.updateChildren(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                               @Override
                               public void onComplete(@NonNull Task<Void> task) {
                                   if (task.isSuccessful())
                                   {
                                       toast("Uploaded successfully");
                                       pd.dismiss();
                                       d.dismiss();
                                       productName.setText("");
                                       productCategory.setText("");
                                       productQuantity.setText("");
                                       productPrice.setText("");
                                       //imageUri=null;
                                       profileImage.setImageURI(null);

                                   }else {
                                       pd.dismiss();

                                       toast(task.getException().getMessage());
                                   }
                               }
                           });
                       }
                   });
               }else {
                   toast(task.getException().getMessage());
               }
           }
       });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (GALLERY_REQUEST_CODE==requestCode && resultCode==RESULT_OK)
        {
            imageUri=data.getData();
            profileImage.setImageURI(imageUri);
        }


    }

    private void toast(String s) {
        Toast.makeText(getContext(), "Message: "+s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();

        myProductsDatabase.keepSynced(true);

        getMostViewedProdects();

        FirebaseRecyclerOptions<Products> options=new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(myProductsDatabase,Products.class)
                .build();

        final FirebaseRecyclerAdapter<Products,productsViewHolder> adapter=new FirebaseRecyclerAdapter<Products, productsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull productsViewHolder holder, int position, @NonNull final Products model) {

                holder.tvName.setText(model.getProduct_name());
                holder.tvQty.setText(model.getProduct_quantity());
                holder.tvPrice.setText("Ksh "+model.getProduct_price());
                Picasso.get().load(model.getPost_image()).resize(180,150).placeholder(R.drawable.ic_photo_library_black_24dp).into(holder.product_imageView);

                myProductsDatabase.child(model.getPost_id()).child("product_ratings").child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String rating = dataSnapshot.child("rating").getValue().toString();
                            float rate = Float.parseFloat(rating);
                            holder.productRatingbar.setRating(rate);
                            //holder.txtProductRating.setText("("+rate+")");
                            holder.txtUserProductRating.setText(""+rate);
                        }else{
                            holder.txtUserProductRating.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                myProductsDatabase.child(model.getPost_id()).child("product_ratings").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            long count = snapshot.getChildrenCount();
                            holder.txtProductRating.setText("("+count+")");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent in=new Intent(getContext(),DetailActivity.class);
                        in.putExtra("name",model.getProduct_name());
                        in.putExtra("qty",model.getProduct_quantity());
                        in.putExtra("price",model.getProduct_price());
                        in.putExtra("image",model.getPost_image());
                        in.putExtra("poster",model.getProduct_poster());
                        in.putExtra("time",model.getPost_time());
                        in.putExtra("postId",model.getPost_id());
                        in.putExtra("category",model.getProduct_category());
                        startActivity(in);

                    }
                });

            }

            @NonNull
            @Override
            public productsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_product,viewGroup,false);
                productsViewHolder viewHolder=new productsViewHolder(view);


                return viewHolder;
            }
        };

        productsList.setAdapter(adapter);
        adapter.startListening();

        productsList.smoothScrollToPosition(adapter.getItemCount()+1);

    }

    private FirebaseRecyclerAdapter<Products, mosttopproductsViewHolder> getMostViewedProdects() {

        FirebaseRecyclerOptions<Products> options=new FirebaseRecyclerOptions.Builder<Products>()
                .setQuery(myProductsDatabase,Products.class)
                .build();

        final FirebaseRecyclerAdapter<Products,mosttopproductsViewHolder> adapter = new FirebaseRecyclerAdapter<Products, mosttopproductsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull mosttopproductsViewHolder mosttopproductsViewHolder, int i, @NonNull Products mosttopproducts) {
                mosttopproductsViewHolder.toptvName.setText(mosttopproducts.getProduct_name());
                Picasso.get().load(mosttopproducts.getPost_image()).resize(80,80).placeholder(R.drawable.ic_photo_library_black_24dp).into(mosttopproductsViewHolder.top_product_imageView);

                mosttopproductsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent in=new Intent(getContext(),DetailActivity.class);
                        in.putExtra("name",mosttopproducts.getProduct_name());
                        in.putExtra("qty",mosttopproducts.getProduct_quantity());
                        in.putExtra("price",mosttopproducts.getProduct_price());
                        in.putExtra("image",mosttopproducts.getPost_image());
                        in.putExtra("poster",mosttopproducts.getProduct_poster());
                        in.putExtra("time",mosttopproducts.getPost_time());
                        in.putExtra("postId",mosttopproducts.getPost_id());
                        in.putExtra("category",mosttopproducts.getProduct_category());
                        startActivity(in);

                    }
                });

            }

            @NonNull
            @Override
            public mosttopproductsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view=LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_most_viewed_product,viewGroup,false);
                mosttopproductsViewHolder viewHolder=new mosttopproductsViewHolder(view);

                return viewHolder;
            }
        };

        mAdapter = adapter;
        mostViewedProductList.addItemDecoration(new HorizontalSpacingDecorator(5));
        mostViewedProductList.setAdapter(adapter);
        adapter.startListening();

        //mostViewedProductList.smoothScrollToPosition(adapter.getItemCount()+1);

        return mAdapter;
    }

    private class AutoScrollTask extends TimerTask {
        @Override
        public void run() {
            if(position == mAdapter.getItemCount() -1){
                end = true;
            } else if (position == 0) {
                end = false;
            }
            if(!end){
                position++;
            } else {
                position--;
            }
            mostViewedProductList.smoothScrollToPosition(position);
        }
    }

    public static class mosttopproductsViewHolder extends RecyclerView.ViewHolder{
        private ImageView top_product_imageView;
        private TextView toptvName;


        public mosttopproductsViewHolder(@NonNull View itemView) {
            super(itemView);
            top_product_imageView=itemView.findViewById(R.id.imgSingleMostViewed);
            toptvName=itemView.findViewById(R.id.txtSingleMostViewed);
        }
    }

    public static class productsViewHolder extends RecyclerView.ViewHolder{
    private ImageView product_imageView;
    private TextView tvName,tvPrice,tvQty,txtProductRating,txtUserProductRating;
    private RatingBar productRatingbar;

        public productsViewHolder(@NonNull View itemView) {
            super(itemView);
            product_imageView=itemView.findViewById(R.id.product_image_view);
            tvName=itemView.findViewById(R.id.name_tv);
            tvPrice=itemView.findViewById(R.id.price_tv);
            tvQty=itemView.findViewById(R.id.qty_tv);
            txtUserProductRating=itemView.findViewById(R.id.txtUserProductRating);
            txtProductRating=itemView.findViewById(R.id.txtProductRating);
            productRatingbar=itemView.findViewById(R.id.productRatingbar);

        }
    }

}