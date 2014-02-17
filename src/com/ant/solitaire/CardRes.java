package com.ant.solitaire;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class CardRes {
	static private Drawable[][] card;
	static private Drawable back;
	static private Drawable background;
	
	public static Drawable getCardPicture(int value, int suit) {
		return card[value-1][suit-1];
	}
	
	public static Drawable getCardBack() {
		return back;
	}
	
	public static Drawable getBackground() {
		return background;
	}
	
	public static void init(Resources res) {
		background = res.getDrawable(R.drawable.background);
		
		back = res.getDrawable(R.drawable.back);
		card = new Drawable[13][4];
		card[0][0] = res.getDrawable(R.drawable.v1s1);
		card[1][0] = res.getDrawable(R.drawable.v2s1);
		card[2][0] = res.getDrawable(R.drawable.v3s1);
		card[3][0] = res.getDrawable(R.drawable.v4s1);
		card[4][0] = res.getDrawable(R.drawable.v5s1);
		card[5][0] = res.getDrawable(R.drawable.v6s1);
		card[6][0] = res.getDrawable(R.drawable.v7s1);
		card[7][0] = res.getDrawable(R.drawable.v8s1);
		card[8][0] = res.getDrawable(R.drawable.v9s1);
		card[9][0] = res.getDrawable(R.drawable.v10s1);
		card[10][0] = res.getDrawable(R.drawable.v11s1);
		card[11][0] = res.getDrawable(R.drawable.v12s1);
		card[12][0] = res.getDrawable(R.drawable.v13s1);
		
		card[0][1] = res.getDrawable(R.drawable.v1s2);
		card[1][1] = res.getDrawable(R.drawable.v2s2);
		card[2][1] = res.getDrawable(R.drawable.v3s2);
		card[3][1] = res.getDrawable(R.drawable.v4s2);
		card[4][1] = res.getDrawable(R.drawable.v5s2);
		card[5][1] = res.getDrawable(R.drawable.v6s2);
		card[6][1] = res.getDrawable(R.drawable.v7s2);
		card[7][1] = res.getDrawable(R.drawable.v8s2);
		card[8][1] = res.getDrawable(R.drawable.v9s2);
		card[9][1] = res.getDrawable(R.drawable.v10s2);
		card[10][1] = res.getDrawable(R.drawable.v11s2);
		card[11][1] = res.getDrawable(R.drawable.v12s2);
		card[12][1] = res.getDrawable(R.drawable.v13s2);
		
		card[0][2] = res.getDrawable(R.drawable.v1s3);
		card[1][2] = res.getDrawable(R.drawable.v2s3);
		card[2][2] = res.getDrawable(R.drawable.v3s3);
		card[3][2] = res.getDrawable(R.drawable.v4s3);
		card[4][2] = res.getDrawable(R.drawable.v5s3);
		card[5][2] = res.getDrawable(R.drawable.v6s3);
		card[6][2] = res.getDrawable(R.drawable.v7s3);
		card[7][2] = res.getDrawable(R.drawable.v8s3);
		card[8][2] = res.getDrawable(R.drawable.v9s3);
		card[9][2] = res.getDrawable(R.drawable.v10s3);
		card[10][2] = res.getDrawable(R.drawable.v11s3);
		card[11][2] = res.getDrawable(R.drawable.v12s3);
		card[12][2] = res.getDrawable(R.drawable.v13s3);
		
		card[0][3] = res.getDrawable(R.drawable.v1s4);
		card[1][3] = res.getDrawable(R.drawable.v2s4);
		card[2][3] = res.getDrawable(R.drawable.v3s4);
		card[3][3] = res.getDrawable(R.drawable.v4s4);
		card[4][3] = res.getDrawable(R.drawable.v5s4);
		card[5][3] = res.getDrawable(R.drawable.v6s4);
		card[6][3] = res.getDrawable(R.drawable.v7s4);
		card[7][3] = res.getDrawable(R.drawable.v8s4);
		card[8][3] = res.getDrawable(R.drawable.v9s4);
		card[9][3] = res.getDrawable(R.drawable.v10s4);
		card[10][3] = res.getDrawable(R.drawable.v11s4);
		card[11][3] = res.getDrawable(R.drawable.v12s4);
		card[12][3] = res.getDrawable(R.drawable.v13s4);
	}
}
