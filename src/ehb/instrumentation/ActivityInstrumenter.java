package ehb.instrumentation;

import soot.Scene;
import soot.SootClass;

import com.app.test.exception.ClassIsNotActivityException;

/**
 * add 5 fields and 2 methods to activities to activity.
 * 5 fields: uiLinkedList, systemLinkedList, interAppLinkedList, isvisited, methodList. 
 * 2 methods: clinit and onCreateOptionMenu.
 * */
public class ActivityInstrumenter implements IInstrumenter {

	SootClass sc;

	public ActivityInstrumenter(SootClass sc) {
		this.sc = sc;
	}

	/**
	 * add 5 fields: uiLinkedList, systemLinkedList, interAppLinkedList, isvisited, activityMenu, contextMenu
	 * and 2 methods: clinit and onCreateOptionMenu   
	 * */
	@Override
	public void instrument() {
		instrumentActivity(sc);
	}

	private void instrumentActivity(SootClass sc) {

		//check whether sc is a real activity(extends to android.app.activity)
		try {
			isTrueActivity(sc);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		addFieldToActivity(sc);
		addMethodToActivity(sc);
	}

	/**
	 * add 2 methods: clinit and onCreateOptionMenu
	 * @param sc activity
	 * */
	private void addMethodToActivity(SootClass sc) {
		new MethodInstrumenter(sc).instrument();
	}

	/**
	 * add 6 fields: uiLinkedList, systemLinkedList, interAppLinkedList, isvisited, activityMenu, contextMenu
	 * @param sc activity
	 * */
	private void addFieldToActivity(SootClass sc) {
		new FieldInstrumenter(sc).instrument();
	}

	//activity declared in androidmanifest is not a activity in code.
	private void isTrueActivity(SootClass sc)
			throws ClassIsNotActivityException {
		if (!Scene.v().getActiveHierarchy().getSuperclassesOf(sc)
				.contains(Scene.v().getSootClass("android.app.Activity"))) {
			throw new ClassIsNotActivityException(sc
					+ "declared in manifest.xml activity is not an activity");
		}
	}
}
