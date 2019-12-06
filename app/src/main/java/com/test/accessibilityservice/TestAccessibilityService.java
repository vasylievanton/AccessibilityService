package com.test.accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

public class TestAccessibilityService extends AccessibilityService {

    private static final String TAG = "SERVICE";
    private AccessibilityNodeInfo info;


    private String getEventTypeString(int eventType) {
        switch (eventType) {
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "TYPE_TOUCH_INTERACTION_END";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "TYPE_WINDOWS_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
        }
        return String.format(Locale.getDefault(), "unknown (%d)", eventType);
    }

    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    private void dumpNode(AccessibilityNodeInfo node, int indent, String viewClass, String description, int action, Bundle args) {
        if (node == null) {
            Log.v(TAG, "node is null (stopping iteration)");
            return;
        }

        if (node.getClassName() != null && node.getClassName().equals(viewClass)) {
            switch (viewClass) {
                case "android.widget.ImageView":
                    if (node.getContentDescription() != null && node.getContentDescription().equals(description)) {
                        node.performAction(action);
                        delay(3000, new DelayCallback() {
                            @Override
                            public void afterDelay() {
                                //set text
                                Bundle arguments = new Bundle();
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "Search request");
                                dumpNode(info, 0, "android.widget.EditText", "Пошук на YouTube", AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            }
                        });
                    }
                    break;

                case "android.widget.EditText":
                    if (node.getHintText() != null && node.getHintText().equals(description))
                        if (node.getText() == null || !node.getText().toString().equalsIgnoreCase(args.getCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE).toString())) {
                            node.performAction(action, args);
                            delay(3000, new DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    // perform click on search result and go back to our app
                                    dumpNode(info, 0, "android.widget.LinearLayout", "", AccessibilityNodeInfo.ACTION_CLICK, null);
                                }
                            });
                        }


                    break;
                case "android.widget.LinearLayout":
                    if (node.isClickable()) {
                        node.performAction(action);
                        delay(3000, new DelayCallback() {
                            @Override
                            public void afterDelay() {
                                goBackApp();
                            }
                        });
                    }
                    break;
            }
            return;
        }

        String indentStr = new String(new char[indent * 3]).replace('\0', ' ');
        Log.v(TAG, String.format("%s NODE: %s", indentStr, node.toString()));
        for (int i = 0; i < node.getChildCount(); i++) {
            dumpNode(node.getChild(i), indent + 1, viewClass, description, action, args);
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /* Show the accessibility event */
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            Log.v(TAG, getEventTypeString(event.getEventType()) + getEventText(event));
            info = getRootInActiveWindow();
            //go search
            dumpNode(info, 0, "android.widget.ImageView", "Пошук", AccessibilityNodeInfo.ACTION_CLICK, null);
        }
    }


    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.v(TAG, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.packageNames = new String[]{"com.google.android.youtube"};
        setServiceInfo(info);
    }


    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    private void goBackApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    // Delay mechanism

    private interface DelayCallback {
        void afterDelay();
    }

    private void delay(long millis, final DelayCallback delayCallback) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayCallback.afterDelay();
            }
        }, millis);
    }
}
