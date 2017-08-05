package ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

import javax.swing.JPanel;

import server.ImageObject;

public class VideoStreamCanvas extends JPanel {

	ImageObject imageObject;
	
	public VideoStreamCanvas(ImageObject imageObject){
		super();
		this.imageObject = imageObject;
	}
	
	
	
	public BufferedImage String2BufferedImage(String imageString){
		byte[] b = new byte[360*40];
		Arrays.fill(b, (byte)0);
		
		if (!imageString.isEmpty()){
			String[] imageStringArray = imageString.split(",");
			//System.out.println(imageStringArray.length);
			b = new byte[imageStringArray.length];
			for(int n = 0; n < imageStringArray.length; n++){
				b[n] = Byte.parseByte(imageStringArray[n]);
			}
		}
		int type = BufferedImage.TYPE_BYTE_GRAY;
		
		BufferedImage img = new BufferedImage(360, 40, type);
		final byte[] targetPixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return img;
	}
	
	
	@Override
	public synchronized void paintComponent(Graphics g){
		super.paintComponent(g);
		
		int width = 450;
		int height = 50;
		int xpos = 0;
		int ypos = 0;
		int y = 1;
		
		
		BufferedImage image = String2BufferedImage(imageObject.getImageString());
		AffineTransform affineTransform = new AffineTransform();
		affineTransform.concatenate(AffineTransform.getScaleInstance(1, -1));
		affineTransform.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
		image = createTransformed(image, affineTransform);
		g.drawImage(image, xpos, ypos, width, height, null);
		xpos = xpos + width;
		repaint();
		
		
	}
	
	 private static BufferedImage createTransformed(
		        BufferedImage image, AffineTransform at)
		    {
		        BufferedImage newImage = new BufferedImage(
		            image.getWidth(), image.getHeight(),
		            BufferedImage.TYPE_INT_ARGB);
		        Graphics2D g = newImage.createGraphics();
		        g.transform(at);
		        g.drawImage(image, 0, 0, null);
		        g.dispose();
		        return newImage;
		    }
}
