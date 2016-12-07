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
	private BufferedImage passTaxiImage;
	private BufferedImage buildingImage;
	private BufferedImage passengerImage;
	private BufferedImage passengersImage;
	
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for (int y = 0; y < map.length; y++) {
			for (int x = 0; x < map[y].length; x++) {
				int ySize = this.getHeight() / map.length;
				int xSize = this.getWidth() / map[y].length;
				
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
				}
			}
		}
	}
}
