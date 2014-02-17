package com.ant.solitaire;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

public class GameActivity extends Activity {
	
	static final int ABOUT_DIALOG_ID = 0;
	static final int HELP_DIALOG_ID = 1;
	static final int SCORE_DIALOG_ID = 2;
	static final int WIN_DIALOG_ID = 3;
	static final int NAME_DIALOG_ID = 4;
	
	private GameSurface mGameSurface;
	private GameThread mThread;
	private ScoreDialog mScoreDialog;
	private int newScore;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        //init game surface
        mGameSurface = (GameSurface) findViewById(R.id.game);
        mThread = mGameSurface.getThread();
        
        //init picture resource
        CardRes.init(getResources());
        
        //init ScoreDialog
        mScoreDialog = new ScoreDialog(this);
        SharedPreferences pref = this.getPreferences(MODE_PRIVATE);
		int nsuit = pref.getInt("Level", 1);
		mScoreDialog.setLevel(nsuit);
		mScoreDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				mThread.setResume();
			}
		});
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	mThread.setPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (mThread.isAlive())
    		mThread.setResume();
    }
    
    @Override
    protected void onDestroy () {
    	super.onDestroy();
    	mThread.quit();
    	/*boolean retry = true;
		while (retry) {
			try {
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
				//do nothing
			}
		}*/
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.findItem(R.id.one).setChecked(false);
    	menu.findItem(R.id.two).setChecked(false);
    	menu.findItem(R.id.three).setChecked(false);
    	menu.findItem(R.id.four).setChecked(false);
    	
    	SharedPreferences pref = this.getPreferences(MODE_PRIVATE);
		int nsuit = pref.getInt("Level", 1);
    	switch (nsuit) {
    	case 1:
    		menu.findItem(R.id.one).setChecked(true);
    		break;
    	case 2: 
    		menu.findItem(R.id.two).setChecked(true);
    		break;
    	case 3:
    		menu.findItem(R.id.three).setChecked(true);
    		break;
    	case 4:
    		menu.findItem(R.id.four).setChecked(true);
    		break;
    	}
    	
		return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	SharedPreferences pref = this.getPreferences(MODE_PRIVATE);
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.menu_newgame:
        	mThread.setNewGame();
            return true;
        case R.id.menu_help:
        	showDialog(HELP_DIALOG_ID);
            return true;
        case R.id.menu_about:
        	showDialog(ABOUT_DIALOG_ID);
        	return true;
        case R.id.menu_score:
        	showDialog(SCORE_DIALOG_ID);
        	return true;
        /*case R.id.menu_deleteScore:
        	mScoreDialog.deleteScore();
        	return true;*/
        case R.id.menu_quit:
        	quitGame(); //finish and not save current state
        	return true;
        case R.id.menu_quit_save:
        	finish(); //normal finish which saves current state
        	return true;
        case R.id.one:
        	pref.edit().putInt("Level", 1).commit();
        	mScoreDialog.setLevel(1);
        	return true;
        case R.id.two:
        	pref.edit().putInt("Level", 2).commit();
        	mScoreDialog.setLevel(2);
        	return true;
        case R.id.three:
        	pref.edit().putInt("Level", 3).commit();
        	mScoreDialog.setLevel(3);
        	return true;
        case R.id.four:
        	pref.edit().putInt("Level", 4).commit();
        	mScoreDialog.setLevel(4);
        	return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
	    case ABOUT_DIALOG_ID:
	    	AboutDialog mAboutDialog = new AboutDialog(this);
	    	return mAboutDialog;
	    case HELP_DIALOG_ID:
	    	HelpDialog mHelpDialog = new HelpDialog(this);
	    	return mHelpDialog;
	    case SCORE_DIALOG_ID:
	    	mScoreDialog.updateScore();
	    	return mScoreDialog;
	    case WIN_DIALOG_ID:
	    	WinDialog mWinDialog = new WinDialog(this);
	    	mWinDialog.setScore(newScore);
	    	return mWinDialog;
    	case NAME_DIALOG_ID:
    		NameAskingDialog mNameAskingDialog = new NameAskingDialog(this, mThread);
        	return mNameAskingDialog;
    	}
    	return null;
    	
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	switch (id) {
    	case SCORE_DIALOG_ID:
    		((ScoreDialog) dialog).updateScore();
    		break;
    	case WIN_DIALOG_ID:
    		((WinDialog) dialog).setScore(newScore);
    		break;
    	}
    }
    
    //save the new score, and call win dialog
    public void gameOver(int score) {
    	newScore = score;
    	showDialog(WIN_DIALOG_ID);
    }
    
    //WinDialog will call this function 
    public void checkHighScore() {
    	//check if the new score is in top 5
    	int pos = mScoreDialog.checkNewScore(newScore);
    	if (pos>5) {
    		mThread.setNewGame();
    		mThread.setResume();
    	} else
    		showDialog(NAME_DIALOG_ID); //call dialog asking name
    }
    
    //NameAskingDialog will call this function to update high score with name 
    public void updateHighScore(String name_str) {
    	//write out
    	mScoreDialog.appendNewScore(newScore, name_str);
    }
    
    public void quitGame() {
    	//quit game and doesn't save the current state
    	mGameSurface.saveStateWhenFinish = false;
    	finish();
    }
}