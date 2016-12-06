package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

	// Tables View
	private JPanel tablesPanel;
	private JTable taxisTable, passengersTable;
	private DefaultTableModel taxisTableModel, passengersTableModel;
	private String[] taxisTableColumnNames = { "Name", "Row", "Col", "Cap" };
	private String[] passengersTableColumnNames = { "Name", "iRow", "iCol", "fRow", "fCol", "Num" };

	// Map View
	private JPanel mapPanel;
	private GridBagConstraints gbc;

	// Images
	private ImageIcon taxiImage;
	private ImageIcon taxisImage;
	private ImageIcon streetImage;
	private ImageIcon centralImage;
	private ImageIcon passTaxiImage;
	private ImageIcon buildingImage;
	private ImageIcon passengerImage;
	private ImageIcon passengersImage;

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
		loadImages();

		taxis = new HashMap<>();
		passengers = new HashMap<>();

		gbc = new GridBagConstraints();

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

		mapPanel = new JPanel();
		mapPanel.setLayout(new GridLayout(map.length, 0));

		displayMap();

		JScrollPane panel1 = new JScrollPane(taxisTable);
		JScrollPane panel2 = new JScrollPane(passengersTable);

		tablesPanel.add(panel1);
		tablesPanel.add(panel2);

		tabbedPanel = new JTabbedPane();
		tabbedPanel.addTab("Tables View", tablesPanel);
		tabbedPanel.addTab("Map View", mapPanel);

		setResizable(true);
		setContentPane(tabbedPanel);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setSize(new Dimension(map.length * CELL_SIZE, map[0].length * CELL_SIZE));
		setMaximumSize(new Dimension(map.length * CELL_SIZE + 60, map[0].length * CELL_SIZE + 100));

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
		try {
			BufferedReader reader = new BufferedReader(new FileReader("resources/times.txt"));

			String firstLine = reader.readLine();
			String sizes[] = firstLine.split(":");
			durationMap = new int[Integer.parseInt(sizes[0])][Integer.parseInt(sizes[1])];

			int j = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				for (int i = 0; i < values.length; i++)
					durationMap[j][i] = Integer.parseInt(values[i]);
				j++;
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadImages() {
		taxiImage = new ImageIcon("resources/images/taxi.png");
		taxisImage = new ImageIcon("resources/images/taxis.png");
		streetImage = new ImageIcon("resources/images/street.png");
		centralImage = new ImageIcon("resources/images/central.png");
		passTaxiImage = new ImageIcon("resources/images/passTaxi.png");
		buildingImage = new ImageIcon("resources/images/building.png");
		passengerImage = new ImageIcon("resources/images/passenger.png");
		passengersImage = new ImageIcon("resources/images/passengers.png");
	}

	private void displayMap() {
		for (int y = 0; y < map.length; y++) {
			gbc.gridy = y;
			for (int x = 0; x < map[y].length; x++) {
				gbc.gridx = x;
				switch (map[y][x]) {
				case 0:
					mapPanel.add(new JLabel(streetImage), gbc);
					break;
				case 1:
					mapPanel.add(new JLabel(buildingImage), gbc);
					break;
				case 2:
					mapPanel.add(new JLabel(taxiImage), gbc);
					break;
				case 3:
					mapPanel.add(new JLabel(passengerImage), gbc);
					break;
				case 4:
					mapPanel.add(new JLabel(passengersImage), gbc);
					break;
				case 5:
					mapPanel.add(new JLabel(passTaxiImage), gbc);
					break;
				case 6:
					mapPanel.add(new JLabel(taxisImage), gbc);
					break;
				case 7:
					mapPanel.add(new JLabel(centralImage), gbc);
					break;
				}
			}
		}
	}

	// Update GUI information functions
	public void updateTaxi(DataSerializable.TaxiData taxi) {
		if (taxisTableModel.getRowCount() == 0) {
			taxis.put(taxi, taxi.getPosition());
			taxisTableModel.addRow(new String[] { "" + taxi.getAID().getLocalName(),
												  "" + taxi.getPosition().getRow(),
												  "" + taxi.getPosition().getCol(),
												  "" + taxi.getCapacity() });
			updateMap(taxi.getPosition());
		} else {
			for (int i = 0; i < taxisTableModel.getRowCount(); i++) {
				if (taxisTableModel.getValueAt(i, 0).equals(taxi.getAID().getLocalName())) {
					Cell temp = taxis.get(taxi);
					taxis.put(taxi, taxi.getPosition());
					taxisTableModel.setValueAt("" + taxi.getPosition().getRow(), i, 1);
					taxisTableModel.setValueAt("" + taxi.getPosition().getCol(), i, 2);
					taxisTableModel.setValueAt("" + taxi.getCapacity(), i, 3);

					updateMap(temp, taxi.getPosition());
					return;
				}
			}

			taxis.put(taxi, taxi.getPosition());
			taxisTableModel.addRow(new String[] { "" + taxi.getAID().getLocalName(),
												  "" + taxi.getPosition().getRow(),
												  "" + taxi.getPosition().getCol(),
												  "" + taxi.getCapacity() });
			updateMap(taxi.getPosition());
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
			updateMap(passenger.getStartingCell());
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

					updateMap(temp, passenger.getStartingCell());
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
			updateMap(passenger.getStartingCell());
		}
	}

	private void updateMap(Cell newPos) {
		// TODO
	}

	private void updateMap(Cell previousPos, Cell newPos) {
		// TODO
	}
}
