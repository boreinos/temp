package com.afilon.mayor.v11.model;

/**
 * Created by BReinosa on 8/10/2017.
 */
public class DrawerItem {
    String itemName;
    String quantity;
    int imgResID;

    public DrawerItem(String itemName, int imgResID, String quantity){
        super();
        this.itemName = itemName;
        this.imgResID = imgResID;
        this.quantity = quantity;
    }
    public String getItemName(){
        return itemName;
    }
    public void setItemName(String itemName){
        this.itemName = itemName;
    }
    public int getImgResID(){
        return imgResID;
    }
    public void setImgResID(int resID) {
        imgResID = resID;
    }
    public String getQuantity(){
        return quantity;
    }
    public void setQuantity(String quantity){
        this.quantity = quantity;
    }

}
