package com.iitr.kaishu.forcelycloser;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AccessibilityService {
    long inittime = 0;
    Runnable willrun;
    Handler hand;
    ArrayList<AccessibilityNodeInfo> buttonNodes;
    String pakagename,showpakage;
    AccessibilityNodeInfo rootNode;
    Boolean forecestopactivity = false;
    Boolean okactivity = false;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.i("test", "  " + event.getEventType());

        if (AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED == event
                .getEventType()) {
            pakagename = event.getPackageName().toString();
            rootNode = getRootInActiveWindow();
            if(forecestopactivity){
            forcestophasopened();
            }
            if(okactivity){
                okactivityhasopened();
            }
        }

}

    @Override
    protected boolean onKeyEvent(KeyEvent event) {

     if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
       if(inittime ==0){ inittime = event.getEventTime();}
         else {if(event.getEventTime()-inittime<800){hand.removeCallbacks(willrun);}inittime = 0;return super.onKeyEvent(event);}
          willrun = new Runnable() {
             @Override
             public void run() {
               logpress();
             }
         } ;
         hand.postDelayed(willrun,800);

     }
        return super.onKeyEvent(event); }

    @Override
    public void onInterrupt() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        hand = new Handler();
        AccessibilityServiceInfo tempInfo = getServiceInfo();
        tempInfo.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        tempInfo.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(tempInfo);
    }
    private void findChildViews(AccessibilityNodeInfo parentView) {

        if (parentView == null || parentView.getClassName() == null ) {
            return;
        }
        int childCount = parentView.getChildCount();
        if (childCount == 0 && (parentView.getClassName().toString().contentEquals("android.widget.Button"))) {
            buttonNodes.add(parentView);
        } else {
            for (int i = 0; i < childCount; i++) {
                findChildViews(parentView.getChild(i));
            }
        }
    }
    public void logpress(){
        showpakage=pakagename;
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + pakagename));
        getApplicationContext().startActivity(intent);
        forecestopactivity = true;

    }
    public void forcestophasopened(){
        buttonNodes = new ArrayList<>();
        findChildViews(rootNode);

        for(AccessibilityNodeInfo mNode : buttonNodes){
            if(mNode.getText()==null){
                return;
            }
            if(mNode.getText().toString().contentEquals("Force stop")){
                forecestopactivity = false;
                okactivity = true;
                mNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        }
    }
    public void okactivityhasopened(){
        buttonNodes = new ArrayList<>();
        findChildViews(rootNode);

        for(AccessibilityNodeInfo mNode : buttonNodes){
            if(mNode.getText()==null){
                return;
            }
            if(mNode.getText().toString().contentEquals("OK")){
                mNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Toast.makeText(MainActivity.this, "Force closed "+showpakage, Toast.LENGTH_SHORT).show();
                okactivity = false;
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }
    }


}
