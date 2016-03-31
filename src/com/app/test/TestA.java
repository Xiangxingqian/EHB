package com.app.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.app.test.data.AndroidIntentFilter;


public class TestA {
	
	public static String str = "qian";
	
	public static Object object = "String";
	
	public static void main(String[] args) {
//		PClass pClass2 = new PClass();
		Class class1 = PClass.class;
		
		try {
			Field declaredField = class1.getDeclaredField("str1");
		} catch (NoSuchFieldException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		Constructor[] declaredConstructors = class1.getDeclaredConstructors();
//		Constructor constructor = declaredConstructors[0];
		for(Constructor con:declaredConstructors){
			con.setAccessible(true);
			Object newInstance;
			try {
				newInstance = con.newInstance(null);
				try {
					Method[] methods = class1.getMethods();
					
					Method declaredMethod = class1.getDeclaredMethod("setStr",new Class[]{String.class});
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PClass pClass = (PClass)newInstance;
				System.out.println(pClass.getString());
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
//		System.out.println(getCurrentActivityName());
	}

	public static String getCurrentActivityName(){
		try {
			Process exec = Runtime.getRuntime().exec("adb shell dumpsys activity");
			InputStream inputStream = exec.getInputStream();
			byte[] b = new byte[1024];
			StringBuilder sb = new StringBuilder();
			while (inputStream.read(b)!=-1) {
				String string = new String(b);
//				System.out.print(string);
				sb.append(string);
			}
			inputStream.close();
			String msg = sb.toString();
			
			String[] split = msg.split("Stack #1");
			String stackMsg = split[split.length-1];
							
			String[] split2 = stackMsg.split("\n");
			for(String s:split2){
				if(s.contains("Run #")){
					String[] split3 = s.split(" ");
					return split3[split3.length-2];
//					System.out.println(split3[split3.length-2]);
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return " ";
	}
	
	
	public static String getStr() {
		return str;
	}

	public static void setStr(String str) {
		TestA.str = str;
	}

	public static Object getObject() {
		return object;
	}

	public static void setObject(Object object) {
		TestA.object = object;
	}
	
	private static Map<String, List<AndroidIntentFilter>> readReceiverToFilters(String file){
		Map<String, List<AndroidIntentFilter>> receiverToFilters = new HashMap<String, List<AndroidIntentFilter>>();
		try{
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			//read the forth object. There are four objects in location: callback.class, activityToIntentFilters, serviceToIntentFilters and receiverToIntentFilters
			ois.readObject();
			ois.readObject();
			ois.readObject();
			Object readObject4 = ois.readObject();
			receiverToFilters = (Map<String, List<AndroidIntentFilter>>)readObject4;
			ois.close();
			fis.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return receiverToFilters;
	}
}

class B{
	
	public static Object bObject  = TestA.object;

	public static Object getbObject() {
		return bObject;
	}

	public static void setbObject(Object bObject) {
		B.bObject = bObject;
	}

}

class C{
	
	public static Object cObject;

	public static Object getcObject() {
		return cObject;
	}

	public static void setcObject(Object cObject) {
		C.cObject = cObject;
	}

	
}
