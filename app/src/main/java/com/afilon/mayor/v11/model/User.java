package com.afilon.mayor.v11.model;

public class User {

	private String Name;
	private String DUI;
	private String party;
	private String JRV;
	private String Organization;
	private String Title;
	private String signInSignature;// yes/no
	private String actaPrecidencialSignature;// yes/no
	private String actaAssemblySignature;// //
	private String actaMunicipalSignature; //
	private String ispresent; // yes/no
	private String isconfirmed;//yes/no
	private boolean proprietario; //proprietario/ suplente
	private String cargoOrder; //order according to display
	private String _duiOne, _duiTwo, _duiThree;
	private String updatedMember="no"; // yes/no
	//constants, screen type:
	public final static int SIGNIN = -1;
	public final static int PRESIDENTIAL = -2;
	public final static int ASSEMBLY = -3;
	public final static int MUNICIPAL = -4;

	/**
	 * @return the name
	 */
	public String getName() {
		return Name;
	}

	public String getParty() {
		return party;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		Name = name;
	}

	public void setParty(String p) { party = p;}
	/**
	 * @return the dUI
	 */
	public String getDUI() {
		return DUI;
	}
	public String getJRV(){return JRV;}
	public String get_duiThree() {return _duiThree;}
	public String get_duiOne(){return _duiOne;}
	public String get_duiTwo(){return _duiTwo;}

	/**
	 * @param dUI
	 *            the dUI to set
	 */
	public void setDUI(String dUI) {
		DUI = dUI;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return Organization;
	}
	public String getSignInSignature(){
		return signInSignature;
	}
	public String getActaSignature(){
		return actaPrecidencialSignature;
	}
	public String getActaAssemblySignature(){
		return actaAssemblySignature;
	}
	public String getActaMunicipalSignature(){
		return actaMunicipalSignature;
	}
	public String getIspresent(){
		return ispresent;
	}
	public String getIsconfirmed(){return isconfirmed; }
	public boolean getProprietario(){
		return proprietario;
	}
	public String getCargoOrder(){
		return cargoOrder;
	}
	public String getUpdatedMember(){return updatedMember;}
	/**
	 * @param organization
	 *            the organization to set
	 */
	public void setOrganization(String organization) {
		Organization = organization;
	}
	public String getTitle(){return Title;}
	public void setTitle(String title){Title = title;}
	public void setJRV(String jrv){JRV=jrv;}
	public void newMemberFromDB(String id, String name,String title, String ispresent,  String isconfirmed, String proprietario){
		this.setDUI(id);
		this.setName(name);
		this.ispresent = ispresent;
		this.isconfirmed = isconfirmed;
		this.setTitle(title);
		this.proprietario = Boolean.valueOf(proprietario);
	}
	public void newMemberFromDBwParty(String id, String name,String title, String ispresent,  String isconfirmed, String proprietario, String p){
		this.setDUI(id);
		this.setName(name);
		this.setParty(p);
		this.ispresent = ispresent;
		this.isconfirmed = isconfirmed;
		this.setTitle(title);
		this.proprietario = Boolean.valueOf(proprietario);
	}
	public void set_duiOne(String duiOne){
		this._duiOne=duiOne;
	}
	public void set_duiTwo(String duiTwo){
		this._duiTwo = duiTwo;
	}
	public void set_duiThree(String duiThree){
		this._duiThree =duiThree;
	}
	public void setUpdatedMember(String isUpdate){
		this.updatedMember=isUpdate;
	}
	public void concateDui(){
		this.setDUI(_duiOne+_duiTwo+_duiThree);
	}
	public String toString(){
		return Name+", "+Title+": Dui "+DUI+" Present "+ispresent+" proprietario -"+String.valueOf(proprietario);
	}

	public String toStringwParty(){
		return Name+", "+Title+": Dui "+DUI+" Present "+ispresent+" proprietario -"+String.valueOf(proprietario) + " Party " + party;
	}
	public void parseDUI(){
		/** this method parses the dui according to Honduras display of dui in
		 * the MERPresentes activity */
		char[] cNumber = DUI.toCharArray();
//		if (cNumber.length<8){
//			String number = DUI+"                      ";
//			cNumber = number.toCharArray();
//		}
		if (cNumber.length>=13) {
			this._duiOne = String.valueOf(cNumber, 0, 4);// todo the last parameter is length, check to see if it copies the first four characters
			this._duiTwo = String.valueOf(cNumber, 4, 4);
			this._duiThree = String.valueOf(cNumber, 8, 5);
		}
	}
	public void isPresent(boolean present){
		if(present){
			this.ispresent = "Yes";
		}else{
			this.ispresent = "No";
		}
	}
	public boolean isConfirmed(){
		return isconfirmed.equals("Yes");
	}

	public boolean isPresent(){
		return (ispresent.equals("Yes"));
	}
	public void signedActa(boolean signed, int signature){
		switch (signature){
			case SIGNIN:
				if (signed){
					this.isconfirmed = "Yes";
				}else{
					this.isconfirmed = "No";
				}
				break;
			case ASSEMBLY:
				if (signed){
					this.actaAssemblySignature = "Yes";
				}else{
					this.actaAssemblySignature = "No";
				}
				break;
			case PRESIDENTIAL:
				if (signed){
					this.actaPrecidencialSignature = "Yes";
				}else{
					this.actaPrecidencialSignature = "No";
				}
				break;
			case MUNICIPAL:
				if (signed){
					this.actaPrecidencialSignature = "Yes";
				}else{
					this.actaPrecidencialSignature = "No";
				}
				break;
		}

	}
	public String isPropietario(){
		return String.valueOf(proprietario);
	}
	public boolean isProprietario(){
		return proprietario;
	}
	public void setCargoOrder(String order){
		this.cargoOrder = order;
	}
	public void clearEntries(int signature){
		this.DUI = "";
		this.Name ="";
		isPresent(false);
		switch (signature){
			case SIGNIN:
				this.signInSignature ="";
				break;
			case ASSEMBLY:
				this.actaAssemblySignature="";
				break;
			case PRESIDENTIAL:
				this.actaPrecidencialSignature="";
				break;
			case MUNICIPAL:
				this.actaMunicipalSignature="";
				break;
		}

	}

}
