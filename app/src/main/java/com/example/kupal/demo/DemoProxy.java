package com.example.kupal.demo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * Created by kupal on 8/11/2017.
 */

public class DemoProxy extends AccessibilityService {

    //<-------------------------------  private Data Members  ---------------------------------------->

    private FrameLayout overlay;
    private TextToSpeech mTts;
    private int result;
    private AccessibilityServiceInfo info;
    private WindowManager wm;
    private WindowManager.LayoutParams lp;
    private boolean once;

    //<-------------------------------  onCreate method  ---------------------------------------->

    @Override
    public void onCreate() {
        super.onCreate();
    }

    //<-------------------------------  override onServiceConnected  ---------------------------------------->

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onServiceConnected() {
        info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        //<-------------------------------  create Text to Speech object  ----------------------------------->

        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    result = mTts.setLanguage(Locale.US);
                } else {
                    Toast.makeText(getApplicationContext(), "Feature not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    //<-------------------------------  override onAccessibilityEvent  ---------------------------------------->

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        //<-------------------------------  remove overlay after exiting whatsapp  ----------------------------->

        if (!event.getPackageName().equals("com.whatsapp")) {
            if (overlay != null) {
                wm.removeView(overlay);
                overlay = null;
                once = false;
            }
            return;
        }

        //<-------------------------------  test  ---------------------------------------->

        if (event.getPackageName().equals("com.android.systemui")){
            Toast.makeText(this, "testing UI", Toast.LENGTH_SHORT).show();
            return;
        }

        //<-------------------------------  add overlay to whatsapp ---------------------------------------->

        if (event.getPackageName().equals("com.whatsapp") ) {
            if (!once) {
                wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                overlay = new FrameLayout(this);
                lp = new WindowManager.LayoutParams();
                lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                lp.format = PixelFormat.TRANSLUCENT;
                lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                lp.gravity = Gravity.TOP;
                lp.alpha = 100;
                LayoutInflater inflater = LayoutInflater.from(this);
                inflater.inflate(R.layout.actionbutton, overlay);
                configureSwipeButton();
                configureVolumeButton();
                configureScrollButton();
                wm.addView(overlay, lp);
                once = true;
            }
            if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                Toast.makeText(getApplication(), event.getText().toString(), Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser(event.getText().toString());
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser(event.getText().toString());
                }

            } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser("Scrolling");
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    speakToUser("Scrolling");
                }
            }
        }
    }

    //<-------------------------------  helper function  ---------------------------------------->

    private void speakToUser(String eventText) {
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Toast.makeText(this, "Feature not supported", Toast.LENGTH_SHORT).show();
        } else {
            if (!eventText.contains("null")) {
                Toast.makeText(this, eventText, Toast.LENGTH_SHORT).show();
                mTts.speak(eventText, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onInterrupt() {
    }

    //<-------------------------------  helper  ---------------------------------------->

    private void configureVolumeButton() {
        Button volumeUpButton = (Button) overlay.findViewById(R.id.volume_up);
        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AccessibilityNodeInfo findScrollableNode(AccessibilityNodeInfo root) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }

    //<-------------------------------  helper  ---------------------------------------->

    private void configureScrollButton() {
        Button scrollButton = (Button) overlay.findViewById(R.id.scroll);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                AccessibilityNodeInfo scrollable = findScrollableNode(getRootInActiveWindow());
                if (scrollable != null) {
                    scrollable.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId());
                }
            }
        });
    }

    //<-------------------------------  helper  ---------------------------------------->

    private void configureSwipeButton() {
        Button swipeButton = (Button) overlay.findViewById(R.id.swipe);
        swipeButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Path swipePath = new Path();
                swipePath.moveTo(1000, 1000);
                swipePath.lineTo(100, 1000);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 500));
                dispatchGesture(gestureBuilder.build(), null, null);
            }
        });
    }
}
//<------------------------------------------------  END  ------------------------------------------------->

