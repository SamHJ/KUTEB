package com.naijaunik.kuteb.Api;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

import static com.naijaunik.kuteb.Utils.AppConstants.API_CONTROLLER;

public interface ApiInterface {

    //This method is used for non parametrized fetching
    @GET(API_CONTROLLER)
    Call<ServerResponse> get(
            @Query("request_type") String method
    );

    //This method is used for login "POST"
    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("param") String param
    );

    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("phone") String phone,
            @Field("newstatus") String newStatus
    );


    //This method is used for making calls with 5 parameters using "POST"
    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("param1") String param1,
            @Field("param2") String param2,
            @Field("param3") String param3,
            @Field("param4") String param4
    );

    //This method is used for update "POST"
    @Multipart
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Part("request_type") RequestBody method,
            @Part("firstname") RequestBody firstname,
            @Part("lastname") RequestBody lastname,
            @Part("param") RequestBody phone,
            @Part("email") RequestBody email,
            @Part("gender") RequestBody gender,
            @Part("image") RequestBody image,
            @Part("isImageEmpty") RequestBody isImageEmpty,
            @Part("image\"; filename=\"myfile.jpg\" ") RequestBody file
    );

    @Multipart
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Part("request_type") RequestBody method,
            @Part("slideId") RequestBody slideId,
            @Part("title") RequestBody title,
            @Part("subtitle") RequestBody subtitle,
            @Part("gotourl") RequestBody gotourl,
            @Part("isimageEmpty") RequestBody isimageEmpty,
            @Part("oldimage") RequestBody oldimage,
            @Part("image\"; filename=\"myfile.jpg\" ") RequestBody file
    );

    @Multipart
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Part("request_type") RequestBody method,
            @Part("title") RequestBody title,
            @Part("description") RequestBody description,
            @Part("no_of_students") RequestBody noOfStudents,
            @Part("status") RequestBody status,
            @Part("course_icon") RequestBody image,
            @Part("courseid") RequestBody courseid,
            @Part("isImageEmpty") RequestBody isImageEmpty,
            @Part("iscourseupdate") RequestBody iscourseupdate,
            @Part("image\"; filename=\"myfile.jpg\" ") RequestBody file
    );

    @Multipart
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Part("request_type") RequestBody method,
            @Part("lessonId") RequestBody lessonId,
            @Part("title") RequestBody title,
            @Part("content") RequestBody content,
            @Part("type") RequestBody type,
            @Part("videofile\"; filename=\"video.mp4\" ") RequestBody videoFile,
            @Part("extlink") RequestBody extlink,
            @Part("attachmentfile\"; filename=\"attachment.file_tension\"") RequestBody attachmentFile,
            @Part("isVideoEmpty") RequestBody isVideoEmpty,
            @Part("isAttachmentEmpty") RequestBody isAttachmentEmpty,
            @Part("attachment") RequestBody attachment,
            @Part("video") RequestBody video,
            @Part("attExt") RequestBody attExt
    );

    @Multipart
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Part("request_type") RequestBody method,
            @Part("questionFile\"; filename=\"attachment.file_tension\"") RequestBody requestFile,
            @Part("isFileAvailable") RequestBody isFileAvailable,
            @Part("qSubject") RequestBody qSubject,
            @Part("question") RequestBody question,
            @Part("qfileType") RequestBody qfileType,
            @Part("qExamOptions") RequestBody qExamOptions,
            @Part("qCorrectAnswer") RequestBody qCorrectAnswer,
            @Part("qQuestionDifficulty") RequestBody qQuestionDifficulty,
            @Part("examId") RequestBody examId,
            @Part("fileExt") RequestBody fileExt,
            @Part("fileInput") RequestBody fileInput

    );

    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("subject") String subject,
            @Field("message") String message,
            @Field("sender") String sender,
            @Field("receiver") String receiver,
            @Field("app_name") String app_name
    );

    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("appName") String appName,
            @Field("trialPeriod") String trialPeriod,
            @Field("aboutApp") String aboutApp,
            @Field("privacyPolicy") String privacyPolicy,
            @Field("terms") String terms,
            @Field("contactPhone") String contactPhone,
            @Field("queryEmail") String queryEmail,
            @Field("paymentOption") String paymentOption,
            @Field("appVersion") String appVersion,
            @Field("currencySymbol") String currencySymbol,
            @Field("currencyName") String currencyName,
            @Field("countryCode") String countryCode,
            @Field("questionDifficulties") String questionDifficulties
    );


    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("planId") String planId,
            @Field("planName") String planName,
            @Field("planAmount") String planAmount,
            @Field("planDurationName") String planDurationName,
            @Field("planDurationInDays") String planDurationInDays,
            @Field("isplanupdate") String isplanupdate
    );

    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("phone")String phone,
            @Field("planId")String planId,
            @Field("datePaid") String datePaid,
            @Field("newNoOfDays")long newNoOfDays);

    @FormUrlEncoded
    @POST(API_CONTROLLER)
    Call<ServerResponse> post(
            @Field("request_type") String method,
            @Field("examtitle")String examtitle,
            @Field("issubjectwise")String issubjectwise,
            @Field("examid") String examid);

}
