package com.ant.solitaire;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.graphics.Canvas;

public class CardBundle extends CardGroup {
	private Card[] mRepCards;
	private int repCardNum;

	public CardBundle() {
		//this constructor is for restoring
	}
	
	public CardBundle(Coordinate coord) {
		//init card group
		this.nCard = 0;
		this.cards = new ArrayList<Card>(121);
		this.coord = new Coordinate(coord.x, coord.y);
		
		//init visual representative
		repCardNum = 5;
		mRepCards = new Card[5];
		for (int i=0; i<5; i++)
			mRepCards[i] = new Card(1, 1, false, new Coordinate(coord.x - i*Math.round(Card.WIDTH*0.2f), coord.y));
	}
	
	@Override
	protected Coordinate calculateCoord(int order) {
		//order 1-10:class 0, 11-20:class 1, ..., 41~: class 4
		int t = (order-1)/10;
		if (t>4) t=4;
		Coordinate c = new Coordinate();
		c.x = mRepCards[t].getCoord().x;
		c.y = mRepCards[t].getCoord().y;
		return c;
	}
	
	@Override
	public Card getCard() {
		if ((nCard-1) <= (repCardNum-1)*10) repCardNum--;
		return super.getCard();
	}
	
	@Override
	public boolean containsOnLastCard(Coordinate c) {
		if (isEmpty()) return false;
		
		Card card = mRepCards[repCardNum-1];
		if (c.x>=card.getCoord().x && c.x<=(card.getCoord().x+Card.WIDTH) 
			&& c.y>=card.getCoord().y && c.y<=(card.getCoord().y+Card.HEIGHT))
			return true;
		else return false;
	}
	
	/*@Override
	public boolean contains(Coordinate c) {
		if (c.x>=mRepCards[repCardNum-1].getCoord().x && c.x<=(mRepCards[0].getCoord().x+Card.WIDTH) 
			&& c.y>=mRepCards[repCardNum-1].getCoord().y && c.y<=(mRepCards[0].getCoord().y+Card.HEIGHT))
			return true;
		else return false;
	}*/
	
	@Override
	public void draw(Canvas canvas) {
		for (int i=0; i<repCardNum; i++)
			mRepCards[i].draw(canvas);
	}
	
	@Override
	public void saveCurrentState(SharedPreferences.Editor editor, String key) {
		super.saveCurrentState(editor, key);
		editor.putInt(key + " repCardNum", repCardNum);
		for (int i=0; i<repCardNum; i++)
			mRepCards[i].saveCurrentState(editor, key + " mRepCards" + Integer.toString(i));
	}
	
	@Override
	public void restoreSavedState(SharedPreferences pref, String key) {
		super.restoreSavedState(pref, key);
		repCardNum = pref.getInt(key + " repCardNum", 0);
		mRepCards = new Card[5];
		for (int i=0; i<repCardNum; i++) {
			mRepCards[i] = new Card();
			mRepCards[i].restoreSavedState(pref, key + " mRepCards" + Integer.toString(i));
		}
	}
}
