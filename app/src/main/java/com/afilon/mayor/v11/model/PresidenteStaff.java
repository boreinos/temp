package com.afilon.mayor.v11.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PresidenteStaff implements Parcelable {


	private String staffID;
	private String description;
	
	private boolean cbOneSelected=false;
	private boolean cbTwoSelected=false;
	
	
	public PresidenteStaff() {
		// TODO Auto-generated constructor stub
	}

	public PresidenteStaff(String id,String description) {
		
		staffID = id;
		this.description = description;
		// TODO Auto-generated constructor stub
	}
	public String getDescription(){return this.description;}

	public String getStaffID() {
		return staffID;
	}

	public void setStaffID(String candidateID) {
		this.staffID = candidateID;
	}
	
	

	public boolean isCbOneSelected() {
		return cbOneSelected;
	}

	public void setCbOneSelected(boolean cbOneSelected) {
		this.cbOneSelected = cbOneSelected;
	}

	public boolean isCbTwoSelected() {
		return cbTwoSelected;
	}

	public void setCbTwoSelected(boolean cbTwoSelected) {
		this.cbTwoSelected = cbTwoSelected;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {

		dest.writeByte((byte) (cbOneSelected ? 1 : 0));
		dest.writeByte((byte) (cbTwoSelected ? 1 : 0));

	}

	public PresidenteStaff(Parcel in) {

		this.staffID = in.readString();
		this.cbOneSelected = in.readByte() != 0;
		this.cbTwoSelected = in.readByte() != 0;

	}



	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof PresidenteStaff))
			return false;

		PresidenteStaff candidate = (PresidenteStaff) obj;
		if (this.staffID == candidate.staffID)
			return true;
		else
			return false;

	}

	@Override
	public int hashCode() {
		String i = staffID;
		return Integer.valueOf(i);
	}

	public static final Creator<PresidenteStaff> CREATOR = new Parcelable.Creator<PresidenteStaff>() {
		public PresidenteStaff createFromParcel(Parcel in) {
			return new PresidenteStaff(in);
		}

		public PresidenteStaff[] newArray(int size) {
			return new PresidenteStaff[size];
		}
	};

}
