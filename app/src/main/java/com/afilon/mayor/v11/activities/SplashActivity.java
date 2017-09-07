package com.afilon.mayor.v11.activities;

import com.afilon.mayor.v11.R;
import com.afilon.mayor.v11.utils.ChallengeHelper;
import com.afilon.mayor.v11.utils.Consts;
import com.afilon.mayor.v11.utils.UnCaughtException;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.TextView;

import com.afilon.mayor.v11.utils.Utilities;
//import com.afilon.mayor.v8.utils.Utilities;

public class SplashActivity extends AfilonActivity {

    private Utilities utilities=new Utilities(this);
	ChallengeHelper challengeHelper = new ChallengeHelper(this);
	boolean isNew;
	private static final int CONTINUE = 1;
	private static final int RESTART = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        utilities.tabletConfiguration(Build.MODEL,this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_splash);
		//catch unexpected error:
		Thread.setDefaultUncaughtExceptionHandler(new UnCaughtException(
				SplashActivity.this));
		challengeHelper.addRoutine(CONTINUE,resume);
		challengeHelper.addRoutine(RESTART,restart);
		startAnimating();
		utilities.removePreferences("anadir");
		utilities.removePreferences("firstScreen");
		utilities.savePreferences("rechazada",false);
		isNew = utilities.loadPreferencesBool("newApplication");
	}

	private void startAnimating() {
		// Fade in top title
		TextView logo1 = (TextView) findViewById(R.id.tse_logo);
		Animation fade1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		logo1.startAnimation(fade1);

		// Transition to Main Menu when bottom title finishes animating
		fade1.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				// The animation has ended, transition to the Main Menu screen
				if(isNew || Consts.LOCALE.equals(Consts.ELSALVADOR) || Consts.MESAINSTALL.equals("YES")){
				goToNextActivity();
				}else{
					challengeHelper.createDialog("La aplicación fue cerrada prematuramente, ¿desea restablecerla y continuar donde la dejo?",CONTINUE);
				}
//				startActivity(new Intent(SplashActivity.this,LoginActivity.class));
//				SplashActivity.this.finish();
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Stop the animation
		TextView logo1 = (TextView) findViewById(R.id.tse_logo);
		logo1.clearAnimation();

	}

	@Override
	protected void onResume() {
		super.onResume();

		startAnimating();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}
	ChallengeHelper.OnApprove resume = new ChallengeHelper.OnApprove() {
		@Override
		public void approved() {
			goToSavedActivity();

		}
	};
	ChallengeHelper.OnApprove restart = new ChallengeHelper.OnApprove() {
		@Override
		public void approved() {
			utilities.savePreferences("newApplication",true);
			goToNextActivity();
		}
	};
	private void goToSavedActivity(){
		Class nextScreen = utilities.loadLastScreen();
		Bundle b = utilities.getLastBundle();
		Intent search = new Intent(SplashActivity.this, nextScreen);//PapeletasActivity.class);
		search.putExtras(b);
		startActivity(search);
		finish();
	}




	// splash activity
	// login activity
	// jrv activity
	// papeletas activity
	// empty table activity
	// horizontal vote counting activity
	// final table activity
	// reclamos activity
	// preferential vote activity
	// candidates table activity
	// camera activity
	// last activity
	


}
