package gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Canvas extends JPanel {

	private static final long serialVersionUID = 1992003389582614298L;
	
	// Variables
	private byte[][] map;
	
	// Images
	private BufferedImage taxiImage;
	private BufferedImage taxisImage;
	private BufferedImage streetImage;
	private BufferedImage centralImage;
	private BufferedImage buildingImage;
	private BufferedImage passTaxiImage;
	private BufferedImage passengerImage;
	private BufferedImage passengersImage;
	
	// Images with target
	private BufferedImage targetImage;
	private BufferedImage taxiTargetImage;
	private BufferedImage taxisTargetImage;
	private BufferedImage passTaxiTargetImage;
	private BufferedImage passengerTargetImage;
	private BufferedImage passengersTargetImage;
	
	public Canvas(byte[][] map) {
		this.map = map;
		loadImages();
	}
	
	public void setMap(byte[][] map) {
		this.map = map;
	}
	
	private void loadImages() {
		try {
			taxiImage = ImageIO.read(new File("resources/images/taxi.png"));
			taxisImage = ImageIO.read(new File("resources/images/taxis.png"));
			streetImage = ImageIO.read(new File("resources/images/street.png"));
			centralImage = ImageIO.read(new File("resources/images/central.png"));
			passTaxiImage = ImageIO.read(new File("resources/images/passTaxi.png"));
			buildingImage = ImageIO.read(new File("resources/images/building.png"));
			passengerImage = ImageIO.read(new File("resources/images/passenger.png"));
			passengersImage = ImageIO.read(new File("resources/images/passengers.png"));
			
			targetImage = ImageIO.read(new File("resources/images/target.png"));
			taxiTargetImage = ImageIO.read(new File("resources/images/taxiTarget.png"));
			taxisTargetImage = ImageIO.read(new File("resources/images/taxisTarget.png"));
			passTaxiTargetImage = ImageIO.read(new File("resources/images/passTaxiTarget.png"));
			passengerTargetImage = ImageIO.read(new File("resources/images/passengerTarget.png"));
			passengersTargetImage = ImageIO.read(new File("resources/images/passengersTarget.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int ySize = this.getHeight() / map.length;
		int xSize = this.getWidth() / map[0].length;
		
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {	
				switch (map[y][x]) {
				case 0:
					g.drawImage(streetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 1:
					g.drawImage(buildingImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 2:
					g.drawImage(taxiImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 3:
					g.drawImage(passengerImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 4:
					g.drawImage(taxisImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 5:
					g.drawImage(passTaxiImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 6:
					g.drawImage(passengersImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 7:
					g.drawImage(centralImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 8:
					g.drawImage(targetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 9:
					g.drawImage(taxiTargetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 10:
					g.drawImage(passengerTargetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 11:
					g.drawImage(taxisTargetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 12:
					g.drawImage(passTaxiTargetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				case 13:
					g.drawImage(passengersTargetImage, x * xSize, y * ySize, xSize, ySize, null);
					break;
				}
			}
		}
	}
}
