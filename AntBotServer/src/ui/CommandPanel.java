package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;


public class CommandPanel extends JPanel implements ActionListener
{
	private ServerController controller;
	
	private JPanel labelPanel;
	private JLabel panelLabel;
	
	private JPanel manualCommandsPanel;
	
	
	
	private JPanel turnPanel;
	private JLabel turnLabel;
	private JTextField turnTextField;
	private JButton turnButton;
	private JButton queueTurnButton;
	
	private JPanel movePanel;
	private JLabel moveLabel;
	private JTextField moveTextField;
	private JButton moveButton;
	private JButton queueMoveButton;
	
	private JButton turnMoveButton;
	private JButton haltButton;
	
	private JButton learnButton;
	
	
	
	public CommandPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 110));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		
		this.controller = controller;
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Movement Commands");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		///////////////////////
		//  Manual Commands  //
		///////////////////////
		
		manualCommandsPanel = new JPanel();
		manualCommandsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		manualCommandsPanel.setPreferredSize(new Dimension(500, 100));
		
		// Turn
		turnPanel = new JPanel();
		turnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		turnPanel.setPreferredSize(new Dimension(500, 32));
		
		turnLabel = new JLabel("Angle:");
		turnPanel.add(turnLabel);
		turnLabel.setPreferredSize(new Dimension(60, 20));
		
		turnTextField = new JTextField();
		turnTextField.addActionListener(this);
		turnTextField.setColumns(4);
		turnPanel.add(turnTextField);
		
		turnButton = new JButton("Turn");
		turnButton.addActionListener(this);
		turnButton.setPreferredSize(new Dimension(100, 26));
		turnPanel.add(turnButton);
		
		//queueTurnButton = new JButton("Queue Turn");
		//queueTurnButton.addActionListener(this);
		//queueTurnButton.setPreferredSize(new Dimension(100, 26));
		//turnPanel.add(queueTurnButton);
		
		manualCommandsPanel.add(turnPanel);
		
		// Move
		movePanel = new JPanel();
		
		moveLabel = new JLabel("Distance:");
		movePanel.add(moveLabel);
		moveLabel.setPreferredSize(new Dimension(60, 20));
		
		moveTextField = new JTextField();
		moveTextField.addActionListener(this);
		moveTextField.setColumns(4);
		movePanel.add(moveTextField);
		
		moveButton = new JButton("Move");
		moveButton.addActionListener(this);
		moveButton.setPreferredSize(new Dimension(100, 26));
		movePanel.add(moveButton);
		
		//queueMoveButton = new JButton("Queue Move");
		//queueMoveButton.addActionListener(this);
		//queueMoveButton.setPreferredSize(new Dimension(100, 26));
		//movePanel.add(queueMoveButton);
				
		manualCommandsPanel.add(movePanel);
		
		//Halt
		
		haltButton = new JButton("STOP");
		haltButton.addActionListener(this);
		manualCommandsPanel.add(haltButton);
	
		//Learn
		
		learnButton = new JButton("LEARN");
		learnButton.addActionListener(this);
		manualCommandsPanel.add(learnButton);
		learnButton.setPreferredSize(new Dimension(100, 26));
		
		
		add(manualCommandsPanel);
		
		
		// Heading
		/*
		turnMoveButton = new JButton("Both");
		turnMoveButton.addActionListener(this);
		manualCommandsPanel.add(turnMoveButton);
		

		*/
		
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try
		{
	    	if(e.getSource() == turnButton )
	    	{
	    		controller.submit_turn( Float.valueOf(turnTextField.getText()) );
	    	}
	    	else if( e.getSource() == moveButton )
	    	{
	    		controller.submit_move( Float.valueOf(moveTextField.getText()) );
	    	}
	    	else if( e.getSource() == turnMoveButton )
	    	{
	    		controller.submit_both( Float.valueOf(turnTextField.getText()), Float.valueOf(moveTextField.getText()) );
	    	}
	    	else if( e.getSource() == haltButton )
	    	{
	    		controller.submit_halt();
	    	}
	    	else if( e.getSource() == learnButton )
	    	{
	    		controller.submit_learn("learn");
	    	}
		}
		catch(NumberFormatException ex)
		{
			System.err.println("Can't submit command - bad format");
		}
		catch(NullPointerException ex)
		{
			System.err.println("Can't submit command - server offline");
		}
	}
}
