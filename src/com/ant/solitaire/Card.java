package com.ant.solitaire;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public class Card {
	//dimension of a card, calculated in GameThread.setCardSize() after surfaceSizeChanged()
	public static int WIDTH;
	public static int HEIGHT;
	public static int PADDING;
	
	private int value;
	private int suit;
	private boolean isUp;
	private Coordinate coord;
	
	public Card() {
		this.value = 1;
		this.suit = 1;
		this.isUp = true;
		this.coord = new Coordinate();
	}
	
	public Card(int value, int suit, boolean isUp, Coordinate coord) {
		this.value = value;
		this.suit = suit;
		this.isUp = isUp;
		this.coord = new Coordinate(coord.x, coord.y);
	}
	
	public void setCoord(Coordinate coord) {
		this.coord.x = coord.x;
		this.coord.y = coord.y;
	}
	
	public Coordinate getCoord() {
		return this.coord;
	}
	
	public void setUpDown(boolean state) {
		isUp = state;
	}
	
	public boolean getUpDown() {
		return isUp;
	}
	
	public void move(int dx, int dy) {
		coord.x += dx;
		coord.y += dy;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getSuit() {
		return suit;
	}
	
	public void draw(Canvas canvas) {
		Drawable d;
		if (isUp) d = CardRes.getCardPicture(value, suit);
		else d = CardRes.getCardBack();
		
		d.setBounds(coord.x, coord.y, coord.x+WIDTH, coord.y+HEIGHT);
		d.draw(canvas);
	}
	
	public void saveCurrentState(SharedPreferences.Editor editor, String key) {
		editor.putInt(key + " value", value);
		editor.putInt(key + " suit", suit);
		editor.putBoolean(key + " isUp", isUp);
		editor.putInt(key + " coord.x", coord.x);
		editor.putInt(key + " coord.y", coord.y);
	}
	
	public void restoreSavedState(SharedPreferences pref, String key) {
		value = pref.getInt(key + " value", 1);
		suit = pref.getInt(key + " suit", 1);
		isUp = pref.getBoolean(key + " isUp", true);
		coord.x = pref.getInt(key + " coord.x", 1);
		coord.y = pref.getInt(key + " coord.y", 1);
	}
}
