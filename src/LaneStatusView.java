/**
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

public class LaneStatusView implements ActionListener, LaneObserver, PinsetterObserver {

	private JPanel jp;

	private JLabel curBowler, foul, pinsDown;
	private JButton viewLane;
	private JButton viewPinSetter, maintenance;

	private PinSetterView psv;
	private LaneView lv;
	private Lane lane;
	int laneNum;

	boolean laneShowing;
	boolean psShowing;

	public LaneStatusView(Lane lane, int laneNum ) {

		this.lane = lane;
		this.laneNum = laneNum;

		laneShowing=false;
		psShowing=false;

		psv = new PinSetterView( laneNum );
		Pinsetter ps = lane.getPinsetter();
		ps.subscribe(psv);

		lv = new LaneView( lane, laneNum );
		lane.subscribe(lv);


		jp = new JPanel();
		jp.setLayout(new FlowLayout());
		JLabel cLabel = new JLabel( "Now Bowling: " );
		curBowler = new JLabel( "(no one)" );
		JLabel fLabel = new JLabel( "Foul: " );
		foul = new JLabel( " " );
		JLabel pdLabel = new JLabel( "Pins Down: " );
		pinsDown = new JLabel( "0" );

		// Button Panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		Insets buttonMargin = new Insets(4, 4, 4, 4);

		viewLane = new JButton("View Lane");
		JPanel viewLanePanel = new JPanel();
		viewLanePanel.setLayout(new FlowLayout());
		viewLane.addActionListener(this);
		viewLanePanel.add(viewLane);

		viewPinSetter = new JButton("Pinsetter");
		JPanel viewPinSetterPanel = new JPanel();
		viewPinSetterPanel.setLayout(new FlowLayout());
		viewPinSetter.addActionListener(this);
		viewPinSetterPanel.add(viewPinSetter);

		maintenance = new JButton("     ");
		maintenance.setBackground( Color.GREEN );
		JPanel maintenancePanel = new JPanel();
		maintenancePanel.setLayout(new FlowLayout());
		maintenance.addActionListener(this);
		maintenancePanel.add(maintenance);

		viewLane.setEnabled( false );
		viewPinSetter.setEnabled( false );


		buttonPanel.add(viewLanePanel);
		buttonPanel.add(viewPinSetterPanel);
		buttonPanel.add(maintenancePanel);

		jp.add( cLabel );
		jp.add( curBowler );
//		jp.add( fLabel );
//		jp.add( foul );
		jp.add( pdLabel );
		jp.add( pinsDown );
		
		jp.add(buttonPanel);

	}

	public JPanel showLane() {
		return jp;
	}

	/** actionPerformed()
	 *
	 * Event that's triggered whenever a LaneStatusView button is pressed.
	 * Calls the action of its associated button.
	 *
	 * @param e Button click event
	 */
	public void actionPerformed( ActionEvent e ) {
		if (e.getSource().equals(viewPinSetter)) {
			buttonActionViewPinSetter();
		}
		if (e.getSource().equals(viewLane)) {
			buttonActionViewLane();
		}
		if (e.getSource().equals(maintenance)) {
			buttonActionMaintenance();
		}
	}

	/** buttonActionViewPinSetter()
	 *
	 * Toggle the visibility of the pin setter for this lane.
	 *
	 * @pre: A party is present in this lane
	 */
	private void buttonActionViewPinSetter() {
		if ( lane.isPartyAssigned() ) {
			if (psShowing == false) {
				psv.show();
				psShowing = true;
			} else {
				psv.hide();
				psShowing = false;
			}
		}
	}

	/** buttonActionViewLane()
	 *
	 * View the scoreboard of the lane.
	 *
	 * @pre: A party is present in this lane
	 */
	private void buttonActionViewLane() {
		if ( lane.isPartyAssigned() ) {
			if ( laneShowing == false ) {
				lv.show();
				laneShowing=true;
			} else if ( laneShowing == true ) {
				lv.hide();
				laneShowing=false;
			}
		}
	}

	/** buttonActionMaintenance()
	 *
	 * Flag the lane's call for maintenance as resolved.
	 *
	 * @pre: A party is present in this lane
	 */
	private void buttonActionMaintenance() {
		if ( lane.isPartyAssigned() ) {
			lane.unPauseGame();
			maintenance.setBackground( Color.GREEN );
		}
	}

	public void receiveLaneEvent(LaneEvent le) {
		curBowler.setText( ( (Bowler)le.getBowler()).getNickName() );
		if ( le.isMechanicalProblem() ) {
			maintenance.setBackground( Color.RED );
		}	
		if ( lane.isPartyAssigned() == false ) {
			viewLane.setEnabled( false );
			viewPinSetter.setEnabled( false );
		} else {
			viewLane.setEnabled( true );
			viewPinSetter.setEnabled( true );
		}
	}

	public void receivePinsetterEvent(PinsetterEvent pe) {
		pinsDown.setText( ( new Integer(pe.totalPinsDown()) ).toString() );
//		foul.setText( ( new Boolean(pe.isFoulCommited()) ).toString() );
		
	}

}
