package com.kiddnation254.kiddnation254;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class ChangePhotoActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private String path, userId;
    Button fileBrowseBtn;
    Button uploadBtn;
    ImageView previewImage;
    TextView fileName;
    LinearLayout linearLayout;
    RelativeLayout relativeLayout;
    RelativeLayout progressBarContainer;
    Uri fileUri;
    Helpers helpers;
    private File file;
    private static final int REQUEST_FILE_CODE = 200;
    private static final int READ_REQUEST_CODE = 300;
    private int GALLERY = 1, CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_photo);
        helpers = new Helpers(this);

        fileBrowseBtn = findViewById(R.id.btn_choose_file);
        uploadBtn = findViewById(R.id.btn_upload);
        previewImage = findViewById(R.id.img_preview);
        fileName = findViewById(R.id.tv_file_name);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout1);
//        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);

//        progressBarContainer.setVisibility(View.GONE);

        fileBrowseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //check if app has permission to access the external storage.
                if (EasyPermissions.hasPermissions(ChangePhotoActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showPictureDialog();

                } else {
                    //If permission is not present request for the same.
                    EasyPermissions.requestPermissions(ChangePhotoActivity.this, getString(R.string.read_file), READ_REQUEST_CODE, Manifest.permission.READ_EXTERNAL_STORAGE);
                }

            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (file != null) {
                    if (helpers.isConnectedToInternet()) {
                        UploadAsyncTask uploadAsyncTask = new UploadAsyncTask(ChangePhotoActivity.this);
                        uploadAsyncTask.execute();
                    } else {
                        Toast toast = new Toast(getApplicationContext());
                        View view = getLayoutInflater().inflate(R.layout.network_error, null);
                        toast.setView(view);
                        int gravity = Gravity.BOTTOM;
                        toast.setGravity(gravity, 10, 10);
                        toast.show();
                    }

                } else {
                    Toast toast = new Toast(getApplicationContext());
                    View view = getLayoutInflater().inflate(R.layout.warning, null);
                    TextView textView = view.findViewById(R.id.message);
                    textView.setText(R.string.no_file);
                    toast.setView(view);
                    int gravity = Gravity.BOTTOM;
                    toast.setGravity(gravity, 10, 10);
                    toast.show();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        userId = Integer.toString(SharedPrefManager.getInstance(getApplicationContext()).getUserId());
        path = Constants.URL_USER_IMG;
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
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == GALLERY) {
                fileUri = data.getData();
                previewFile(fileUri);

            } else if (requestCode == CAMERA) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                fileUri = getImageUri(this, bitmap);
                previewFile(fileUri);

            }

        }

        /*super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }*/
        /*if (requestCode == GALLERY) {
            if (data != null) {
                fileUri = data.getData();
                previewFile(fileUri);
            }

        } else if (requestCode == CAMERA) {
            if (data != null) {
                fileUri = data.getData();
                previewFile(fileUri);
            }
        }*/
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    /**
     * Show the file name and preview once the file is chosen
     *
     * @param uri
     */
    private void previewFile(Uri uri) {
        String filePath = getRealPathFromURIPath(uri, ChangePhotoActivity.this);
        file = new File(filePath);
        fileName.setText(file.getName());

        ContentResolver cR = this.getContentResolver();
        String mime = cR.getType(uri);

        //Show preview if the uploaded file is an image.
        if (mime != null && mime.contains("image")) {
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            previewImage.setImageBitmap(bitmap);
        } else {
            previewImage.setImageResource(R.drawable.ic_file);
        }

        hideFileChooser();
    }

    /**
     * Shows an intent which has options from which user can choose the file like File manager, Gallery etc
     */
    private void showFileChooserIntent() {
        Intent fileManagerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        //Choose any file
        fileManagerIntent.setType("image/*");
        startActivityForResult(fileManagerIntent, REQUEST_FILE_CODE);

    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
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

    /**
     * Returns the actual path of the file in the file system
     *
     * @param contentURI
     * @param activity
     * @return
     */
    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, ChangePhotoActivity.this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
//        showFileChooserIntent();
        showPictureDialog();
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
//        Log.d(TAG, "Permission has been denied");
    }

    /**
     * Hides the Choose file button and displays the file preview, file name and upload button
     */
    private void hideFileChooser() {
        relativeLayout.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.GONE);
    }

    /**
     * Displays Choose file button and Hides the file preview, file name and upload button
     */
    private void showFileChooser() {
        relativeLayout.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);

    }

    /**
     * Background network task to handle file upload.
     */
    private class UploadAsyncTask extends AsyncTask<Void, Integer, String> {

        HttpClient httpClient = new DefaultHttpClient();
        private Context context;
        private Exception exception;
        private ProgressDialog progressDialog;

        private UploadAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {

            HttpResponse httpResponse = null;
            HttpEntity httpEntity = null;
            String responseString = null;
            String responseJSON = null;

            try {
                HttpPost httpPost = new HttpPost(Constants.URL_UPLOAD_PHOTO);
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();

                // Add the file to be uploaded
                multipartEntityBuilder.addPart("file", new FileBody(file));
                multipartEntityBuilder.addTextBody("userId", userId);

                // Progress listener - updates task's progress
                MyHttpEntity.ProgressListener progressListener =
                        new MyHttpEntity.ProgressListener() {
                            @Override
                            public void transferred(float progress) {
                                publishProgress((int) progress);
                            }
                        };

                // POST
                httpPost.setEntity(new MyHttpEntity(multipartEntityBuilder.build(),
                        progressListener));


                try {
                    httpResponse = httpClient.execute(httpPost);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                httpEntity = httpResponse.getEntity();

                responseJSON = EntityUtils.toString(httpResponse != null ? httpResponse.getEntity() : null);

                /*int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(httpEntity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }*/
            } catch (UnsupportedEncodingException | ClientProtocolException e) {
                e.printStackTrace();
                Log.e("UPLOAD", e.getMessage());
                this.exception = e;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return responseJSON;
        }

        @Override
        protected void onPreExecute() {
                // Init and show background_dialog
                this.progressDialog = new ProgressDialog(this.context);
                this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                this.progressDialog.setCancelable(false);
                this.progressDialog.show();
        }


        @Override
        protected void onPostExecute(String result) {
            // Close background_dialog
            this.progressDialog.dismiss();
//            progressBarContainer.setVisibility(View.GONE);
            try {
                JSONObject jsonObject = new JSONObject(result);
                if (!jsonObject.getBoolean("error")) {
                    Toast toast = new Toast(getApplicationContext());
                    View view = getLayoutInflater().inflate(R.layout.message, null);
                    TextView textView = view.findViewById(R.id.message);
                    textView.setText(jsonObject.getString("message"));
                    toast.setView(view);
                    int gravity = Gravity.BOTTOM;
                    toast.setGravity(gravity, 10, 10);
                    toast.show();
                    SharedPrefManager.getInstance(getApplicationContext())
                            .setUserImageLink(jsonObject.getString("user_image"));

                } else {
                    Toast toast = new Toast(getApplicationContext());
                    View view = getLayoutInflater().inflate(R.layout.warning, null);
                    TextView textView = view.findViewById(R.id.message);
                    textView.setText(jsonObject.getString("message"));
                    toast.setView(view);
                    int gravity = Gravity.BOTTOM;
                    toast.setGravity(gravity, 10, 10);
                    toast.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            showFileChooser();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update process
            this.progressDialog.setProgress(progress[0]);
        }
    }

    private boolean hasActiveInternetConnection(Context context) {
        if (isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection)
                        (new URL("http://clients3.google.com/generate_204")
                                .openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.setRequestMethod("POST");
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e("TAG", "Error checking internet connection", e);
            }
        } else {
            Log.d("TAG", "No network available!");
        }
        return false;
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager
                .getActiveNetworkInfo() : null;
        return activeNetworkInfo != null;
    }
}
