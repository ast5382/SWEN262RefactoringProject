
/* $Id$
 *
 * Revisions:
 *   $Log: Lane.java,v $
 *   Revision 1.52  2003/02/20 20:27:45  ???
 *   Fouls disables.
 *
 *   Revision 1.51  2003/02/20 20:01:32  ???
 *   Added things.
 *
 *   Revision 1.50  2003/02/20 19:53:52  ???
 *   Added foul support.  Still need to update laneview and test this.
 *
 *   Revision 1.49  2003/02/20 11:18:22  ???
 *   Works beautifully.
 *
 *   Revision 1.48  2003/02/20 04:10:58  ???
 *   Score reporting code should be good.
 *
 *   Revision 1.47  2003/02/17 00:25:28  ???
 *   Added disbale controls for View objects.
 *
 *   Revision 1.46  2003/02/17 00:20:47  ???
 *   fix for event when game ends
 *
 *   Revision 1.43  2003/02/17 00:09:42  ???
 *   fix for event when game ends
 *
 *   Revision 1.42  2003/02/17 00:03:34  ???
 *   Bug fixed
 *
 *   Revision 1.41  2003/02/16 23:59:49  ???
 *   Reporting of sorts.
 *
 *   Revision 1.40  2003/02/16 23:44:33  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.39  2003/02/16 23:43:08  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.38  2003/02/16 23:41:05  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.37  2003/02/16 23:00:26  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.36  2003/02/16 21:31:04  ???
 *   Score logging.
 *
 *   Revision 1.35  2003/02/09 21:38:00  ???
 *   Added lots of comments
 *
 *   Revision 1.34  2003/02/06 00:27:46  ???
 *   Fixed a race condition
 *
 *   Revision 1.33  2003/02/05 11:16:34  ???
 *   Boom-Shacka-Lacka!!!
 *
 *   Revision 1.32  2003/02/05 01:15:19  ???
 *   Real close now.  Honest.
 *
 *   Revision 1.31  2003/02/04 22:02:04  ???
 *   Still not quite working...
 *
 *   Revision 1.30  2003/02/04 13:33:04  ???
 *   Lane may very well work now.
 *
 *   Revision 1.29  2003/02/02 23:57:27  ???
 *   fix on pinsetter hack
 *
 *   Revision 1.28  2003/02/02 23:49:48  ???
 *   Pinsetter generates an event when all pins are reset
 *
 *   Revision 1.27  2003/02/02 23:26:32  ???
 *   ControlDesk now runs its own thread and polls for free lanes to assign queue members to
 *
 *   Revision 1.26  2003/02/02 23:11:42  ???
 *   parties can now play more than 1 game on a lane, and lanes are properly released after games
 *
 *   Revision 1.25  2003/02/02 22:52:19  ???
 *   Lane compiles
 *
 *   Revision 1.24  2003/02/02 22:50:10  ???
 *   Lane compiles
 *
 *   Revision 1.23  2003/02/02 22:47:34  ???
 *   More observering.
 *
 *   Revision 1.22  2003/02/02 22:15:40  ???
 *   Add accessor for pinsetter.
 *
 *   Revision 1.21  2003/02/02 21:59:20  ???
 *   added conditions for the party choosing to play another game
 *
 *   Revision 1.20  2003/02/02 21:51:54  ???
 *   LaneEvent may very well be observer method.
 *
 *   Revision 1.19  2003/02/02 20:28:59  ???
 *   fixed sleep thread bug in lane
 *
 *   Revision 1.18  2003/02/02 18:18:51  ???
 *   more changes. just need to fix scoring.
 *
 *   Revision 1.17  2003/02/02 17:47:02  ???
 *   Things are pretty close to working now...
 *
 *   Revision 1.16  2003/01/30 22:09:32  ???
 *   Worked on scoring.
 *
 *   Revision 1.15  2003/01/30 21:45:08  ???
 *   Fixed speling of received in Lane.
 *
 *   Revision 1.14  2003/01/30 21:29:30  ???
 *   Fixed some MVC stuff
 *
 *   Revision 1.13  2003/01/30 03:45:26  ???
 *   *** empty log message ***
 *
 *   Revision 1.12  2003/01/26 23:16:10  ???
 *   Improved thread handeling in lane/controldesk
 *
 *   Revision 1.11  2003/01/26 22:34:44  ???
 *   Total rewrite of lane and pinsetter for R2's observer model
 *   Added Lane/Pinsetter Observer
 *   Rewrite of scoring algorythm in lane
 *
 *   Revision 1.10  2003/01/26 20:44:05  ???
 *   small changes
 *
 * 
 */

import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;

public class Lane extends Thread implements PinsetterObserver {	
	private Party party;
	private Pinsetter setter;
	private HashMap scores;
	private Vector subscribers;

	private boolean gameIsHalted;

	private boolean partyAssigned;
	private boolean gameFinished;
	private Iterator bowlerIterator;
	private int ball;
	private int bowlIndex;
	private int frameNumber;
	private boolean tenthFrameStrike;

	private int[] curScores;
	private int[][] cumulScores;
	private boolean canThrowAgain;
	
	private int[][] finalScores;
	private int gameNumber;
	
	private Bowler currentThrower;			// = the thrower who just took a throw

	/** Lane()
	 * 
	 * Constructs a new lane and starts its thread
	 * 
	 * @pre none
	 * @post a new lane has been created and its thered is executing
	 */
	public Lane() { 
		setter = new Pinsetter();
		scores = new HashMap();
		subscribers = new Vector();

		gameIsHalted = false;
		partyAssigned = false;

		gameNumber = 0;

		setter.subscribe( this );
		
		this.start();
	}

	/** run()
	 * 
	 * entry point for execution of this lane 
	 */
	public void run() {
		
		while (true) {
			if (partyAssigned && !gameFinished) {	// we have a party on this lane, 
								// so next bower can take a throw

				// If a maintenance call has been made,
				// idle until the lane is no longer paused
				while (gameIsHalted) {
					shortIdle();
				}

				// Keep on iterating over each bowler for this frame
				if (bowlerIterator.hasNext()) {
					bowlNextFrame();
				}
				// All bowlers have played for this frame; reset for the next frame
				else {
					nextFrame();
				}
			} else if (partyAssigned && gameFinished) {
				gameOver();
			}
			
			// Idle once per loop to free up resources on this thread
			shortIdle();
		}
	}

	/** shortIdle()
	 *
	 * Pause this lane's Thread for a short duration (10 milliseconds)
	 */
	private void shortIdle() {
		try {
			sleep(10);
		} catch (Exception e) {}
	}

	/** bowlNextFrame()
	 *
	 * Bowl out the next frame for the next bowler
	 */
	private void bowlNextFrame() {
		currentThrower = (Bowler)bowlerIterator.next();

		canThrowAgain = true;
		tenthFrameStrike = false;
		ball = 0;
		while (canThrowAgain) {
			setter.ballThrown();		// simulate the thrower's ball hitting
			ball++;
		}

		// If it's the last frame, save the final result for the bowler
		if (frameNumber == 9){
			finalizeScore(bowlIndex, gameNumber);
		}

		// Reset the pins for the next bowler
		setter.reset();
		bowlIndex++;
	}

	/** nextFrame()
	 *
	 * After all bowlers have played for a given frame,
	 * call this method to prepare the lane for the next frame
	 */
	private void nextFrame() {
		frameNumber++;
		resetBowlerIterator();
		bowlIndex = 0;
		if (frameNumber > 9) {
			gameFinished = true;
			gameNumber++;
		}
	}

	/** finalizeScore()
	 *
	 * After a player finishes bowling their 10th frame,
	 * handle all logic for saving their final score
	 *
	 * @param bowlIndex Index in the party of the current bowler
	 * @param gameNumber Current game ID count, in case multiple games are being played
	 */
	private void finalizeScore(int bowlIndex, int gameNumber) {
		finalScores[bowlIndex][gameNumber] = cumulScores[bowlIndex][9];
		try{
			Date date = new Date();
			String dateString = "" + date.getHours() + ":" + date.getMinutes() + " " + date.getMonth() + "/" + date.getDay() + "/" + (date.getYear() + 1900);
			ScoreHistoryFile.addScore(currentThrower.getNick(), dateString, new Integer(cumulScores[bowlIndex][9]).toString());
		} catch (Exception e) {System.err.println("Exception in addScore. "+ e );}
	}

	/** gameOver()
	 *
	 * After the last bowler bowls frame 10, prompt for
	 * whether the party will play another round
	 */
	private void gameOver() {
		// The game has finished; prompt if the bowlers wish to play again
		int result = endGamePrompt();

		// TODO: send record of scores to control desk
		if (result == 1) {					// yes, want to play again
			playAgain();
		} else if (result == 2) {// no, dont want to play another game
			endLane();
		}
	}

	/** endGamePrompt()
	 *
	 * Display the EndGamePrompt GUI class to determine if the bowlers want to play again.
	 *
	 * @return 1 = play again, 2 = end session
	 */
	public int endGamePrompt() {
		EndGamePrompt egp = new EndGamePrompt( ((Bowler) party.getMembers().get(0)).getNickName() + "'s Party" );
		int result = egp.getResult();
		egp.distroy();
		egp = null;

		System.out.println("result was: " + result);

		return result;
	}

	/** playAgain()
	 *
	 * If the bowlers want to play another game, reset the lane
	 */
	public void playAgain() {
		resetScores();
		resetBowlerIterator();
	}

	/** endLane()
	 *
	 * When the bowlers do not want to play another game,
	 * Save and print all information associated with the lane.
	 */
	public void endLane() {
		Vector printVector;
		EndGameReport egr = new EndGameReport( ((Bowler)party.getMembers().get(0)).getNickName() + "'s Party", party);
		printVector = egr.getResult();
		partyAssigned = false;
		Iterator scoreIt = party.getMembers().iterator();
		party = null;
		partyAssigned = false;

		publish(lanePublish());

		int myIndex = 0;
		while (scoreIt.hasNext()){
			Bowler thisBowler = (Bowler)scoreIt.next();
			ScoreReport sr = new ScoreReport( thisBowler, finalScores[myIndex++], gameNumber );
			sr.sendEmail(thisBowler.getEmail());
			Iterator printIt = printVector.iterator();
			while (printIt.hasNext()){
				if (thisBowler.getNick() == (String)printIt.next()){
					System.out.println("Printing " + thisBowler.getNick());
					sr.sendPrintout();
				}
			}

		}
	}
	
	/** recievePinsetterEvent()
	 * 
	 * recieves the thrown event from the pinsetter
	 *
	 * @pre none
	 * @post the event has been acted upon if desiered
	 * 
	 * @param pe 		The pinsetter event that has been received.
	 */
	public void receivePinsetterEvent(PinsetterEvent pe) {
		
			if (pe.pinsDownOnThisThrow() >=  0) {			// this is a real throw
				markScore(currentThrower, frameNumber + 1, pe.getThrowNumber(), pe.pinsDownOnThisThrow());
	
				// next logic handles the ?: what conditions dont allow them another throw?
				// handle the case of 10th frame first
				if (frameNumber == 9) {
					handleTenthFrame(pe);
				} else { // it's not the 10th frame
					handleNormalFrame(pe);
				}
			} else {								//  this is not a real throw, probably a reset
			}
	}

	/** handleNormalFrame()
	 *
	 * Handle all logic regarding frames 1-9.
	 *
	 * @param pe The pinsetter event that has been received by receivePinSetterEvent(...).
	 */
	private void handleNormalFrame(PinsetterEvent pe) {
		if (pe.pinsDownOnThisThrow() == 10) {        // threw a strike
			canThrowAgain = false;
		} else if (pe.getThrowNumber() == 2) {
			canThrowAgain = false;
		} else if (pe.getThrowNumber() == 3) {        // should never occur on frames 1-9
			System.out.println("Unexpected third throw on non-tenth frame...");
		}
	}

	/** handleTenthFrame()
	 *
	 * Handle all regular and edge case logic regarding the final frame.
	 *
	 * @param pe The pinsetter event that has been received by receivePinSetterEvent(...).
	 */
	private void handleTenthFrame(PinsetterEvent pe) {
		if (pe.totalPinsDown() == 10) {
			setter.resetPins();
			if(pe.getThrowNumber() == 1) {
				tenthFrameStrike = true;
			}
		}

		if ((pe.totalPinsDown() != 10) && (pe.getThrowNumber() == 2 && tenthFrameStrike == false)) {
			canThrowAgain = false;
		}

		if (pe.getThrowNumber() == 3) {
			canThrowAgain = false;
		}
	}
	
	/** resetBowlerIterator()
	 * 
	 * sets the current bower iterator back to the first bowler
	 * 
	 * @pre the party as been assigned
	 * @post the iterator points to the first bowler in the party
	 */
	private void resetBowlerIterator() {
		bowlerIterator = (party.getMembers()).iterator();
	}

	/** resetScores()
	 * 
	 * resets the scoring mechanism, must be called before scoring starts
	 * 
	 * @pre the party has been assigned
	 * @post scoring system is initialized
	 */
	private void resetScores() {
		// Reset the scores HashMap variable
		Iterator bowlIt = (party.getMembers()).iterator();
		while ( bowlIt.hasNext() ) {
			int[] toPut = new int[25];
			for ( int i = 0; i != 25; i++){
				toPut[i] = -1;
			}
			scores.put( bowlIt.next(), toPut );
		}
		
		// Reset the cumulScores int[][] variable
		for(int i = 0; i < cumulScores.length; i++) {
			for(int j = 0; j < cumulScores[i].length; j++) {
				cumulScores[i][j] = 0;
			}
		}
		
		gameFinished = false;
		frameNumber = 0;
	}
		
	/** assignParty()
	 * 
	 * assigns a party to this lane
	 * 
	 * @pre none
	 * @post the party has been assigned to the lane
	 * 
	 * @param theParty		Party to be assigned
	 */
	public void assignParty( Party theParty ) {
		party = theParty;
		resetBowlerIterator();
		partyAssigned = true;
		
		curScores = new int[party.getMembers().size()];
		cumulScores = new int[party.getMembers().size()][10];
		finalScores = new int[party.getMembers().size()][128]; //Hardcoding a max of 128 games, bite me.
		gameNumber = 0;
		
		resetScores();
	}

	/** markScore()
	 *
	 * Method that marks a bowlers score on the board.
	 * 
	 * @param Cur		The current bowler
	 * @param frame	The frame that bowler is on
	 * @param ball		The ball the bowler is on
	 * @param score	The bowler's score 
	 */
	private void markScore( Bowler Cur, int frame, int ball, int score ){
		int[] curScore;
		int index =  ( (frame - 1) * 2 + ball);

		curScore = (int[]) scores.get(Cur);

	
		curScore[ index - 1] = score;
		scores.put(Cur, curScore);
		getScore( Cur, frame );
		publish( lanePublish() );
	}

	/** lanePublish()
	 *
	 * Method that creates and returns a newly created laneEvent
	 * 
	 * @return		The new lane event
	 */
	private LaneEvent lanePublish(  ) {
		LaneEvent laneEvent = new LaneEvent(party, bowlIndex, currentThrower, cumulScores, scores, frameNumber+1, curScores, ball, gameIsHalted);
		return laneEvent;
	}

	/** getScore()
	 *
	 * Method that calculates a bowlers score
	 * 
	 * @param Cur		The bowler that is currently up
	 * @param frame	The frame the current bowler is on
	 */
	private void getScore( Bowler Cur, int frame) {
		int[] curScore;

		// Get all scores that the bowler has made so far
		curScore = (int[]) scores.get(Cur);

		// Reset the scores for each of the 10 frames
		for (int i = 0; i < 10; i++){
			cumulScores[bowlIndex][i] = 0;
		}

		// Calculate the cumulative score for each frame
		for (int frameIndex = 0; frameIndex < 10; frameIndex++) {
			// If the first ball of this frame is -1, no more throws
			// have been made; all following scores are 0, leave the for-loop early
			if(curScore[frameIndex * 2] == -1)
				break;

			// Get a reference to the first throw of this frame
			// and the next two throws afterwards
			int[] nextThreeThrows = getThreeBallScores(curScore, frameIndex);
			int frameScore = calcFrameValue(nextThreeThrows);

			// Add the previous frame's score to this frame's points, unless it's frame 1
			if(frameIndex > 0)
				cumulScores[bowlIndex][frameIndex] = cumulScores[bowlIndex][frameIndex - 1] + frameScore;
			else
				cumulScores[bowlIndex][0] = frameScore;
		}
	}

	/** calcFrameValue()
	 *
	 * Given 3 throws, calculate the points earned in a single frame.
	 *
	 * @param nextThreeThrows int[3] of the scores for the first throw of a frame
	 *                        and the two throws following it
	 * @return Points earned in that frame
	 */
	private int calcFrameValue(int nextThreeThrows[]) {
		// Unpack the data structure into more identifiable names
		int firstBallPins = nextThreeThrows[0];
		int secondBallPins = nextThreeThrows[1];
		int thirdBallPins = nextThreeThrows[2];

		// No ball has been thrown yet, return early
		if(firstBallPins == -1)
			return 0;

		// If the second ball hasn't been thrown yet,
		// just return the first ball's value for now
		if(secondBallPins == -1)
			return firstBallPins;

		// If the third ball hasn't been thrown yet,
		// just return the first two throws for now
		if(thirdBallPins == -1)
			return firstBallPins + secondBallPins;

		// Strike!
		if(firstBallPins == 10) {
			// Already verified above that all 3 balls have been thrown
			// Return strike points: 10 + next two throws
			return 10 + secondBallPins + thirdBallPins;
		}
		// Spare
		else if(firstBallPins + secondBallPins == 10) {
			// Same functionality as strike code,
			// but logic split up for easier readability
			return 10 + thirdBallPins;
		}

		// Not a strike or spare, return only first + second values
		return firstBallPins + secondBallPins;
	}

	/** getThreeBallScores()
	 *
	 * Given a score set and index to a particular frame,
	 * return an array of the first throw on frame n and the following 2 throws.
	 * This simplifies the logic of skipping due to strikes.
	 * (e.g. [10, -1, 3, 4, ...] -> [10, 3, 4])
	 *
	 * @param curScore integer array of all 21 throws for a given bowler
	 * @param frameIndex Index of the frames to return scores for (0-9, not 1-10)
	 * @return int[3] of the scores for the first throw of a frame
	 * and the two throws following it, with -1 for non-thrown balls
	 */
	private int[] getThreeBallScores(int[] curScore, int frameIndex) {
		// Initialize all values to -1, which represents a ball not thrown
		int[] points = {-1, -1, -1};
		int ballCount = 0;

		// Get up to 3 point values that aren't -1
		for(int i = frameIndex * 2; i < curScore.length && ballCount < 3; i++) {
			// This ball has been thrown; count its point value
			if(curScore[i] != -1) {
				points[ballCount] = curScore[i];
				ballCount++;
			}
		}

		return points;
	}

	/** isPartyAssigned()
	 * 
	 * checks if a party is assigned to this lane
	 * 
	 * @return true if party assigned, false otherwise
	 */
	public boolean isPartyAssigned() {
		return partyAssigned;
	}
	
	/** isGameFinished
	 * 
	 * @return true if the game is done, false otherwise
	 */
	public boolean isGameFinished() {
		return gameFinished;
	}

	/** subscribe
	 * 
	 * Method that will add a subscriber
	 * 
	 * @param subscribe	Observer that is to be added
	 */

	public void subscribe( LaneObserver adding ) {
		subscribers.add( adding );
	}

	/** unsubscribe
	 * 
	 * Method that unsubscribes an observer from this object
	 * 
	 * @param removing	The observer to be removed
	 */
	
	public void unsubscribe( LaneObserver removing ) {
		subscribers.remove( removing );
	}

	/** publish
	 *
	 * Method that publishes an event to subscribers
	 * 
	 * @param event	Event that is to be published
	 */

	public void publish( LaneEvent event ) {
		if( subscribers.size() > 0 ) {
			Iterator eventIterator = subscribers.iterator();
			
			while ( eventIterator.hasNext() ) {
				( (LaneObserver) eventIterator.next()).receiveLaneEvent( event );
			}
		}
	}

	/**
	 * Accessor to get this Lane's pinsetter
	 * 
	 * @return		A reference to this lane's pinsetter
	 */

	public Pinsetter getPinsetter() {
		return setter;	
	}

	/**
	 * Pause the execution of this game
	 */
	public void pauseGame() {
		gameIsHalted = true;
		publish(lanePublish());
	}
	
	/**
	 * Resume the execution of this game
	 */
	public void unPauseGame() {
		gameIsHalted = false;
		publish(lanePublish());
	}

}
