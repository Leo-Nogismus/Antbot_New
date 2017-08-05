package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.BindException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;


public class ConnectPanel extends JPanel implements ActionListener
{
	private JLabel portLabel;
	private JTextField portTextField;
	private JButton startButton;
	
	private JPanel labelPanel;
	private JPanel bottomPanel;
	
	private JLabel panelLabel;
	private ServerController controller;
	
	
	/**
	 * Constructor
	 */
	public ConnectPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 70));
		
		this.controller = controller;
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Server Control");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		// Bottom Panel - the actual UI
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		portLabel = new JLabel("Port: ");
		bottomPanel.add(portLabel);
		
		portTextField = new JTextField("8080");
		portTextField.addActionListener(this);
		portTextField.setColumns(4);
		bottomPanel.add(portTextField);
		
		startButton = new JButton("Search for AntBot");
		startButton.addActionListener(this);
		bottomPanel.add(startButton);
		
		add(bottomPanel);
	}
	
	/**
	 * Action Performed Listener
	 */
	public void actionPerformed(ActionEvent e)
	{
    	if (e.getSource() == startButton )
    	{
			if( !controller.serverOnline())
			{
				// Server is offline - start it
				controller.setPort( Integer.valueOf(portTextField.getText()) );
				controller.startServer();
			}
			else
			{
				controller.stopServer();
			}
    	}
	}
	
	/**
	 * 
	 */
	public void onSuccessfulServerSetup()
	{
		portTextField.setEditable(false);
		portTextField.setFocusable(false);
		startButton.setText("Disconnect AntBot");
	}
	
	public void onServerStop()
	{
		portTextField.setEditable(true);
		startButton.setText("Search for AntBot");
	}
}
