package ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

class VideoCanvas extends JPanel implements MouseListener
{
	VideoCapture camera;
	CameraPanel cameraPanel;
	Mat displayFrame;
	Mat backgroundFrame;
	Mat element;
	boolean haveBackground;
	long addPointDelay = 100;
	long lastPointTime = 0;
	
	double spinAngle;
	
	ArrayList<Point> truthPath;
	
	VideoCanvas(VideoCapture cam, CameraPanel cameraPanel)
	{
		super();
		camera = cam;
		
		displayFrame = new Mat();
		backgroundFrame = new Mat();
		haveBackground = false;
		element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(25, 25));
		
		this.cameraPanel = cameraPanel;
		addMouseListener(this);
		this.setBackground(Color.black);
		this.setPreferredSize(new Dimension(640, 480));
		
		truthPath = new ArrayList<Point>();
		
		new Thread(new VideoUpdateThread(cam, this)).start();
	}

	public BufferedImage Mat2BufferedImage(Mat m)
	{
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1)
		{
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return img;
	}
	
	public synchronized void takeHomePhoto()
	{
		if( Core.sumElems(displayFrame).val[0] > 0 )
		{
			displayFrame.copyTo(backgroundFrame);
			haveBackground = true;
		}
	}

	@Override
	public synchronized void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		int width = 640;
		int height = 480;
		int xpos = 0;
		int ypos = 0;
		int y = 1;
		
		spinAngle += 1;
		if(spinAngle >= 360)
		{
			spinAngle = 0;
		}
		
		camera.read(displayFrame);

		Mat modFrame = new Mat();
		displayFrame.copyTo(modFrame);
		
		if(haveBackground)
		{
			// Remove background
			Core.subtract(modFrame, backgroundFrame, modFrame);
			
			// Grayscale, threshold and dilate
			Imgproc.cvtColor(modFrame, modFrame, Imgproc.COLOR_RGBA2GRAY);
			Imgproc.threshold(modFrame, modFrame, 65, 255, Imgproc.THRESH_BINARY);
			Imgproc.dilate(modFrame, modFrame, element);
			
			// Find AntBot
			ArrayList<org.opencv.core.Point> objectsInFrame = findAntBot(modFrame);
			
			if( objectsInFrame.size() != 0 )
			{
				if( objectsInFrame.size() > 2 )
				{
					//???
				}
				else 
				{
					Imgproc.rectangle(displayFrame, objectsInFrame.get(0), objectsInFrame.get(1), new Scalar(new double[]{255.0, 0, 0}));

					// Add the halfway point to the top left point to get the position
					double px = objectsInFrame.get(0).x + ((objectsInFrame.get(1).x - objectsInFrame.get(0).x) / 2);
					double py = objectsInFrame.get(0).y + ((objectsInFrame.get(1).y - objectsInFrame.get(0).y) / 2);
					
					if(System.currentTimeMillis() >= lastPointTime + addPointDelay)
					{
						lastPointTime = System.currentTimeMillis();
									
						
						truthPath.add(new Point((int) px, (int) py));
						System.out.println("Added new point at: " + px + ", " + py);
					}
					
					//System.out.println("AntBot at: " + objectsInFrame.get(1).x + ", " + objectsInFrame.get(1).y);
					//System.out.println("AntBot at: " + px + ", " + py);
					
					//Imgproc.circle(displayFrame, center, 5, new Scalar(new double[]{255.0, 0, 0}));
					
				}
			}
			
			//modFrame.copyTo(displayFrame);
		}
		
		BufferedImage image = Mat2BufferedImage( displayFrame );
		g.drawImage(image, xpos, ypos, width, height, null);
		xpos = xpos + width;
		
		//System.out.println("Image: " + displayFrame.width() + "x" + displayFrame.height());
		
		///////////////
		//	Overlay	 //
		///////////////
		try
		{
			// Draw blue circle as start point
			Point startPoint = cameraPanel.getAntbotStartPoint();
			if(startPoint != null)
			{
				g.setColor(Color.blue);
				g.fillOval(startPoint.x - 10, startPoint.y - 10, 20, 20);
				
				// Draw direction marker
				Point directionPoint = cameraPanel.getAntbotDirectionPoint();
				if(directionPoint != null)
				{
					g.setColor(Color.red);
					g.drawLine(startPoint.x, startPoint.y, directionPoint.x, directionPoint.y);
									
					// Draw PI path
					ArrayList<Point> path = cameraPanel.getPath();
					if(path != null && cameraPanel.getShowIntegratedPath())
					{
						g.setColor(Color.yellow);
						Point lastPoint = new Point(-1,-1);
						for(Point p: path)
						{
							if(lastPoint.x == -1)
							{
								g.drawLine(startPoint.x, startPoint.y, p.x, p.y);
								lastPoint = p;
							}
							else
							{
								g.drawLine(lastPoint.x, lastPoint.y, p.x, p.y);
								lastPoint = p;
							}
						}
						
						// Draw home line from PI line
						if(lastPoint.x != -1 && cameraPanel.getShowHomeVector() == 0)
						{
							
							g.setColor(Color.green);
							g.drawLine(lastPoint.x,
									   lastPoint.y,
									   lastPoint.x + cameraPanel.getHomeVector().x,
									   lastPoint.y + cameraPanel.getHomeVector().y);
						}
						
					}
					
					if(truthPath != null && truthPath.size() >= 2 && cameraPanel.getShowTruthPath())
					{
						Point lastPoint = new Point(-1,-1);
						for(Point p: truthPath)
						{
							if(lastPoint.x == -1)
							{
								lastPoint = p;
							}
							else
							{
								g.setColor(Color.cyan);
								g.drawLine(lastPoint.x, lastPoint.y, p.x, p.y);
								lastPoint = p;
							}
						}
						
						// Draw home line from truth line
						if(lastPoint.x != -1 && cameraPanel.getShowHomeVector() == 1)
						{
							
							g.setColor(Color.green);
							g.drawLine(lastPoint.x,
									   lastPoint.y,
									   lastPoint.x + cameraPanel.getHomeVector().x,
									   lastPoint.y + cameraPanel.getHomeVector().y);
						}
					}
				}
			}
		}
		catch(NullPointerException e)
		{
			
		}
		catch(ConcurrentModificationException e)
		{
			g.drawImage(image, xpos, ypos, width, height, null);
		}
	}
	
	private ArrayList<org.opencv.core.Point> findAntBot(Mat image)
	{
		Mat hierarchy = new Mat();
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		ArrayList<org.opencv.core.Point> result = new ArrayList<org.opencv.core.Point>();
		
		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
		
		if(contours.size() > 0)
		{
			int largestArea = 0;
			
			Rect boundingRect = new Rect();
			
			for(int i = 0; i < contours.size(); i++)
			{
				double a = Imgproc.contourArea(contours.get(i), false);
				
				if(a > largestArea)
				{
					largestArea = (int) a;
					boundingRect = Imgproc.boundingRect(contours.get(i));
				}
			}
			
			org.opencv.core.Point tl = boundingRect.tl();
			org.opencv.core.Point br = boundingRect.br();
			
			result.add(tl);
			result.add(br);
		}
		
		return result;
	}
	
	public synchronized void clearTruthPath()
	{
		truthPath.clear();
	}
	
	public synchronized void saveCanvas()
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date date = new Date();
		
		Container c = this;
		BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
		c.paint(im.getGraphics());
		
		try
		{
			ImageIO.write(im, "PNG", new File(dateFormat.format(date)));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/////////////////////
	//	Mouse Clicked  //
	/////////////////////
	
	public void mouseClicked(MouseEvent event)
	{
		System.out.println("x: " + event.getPoint().x + " y: " + event.getPoint().y);
		
		cameraPanel.videoCanvasMouseEvent(event);
	}

	public void mouseEntered(MouseEvent arg0){}

	public void mouseExited(MouseEvent arg0){}

	public void mousePressed(MouseEvent event){}

	public void mouseReleased(MouseEvent arg0){}
	
	/**
	 * Probably wisest to do this in a separate thread
	 */
	class VideoUpdateThread implements Runnable
	{
		VideoCapture camera;
		VideoCanvas view;
		
		public VideoUpdateThread(VideoCapture cam, VideoCanvas vp)
		{
			camera = cam;
			view = vp;
		}
		
		public void run()
		{
			while (camera.isOpened())
			{
				view.repaint();
			}
		}
	}
}