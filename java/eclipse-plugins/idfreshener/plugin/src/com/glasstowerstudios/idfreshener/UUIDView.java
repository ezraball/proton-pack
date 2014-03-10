package com.glasstowerstudios.idfreshener;

import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

public class UUIDView extends ViewPart {

	public UUIDView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		mTable = new Table(parent, SWT.SINGLE);
		mTable.setHeaderVisible(true);
		mTable.setLinesVisible(true);

		TableColumn column1 = new TableColumn(mTable, SWT.LEFT);
		TableColumn column2 = new TableColumn(mTable, SWT.LEFT);

		column1.setWidth(200);
		column1.setText("Generation Date");
		column2.setWidth(500);
		column2.setText("UUID");
	}
	
	public void addUUID(String aDescription, UUID aUID) {
		TableItem row1 = new TableItem(mTable, SWT.NONE);
		row1.setText(new String[] { aDescription, aUID.toString() });
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	private Table mTable;
	
	public static final String VIEW_ID = "idfreshener.uuidview";

}
