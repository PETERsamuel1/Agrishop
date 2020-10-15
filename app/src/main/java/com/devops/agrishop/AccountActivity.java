package com.devops.agrishop;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.devops.agrishop.utility.FilePaths;
import com.devops.agrishop.utility.UniversalImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class AccountActivity extends AppCompatActivity implements ChangePhotoDialog.OnPhotoReceivedListener{

    private static final String TAG = "AccountActivity";
    private TextView txtName,txtPhone,txtCounty,txtSubCounty,txtFarmingType,txtScaleType,txtEmail,txtRole;
    private FirebaseAuth mAuth;
    private DatabaseReference myUsersDatabase;
    private ImageView imageView,imgProfRole, imgProfName,imgProfPhoneno;
    private CircleImageView profImage;

    //vars
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;

    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;

    private Button mSave;
    private ProgressBar mProgressBar;
    private String role_id, name, pno;

    private String isAdmin = "1";
    public static boolean isActivityRunning;

    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d(TAG, "getImagePath: got the image uri: " + mSelectedImageUri);

            ImageLoader.getInstance().displayImage(imagePath.toString(), profImage);
        }
    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d(TAG, "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);

            profImage.setImageBitmap(bitmap);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = findViewById(R.id.accounttoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtName = findViewById(R.id.accountName);
        txtPhone = findViewById(R.id.phoneNumber);
        txtCounty = findViewById(R.id.county);
        txtSubCounty = findViewById(R.id.subCounty);
        txtFarmingType = findViewById(R.id.farmingType);
        txtScaleType = findViewById(R.id.scaleType);
        txtEmail = findViewById(R.id.profEmail);
        txtRole = findViewById(R.id.accountRole);
        imageView = findViewById(R.id.imgProfEmail);
        imgProfRole = findViewById(R.id.imgProfRole);
        profImage = findViewById(R.id.profile_image);
        imgProfName = findViewById(R.id.imgProfName);
        imgProfPhoneno = findViewById(R.id.imgProfPhoneNumber);
        mSave = findViewById(R.id.btnSave);
        mProgressBar = findViewById(R.id.accountprogressBar);

        UniversalImageLoader universalImageLoader = new UniversalImageLoader(AccountActivity.this);
        ImageLoaderConfiguration config = universalImageLoader.getConfig();
        ImageLoader.getInstance().init(config);

        mAuth=FirebaseAuth.getInstance();
        myUsersDatabase= FirebaseDatabase.getInstance().getReference().child("ENACTUS UOE").child("Users");

        verifyStoragePermissions();


        String userId=mAuth.getCurrentUser().getUid();
        String email= mAuth.getCurrentUser().getEmail();
        myUsersDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    role_id = dataSnapshot.child("role_id").getValue().toString();
                    name = dataSnapshot.child("name").getValue().toString();
                    pno = dataSnapshot.child("phone").getValue().toString();
                    String cnty = dataSnapshot.child("county").getValue().toString();
                    String subcnty = dataSnapshot.child("subcounty").getValue().toString();
                    String tscale = dataSnapshot.child("type_scale").getValue().toString();
                    String ttype = dataSnapshot.child("type_farming").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("profile_image").getValue().toString();

                    getSupportActionBar().setTitle(name.toUpperCase());
                    if (role_id != null && role_id.equals(isAdmin))
                    {
                        txtRole.setText("Admin");
                    }else{
                        txtRole.setText("Farmer");
                    }
                    txtName.setText(name);
                    txtPhone.setText(pno);
                    txtCounty.setText(cnty);
                    txtSubCounty.setText(subcnty);
                    txtFarmingType.setText(ttype);
                    txtScaleType.setText(tscale);
                    txtEmail.setText(email);
                    Picasso.get().load(retrieveProfileImage).resize(100,100).placeholder(R.drawable.ic_android).into(profImage);

                }else {
                    startActivity(new Intent(   AccountActivity.this,UserDetailsActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imgProfName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (name != null)
                changeProfileNameBottomSheet();
            }
        });

        imgProfPhoneno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pno != null){
                    changeProfilePhoneNumberBottomSheet();
                }
            }
        });

        imgProfRole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (role_id != null && role_id.equals(isAdmin))
                {
                    Intent intent = new Intent(AccountActivity.this, AdminActivity.class);
                    startActivity(intent);
                }
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedImageUri != null){
                    uploadNewPhoto(mSelectedImageUri);
                }else if(mSelectedImageBitmap  != null){
                    uploadNewPhoto(mSelectedImageBitmap);
                }

            }
        });

        profImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStoragePermissions){
                    ChangePhotoDialog dialog = new ChangePhotoDialog();
                    dialog.show(getSupportFragmentManager(), getString(R.string.dialog_change_photo));
                }else{
                    verifyStoragePermissions();
                }

            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle("Confirm Action");
                builder.setMessage("Do you want to Sign out?");
                builder.setPositiveButton("Accept", (dialog, which) -> {

                    mAuth.signOut();
                    sendToLogin();
                });
                builder.setNegativeButton("Later",null);
                builder.show();
            }
        });
    }

    public void changeProfileNameBottomSheet(){
        ChangeProfileNameFragment changeProfileNameFragment = new ChangeProfileNameFragment();
        changeProfileNameFragment.show(getSupportFragmentManager(), changeProfileNameFragment.getTag());
    }

    public void changeProfilePhoneNumberBottomSheet() {
        ChangePhoneNumberFragment changePhoneNumberFragment = new ChangePhoneNumberFragment();
        changePhoneNumberFragment.show(getSupportFragmentManager(), changePhoneNumberFragment.getTag());
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageUri***
     * @param imageUri
     */
    public void uploadNewPhoto(Uri imageUri){
        /*
            upload a new profile photo to firebase storage
         */
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageBitmap***
     * @param imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        /*
            upload a new profile photo to firebase storage
         */
        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog();
            Toast.makeText(AccountActivity.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(AccountActivity.this.getContentResolver(), params[0]);
                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(AccountActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            hideDialog();
            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask(){
        showDialog();
        FilePaths filePaths = new FilePaths();
//specify where the photo will be stored
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("ENACTUS UOE").child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_image"); //just replace the old image with the new one

        if(mBytes.length/MB < MB_THRESHHOLD) {

            // Create file metadata including the content type
            StorageMetadata metadata = new StorageMetadata.Builder()
                    .setContentType("image/jpg")
                    .setContentLanguage("en") //see nodes below
                    /*
                    Make sure to use proper language code ("English" will cause a crash)
                    I actually submitted this as a bug to the Firebase github page so it might be
                    fixed by the time you watch this video. You can check it out at https://github.com/firebase/quickstart-unity/issues/116
                     */
                    .setCustomMetadata("Mitch's special meta data", "JK nothing special here")
                    .setCustomMetadata("location", "Iceland")
                    .build();
            //if the image size is valid then we can submit to database
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes, metadata);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {


                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Now insert the download url into the firebase database
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String url = uri.toString();
                            Toast.makeText(AccountActivity.this, "Upload Success", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "onSuccess: firebase download url : " + url);
                            FirebaseDatabase.getInstance().getReference()
                                    .child("ENACTUS UOE").child(getString(R.string.dbnode_users))
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child(getString(R.string.field_profile_image))
                                    .setValue(url);

                            hideDialog();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(AccountActivity.this, "could not upload photo", Toast.LENGTH_SHORT).show();

                    hideDialog();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d(TAG, "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(AccountActivity.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }

                }
            })
            ;
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    public void verifyStoragePermissions(){
        Log.d(TAG, "verifyPermissions: asking user for permissions.");
        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions(
                    AccountActivity.this,
                    permissions,
                    REQUEST_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG, "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0]);

                }
                break;
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

    private void sendToLogin() {
        startActivity(new Intent(AccountActivity.this,LoginActivity.class));
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActivityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        isActivityRunning = false;
    }
}
