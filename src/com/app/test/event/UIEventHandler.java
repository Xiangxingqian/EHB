package com.app.test.event;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.test.AppDir;
import com.app.test.CallBack;
import com.app.test.Constants;
import com.app.test.MathUtil;
import com.app.test.Util;

public class UIEventHandler{

	public static final String ONEEVENT = "oneevent";
	public static final String ONESEQ = "onesequence";
	public static final String MANYSEQ= "manysequence";
	
	public static final int fixSeq = 1;
	public static String file = AppDir.file;
	public static final String itemName = Constants.uiTest; 
	/**
	 * test UI events.
	 * <p> handle UI events who are from uilinkedList, menu, listActivity and
	 * preferenceActivity
	 * */
	public static void doUIEventTest(Activity activity) {
		
		String strategy = readEHBStgy(file);
		try {

			// Attention: XML UI event has been integreted into linkedList
			Field uiEventLinkedList_reflectField = activity.getClass()
					.getField(Constants.EHBField.UIEVENTLINKEDLIST);
			Object object = uiEventLinkedList_reflectField.get(null);
			LinkedList<UIEvent> uiEventLinkedList = (LinkedList<UIEvent>) object;
			UIEventTest.doUIEventFromLinkedListTest(uiEventLinkedList,
					strategy);

			UIEventTest.doUIEventFromMenuTest(activity);

			if (UIEventTest.containsListEvent(activity))
				UIEventTest.doUIEventFromListActTest((ListActivity) activity);

			if (UIEventTest.containsPreferenceAct(activity))
				UIEventTest.doUIEventFromPreActTest(activity);

		} catch (Exception e) {
			Util.LogException(e);
		} finally {
			Log.v("EVENT", AppDir.visitedMethodCount + "");
		}
	}

	//add menuItem to activity
	public static void addMenuItem(final Activity activity, Menu menu) {
		MenuItem add = menu.add(itemName);
		add.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		add.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				doUIEventTest(activity);
				return true;
			}
		});
	}

//	public void doTest(Activity activity) {
//		doUIEventTest(activity);
//	}

	/**
	 * handle each type of event
	 * */
	public static class UIEventTest {

		// if activity contains listEvent(isListActivity?)
		public static boolean containsListEvent(Activity activity) {
			return activity instanceof ListActivity;
		}

		// if activity contains preferenceEvent(isPreferenceActivity)
		public static boolean containsPreferenceAct(Activity activity) {
			return activity instanceof PreferenceActivity;
		}

		// 1. UI events from linkedList
		public static void doUIEventFromLinkedListTest(
				LinkedList<UIEvent> uiEvents, String stgy) {

			LinkedList<UIEvent> jumpLinkedList = new LinkedList<UIEvent>();
			LinkedList<UIEvent> nonJumpLinkedList = new LinkedList<UIEvent>();
			for (UIEvent uiEvent : uiEvents) {
				if (uiEvent.isJump())
					jumpLinkedList.add(uiEvent);
				else
					nonJumpLinkedList.add(uiEvent);
			}

			if (stgy.equals(ONEEVENT)) {
				doUIEventOneEventStgy(uiEvents);
			} else if (stgy.equals(ONESEQ)) {
				doUIEventOneSeqStgy(uiEvents);
			} else if (stgy.equals(MANYSEQ)) {
				doUIEventManySeqStgy(jumpLinkedList, nonJumpLinkedList);
			}
		}

		// 1.1 oneEventStgy @see EHBOptions.ONEEVENT
		public static void doUIEventOneEventStgy(LinkedList<UIEvent> uiEvents) {
			double d = Math.random();
			int size = uiEvents.size();
			int ramdonValue = (int) (d * size);
			UIEvent uiEvent = uiEvents.get(ramdonValue);
			handleUIEvent(uiEvent);
		}

		// 1.2 oneSeqStgy @see EHBOptions.ONESEQ
		public static void doUIEventOneSeqStgy(LinkedList<UIEvent> uiEvents) {
			for (UIEvent uiEvent : uiEvents) {
				handleUIEvent(uiEvent);
			}
		}

		// 1.3 mangSeqStgy @see EHBOptions.MANYSEQ
		public static void doUIEventManySeqStgy(LinkedList<UIEvent> jumpLinkedList,
				LinkedList<UIEvent> nonJumpLinkedList) {

			// a fixed num of event sequences, like 10.
			int fixNumber = fixSeq;

			// size of jump events
			int jumpSize = jumpLinkedList.size();

			// size of non jump events 
			int nonjumpSize = nonJumpLinkedList.size();

			List<List<Integer>> nonPermutation = MathUtil.getNPermutation(fixNumber,nonjumpSize);
			for (List<Integer> list : nonPermutation) {
				for (int i : list) {
					Log.v(Constants.LogTag.eventTag, i+"XX");
					UIEvent uiEvent = nonJumpLinkedList.get(i);
					handleUIEvent(uiEvent);
				}
			}
			
			// I would not perefer to permute the jumping events.
			List<List<Integer>> jumpPermutation = MathUtil.getNPermutation(fixNumber,jumpSize);
			for (List<Integer> list : jumpPermutation) {
				for (int i : list) {
					Log.v(Constants.LogTag.eventTag, i+"XXX");
					UIEvent uiEvent = jumpLinkedList.get(i);
					handleUIEvent(uiEvent);
				}
			}
			
		}

		/**
		 * handle ui event
		 * */
		public static void handleUIEvent(UIEvent uiEvent) {
			String callback = uiEvent.getCallback();
			Object listener = uiEvent.getListener();
			Object ui = uiEvent.getUi();
			Method method = Util.getMethod(listener.getClass(), callback);
			if (ui instanceof View) {
				UIEventAnalysis.doViewAnalysis(method, (View) ui, listener);
			} else if (ui instanceof Dialog) {
				UIEventAnalysis.doDialogAnalysis(method, (Dialog) ui, listener);
			}
		}

		/**
		 * 2. UI event from Menu, invoke onOptionsItemSelected(item)
		 * */
		public static void doUIEventFromMenuTest(Activity activity) {

			Field field;
			try {
				field = activity.getClass().getField(
						Constants.EHBField.ACTIVITYMENU);
				Object object = field.get(null);
				
				if(object==null){
					Log.v(Constants.LogTag.menuTag, "Menu is not used in "+activity.getClass().getName());
					return;
				}
				
				Menu menu = (Menu) object;

				for (int i = 0; i < menu.size(); i++) {
					MenuItem item = menu.getItem(i);
					CharSequence title = item.getTitle();
					// ignore self defined menuItem
					if (!(Constants.uiTest.equals(title)
							|| Constants.sysTest.equals(title) || Constants.interTest
								.equals(title))){
						Log.v(Constants.LogTag.eventTag, "Activity: "+activity.getClass().getName()+" MenuItem: "+item.getTitle());
						activity.onOptionsItemSelected(item);
						}
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 3. UI event from listActivity, invoke onListItemClick of ListActivity
		 * */
		public static void doUIEventFromListActTest(ListActivity activity) {
			Class<? extends ListActivity> class1 = activity.getClass();
			// void onListItemClick(ListView l, View v, int position, long id);
			ListView listView = activity.getListView();
			try {
				Method method = class1.getDeclaredMethod("onListItemClick",
						new Class[] { ListView.class, View.class, int.class,
								long.class });
				method.setAccessible(true);
				for (int i = 0; i < listView.getChildCount(); i++) {
					View childView = listView.getChildAt(i);
					Adapter adapter = listView.getAdapter();
					long itemId = adapter.getItemId(i);
					try {
						method.invoke(activity, new Object[] { listView,
								childView, i, itemId });
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
				}

			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 4. UI event from preference activity. invoke onItemClick of
		 * PreferenceActivity
		 * */
		public static void doUIEventFromPreActTest(Activity activity) {
			PreferenceActivity pActivity = (PreferenceActivity) activity;
			PreferenceScreen pScreen = pActivity.getPreferenceScreen();
			// int cou = pScreen.getPreferenceCount();
			ListView listView = pActivity.getListView();
			int count = listView.getCount();
			Log.v("EVENT", "listView count: " + count
					+ " listView child size: " + listView.getChildCount());
			for (int i = 0; i < count; i++) {
				pScreen.onItemClick(listView, null, i, 0);
			}
		}
	}
	
	/**
	 * handle each UI event
	 * we use view.getVisibility() to know if the view is visible. 
	 * If view is invisible, we ignore it, or invoke it.
	 * */
	public static class UIEventAnalysis {

		static void doViewAnalysis(Method method, View view, Object object) {
			//check the visibility of view£¬ if it is not visible, return
			if(!(view.getVisibility()==View.VISIBLE)){
//				Util.Log(view, object, method.getName()+"12345");
				return;
			}
			Util.Log(view, object, method.getName());
			String subSig = Util.getSubsignature(method);
			try {
				// void onStartTrackingTouch(android.widget.SeekBar)
				// void onStopTrackingTouch(android.widget.SeekBar)
				// void onClick(android.view.View)
				// boolean onLongClick(android.view.View)
				// void onNothingSelected(android.widget.AdapterView)
				if ((CallBack.ONLONGCLICK.equals(subSig))
						|| (CallBack.ONCLICK.equals(subSig))
						|| (CallBack.ONNOTHINGSELECTED.equals(subSig))
						|| (CallBack.onStartTrackingTouch.equals(subSig))
						|| (CallBack.onStopTrackingTouch.equals(subSig))) {
					method.invoke(object, new Object[] { view });
				}
				// void onCheckedChanged(android.widget.CompoundButton,boolean)
				// void onFocusChange(android.view.View,boolean)
				else if ((CallBack.ONFOCUSCHANGE.equals(subSig))
						|| (CallBack.onCheckedChanged.equals(subSig))) {
					Object[] params = new Object[2];
					params[0] = view;
					params[1] = true;
					method.invoke(object, params);
					params[1] = false;
					method.invoke(object, params);
				}
				// void onSystemUiVisibilityChange(int)
				else if (CallBack.ONSYSTEMUIVISIBILITYCHANGE.equals(subSig)) {
					method.invoke(object, new Object[] { 0 });
					method.invoke(object, new Object[] { 8 });
				}
				// void onItemClick(android.widget.AdapterView,android.view.View,int,long)
				// boolean onItemLongClick(android.widget.AdapterView,android.view.View,int,long)
				// void onItemSelected(android.widget.AdapterView,android.view.View,int,long)
				else if ((CallBack.ONITEMCLICK.equals(subSig))
						|| (CallBack.ONITEMLONGCLICK.equals(subSig))
						|| (CallBack.ONITEMSELECTED.equals(subSig))) {
					AdapterView adapterView = (AdapterView) view;
					int childCount = adapterView.getChildCount() > 5 ? 5
							: adapterView.getChildCount();

					for (int i = 0; i < childCount; i++) {
						View childView = adapterView.getChildAt(i);
						Adapter adapter = adapterView.getAdapter();
						long itemId = adapter.getItemId(i);
						method.invoke(object, new Object[] { view, childView,
								i, itemId });
					}
				}
				// void onProgressChanged(android.widget.SeekBar,int,boolean)
				else if (CallBack.onProgressChanged.equals(subSig)) {
					Object[] params = new Object[3];
					SeekBar seekBar = (SeekBar) view;
					params[0] = view;
					int max = seekBar.getMax(); // set process = max/2
					params[1] = max / 2;
					params[2] = true;
					method.invoke(object, params);
					params[2] = false;
					method.invoke(object, params);
				}
				// void onScrollStateChange(android.widget.AbsListView,int)
				else if (CallBack.onScrollStateChanged.equals(subSig)) {
					for (int i = 0; i < 3; i++) {
						method.invoke(object, new Object[] { view, i });
					}
				}
				// void onScroll(AbsListView view, int firstVisibleItem, int
				// visibleItemCount,int totalItemCount);
				else if (CallBack.onScroll.equals(subSig)) {
					AbsListView listView = (AbsListView) view;
					int childCount = listView.getCount();
					int start = listView.getFirstVisiblePosition();
					int stop = listView.getLastVisiblePosition();
					method.invoke(object, new Object[] { view, start, stop,
							childCount });
				}
				// void onPageScrollStateChanged(int)
				else if (CallBack.onPageScrollStateChanged.equals(subSig)) {
					for (int i = 0; i < 3; i++) {
						method.invoke(object, new Object[] { i });
					}
				}
				// void onPageScrolled(int,float,int)
				else if (CallBack.onPageScrolled.equals(subSig)) {
					method.invoke(object, new Object[] { 0, 0.2f, 0 });
					method.invoke(object, new Object[] { 0, 0.8f, 0 });
				}
				// void onPageSelected(int)
				else if (CallBack.onPageSelected.equals(subSig)) {
					method.invoke(object, new Object[] { 1 });
				}
				// boolean onClose()
				else if (CallBack.onClose.equals(subSig)) {
					method.invoke(object);
				}
				// void onScrollStateChange(android.widget.NumberPicker,int)
				else if (CallBack.NumberPicker_onScrollStateChange
						.equals(subSig)) {
					for (int i = 0; i < 3; i++) {
						method.invoke(object, new Object[] { view, i });
					}
				}
				// boolean onQueryTextSubmit(java.lang.String)
				else if (CallBack.onQueryTextSubmit.equals(subSig)) {
					method.invoke(object, "android");
				}
				// boolean onQueryTextChange(java.lang.String)
				else if (CallBack.onQueryTextChange.equals(subSig)) {
					method.invoke(object, "java");
				}
				// boolean onGenericMotion(android.view.View,android.view.MotionEvent)
				else if (CallBack.ONGENERICMOTION.equals(subSig)) {
					MotionEvent downEvent = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_SCROLL, view.getX(),
							view.getY(), 0);
					method.invoke(object, new Object[] { view, downEvent });
				}
				// boolean onTouch(android.view.View,android.view.MotionEvent)
				else if (CallBack.ONTOUCH.equals(subSig)) {
					MotionEvent downEvent = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_DOWN, view.getX(), view.getY(),
							0);
					method.invoke(object, new Object[] { view, downEvent });
					MotionEvent upEvent = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
							view.getX(), view.getY(), 0);
					method.invoke(object, new Object[] { view, upEvent });
				}
				// void onRatingChanged(android.widget.RatingBar,float,boolean)
				else if (CallBack.onRatingChanged.equals(subSig)) {
					RatingBar ratingBar = (RatingBar) view;
					int numStars = ratingBar.getNumStars();
					float rating = numStars / 2;
					method.invoke(object, new Object[] { view, rating, true });
				}
				// boolean onHover(android.view.View,android.view.MotionEvent)
				else if (CallBack.ONHOVER.equals(subSig)) {
					MotionEvent centerEvent = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_HOVER_ENTER, view.getX(),
							view.getY(), 0);
					method.invoke(object, new Object[] { view, centerEvent });
					MotionEvent exitEvent = MotionEvent.obtain(
							SystemClock.uptimeMillis(),
							SystemClock.uptimeMillis(),
							MotionEvent.ACTION_HOVER_EXIT, view.getX(),
							view.getY(), 0);
					method.invoke(object, new Object[] { view, exitEvent });
				}
				// boolean onSuggestionSelect(int)
				else if (CallBack.onSuggestionSelect.equals(subSig)) {
					SearchView sView = (SearchView) view;
					CursorAdapter suggestionsAdapter = sView
							.getSuggestionsAdapter();
					int count = suggestionsAdapter.getCount();
					if (count != 0)
						method.invoke(object, count - 1);
				}
				// boolean onSuggestionClick(int)
				else if (CallBack.onSuggestionClick.equals(subSig)) {
					SearchView sView = (SearchView) view;
					CursorAdapter suggestionsAdapter = sView
							.getSuggestionsAdapter();
					int count = suggestionsAdapter.getCount();
					if (count != 0)
						method.invoke(object, count - 1);
				}
				// boolean onDrag(android.view.View,android.view.DragEvent)
				else if (CallBack.ONDRAG.equals(subSig)) {
					// TODO
				}
				// boolean
				// onPreferenceTreeClick(android.preference.PreferenceScreen,android.preference.Preference)
				else if (CallBack.ONPREFERENCETREECLICK.equals(subSig)) {
					// done! We consider preference activity seperately @UIEventHandler.
				}
				// void onInflate(android.view.ViewStub,android.view.View)
				else if (CallBack.onInflate.equals(subSig)) {
					ViewStub stub = (ViewStub)view;
					View inflate = stub.inflate();
					method.invoke(object, new Object[]{stub,inflate});
				}
				// boolean
				// onEditorAction(android.widget.TextView,int,android.view.KeyEvent)
				else if (CallBack.ONEDITORACTION.equals(subSig)) {
					TextView tView = (TextView)view;
					KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER);
					method.invoke(object, new Object[]{tView, EditorInfo.IME_NULL, keyEvent});
				}
				// boolean onKey(android.view.View,int,android.view.KeyEvent)
				else if (CallBack.ONKEY.equals(subSig)) {
					TextView tView = (TextView)view;
					KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER);
					method.invoke(object, new Object[]{tView, KeyEvent.KEYCODE_ENTER, keyEvent});
				}
				// void onChildViewAdded(android.view.View,android.view.View)
				// void onChildViewRemoved(android.view.View,android.view.View)
				else if (CallBack.ONCHILDVIEWADDED.equals(subSig)
						|| CallBack.ONCHILDVIEWREMOVED.equals(subSig)) {
					// do nothing, I can hardly find an example.
				}
				//it seems that onCreateContextMenu was hardly used in the real-world app. 
				//void onCreateContextMenu(android.view.ContextMenu,android.view.View,android.view.ContextMenuInfo)
				else if (CallBack.ONCREATECONTEXTMENU.equals(subSig)) {
//					ContextMenu contextMenu = (ContextMenu)view;
//					int size = contextMenu.size();
//					for(int i = 0;i<size;i++){
//						MenuItem item = contextMenu.getItem(i);
//						TODO 
//					}
				}
				// handle event from layout event, like click(view);
				else {
					method.invoke(object, new Object[] { view });
				}
			} catch (Exception localException) {
				Util.LogException(localException);
			}
		}

		static void doDialogAnalysis(Method method, Dialog dialog, Object object) {
			Util.Log(dialog, object, method.getName());
			if(!dialog.isShowing())
				dialog.show();
			//TODO add dialog.show();
			String subSig = Util.getSubsignature(method);
			try {
				// void onCancel(android.content.DialogInterface)
				// void onDismiss(android.content.DialogInterface)
				// void onShow(android.content.DialogInterface)
				if ((CallBack.DialogInterface_onDismiss.equals(subSig))
						|| (CallBack.DialogInterface_onShow.equals(subSig))
						|| (CallBack.DialogInterface_onCancel.equals(subSig))) {
					method.invoke(object, new Object[] { dialog });
				}
				// void onClick(android.content.DialogInterface,int)
				else if (CallBack.DialogInterface_onClick.equals(subSig)) {
					method.invoke(object, new Object[] { dialog, -1 });
					method.invoke(object, new Object[] { dialog, -2 });
					method.invoke(object, new Object[] { dialog, -3 });
				}
				// void onClick(android.content.DialogInterface,int,boolean)
				else if (CallBack.DialogInterface_OnMultiChoiceClickListener_onClick
						.equals(subSig)) {
					method.invoke(object, new Object[] { dialog, -1, true });
					method.invoke(object, new Object[] { dialog, -2, true });
					method.invoke(object, new Object[] { dialog, -3, true });
				}
				// boolean onKey(android.content.DialogInterface,int,android.view.KeyEvent)
				else if (CallBack.DialogInterface_onKey.equals(subSig)) {
					KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_ENTER);
					method.invoke(object, new Object[]{dialog, KeyEvent.KEYCODE_BACK, keyEvent});
				}
			} catch (Exception localException) {
				Util.LogException(localException);
			}
		}
	}
	
	private static String readEHBStgy(String file){
		String ehbStgy = "";
		try{
			FileInputStream fis = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ois.readObject();
			ois.readObject();
			ois.readObject();
			ois.readObject();
			ois.readObject();
			Object readObject6 = ois.readObject();
			ehbStgy = (String)readObject6;
			ois.close();
			fis.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return ehbStgy;
	}
	
}
