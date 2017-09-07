package com.afilon.mayor.v11.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ccano on 8/24/17.
 */

public class ActaSumaryReport {

    private String titleA;
    private String titleB;
    private String titleC;

    private String footerA;
    private String footerB;

    private String conceptA;
    private String conceptB;
    private String conceptC;
    private String conceptD;
    private String conceptE;
    private String conceptF;
    private String conceptG;
    private String conceptH;
    private String conceptI;
    private String conceptJ;
    private String conceptK;

    private List<String> partiesName = new ArrayList<>();
    private List<String> partiesTotalMarcas = new ArrayList<>();
    private List<String> partiesPlanchaMarcas = new ArrayList<>();
    private List<String> partiesParcialVotes = new ArrayList<>();
    private List<String> partiesCruzadoVotes = new ArrayList<>();
    private List<String> partiesOther = new ArrayList<>();

    public ActaSumaryReport(String titleA, String titleB, String titleC, String footerA,
                            String footerB, String conceptA, String conceptB, String conceptC,
                            String conceptD, String conceptE, String conceptF, String conceptG,
                            String conceptH, String conceptI, String conceptJ, String conceptK,
                            List<String> partiesName, List<String> partiesTotalMarcas,
                            List<String> partiesPlanchaMarcas, List<String> partiesParcialVotes,
                            List<String> partiesCruzadoVotes, List<String> partiesOther) {
        this.titleA = titleA;
        this.titleB = titleB;
        this.titleC = titleC;
        this.footerA = footerA;
        this.footerB = footerB;
        this.conceptA = conceptA;
        this.conceptB = conceptB;
        this.conceptC = conceptC;
        this.conceptD = conceptD;
        this.conceptE = conceptE;
        this.conceptF = conceptF;
        this.conceptG = conceptG;
        this.conceptH = conceptH;
        this.conceptI = conceptI;
        this.conceptJ = conceptJ;
        this.conceptK = conceptK;
        this.partiesName = partiesName;
        this.partiesTotalMarcas = partiesTotalMarcas;
        this.partiesPlanchaMarcas = partiesPlanchaMarcas;
        this.partiesParcialVotes = partiesParcialVotes;
        this.partiesCruzadoVotes = partiesCruzadoVotes;
        this.partiesOther = partiesOther;
    }

    public String getTitleA() {
        return titleA;
    }

    public void setTitleA(String titleA) {
        this.titleA = titleA;
    }

    public String getTitleB() {
        return titleB;
    }

    public void setTitleB(String titleB) {
        this.titleB = titleB;
    }

    public String getTitleC() {
        return titleC;
    }

    public void setTitleC(String titleC) {
        this.titleC = titleC;
    }

    public String getFooterA() {
        return footerA;
    }

    public void setFooterA(String footerA) {
        this.footerA = footerA;
    }

    public String getFooterB() {
        return footerB;
    }

    public void setFooterB(String footerB) {
        this.footerB = footerB;
    }

    public String getConceptA() {
        return conceptA;
    }

    public void setConceptA(String conceptA) {
        this.conceptA = conceptA;
    }

    public String getConceptB() {
        return conceptB;
    }

    public void setConceptB(String conceptB) {
        this.conceptB = conceptB;
    }

    public String getConceptC() {
        return conceptC;
    }

    public void setConceptC(String conceptC) {
        this.conceptC = conceptC;
    }

    public String getConceptD() {
        return conceptD;
    }

    public void setConceptD(String conceptD) {
        this.conceptD = conceptD;
    }

    public String getConceptE() {
        return conceptE;
    }

    public void setConceptE(String conceptE) {
        this.conceptE = conceptE;
    }

    public String getConceptF() {
        return conceptF;
    }

    public void setConceptF(String conceptF) {
        this.conceptF = conceptF;
    }

    public String getConceptG() {
        return conceptG;
    }

    public void setConceptG(String conceptG) {
        this.conceptG = conceptG;
    }

    public String getConceptH() {
        return conceptH;
    }

    public void setConceptH(String conceptH) {
        this.conceptH = conceptH;
    }

    public String getConceptI() {
        return conceptI;
    }

    public void setConceptI(String conceptI) {
        this.conceptI = conceptI;
    }

    public String getConceptJ() {
        return conceptJ;
    }

    public void setConceptJ(String conceptJ) {
        this.conceptJ = conceptJ;
    }

    public String getConceptK() {
        return conceptK;
    }

    public void setConceptK(String conceptK) {
        this.conceptK = conceptK;
    }

    public List<String> getPartiesName() {
        return partiesName;
    }

    public void setPartiesName(List<String> partiesName) {
        this.partiesName = partiesName;
    }

    public List<String> getPartiesTotalMarcas() {
        return partiesTotalMarcas;
    }

    public void setPartiesTotalMarcas(List<String> partiesTotalMarcas) {
        this.partiesTotalMarcas = partiesTotalMarcas;
    }

    public List<String> getPartiesPlanchaMarcas() {
        return partiesPlanchaMarcas;
    }

    public void setPartiesPlanchaMarcas(List<String> partiesPlanchaMarcas) {
        this.partiesPlanchaMarcas = partiesPlanchaMarcas;
    }

    public List<String> getPartiesParcialVotes() {
        return partiesParcialVotes;
    }

    public void setPartiesParcialVotes(List<String> partiesParcialVotes) {
        this.partiesParcialVotes = partiesParcialVotes;
    }

    public List<String> getPartiesCruzadoVotes() {
        return partiesCruzadoVotes;
    }

    public void setPartiesCruzadoVotes(List<String> partiesCruzadoVotes) {
        this.partiesCruzadoVotes = partiesCruzadoVotes;
    }

    public List<String> getPartiesOther() {
        return partiesOther;
    }

    public void setPartiesOther(List<String> partiesOther) {
        this.partiesOther = partiesOther;
    }

    @Override
    public String toString() {
        return "ActaSumaryReport{" +
                "titleA='" + titleA + '\'' +
                ", titleB='" + titleB + '\'' +
                ", titleC='" + titleC + '\'' +
                ", footerA='" + footerA + '\'' +
                ", footerB='" + footerB + '\'' +
                ", conceptA='" + conceptA + '\'' +
                ", conceptB='" + conceptB + '\'' +
                ", conceptC='" + conceptC + '\'' +
                ", conceptD='" + conceptD + '\'' +
                ", conceptE='" + conceptE + '\'' +
                ", conceptF='" + conceptF + '\'' +
                ", conceptG='" + conceptG + '\'' +
                ", conceptH='" + conceptH + '\'' +
                ", conceptI='" + conceptI + '\'' +
                ", conceptJ='" + conceptJ + '\'' +
                ", conceptK='" + conceptK + '\'' +
                ", partiesName=" + partiesName +
                ", partiesTotalMarcas=" + partiesTotalMarcas +
                ", partiesPlanchaMarcas=" + partiesPlanchaMarcas +
                ", partiesParcialVotes=" + partiesParcialVotes +
                ", partiesCruzadoVotes=" + partiesCruzadoVotes +
                ", partiesOther=" + partiesOther +
                '}';
    }
}
