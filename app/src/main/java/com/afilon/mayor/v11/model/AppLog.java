package com.afilon.mayor.v11.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author CCano
 * @category Parcelable AppLog Class
 * @since 2014-09-18
 */
public class AppLog implements Parcelable {

	private String jrv;
	private String electionId;
	private String dui;
	private String screenName; //JRV, FinalTableLazyLoading, etc
	private String typeDescription; //Concepts, Boleta, etc
	private String originalValue;
	private String finalValue;
	private String datetime;
	
	public AppLog(String jrv) {
		this.jrv = jrv;
	}
	
	public AppLog (Parcel in) {
		String[] data = new String[8];
		in.readStringArray(data);
		this.jrv = data[0];
		this.electionId = data[1];
		this.dui = data[2];
		this.screenName = data[3];
		this.typeDescription = data[4];
		this.originalValue = data[5];
		this.finalValue = data[6];
		this.datetime = data[7];
	}
	
	public void setJrv(String jrv) {
		this.jrv = jrv;
	}
	
	public String getJrv() {
		return this.jrv;
	}
	
	public void setElectionId(String electionId) {
		this.electionId = electionId;
	}
	
	public String getElectionId() {
		return this.electionId;
	}
	
	public void setDui(String dui) {
		this.dui = dui;
	}
	
	public String getDui() {
		return this.dui;
	}
	
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	
	public String getScreenName() {
		return this.screenName;
	}
	
	
	public void  setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}
	
	public String getTypeDescription() {
		return this.typeDescription;
	}
	
	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}
	
	public String getOriginalValue() {
		return this.originalValue;
	}
	
	public void setFinalValue(String finalValue) {
		this.finalValue = finalValue;
	}
	
	public String getFinalValue() {
		return this.finalValue;
	}
	
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}
	
	public String getDatetime() {
		return this.datetime;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { this.jrv, this.electionId,
				this.dui, this.screenName, this.typeDescription,
				this.originalValue, this.finalValue, this.datetime });
		
	}
	
	public static final Parcelable.Creator<AppLog> CREATOR = new Parcelable.Creator<AppLog>() {
		public AppLog createFromParcel(Parcel in) {
			return new AppLog(in);
		}

		public AppLog[] newArray(int size) {
			return new AppLog[size];
		}
	};

}
