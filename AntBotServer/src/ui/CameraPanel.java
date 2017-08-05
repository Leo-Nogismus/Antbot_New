package ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.opencv.videoio.VideoCapture;

public class CameraPanel extends JPanel implements ActionListener
{
	ServerController controller;
	JButton homingButton;
	
	JPanel controlPanel;
	VideoCanvas videoCanvas;
	
	JButton takeBackgroundButton;
	JButton clearTruthPath;
	
	JButton clearButton;
	JButton setAntBotStartPositionButton;
	
	JButton screenshotButton;
	
	JButton toggleIntegratedPathButton;
	JButton toggleHomeVectorButton;
	JButton toggleTruthPathButton;
	
	boolean settingStartPosition;
	
	boolean showIntegratedPath;
	boolean showTruthPath;
	int showHomeVector;
	
	/**
	 * Constructor
	 */
	public CameraPanel(ServerController controller)
	{
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(640, 549));
		this.setBackground(Color.blue);
		
		showIntegratedPath = true;
		showTruthPath = true;
		showHomeVector = 0;
		
		this.controller = controller;
		
		// Setup video stream
		VideoCapture camera = new VideoCapture(0);
		videoCanvas = new VideoCanvas(camera, this);
		videoCanvas.setPreferredSize(new Dimension(640, 480));
		add(videoCanvas);
		
		// Set up buttons
		controlPanel = new JPanel();
		controlPanel.setPreferredSize(new Dimension(640, 69));
		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		takeBackgroundButton = new JButton("Take Background Photo");
		takeBackgroundButton.addActionListener(this);
		
		setAntBotStartPositionButton = new JButton("Edit AntBot Start Position");
		setAntBotStartPositionButton.addActionListener(this);
		
		clearTruthPath = new JButton("Clear Truth Path");
		clearTruthPath.addActionListener(this);
		
		clearButton = new JButton("Clear Integrated Path");
		clearButton.addActionListener(this);
		
		screenshotButton = new JButton("Take Screenshot");
		screenshotButton.addActionListener(this);
		
		toggleIntegratedPathButton = new JButton("Toggle Integrated Path");
		toggleIntegratedPathButton.addActionListener(this);
		
		toggleTruthPathButton = new JButton("Toggle Truth Path");
		toggleTruthPathButton.addActionListener(this);
		
		toggleHomeVectorButton = new JButton("Toggle Home Vector");
		toggleHomeVectorButton.addActionListener(this);
		
		controlPanel.add(takeBackgroundButton);
		controlPanel.add(setAntBotStartPositionButton);
		controlPanel.add(clearTruthPath);
		controlPanel.add(clearButton);
		controlPanel.add(screenshotButton);
		
		controlPanel.add(toggleIntegratedPathButton);
		controlPanel.add(toggleTruthPathButton);
		controlPanel.add(toggleHomeVectorButton);
		
		controlPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray));
		add(controlPanel);
	}
	
	public void videoCanvasMouseEvent(MouseEvent event)
	{
		if(settingStartPosition)
		{
			if( (event.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK )
			{
				System.out.println("right click");
				setAntbotDirectionPoint( event.getPoint() );
			}
			else if( (event.getModifiers() & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK )
			{
				System.out.println("left click");
				setAntbotStartPoint( event.getPoint() );
			}
		}
	}
	
    public Point getAntbotStartPoint()
    {
    	return controller.getAntbotStartPoint();
    }
    
    public void setAntbotStartPoint(Point point)
    {
    	controller.setAntbotStartPoint(point);
    }
    
    public Point getAntbotDirectionPoint()
    {
    	return controller.getAntbotDirectionPoint();
    }
    
    public void setAntbotDirectionPoint(Point point)
    {
    	controller.setAntbotDirectionPoint(point);
    }
    
    public ArrayList<Point> getPath()
    {
    	return controller.getPath();
    }
	
    public Point getHomeVector()
    {
    	return controller.getHomeVector();
    }
    
    public boolean getShowIntegratedPath()
    {
    	return showIntegratedPath;
    }
    
    public boolean getShowTruthPath()
    {
    	return showTruthPath;
    }
    
    public int getShowHomeVector()
    {
    	return showHomeVector;
    }
    
	/**
	 * 
	 */
	public void actionPerformed(ActionEvent event)
	{
		if(event.getSource() == clearButton)
		{
			controller.clearpath();
		}
		else if( event.getSource() == takeBackgroundButton )
		{
			takeBackgroundButton.setText("Retake Background Photo");
			videoCanvas.takeHomePhoto();
		}
		else if( event.getSource() == clearTruthPath )
		{
			videoCanvas.clearTruthPath();
		}
		else if( event.getSource() == setAntBotStartPositionButton )
		{
			if(!settingStartPosition)
			{
				setAntBotStartPositionButton.setText("Lock AntBot Start Position");
				settingStartPosition = true;
			}
			else
			{
				setAntBotStartPositionButton.setText("Set AntBot Start Position");
				settingStartPosition = false;
			}
		}
		else if( event.getSource() == toggleIntegratedPathButton )
		{
			showIntegratedPath = !showIntegratedPath;
		}
		else if( event.getSource() == toggleHomeVectorButton )
		{
			showHomeVector++;
			showHomeVector = showHomeVector % 3;
			
		}
		else if( event.getSource() == toggleTruthPathButton )
		{
			showTruthPath = !showTruthPath;	
		}
		else if( event.getSource() == screenshotButton )
		{
			videoCanvas.saveCanvas();
		}
	}
}
