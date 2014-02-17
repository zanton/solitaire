package com.ant.solitaire;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WinDialog extends Dialog {
	private Context mContext;
	private int score;
	
	public WinDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.win);
		this.setTitle(R.string.title_win);
		
		TextView text = (TextView) findViewById(R.id.Text_Win);
		String str = "Your score is " + Integer.toString(score);
		text.setText(str);
		
		Button but = (Button) this.findViewById(R.id.btnWin);
		but.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dismiss();
				((GameActivity) mContext).checkHighScore();
			}
		});
	}
}
