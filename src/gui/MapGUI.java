package gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import jade.core.AID;
import utils.DataSerializable;
import utils.DataSerializable.PassengerData;
import utils.DataSerializable.TaxiData;

public class MapGUI extends JFrame {

	private static final long serialVersionUID = -9064675381612316299L;

	// Main Panel
	private JTabbedPane tabbedPanel;
	private final byte CELL_SIZE = 15;

	// Tables View
	private JPanel tablesPanel;
	private JTable taxisTable, passengersTable;
	private DefaultTableModel taxisTableModel, passengersTableModel;
	private String[] taxisTableColumnNames = { "Name", "X", "Y", "Cap" };
	private String[] passengersTableColumnNames = { "Name", "Xi", "Yi", "Xf", "Yf", "Num" };

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

	// Constructor
	public MapGUI() {
		super("Map");

		loadImages();
		map = loadMap();
		durationMap = loadTimes();
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

		displayMap(map);

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
	public byte[][] getMap(){
		return map;
	}

	public int[][] getDurationMap(){
		return durationMap;
	}

	// File loading functions
	private byte[][] loadMap() {
		byte[][] map = null;

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

		return map;
	}

	private int[][] loadTimes() {
		int[][] times = null;

		try {
			BufferedReader reader = new BufferedReader(new FileReader("resources/times.txt"));

			String firstLine = reader.readLine();
			String sizes[] = firstLine.split(":");
			times = new int[Integer.parseInt(sizes[0])][Integer.parseInt(sizes[1])];

			int j = 0;
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] values = line.split(",");
				for (int i = 0; i < values.length; i++)
					times[j][i] = Integer.parseInt(values[i]);
				j++;
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return times;
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

	private void displayMap(byte[][] map) {
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
	public void updateTaxisTable(HashMap<AID, DataSerializable.TaxiData> taxis) {
		for (int i = 0; i < taxisTableModel.getRowCount(); i++)
			taxisTableModel.removeRow(i);

		for (Entry<AID, TaxiData> taxi: taxis.entrySet()) {
			taxisTableModel.addRow(new String[] { "" + taxi.getValue().getAID(),
												  "" + taxi.getValue().getXCoord(),
												  "" + taxi.getValue().getYCoord(),
												  "" + taxi.getValue().getCapacity() });
		}
	}

	public void updatePassengersTable(HashMap<AID, DataSerializable.PassengerData> passengers) {
		for (int i = 0; i < passengersTableModel.getRowCount(); i++)
			passengersTableModel.removeRow(i);

		for (Entry<AID, PassengerData> passenger: passengers.entrySet()) {
			passengersTableModel.addRow(new String[] { "" + passenger.getValue().getAID(),
													   "" + passenger.getValue().getStartingCell().getRow(),
													   "" + passenger.getValue().getStartingCell().getCol(),
													   "" + passenger.getValue().getEndingCell().getRow(),
													   "" + passenger.getValue().getEndingCell().getCol(),
													   "" + passenger.getValue().getNumberOfPassenger() });
		}
	}

	public void updateMap(byte[][] map) {
		displayMap(map);
		mapPanel.repaint();
	}
}
