package com.glasstowerstudios.idfreshener;
import java.util.Date;
import java.util.UUID;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ShowUUIDViewActionHandler implements
		IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		try {
			window.getActivePage().showView(UUIDView.VIEW_ID);
			UUIDView view = (UUIDView) window.getActivePage().findView(UUIDView.VIEW_ID);
			UUID uuid = UUID.randomUUID();
			view.addUUID(new Date().toString(), uuid);
			try {
				changeSerialVersionUIDInEditor(uuid.getMostSignificantBits());
			} catch (UUIDNotFoundException e) {
				// We didn't find a UUID...
				System.out.println("NO UUID FOUND IN FILE");
			}
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

	private class UUIDNotFoundException extends Exception {
		public UUIDNotFoundException() {}
	};
	
	private void changeSerialVersionUIDInEditor(long aNewUUID) throws UUIDNotFoundException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (!(editor instanceof AbstractTextEditor)) {
			return;
		}
		
		ITextEditor textEditor = (ITextEditor)editor;
		IDocumentProvider docProv = textEditor.getDocumentProvider();
		IDocument doc = docProv.getDocument(editor.getEditorInput());
		FindReplaceDocumentAdapter searcher = new FindReplaceDocumentAdapter(doc);
		try {
			IRegion foundText = searcher.find(0, "serialVersionUID(.*)=(.*);", true, true, false, true);
			searcher.replace("serialVersionUID = " + Long.toString(aNewUUID) + "L;", false);
		} catch (BadLocationException e) {
			throw new UUIDNotFoundException();
		} catch (IllegalStateException e1) {
			throw new UUIDNotFoundException();
		}
	}
}
