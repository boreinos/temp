package com.afilon.mayor.v11.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Escrudata implements Parcelable {

	private String jrv;
	private String barcode;
	private String sobrantes;
	private String inutilizadas;
	private String votosPartido_1;
	private String votosPartido_2;
	private String votosPartido_3;
	private String votosPartido_4;
	private String votosPartido_5;
	private String votosPartido_6;
	private String inpugnados;
	private String nulos;
	private String abstenciones;
	private String escrutadas;
	private String faltantes;
	private String entregadas;
	private String papeletasTotal;
	private String papeletasInicio;
	private String papeletasFinal;
	private String actaImageLink;
	private String reclamos;
	private String horaCierre;
	private String errorTypeOne;
	private String errorTypeTwo;
	private String errorTypeThree;
	private String errorTypeFour;
	private String errorTypeFive;
	private String errorTypeSix;
	private String submitterOne;
	private String submitterTwo;
	private String votosValidos;
	private String partyVotes=null;
	private String valueMap="";
	private String pageTitle;

	public Escrudata(String jrvString) {
		this.jrv = jrvString;

	}
	public void setPartyVotes(String votes){
		this.partyVotes = votes;
	}
	public String getPartyVotes(){
		return partyVotes;
	}


	public void setPageTitle(String title){
		this.pageTitle = title;
	}
	public String getPageTitle(){
		return pageTitle;
	}
	public void setValuMap(String valueMap){
		this.valueMap = valueMap;
	}
	public String getValueMap(){
		return valueMap;
	}


	public String getJrv() {
		return jrv;
	}

	public void setJrv(String jrv) {
		this.jrv = jrv;
	}

	public String getSobrantes() {
		return sobrantes;
	}

	public void setSobrantes(String sobrantes) {
		this.sobrantes = sobrantes;
	}

	public String getInutilizadas() {
		return inutilizadas;
	}

	public void setInutilizadas(String inutilizadas) {
		this.inutilizadas = inutilizadas;
	}

	public String getVotosPartido_1() {
		return votosPartido_1;
	}

	public void setVotosPartido_1(String votosPartido_1) {
		this.votosPartido_1 = votosPartido_1;
	}

	public String getVotosPartido_2() {
		return votosPartido_2;
	}

	public void setVotosPartido_2(String votosPartido_2) {
		this.votosPartido_2 = votosPartido_2;
	}

	public String getVotosPartido_3() {
		return votosPartido_3;
	}

	public void setVotosPartido_3(String votosPartido_3) {
		this.votosPartido_3 = votosPartido_3;
	}

	public String getVotosPartido_4() {
		return votosPartido_4;
	}

	public void setVotosPartido_4(String votosPartido_4) {
		this.votosPartido_4 = votosPartido_4;
	}

	public String getVotosPartido_5() {
		return votosPartido_5;
	}

	public void setVotosPartido_5(String votosPartido_5) {
		this.votosPartido_5 = votosPartido_5;
	}

	public String getVotosPartido_6() {
		return votosPartido_6;
	}

	public void setVotosPartido_6(String votosPartido_6) {
		this.votosPartido_6 = votosPartido_6;
	}

	public String getInpugnados() {
		return inpugnados;
	}

	public void setInpugnados(String inpugnados) {
		this.inpugnados = inpugnados;
	}

	public String getNulos() {
		return nulos;
	}

	public void setNulos(String nulos) {
		this.nulos = nulos;
	}

	public String getAbstenciones() {
		return abstenciones;
	}

	public void setAbstenciones(String abstenciones) {
		this.abstenciones = abstenciones;
	}

	public String getEscrutadas() {
		return escrutadas;
	}

	public void setEscrutadas(String escrutadas) {
		this.escrutadas = escrutadas;
	}

	public String getFaltantes() {
		return faltantes;
	}

	public void setFaltantes(String faltantes) {
		this.faltantes = faltantes;
	}

	public String getEntregadas() {
		return entregadas;
	}

	public void setEntregadas(String entregadas) {
		this.entregadas = entregadas;
	}
	public String getVotosValidos(){return votosValidos;}
	public void setVotosValidos(String votos){
		this.votosValidos = votos;
	}

	public String getPapeletasTotal() {
		return papeletasTotal;
	}

	public void setPapeletasTotal(String papeletasTotal) {
		this.papeletasTotal = papeletasTotal;
	}

	public String getPapeletasInicio() {
		return papeletasInicio;
	}

	public void setPapeletasInicio(String papeletasInicio) {
		this.papeletasInicio = papeletasInicio;
	}

	public String getPapeletasFinal() {
		return papeletasFinal;
	}

	public void setPapeletasFinal(String papeletasFinal) {
		this.papeletasFinal = papeletasFinal;
	}

	public String getActaImageLink() {
		return actaImageLink;
	}

	public void setActaImageLink(String actaImageLink) {
		this.actaImageLink = actaImageLink;
	}

	public String getReclamos() {
		return reclamos;
	}

	public void setReclamos(String reclamos) {
		this.reclamos = reclamos;
	}

	public String getHoraCierre() {
		return horaCierre;
	}

	public void setHoraCierre(String horaCierre) {
		this.horaCierre = horaCierre;

	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getErrorTypeOne() {
		return errorTypeOne;
	}

	public void setErrorTypeOne(String errorTypeOne) {
		this.errorTypeOne = errorTypeOne;
	}

	public String getErrorTypeTwo() {
		return errorTypeTwo;
	}

	public void setErrorTypeTwo(String errorTypeTwo) {
		this.errorTypeTwo = errorTypeTwo;
	}

	public String getErrorTypeThree() {
		return errorTypeThree;
	}

	public void setErrorTypeThree(String errorTypeThree) {
		this.errorTypeThree = errorTypeThree;
	}

	public String getErrorTypeFour() {
		return errorTypeFour;
	}

	public void setErrorTypeFour(String errorTypeFour) {
		this.errorTypeFour = errorTypeFour;
	}

	public String getErrorTypeFive() {
		return errorTypeFive;
	}

	public void setErrorTypeFive(String errorTypeFive) {
		this.errorTypeFive = errorTypeFive;
	}

	public String getErrorTypeSix() {
		return errorTypeSix;
	}

	public void setErrorTypeSix(String errorTypeSix) {
		this.errorTypeSix = errorTypeSix;
	}

	public String getSubmitterOne() {
		return submitterOne;
	}

	public void setSubmitterOne(String submitterOne) {
		this.submitterOne = submitterOne;
	}

	public String getSubmitterTwo() {
		return submitterTwo;
	}

	public void setSubmitterTwo(String submitterTwo) {
		this.submitterTwo = submitterTwo;
	}

	// Parcelling part
	public Escrudata(Parcel in) {

		String[] data = new String[32];
		in.readStringArray(data);
		this.jrv = data[0];
		this.sobrantes = data[1];
		this.inutilizadas = data[2];
		this.votosPartido_1 = data[3];
		this.votosPartido_2 = data[4];
		this.votosPartido_3 = data[5];
		this.votosPartido_4 = data[6];
		this.partyVotes = data[7];
		this.pageTitle= data[8];
		this.inpugnados = data[9];
		this.nulos = data[10];
		this.abstenciones = data[11];
		this.escrutadas = data[12];
		this.faltantes = data[13];
		this.entregadas = data[14];
		this.papeletasInicio = data[15];
		this.papeletasFinal = data[16];
		this.actaImageLink = data[17];
		this.reclamos = data[18];
		this.horaCierre = data[19];
		this.papeletasTotal = data[20];
		this.errorTypeOne = data[21];
		this.errorTypeTwo = data[22];
		this.errorTypeThree = data[23];
		this.errorTypeFour = data[24];
		this.errorTypeFive = data[25];
		this.errorTypeSix = data[26];
		this.barcode = data[27];
		this.submitterOne = data[28];
		this.submitterTwo = data[29];
		this.votosValidos = data[30];
		this.valueMap = data[31];
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof Escrudata))
			return false;

		Escrudata escrudata = (Escrudata) obj;
		if (this.barcode == escrudata.barcode)
			return true;
		else
			return false;

	}

	@Override
	public int hashCode() {
		String i = (String) barcode;
		return Integer.valueOf(i);
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(new String[] { this.jrv, this.sobrantes,
				this.inutilizadas, this.votosPartido_1, this.votosPartido_2,
				this.votosPartido_3, this.votosPartido_4, this.partyVotes,
				this.pageTitle, this.inpugnados, this.nulos,
				this.abstenciones, this.escrutadas, this.faltantes,
				this.entregadas, this.papeletasInicio, this.papeletasFinal,
				this.actaImageLink, this.reclamos, this.horaCierre,
				this.papeletasTotal, this.errorTypeOne, this.errorTypeTwo,
				this.errorTypeThree, this.errorTypeFour, this.errorTypeFive,
				this.errorTypeSix, this.barcode,
				this.submitterOne, this.submitterTwo, this.votosValidos, this.valueMap });
	}

	public static final Parcelable.Creator<Escrudata> CREATOR = new Parcelable.Creator<Escrudata>() {
		public Escrudata createFromParcel(Parcel in) {
			return new Escrudata(in);
		}

		public Escrudata[] newArray(int size) {
			return new Escrudata[size];
		}
	};
}
