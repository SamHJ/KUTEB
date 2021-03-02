package com.naijaunik.kuteb.Api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.naijaunik.kuteb.Utils.Utilities;
import com.squareup.otto.Produce;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.naijaunik.kuteb.Utils.AppConstants.BASE_URL;

public class Communicator {
    private static final String TAG = "Communicator";

    public  void nonParametrizedCall(String method, final Context mContext){

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);


        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();

        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.get(method);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {

                if(response.isSuccessful()){

                    BusProvider.getInstance().post(new ServerEvent(response.body()));
                    Log.e(TAG, "Success");

                }else{

                    Log.e("Error: ",response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }

    public void singleParametrizedCall(String param,String method) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, param);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });
    }

    public void sendHelpEmail(String method, String subject, String message, String receiver, String sender,
                              String app_name){

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, subject, message, receiver, sender,app_name);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }

    public void updateAdminSettings(String method,String appName, String trialPeriod, String aboutApp,
                                    String privacyPolicy, String terms, String contactPhone, String queryEmail,
                                    String paymentOption, String version,String currencySymbol,
                                    String currencyName, String countryCode,String questionDifficulties){
        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, appName, trialPeriod, aboutApp,
                privacyPolicy, terms, contactPhone, queryEmail, paymentOption,version,currencySymbol,
                currencyName, countryCode,questionDifficulties);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }

    public void updateUserProfile(String firstname, String lastname, String phone, String email, String gender,
                                  String image, String isImageEmpty, String method,Uri fileUri,Context context){

        RequestBody requestFile = null;

       if(fileUri != null){

           //creating a file
           File file = new File(image);

           //creating request body for file
           requestFile = RequestBody.create(MediaType.
                   parse(context.getContentResolver().getType(fileUri)), file);

       }

        RequestBody reqMethod = RequestBody.create(MediaType.parse("text/plain"), method);
        RequestBody firstName = RequestBody.create(MediaType.parse("text/plain"), firstname);
        RequestBody lastName = RequestBody.create(MediaType.parse("text/plain"), lastname);
        RequestBody userPhone = RequestBody.create(MediaType.parse("text/plain"), phone);
        RequestBody userEmail = RequestBody.create(MediaType.parse("text/plain"), email);
        RequestBody userGender = RequestBody.create(MediaType.parse("text/plain"), gender);
        RequestBody userImage = RequestBody.create(MediaType.parse("text/plain"), image);
        RequestBody userIsImageEmpty = RequestBody.create(MediaType.parse("text/plain"),isImageEmpty);



        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(1, TimeUnit.HOURS);
        httpClient.readTimeout(1, TimeUnit.HOURS);
        httpClient.writeTimeout(1, TimeUnit.HOURS);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(reqMethod, firstName,lastName,userPhone,
                userEmail,userGender,userImage,userIsImageEmpty,requestFile);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });
    }


    public void fiveParametrizedCall(String method, String param1, String param2,
                                     String param3, String param4){

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, param1, param2,param3,param4);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }


    public void updateCourse(String courseid, String title, String description, String noOfStudents, String status,
                             String image, String isImageEmpty, String method,Uri fileUri,Context context){

        RequestBody requestFile = null;

        if(fileUri != null){

            //creating a file
            File file = new File(image);

            //creating request body for file
            requestFile = RequestBody.create(MediaType.
                    parse(context.getContentResolver().getType(fileUri)), file);

        }

        RequestBody reqMethod = RequestBody.create(MediaType.parse("text/plain"), method);
        RequestBody ctitle = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody cdescription = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody cnoOfStudents = RequestBody.create(MediaType.parse("text/plain"), noOfStudents);
        RequestBody cstatus = RequestBody.create(MediaType.parse("text/plain"), status);
        RequestBody cCourseImage = RequestBody.create(MediaType.parse("text/plain"), image);
        RequestBody cCourseID = RequestBody.create(MediaType.parse("text/plain"), courseid);
        RequestBody userIsImageEmpty = RequestBody.create(MediaType.parse("text/plain"),isImageEmpty);



        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(1, TimeUnit.HOURS);
        httpClient.readTimeout(1, TimeUnit.HOURS);
        httpClient.writeTimeout(1, TimeUnit.HOURS);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(reqMethod, ctitle,cdescription,cnoOfStudents,
                cstatus,cCourseImage,cCourseID,userIsImageEmpty,null,requestFile);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });
    }

    public void updateLesson(String lessonId, String title, String content, String type, String video,
                             String extlink, String attachment, String method,String isVideoEmpty,
                             String isAttachmentEmpty,Uri videoFileUri,Uri attachmentFileUri,Context context,
                             String videoFile, String attachmentFile){

        RequestBody requestVideoFile = null;
        RequestBody requestAttachmentFile = null;

        if(videoFileUri != null){

            //creating a file
            File vfile = new File(videoFile);


            //creating request body for file
            requestVideoFile = RequestBody.create(MediaType.
                    parse(context.getContentResolver().getType(videoFileUri)), vfile);

        }

        String extension = "";

        if(attachmentFileUri != null){

            File afile = new File(attachmentFile);

            requestAttachmentFile = RequestBody.create(MediaType.
                    parse(context.getContentResolver().getType(attachmentFileUri)), afile);

            extension = attachmentFile.substring(attachmentFile.lastIndexOf("."));

        }

        RequestBody reqMethod = RequestBody.create(MediaType.parse("text/plain"), method);
        RequestBody clessonId = RequestBody.create(MediaType.parse("text/plain"), lessonId);
        RequestBody ctitle = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody cContent = RequestBody.create(MediaType.parse("text/plain"), content);
        RequestBody ctype = RequestBody.create(MediaType.parse("text/plain"), type);
        RequestBody cextlink = RequestBody.create(MediaType.parse("text/plain"), extlink);
        RequestBody cisVideoEmpty = RequestBody.create(MediaType.parse("text/plain"), isVideoEmpty);
        RequestBody cisAttachmentEmpty = RequestBody.create(MediaType.parse("text/plain"), isAttachmentEmpty);
        RequestBody cattachment = RequestBody.create(MediaType.parse("text/plain"),attachment);
        RequestBody cvideo = RequestBody.create(MediaType.parse("text/plain"),video);
        RequestBody attExt = RequestBody.create(MediaType.parse("text/plain"),extension);



        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(1, TimeUnit.HOURS);
        httpClient.readTimeout(1, TimeUnit.HOURS);
        httpClient.writeTimeout(1, TimeUnit.HOURS);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(reqMethod, clessonId,ctitle,cContent,
                ctype,requestVideoFile,cextlink,requestAttachmentFile,cisVideoEmpty,cisAttachmentEmpty,
                cattachment,cvideo,attExt);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call, @NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call, @NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });
    }

    public void updateSlideItem(String method, String title, String subtitle, String gotourl, String slideId,
                                String image,
                                Uri imageFileUri,String imagePath,Context context,String isimageEmpty){


        RequestBody requestFile = null;

        if(imageFileUri != null){

            //creating a file
            File file = new File(imagePath);

            //creating request body for file
            requestFile = RequestBody.create(MediaType.
                    parse(context.getContentResolver().getType(imageFileUri)), file);

        }

        RequestBody reqMethod = RequestBody.create(MediaType.parse("text/plain"), method);
        RequestBody ctitle = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody csubtitle = RequestBody.create(MediaType.parse("text/plain"), subtitle);
        RequestBody cgotourl = RequestBody.create(MediaType.parse("text/plain"), gotourl);
        RequestBody cslideID = RequestBody.create(MediaType.parse("text/plain"), slideId);
        RequestBody cImage = RequestBody.create(MediaType.parse("text/plain"), image);
        RequestBody cIsImageEmpty = RequestBody.create(MediaType.parse("text/plain"),isimageEmpty);



        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(1, TimeUnit.HOURS);
        httpClient.readTimeout(1, TimeUnit.HOURS);
        httpClient.writeTimeout(1, TimeUnit.HOURS);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(reqMethod,cslideID, ctitle,csubtitle,cgotourl,
                cIsImageEmpty,cImage,requestFile);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }


    public void updatePlan(String method,String planName,String planAmount, String planDurationName, String planDurationInDays,
                           String planId){

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, planId, planName, planAmount,
                planDurationName, planDurationInDays, "yes");

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }


    public void upgradeUserPlan(String method, String phone, long newNoOfDays, String planId){

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        String todaysDate = Utilities.getCurrentDate();

        Call<ServerResponse> call = service.post(method, phone, planId, todaysDate,newNoOfDays);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }

    public void changeUserStatus(String phone, String newStatus, String method) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, phone, newStatus);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });
    }

    public void updateExam(String method, String examTitle, String isExamSubjectWise, String id) {

        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(method, examTitle, isExamSubjectWise, id);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }


    public void uploadUpdateExamQuestion(String method, String isfileAvailable, String subject,
                                         String examQuestion, String filePath, Uri fileUri, String fileType,
                                         String examOptions, String correctAnswer, String questionDifficulty,
                                         Context context,String examId,String fileInput) {


        RequestBody requestFile = null;

        String extension = "";

        if(fileUri != null){

            //creating a file
            File file = new File(filePath);

            //creating request body for file
            requestFile = RequestBody.create(MediaType.
                    parse(context.getContentResolver().getType(fileUri)), file);

            extension = filePath.substring(filePath.lastIndexOf("."));

        }

        RequestBody reqMethod = RequestBody.create(MediaType.parse("text/plain"), method);
        RequestBody isFileAvailable = RequestBody.create(MediaType.parse("text/plain"), isfileAvailable);
        RequestBody qSubject = RequestBody.create(MediaType.parse("text/plain"), subject);
        RequestBody question = RequestBody.create(MediaType.parse("text/plain"), examQuestion);
        RequestBody qfileType = RequestBody.create(MediaType.parse("text/plain"), fileType);
        RequestBody qExamOptions = RequestBody.create(MediaType.parse("text/plain"), examOptions);
        RequestBody qCorrectAnswer = RequestBody.create(MediaType.parse("text/plain"), correctAnswer);
        RequestBody qQuestionDifficulty = RequestBody.create(MediaType.parse("text/plain"),questionDifficulty);
        RequestBody qexamId = RequestBody.create(MediaType.parse("text/plain"),examId);
        RequestBody fileExt = RequestBody.create(MediaType.parse("text/plain"),extension);
        RequestBody qfileInput = RequestBody.create(MediaType.parse("text/plain"),fileInput);



        //Here a logging interceptor is created
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //The logging interceptor will be added to the http client
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.connectTimeout(1, TimeUnit.HOURS);
        httpClient.readTimeout(1, TimeUnit.HOURS);
        httpClient.writeTimeout(1, TimeUnit.HOURS);

        //The Retrofit builder will have the client attached, in order to get connection logs
        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        ApiInterface service = retrofit.create(ApiInterface.class);

        Call<ServerResponse> call = service.post(reqMethod,requestFile,isFileAvailable,qSubject,question,qfileType,
                qExamOptions,qCorrectAnswer,qQuestionDifficulty,qexamId,fileExt,qfileInput);

        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(@NotNull Call<ServerResponse> call,@NotNull Response<ServerResponse> response) {
                BusProvider.getInstance().post(new ServerEvent(response.body()));
                Log.e(TAG, "Success");
            }

            @Override
            public void onFailure(@NotNull Call<ServerResponse> call,@NotNull Throwable t) {
                // handle execution failures like no internet connectivity
                BusProvider.getInstance().post(new ErrorEvent(-2, t.getMessage()));
            }
        });

    }

    @Produce
    public ServerEvent produceServerEvent(ServerResponse serverResponse) {
        return new ServerEvent(serverResponse);
    }

    @Produce
    public ErrorEvent produceErrorEvent(int errorCode, String errorMsg) {
        return new ErrorEvent(errorCode, errorMsg);
    }


}