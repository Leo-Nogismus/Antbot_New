package ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.opencv.core.Core;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import server.ImageObject;
import server.ReceiveStringThread;


public class ServerView implements Observer
{
    private JFrame frame;
    private ConnectPanel connectPanel;
    private CommandPanel commandPanel;
    private HomingPanel homingPanel;
    private RoutingPanel routingPanel;
    private ConsolePanel consolePanel;
    private CameraPanel cameraPanel;
    private CombinerPanel combinerPanel;
    private ServerController controller;
    private VideoStreamPanel videoStreamPanel;
    
    private JPanel controlPanel;
    private JPanel cameraControlPanel;
    
    private static String lastMessage;
    
    ImageObject imageObject;
    
    public ServerView(ServerController controller)
    {
    	this.controller = controller;
    	imageObject = controller.imageObject;
    	setUpGUI();
    }
    
    /**
     * For the purposes of having clean code, this is in a separate function
     */
    private void setUpGUI()
    {
    	// Make a new frame
        frame = new JFrame("AntBot Remote Control Server");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        
        // Grab the content container
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
        
        // Group Panels
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        controlPanel.setPreferredSize(new Dimension(500, 549));
        
        cameraControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        cameraControlPanel.setPreferredSize(new Dimension(640, 549));
        
        
        /**************************
  				Camera Panel
         **************************/
        
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		cameraPanel = new CameraPanel(controller);

		cameraControlPanel.add(cameraPanel);
		contentPane.add(cameraControlPanel);
        
        
        /**************************
          		Control Panel
         **************************/
        
        
               
        // Add the panel containing the server start/stop
        connectPanel = new ConnectPanel(controller);
        controlPanel.add(connectPanel);
        
        // Add the panel containing the server start/stop
        commandPanel = new CommandPanel(controller);
        controlPanel.add(commandPanel);
        
        // Add the panel containing the server start/stop
        homingPanel = new HomingPanel(controller);
        controlPanel.add(homingPanel);
        
        // Add the panel containing the server start/stop
        routingPanel = new RoutingPanel(controller);
        controlPanel.add(routingPanel);
        
        //Add phone video stream
        videoStreamPanel = new VideoStreamPanel(imageObject);
        controlPanel.add(videoStreamPanel);
        
        // Add the panel containing the server start/stop
        //consolePanel = new ConsolePanel(controller);
        //controlPanel.add(consolePanel);        
        // Combiner Panel
        //combinerPanel = new CombinerPanel(controller);
        //controlPanel.add(combinerPanel);
        
        contentPane.add(controlPanel);

        /**************************
  				Finalise
         **************************/

        // Finalise
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.repaint();
    }

    /**
     * Repaint the frame when notifyObservers() is called
     */
	public void update(Observable arg0, Object arg1)
	{
		if( controller.serverOnline() )
		{
			connectPanel.onSuccessfulServerSetup();
		}
		else
		{
			connectPanel.onServerStop();
		}
		
		if( controller.isHoming() )
		{
			homingPanel.onSuccessfulHomingStart();
		}
		else
		{
			homingPanel.onSuccessfulHomingStop();
		}
		
		/*
		if( controller.isRouting() )
		{
			routingPanel.onSuccessfulRoutingStart();
		}
		else
		{
			routingPanel.onSuccessfulRoutingStop();
		}
		*/
		
		if( controller.getLastMessage() != lastMessage )
		{
			try
			{
				lastMessage = controller.getLastMessage();
				consolePanel.appendToConsole(lastMessage);
			}
			catch(NullPointerException e)
			{
				
			}
		}
		
		frame.repaint();
	}
}
