/*
 *  constructs a prototype Lane View
 *
 */

import java.awt.*;
import java.awt.event.*;
import javax.naming.OperationNotSupportedException;
import javax.swing.*;
import java.util.*;

public class LaneView implements LaneObserver, ActionListener {

	private int roll;
	private boolean initDone = true;

	JFrame frame;
	Container cpanel;
	Vector bowlers;
	int cur;
	Iterator bowlIt;

	JPanel[][] balls;
	JLabel[][] ballLabel;
	JPanel[][] scores;
	JLabel[][] scoreLabel;
	JPanel[][] ballGrid;
	JPanel[] pins;

	JButton maintenance;
	Lane lane;

	public LaneView(Lane lane, int laneNum) {

		this.lane = lane;

		initDone = true;
		frame = new JFrame("Lane " + laneNum + ":");
		cpanel = frame.getContentPane();
		cpanel.setLayout(new BorderLayout());

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				frame.hide();
			}
		});

		cpanel.add(new JPanel());

	}

	public void show() {
		frame.show();
	}

	public void hide() {
		frame.hide();
	}

	private JPanel makeFrame(Party party) {

		initDone = false;
		bowlers = party.getMembers();
		int numBowlers = bowlers.size();

		JPanel panel = new JPanel();

		panel.setLayout(new GridLayout(0, 1));

		balls = new JPanel[numBowlers][23];
		ballLabel = new JLabel[numBowlers][23];
		scores = new JPanel[numBowlers][10];
		scoreLabel = new JLabel[numBowlers][10];
		ballGrid = new JPanel[numBowlers][10];
		pins = new JPanel[numBowlers];

		for (int i = 0; i != numBowlers; i++) {
			for (int j = 0; j != 23; j++) {
				ballLabel[i][j] = new JLabel(" ");
				balls[i][j] = new JPanel();
				balls[i][j].setBorder(
					BorderFactory.createLineBorder(Color.BLACK));
				balls[i][j].add(ballLabel[i][j]);
			}
		}

		for (int i = 0; i != numBowlers; i++) {
			for (int j = 0; j != 9; j++) {
				ballGrid[i][j] = new JPanel();
				ballGrid[i][j].setLayout(new GridLayout(0, 3));
				ballGrid[i][j].add(new JLabel("  "), BorderLayout.EAST);
				ballGrid[i][j].add(balls[i][2 * j], BorderLayout.EAST);
				ballGrid[i][j].add(balls[i][2 * j + 1], BorderLayout.EAST);
			}
			int j = 9;
			ballGrid[i][j] = new JPanel();
			ballGrid[i][j].setLayout(new GridLayout(0, 3));
			ballGrid[i][j].add(balls[i][2 * j]);
			ballGrid[i][j].add(balls[i][2 * j + 1]);
			ballGrid[i][j].add(balls[i][2 * j + 2]);
		}

		for (int i = 0; i != numBowlers; i++) {
			pins[i] = new JPanel();
			pins[i].setBorder(
				BorderFactory.createTitledBorder(
					((Bowler) bowlers.get(i)).getNick()));
			pins[i].setLayout(new GridLayout(0, 10));
			for (int k = 0; k != 10; k++) {
				scores[i][k] = new JPanel();
				scoreLabel[i][k] = new JLabel("  ", SwingConstants.CENTER);
				scores[i][k].setBorder(
					BorderFactory.createLineBorder(Color.BLACK));
				scores[i][k].setLayout(new GridLayout(0, 1));
				scores[i][k].add(ballGrid[i][k], BorderLayout.EAST);
				scores[i][k].add(scoreLabel[i][k], BorderLayout.SOUTH);
				pins[i].add(scores[i][k], BorderLayout.EAST);
			}
			panel.add(pins[i]);
		}

		initDone = true;
		return panel;
	}

	public void receiveLaneEvent(LaneEvent le) {
		if (lane.isPartyAssigned()) {
			int numBowlers = le.getParty().getMembers().size();
			while (!initDone) {
				//System.out.println("chillin' here.");
				try {
					Thread.sleep(1);
				} catch (Exception e) {
				}
			}

			if (le.getFrameNum() == 1
				&& le.getBall() == 0
				&& le.getIndex() == 0) {
				System.out.println("Making the frame.");
				cpanel.removeAll();
				cpanel.add(makeFrame(le.getParty()), "Center");

				// Button Panel
				JPanel buttonPanel = new JPanel();
				buttonPanel.setLayout(new FlowLayout());

				Insets buttonMargin = new Insets(4, 4, 4, 4);

				maintenance = new JButton("Maintenance Call");
				JPanel maintenancePanel = new JPanel();
				maintenancePanel.setLayout(new FlowLayout());
				maintenance.addActionListener(this);
				maintenancePanel.add(maintenance);

				buttonPanel.add(maintenancePanel);

				cpanel.add(buttonPanel, "South");

				frame.pack();

			}

			// Get the frame scores from the LaneEvent
			int[][] leCumulScores = le.getCumulScore();

			// Get the ball scores from the LaneEvent
			int[][] leBallScores = new int[bowlers.size()][];
			for(int i = 0; i < bowlers.size(); i++) {
				// Convert the event's hashmap into integer arrays
				leBallScores[i] = (int[])(le.getScore().get(bowlers.get(i)));
			}

			// Update all numberic scorecard labels
			calcAllScorecardLabels(leCumulScores, leBallScores);
		}
	}

	/** calcAllScorecardLabels()
	 *
	 * Update the cumulative and ball scores for all bowlers
	 *
	 * @param leCumulScores 2D array of cumulative frame scores for all bowlers
	 * @param leBallScores 2D array of individual ball scores for all bowlers
	 */
	public void calcAllScorecardLabels(int[][] leCumulScores, int[][] leBallScores) {
		// Calculate all values for each bowler
		for (int k = 0; k < leCumulScores.length; k++) {
			// For this bowler, update the labels for each frame's score
			updateBowlerFrameLabels(leCumulScores[k], k);

			// For this bowler, update the ball score labels
			updateBowlerBallLabels(leBallScores[k], k);
		}
	}

	/** updateBowlerFrameLabels()
	 *
	 * Update the JFrame variable for a given bowler
	 * to match their associated score sheet
	 *
	 * @param bowlerFrameScores Cumulative scores for the associated bowler
	 * @param bowlIndex Index of the target bowler for updating scoreLabel
	 */
	private void updateBowlerFrameLabels(int[] bowlerFrameScores, int bowlIndex) {
		for (int i = 0; i < 10; i++) {
			if (bowlerFrameScores[i] != 0)
				scoreLabel[bowlIndex][i].setText("" + bowlerFrameScores[i]);
			else
				scoreLabel[bowlIndex][i].setText(" ");
		}
	}

	/** updateBowlerBallLabels()
	 *
	 * For the indexed bowler, update the JFrame variable
	 * to display the bowler's individual throws
	 *
	 * @param bowlerBallScores Individual throw scores for the associated bowler
	 * @param bowlIndex Index of the target bowler for updating ballLabel
	 */
	private void updateBowlerBallLabels(int[] bowlerBallScores, int bowlIndex) {
		// Update the label for all 10 frames (2 balls per frame, +1 for frame 3)
		for (int i = 0; i < 21; i++) {
			// If the ball hasn't been thrown, display an empty cell
			if(bowlerBallScores[i] == -1) {
				ballLabel[bowlIndex][i].setText(" ");
				continue;
			}

			// First half (or third for frame 10)
			// Either a strike or normal number
			if(i % 2 == 0) {
				if(bowlerBallScores[i] == 10)
					ballLabel[bowlIndex][i].setText("X");
				else
					ballLabel[bowlIndex][i].setText("" + bowlerBallScores[i]);
			}
			// Second half; either a spare or normal number
			else {
				if(bowlerBallScores[i] + bowlerBallScores[i - 1] == 10)
					ballLabel[bowlIndex][i].setText("/");
				else
					ballLabel[bowlIndex][i].setText("" + bowlerBallScores[i]);
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(maintenance)) {
			lane.pauseGame();
		}
	}

}
