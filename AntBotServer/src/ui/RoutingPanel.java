package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

public class RoutingPanel extends JPanel implements ActionListener
{
	ServerController controller;
	
	private JPanel labelPanel;
	private JPanel bottomPanel;
	private JPanel buttonPanel;

	private JLabel panelLabel;
	
	private JScrollPane routeScroll;
	private JTextArea routeCommands;
	
	private JButton clearButton;
	private JButton submitButton;
	
	private JButton routingButton;

	
	public RoutingPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 130));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		
		this.controller = controller;
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Auto-Follow Route");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		// Bottom Panel - everything else
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		routeCommands = new JTextArea(5, 20);
		routeScroll = new JScrollPane(routeCommands);
		routeScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		bottomPanel.add(routeScroll);
		
		// Button panel - the buttons (inside bottom panel)
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(2, 2));
		
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		
		submitButton = new JButton("Submit");
		submitButton.addActionListener(this);
		
		routingButton = new JButton("Start Following");
		routingButton.addActionListener(this);
		
		buttonPanel.add(clearButton);
		buttonPanel.add(submitButton);
		buttonPanel.add(routingButton);
		
		bottomPanel.add(buttonPanel);
		
		add(bottomPanel);
	}
	
	public void actionPerformed(ActionEvent e)
	{
    	if(e.getSource() == clearButton)
    	{
    		routeCommands.setText("");
    	}
    	else if(e.getSource() == submitButton)
    	{
    		// Just send the contents raw - the phone can do the work as we can't send a data structure the way it's set up
    		System.out.println("ROUTE:");
    		System.out.println( routeCommands.getText() );
    		
    		controller.submit_route(routeCommands.getText());
    	}
    	else if(e.getSource() == routingButton)
    	{
    		if( !controller.isRouting() )
    		{
    			controller.submit_start_routing();
    		}
    		else
    		{
    			controller.submit_stop_routing();
    		}
    	}
	}
	
	public void onSuccessfulRoutingStart()
	{
		routingButton.setText("Stop Following");
	}
	
	public void onSuccessfulRoutingStop()
	{
		routingButton.setText("Start Following");
	}
}
