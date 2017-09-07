package com.afilon.mayor.v11.model;

/**
 * Created by BReinosa on 7/8/2016.
 */
public class LogConfirmation {
    private String Jrv;
    private String DUI1;
    private String DUI2;
    private String DUI3;
    private String Key;

    public String getJrv(){
        return Jrv;
    }
    public String getDUI1(){
        return DUI1;
    }
    public String getDUI2(){
        return DUI2;
    }
    public String getDUI3(){
        return DUI3;
    }
    public String getKey(){
        return Key;
    }
    public void setJrv(String jrv){
        Jrv = jrv;
    }
    public void setDUI1(String dui1){
        DUI1 = dui1;
    }
    public void setDUI2(String dui2){
        DUI2=dui2;
    }
    public void setDUI3(String dui3) { DUI3=dui3; }
    public void setKey(String key){
        Key = key;
    }
}
