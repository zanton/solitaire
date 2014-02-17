package com.ant.solitaire;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class GameThread extends Thread {
	//game state constant
	public static final int MODE_NULL = 0;
	public static final int MODE_READY = 1;
	public static final int MODE_DEALING = 2;
	public static final int MODE_DEALING_ONE = 3;
	public static final int MODE_RUNNING = 4;
    public static final int MODE_HOLDINGON = 5;
    public static final int MODE_GAMEOVER = 6;
    
    public static final int SUITE_NUMBER = 8;
    
    //variables concerning system
	private SurfaceHolder mSurfaceHolder;
	private Context mContext;
	private Handler mHandler;
	private int mMode;
	private boolean mRun;
	private boolean threadKilled;
	
	//variables concerning graphics
	private int mCanvasWidth;
	private int mCanvasHeight;
	private CardGroup[] group; //0->cardbundle, (1-10)-> ten cardgroup, 11->card group which is on move
	private MoveController mMoveController;
	private TouchController mTouchController;
	private SuiteController mSuiteController;
	private ScoreController mScoreController;
	
	public GameThread(SurfaceHolder surfaceholder, Context context, Handler handler) {
		mSurfaceHolder = surfaceholder;
		mContext = context;
		mHandler = handler;
		
		mMoveController = new MoveController();
		mTouchController = new TouchController();
		mSuiteController = new SuiteController();
		mScoreController = new ScoreController();
		mMode = MODE_NULL;
		mRun = false;
		threadKilled = false;
		group = new CardGroup[12];
	}
	
	public boolean setSurfaceSize(int width, int height) {
		synchronized (mSurfaceHolder) {
			boolean flag = ((mCanvasWidth!=width) || (mCanvasHeight!=height))?true:false;
			mCanvasWidth = width;
			mCanvasHeight = height;
			setCardSize(width, height);
			return flag;
		}
	}
	
	private void setCardSize(int w, int h) {
		int unit = w/72;
		Card.WIDTH = unit * 6;
		Card.HEIGHT = (Card.WIDTH * 3) / 2;
		Card.PADDING = Math.round(Card.HEIGHT*18.0f/100.0f);
	}
	
	public void setMode(int newMode) {
		synchronized (mSurfaceHolder) {
			if (mMode==MODE_RUNNING && newMode!=MODE_RUNNING && !mRun)
				setResume();
			mMode = newMode;
			if (newMode==MODE_READY) initReady();
			if (newMode==MODE_DEALING) initDealing();
			if (newMode==MODE_DEALING_ONE) initDealingOne();
			if (newMode==MODE_GAMEOVER) finishGame();
		}
	}
	
	public int getMode() {
		return mMode;
	}
	
	private void initReady() {
		//init group 1->10
		int pad = (mCanvasWidth - 10*Card.WIDTH)/(9+3);
		int offset = Math.round(pad*1.5f);
		for (int i=1; i<=10; i++) {
			Coordinate coord = new Coordinate();
			coord.x = offset + (i-1)*(Card.WIDTH + pad);
			coord.y = offset;
			group[i] = new CardGroup(coord);
		}
		//init group 0
		Coordinate c = new Coordinate();
		c.x = mCanvasWidth - Card.WIDTH - 2*offset;
		c.y = mCanvasHeight - Card.HEIGHT - offset;
		group[0] = new CardBundle(c);
		//init group 11
		group[11] = null;
		
		//set coordinate for SuiteController
		c = new Coordinate();
		c.x = 2*offset;
		c.y = mCanvasHeight - Card.HEIGHT - offset;
		mSuiteController.setCoord(c);
		
		//set coordinate for ScoreController
		c = new Coordinate();
		c.x = offset / 2;
		c.y = offset - 2;
		mScoreController.setCoord(c);
		mScoreController.setTextSize(offset+1);
		
		//get preferences to set level
		SharedPreferences pref = ((Activity) mContext).getPreferences(Activity.MODE_PRIVATE);
		int nsuit = pref.getInt("Level", 1);
		
		//put all cards into group[0]
		int suit = 1;
		for (int i=0; i<SUITE_NUMBER; i++) {
			if (i >= suit*(SUITE_NUMBER/nsuit)) suit++;
			for (int value=1; value<=13; value++)
				group[0].addCard(value, suit, false);
		}
		//shuffle every card into a new position
		group[0].shuffle();
	}
	
	private int dealingTarget;
	private boolean dealingCardState;
	private int dealingCardNumber;
	private int dealingIndex;
	
	private void initDealing() {
		if (group[11] != null) return;
		
		//set attributes of the move
		dealingCardState = false;
		dealingCardNumber = 13*SUITE_NUMBER - ((13*SUITE_NUMBER/2)/10)*10;
		dealingIndex = 0;
		dealingTarget = dealingIndex % 10 + 1;
		
		//get card from group bundle and put into group for moving
		Card card = group[0].getCard();
		card.setUpDown(dealingCardState);
		group[11] = new CardGroup(card.getCoord());
		group[11].addCard(card);
		
		//init new move for mMoveController
		mMoveController.setNewMove(group[dealingTarget]);
	}
	
	private void dealing() {
		//check if the mMoveController has finished the moving job, if so set new card for it to deal
		if (!mMoveController.onMove) {
			//increase the dealingCardOrder and/or dealingTarget
			dealingIndex++;
			if (dealingIndex >= dealingCardNumber) {
				//finish, stop dealing
				setMode(MODE_RUNNING);
				return;
			}
			dealingTarget = dealingIndex % 10 + 1;
			if (dealingIndex >= dealingCardNumber-10)
				dealingCardState = true;
			
			//get card from group bundle and put into group for moving
			Card card = group[0].getCard();
			card.setUpDown(dealingCardState);
			group[11] = new CardGroup(card.getCoord());
			group[11].addCard(card);
			
			//init new move for mMoveController
			mMoveController.setNewMove(group[dealingTarget]);
		}
	}
	
	private void initDealingOne() {
		if (group[11]!=null) return;
				
		//set attributes of the move
		dealingTarget = 1; //group number 1
		dealingCardState = true; //open card
				
		//get card from group bundle and put into group for moving
		Card card = group[0].getCard();
		card.setUpDown(dealingCardState);
		group[11] = new CardGroup(card.getCoord());
		group[11].addCard(card);
				
		//initialize new move for mMoveController
		mMoveController.setNewMove(group[dealingTarget]);
	}
	
	private void dealingOne() {
		//check if the mMoveController has finished the moving job, if so set new card for it to deal
		if (!mMoveController.onMove) {
			//increase the dealingTarget
			dealingTarget++;
			
			//check to finish
			if (dealingTarget > 10) {
				//finish, stop dealing
				setMode(MODE_RUNNING);
				return;
			}
			
			//if not finished yet, get card from group bundle and put into group for moving
			Card card = group[0].getCard();
			card.setUpDown(dealingCardState);
			group[11] = new CardGroup(card.getCoord());
			group[11].addCard(card);
			
			//init new move for mMoveController
			mMoveController.setNewMove(group[dealingTarget]);
		}
	}
	
	public void setPause() {
		synchronized (mSurfaceHolder) {
			setRun(false);
		}
	}
	
	public void setResume() {
		synchronized (mSurfaceHolder) {
			setRun(true);
		}
	}
	
	public void setRun(boolean run) {
		mRun = run;
	}
	
	public void finishGame() {
		setRun(false);
		synchronized (mSurfaceHolder) {
			((GameActivity) mContext).runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((GameActivity) mContext).gameOver(mScoreController.getScore());
				}
			});
		}
	}
	
	public void setNewGame() {
		mSuiteController.reset();
		mScoreController.resetScore();
		setMode(MODE_READY);
    	setMode(MODE_DEALING);
	}
	
	@Override
	public void run() {
		while (!threadKilled) 
			if (mRun) {
				Canvas c = null;
				try {
					c = mSurfaceHolder.lockCanvas(null);
					synchronized (mSurfaceHolder) {
						//for dealing mode
						if (mMode==MODE_DEALING) 
							dealing();
						if (mMode==MODE_DEALING_ONE)
							dealingOne();
						
						//process gathering
						if (mSuiteController.onGathering)
							mSuiteController.run();
						//update move
						if (mMoveController.onMove) {
							mMoveController.updatePosition();
							if (!mMoveController.onMove && mSuiteController.isReadyToCheck)
								mSuiteController.check();
						}
						//draw on canvas
						doDraw(c);
					}
				} finally {
					if (c!=null) 
						mSurfaceHolder.unlockCanvasAndPost(c);
				}
				//if there's no change in canvas, pause game
				if (mMode==MODE_RUNNING && !mSuiteController.onGathering 
						&& !mMoveController.onMove && mTouchController.mTouchMode==TouchController.NO_TOUCH)
					setPause();
			} else {
				try {
					sleep(100);
				} catch(InterruptedException e) {
					//do nothing
				}
			}
	}
	
	public boolean isRunning() {
		if (mMode==MODE_RUNNING) return true;
		else return false;
	}
	
	private void doDraw(Canvas canvas) {
		//draw the surface here
		//background
		//canvas.drawColor(Color.BLACK);
		Drawable bg = CardRes.getBackground();
		bg.setBounds(0, 0, mCanvasWidth, mCanvasHeight);
		bg.draw(canvas);
		
		mSuiteController.draw(canvas);
		for (int i=0; i<=11; i++)
			if (group[i] != null)
				group[i].draw(canvas);
		if (mTouchController.mTouchMode != TouchController.NO_TOUCH)
			drawLightRect(canvas);
		mScoreController.draw(canvas);
	}
	
	private void drawLightRect(Canvas canvas) {
		if (group[11]==null) return;
		
		//Coordinate c1 = new Coordinate(200, 200); 
		int left= group[11].coord.x;
		int top= group[11].coord.y;
		//Coordinate c2 = new Coordinate(250, 250); 
		int right = group[11].getLastCardCoord().x + Card.WIDTH;
		int bottom = group[11].getLastCardCoord().y + Card.HEIGHT;
		
		int pad = Card.PADDING/2;
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setAlpha(170);
		canvas.drawRect(left-pad, top-pad, right+pad, top, paint);
		canvas.drawRect(left-pad, top, left, bottom+pad, paint);
		canvas.drawRect(right, top, right+pad, bottom+pad, paint);
		canvas.drawRect(left, bottom, right, bottom+pad, paint);
	}

	public void doTouchEvent(MotionEvent event) {
		if (!mMoveController.onMove && !mSuiteController.onGathering) {
			setResume();
			mTouchController.doTouchEvent(event);
		}
	}

	public void restoreSavedState() {
		SharedPreferences pref = mContext.getSharedPreferences("LastSurfaceState", Context.MODE_PRIVATE);
		
		for (int i=0; i<=11; i++) {
			String str = "Group" + Integer.toString(i);
			if (pref.getBoolean(str, false)) {
				if (i==0)
					group[0] = new CardBundle();
				else 
					group[i] = new CardGroup();
				group[i].restoreSavedState(pref, str);
			} else 
				group[i] = null;
		}
		//remember to restore mSuiteController before mMoveController
		mSuiteController.restoreSavedState(pref, "SuiteController");
		mMoveController.restoreSavedState(pref, "MoveController");
		mScoreController.restoreSavedState(pref, "ScoreController");
		
		mMode = pref.getInt("mMode", 1);
		mRun = pref.getBoolean("mRun", true);
		dealingTarget = pref.getInt("dealingTarget", 1);
		dealingCardNumber = pref.getInt("dealingCardNumber", 1);
		dealingIndex = pref.getInt("dealingIndex", 1);
		dealingCardState = pref.getBoolean("dealingCardState", false);
		
		//delete the saved state and then mark there's not saved state
		pref.edit().clear().commit();
		pref.edit().putBoolean("SavedState", false).commit();
	}

	public void saveCurrentState() {
		SharedPreferences pref = mContext.getSharedPreferences("LastSurfaceState", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		
		for (int i=0; i<=11; i++)
			if (group[i] != null) {
				editor.putBoolean("Group" + Integer.toString(i), true);
				group[i].saveCurrentState(editor, "Group" + Integer.toString(i));
			}
			else
				editor.putBoolean("Group" + Integer.toString(i), false);
		mMoveController.saveCurrentState(editor, "MoveController");
		mSuiteController.saveCurrentState(editor, "SuiteController");
		mScoreController.saveCurrentState(editor, "ScoreController");
		
		editor.putInt("mMode", mMode);
		editor.putBoolean("mRun", mRun);
		editor.putInt("dealingTarget", dealingTarget);
		editor.putInt("dealingCardNumber", dealingCardNumber);
		editor.putInt("dealingIndex", dealingIndex);
		editor.putBoolean("dealingCardState", dealingCardState);
		
		editor.putBoolean("SavedState", true);
		editor.commit();
	}
	
	public void quit() {
		//finish the thread
		threadKilled = true;
	}

	//class to manage suite of cards, and gather them down
	private class SuiteController {
		public CardSuite[] suite;
		public int nsuite;
		
		public boolean isReadyToCheck;
		public boolean onGathering;
		public Coordinate coord;
		public int targetID;
		public int gatheringIndex;
		
		public SuiteController() {
			//initialize
			isReadyToCheck = false;
			onGathering = false;
			coord = new Coordinate();
			
			suite = new CardSuite[SUITE_NUMBER];
			nsuite = 0;
			
		}
		
		public void setCoord(Coordinate c) {
			coord.x = c.x;
			coord.y = c.y;
		}
		
		//reset to prepare for new game
		public void reset() {
			isReadyToCheck = false;
			onGathering = false;
			nsuite = 0;
		}
		
		//prepare to do checking after the animation finishing
		public void prepareToCheck(int groupID) {
			targetID = groupID;
			isReadyToCheck = true;
		}
		
		//check the group[groupID] is containing a right suite to gather down
		public boolean check() {
			isReadyToCheck = false;
			CardGroup cg = group[targetID];
			int n = cg.size();
			if (n<13) 
				return false;
			
			int suit = cg.viewLastCard().getSuit();
			for (int i=1; i<=13; i++)
				if (cg.viewCard(n-i).getSuit() != suit ||
						cg.viewCard(n-i).getValue() != i ||
						cg.viewCard(n-i).getUpDown() != true)
					return false;
			//found one suite
			initGathering();
			return true;
		}
		
		public Coordinate getCoordinateForNewSuite(int index) {
			Coordinate c = new Coordinate();
			c.x = coord.x + (index-1)*(Card.WIDTH + 5);
			c.y = coord.y;
			return c;
		}
		
		//prepare for gathering animation
		public void initGathering() {
			//check if the MoveController is ready
			if (group[11]!=null) {
				isReadyToCheck = true;
				return;
			}
			
			//set attributes of new gathering process
			nsuite++;
			suite[nsuite-1] = new CardSuite(getCoordinateForNewSuite(nsuite));
			gatheringIndex = 1;
			
			//get card from source group and put into group[11] for moving
			Card card = group[targetID].getCard();
			group[11] = new CardGroup(card.getCoord());
			group[11].addCard(card);
			
			//initialize new move for mMoveController
			mMoveController.setNewMove(suite[nsuite-1]);
			
			//set flag
			onGathering = true;
			
			//add score
			mScoreController.updateScoreMoves(99,0);
		}
		
		public void run() {
			//check if the mMoveController has finished the moving job, if so set new card for it to deal
			if (!mMoveController.onMove) {
				//increase the gatheringIndex
				gatheringIndex++;
				
				//check to finish
				if (gatheringIndex > 13) {
					//finish, stop gathering
					onGathering = false;
					
					//set up state for last card of previous group
					if (!group[targetID].isEmpty()) 
						group[targetID].viewLastCard().setUpDown(true);
				
					//if all suites were collected then finish
					if (nsuite == SUITE_NUMBER)
						setMode(MODE_GAMEOVER);
					
					return;
				}
				
				//if not finished yet, get card from group bundle and put into group for moving
				Card card = group[targetID].getCard();
				group[11] = new CardGroup(card.getCoord());
				group[11].addCard(card);
				
				//init new move for mMoveController
				mMoveController.setNewMove(suite[nsuite-1]);
			}
		}
		
		public void draw(Canvas canvas) {
			for (int i=0; i<nsuite; i++)
				if (suite[i] != null)
					suite[i].draw(canvas);
		}
		
		public void saveCurrentState(SharedPreferences.Editor editor, String key) {
			editor.putInt(key + " nsuite", nsuite);
			editor.putInt(key + " coord.x", coord.x);
			editor.putInt(key + " coord.y", coord.y);
			editor.putInt(key + " targetID", targetID);
			editor.putInt(key + " gatheringIndex", gatheringIndex);
			editor.putBoolean(key + " onGathering", onGathering);
			editor.putBoolean(key + " isReadyToCheck", isReadyToCheck);
			for (int i=0; i<nsuite; i++) {
				String str = key + " Suite" + Integer.toString(i);
				if (suite[i] != null) {
					editor.putBoolean(str, true);
					suite[i].saveCurrentState(editor, str);
				}
				else
					editor.putBoolean(str, false);
			}
		}
		
		public void restoreSavedState(SharedPreferences pref, String key) {
			nsuite = pref.getInt(key + " nsuite", 0);
			coord.x = pref.getInt(key + " coord.x", 1);
			coord.y = pref.getInt(key + " coord.y", 1);			
			targetID = pref.getInt(key + " targetID", 1);
			gatheringIndex = pref.getInt(key + " gatheringIndex", 0);
			onGathering = pref.getBoolean(key + " onGathering", false);
			isReadyToCheck = pref.getBoolean(key + " isReadyToCheck", false);
			for (int i=0; i<nsuite; i++) {
				String str = key + " Suite" + Integer.toString(i);
				if (pref.getBoolean(str, false)) {
					suite[i] = new CardSuite();
					suite[i].restoreSavedState(pref, str);
				} else 
					suite[i] = null;
			}
		}
	}

	private class TouchController {
		//constants for controller state
		public static final int NO_TOUCH = 0;
		public static final int DOWN_FOR_DRAGGING = 1;
		public static final int DOWN_FOR_DEALING = 2;
		public static final int DRAGGING = 3;
		
		//time period before detaching the touched card
		public static final long DOWN_PERIOD = 200; //milliseconds
		
		public int mTouchMode;
		public Coordinate lastPos; //last position of the moving (dragging) group[11]
		public Coordinate buffer_coord; //reserved virtual moving position, while not moving the real card yet, 
		public int mOldGroupID;
		
		public TouchController() {
			//initialize
			mTouchMode = NO_TOUCH;
		}
		
		public void doTouchEvent(MotionEvent event) {
			Log.i("TouchController", "doTouchEvent()");
			synchronized (mSurfaceHolder) {
				if (event.getAction()==MotionEvent.ACTION_DOWN) 
					doTouchDown(event);
				else if (event.getAction()==MotionEvent.ACTION_MOVE)
					doTouchMove(event);
				else if (event.getAction()==MotionEvent.ACTION_UP)
					doTouchUp(event);
			}
		}
		
		public void doTouchDown(MotionEvent event) {
			Log.i("TouchController", "doTouchDown()");
			Coordinate c = new Coordinate((int) event.getX(), (int) event.getY());
			//if the touched position is at one in 10 card groups, prepare for dragging
			for (int i=1; i<=10; i++)
				if (group[i].containsOnLastCard(c)) {
					mTouchMode = DOWN_FOR_DRAGGING;
					lastPos = c;
					detachCard(i);
					buffer_coord = new Coordinate(group[11].coord);
					return;
				}
				else if (group[i].contains(c)) { 
					//if the touched position is in the range of one group, detach a possible group of cards for moving 
					mTouchMode = DRAGGING;
					lastPos = c;
					detachAllPossibleCards(i);
					return;
				}
			//if the touched position is at group[0] (card bundle), prepare for dealing
			if (group[0].containsOnLastCard(c)) {
				mTouchMode = DOWN_FOR_DEALING;
				return;
			}
		}
		
		public void doTouchMove(MotionEvent event) {
			Log.i("TouchController", "doTouchMove()");
			//return if the touch_down action was not approved
			if (mTouchMode==NO_TOUCH) return;
			if (mTouchMode==DOWN_FOR_DEALING) return;
			
			//measure moved distance
			Coordinate c = new Coordinate((int) event.getX(), (int) event.getY());
			int move_x = c.x - lastPos.x;
			int move_y = c.y - lastPos.y;
			lastPos = c;
			
			if (mTouchMode==DOWN_FOR_DRAGGING) {
				if (check1(mOldGroupID)) {
					//not move yet, there is still a card possible to be adapted 
					buffer_coord.x += move_x;
					buffer_coord.y += move_y;
					
					if (checkInRange(buffer_coord, group[11].coord, group[mOldGroupID].viewLastCard().getCoord())) {
						if (checkGetMoreCard(buffer_coord, group[mOldGroupID].viewLastCard().getCoord()))
							detachMoreCard(mOldGroupID);
					} else {
						mTouchMode = DRAGGING;
						group[11].moveto(buffer_coord);
					}
				} else {
					//move along with the finger
					mTouchMode = DRAGGING;
					group[11].move(move_x, move_y);
				}
			}
			if (mTouchMode==DRAGGING)
				group[11].move(move_x, move_y);
		}
		
		public void doTouchUp(MotionEvent event) {
			Log.i("TouchController", "doTouchUp()");
			//return if the touch_down action was not approved
			if (mTouchMode==NO_TOUCH) return;
			
			//treatment after dragging
			if (mTouchMode==DRAGGING || mTouchMode==DOWN_FOR_DRAGGING) {
				//search for nearby group which is close enough to adopt touching card group
				int target = searchNearest(group[11].coord); //range 1..10
				if (target!=0) {
					if (checkAdoptCondition(target)) {
						//move cards to new group
						if (!group[mOldGroupID].isEmpty())
							group[mOldGroupID].viewLastCard().setUpDown(true); //open the last card of old group
						mMoveController.setNewMove(group[target]); //move card to adopting card group (group[11])
						mSuiteController.prepareToCheck(target);
						mScoreController.updateScoreMoves(-1,1); //subtract 1 score
					} else 
						//move back to old group
					    mMoveController.setNewMove(group[mOldGroupID]);
				} else 
					//move back to old group
					mMoveController.setNewMove(group[mOldGroupID]); 
				mTouchMode = NO_TOUCH;
			}
			//start a deal
			if (mTouchMode==DOWN_FOR_DEALING) {
				Coordinate c = new Coordinate((int) event.getX(), (int) event.getY());
				//check if the TOUCH_UP position is still on card bundle
				if (group[0].containsOnLastCard(c))
					setMode(MODE_DEALING_ONE);
				mTouchMode = NO_TOUCH;
			}
			//finally, always set mode back to no_touch
			mTouchMode = NO_TOUCH;
		}

		public boolean check1(int groupID) {
			//check if the group, which is being touched, is not empty and possible to be detached one more card
			if (group[groupID].isEmpty()) return false;
			if (group[groupID].viewLastCard().getUpDown()==true &&
				group[groupID].viewLastCard().getSuit()==group[11].viewFirstCard().getSuit() && 
				group[groupID].viewLastCard().getValue()==group[11].viewFirstCard().getValue()+1)
				return true;
			else return false;
		}
		
		public void detachCard(int groupID) {
			Log.i("TouchController", "detachCard("+groupID+") begins");
			if (group[11]!=null) return;
			
			//get card from group[groupID] and put into group[11]
			Card card = group[groupID].getCard();
			group[11] = new CardGroup(card.getCoord());
			group[11].addCard(card);
			
			//save the old group for returning
			mOldGroupID = groupID;
		}
		
		public void detachAllPossibleCards(int groupID) {
			if (group[11]!=null) return;
			
			//get all possible cards from group[groupID] and put into group[11]
			Card card = group[groupID].getCard();
			group[11] = new CardGroup(card.getCoord());
			group[11].addCard(card);
			while (check1(groupID)) {
				detachMoreCard(groupID);
			}
			
			//save the old group for returning
			mOldGroupID = groupID;
			Log.i("TouchController", "detachCard("+groupID+") finishes");
		}

		//detach the last card of group[groupID] and put it into group[11]
		public void detachMoreCard(int groupID) {
			Log.i("TouchController", "detachCard("+groupID+") begins");
			//get card from group[groupID] and put into group[11]
			Card card = group[groupID].getCard();
			CardGroup cg = new CardGroup(card.getCoord());
			cg.addCard(card);
			while (!group[11].isEmpty())
				cg.addCard(group[11].getFirstCard());
			group[11] = cg;
			
			buffer_coord = new Coordinate(group[11].coord);
			Log.i("TouchController", "detachCard("+groupID+") finishes");
		}
		
		public boolean checkInRange(Coordinate c1, Coordinate c2, Coordinate c3) {
			//kiem tra c1 o trog vog tron tam c2, va trong hinh chu nhat cua c3
			int checking_range = Card.PADDING / 2;
			if (c1.distance(c2) <= checking_range) {
				return true;
			}
			if (c1.y>=c3.y-2*checking_range && c1.y<=c2.y && c1.x>(c3.x-checking_range) && c1.x<(c3.x+checking_range))
				return true;
			return false;
		}
		
		public boolean checkGetMoreCard(Coordinate c1, Coordinate c3) {
			int checking_range = Card.PADDING / 2;
			if (c1.y>=c3.y-2*checking_range && c1.y<=c3.y+checking_range)
				return true;
			else return false;
		}

		public boolean checkAdoptCondition(int groupId) {
			if (group[groupId].isEmpty()) 
				return true;
			Card card1 = group[groupId].viewLastCard();
			Card card2 = group[11].viewFirstCard();
			if (card1.getValue()==card2.getValue()+1)
				return true;
			else return false;
		}
		
		public int searchNearest(Coordinate c) {
			int min = 1;
			for (int i=2; i<=10; i++)
				if (group[i].getLastCardCoord().distance(c) < group[min].getLastCardCoord().distance(c))
					min = i;
			if (group[min].getLastCardCoord().distance(c) <= 35)
				return min;
			else return 0;
		}
	}
	
	private class MoveController {
		public boolean onMove; //true if this class is doing one moving job
		public Coordinate targetCoord;
		public CardGroup targetGroup;
		public int vel;
		public int pixelVelX, pixelVelY; 
		public MoveController() {
			onMove = false;
		}
		
		public void setNewMove(CardGroup target) {
			//set the target for moving card
			targetGroup = target;
			targetCoord = targetGroup.getCoordforNewCard();
			
			//init value for one moving session
			onMove = true;
		}
		
		public void updatePosition() {
			//check if the group[11] already arrive?
			if (checkArrive()) {
				moveCards();
				finishMoving();
				return;
			}

			//calculate velocity
			vel = 12;
			double d = targetCoord.distance(group[11].getCoord()) / 2;
			if (GameThread.this.mMode == MODE_DEALING) 
				d = d/3*4;
			if (d > vel) vel = (int) Math.round(d);
			
			
			//calculate velocity for x-axis and y-axis
			double alpha = Math.atan((targetCoord.y-group[11].getCoord().y)*(1.0f)/(targetCoord.x-group[11].getCoord().x));
			pixelVelX = (int) Math.round(Math.cos(alpha)*vel);
			pixelVelY = (int) Math.round(Math.sin(alpha)*vel);
			if (targetCoord.x < group[11].getCoord().x) {
				pixelVelX *= -1;
				pixelVelY *= -1;
			}
			
			//update position
			group[11].move(pixelVelX, pixelVelY);
		}
		
		public boolean checkArrive() {
			if (targetCoord.distance(group[11].getCoord()) <= 12) return true;
			else return false;
		}
		
		public void moveCards() {
			targetGroup.merge(group[11]);
		}
		
		public void finishMoving() {
			group[11] = null;
			onMove = false;
			targetGroup = null;
			targetCoord = null;
		}
		
		public void saveCurrentState(SharedPreferences.Editor editor, String key) {
			editor.putBoolean(key + " onMove", onMove);
			if (onMove) {
				editor.putInt(key + " targetCoord.x", targetCoord.x);
				editor.putInt(key + " targetCoord.y", targetCoord.y);
				editor.putInt(key + " vel", vel);
			}
		}
		
		public void restoreSavedState(SharedPreferences pref, String key) {
			//restore state
			onMove = pref.getBoolean(key + " onMove", false);
			if (onMove) {
				//restore targetCoord
				if (targetCoord == null) 
					targetCoord = new Coordinate();
				targetCoord.x = pref.getInt(key + " targetCoord.x", 1);
				targetCoord.y = pref.getInt(key + " targetCoord.y", 1);
				vel = pref.getInt(key + " vel", 12);
				
				//restore targetGroup
				int i;
				targetGroup = null;
				//look for targetGroup in group[]
				for (i=1; i<=10; i++)
					if ((group[i] != null) && (group[i].getCoordforNewCard().equals(targetCoord))) {
						targetGroup = group[i];
						break;
					}
				//look for targetGroup in mSuiteController
				if (i>10)
					for (i=0; i<mSuiteController.nsuite; i++)
						if (mSuiteController.suite[i].getCoordforNewCard().equals(targetCoord))
							targetGroup = mSuiteController.suite[i];
				//if no targetGroup was found, stop the onMove state
				if (targetGroup == null)
					onMove = false;
			}
		}
	}

	private class ScoreController {
		public Coordinate coord;
		public int mScore; //current score of game, changed only by function updateScoreMoves()
		public int nMoves; //current number of moves, changed only by function updateScoreMoves()
		public int textSize;
		
		public long lastTime;
		public long count;
		public long fps;
		
		public ScoreController() {
			mScore = 500;
			nMoves = 0;
			lastTime = System.currentTimeMillis();
			count = 0;
			fps = 0;
		}
		
		public void resetScore() {
			mScore = 500;
			nMoves = 0;
		}
		
		public void setCoord(Coordinate c) {
			coord = new Coordinate(c.x, c.y);
		}
		
		public void setTextSize(int text_size) {
			textSize = text_size;
		}
		
		public void updateScoreMoves(int s, int m) {
			mScore += s;
			nMoves += m;
		}
		
		public int getScore() {
			return mScore;
		}
		
		public void draw(Canvas canvas) {
			String str = "Score: "+Integer.toString(mScore)+"   Moves: "+Integer.toString(nMoves);
			//str = str + "   "+calculateFPS()+"fps";
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setTextSize(textSize);
			paint.setAntiAlias(true);
			canvas.drawText(str, coord.x, coord.y, paint);
		}
		
		public String calculateFPS() {
			count++;
			long currentTime = System.currentTimeMillis();
			long period = currentTime-lastTime;
			if (period > 1000) {
				fps = Math.round(count*1000.0/(double) period);
				lastTime = currentTime;
				count = 0;
			}
			return Long.toString(fps);
		}
		
		public void saveCurrentState(SharedPreferences.Editor editor, String key) {
			editor.putInt(key + " mScore", mScore);
			editor.putInt(key + " nMoves", nMoves);
			editor.putInt(key + " textSize", textSize);
			editor.putInt(key + " coord.x", coord.x);
			editor.putInt(key + " coord.y", coord.y);
		}
		
		public void restoreSavedState(SharedPreferences pref, String key) {
			mScore = pref.getInt(key + " mScore", 1);
			nMoves = pref.getInt(key + " nMoves", 1);
			textSize= pref.getInt(key + " textSize", 5);
			if (coord == null)
				coord = new Coordinate();
			coord.x = pref.getInt(key + " coord.x", 1);
			coord.y = pref.getInt(key + " coord.y", 1);
		}
	}
}
