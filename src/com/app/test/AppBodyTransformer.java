package com.app.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import soot.Body;
import soot.BodyTransformer;
import soot.PatchingChain;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.util.Chain;

import com.app.test.methodCoverage.MethodCoverageFieldInstrumenter;
import com.app.test.methodCoverage.MethodCoverageStmtsInstrumenter;

import ehb.global.Global;
import ehb.global.GlobalHost;
import ehb.instrumentation.ActivityInstrumenter;
import ehb.instrumentation.BodyInstrumenter;
import ehb.instrumentation.ApplicationClassInstrumenter;
import ehb.instrumentation.FieldInstrumenter;

/**
 * There are three events in Android: UIEvent, SystemEvent, InterAppEvent.
 * 
 * UIEvent consists of: XML, Stmt and Method.
 * SystemEvent consists of: XML and stmt
 * InterAppEvent consists of: XML.
 * 
 * 1. In event collecting and dispatching phase,
 * Stmt of UIEvent and SystemEvent are dispatched to belonging activity, and will be stored in every activity.
 * While the the belonging activity of event that defined in XML or method can be easily found, there is no need 
 * to dispath them. 
 * 
 * 2. In event triggerrig phase:
 * UIEvent and SystemEvent¡¢InterAppEvent are triggerred seperately. 
 * UIEvent is controlled by "Test";
 * SystemEvent¡¢InterAppEvent are controlled by "SysTest";
 * 
 * */
public class AppBodyTransformer extends BodyTransformer implements GlobalHost {

	public static Set<String> activities = (HashSet<String>)(((HashSet<String>)Global.v().getActivities()).clone());
	
	public static boolean classIntrumented = false;
	
	String lastClass = ""; //recording the last class name
	String mainActivity = Global.v().getMainActivity();
	
	@Override
	protected void internalTransform(Body b, String phaseName,
			Map<String, String> options) {
		
		//1. add new classes
		if(!classIntrumented){
			instrumentApplicationClasses();
			instrumentFieldForMainActivity();
			classIntrumented = true;
		}
		
		final SootClass sc = b.getMethod().getDeclaringClass();
		String name = sc.getName();
		if(name.startsWith("android")||name.startsWith("java")||name.startsWith("com.facebook"))
			return;
		
		//2. instrument activity
		if(isActivity(sc)){
			instrumentActivity(sc);
		}
		
		//3. instrument body
		instrumentBody(b);
		
		new SignCheckRemover(b).removeSignCheckingStmt();
		//4. instrument method coverage stmts
		//if(!isExcludedBody(b)){
//			instrumentMethodCoverage(b, sc);
//			calculateCounts(b, sc);
//		}
	}

	//add SYSTEMEVENTLINKEDLIST fields to mainActivity
	private void instrumentFieldForMainActivity() {
		new FieldInstrumenter(Global.v().getmActivity()).instrument();
	}

	private void instrumentApplicationClasses() {
		new ApplicationClassInstrumenter().instrument();
	}

	private void instrumentActivity(SootClass sc) {
		new ActivityInstrumenter(sc).instrument();
	}

	//instrument body
	private void instrumentBody(Body b) {
		new BodyInstrumenter(b).instrument();
	}
	
	//instrument field and statements
	private void instrumentMethodCoverage(Body b, SootClass sc) {
		new MethodCoverageFieldInstrumenter(sc).instrument();
		new MethodCoverageStmtsInstrumenter(b).instrument();
	}

	private boolean isActivity(SootClass sc) {
		String name = sc.getName();
		if(activities.contains(name)){
			activities.remove(sc.getName());
			return true;
		}
		return false;
	}
	
	public void calculateCounts(Body b, SootClass sc){
		if(b.getMethod().isConcrete()){
			PatchingChain<Unit> units = b.getUnits();
			Main.totalLine = Main.totalLine+units.size();
			if(sc.getName()!=lastClass){
				lastClass = sc.getName();
				Main.totalClass++;
			}
			Main.totalMethod++;
			Main.methods.add(b.getMethod().toString());
		}
	}
	
	/**
	 * We claim the body whose name is <clinit> or <init> with 3 or 5 init statements or access$ 
	 * as invalid body.
	 * 
	 *<p>
	 *<li>If the class is a out class(not an inner class)
	 *The init method usually will overrides its super class's construct by default. 
	 * The following is an example of an activity overrides super class android.app.Activity:
	 * <pre class="prettyprint">
	 * public void <init>(){
        com.adobe.reader.AdobeReader $r0;

        $r0 := @this: com.adobe.reader.AdobeReader;
        specialinvoke $r0.<android.app.Activity: void <init>()>();
        return;}
       </pre>
	 *</p>
	 *
	 *<li> if the class is an inner class
	 *The init method usually will overrides its super class's construct by default. 
	 * The following is an example of an activity overrides super class android.view.OnItemLongClickListener:
	 * <pre class="prettyprint">
	 * public void <init>(){
	$r0 := @this: a2dp.Vol.main$4
	$r1 := @parameter0: a2dp.Vol.main
	$r0.<a2dp.Vol.main$4: a2dp.Vol.main this$0> = $r1
	specialinvoke $r0.<java.lang.Object: void <init>()>()
	return}
       </pre>
	 *</p>
	 * @param b body to be analyzed
	 * */
	public boolean isExcludedBody(Body b){
		String methodName = b.getMethod().getName();
		SootClass sc = b.getMethod().getDeclaringClass();
		
		SootMethod method = b.getMethod();
		if(methodName.equals("<clinit>")){
			return true;
		}
		//static inner class's stmts num is 3
		else if(methodName.equals("<init>")){
			PatchingChain<Unit> units = b.getUnits();
			if(sc.getName().contains(".R$")){
				if(units.size()==3){
					return true;
				}
			}
			else{
				int stmtsNum = calculateStmtsNum(b.getMethod());
				if(units.size()==stmtsNum){
					return true;
				}
			}
		}
		else if(methodName.startsWith("access$")){
			return true;
		}
		return false;
	}
	
	@Override
	public void addToGlobal() {
		
	}
	
	/**
	 * initMethod is an innerClass,
	 * if contains 1 $, the stmtsNum is 5.
	 * if contains 2 $, the stmtsNum is 7. 
	 * */
	public int calculateStmtsNum(SootMethod initMethod){
		if(!initMethod.getName().equals("<init>")){
			return -1;
		}
		SootClass sootClass = initMethod.getDeclaringClass();
		String className = sootClass.getName();
		int statementsNum = 3;
		if(className.contains("$")){
			String[] split = className.split("\\$");
			if(split[1].matches("[0-9]")){
				statementsNum = 2*split.length+1;
			}
		}
		//if s is a special character, like $,|, we should use \\$ instead of $
		
		return statementsNum;
	}
	
	public void print(Chain<Unit> units){
		for(Unit u:units){
			System.out.println("Units: "+u);
		}
	}
	
}
