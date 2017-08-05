package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class HomingPanel extends JPanel implements ActionListener
{
	ServerController controller;
	JButton homingButton;
	
	private JPanel labelPanel;
	private JLabel panelLabel;
	
	private JPanel bottomPanel;
	
	
	
	public HomingPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 70));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		
		this.controller = controller;
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Homing");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		homingButton = new JButton("Start Homing");
		homingButton.addActionListener(this);
		bottomPanel.add(homingButton);
		add(bottomPanel);
	}
	
	public void actionPerformed(ActionEvent e)
	{
    	if( e.getSource() == homingButton )
    	{
    		if( !controller.isHoming() )
    		{
    			controller.submit_start_homing();
    		}
    		else
    		{
    			controller.submit_stop_homing();
    		}
    	}
	}
	
	public void onSuccessfulHomingStart()
	{
		homingButton.setText("Stop Homing");
	}
	
	public void onSuccessfulHomingStop()
	{
		homingButton.setText("Start Homing");
	}
}
