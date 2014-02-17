package com.ant.solitaire;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class ScoreDialog extends Dialog {
	static public String SCORE_FILE_LEVEL_1 = "score1";
	static public String NAME_FILE_LEVEL_1 = "name1";
	static public String SCORE_FILE_LEVEL_2 = "score2";
	static public String NAME_FILE_LEVEL_2 = "name2";
	static public String SCORE_FILE_LEVEL_3 = "score3";
	static public String NAME_FILE_LEVEL_3 = "name3";
	static public String SCORE_FILE_LEVEL_4 = "score4";
	static public String NAME_FILE_LEVEL_4 = "name4";
	
	private String scoreFileName;
	private String nameFileName;
	private Context mContext;
	private int level;
	private String title;
	private int n;
	private int[] score;
	private String[] name;
	
	public ScoreDialog(Context context) {
		super(context);
		mContext = context;
		score = new int[6];
		name = new String[6];
		scoreFileName = new String();
		nameFileName = new String();
		setLevel(3);
		this.setContentView(R.layout.score);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.score);
		title = mContext.getResources().getString(R.string.title_score) + " ";
		this.setTitle(title + Integer.toString(level));
	}
	
	public void setLevel(int level) {
		//update level and file name
		this.level = level;
		getFileName();
		
		//update title
		this.setTitle(title + Integer.toString(this.level));
	}
	
	public void getFileName() {
		switch (level) {
		case 1:
			scoreFileName = SCORE_FILE_LEVEL_1;
			nameFileName = NAME_FILE_LEVEL_1;
			break;
		case 2:
			scoreFileName = SCORE_FILE_LEVEL_2;
			nameFileName = NAME_FILE_LEVEL_2;
			break;
		case 3:
			scoreFileName = SCORE_FILE_LEVEL_3;
			nameFileName = NAME_FILE_LEVEL_3;
			break;
		case 4:
			scoreFileName = SCORE_FILE_LEVEL_4;
			nameFileName = NAME_FILE_LEVEL_4;
			break;
		}
	}
	
	public void updateScore() {
		resetText();
		DataInputStream scoreFile = null;
		DataInputStream nameFile = null;
		try {
			scoreFile = new DataInputStream(mContext.openFileInput(scoreFileName));
			nameFile = new DataInputStream(mContext.openFileInput(nameFileName));
		} catch (FileNotFoundException e) {
			n = 0;
			return;
		}
		
		//read data
		n = 0;
		try {
			while (n<5) 
				try { 
					score[n++] = scoreFile.readInt();
				} catch (EOFException e) {
					n--;
					break;
				}
				
			int m = 0;
			String str = "";
			char c;
			while (m<n) 
				try {
					c = nameFile.readChar();
					if (c!='\n') 
						if (c!='|') str += c;
						else {
							name[m++] = str;
							str = "";
						}
				} catch (EOFException e) {
					break;
				}
			scoreFile.close();
			nameFile.close();
		} catch (IOException e) {
			//do nothing
		}
		
		if (n==0) return;
		
		//sort
		for (int i=0; i<n-1; i++)
			for (int j=i+1; j<n; j++)
				if (score[j]>score[i]) {
					int t = score[i];
					score[i] = score[j];
					score[j] = t;
					
					String s = name[i];
					name[i] = name[j];
					name[j] = s;
				}
		
		printOut();
	}

	private void printOut() {
		//print out
		TextView nameText;
		TextView scoreText;
		for (int i=0; i<n; i++) 
			switch (i) {
			case 0:
				nameText = (TextView) findViewById(R.id.name_1);
				scoreText = (TextView) findViewById(R.id.score_1);
				nameText.setText(name[i]);
				scoreText.setText(""+score[i]);
				break;
			case 1:
				nameText = (TextView) findViewById(R.id.name_2);
				scoreText = (TextView) findViewById(R.id.score_2);
				nameText.setText(name[i]);
				scoreText.setText(""+score[i]);
				break;
			case 2:
				nameText = (TextView) findViewById(R.id.name_3);
				scoreText = (TextView) findViewById(R.id.score_3);
				nameText.setText(name[i]);
				scoreText.setText(""+score[i]);
				break;
			case 3:
				nameText = (TextView) findViewById(R.id.name_4);
				scoreText = (TextView) findViewById(R.id.score_4);
				nameText.setText(name[i]);
				scoreText.setText(""+score[i]);
				break;
			case 4:
				nameText = (TextView) findViewById(R.id.name_5);
				scoreText = (TextView) findViewById(R.id.score_5);
				nameText.setText(name[i]);
				scoreText.setText(""+score[i]);
				break;
			}
	}
	
	private void resetText() {
		TextView nameText;
		TextView scoreText;
		for (int i=0; i<n; i++) 
			switch (i) {
			case 0:
				nameText = (TextView) findViewById(R.id.name_1);
				scoreText = (TextView) findViewById(R.id.score_1);
				nameText.setText("");
				scoreText.setText("");
				break;
			case 1:
				nameText = (TextView) findViewById(R.id.name_2);
				scoreText = (TextView) findViewById(R.id.score_2);
				nameText.setText("");
				scoreText.setText("");
				break;
			case 2:
				nameText = (TextView) findViewById(R.id.name_3);
				scoreText = (TextView) findViewById(R.id.score_3);
				nameText.setText("");
				scoreText.setText("");
				break;
			case 3:
				nameText = (TextView) findViewById(R.id.name_4);
				scoreText = (TextView) findViewById(R.id.score_4);
				nameText.setText("");
				scoreText.setText("");
				break;
			case 4:
				nameText = (TextView) findViewById(R.id.name_5);
				scoreText = (TextView) findViewById(R.id.score_5);
				nameText.setText("");
				scoreText.setText("");
				break;
			}
	}
	
	public int checkNewScore(int newScore) {
		updateScore();
		int over = 0;
		for (int i=0; i<n; i++)
			if (score[i]>=newScore) over++;
		return over+1;
	}
	
	public void appendNewScore(int newScore, String name_str) {
		n++;
		score[n-1] = newScore;
		name[n-1] = name_str;
		
		DataOutputStream scoreFile = null;
		DataOutputStream nameFile = null;
		try {
			scoreFile = new DataOutputStream(mContext.openFileOutput(scoreFileName, Context.MODE_PRIVATE));
			nameFile = new DataOutputStream(mContext.openFileOutput(nameFileName, Context.MODE_PRIVATE));
		} catch (FileNotFoundException ex) {
			return;
		}
		
		//write out
		for (int i=0; i<((n>5)?5:n); i++)
			try {
				int max = 0;
				for (int j=1; j<n; j++)
					if (score[j]>score[max]) max = j;
				scoreFile.writeInt(score[max]);
				nameFile.writeChars(name[max]+"|");
				score[max] = 0;
			} catch (IOException e) {
				//doing nothing
			}
			
		//close stream
		try {
			scoreFile.close();
			nameFile.close();
		} catch (IOException e) {
			//doing nothing
		}
	}
	
	public void deleteScore() {
		DataOutputStream scoreFile = null;
		DataOutputStream nameFile = null;
		try {
			scoreFile = new DataOutputStream(mContext.openFileOutput(scoreFileName, Context.MODE_PRIVATE));
			nameFile = new DataOutputStream(mContext.openFileOutput(nameFileName, Context.MODE_PRIVATE));
		} catch (FileNotFoundException ex) {
			return;
		}
		
		//close stream
		try {
			scoreFile.close();
			nameFile.close();
		} catch (IOException e) {
			//doing nothing
		}
	}
}
