package gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridLayout;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;

import application.Passenger;
import application.Taxi;
import jade.core.AID;

import java.awt.Color;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;

public class StationGUI extends JFrame {
	private static final long serialVersionUID = -3134066878530981813L;

	// Important components
	private JPanel contentPane;

	// Taxis table
	private String[] taxisTableColumnNames = {"Name", "X", "Y", "Cap"};
	private JTable taxisTable;
	private DefaultTableModel taxisTableModel;

	// Passengers table
	private String[] passengersTablecolumnNames = {"Name", "Xi", "Yi", "Xf", "Yf", "Num"};
	private JTable passengersTable;
	private DefaultTableModel passengersTableModel;

	// Constructor
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

		taxisTable = new JTable();
		taxisTableModel = new DefaultTableModel();
		taxisTableModel.setColumnIdentifiers(taxisTableColumnNames);
		taxisTable.setModel(taxisTableModel);

		taxisTable.setEnabled(false);
		taxisTable.setFillsViewportHeight(true);

		JScrollPane stationScrollPane = new JScrollPane(taxisTable);

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
		passengersTableModel = new DefaultTableModel();
		passengersTableModel.setColumnIdentifiers(passengersTablecolumnNames);
		passengersTable.setModel(passengersTableModel);

		passengersTable.setEnabled(false);
		passengersTable.setFillsViewportHeight(true);

		JScrollPane passengersScrollPane = new JScrollPane(passengersTable);

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

	// Update data to display
	public void updateTaxis(HashMap<AID, Taxi> taxisHashMap){

		// Removes old info
		for(int i = taxisTableModel.getRowCount() - 1; i >= 0; i--){
			taxisTableModel.removeRow(i);
		}

		// Inserts new info
		for(Map.Entry<AID, Taxi> entry : taxisHashMap.entrySet()){

			taxisTableModel.addRow(new String[] {
					entry.getKey().getLocalName(),
					"" + entry.getValue().getXCoord(),
					"" + entry.getValue().getYCoord(),
					"" + entry.getValue().getCapacity()
			});
		}
	}

	public void updatePassengers(HashMap<AID, Passenger> passengersHashMap){

		// Removes old info
		for(int i = passengersTableModel.getRowCount() - 1; i >= 0; i--){
			passengersTableModel.removeRow(i);
		}

		// Inserts new info
		for(Map.Entry<AID, Passenger> entry : passengersHashMap.entrySet()){
			passengersTableModel.addRow(new String[] {
					entry.getKey().getLocalName(),
					"" + entry.getValue().getXiCoord(),
					"" + entry.getValue().getYiCoord(),
					"" + entry.getValue().getXfCoord(),
					"" + entry.getValue().getYfCoord(),
					"" + entry.getValue().getNumberOfPassengers()
			});
		}
	}
}