package com.kiddnation254.kiddnation254;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
public interface UploadImageInterface {
    @Multipart
    @POST("uploadPhoto.php")
    Call<UploadObject> uploadFile(@Part MultipartBody.Part file, @Part("name") RequestBody name,
                                  @Part("userId") RequestBody userId);
}