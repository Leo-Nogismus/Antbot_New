package ui;

import java.awt.FlowLayout;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

public class ConsolePanel extends JPanel implements ActionListener, KeyListener
{
	ServerController controller;
	
	private JPanel labelPanel;
	private JPanel consolePanel;
	private JPanel buttonPanel;

	private JLabel panelLabel;
	
	private JScrollPane consoleScroll;
	private JTextArea consoleCommands;
	
	private JTextField commandTextField;
	private JButton submitButton;
	
	private String helpText;
	
	public ConsolePanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		this.controller = controller;
		
		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("Console");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		
		// Mid Panel - Console
		consolePanel = new JPanel();
		consolePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		consoleCommands = new JTextArea(15, 39);
		consoleCommands.setEditable(false);
		consoleScroll = new JScrollPane(consoleCommands);
		consoleScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		consoleScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		consolePanel.add(consoleScroll);
		add(consolePanel);
		
		
		// Bottom Panel - Console Commands
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		commandTextField = new JTextField(34);
		commandTextField.addKeyListener(this);
		
		submitButton = new JButton("Submit");
		submitButton.addActionListener(this);
		
		buttonPanel.add(commandTextField);
		buttonPanel.add(submitButton);
		add(buttonPanel);
		
		
		// Help output
		helpText = "COMMANDS:\n" +
				   "turn <angle>\n" +
				   "move <distance>\n" +
				   "head <angle> <distance>\n" +
				   "start <homing/routing>\n" +
				   "stop <homing/routing>\n" +
				   "wheels <left PWM> <left DIR> <right PWM> <right DIR>\n" +
				   "route <cmd 1> <cmd 2> ... <cmd n>\n" +
				   "halt\n" +
				   "";
		
	}

	public void actionPerformed(ActionEvent e)
	{
		if( e.getSource() == submitButton )
		{
			submit_console_command();
		}
	}
	
	private void submit_console_command()
	{
		if( commandTextField.getText().equals("help") )
		{
			consoleCommands.append( helpText );
		}
		else
		{
			controller.submit_string(commandTextField.getText());
			consoleCommands.append( commandTextField.getText() + "\n" );
			commandTextField.setText("");
		}
	}
	
	public void appendToConsole(String newLine)
	{		
		consoleCommands.append( newLine + "\n" );
	}

	public void keyPressed(KeyEvent k)
	{		
		if( k.getKeyCode() == 10 )
		{
			submit_console_command();
		}
	}

	public void keyReleased(KeyEvent arg0)
	{

	}

	public void keyTyped(KeyEvent arg0)
	{
		
	}
}
