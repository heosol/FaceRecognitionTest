package com.sheotest.app.facerecognitiontest.network;

import com.google.gson.JsonObject;
import com.sheotest.app.facerecognitiontest.network.model.NaverRepo;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by sheo on 2019-11-26.
 */
public interface RetrofitService {

    // 게시물 전체 조회
    @Headers("Content-Type: application/json")
    @POST("content/showContents")
    Call<JsonObject> getAllPosts();

    // 게시물 ID 조회
    @FormUrlEncoded
    @POST("content/showContent")
    Call<JsonObject> getPostId(
            @Field("_id") String _id
    );

//    @Headers("POST /v1/vision/face HTTP/1.1\n" +
//            "Host: openapi.naver.com\n" +
//            "Content-Type: multipart/form-data; boundary={boundary-text}\n" +
//            "X-Naver-Client-Id: {앱 등록 시 발급받은 Client ID}\n" +
//            "X-Naver-Client-Secret: {앱 등록 시 발급 받은 Client Secret}\n" +
//            "Content-Length: 96703\n" +
//            "\n" +
//            "--{boundary-text}\n" +
//            "Content-Disposition: form-data; name=\"image\"; filename=\"test.jpg\"\n" +
//            "Content-Type: image/jpeg\n" +
//            "\n" +
//            "{image binary data}\n" +
//            "--{boundary-text}--")

//    @Headers({
//            "Content-Type: multipart/form-data; boundary={boundary-text}",
//            "X-Naver-Client-Id: {lL1DuMSVssWahYB80wAX}",
//            "X-Naver-Client-Secret: {jU5MU86HyE}",
//    })
//    @Multipart
//    @POST("v1/vision/face")
//    Call<JsonObject> test(@Part MultipartBody.Part file);


    @Multipart
    @POST("/v1/vision/face")
//    Call<JsonObject> test2(@Header("X-Naver-Client-Id") String id
//            ,@Header("X-Naver-Client-Secret") String secret
//            ,@Part MultipartBody.Part file);
    Call<NaverRepo> test2(@Header("X-Naver-Client-Id") String id
            , @Header("X-Naver-Client-Secret") String secret
            , @Part MultipartBody.Part file);

    /**
     * 작업 진행중
     */
    String HEADER_CONTENT_TYPE = "Content-Type: application/json";
    String HEADER_ACCEPT = "Accept: application/json";

//    @Headers({HEADER_CONTENT_TYPE, HEADER_ACCEPT})
//    @POST("member/checkEmail")
//    Call<MiracleResult<Void>> test(@Body JsonObject custNo);

}
