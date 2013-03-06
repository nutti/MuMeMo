package nutti.lib;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import nutti.mumemo.Constant;

public class ImagedPanel extends JPanel
{
	private BufferedImage			m_BGImage;				// 背景画像

	public ImagedPanel( String path )
	{
		super();
		try{
			m_BGImage = ImageIO.read( new File( path ) );
		}
		catch( IOException e ){
			e.printStackTrace();
		}
	}

	public void paintComponent( Graphics graphics )
	{
		Graphics2D g = ( Graphics2D ) graphics;

		double imgW = m_BGImage.getWidth();
		double imgH = m_BGImage.getHeight();
		double panelW = this.getWidth();
		double panelH = this.getHeight();

		double sx = panelW / imgW;
		double sy = panelH / imgH;

		AffineTransform trans = AffineTransform.getScaleInstance( sx, sy );

		g.drawImage( m_BGImage, trans, this );
	}
}
