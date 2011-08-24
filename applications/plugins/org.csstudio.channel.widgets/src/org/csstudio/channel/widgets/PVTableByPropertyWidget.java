package org.csstudio.channel.widgets;

import static org.epics.pvmanager.ExpressionLanguage.*;
import static org.epics.pvmanager.data.ExpressionLanguage.*;
import static org.epics.pvmanager.util.TimeDuration.*;

import gov.bnl.channelfinder.api.Channel;
import gov.bnl.channelfinder.api.ChannelFinderClient;
import gov.bnl.channelfinder.api.ChannelUtil;
import gov.bnl.channelfinder.api.Property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.csstudio.utility.pvmanager.ui.SWTUtil;
import org.csstudio.utility.pvmanager.widgets.VTableDisplay;
import org.csstudio.utility.pvmanager.widgets.WaterfallWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.epics.pvmanager.PVManager;
import org.epics.pvmanager.PVReader;
import org.epics.pvmanager.PVReaderListener;
import org.epics.pvmanager.data.VTable;
import org.epics.pvmanager.data.VTableColumn;
import org.epics.pvmanager.expression.DesiredRateExpressionList;

public class PVTableByPropertyWidget extends Composite {
	
	private VTableDisplay table;

	public PVTableByPropertyWidget(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout(SWT.HORIZONTAL | SWT.VERTICAL));
		table = new VTableDisplay(this);
	}
	
	private String channelQuery;
	private String rowProperty;
	private String columnProperty;
	
	private List<List<String>> cellPvs;
	private List<String> columnNames;
	private List<String> rowNames;
	
	private PVReader<VTable> pv;
	private PVReaderListener listener = new PVReaderListener() {
		
		@Override
		public void pvChanged() {
			if (!table.isDisposed()) {
				table.setVTable(pv.getValue());
			}
		}
	};
	
	private void reconnect() {
		if (pv != null) {
			pv.close();
			pv = null;
		}
		
		if (columnNames == null || rowNames == null || cellPvs == null ||
				cellPvs.size() != columnNames.size()) {
			// Invalid data: don't connect
			return;
		}
		
		VTableColumn[] columns = new VTableColumn[columnNames.size() + 1];
		columns[0] = column(rowProperty + " \\ " + columnProperty, vStringConstants(rowNames));
		for (int nColumn = 0; nColumn < columnNames.size(); nColumn++) {
			String name = columnNames.get(nColumn);
			List<String> columnPvs = cellPvs.get(nColumn);
			columns[nColumn + 1] = column(name, latestValueOf(channels(columnPvs)));
		}
		pv = PVManager.read(vTable(columns)).notifyOn(SWTUtil.swtThread()).every(ms(200));
		pv.addPVReaderListener(listener);
	}
	
	public String getChannelQuery() {
		return channelQuery;
	}
	
	public void setChannelQuery(String channelQuery) {
		this.channelQuery = channelQuery;
		queryChannels();
		computeTableChannels();
	}
	
	public String getRowProperty() {
		return rowProperty;
	}
	
	public void setRowProperty(String rowProperty) {
		this.rowProperty = rowProperty;
		computeTableChannels();
	}
	
	public String getColumnProperty() {
		return columnProperty;
	}
	
	public void setColumnProperty(String columnProperty) {
		this.columnProperty = columnProperty;
		computeTableChannels();
	}
	
	public Collection<Channel> getChannels() {
		return channels;
	}
	
	private Collection<Channel> channels;
	
	private void queryChannels() {
		try {
			// Should be done in a background task
			channels = ChannelFinderClient.getInstance().findChannelsByTag(channelQuery);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void computeTableChannels() {
		// Not have all the bits to prepare the channel list
		if (channels == null || rowProperty == null || columnProperty == null)
			return;
		
		// Find the rows and columns
		List<String> possibleRows = new ArrayList<String>();
		List<String> possibleColumns = new ArrayList<String>();
		// TODO replace this mess when the API gets better
		for (Channel channel : channels) {
			for (Property prop : channel.getProperties()) {
				if (prop.getName().equals(rowProperty)) {
					String value = prop.getValue();
					if (!possibleRows.contains(value)) {
						possibleRows.add(value);
					}
				}
				if (prop.getName().equals(columnProperty)) {
					String value = prop.getValue();
					if (!possibleColumns.contains(value)) {
						possibleColumns.add(value);
					}
				}
			}
		}
		Collections.sort(possibleRows);
		Collections.sort(possibleColumns);
		
		List<List<String>> cells = new ArrayList<List<String>>();
		for (int nColumn = 0; nColumn < possibleColumns.size(); nColumn++) {
			List<String> column = new ArrayList<String>();
			for (int nRow = 0; nRow < possibleRows.size(); nRow++) {
				column.add(null);
			}
			cells.add(column);
		}
		
		for (Channel channel : channels) {
			String row = null;
			String column = null;
			// TODO replace this mess when the API gets better
			for (Property prop : channel.getProperties()) {
				if (prop.getName().equals(rowProperty)) {
					row = prop.getValue();
				}
				if (prop.getName().equals(columnProperty)) {
					column = prop.getValue();
				}
			}
			
			int nRow = possibleRows.indexOf(row);
			int nColumn = possibleColumns.indexOf(column);
			
			if (nRow != -1 && nColumn != -1) {
				cells.get(nColumn).set(nRow, channel.getName());
			}
		}
		
		columnNames = possibleColumns;
		rowNames = possibleRows;
		cellPvs = cells;
		
		System.out.println("Computed: " + columnNames + " " + rowNames + " " + cellPvs);
		
		reconnect();
	}

}
