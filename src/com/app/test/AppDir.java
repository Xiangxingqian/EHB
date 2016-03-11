package com.app.test;

import java.util.LinkedList;


/**
 * AppDir will be instrumented into Android App.
 * */
public class AppDir {
	
	//additional file recording events defined in xml.
	public final static String APPNAME = "WordPress";
	public final static String XMLEVENT = APPNAME+"_xmlevents";
	public final static String SDCARD = "/mnt/sdcard/";
	public static LinkedList<Integer> linkedList = new LinkedList<Integer>();
	static{
		for(int i =0;i<500;i++)
			linkedList.add(0);
	}
	
	public static String file = SDCARD+XMLEVENT+".txt";
	
	public static int visitedMethodCount = 0;
	
//	public static void  main(String[] args) {
//		File f = new File("L:/EHBbenchmarks/TestedApkSimple");
//		String files = "";
//		for(String s:f.list()){
//			files = files+","+s.substring(0,s.indexOf("."));
//		}
//		System.out.println(files);
//	}
}
