package com.app.test;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Intent;

import com.app.test.data.AndroidIntentFilter;


public class TestA {
	
	public static Object object = "String";
	
	public static void test(){
		System.out.println(new TestA()+" 123.");
	}
	
	public static void main(String[] args) {
		Map<String, List<AndroidIntentFilter>> receiverToFilters = readReceiverToFilters("C:/Users/lenovo/workspace/EHBDroid/output/AutoAnswer/AutoAnswer_xmlevents.txt");
		for(String key:receiverToFilters.keySet()){
			
			for(AndroidIntentFilter value:receiverToFilters.get(key)){
				System.out.println(key+" "+value);
//				Class receiverClass = Class.forName(key);
//				Object receiver = receiverClass.newInstance();
//				BroadcastReceiver broadcastReceiver = (BroadcastReceiver)receiver;
//				Intent intent = initIntent(value);
//				Util.LogReceiverEvent(broadcastReceiver, intent);
//				activity.sendBroadcast(intent);
			}
		}
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
