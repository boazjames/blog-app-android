package com.kiddnation254.kiddnation254;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ChangeProfilePhotoActivity extends AppCompatActivity {
    ImageView image;
    Button choose, upload;
    String userId;
    Bitmap bitmap;
    ProgressDialog progressDialog;
    RelativeLayout relativeLayout, progressContainer;
    int CAMERA = 2, GALLERY = 1;
    File file;
    TextView fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_photo);

        image = (ImageView) findViewById(R.id.img_preview);
        choose = (Button) findViewById(R.id.btn_choose_file);
        upload = (Button) findViewById(R.id.btn_upload);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout1);
        fileName = findViewById(R.id.tv_file_name);
        progressContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);

        progressContainer.setVisibility(View.GONE);

        relativeLayout.setVisibility(View.GONE);
        userId = Integer.toString(SharedPrefManager.getInstance(getApplicationContext()).getUserId());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Change Profile Photo");

        //opening image chooser option
        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if app has permission to access the external storage.
                Dexter.withActivity(ChangeProfilePhotoActivity.this)
                        .withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA)
                        .withListener(new MultiplePermissionsListener() {
                            @Override
                            public void onPermissionsChecked(MultiplePermissionsReport report) {
                                if(!report.getDeniedPermissionResponses().isEmpty()) {
                                    Log.i("TAG", "permissions not empty");
                                }
                                if(report.areAllPermissionsGranted()) {
                                    showPictureDialog();
                                } else {
                                    showSettingsDialog();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();

                        /*.withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                showPictureDialog();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                if (response.isPermanentlyDenied()) {
                                    showSettingsDialog();
                                } else {
                                    Toast toast = new Toast(getApplicationContext());
                                    View view = getLayoutInflater().inflate(R.layout.warning, null);
                                    TextView textView = view.findViewById(R.id.message);
                                    textView.setText(R.string.permission_denied);
                                    toast.setView(view);
                                    int gravity = Gravity.BOTTOM;
                                    toast.setGravity(gravity, 90, 90);
                                    toast.show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                           PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        }).check();*/

            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == GALLERY) {
                Uri uri = data.getData();
                relativeLayout.setVisibility(View.VISIBLE);
                choose.setVisibility(View.GONE);
                String filePath = getRealPathFromURIPath(uri, this);
                file = new File(filePath);
                fileName.setText(file.getName());

                try {
                    //getting image from gallery
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

                    //Setting image to ImageView
                    image.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (requestCode == CAMERA) {
                bitmap = (Bitmap) data.getExtras().get("data");
                Uri uri = getImageUri(this, bitmap);
                relativeLayout.setVisibility(View.VISIBLE);
                choose.setVisibility(View.GONE);
                String filePath = getRealPathFromURIPath(uri, this);
                file = new File(filePath);
                fileName.setText(file.getName());
                image.setImageBitmap(bitmap);

            }

        }
    }

    private void uploadPhoto() {

        //converting image to base64 string
        /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);*/

        progressContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        RequestBody mFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), mFile);
        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());
        RequestBody user_id = RequestBody.create(MediaType.parse("text/plain"), userId);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UploadImageInterface uploadImage = retrofit.create(UploadImageInterface.class);
        Call<UploadObject> fileUpload = uploadImage.uploadFile(fileToUpload, filename, user_id);
        fileUpload.enqueue(new Callback<UploadObject>() {
            @Override
            public void onResponse(Call<UploadObject> call, retrofit2.Response<UploadObject> response) {
                progressContainer.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                if (!response.body().getError()) {
                    Toast toast = new Toast(getApplicationContext());
                    View view = getLayoutInflater().inflate(R.layout.message, null);
                    TextView textView = view.findViewById(R.id.message);
                    textView.setText(response.body().getMessage());
                    toast.setView(view);
                    int gravity = Gravity.BOTTOM;
                    toast.setGravity(gravity, 90, 90);
                    toast.show();
                    SharedPrefManager.getInstance(getApplicationContext())
                            .setUserImageLink(response.body().getUser_image());
                    onBackPressed();

                } else {
                    Toast toast = new Toast(getApplicationContext());
                    View view = getLayoutInflater().inflate(R.layout.warning, null);
                    TextView textView = view.findViewById(R.id.message);
                    textView.setText(response.body().getMessage());
                    toast.setView(view);
                    int gravity = Gravity.BOTTOM;
                    toast.setGravity(gravity, 10, 10);
                    toast.show();
                }
            }

            @Override
            public void onFailure(Call<UploadObject> call, Throwable t) {
                progressContainer.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                Toast toast = new Toast(getApplicationContext());
                View view = getLayoutInflater().inflate(R.layout.network_error, null);
                toast.setView(view);
                int gravity = Gravity.BOTTOM;
                toast.setGravity(gravity, 10, 10);
                toast.show();
            }
        });

    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
        startActivityForResult(intent, CAMERA);
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this,
                R.style.MyDialogTheme);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "Photo Gallery",
                "Camera"};
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null,
                null, null, null);
        String realPath = "";
        if (cursor == null) {
            realPath = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            realPath = cursor.getString(idx);
        }
        if (cursor != null) {
            cursor.close();
        }

        return realPath;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature." +
                " You can grant them in app settings under permissions.");
        builder.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }
}