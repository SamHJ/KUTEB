package com.naijaunik.kuteb.Utils;

import com.paypal.android.sdk.payments.PayPalConfiguration;

public interface AppConstants {

    String BASE_URL = "http://kelearning.naijaunik.com/";//change to your own domain where backend files have been hosted
    String API_CONTROLLER = "ApiController.php";
    String COUNTRY_PHONE_CODE = "+234"; //country code for phone verification

    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String APP_NAME = "KUTEB";//change to your own app name

    // Google Console APIs developer key
    // Replace this key with your's
    public static final String YOUTUBE_API_KEY = "AIzaSyC5vt57KIS_ym-JgM1OqFhar2hevz1WE48";//your Google Api key for YouTube


    // PayUmoney Constants
    String MONEY_HASH = BASE_URL+"PayUMoneyHash.php";
    String SURL = "https://www.payumoney.com/mobileapp/payumoney/success.php";
    String FURL = "https://www.payumoney.com/mobileapp/payumoney/failure.php";
    String MERCHANT_KEY = "OlChSkE0";//your PayUmoney merchant key
    String MERCHANT_ID = "4934580";//your PayUmoney merchant ID
    boolean DEBUG = true; //used for testing. Please change this to "false" before you publish your app !!!

    //Paystack Constants
    String PAYMENT_VERIFIER = BASE_URL+"PaystackPaymentVerifier.php?reference=";
    String ERROR_MSG = "Declined. Please use the test card since you are doing a test transaction.";
    //Paystack Test Cards? Go to https://developers.paystack.co/docs/test-cards to get test cards from Paystack!

    //PayPal Constants
    String PAYPAL_CLIENT_ID = "AXDTetL-yJWYm4M00nYy7EXj_sHaZFLFCKaDlXK0d5haRBrWD1QZfsQMNNZSBdboj0UsRfYl4bHrJSZH";//change to your PayPal client ID
    String PAYPAL_CONFIG_ENV = PayPalConfiguration.ENVIRONMENT_SANDBOX;  //Remember to change to ENVIRONMENT_PRODUCTION for production use!

    //Flutterwave Constants
    String FLUTTERWAVE_PUBLIC_KEY = "FLWPUBK-322dd7b6f3f54b9176490df07383b872-X"; //Remember to change to your live public key!!!
    String FLUTTERWAVE_ENCRYPTION_KEY = "f32975391b719233c357c7db";//Remember to change to your live encryption key!!!
    String FLUTTERWAVE_PAYMENT_VERIFIER = BASE_URL+"flutterwavePaymentVerifier.php?id=";


    //App wide consts
    int REQUEST_STORAGE_PERMISSION = 5385;
    int VIDEO_GALLERY_PICK = 4728;
    int DOCUMENT_GALLERY_PICK = 3343;
    int AUDIO_GALLERY_PICK = 4783;
    int IMAGE_GALLERY_PICK = 3938;
}
