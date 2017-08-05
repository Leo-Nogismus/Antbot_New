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

public class CombinerPanel extends JPanel implements ActionListener
{
	ServerController controller;
	JButton homingButton;
	
	private JPanel labelPanel;
	private JLabel panelLabel;
	
	private JPanel bottomPanel;
	
	public CombinerPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 70));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		this.setBackground(Color.red);
		
		this.controller = controller;

		
		// Top Panel - label
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		labelPanel.setPreferredSize(new Dimension(500, 20));
		
		panelLabel = new JLabel("Combiner");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		// TODO Auto-generated method stub
		
	}
}
