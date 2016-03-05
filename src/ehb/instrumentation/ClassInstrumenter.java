package ehb.instrumentation;

import soot.Scene;
import soot.SootClass;
import ehb.analysis.CallGraphBuilder;

/**
 * class instrumenter
 * */
public class ClassInstrumenter implements IInstrumenter {

	/**
	 * instrument new classes to Activity.<br>
	 * New Classes come from Main.applicationClasses
	 * */
	@Override
	public void instrument() {
		addNewClasses();
	}

	private void addNewClasses() {
		for(String classAsApplication:CallGraphBuilder.getApplicationClasses()){
			if(!classAsApplication.startsWith("android")){
				SootClass sClass = Scene.v().getSootClass(classAsApplication);
				if(!Scene.v().getApplicationClasses().contains(sClass)){
					sClass.setApplicationClass();
				}
			}
		}
	}
}
