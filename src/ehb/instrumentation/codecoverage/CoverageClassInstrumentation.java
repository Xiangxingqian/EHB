package ehb.instrumentation.codecoverage;

import java.util.ArrayList;
import java.util.List;

import com.app.test.Constants;

import ehb.analysis.IAnalysis;
import ehb.builderFactory.LocalBuilder;
import ehb.instrumentation.CoverageClinit;
import ehb.instrumentation.IInstrumenter;
import soot.Body;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;

/**
 * 代码覆盖率之--类插桩，类插桩分为两个步骤：
 * 1. 添加静态代码块
 * 2. 修改静态代码块
 * 
 * 存在一个问题: 如果一个类是内部类，其不能定义静态代码块，因此内部类的初始化在其最外部类中(注意：内部类的嵌套)
 * 
 * @author Administrator
 * 
 * Usage: new CoverageClassInstrumentation(sc).instrument();
 *
 */
public class CoverageClassInstrumentation extends LocalBuilder implements IInstrumenter{
	
	public static List<String> visitedClasses = new ArrayList<>();
	public static List<String> visitedInnerClasses = new ArrayList<>();
	
	private SootClass sc;
	private boolean isInnerClass;//is an inner class?
	
	/**
	 * Jimple transforms some Java elements(e,g. static block, constructor and access) to methods
	 * (<clinit>, <init>, <access$1> etc), we should exclude such methods. 
	 * validMethodCount represents the valid method count in a jimple class.
	 */
	private int validMethodCount;
	
	public CoverageClassInstrumentation(SootClass sc){
		this.sc = sc;
		isInnerClass = new InnerClassAnalysis(sc).isInnerClass();
		validMethodCount = new ValidMethodAnalysis(sc).getValidMethodCount();
	}
	
	/**
	 * 只供内部类调用
	 * @param sc
	 * @param validMethodCount
	 */
	public CoverageClassInstrumentation(SootClass sc, int validMethodCount){
		this.sc = sc;
		isInnerClass = false;
		this.validMethodCount = validMethodCount;
	}

	@Override
	public void instrument() {
		if(isInnerClass){
			if(!visitedInnerClasses.contains(sc.getName())){
				visitedInnerClasses.add(sc.getName());
				SootClass outClass = new InnerClassAnalysis(sc).getOutClass();
				new CoverageClassInstrumentation(outClass, validMethodCount).instrument();;
			}
		}else {
			if(!visitedClasses.contains(sc.getName())){
				visitedClasses.add(sc.getName());
				handleClinit();
			}
		}
	}
	
	private void handleClinit(){
		if(sc.declaresMethodByName("<clinit>"))
			modifyClinit();
		else 
			addClinit();
		CoverageGlobals.reset();
	}
	
	/**
	 * Insert 
	 */
	private void modifyClinit(){
		SootMethod clinitMethod = sc.getMethodByName("<clinit>");
		Body b = clinitMethod.retrieveActiveBody();
		PatchingChain<Unit> units = clinitMethod.getActiveBody().getUnits();
		
		List<Value> values = new ArrayList<>();
		values.add(IntConstant.v(CoverageGlobals.classIndex));
		values.add(IntConstant.v(validMethodCount));
		
		InvokeStmt initbbblllij = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(Constants.codeCoverageToolkitInitbbblllij.makeRef(), values));
		Unit last = units.getFirst();
		units.insertBefore(initbbblllij, last);
		b.validate();
	}
	
	private void addClinit(){
		new CoverageClinit(sc, CoverageGlobals.classIndex, validMethodCount).build();
	}
	
	
	//----------------------------------------------------------------------------
	public static class InnerClassAnalysis{
		
		SootClass sc;
		
		public InnerClassAnalysis(SootClass sc) {
			this.sc = sc;
		}
		
		public boolean isInnerClass(){
			return sc.getName().contains("$");
		}
		
		public SootClass getOutClass(){
			if(isInnerClass()){
				return parseOutClass(sc);
			}
			else 
				return sc;
		}
		
		private SootClass parseOutClass(SootClass sc){
			String outClassName = sc.getName().split("$")[0];
			return Scene.v().getSootClass(outClassName);		
		}
	}

	
	//----------------------------------------------------------------------------
	/**
	 * 分析一个class中有效的方法数
	 * @author Administrator
	 *
	 */
	public static class ValidMethodAnalysis implements IAnalysis{ 
		
		private SootClass sc;

		/**
		 * the count of valid method
		 */
		private int validMethodCount;
		
		public ValidMethodAnalysis(SootClass sc) {
			this.sc = sc;
			analyze();
		}
		
		public void analyze(){
			for(SootMethod sm:sc.getMethods()){
				if(isValidMethod(sm)){
					validMethodCount++;
				}
			}
		}

		public int getValidMethodCount() {
			return validMethodCount;
		}
		
		/**
		 * 判断一个方法是否是有效方法：它是否在Java代码中有体现。
		 * 
		 * <clinit>()对应static{}
		 * <init>()对应默认构造器
		 * access$()对应内部类获取外部类变量的入口
		 * 这三个方法在Java代码中是不存在的，因此排除他们
		 * 
		 * @param sm 
		 * @return whether sm is a valid method
		 */
		public static boolean isValidMethod(SootMethod sm){
			if(sm.isConcrete()){
				String subSignature = sm.getSubSignature();
				if(subSignature.contains("<clinit>()")||subSignature.contains("<init>()")||subSignature.contains("access$")){
					return false;
				}else return true;
				
			}else return false;
		}
	}
}
	
	
