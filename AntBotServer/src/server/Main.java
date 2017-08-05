package server;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ui.ServerController;
import ui.ServerView;

public class Main
{
	public static void main(String[] args)
	{
		// Set the look and feel
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e){e.printStackTrace();}
		catch (InstantiationException e){e.printStackTrace();}
		catch (IllegalAccessException e){e.printStackTrace();}
		catch (UnsupportedLookAndFeelException e){e.printStackTrace();}
		
		//SetUp ImageModel for Phone transmission
		ImageObject imageObject = new ImageObject();
		
		// Set up model
		ServerModel serverModel = new ServerModel(imageObject);
		
		// Set up controller
		ServerController serverController = new ServerController(serverModel);
		
		// Set up view
		ServerView serverView = new ServerView(serverController);
		serverModel.addObserver(serverView);
	}
}
