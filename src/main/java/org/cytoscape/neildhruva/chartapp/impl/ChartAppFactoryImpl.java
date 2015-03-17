package org.cytoscape.neildhruva.chartapp.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.neildhruva.chartapp.ChartAppFactory;
import org.cytoscape.neildhruva.chartapp.CytoChart;

import org.jfree.chart.ChartPanel;

public class ChartAppFactoryImpl implements ChartAppFactory {

	private final AxisMode DEFAULT_MODE = AxisMode.ROWS;
	private CyTableFactory tableFactory;
	private CyTableManager cyTableManager;
	private JPanel jpanel;
	
	public ChartAppFactoryImpl(	CyTableFactory tableFactory, CyTableManager cyTableManager) {

		this.tableFactory = tableFactory;
		this.cyTableManager = cyTableManager;
		
	}
	
	@Override
	public CytoChart createChart(String chartName, CyTable cyTable) {
		return createChart(chartName, cyTable, DEFAULT_MODE, null, null);
	}
	
	@Override
	public CytoChart createChart(String chartName, CyTable cyTable, AxisMode mode) {
		return createChart(chartName, cyTable, mode, null, null);
	}
	
	@Override
	public CytoChart createChart(String chartName, CyTable cyTable, AxisMode mode, List<String> rows, List<String> columns){
		
		PanelLayout panelLayout = new PanelLayout();
		PanelComponents panelComponents = new PanelComponents(tableFactory, cyTableManager, panelLayout);
		
		MyTableModel myTableModel = new MyTableModel(cyTable);
		//tableColumnCount is the count of the plottable columns - int, long, double
		int tableColumnCount = myTableModel.getColumnCount();
		
		ChartPanel myChartPanel = null;
		if(tableColumnCount>0) {
			panelComponents.initComponents(cyTable, mode, myTableModel, rows, columns, null);
			
			//get all components and send them to the panel layout class.
			JComboBox chartTypeComboBox = panelComponents.getComboBox();
			JCheckBox[] checkBoxArray = panelComponents.getCheckBoxArray();
			myChartPanel  = panelComponents.getChartPanel();
			int checkBoxCount = panelComponents.getCheckBoxCount();
			
			jpanel = panelLayout.initLayout(checkBoxCount, checkBoxArray, chartTypeComboBox, myChartPanel);
			
		} else {
			jpanel = panelLayout.nullJPanel();
		}
		
		CytoChart cytoChart = new CytoChartImpl(jpanel, myChartPanel, myTableModel, cyTable, mode, panelComponents, panelLayout);
		return cytoChart;
	}
	
	@Override
	public CytoChart getSavedChart(String chartName, CyTable cyTable, Set<CyTableMetadata> cyTableMetadata) {
		
		CyTable myCyTable = null;
		String tableName = cyTable.getTitle();
		
		if(cyTableMetadata!=null) {
			Iterator<CyTableMetadata> iterator = cyTableMetadata.iterator();
			CyTable tempCyTable;
			while(iterator.hasNext()) {
				tempCyTable = iterator.next().getTable();
				if(tempCyTable.getTitle().equals(chartName+"CytoChart "+tableName)) {
					myCyTable = tempCyTable;
					break;
				}
			}
		}
		
		long suid = -1;
		
		if(myCyTable==null) {
			Iterator<CyTable> cyIterator = cyTableManager.getAllTables(true).iterator();
			CyTable tempCyTable;
			while(cyIterator.hasNext()) {
				tempCyTable = cyIterator.next();
				if(tempCyTable.getTitle().equals(chartName+"CytoChart "+tableName)) {
					myCyTable = tempCyTable;
					break;
				}
			}
		} else {
			Iterator<CyTable> cyIterator = cyTableManager.getAllTables(true).iterator();
			CyTable tempCyTable;
			while(cyIterator.hasNext()) {
				tempCyTable = cyIterator.next();
				if(tempCyTable.getTitle().equals(chartName+"CytoChart "+tableName)) {
					suid = tempCyTable.getSUID();
					if(suid==myCyTable.getSUID())
						continue;
					else
						break;
				}
			}
			if(suid!=-1 && suid!=myCyTable.getSUID()) {
				cyTableManager.deleteTable(suid);
			}
		}
		
		
		
		//if the chart doesn't exist, create a new one
		if(myCyTable==null) {
			//TODO show error message that chart doesn't exist
			return null;
		} else {
			PanelLayout panelLayout = new PanelLayout();
			PanelComponents panelComponents = new PanelComponents(tableFactory, cyTableManager, panelLayout);
		
			MyTableModel myTableModel = new MyTableModel(cyTable);
			
			panelComponents.reInitComponents(cyTable, myCyTable, myTableModel);
			
			//get all components and send them to the panel layout class.
			JComboBox chartTypeComboBox = panelComponents.getComboBox();
			JCheckBox[] checkBoxArray = panelComponents.getCheckBoxArray();
			ChartPanel myChartPanel = panelComponents.getChartPanel();
			AxisMode mode = panelComponents.getAxisMode();
			int checkBoxCount = panelComponents.getCheckBoxCount(); 
			
			jpanel = panelLayout.initLayout(checkBoxCount, checkBoxArray, chartTypeComboBox, myChartPanel);
		
			CytoChart cytoChart = new CytoChartImpl(jpanel, myChartPanel, myTableModel, cyTable, mode, panelComponents, panelLayout);
			return cytoChart;
		}
	}
	
	@Override
	public void deleteCytoChart(String chartName, CyTable cyTable) {
		Iterator<CyTable> iterator = cyTableManager.getAllTables(true).iterator();
		CyTable myTable;
		while(iterator.hasNext()) {
			myTable = iterator.next();
			if(myTable.getTitle().equals(chartName+"CytoChart "+cyTable.getTitle())) {
				cyTableManager.deleteTable(myTable.getSUID());
			}
		}
		
	}
}
