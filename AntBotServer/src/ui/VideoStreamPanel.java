package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import server.ImageObject;



public class VideoStreamPanel extends JPanel {
	
	VideoStreamCanvas videoStreamCanvas;
	private JPanel labelPanel;
	private JLabel panelLabel;
	
	public VideoStreamPanel(ImageObject imageObject)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(500, 70));
		this.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
		
		labelPanel = new JPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		panelLabel = new JLabel("AntBot Video Stream");
		labelPanel.add(panelLabel);
		
		add(labelPanel);
		
		// Setup video stream
		videoStreamCanvas = new VideoStreamCanvas(imageObject);
		videoStreamCanvas.setPreferredSize(new Dimension(360, 52));
		add(videoStreamCanvas);;
		
	}
	
}
