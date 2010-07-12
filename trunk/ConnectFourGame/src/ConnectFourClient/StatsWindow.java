package ConnectFourClient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.ScrollPane;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import common.StatsReport;
import common.UserStatistics;

public class StatsWindow extends JDialog {
	private JScrollPane topTen;
	private JPanel userPane;
	public StatsWindow (StatsReport report)
	{
	this.setTitle("Statistics For: "+report.getCurrentUser().getUsername());
	
	userPane= new JPanel();
	userPane.setLayout(new BoxLayout(userPane,  BoxLayout.PAGE_AXIS));



	JTable table= new JTable(new DefaultTableModel());
	table.setFillsViewportHeight(true);
	((DefaultTableModel)table.getModel()).addColumn("username");
	((DefaultTableModel)table.getModel()).addColumn("wins");
	((DefaultTableModel)table.getModel()).addColumn("loses");
	((DefaultTableModel)table.getModel()).addColumn("rank");
	for (UserStatistics stats : report.getTopTen()) {
		((DefaultTableModel)table.getModel()).addRow(new Object[] {stats.getUsername(),stats.getWins(),stats.getLoses(),stats.getRank()});
	}
	topTen= new JScrollPane(table);

	JLabel label= new JLabel("Your Statistics are:");
	JLabel winsL= new JLabel("Wins: "+report.getCurrentUser().getWins());
	winsL.setForeground(Color.green);
	JLabel losesL= new JLabel("Loses: "+report.getCurrentUser().getLoses());
	losesL.setForeground(Color.red);
	JLabel rankL= new JLabel("Rank: "+report.getCurrentUser().getRank());
	rankL.setForeground(Color.blue);
	userPane.add(label);
	userPane.add(winsL);
	userPane.add(losesL);
	userPane.add(rankL);
	JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			userPane, topTen);
	splitPane.setOneTouchExpandable(true);
	splitPane.setDividerLocation(150);
	//Provide minimum sizes for the two components in the split pane
	Dimension minimumSize = new Dimension(100, 50);
	userPane.setMinimumSize(minimumSize);
	 topTen.setMinimumSize(minimumSize);
	getContentPane().add(splitPane);
	this.setSize(500,120);
	setModal(true);
	setVisible(true);
	}
}
