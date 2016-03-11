package com.app.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.options.Options;
import ehb.analysis.CallGraphBuilder;
import ehb.global.EHBOptions;
import ehb.global.Global;
import ehb.xml.manifest.CallBackGenerator;
import ehb.xml.manifest.ProcessManifest;
import ehb.xml.resource.ProcessResource;
import ehb.xml.resource.ResourceAttributes;

/**
 * good programming<--beauty<--perfect
 * */
public class Main {
	
	//total number of CLASS, Method, LOC and act in app
	public static int totalMethod; 
	public static int totalClass;
	public static int totalLine;
	public static int totalAct;
	
	public static String source = "source";
	public static String output = "output";
	
	public static String ANALYSISRESULT;
	public static String MOC;
	
	public static List<String> methods = new ArrayList<String>();
	
	public static void main(String[] args){
		
		//windows
		String params[] = {"-android-jars","D:/adt-eclipse/sdk/platforms","-process-dir", "L:/EHBbenchmarks/TestedApk_Top100/"+AppDir.APPNAME+".apk"};
		String apk = params[3];
		EHBOptions.v().setStrategy(EHBOptions.MANYSEQ);
		output = output+"/"+AppDir.APPNAME;
		
//		EHBOptions.v().setStaticAnalysis(true);
		
		//EHBDroid.jar
		//String params[] = {"-android-jars",args[0],"-process-dir", srcApk+"/"+args[1]+".apk"};		
		
		analyzeXML(apk);
		analyzeCode(apk, params);
		
		ANALYSISRESULT = output+"/"+Global.v().getPkg()+"_EhbEvents.txt";
		MOC = output+"/"+Global.v().getPkg()+"_moc.txt";
		output();
	}

	//start to analyze XML, to get two maps: <id, Calllback> and <Component, IntentFilter>, and write these two maps to file.
	public static void analyzeXML(String apk){
		
		//parse AndroidManifest.xml to get activity, service or receiver that contains IntentFilter, then add <component, IntentFilter> to Global.v();
		//write activityTointentFilter, serviceToIntentFilter and receiverToIntentFilter to global.
		ProcessManifest processMan = new ProcessManifest();
		processMan.loadManifestFile(apk);
		processMan.addToGlobal();
		
		//parse resource files to get XML elements that contains onClick=" ", then get @ResourceAttributes.
		ProcessResource processResource = new ProcessResource();
		processResource.loadResourceFile(apk);
		List<ResourceAttributes> resourceAttributes = processResource.getResources();
		
		//parse resourceAttributes and get map <id, Callback>, then add to Global. 
		CallBackGenerator generator = new CallBackGenerator(resourceAttributes);
		generator.generate();
		generator.addToGlobal();
	}

	//start to analyze Code
	private static void analyzeCode(String apk, String[] params) {
		
		//1. build callgraph for static analysis
		CallGraphBuilder cgb = new CallGraphBuilder(apk);
		cgb.build();
		cgb.addToGlobal();
		
		G.reset();
//		Scene.v().setCallGraph(Global.v().getCallGraph());
		//2. reset Soot
		initSoot(params);

		//3. start to instrument app
		instrumentApp(params);
	}
	
	//output App's information
	private static void output() {
		writeTotalNumbers();
		writeEHBEvents();
		String seriDir = output+"/" + AppDir.XMLEVENT + ".txt";
		writeFile(seriDir);
//		System.out.println(methods);
	}	

	//start to instrument app
	private static void instrumentApp(String[] params) {
		AndroidInstrumentor androidInstrument = new AndroidInstrumentor();
		androidInstrument.instrument(params);
	}
	
	/**
	 * store five elements: viewToCallBacks,activityToFilters,serviceToFilters,receiverToFilters, mainActivity
	 * */
	private static void writeFile(String location) {
		File file = new File(location);
		try {
			if(file.exists()){
				file.delete();
				file = new File(location);
			}
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(CallBack.viewToCallBacks);
			oos.writeObject(Global.v().getActivityToFilters());
			oos.writeObject(Global.v().getServiceToFilters());
			oos.writeObject(Global.v().getReceiverToFilters());
			oos.writeObject(Global.v().getMainActivity());
			oos.writeObject(EHBOptions.v().getStrategy());
			oos.flush();
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
//	private static void recordAnalysisResult(String pathName){
//		
//	}
	
	private static void writeEHBEvents(){
//		String string;
		StringBuilder sb = new StringBuilder();
		for(String s: Global.v().getEHBEventsSet()){
			sb.append(s+"\n");
		}
		writeContentToFile(ANALYSISRESULT, sb.toString());
	}
	
	private static void writeTotalNumbers(){
		System.out.println("MainActivity: "+Global.v().getMainActivity()+"\nNumbers of Activities: "+Global.v().getActivities().size()+"\nActivities: "+Global.v().getActivities());
		for(int i: Global.v().getIdToCallBack().keySet())
			System.out.println("id: "+i+" methods: "+Global.v().getIdToCallBack().get(i));
		int actSize = Global.v().getActivities().size();
		String totalNumbers = "Activitys Count: "+actSize+" Methods Count:"+totalMethod+" Classes Count: "+totalClass+" LOC:"+totalLine;
		System.out.println(totalNumbers);
		writeContentToFile(MOC, totalNumbers);
	}
	
	private static void writeContentToFile(String pathName,String content){
		File file = new File(pathName);
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			fos.write(content.getBytes());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void initSoot(String[] args){
		Options.v().set_soot_classpath(args[3]+";"+
				"lib/rt.jar;" +
				"lib/jce.jar;" +
				"lib/tools.jar;" +
				"lib/android.jar;"+
				"lib/android-support-v4.jar;"+
				"bin"
//				"EHBDroid.jar" // for EHBDroid.jar
//				"category/;"
				);	
//		Options.v().set_validate(true);
//		Options.v().set_android_jars(args[1]);
		Options.v().set_src_prec(Options.src_prec_apk);
//		Options.v().set_output_format(Options.output_format_jimple);
//		Options.v().set_output_dir("JimpleOutput");
		Options.v().set_output_format(Options.output_format_dex);
		Options.v().set_output_dir(output);
		Options.v().set_allow_phantom_refs(true);
	
//		Scene.v().addBasicClass("android.app.Dialog",SootClass.BODIES);
//        Scene.v().addBasicClass("android.view.MenuItem",SootClass.BODIES);
//        Scene.v().addBasicClass("android.view.View",SootClass.BODIES);
//        Scene.v().addBasicClass("android.content.Context",SootClass.BODIES);
//        Scene.v().addBasicClass("android.view.MenuItem$OnMenuItemClickListener",SootClass.BODIES);
	        
		for(String classAsSignature:CallGraphBuilder.getClassesAsSignature()){
			Scene.v().addBasicClass(classAsSignature,SootClass.SIGNATURES);
		}
		
//        applicationClasses.add("android.app.Dialog");
//        applicationClasses.add("android.view.MenuItem");
//        applicationClasses.add("android.view.View");
//        applicationClasses.add("android.content.Context");
//        applicationClasses.add("android.view.MenuItem$OnMenuItemClickListener");

       
		for(String classAsBody:CallGraphBuilder.getApplicationClasses()){
			Scene.v().addBasicClass(classAsBody,SootClass.BODIES);
		}
		for (String className : CallGraphBuilder.getEntrypoints()){		
			Scene.v().addBasicClass(className, SootClass.BODIES);	
		}		
		Scene.v().loadNecessaryClasses();
		
//		Map<String, List<String>> buildCallBackFunctions = buildCallBackFunctions();
//		
//		entrypoints.addAll(buildCallBackFunctions.keySet());
//		SootMethod entry2 = buildDummyMainMethod(buildCallBackFunctions); 
//		Scene.v().setEntryPoints(Collections.singletonList(entry2));
//		CHATransformer.v().transform();
//		Global.v().setCallGraph(Scene.v().getCallGraph());
	}
	
	public static final Set<String> signCheckStmts = new HashSet<String>();
	
	static{
		signCheckStmts.add("void checkSignLegal(android.content.Context)");
	}
	
	
}
