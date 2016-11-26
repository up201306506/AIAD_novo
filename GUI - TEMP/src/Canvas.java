import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class Canvas extends JPanel {

	private static final long serialVersionUID = -1354251777507926593L;

	// Map content
	private byte[][] map;
	private byte CELL_SIZE;
	
	// Map colors
	private final Color BUILDING_COLOR = new Color(0, 0, 0);
	private final Color TAXI_COLOR = new Color(255, 255, 0);
	private final Color CENTRAL_COLOR = new Color(255, 0, 0);
	private final Color PASSENGER_COLOR = new Color(0, 0, 255);
	private final Color STREET_COLOR = new Color(169, 169, 169);
	
	public Canvas(byte[][] map, byte cellSize) {
		this.map = map;
		this.CELL_SIZE = cellSize;
	}
	
	public void updateMap(byte[][] map) {
		this.map = map;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		for (int y = 0; y < map.length; y++) {
			int Y = y * CELL_SIZE;
			
			for (int x = 0; x < map[y].length; x++) {
				int X = x * CELL_SIZE;
				
				switch (map[y][x]) {
				case 0:
					g.setColor(STREET_COLOR);
					g.fillRect(X, Y, CELL_SIZE, CELL_SIZE);
					break;
				case 1:
					g.setColor(BUILDING_COLOR);
					g.fillRect(X, Y, CELL_SIZE, CELL_SIZE);
					break;
				case 2:
					g.setColor(STREET_COLOR);
					g.fillRect(X, Y, CELL_SIZE, CELL_SIZE);
					g.setColor(TAXI_COLOR);
					g.fillRoundRect(X, Y, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
					break;
				case 3:
					g.setColor(STREET_COLOR);
					g.fillRect(X, Y, CELL_SIZE, CELL_SIZE);
					g.setColor(PASSENGER_COLOR);
					g.fillRoundRect(X, Y, CELL_SIZE, CELL_SIZE, CELL_SIZE, CELL_SIZE);
					break;
				case 7:
					g.setColor(CENTRAL_COLOR);
					g.fillRect(X, Y, CELL_SIZE, CELL_SIZE);
					break;
				/*case 4:
					g.setColor(STREET_COLOR);
					g.fillRect(x * 10, y * 10, 10, 10);
					break;
				case 5:
					g.setColor(STREET_COLOR);
					g.fillRect(x * 10, y * 10, 10, 10);
					break;
				case 6:
					g.setColor(STREET_COLOR);
					g.fillRect(x * 10, y * 10, 10, 10);
					break;*/
				}
			}
		}
	}
}
