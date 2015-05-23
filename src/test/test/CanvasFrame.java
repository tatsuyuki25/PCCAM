package test.test;

import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Image;

public class CanvasFrame extends JFrame
{

	private static final long serialVersionUID = -8484741011130969205L;
	public static JLabel lblVideo;
	public static JLabel lblInvideo;
	public static class Exception extends java.lang.Exception
	{

		private static final long serialVersionUID = 1L;

		public Exception(String message)
		{
			super(message);
		}

		public Exception(String message,Throwable cause)
		{
			super(message,cause);
		}
	}

	/**
	 * @wbp.parser.constructor
	 */
	public CanvasFrame(String title) throws Exception
	{
		super(title);
		init();
	}

	private void init()
	{
		setVisible(true);
		setBounds(0,0,500,500);
		getContentPane().setBounds(0,0,500,500);
		lblVideo = new JLabel();
		getContentPane().add(lblVideo,BorderLayout.CENTER);
		lblInvideo = new JLabel();
		getContentPane().add(lblInvideo, BorderLayout.EAST);

	}
	public Image createImage(byte[] b)
	{
		return getToolkit().createImage(b);
	}
}
