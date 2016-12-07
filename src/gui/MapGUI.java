package gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import utils.Cell;
import utils.DataSerializable;

public class MapGUI extends JFrame {

	private static final long serialVersionUID = -9064675381612316299L;

	// Main Panel
	private JTabbedPane tabbedPanel;
	private final byte CELL_SIZE = 15;
	private final byte OFFSET_WIDTH = 61;
	private final byte OFFSET_HEIGHT = 108;

	// Tables View
	private JPanel tablesPanel;
	private JTable taxisTable, passengersTable;
	private DefaultTableModel taxisTableModel, passengersTableModel;
	private String[] taxisTableColumnNames = { "Name", "Row", "Col", "Cap" };
	private String[] passengersTableColumnNames = { "Name", "iRow", "iCol", "fRow", "fCol", "Num" };

	// Map View
	private Canvas canvas;

	// Variables
	private byte[][] map;
	private int[][] durationMap;
	private HashMap<DataSerializable.TaxiData, Cell> taxis;
	private HashMap<DataSerializable.PassengerData, Cell> passengers;

	// Constructor
	public MapGUI() {
		super("Map");

		loadMap();
		loadTimes();

		taxis = new HashMap<>();
		passengers = new HashMap<>();

		canvas = new Canvas(map);

		taxisTable = new JTable();
		taxisTable.setFocusable(false);
		taxisTableModel = new DefaultTableModel();
		taxisTableModel.setColumnIdentifiers(taxisTableColumnNames);
		taxisTable.setModel(taxisTableModel);

		passengersTable = new JTable();
		passengersTable.setFocusable(false);
		passengersTableModel = new DefaultTableModel();
		passengersTableModel.setColumnIdentifiers(passengersTableColumnNames);
		passengersTable.setModel(passengersTableModel);

		tablesPanel = new JPanel();
		tablesPanel.setLayout(new GridLayout(1, 2));

		JScrollPane panel1 = new JScrollPane(taxisTable);
		JScrollPane panel2 = new JScrollPane(passengersTable);

		tablesPanel.add(panel1);
		tablesPanel.add(panel2);

		tabbedPanel = new JTabbedPane();
		tabbedPanel.addTab("Tables View", tablesPanel);
		tabbedPanel.addTab("Map View", canvas);

		setContentPane(tabbedPanel);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(new Dimension((map.length * CELL_SIZE) + OFFSET_WIDTH, (map[0].length * CELL_SIZE) + OFFSET_HEIGHT));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int) screenSize.getWidth() / 2;
		int centerY = (int) screenSize.getHeight() / 2;

		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}

	// Getters and setters
	public byte[][] getMap() {
		return map;
	}

	public int[][] getDurationMap() {
		return durationMap;
	}

	// File loading functions
	private void loadMap() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader("resources/map.txt"));

			String firstLine = reader.readLine();
			String sizes[] = firstLine.split(":");
			map = new byte[Integer.parseInt(sizes[0])][Integer.parseInt(sizes[1])];

			int j = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				for (int i = 0; i < line.length(); i++)
					map[j][i] = (byte) Character.getNumericValue(line.charAt(i));
				j++;
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadTimes() {
		Random r = new Random();
		durationMap = new int[map.length][map[0].length];
		
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[i].length; j++) {
				if (map[i][j] == 0)
					durationMap[i][j] = r.nextInt((1500 - 500) + 1) + 500;
				else if (map[i][j] == 7)
					durationMap[i][j] = 0;
			}
		}
	}

	// Update GUI information functions
	public void updateCanvas() {
		canvas.setMap(map);
		canvas.repaint();
	}

	public void updateTaxi(DataSerializable.TaxiData taxi) {
		if (taxisTableModel.getRowCount() == 0) {
			taxis.put(taxi, taxi.getPosition());
			taxisTableModel.addRow(new String[] { "" + taxi.getAID().getLocalName(),
					"" + taxi.getPosition().getRow(),
					"" + taxi.getPosition().getCol(),
					"" + taxi.getCapacity() });
			updateMap(taxi.getPosition(), "taxi");
		} else {
			for (int i = 0; i < taxisTableModel.getRowCount(); i++) {
				if (taxisTableModel.getValueAt(i, 0).equals(taxi.getAID().getLocalName())) {
					Cell temp = taxis.get(taxi);

					taxis.put(taxi, taxi.getPosition());
					taxisTableModel.setValueAt("" + taxi.getPosition().getRow(), i, 1);
					taxisTableModel.setValueAt("" + taxi.getPosition().getCol(), i, 2);
					taxisTableModel.setValueAt("" + taxi.getCapacity(), i, 3);

					updateMap(temp, taxi.getPosition(), "taxi");
					return;
				}
			}

			taxis.put(taxi, taxi.getPosition());
			taxisTableModel.addRow(new String[] { "" + taxi.getAID().getLocalName(),
					"" + taxi.getPosition().getRow(),
					"" + taxi.getPosition().getCol(),
					"" + taxi.getCapacity() });
			updateMap(taxi.getPosition(), "taxi");
		}
	}

	public void updatePassenger(DataSerializable.PassengerData passenger) {
		if (passengersTableModel.getRowCount() == 0) {
			passengers.put(passenger, passenger.getStartingCell());
			passengersTableModel.addRow(new String[] { "" + passenger.getAID().getLocalName(),
					"" + passenger.getStartingCell().getRow(),
					"" + passenger.getStartingCell().getCol(),
					"" + passenger.getEndingCell().getRow(),
					"" + passenger.getEndingCell().getCol(),
					"" + passenger.getNumberOfPassengers() });
			updateMap(passenger.getStartingCell(), "passenger");
		} else {
			for (int i = 0; i < passengersTableModel.getRowCount(); i++) {
				if (passengersTableModel.getValueAt(i, 0).equals(passenger.getAID().getLocalName())) {
					Cell temp = passengers.get(passenger);

					passengers.put(passenger, passenger.getStartingCell());
					passengersTableModel.setValueAt("" + passenger.getStartingCell().getRow(), i, 1);
					passengersTableModel.setValueAt("" + passenger.getStartingCell().getCol(), i, 2);
					passengersTableModel.setValueAt("" + passenger.getEndingCell().getRow(), i, 3);
					passengersTableModel.setValueAt("" + passenger.getEndingCell().getCol(), i, 4);
					passengersTableModel.setValueAt("" + passenger.getNumberOfPassengers(), i, 5);

					updateMap(temp, passenger.getStartingCell(), "passenger");
					return;
				}
			}

			passengers.put(passenger, passenger.getStartingCell());
			passengersTableModel.addRow(new String[] { "" + passenger.getAID().getLocalName(),
					"" + passenger.getStartingCell().getRow(),
					"" + passenger.getStartingCell().getCol(),
					"" + passenger.getEndingCell().getRow(),
					"" + passenger.getEndingCell().getCol(),
					"" + passenger.getNumberOfPassengers() });
			updateMap(passenger.getStartingCell(), "passenger");
		}
	}

	private void updateMap(Cell newPos, String type) {
		if (newPos.getRow() >= 0 && newPos.getRow() < map.length
				&& newPos.getCol() >= 0 && newPos.getCol() < map[0].length) {
			if (map[newPos.getRow()][newPos.getCol()] == 1)
				System.out.println("Invalid position!");
			else if (map[newPos.getRow()][newPos.getCol()] == 0)
				map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 2 : 3);
			else if (map[newPos.getRow()][newPos.getCol()] == 2)
				map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 4 : 5);
			else if (map[newPos.getRow()][newPos.getCol()] == 3)
				map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 5 : 6);
			else if (map[newPos.getRow()][newPos.getCol()] == 4)
				map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 4 : 5);
			else if (map[newPos.getRow()][newPos.getCol()] == 5)
				map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 5 : 5);
			else if (map[newPos.getRow()][newPos.getCol()] == 6)
				map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 5 : 6);
		}
	}

	private void updateMap(Cell currPos, Cell newPos, String type) {
		if (currPos.getRow() >= 0 && currPos.getRow() < map.length
				&& currPos.getCol() >= 0 && currPos.getCol() < map[0].length) {
			if (newPos.getRow() >= 0 && newPos.getRow() < map.length
					&& newPos.getCol() >= 0 && newPos.getCol() < map[0].length) {
				if (map[currPos.getRow()][currPos.getCol()] == 2)
					map[currPos.getRow()][currPos.getCol()] = 0;
				else if (map[currPos.getRow()][currPos.getCol()] == 3)
					map[currPos.getRow()][currPos.getCol()] = 0;
				else if (map[currPos.getRow()][currPos.getCol()] == 4)
					map[currPos.getRow()][currPos.getCol()] = 2;
				else if (map[currPos.getRow()][currPos.getCol()] == 5)
					map[currPos.getRow()][currPos.getCol()] = (byte) (type.equals("taxi") ? 3 : 2);
				else if (map[currPos.getRow()][currPos.getCol()] == 6)
					map[currPos.getRow()][currPos.getCol()] = 3;

				if (map[newPos.getRow()][newPos.getCol()] == 1)
					System.out.println("Invalid position!");
				else if (map[newPos.getRow()][newPos.getCol()] == 0)
					map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 2 : 3);
				else if (map[newPos.getRow()][newPos.getCol()] == 2)
					map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 4 : 5);
				else if (map[newPos.getRow()][newPos.getCol()] == 3)
					map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 5 : 6);
				else if (map[newPos.getRow()][newPos.getCol()] == 4)
					map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 4 : 5);
				else if (map[newPos.getRow()][newPos.getCol()] == 5)
					map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 5 : 5);
				else if (map[newPos.getRow()][newPos.getCol()] == 6)
					map[newPos.getRow()][newPos.getCol()] = (byte) (type.equals("taxi") ? 5 : 6);
			}
		}
	}

	public void removeTaxi(DataSerializable.TaxiData taxi) {
		// Remove from table
		for (int i = taxisTableModel.getRowCount(); i >= 0; i--) {
			if (taxisTableModel.getValueAt(i, 0).equals(taxi.getAID().getLocalName())) {
				taxisTableModel.removeRow(i);
				break;
			}
		}

		// Remove from hashmap
		Cell temp = taxis.get(taxi);
		if (temp != null)
			taxis.remove(taxi);

		// Remove from map
		removeFromMap(temp, "taxi");
	}

	public void removePassenger(DataSerializable.PassengerData passenger) {
		// Remove from table
		for (int i = passengersTableModel.getRowCount(); i >= 0; i--) {
			if (passengersTableModel.getValueAt(i, 0).equals(passenger.getAID().getLocalName())) {
				passengersTableModel.removeRow(i);
				break;
			}
		}

		// Remove from hashmap
		Cell temp = passengers.get(passenger);
		if (temp != null)
			passengers.remove(passenger);

		// Remove from map
		removeFromMap(temp, "passenger");
	}

	private void removeFromMap(Cell pos, String type) {
		if (pos.getRow() >= 0 && pos.getRow() < map.length
				&& pos.getCol() >= 0 && pos.getCol() < map[0].length) {
			if (map[pos.getRow()][pos.getCol()] == 2)
				map[pos.getRow()][pos.getCol()] = 0;
			else if (map[pos.getRow()][pos.getCol()] == 3)
				map[pos.getRow()][pos.getCol()] = 0;
			else if (map[pos.getRow()][pos.getCol()] == 4)
				map[pos.getRow()][pos.getCol()] = 2;
			else if (map[pos.getRow()][pos.getCol()] == 5)
				map[pos.getRow()][pos.getCol()] = (byte) (type.equals("taxi") ? 3 : 2);
			else if (map[pos.getRow()][pos.getCol()] == 6)
				map[pos.getRow()][pos.getCol()] = 3;
		}
	}
}