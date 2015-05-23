package test.test;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class test extends JFrame
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5374463333342657790L;

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	DatagramPacket packet;
	DatagramSocket socket;
	byte buffer[];
	JLabel lblTest;

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				try
				{
					test frame = new test();
					frame.setVisible(true);
				} catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public test()
	{
		buffer = new byte[555555];
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100,100,476,390);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5,5,5,5));
		contentPane.setLayout(new BorderLayout(0,0));
		setContentPane(contentPane);

		lblTest = new JLabel();
		contentPane.add(lblTest,BorderLayout.CENTER);

		JButton btnStart = new JButton("start");
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				packet = new DatagramPacket(buffer,buffer.length);
				try
				{
					socket = new DatagramSocket(8809);
					socket.setReceiveBufferSize(131070);
					t.start();
				} catch(SocketException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		contentPane.add(btnStart,BorderLayout.EAST);
	}

	public void setImage()
	{
		try
		{
			socket.receive(packet);
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer,0,
					packet.getLength());
			ObjectInputStream ois = new ObjectInputStream(bais);
			Object o = ois.readObject();
			campacket p = (campacket) o;
			Image imgs = getToolkit().createImage(p.getData());
			
			BufferedImage bi = toBufferedImage(imgs);
			bi = rotateImage(bi,-90);
			bi = resize(bi,320,240);
			lblTest.setIcon(new ImageIcon(bi));
		} catch(IOException | ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Thread t = new Thread(new Runnable()
	{
		public void run()
		{
			while(true)
			{
				setImage();
			}
		}
	});

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
