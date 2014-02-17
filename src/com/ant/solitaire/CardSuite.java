package com.ant.solitaire;

import android.graphics.Canvas;

public class CardSuite extends CardGroup {
	public CardSuite() {
		super();
	}
	
	public CardSuite(Coordinate c) {
		super(c);
	}
	
	@Override
	protected Coordinate calculateCoord(int order) {
		Coordinate c = new Coordinate(coord.x, coord.y); 
		return c;
	}
	
	@Override
	public void draw(Canvas canvas) {
		if (!cards.isEmpty())
			cards.get(0).draw(canvas);
	}
}
