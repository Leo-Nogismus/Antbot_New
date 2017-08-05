package ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import server.ImageObject;
import server.ServerModel;

/**
 * Checks if the commands being issued are valid (when applicable).
 * 
 * Most of the other commands are redundant, simply calling something in the model - they exist
 * basically to do a proper MVC implementation.
 */
public class ServerController
{
	ServerModel model;
	public ImageObject imageObject;
	
	public ServerController(ServerModel model)
	{
		this.model = model;
		this.imageObject = model.imageObject;
	}
	
	public boolean serverOnline()
	{
		return model.isOnline();
	}
	
	public void startServer()
	{
		model.startServer();
	}
	
	public void stopServer()
	{
		model.stopServer();
	}
	
	public void setPort(int port)
	{
		if(port > 65535 || port < 0)
		{
			
		}
		else
		{
			model.setPort(port);
		}
	}
	
	public void submit_turn(float angle)
	{
		model.sendString("Serialturn " + angle);
	}
	
	public void submit_move(float distance)
	{
		model.sendString("Serialmove " + distance);
	}
	
	public void submit_both(float angle, float distance)
	{
		model.sendString("Serialhead " + angle + " " + distance);
	}
	
	public void submit_halt()
	{
		model.sendString("Serialhalt");
	}
	
	public void submit_start_homing()
	{
		model.sendString("start homing");
	}
	
	public void submit_stop_homing()
	{
		model.sendString("stop homing");
	}
	
	public void submit_route(String route)
	{
		model.sendString("route " + route.replaceAll("[\n\r]", " "));
	}
	
	public void submit_start_routing()
	{
		model.sendString("start routing");
	}
	
	public void submit_stop_routing()
	{
		model.sendString("stop routing");
	}
	
	public void submit_learn(String message)
	{
		model.sendString("learn");
	}
	
	public void submit_string(String message)
	{
		model.sendString(message);
	}
	
	
	
	public boolean isHoming()
	{
		return model.isHoming();
	}
	
    public boolean isRouting()
    {
    	return model.isRouting();
    }
    
    public String getLastMessage()
    {
    	return model.getLastMessage();
    }
    
    public Point getAntbotStartPoint()
    {
    	return model.getAntbotStartPoint();
    }
    
    public void setAntbotStartPoint(Point point)
    {
    	model.setAntbotStartPoint(point);
    }
    
    public Point getAntbotDirectionPoint()
    {
    	return model.getAntbotDirectionPoint();
    }
    
    public void setAntbotDirectionPoint(Point point)
    {
    	model.setAntbotDirectionPoint(point);
    }
    
    public ArrayList<Point> getPath()
    {
    	return model.getPath();
    }
    
    public void clearpath()
    {
    	model.clearPath();
    }
    
    public Point getHomeVector()
    {
    	return model.getHomeVector();
    }
}
