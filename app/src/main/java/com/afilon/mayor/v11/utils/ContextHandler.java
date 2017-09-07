package com.afilon.mayor.v11.utils;

import android.content.Context;

/**
 * Created by BReinosa on 4/13/2017.
 */
public class ContextHandler {
    private static Context electionContext;
    public static void setElectionContext(Context context){
        electionContext = context;
    }
    public static Context getElectionContext(){
        return electionContext;
    }
}
