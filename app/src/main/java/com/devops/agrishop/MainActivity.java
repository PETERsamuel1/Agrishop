package com.devops.agrishop;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;

import com.devops.agrishop.models.Chatroom;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {
    //private TextView tvUsername;
    private ViewPager mViewPager;
    private PagerViewAdapter pagerViewAdapter;
    private FirebaseAuth mAuth;
    private CircleImageView mProfileImage;
    private String userId;
    private DatabaseReference myUsersDatabase,myFavoriteDatabase,myCartDatabase,myProductsDatabase;
    private Toolbar mToolbar;

    private Long count;

    private int numProducts,productcount;

    private ShimmerFrameLayout shimmerFrameLayout;

    private BottomNavigationView bottomNavigationView;

    private static final String TAG = "MainActivity";

    public static boolean isActivityRunning;


    // private final int SPLASH_DISPLAY_LENGTH = 7500;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // updateUsercart(userId);

        initFCM();

        getPendingIntent();

        updateNumProducts();

        mAuth=FirebaseAuth.getInstance();
       // userId = mAuth.getCurrentUser().getUid();

        myProductsDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Products");
        myProductsDatabase.keepSynced(true);
        myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");
        myUsersDatabase.keepSynced(true);
       // profileLabel=findViewById(R.id.profileLabel);
        //usersLabel=findViewById(R.id.usersLabel);
       // notificationLabel=findViewById(R.id.notificationLabel);
        mViewPager=findViewById(R.id.mainViewPager);
        mProfileImage = findViewById(R.id.mainToolbarImageView);
        //tvUsername=findViewById(R.id.useraname);

        shimmerFrameLayout = findViewById(R.id.shimmerFrameLayout);
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        mViewPager.setVisibility(View.INVISIBLE);

        mToolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);/* Just put this line after setting your Action Bar*/

        // mToolbar.setNavigationOnClickListener(v -> showTopProductsBottomSheet());
    /*
        profileLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mViewPager.setCurrentItem(0);
            }
        });
        usersLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);

            }
        });
        notificationLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(2);

            }
        });

     */


        bottomNavigationView = findViewById(R.id.bottom_navigation);
        //bottomNavigationView.setItemIconTintList(null);
        //bottomNavigationView.getOrCreateBadge(R.id.action_notif).setNumber(2);
        BadgeDrawable badgeDrawable = bottomNavigationView.getBadge(R.id.action_notif);
        if (badgeDrawable == null){
            bottomNavigationView.getOrCreateBadge(R.id.action_notif).setNumber(2);
        }else{
            int previousValue = badgeDrawable.getNumber();
            badgeDrawable.setNumber(previousValue);
        }

             bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                 int id = item.getItemId();

                 //noinspection SimplifiableIfStatement

                 if (id == R.id.action_account) {

                     goToAccount();
                     return true;
                 }

                 if (id == R.id.action_notif) {

                     goToChat();
                    // mViewPager.setCurrentItem(2);
                     return true;
                 }

                 if (id == R.id.action_favorite){

                     mViewPager.setCurrentItem(2);
                     return true;
                 }

                 if (id == R.id.action_cart) {

                     mViewPager.setCurrentItem(1);
                     return true;
                 }

                 if (id == R.id.action_products) {

                     mViewPager.setCurrentItem(0);
                     updateNumProducts();
                     return true;
                 }
                 return false;
             });

        pagerViewAdapter=new PagerViewAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pagerViewAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position ,float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int i) {

                //changeTabs(i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
/*
        Handler handler = new Handler();
        int delay = 1000; //milliseconds

        handler.postDelayed(new Runnable(){
            public void run(){
                ProfileFragment profileFragment = new ProfileFragment();
                isInflated = profileFragment.inflated;
                if (isInflated == "inflated" && isInflated != null){
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    mViewPager.setVisibility(View.VISIBLE);
                }
                handler.postDelayed(this, delay);
            }
        }, delay);


        new Handler().postDelayed(() -> {
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            mViewPager.setVisibility(View.VISIBLE);

        }, SPLASH_DISPLAY_LENGTH);*/
    }

    private void getPendingIntent(){
        Log.d(TAG, "getPendingIntent: checking for pending intents.");

        Intent intent = getIntent();
        if(intent.hasExtra(getString(R.string.intent_chatroom))){
            Log.d(TAG, "getPendingIntent: pending intent detected.");

            //get the chatroom
            Chatroom chatroom = intent.getParcelableExtra(getString(R.string.intent_chatroom));
            //navigate to the chatoom
            Intent chatroomIntent = new Intent(MainActivity.this, ChatroomActivity.class);
            chatroomIntent.putExtra(getString(R.string.intent_chatroom), chatroom);
            startActivity(chatroomIntent);
        }
    }

    private void getNumProducts(){
        myProductsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    numProducts = (int) snapshot.getChildrenCount();

                    //updateNumProducts(numProducts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateNumProducts(){
        if (userId != null){
            myProductsDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        int numUserProducts = (int) snapshot.getChildrenCount();

                        myUsersDatabase
                                .child(userId)
                                .child("products_seen")
                                .setValue(String.valueOf(numUserProducts));

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void initFCM(){
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                sendRegistrationToServer(token);
            }
        });
    }

    private void sendRegistrationToServer(String token) {
        Log.d(TAG, "sendRegistrationToServer: sending token to server: " + token);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("ENACTUS UOE").child(getString(R.string.dbnode_users))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(getString(R.string.field_messaging_token))
                .setValue(token);
    }

    private void goToChat() {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        startActivity(intent);
    }

    private void goToAccount() {
        Intent intent = new Intent(MainActivity.this, AccountActivity.class);
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);
        startActivity(intent);
    }

    /**
     * showing bottom sheet dialog fragment
     * same layout is used in both dialog and dialog fragment

    public void showBottomSheetDialogFragment() {
        BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    public void showTopProductsBottomSheet(){
        TopProductsBottomSheet topproductsBottomSheetFragment = new TopProductsBottomSheet();
        topproductsBottomSheetFragment.show(getSupportFragmentManager(), topproductsBottomSheetFragment.getTag());
    }
    private void changeTabs(int position) {
        if (position==0)
        {


            profileLabel.setTextColor(getResources().getColor(R.color.textTabBright));
            profileLabel.setTextSize(22);
            usersLabel.setTextColor(getResources().getColor(R.color.textTabLight));
            usersLabel.setTextSize(16);
            notificationLabel.setTextColor(getResources().getColor(R.color.textTabLight));
            notificationLabel.setTextSize(16);

        }
        if (position==1)
        {


            profileLabel.setTextColor(getResources().getColor(R.color.textTabLight));
            profileLabel.setTextSize(16);
            usersLabel.setTextColor(getResources().getColor(R.color.textTabBright));
            usersLabel.setTextSize(22);
            notificationLabel.setTextColor(getResources().getColor(R.color.textTabLight));
            notificationLabel.setTextSize(16);

        }
        if (position==2)
        {


            profileLabel.setTextColor(getResources().getColor(R.color.textTabLight));
            profileLabel.setTextSize(16);
            usersLabel.setTextColor(getResources().getColor(R.color.textTabLight));
            usersLabel.setTextSize(16);
            notificationLabel.setTextColor(getResources().getColor(R.color.textTabBright));
            notificationLabel.setTextSize(22);

        }
    }
    */


    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivityRunning = true;
        FirebaseUser currentUser=mAuth.getCurrentUser();
       // GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (currentUser==null)
        {
           sendToLogin();
        }else {
            userId= currentUser.getUid();
            updateUsercart(userId);
            updateUserFavorite(userId);
            verifyUserDetails();
            getNumProducts();
            getUserProductsSeen(userId);
        }

    }

    private void getUserProductsSeen(String userId) {
        myUsersDatabase
                .child(userId)
                .child("products_seen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    String pcount = snapshot.getValue().toString();

                    productcount = Integer.parseInt(pcount);
                    int i = numProducts-productcount;

                    if (i < 1){

                        bottomNavigationView.removeBadge(R.id.action_products);
                    }else{
                        // BadgeDrawable badgeCartDrawable = bottomNavigationView.getBadge(R.id.action_cart);
                        bottomNavigationView.getOrCreateBadge(R.id.action_products);
                        bottomNavigationView.getOrCreateBadge(R.id.action_products).setBackgroundColor(Color.RED);
                        // badgeCartDrawable.setBackgroundColor(Color.GREEN);

                    /*
                        Drawable drawable = AppCompatResources.getDrawable(MainActivity.this,R.drawable.ic_add_shopping_cart_black_24dp);
                        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTint(wrappedDrawable, Color.YELLOW);
                     */


                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void updateUserFavorite(String userId) {
        if (userId != null) {
            myFavoriteDatabase = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Favorite").child(userId);
            myFavoriteDatabase.keepSynced(true);
            myFavoriteDatabase.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    count = dataSnapshot.getChildrenCount();
                    int i = count.intValue();
                    if (i < 1){

                        bottomNavigationView.removeBadge(R.id.action_favorite);
                    }else{
                        // BadgeDrawable badgeCartDrawable = bottomNavigationView.getBadge(R.id.action_cart);
                        bottomNavigationView.getOrCreateBadge(R.id.action_favorite).setNumber(i);
                        bottomNavigationView.getOrCreateBadge(R.id.action_favorite).setBackgroundColor(Color.RED);
                        // badgeCartDrawable.setBackgroundColor(Color.GREEN);
                        /*
                        Drawable drawable = AppCompatResources.getDrawable(MainActivity.this,R.drawable.ic_add_shopping_cart_black_24dp);
                        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTint(wrappedDrawable, Color.YELLOW);

                         */

                    }

                }
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    public void updateUsercart(String userId) {
        if (userId != null) {
            myCartDatabase = FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Cart").child(userId);
            myCartDatabase.keepSynced(true);
            myCartDatabase.addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    count = dataSnapshot.getChildrenCount();
                    int i = count.intValue();
                    if (i < 1){

                        bottomNavigationView.removeBadge(R.id.action_cart);
                    }else{
                       // BadgeDrawable badgeCartDrawable = bottomNavigationView.getBadge(R.id.action_cart);
                        bottomNavigationView.getOrCreateBadge(R.id.action_cart).setNumber(i);
                        bottomNavigationView.getOrCreateBadge(R.id.action_cart).setBackgroundColor(Color.RED);
                        // badgeCartDrawable.setBackgroundColor(Color.GREEN);
                        /*
                        Drawable drawable = AppCompatResources.getDrawable(MainActivity.this,R.drawable.ic_add_shopping_cart_black_24dp);
                        Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
                        DrawableCompat.setTint(wrappedDrawable, Color.YELLOW);

                         */

                    }

                }
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void verifyUserDetails() {
        String userId=mAuth.getCurrentUser().getUid();
        myUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists())
                {
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    mViewPager.setVisibility(View.VISIBLE);
                    String retrieveProfileImage = dataSnapshot.child("profile_image").getValue().toString();
                    Picasso.get().load(retrieveProfileImage).resize(40,40).placeholder(R.drawable.profile_thumbnail).into(mProfileImage);

                    /*
                    String name=dataSnapshot.child("name").getValue().toString();
                    tvUsername.setText("You are signed in as:  "+name.toUpperCase()+ "  Sign out ? ");
                    tvUsername.setOnClickListener(v -> {
                        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Confirm Action");
                        builder.setMessage("Do you want to Sign out?");
                        builder.setPositiveButton("Accept", (dialog, which) -> {

                            mAuth.signOut();
                            sendToLogin();
                        });
                        builder.setNegativeButton("Later",null);
                        builder.show();

                    });*/

                }else {
                    startActivity(new Intent(   MainActivity.this,UserDetailsActivity.class));
                finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendToLogin() {
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        finish();
    }
}
