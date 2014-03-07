package com.glasstowerstudios.idfreshener;
import java.util.Date;
import java.util.UUID;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;


public class ShowUUIDViewActionHandler implements
		IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			window.getActivePage().showView(UUIDView.VIEW_ID);
			UUIDView view = (UUIDView) window.getActivePage().findView(UUIDView.VIEW_ID);
			view.addUUID(new Date().toString(), UUID.randomUUID());
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}
