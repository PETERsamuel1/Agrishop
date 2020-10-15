package com.devops.agrishop;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class DetailActivity extends AppCompatActivity {

    private String name,qty,price,image,poster,time,postId,category;
    private TextView tvDetailTitle,tvDetailViews,tvDetailLikes,tvDetailQty,tvDetailShare,tvDetailCategory,tvDetailPoster,tvDetailPrice,tvPosterPhone,tvPosterCounty,tvPosterSubcounty,tvDetailFavorite;
    private ImageView ivImage;
    private DatabaseReference myUsersDatabase;
    private FirebaseAuth mAuth;
    private String userId;
    private Bitmap bitmapImage = null;
    private DatabaseReference myProductsDatabase,myCartDatabase,myFavoriteDatabase;
    private RatingBar ratingBar;
    private Button btnRating;

    public static boolean isActivityRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ratingBar = findViewById(R.id.ratingBar);

        tvDetailShare = findViewById(R.id.tvDetailShare);
        tvDetailTitle=findViewById(R.id.tvDetailTitle);
        tvDetailQty=findViewById(R.id.tvDetailQty);
        tvDetailCategory=findViewById(R.id.tvDetailCategory);
        tvDetailPoster=findViewById(R.id.tvDetailPoster);
        tvDetailPrice=findViewById(R.id.tvDetailPrice);
        tvDetailFavorite = findViewById(R.id.tvDetailFavorite);

        tvPosterPhone=findViewById(R.id.tvDetailPosterPhone);
        tvPosterCounty=findViewById(R.id.tvDetailPosterCounty);
        tvPosterSubcounty=findViewById(R.id.tvDetailPosterSubcounty);

        tvDetailViews=findViewById(R.id.tvDetailViews);
        tvDetailLikes=findViewById(R.id.tvDetailLikes);
        mAuth=FirebaseAuth.getInstance();

        myCartDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Cart");
        myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");
        myProductsDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Products");
        myProductsDatabase.keepSynced(true);
        myFavoriteDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Favorite");


        userId=mAuth.getCurrentUser().getUid();
        ivImage=findViewById(R.id.ivDetailImage);

        name=getIntent().getStringExtra("name");
        qty=getIntent().getStringExtra("qty");
        price=getIntent().getStringExtra("price");
        image=getIntent().getStringExtra("image");
        poster=getIntent().getStringExtra("poster");
        time=getIntent().getStringExtra("time");
        postId=getIntent().getStringExtra("postId");
        category=getIntent().getStringExtra("category");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(name);
        tvDetailTitle.setText(""+name);
        tvDetailQty.setText("Qty: "+qty+" Kgs ");
        tvDetailPrice.setText("Ksh: "+price+" /Kg");
        tvDetailCategory.setText(category);
        Uri imageUri = Uri.parse(image);

        Picasso.get().load(image).placeholder(R.drawable.ic_photo_library_black_24dp).into(ivImage);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> sendProductToCart(view,name,qty,price,poster,postId));

        tvDetailShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //shareOnWhatsapp(DetailActivity.this,name,imageUri);
            }
        });

        btnRating = findViewById(R.id.btnRating);
        btnRating.setOnClickListener(v -> setupProductRating(v,postId));
        tvDetailFavorite.setOnClickListener(view->sendProductToFavorite(view,name,qty,price,poster,postId));


        //share product

         setupUploader();
         setUpViewsandLikes();
         setupRating();

         tvDetailLikes.setOnClickListener(v -> {
             myProductsDatabase.child(postId).child("product_likes").child(userId).setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                 @Override
                 public void onComplete(@NonNull Task<Void> task) {
                     if (task.isSuccessful()) {
                         Toast.makeText(DetailActivity.this, "You Liked this product...", Toast.LENGTH_SHORT).show();
                     }
                 }
             });
         });

    }

    private void sendProductToFavorite(final View view, String name, String qty, String price, String poster, String postId) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query queryToGetData = dbRef.child("ENACTUS UOE").child("Favorite").child(userId)
                .orderByChild("post_id").equalTo(postId);
        queryToGetData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    String newFavoriteID=myFavoriteDatabase.child(userId).push().getKey();
                    DatabaseReference newFavorite=myFavoriteDatabase.child(userId).child(newFavoriteID);
                    DateFormat dateFormat=new SimpleDateFormat("HH:mm:ss ");
                    Date date=new Date();
                    String tim=dateFormat.format(date);
                    HashMap<String,Object> myMap=new HashMap<>();
                    myMap.put("name",name);
                    myMap.put("qty",qty);
                    myMap.put("price",price);
                    myMap.put("poster_id",poster);
                    myMap.put("post_id",postId);
                    myMap.put("time",tim);
                    myMap.put("post_image",image);

                    newFavorite.updateChildren(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                myProductsDatabase.child(postId).child("product_favorites").child(userId).setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Snackbar.make(view, "You have added "+name+" to your favorite. ", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();                                        }
                                    }
                                });
                            }
                        }
                    });
                }else{
                    Snackbar.make(view, "You have already added "+name+" to your favorite. ", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setupProductRating(final View v,String postId) {
        String rating = String.valueOf(ratingBar.getRating());
        if (rating != null){
            myProductsDatabase.child(postId).child("product_ratings").child(userId).child("rating").setValue(rating).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Snackbar.make(v, "Thank you for rating this product. ", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();                    }
                }
            });
        }
    }

    /*
    public static void shareOnWhatsapp(AppCompatActivity appCompatActivity, String textBody, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");
        intent.putExtra(Intent.EXTRA_TEXT,!TextUtils.isEmpty(textBody) ? textBody : "");

        if (fileUri != null) {

            try {
                Bitmap  mBitmap = MediaStore.Images.Media.getBitmap(appCompatActivity.getContentResolver(), fileUri);

                intent.putExtra(Intent.EXTRA_STREAM, mBitmap);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setType("image/*");

                try {
                    appCompatActivity.startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    ex.printStackTrace();
                    showWarningDialog(appCompatActivity, appCompatActivity.getString(R.string.error_activity_not_found));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

     */

    private static void showWarningDialog(Context context, String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton(context.getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(true)
                .create().show();
    }

    public void shareProduct() {
        Picasso.get().load(image).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                bitmapImage = bitmap;
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, name);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapImage);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share product..."));
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }



    private void sendProductToCart(final View view, final String name, String qty, String price, String poster, String postId) {

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        Query queryToGetData = dbRef.child("ENACTUS UOE").child("Cart").child(userId)
                .orderByChild("post_id").equalTo(postId);
        queryToGetData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    String newCartID=myCartDatabase.child(userId).push().getKey();
                    DatabaseReference newCart=myCartDatabase.child(userId).child(newCartID);
                    DateFormat dateFormat=new SimpleDateFormat("HH:mm:ss ");
                    Date date=new Date();
                    String tim=dateFormat.format(date);
                    HashMap<String,Object> myMap=new HashMap<>();
                    myMap.put("name",name);
                    myMap.put("qty",qty);
                    myMap.put("price",price);
                    myMap.put("poster_id",poster);
                    myMap.put("post_id",postId);
                    myMap.put("time",tim);
                    myMap.put("post_image",image);

                    newCart.updateChildren(myMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                Snackbar.make(view, "You have added "+name+" to your cart. ", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    });
                }else{
                    Snackbar.make(view, "You have already added "+name+" to your cart. ", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void setupRating(){
        myProductsDatabase.child(postId).child("product_ratings").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               if (dataSnapshot.exists()){
                   String rating = dataSnapshot.child("rating").getValue().toString();
                   float rate = Float.parseFloat(rating);
                   ratingBar.setRating(rate);
               }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setTextViewDrawableColor(TextView textView, int color) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(new PorterDuffColorFilter(getColor(color), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    private void setUpViewsandLikes() {

        myProductsDatabase.child(postId).child("product_favorites").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    tvDetailFavorite.setText("Your Favorite");
                    tvDetailFavorite.setTextColor(getColor(R.color.colorOrange));
                    setTextViewDrawableColor(tvDetailFavorite, R.color.colorOrange);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myProductsDatabase.child(postId).child("product_views").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    tvDetailViews.setTextColor(getColor(R.color.colorPrimary));
                    setTextViewDrawableColor(tvDetailViews, R.color.colorPrimary);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myProductsDatabase.child(postId).child("product_likes").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    tvDetailLikes.setTextColor(getColor(R.color.colorBlue));
                    setTextViewDrawableColor(tvDetailLikes, R.color.colorBlue);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        myProductsDatabase.child(postId).child("product_views").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    long views=dataSnapshot.getChildrenCount();
                    tvDetailViews.setText(" "+views+" Views");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        myProductsDatabase.child(postId).child("product_likes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    long likes=dataSnapshot.getChildrenCount();
                    tvDetailLikes.setText(" "+likes+" Likes");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupUploader() {
        myUsersDatabase.child(poster).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {

                    String name=dataSnapshot.child("name").getValue().toString();
                    String phon=dataSnapshot.child("phone").getValue().toString();
                    String coun=dataSnapshot.child("county").getValue().toString();
                    String subcou=dataSnapshot.child("subcounty").getValue().toString();
                    String scalet=dataSnapshot.child("type_scale").getValue().toString();

                    tvDetailPoster.setText("Provided by "+name);
                    tvPosterPhone.setText("Provider Contact "+phon);
                    tvPosterCounty.setText("Area County name "+coun);
                    tvPosterSubcounty.setText("Are Sub-County "+subcou);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
        myProductsDatabase.child(postId).child("product_views").child(userId).setValue("1").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                   // Toast.makeText(DetailActivity.this, "You Liked this product...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
