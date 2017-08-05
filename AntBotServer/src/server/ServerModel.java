package server;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

public class ServerModel extends Observable
{
    private static int portNumber = 8080;
    private boolean online;
    private boolean antbotIsHoming;
    private boolean antbotIsRouting;
    
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static PrintWriter clientSocketOut;
    private static BufferedReader clientSocketIn;
    
    private static String lastMessage;
    
    double x;
    double y;
    double antbotOrientation;
    
    int currentVisionError;
    
	Point antbotStartPoint;
	Point antbotDirectionPoint;
	double antbotStartOrientation;
	ArrayList<Point> path;
	double correctionRotation;
    
	Point homePoint;
	Point lastPoint;
	
    // For the nested class
    private static ServerModel me;
    public ImageObject imageObject;
    
    public ServerModel(ImageObject imageObject)
    {
    	this.imageObject = imageObject;
    	online = false;
    	antbotIsHoming = false;
    	antbotIsRouting = false;
    	me = this;
    	
    	
    	homePoint = new Point();
    	lastPoint = new Point();
    	//antbotStartPoint = new Point(550, 404);
    	//antbotDirectionPoint = new Point(500, 404);
    	
    	antbotStartPoint = new Point(284, 231);
    	antbotDirectionPoint = new Point(284, 231);
    	
    	// Set initial orientation
    	antbotStartOrientation = Math.toRadians(360);
    	antbotOrientation = antbotStartOrientation;
    	
    	x = antbotStartPoint.x;
    	y = antbotStartPoint.y;
    	
    	// Makes 0 degree point down, this roughly lines up a 'normal' notion of how coords work with how the screen works
    	correctionRotation = Math.toRadians(0);
    	
    	path = new ArrayList<Point>();
    }
    
    //////////////////////////////
    //		Status & Setup		//
    //////////////////////////////
    
    public void setPort(int newPort)
    {
    	portNumber = 8080;
    }
    
    public boolean isOnline()
    {
    	return online;
    }
    
    public void sendString(String message)
    {
    	System.out.println("ServerModel sendString " + message);
    	
    	lastMessage = message;
    	
    	try
    	{
    		clientSocketOut.println(message + "\n");
    	}
    	catch(NullPointerException e)
    	{
    		System.err.println("Can't sendString - no client");
    	}
    	
		setChanged();
		notifyObservers();
    }
    
    public void setHoming(boolean newHoming)
    {
    	antbotIsHoming = newHoming;
        
		setChanged();
		notifyObservers();
    }
    
    public boolean isHoming()
    {
    	return antbotIsHoming;
    }
    
    public void setRouting(boolean newRouting)
    {
    	antbotIsRouting = newRouting;
        
		setChanged();
		notifyObservers();
    }
    
    public boolean isRouting()
    {
    	return antbotIsRouting;
    }
    
    public void startServer()
    {
    	System.out.println("ServerModel startServer");
    	new Thread(new startServerThread()). start();
    }
    
    public void stopServer()
    {
    	try
    	{
    		// Close sockets
    		serverSocket.close();
    		clientSocket.close();
			
			// Close buffers
			clientSocketOut.close();
			clientSocketIn.close();
			
			// flag as offline
			online = false;
			
			antbotIsHoming = false;
			antbotIsRouting = false;
			
			// update UI
			setChanged();
			notifyObservers();
		}
    	catch (IOException e)
    	{
			e.printStackTrace();
		}
    }
    
    public String getLastMessage()
    {
    	return lastMessage;
    }
    
    public void setCurrentVisionError(int newError)
    {
    	currentVisionError = newError;
    }
    
    public int getCurrentVisionError()
    {
    	return currentVisionError;
    }

    ///////////////////////////
    //		Video Canvas     //
    ///////////////////////////
    
    public Point getAntbotStartPoint()
    {
    	return antbotStartPoint;
    }
    
    public void setAntbotStartPoint(Point point)
    {
    	antbotStartPoint.setLocation(point);
    	lastPoint.setLocation(antbotStartPoint);
    	
    	x = antbotStartPoint.x;
    	y = antbotStartPoint.y;
    	
    	System.out.println("Start Point set to " + antbotStartPoint.x + ", " + antbotStartPoint.y);
    }
    
    public Point getAntbotDirectionPoint()
    {
    	return antbotDirectionPoint;
    }
    
    public void setAntbotDirectionPoint(Point point)
    {
    	antbotDirectionPoint = point;
    	
    	if(antbotStartPoint != null)
    	{
    		double dx = antbotStartPoint.getX() - antbotDirectionPoint.getX();
    		double dy = antbotStartPoint.getY() - antbotDirectionPoint.getY();
    		
    		double angle = Math.atan2(dx, dy);
    		angle -= Math.PI;
    		angle *= -1;
    		
    		antbotStartOrientation = angle;
    		antbotOrientation = angle;
    		
    		
    		System.out.println("Start Orientation set to " + Math.toDegrees(angle));
    	}
    }
    
    public ArrayList<Point> getPath()
    {
    	return path;
    }
    
    public void clearPath()
    {
    	path.clear();
    	x = antbotStartPoint.x;
    	y = antbotStartPoint.y;
    	antbotOrientation = antbotStartOrientation;
    	
    	lastPoint.x = antbotStartPoint.x;
    	lastPoint.y = antbotStartPoint.y;
    }
    
    
    /**
     * The idea is that to begin with the new point is worked out in canvas space, i.e. within the canvas object that
     * the frame is drawn on. Here, x is from left to right and y is from top to bottom. '0' degrees is pointing down.
     * 
     * This is then rotated about the start point
     */
    public synchronized void updatePosition(double deltaDist)
    {
        // Work out difference in position
        double dx = deltaDist * Math.sin( antbotOrientation + correctionRotation );
        double dy = deltaDist * Math.cos( antbotOrientation + correctionRotation );

        System.out.println("antbotOrientation: " + Math.toDegrees(antbotOrientation) );
        System.out.println("correctionRotation: " + Math.toDegrees(correctionRotation) );
        
        // Convert dx and dy into pixels
        dx *= -145;
        dy *= 145;

        System.out.println("(pixl) dx: " + dx + " dy: " + dy);
        System.out.println("(init) x: " + x + " y: " + y);
        
        // update position vector
        x += dx;
        y += dy;
    	
        System.out.println(" (add) x: " + x + " y: " + y);
    	lastPoint.setLocation(x, y);
		
    	// To make the function below easier to read
    	double angle = Math.toRadians(270);
    	double centreX = antbotStartPoint.getX();
    	double centreY = antbotStartPoint.getY();
    	
    	//double rx = centreX + Math.cos(angle)*(x - centreX) - Math.sin(angle)*(y - centreY);
    	//double ry = centreY + Math.sin(angle)*(x - centreX) - Math.cos(angle)*(y - centreY);
	
    	int nx = (int) x;
    	int ny = (int) y;
    	
    	path.add(new Point(nx, ny));
    	
    	System.out.println("fx: " + path.get(path.size()-1).x + " fy: " + path.get(path.size()-1).y);
    }
    
    public void updateOrientation(double deltaAngle)
    {
    	antbotOrientation += Math.toRadians(deltaAngle);
    	
    	if( antbotOrientation > Math.PI*2 )
    	{
    		antbotOrientation -= Math.PI*2;
    	}
    	else if ( antbotOrientation < Math.PI*2 )
    	{
    		antbotOrientation += Math.PI*2;
    	}
    	
    	System.out.println("antbotOrientation set to " + Math.toDegrees(antbotOrientation) );
    }
    
    /**
     * This should be drawn from the last PI position.
     * 
     * The home point is also in rotated space
     */
    public void setHomeVector(double angle, double distance)
    {
    	
    	double hx = Math.sin(angle + antbotStartOrientation) * distance;
    	double hy = Math.cos(angle + antbotStartOrientation) * distance;
    	System.out.println("\nsetHomeVector (trig): " + hx + ", " + hy );
    	
    	double orntDegrees = Math.toDegrees(angle);
    	System.out.println("angle: " + orntDegrees );
    	System.out.println("antbotStartOrientation: " + Math.toDegrees(antbotStartOrientation) );
    	
    	hx *= 145;
    	hy *= -145;
    	
    	System.out.println("setHomeVector (adj): " + hx + ", " + hy );
    	System.out.println("           lastPoint: " + lastPoint.x + ", " + lastPoint.y );
    	
    	// Add to last point's unadjusted x & y
    	//hx += lastPoint.getX();
    	//hy += lastPoint.getY();
    	
    	System.out.println("setHomeVector  (add): " + hx + ", " + hy);
    	
    	
    	
		homePoint.setLocation(hx, hy);
		System.out.println();
    }
    
    public Point getHomeVector()
    {
    	return homePoint;
    }
    
    /**
     * Using a nested class to spawn a thread stops the UI from freezing up
     */
    private class startServerThread implements Runnable
    {
		public void run()
		{
	        try
	        {
	        	System.out.println("startServerThread run on port " + portNumber);
	        	
	        	// Sockets
		        serverSocket = new ServerSocket(portNumber);
		        clientSocket = serverSocket.accept();
		        online = true;
		        
		        // I/O on the client
		        clientSocketOut = new PrintWriter(clientSocket.getOutputStream(), true);                   
		        clientSocketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	            
	            // Start a thread to monitor for things we get sent
	            new Thread(new ReceiveStringThread(clientSocket, clientSocketIn, me, imageObject)).start();
	            
	    		setChanged();
	    		notifyObservers();
	        }
	        catch(BindException e)
	        {
	        	System.out.println("Can't launch a new connection thread - one already exists");
	        }
	        catch (IOException e)
	        {
	        	e.printStackTrace();
	            System.out.println("Exception caught when trying to listen on port " + portNumber + " or listening for a connection");
	            System.out.println(e.getMessage());
	            
	            // Flag as off-line and close anything that is open
	            online = false;
	            stopServer();
	            
	    		setChanged();
	    		notifyObservers();
	        }
		}
    }
}
