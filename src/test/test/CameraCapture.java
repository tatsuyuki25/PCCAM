package test.test;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import static com.googlecode.javacv.cpp.opencv_core.cvReleaseImage;

public class CameraCapture
{
	private CanvasFrame canvasFrame;
	public static void main(String[] args) throws Exception
	{
		CameraCapture cc = new CameraCapture();
		cc.openMyWebcam();
	}
	public void openInWebcam()
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					DatagramSocket socket = new DatagramSocket(8810);
					socket.setReceiveBufferSize(131070);
					byte[] buffer = new byte[65535];
					DatagramPacket packet = new DatagramPacket(buffer,buffer.length);

					while(true)
					{
						socket.receive(packet);
						ByteArrayInputStream bais = new ByteArrayInputStream(buffer,
								0,packet.getLength());
						ObjectInputStream ois = new ObjectInputStream(bais);
						Object o = ois.readObject();
						campacket p = (campacket) o;
						Image imgs = canvasFrame.createImage(p.getData());
						BufferedImage bi = toBufferedImage(imgs);
						bi = rotateImage(bi,-90);
						bi = resize(bi,320,240);
						CanvasFrame.lblInvideo.setIcon(new ImageIcon(bi));
					}
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}
	public void openMyWebcam()throws Exception
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					// open camera source
					OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
					grabber.setImageHeight(160);
					grabber.setImageWidth(240);
					grabber.start();
					// create a frame for real-time image display
					canvasFrame = new CanvasFrame("Camera");
					IplImage image = grabber.grab();
					int width = image.width();
					int height = image.height();
					System.out.println(width+" "+height);
					openInWebcam();
					final BufferedImage bImage = new BufferedImage(width,height,
							BufferedImage.TYPE_INT_RGB);
					Graphics2D bGraphics = bImage.createGraphics();
					// real-time image display
					while(canvasFrame.isVisible() && (image = grabber.grab()) != null)
					{
						if(true)
						{ 
							bGraphics.drawImage(image.getBufferedImage(),null,0,0);
							byte[]b = imageToBytes(bImage,"jpg");
							CanvasFrame.lblVideo.setIcon(new ImageIcon(bImage));
							System.out.println(b.length);
							DatagramSocket socket = new DatagramSocket();
							InetAddress iddr = InetAddress.getByName("192.168.1.102");
							ByteArrayOutputStream bao = new ByteArrayOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(
									bao);
							campacket p = new campacket(b);
							oos.writeObject(p);
							byte[] buffer = bao.toByteArray();
							DatagramPacket packet = new DatagramPacket(buffer,
									buffer.length,iddr,8809);
							socket.setSendBufferSize(131070);
							socket.send(packet);
						}
					}
					cvReleaseImage(image);
					grabber.stop();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}
	public static byte[] imageToBytes(Image image, String format) {
		 BufferedImage bImage = new BufferedImage(image.getWidth(null), image
		 .getHeight(null), BufferedImage.TYPE_INT_RGB);
		 Graphics bg = bImage.getGraphics();
		 bg.drawImage(image, 0, 0, null);
		 bg.dispose();

		 ByteArrayOutputStream out = new ByteArrayOutputStream();
		 try {
		 ImageIO.write(bImage, format, out);
		 } catch (IOException e) {
		 e.printStackTrace();
		 }
		 return out.toByteArray();
		 }  
	// This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(Image image)
	{
		if(image instanceof BufferedImage)
		{
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		try
		{
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if(hasAlpha)
			{
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null),
					image.getHeight(null),transparency);
		} catch(HeadlessException e)
		{
			// The system does not have a screen
		}

		if(bimage == null)
		{
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if(hasAlpha)
			{
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null),image.getHeight(null),
					type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image,0,0,null);
		g.dispose();

		return bimage;
	}

	// Determining If an Image Has Transparent Pixels

	// This method returns true if the specified image has transparent pixels
	public static boolean hasAlpha(Image image)
	{
		// If buffered image, the color model is readily available
		if(image instanceof BufferedImage)
		{
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image,0,0,1,1,false);
		try
		{
			pg.grabPixels();
		} catch(InterruptedException e)
		{
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	public static BufferedImage rotateImage(final BufferedImage bufferedimage,
			final int degree)
	{
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		int type = bufferedimage.getColorModel().getTransparency();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w,h,type)).createGraphics())
				.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics2d.rotate(Math.toRadians(degree),w / 2,h / 2);
		graphics2d.drawImage(bufferedimage,0,0,null);
		graphics2d.dispose();
		return img;
	}
   private static BufferedImage resize(BufferedImage source, int targetW,    
         int targetH) {    
     int type = source.getType();    
     BufferedImage target = null;    
     double sx = (double) targetW / source.getWidth();    
     double sy = (double) targetH / source.getHeight();    

     if (sx < sy) {    
         sx = sy;    
         targetW = (int) (sx * source.getWidth());    
     } else {    
         sy = sx;    
         targetH = (int) (sy * source.getHeight());    
     }    
     if (type == BufferedImage.TYPE_CUSTOM) { // handmade    
         ColorModel cm = source.getColorModel();    
         WritableRaster raster = cm.createCompatibleWritableRaster(targetW,    
                 targetH);    
         boolean alphaPremultiplied = cm.isAlphaPremultiplied();    
         target = new BufferedImage(cm, raster, alphaPremultiplied, null);    
     } else   
         target = new BufferedImage(targetW, targetH, type);    
     Graphics2D g = target.createGraphics();    
     // smoother than exlax:    
     g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,    
             RenderingHints.VALUE_INTERPOLATION_BICUBIC);    
     g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));    
     g.dispose();    
     return target;    
 }    
}
