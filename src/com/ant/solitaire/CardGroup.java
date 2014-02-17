package com.ant.solitaire;

import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences;
import android.graphics.Canvas;

public class CardGroup {
	protected int nCard; //value from 1
	protected ArrayList<Card> cards;
	protected Coordinate coord;
	
	public CardGroup() {
		//this constructor is for the cardbundle to initialize itself
	}
	
	public CardGroup(Coordinate coord) {
		this.nCard = 0;
		this.cards = new ArrayList<Card>(); //(13)
		this.coord = new Coordinate(coord.x, coord.y);
	}
	
	public void addCard(int value, int suit, boolean isUp) {
		nCard++;
		Card card = new Card(value, suit, isUp, calculateCoord(nCard));
		cards.add(card);
	}
	
	public void addCard(Card card) {
		nCard++;
		card.setCoord(calculateCoord(nCard));
		cards.add(card);
	}
	
	/*public void addCardOnBase(Card card) {
		nCard++;
		card.setCoord(coord);
		cards.add(0, card);
		
		for (int i=1; i<nCard-1; i++)
			cards.get(i).setCoord(cards.get(i+1).getCoord());
		cards.get(nCard-1).setCoord(calculateCoord(nCard));
	}*/
	
	public Card getCard() {
		if (--nCard < 0) {
			nCard = 0;
			return null;
		}
		
		//write out the error
		//if (nCard < 0) 
		//	Log.i("CardGroup", "Access to " + nCard + " element of cardgroup.");
		
		Card card = cards.get(nCard);
		cards.remove(nCard);
		return card;
	}
	
	protected Coordinate calculateCoord(int order) {
		Coordinate c = new Coordinate(coord.x, coord.y + (order-1)*Card.PADDING); 
		return c;
	}
	
	public Coordinate getCoord() {
		return coord;
	}
	
	public Coordinate getCoordforNewCard() {
		return calculateCoord(nCard+1);
	}
	
	public int size() {
		return cards.size();
	}
	
	public Card getFirstCard() {
		if (--nCard < 0) {
			nCard = 0;
			return null;
		}
		Card card = cards.get(0);
		cards.remove(0);
		return card;
	}
	
	public Card viewLastCard() {
		return cards.get(cards.size()-1);
	}
	
	public Card viewFirstCard() {
		return cards.get(0);
	}
	
	public Card viewCard(int index) {
		return cards.get(index);
	}
	
	public Coordinate getLastCardCoord() {
		if (!isEmpty())
			return cards.get(cards.size()-1).getCoord();
		else return coord;
	}
	
	public void move(int dx, int dy) {
		coord.x += dx;
		coord.y += dy;
		for (Card card : cards) {
			card.move(dx,dy);
		}
	}
	
	public void moveto(Coordinate c) {
		int dx = c.x - coord.x;
		int dy = c.y - coord.y;
		move(dx,dy);
	}
	
	public void merge(CardGroup cg) {
		while (cg.size()>0) 
			addCard(cg.getFirstCard());
	}
	
	public void shuffle() {
		Collections.shuffle(cards);
	}
	
	/*public boolean contains(Coordinate c) {
		if (c.x>=coord.x && c.x<=(coord.x+width) && c.y>=coord.y && c.y<=(coord.y+height)) 
			return true;
		else return false;
	}*/
	
	public boolean isEmpty() {
		return cards.isEmpty();
	}
	
	public boolean contains(Coordinate c) {
		if (isEmpty()) return false;
		
		for (int i=0; i<nCard; i++) {
			Card card = cards.get(i);
			if (c.x>=card.getCoord().x && c.x<=(card.getCoord().x+Card.WIDTH) 
				&& c.y>=card.getCoord().y && c.y<=(card.getCoord().y+Card.HEIGHT))
				return true;
		}
		return false;
	}
	
	public boolean containsOnLastCard(Coordinate c) {
		if (isEmpty()) return false;
		
		Card card = cards.get(nCard-1);
		if (c.x>=card.getCoord().x && c.x<=(card.getCoord().x+Card.WIDTH) 
			&& c.y>=card.getCoord().y && c.y<=(card.getCoord().y+Card.HEIGHT))
			return true;
		else return false;
	}
	
	public void draw(Canvas canvas) {
		for (Card card : cards)
			card.draw(canvas);
	}
	
	public void saveCurrentState(SharedPreferences.Editor editor, String key) {
		editor.putInt(key + " nCard", nCard);
		editor.putInt(key + " coord.x", coord.x);
		editor.putInt(key + " coord.y", coord.y);
		for (int i=0; i<nCard; i++)
			cards.get(i).saveCurrentState(editor, key + " card" + Integer.toString(i));
		editor.commit();
	}
	
	public void restoreSavedState(SharedPreferences pref, String key) {
		nCard = pref.getInt(key + " nCard", 0);
		coord = new Coordinate();
		coord.x = pref.getInt(key + " coord.x", 1);
		coord.y = pref.getInt(key + " coord.y", 1);
		cards = new ArrayList<Card>();
		Card card;
		for (int i=0; i<nCard; i++) {
			card = new Card();
			card.restoreSavedState(pref, key + " card" + Integer.toString(i));
			cards.add(card);
		}
	}
}
