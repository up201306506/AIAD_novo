package gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;

public class StationGUI extends JFrame {
	private static final long serialVersionUID = -3134066878530981813L;

	// important components
	private JPanel contentPane;

	// station table
	private String[] stationTableColumnNames = {"Name", "X", "Y", "Capacity"};
	private JTable stationTable;
	private DefaultTableModel stationTableModel;
	// passengers table
	private String[] passengersTablecolumnNames = {"Name", "Xi", "Yi", "Xd", "Yd", "Passengers"};
	private JTable passengersTable;
	private DefaultTableModel passengersTableModel;

	// constructor
	public StationGUI() {
		super("Taxi Station");

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);

		contentPane.setLayout(new GridLayout(1, 0, 0, 0));

		// ------------------------------------------------
		// station content
		JPanel stationPanel = new JPanel();
		stationPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		contentPane.add(stationPanel);

		JLabel stationLabel = new JLabel("Station");
		stationLabel.setHorizontalAlignment(SwingConstants.CENTER);
		stationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		stationTable = new JTable();
		stationTableModel = new DefaultTableModel(0, 0);
		stationTableModel.setColumnIdentifiers(stationTableColumnNames);
		stationTable.setModel(stationTableModel);

		stationTable.getColumnModel().getColumn(0).setPreferredWidth(100); // name
		stationTable.getColumnModel().getColumn(1).setPreferredWidth(30); // xi
		stationTable.getColumnModel().getColumn(2).setPreferredWidth(30); // yi
		stationTable.getColumnModel().getColumn(3).setPreferredWidth(50); // free spaces

		JScrollPane stationScrollPane = new JScrollPane(stationTable);
		stationTable.setEnabled(false);
		stationTable.setFillsViewportHeight(true);

		stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
		stationPanel.add(stationLabel);
		stationPanel.add(stationScrollPane);

		// ------------------------------------------------
		// passengers content
		JPanel passengersPanel = new JPanel();
		passengersPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		contentPane.add(passengersPanel);
		passengersPanel.setLayout(new BoxLayout(passengersPanel, BoxLayout.Y_AXIS));

		JLabel passengersLabel = new JLabel("Passengers");
		passengersLabel.setAlignmentX(0.5f);
		passengersPanel.add(passengersLabel);

		passengersTable = new JTable();
		passengersTableModel = new DefaultTableModel(0, 0);
		passengersTableModel.setColumnIdentifiers(passengersTablecolumnNames);
		passengersTable.setModel(passengersTableModel);

		passengersTable.getColumnModel().getColumn(0).setPreferredWidth(100); // name
		passengersTable.getColumnModel().getColumn(1).setPreferredWidth(30); // xi
		passengersTable.getColumnModel().getColumn(2).setPreferredWidth(30); // yi
		passengersTable.getColumnModel().getColumn(3).setPreferredWidth(30); // xd
		passengersTable.getColumnModel().getColumn(4).setPreferredWidth(30); // yd
		passengersTable.getColumnModel().getColumn(5).setPreferredWidth(50); // number of passengers

		JScrollPane passengersScrollPane = new JScrollPane(passengersTable);
		passengersTable.setEnabled(false);
		passengersTable.setFillsViewportHeight(true);

		passengersPanel.add(passengersScrollPane);

		// ------------------------------------------------
		// travelling content
		JPanel travellingPanel = new JPanel();
		travellingPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		contentPane.add(travellingPanel);
		travellingPanel.setLayout(new BoxLayout(travellingPanel, BoxLayout.Y_AXIS));

		JLabel travellingLabel = new JLabel("Travelling");
		travellingLabel.setAlignmentX(0.5f);
		travellingPanel.add(travellingLabel);

		JScrollPane travellingScrollPane = new JScrollPane();
		travellingPanel.add(travellingScrollPane);

		setSize(new Dimension(1200, 600));

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setResizable(false);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int centerX = (int) screenSize.getWidth() / 2;
		int centerY = (int) screenSize.getHeight() / 2;
		setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
		setVisible(true);
	}

	// update data to display
	public void addTaxiToStation(String taxiName, int xCoord, int yCoord, int capacity){
		Object[] lineToAdd = {taxiName, "" + xCoord, "" + yCoord, "" + capacity};
		stationTableModel.addRow(lineToAdd);
	}

	public void addPassengerToStation(String passengerName,
			int xInitialCoord, int yInitialCoord,
			int xDestinationCoord, int yDestinationCoord,
			int numberOfPassengers){

		Object[] lineToAdd = {passengerName, "" + xInitialCoord, "" + yInitialCoord, "" + xDestinationCoord, "" + yDestinationCoord, "" + numberOfPassengers};
		passengersTableModel.addRow(lineToAdd);
	}
}