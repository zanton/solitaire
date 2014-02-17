package com.ant.solitaire;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NameAskingDialog extends Dialog {
	private Context mContext;
	private GameThread mThread;
	
	public NameAskingDialog(Context context, GameThread thread) {
		super(context);
		mContext = context;
		mThread = thread;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.title_nameAsking);
		setContentView(R.layout.name);
		
		Button btn1 = (Button) findViewById(R.id.btn_nameAskingOk);
		btn1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String name_str = ((EditText) findViewById(R.id.nameAsking)).getText().toString();
				((GameActivity) mContext).updateHighScore(name_str);
				dismiss();
				((GameActivity) mContext).showDialog(GameActivity.SCORE_DIALOG_ID);
				mThread.setNewGame();
			}
		});
		
		Button btn2 = (Button) findViewById(R.id.btn_nameAskingCancel);
		btn2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mThread.setNewGame();
				dismiss();
				mThread.setResume();
			}
		});
	}
}
