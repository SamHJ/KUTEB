package com.naijaunik.kuteb.Application;

import android.app.Application;

public class GreenDemy extends Application {

    static GreenDemy myAppInstance;

    public GreenDemy(){

        myAppInstance = this;

    }

    public static GreenDemy getInstance(){

        return  myAppInstance;
    }
}
