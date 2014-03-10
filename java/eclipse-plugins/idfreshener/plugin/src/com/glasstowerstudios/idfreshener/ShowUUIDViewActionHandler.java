package com.glasstowerstudios.idfreshener;
import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;

import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
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
				IDocument doc = getDocumentFromEditor();
				IProblem[] problems = getProblemsForDocument(doc);
				for (IProblem prob : problems) {
					System.out.println("Problem found with message: " + prob.getMessage());
					if (prob.getID() == IProblem.MissingSerialVersion) {
						System.out.println("Found problem where serial version is missing.");
						return;
					}
				}
				
				System.out.println("No UUID found in file, and problem not found.");
//				IProblem missingUUID = IProblem.MissingSerialVersion;
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
	
	private class UUIDProblemRequestor implements IProblemRequestor {

		@Override
		public void acceptProblem(IProblem problem) {
			System.out.println("Accepted problem: " + problem.getMessage());
		}

		@Override
		public void beginReporting() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void endReporting() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isActive() {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
	
	private IProblem[] getProblemsForDocument(IDocument aDocument) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setCompilerOptions(prepOptions());
		parser.setSource(aDocument.get().toCharArray());
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		return cu.getProblems();
	}
	
	private Hashtable<String, String> prepOptions() {
		Hashtable<String, String> ht = (Hashtable<String,String>)JavaCore.getOptions();
		ht.put("org.eclipse.jdt.core.compiler.problem.suppressWarnings", "disabled");
		ht.put("org.eclipse.jdt.core.compiler.problem.missingSerialVersion", "error");
		
		for (String key : ht.keySet()) {
			System.out.println(key + " -> " + ht.get(key));
		}
		JavaCore.setOptions(ht);
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, ht);
		return ht;
	}
	private IDocument getDocumentFromEditor() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IEditorPart editor = window.getActivePage().getActiveEditor();
		if (!(editor instanceof AbstractTextEditor)) {
			return null;
		}
		
		ITextEditor textEditor = (ITextEditor)editor;
		IDocumentProvider docProv = textEditor.getDocumentProvider();
		IDocument doc = docProv.getDocument(editor.getEditorInput());
		return doc;
	}
	
	private void changeSerialVersionUIDInEditor(long aNewUUID) throws UUIDNotFoundException {
		IDocument doc = getDocumentFromEditor();
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
