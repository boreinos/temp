package com.afilon.mayor.v11.model;

/**
 * Created by BReinosa on 8/14/2017.
 */
public class DirectParty extends Party {

    int tempVote=0;
    int currentAccumulation=0;
    int updatedAccumulation=0;
    int imgResId;
    boolean selected=false;
    boolean confirmed = false;
    boolean enable = false;

    public int getImgResId(){
        return imgResId;
    }
    public void setImgResId(int resId){
        this.imgResId = resId;
    }
    public void select(){
//        this.tempVote = selected?1:0;
        tempVote = 1;
        selected=true;
    }
    public void confirm(){
        tempVote = 1;
        confirmed = true;
    }
    public void unConfirm(){
        tempVote= 0;
        confirmed = false;
    }

    public String getCurrentVote(){
        return String.valueOf(tempVote);
    }
    public void setUpdatedAccumulation(){
        this.updatedAccumulation = currentAccumulation+tempVote;
        setParty_votes(String.valueOf(updatedAccumulation));
    }
    public void setCurrentAccumulation(){
        this.currentAccumulation = updatedAccumulation;
    }
    public String getCurrentAcr(){
        return String.valueOf(currentAccumulation);
    }
    public String getNewAcr(){
        return String.valueOf(updatedAccumulation);
    }
    public void reset(){
        tempVote=0;
        selected = false;
        confirmed = false;
        enable = false;
    }
    public void deselect(){
        tempVote =0;
        selected  =false;
    }
    public boolean isSelected(){
        return selected;
    }
    public boolean isConfirmed(){
        return  confirmed;
    }
    public boolean match(){
        if((selected&&confirmed) || (!selected&& !confirmed)){
            return true;
        }
        enable = true;
        return false;
    }
    public boolean isEnable(){
        return enable;
    }
    public void clearTempVote(){
        tempVote=0;
    }


}
