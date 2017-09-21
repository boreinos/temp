package com.afilon.mayor.v11.utils;

import android.app.Activity;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.activities.CameraActaActivity;
import com.afilon.mayor.v11.activities.CaptureActivity;
import com.afilon.mayor.v11.activities.CheckListActivity;
import com.afilon.mayor.v11.activities.CrossVoteSummaryActivity;
import com.afilon.mayor.v11.activities.CrossedVoteActivity;
import com.afilon.mayor.v11.activities.EmptyTableActivity;
import com.afilon.mayor.v11.activities.ExitActivity;
import com.afilon.mayor.v11.activities.FinalTableLazyLoadingActivity;
import com.afilon.mayor.v11.activities.JrvActivity;
import com.afilon.mayor.v11.activities.LastActivity;
import com.afilon.mayor.v11.activities.LoginActivity;
import com.afilon.mayor.v11.activities.PapeletasActivity;
import com.afilon.mayor.v11.activities.ParlacenCandidateListActivity;
import com.afilon.mayor.v11.activities.ParlacenVoteTableActivity;
import com.afilon.mayor.v11.activities.PuestaCeroTableActivity;
import com.afilon.mayor.v11.activities.ReclamosActivity;
import com.afilon.mayor.v11.activities.RollCall;
import com.afilon.mayor.v11.activities.SplashActivity;
import com.afilon.mayor.v11.activities.SummaryActivity;
import com.afilon.mayor.v11.activities.SummaryActivityPreferential;
import com.afilon.mayor.v11.activities.VerticalConceptoTableActivity;
import com.afilon.mayor.v11.activities.VoteCounterActivity;
import com.afilon.mayor.v11.activities.WebViewJrvActivity;
import com.afilon.mayor.v11.fragments.DialogToConfirmDuiTwoBtns;

public final class Consts {

	private Consts() {
		// this prevents even the native class from
		// calling this constructor as well :
		throw new AssertionError();
	}

	public static final String EMPTY_STRING = "";
	public static final String SPACE = " ";
	public static final String TAB = "\t";
	public static final String SINGLE_QUOTE = "'";
	public static final String PERIOD = ".";
	public static final String DOUBLE_QUOTE = "\"";
	public static final String MESAINSTALL = ContextHandler.getElectionContext().getResources().getString(R.string.ismesa);

	public static final String BASE_URI = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.loginApp);
	public static final String PATH_NAME = "/rest/UserInfoService/name/";
	public static final String MER_LOGIN_PATH = "https://four.afilon.com:8443/" + ContextHandler.getElectionContext().getResources().getString(R.string.staffApp) + "/rest/UserValidation/loginMerManagerELSA/";
	public static final String PATH_CONFIRM = "/rest/UserInfoService/login/"+ContextHandler.getElectionContext().getResources().getString(R.string.electionType)+"/";
	public static final String DUI_KEY = "1533794"; // For testing only
	public static final String MER_MANAGEMENT = "https://four.afilon.com:8443/HONDURAS_Mesa_WS/rest/UserValidation/UpdateMerMembersELSA/";

	public static final String enlaceUser = "2ElSalvador2014";
	public static final String enlacePassword = "Xerr0rD@t@2014Now";

	public static final String PREF_ELECTION_URL = "https://four.afilon.com:8443/"+ ContextHandler.getElectionContext().getResources().getString(R.string.webApp)+"/rest/save";
	public static final String PREF_ELECTION_IMAGE_URL = "https://four.afilon.com:8443/"+ ContextHandler.getElectionContext().getResources().getString(R.string.imageApp)+"/rest/save/Image";
	public static final String PREF_ELECTION_SIG_URL = "https://four.afilon.com:8443/"+ ContextHandler.getElectionContext().getResources().getString(R.string.webApp)+"/rest/save/Signatures";
    public static final String NEWMEMBERS = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.staffApp) +"/rest/UserValidation/"+ ContextHandler.getElectionContext().getResources().getString(R.string.webUpdate)+"/";
	public static final String GETMEMBERS = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.staffApp) +"/rest/UserValidation/"+ContextHandler.getElectionContext().getResources().getString(R.string.webRequest)+"/";
	public static final String GETMEMBERSELSA = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.staffAppELSA) +"/rest/UserValidation/"+ContextHandler.getElectionContext().getResources().getString(R.string.webRequest)+"/";
	public static final String GETMEMBERSWPARTY = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.staffApp) +"/rest/UserValidation/"+ContextHandler.getElectionContext().getResources().getString(R.string.webRequest)+"/";
	public static final String VALIDATETABLET = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.staffApp) +"/rest/UserValidation/tablet/"+ContextHandler.getElectionContext().getResources().getString(R.string.electionType)+"/";
	public static final String VALIDATETABLETELSA = "https://four.afilon.com:8443/"+ContextHandler.getElectionContext().getResources().getString(R.string.staffApp) +"/rest/UserValidation/tabletELSA/"+ContextHandler.getElectionContext().getResources().getString(R.string.electionType)+"/";

	public static final String PREF_ELECTION_JRVAccept_URL = "https://four.afilon.com:8443/"+ ContextHandler.getElectionContext().getResources().getString(R.string.webApp)+"/rest/save/JRVAccept";
	public static final String PREF_ELECTION_JRV_PROCESSABLE_URL = "https://four.afilon.com:8443/"+ ContextHandler.getElectionContext().getResources().getString(R.string.webApp)+"/rest/save/JRVProcessable";
	public static final String PREF_ELECTION_JRV_PROVISIONAL_URL = "https://four.afilon.com:8443/"+ ContextHandler.getElectionContext().getResources().getString(R.string.webApp)+"/rest/save/JRVProvisionalAccept";

	//VIEW JRV LINK:
	public static final String DIR_VIEW_JRV_ASSAMBLEA_URL = "http://ten.afilon.com:8085/" +	ContextHandler.getElectionContext().getResources().getString(R.string.enalceWeb)+"?j_jrv=";

	public static final String CROSS_VOTE_SUMMARY = "Resumen de Votos Cruzados";
	public static final String TOTAL_VOTE_SUMMARY = "Resumen de Resultados";
    public static final String VOTO_CRUZADO = "votoCruzado";
	public static final String CURRENT_JRV = "CURRENT_JRV";
	public static final String CURRENT_TOTAL_PARTIES_MARK = "CURRENT_TOTAL_PARTIES_MARK";
	public static final String CURRENT_TOTAL_OF_MARKS = "CURRENT_TOTAL_OF_MARKS";
	public static final String ENABLE_CONTINUAR_BUTTON = "ENABLE_CONTINUAR_BUTTON"; //Used on Cross Vote

	public static final String DUI1 = "del PRESIDENTE";
	public static final String DUI2 = "del SECRETARIO";

	//ELECTIONS AVAILABLE:
	public static final String PRESIDENT ="PRESIDENTE";
	public static final String MAYOR= "MAYOR";
	public static final String ASAMBLEA="ASAMBLEA";
	public static final String PARLACEN="PARLACEN";

	//VOTE TYPE AVAILABLE:
	public static final String DIRECT="DIRECT";

	//COUNTRIES AVAILABLE:
	public static final String HONDURAS = "HON";
	public static final String ELSALVADOR = "ELSAL";

	//DIALOG FRAGMENT OPTIONS
	public static final String FIRST_BTN = "first_btn";
	public static final String SECOND_BTN ="second_btn";
	public static final String THRID_BTN="third_btn";
	public static final String FOURTH_BTN="fourth_btn";

	//TODO: debug only, remove
//	public static final String LOCALE = "ELSA";
	public static final String LOCALE = ContextHandler.getElectionContext().getResources().getString(R.string.locale);
	public static final Class SPLASHACT = SplashActivity.class;
	public static final Class LOGINACT = LoginActivity.class;
	public static final Class JRVACT = JrvActivity.class;
	public static final Class CAPTUREACT = CaptureActivity.class;
	public static final Class PAPALETASACT = PapeletasActivity.class;
	public static final Class EMPTYTABLEACT = EmptyTableActivity.class;
	public static final Class PUESTACEROACT = PuestaCeroTableActivity.class;
	public static final Class CONCEPTOACT = VerticalConceptoTableActivity.class;
	public static final Class CROSSVOTEACT = CrossedVoteActivity.class;
	public static final Class SUMCROSSACT = CrossVoteSummaryActivity.class;
	public static final Class FINALTABLEACT = FinalTableLazyLoadingActivity.class;
	public static final Class CANDLISTACT = ParlacenCandidateListActivity.class;
	public static final Class VOTETABLEACT = ParlacenVoteTableActivity.class;
	public static final Class RECACT = ReclamosActivity.class;
	public static final Class ROLLACT = RollCall.class;
	public static final Class SUMACT = SummaryActivity.class;
	public static final Class PREFSUMACT = SummaryActivityPreferential.class;
	public static final Class CHECKLISTACT = CheckListActivity.class;
	public static final Class CAMACT = CameraActaActivity.class;
	public static final Class LASTACT = LastActivity.class;
	public static final Class WEBACT = WebViewJrvActivity.class;
	public static final Class EXITACT = ExitActivity.class;
	public static final Class DIALOGTOCONFIRMDUITWOBTNS = DialogToConfirmDuiTwoBtns.class;
	public static final Class VOTECOUNTER = VoteCounterActivity.class;


	public static final String FLAGVOTE = "Bandera";

}

