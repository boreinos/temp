package com.afilon.mayor.v11.model;

public class elsaLog {

    private String jrv;
    private String election_id,logIndex, Dui_1, Dui_2, Dui_3, Dui_4, Dui_5, time_stamp;

    public String getJrv() {
        return jrv;
    }
    public void setJrv(String jrv) {
        this.jrv = jrv;
    }

    public String getelection_id() {
        return election_id;
    }
    public void setelection_id(String preferential_election_id) {this.election_id = preferential_election_id;}

    public String getLogIndex(){ return logIndex;}
    public void setLogIndex(String index){ this.logIndex = index;}

    public String getDui_1(){return Dui_1;}
    public void setDui_1(String dui){this.Dui_1 = dui;}

    public String getDui_2(){return Dui_2;}
    public void setDui_2(String dui){this.Dui_2 = dui;}

    public String getDui_3(){return Dui_3;}
    public void setDui_3(String dui){this.Dui_3 = dui;}

    public String getDui_4(){return Dui_4;}
    public void setDui_4(String dui){this.Dui_4 = dui;}

    public String getDui_5(){return Dui_5;}
    public void setDui_5(String dui){this.Dui_5 = dui;}

    public String getTime_stamp(){return time_stamp;}
    public void setTime_stamp(String time){this.time_stamp = time;}
}