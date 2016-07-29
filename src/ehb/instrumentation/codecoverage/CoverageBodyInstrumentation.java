package ehb.instrumentation.codecoverage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.app.test.methodBuilder.MethodBuilder;

import ehb.analysis.IAnalysis;
import ehb.builderFactory.LocalBuilder;
import ehb.instrumentation.IInstrumenter;
import jas.Method;
import soot.Body;
import soot.PatchingChain;
import soot.Unit;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceInvokeExpr;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class CoverageBodyInstrumentation extends LocalBuilder implements IInstrumenter {

	Body body;

	public CoverageBodyInstrumentation(Body b) {
		this.body = b;
	}

	@Override
	public void instrument() {
		boolean isValid = CoverageClassInstrumentation.ValidMethodAnalysis.isValidMethod(body.getMethod());
		if(isValid){
			
		}
	}
	
	public void instrumentBody(){
		MethodAnaysis analysis = new MethodAnaysis(body);
		Set<Unit> branchUnits = analysis.getBranchUnits();
		Unit startUnit = analysis.getStartUnit();
		int branch = analysis.getBranch();
	}

	
	//---------------------------------------------------------------------------------
	public static class MethodAnaysis implements IAnalysis{
	
		/**
		 * a branch unit is a unit with two or more processors
		 */
		private Set<Unit> branchUnits = new HashSet<>();
		
		private int branch;
		
		/**
		 * In Jimple, Identity Stmt must be at the first line.
		 * In Java, super() and this() must be at the first line.
		 * 
		 * Therefore, the structure of a jimple method must be like:
		 * 1. Identity Statements
		 * 2. super() or this() 
		 * 3. other statments.
		 */
		private Unit startUnit;
		
		private Body body;
		
		public MethodAnaysis(Body b){
			this.body = b;
			analyze();
		}

		@Override
		public void analyze() {
			
			ExceptionalUnitGraph unitGraph = new ExceptionalUnitGraph(body);
			PatchingChain<Unit> units = body.getUnits();
			
			for(Unit unit:units){
				if(unitGraph.getSuccsOf(unit).size()>1){
					branchUnits.add(unit);
				}
				//TODO 应该没有问题吧, thisRef
				if(unit instanceof IdentityStmt){
					startUnit = unit;
				}
			}
		}

		public Set<Unit> getBranchUnits() {
			return branchUnits;
		}

		public int getBranch() {
			return branchUnits.size();
		}

		public Unit getStartUnit() {
			return startUnit;
		}
	}
}
