package com.afilon.mayor.v11.model;

import android.os.Parcel;
import android.os.Parcelable;

public class VotingCenter implements Parcelable {

	private String jrvid;
	private String JRV;
	private String barcode;
	private String department;
	private String municipality;
	private String votingCenter;
	private String voters;
	private String vc_event1_locality_id;
	private String event1_locality_id;
	private String vc_event2_locality_id;
	private String event2_locality_id;
	private String preferential_election2_id;

	public VotingCenter(String q, String a, String b, String c, String d) {
		JRV = q;
		barcode = a;
		department = b;
		municipality = c;
		votingCenter = d;
	}

	public VotingCenter() {

	}
	//----------------------------------------------------------------------------------------------
	// Set-Get Methods:  ROLL CALL ACTIVITY
	public String getJRV(){ return JRV;  }
	public void setJRV(String jrv){ this.JRV=jrv;   }
	public String getBarcode(){  return barcode;    }
	public void setBarcode(String barcode){ this.barcode = barcode;    }
	public String getDepartment(){  return department;   }
	public void setDepartment(String department){ this.department = department; }
	public String getMunicipality(){ return municipality;    }
	public void setMunicipality(String municipality){ this.municipality = municipality; }
	public String getVotingCenter(){ return votingCenter;  }
	public void setVotingCenter(String votingCenter){ this.votingCenter = votingCenter; }
	public String getPreferentialElectionId(){ return preferential_election2_id;  }
	public void setPreferentialElectionId(String preferentialElectionId){
		this.preferential_election2_id=preferentialElectionId; }

	public String getJrvString() {
		return 	JRV;}

	public void setJrvString(String jrvString) {
		this.JRV = jrvString;
	}

	public String getBarcodeString() {
		return barcode;
	}

	public void setBarcodeString(String barcodeString) {
		this.barcode = barcodeString;
	}

	public void setDepartmentoString(String departmentString) {
		this.department = departmentString;
	}

	public void setMunicipioString(String municipioString) {
		this.municipality = municipioString;
	}

	public void setVoteCenterString(String voteCenterString) {
		this.votingCenter = voteCenterString;
	}

	public String getDepartamentoString() {
		return department;
	}

	public String getMunicipioString() {
		return municipality;
	}

	public String getVoteCenterString() {
		return votingCenter;
	}

	public String getJrvid() {
		return jrvid;
	}

	public void setJrvid(String jrvid) {
		this.jrvid = jrvid;
	}

	public String getVoters() {
		return voters;
	}

	public void setVoters(String voters) {
		this.voters = voters;
	}

	public String getVc_event1_locality_id() {
		return vc_event1_locality_id;
	}

	public void setVc_Direct_Election_id(String vc_event1_locality_id) {
		this.vc_event1_locality_id = vc_event1_locality_id;
	}

	public String getEvent1_locality_id() {
		return event1_locality_id;
	}

	public void setDirect_Election_id(String event1_locality_id) {
		this.event1_locality_id = event1_locality_id;
	}

	public String getVc_event2_locality_id() {
		return vc_event2_locality_id;
	}

	public void setVc_Preferential_Election_id(String vc_event2_locality_id) {
		this.vc_event2_locality_id = vc_event2_locality_id;
	}

	public String getPref_election_id() {
		return event2_locality_id;
	}

	public void setPreferential_Election_id(String event2_locality_id) {
		this.event2_locality_id = event2_locality_id;
	}

	public String getPreferential_election2_id() {
		return preferential_election2_id;
	}

	public void setPreferential_election2_id(String preferential_election2_id) {
		this.preferential_election2_id = preferential_election2_id;
	}

	// Parcelling part
	public VotingCenter(Parcel in) {
		String[] data = new String[12];
		in.readStringArray(data);

		this.jrvid = data[0];
		this.JRV = data[1];
		this.barcode = data[2];
		this.department = data[3];
		this.municipality = data[4];
		this.votingCenter = data[5];
		this.voters = data[6];
		this.vc_event1_locality_id = data[7];
		this.event1_locality_id = data[8];
		this.vc_event2_locality_id = data[9];
		this.event2_locality_id = data[10];
		this.preferential_election2_id = data[11];

	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;

	}

	@Override
	public int hashCode() {
		String i = JRV;
		return Integer.valueOf(i);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { this.jrvid, this.JRV,
				this.barcode, this.department,
				this.municipality, this.votingCenter, this.voters,
				this.vc_event1_locality_id, this.event1_locality_id,
				this.vc_event2_locality_id, this.event2_locality_id,
				this.preferential_election2_id });

	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public VotingCenter createFromParcel(Parcel in) {
			return new VotingCenter(in);
		}

		public VotingCenter[] newArray(int size) {
			return new VotingCenter[size];
		}
	};

}
