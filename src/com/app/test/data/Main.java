package com.app.test.data;

import java.util.Set;

import ehb.analysis.CallGraphBuilder;
import ehb.analysis.EventDistinguisher;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class Main {
	public static void main(String[] args) {
		String params[] = {"-android-jars","D:/adt-eclipse/sdk/platforms","-process-dir", "D:/WorkSpace/Administrator/workspace/svn/TestedApk_Simple/"+args[0]+".apk","-option","oneevent"};
		String apkFileLocation = params[3];
		CallGraphBuilder cgb = new CallGraphBuilder(apkFileLocation);
		soot.G.reset();
		cgb.build();
		Set<String> entryPoint = cgb.getEntryPoint();
		System.out.println(entryPoint);
		test();
	}
	
	public static void test(){
		SootClass sootClass = Scene.v().getSootClass("cz.romario.opensudoku.gui.FileImportActivity");
		SootMethod method = sootClass.getMethodByName("onCreate");
		
		EventDistinguisher eventDistinguisher = new EventDistinguisher(method);
		System.out.println(eventDistinguisher.isActivityJumpingEvent());
		
		SootClass sootClass1 = Scene.v().getSootClass("cz.romario.opensudoku.gui.SudokuListActivity");
		SootMethod method1 = sootClass1.getMethodByName("onListItemClick");
		
		EventDistinguisher eventDistinguisher1 = new EventDistinguisher(method1);
		System.out.println(eventDistinguisher1.isActivityJumpingEvent());
		
	}
}
