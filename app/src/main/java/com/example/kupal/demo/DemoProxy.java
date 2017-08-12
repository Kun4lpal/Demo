package com.example.kupal.demo;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.speech.tts.TextToSpeech;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by kupal on 8/11/2017.
 */

public class DemoProxy extends AccessibilityService{

    FrameLayout mLayout;
    private TextToSpeech mTts;
    private int result;
    private AccessibilityServiceInfo info;

    @Override
    protected void onServiceConnected() {

        info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;

        mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    result =  mTts.setLanguage(Locale.US);
                }else{
                    Toast.makeText(getApplicationContext(), "Feature not supported", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //for specific app names
        info.packageNames = new String[]
                {"com.whatsapp"};
//

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        String eventText = null;
        switch(eventType) {
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventText = "";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventText = "";
                break;
        }

        eventText = eventText + event.getContentDescription();

        // Do something nifty with this text, like speak the composed string
        // back to the user.
        speakToUser(eventText);
    }

    private void speakToUser(String eventText) {
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
            Toast.makeText(this, "Feature not supported", Toast.LENGTH_SHORT).show();
        }else{
            if(!eventText.contains("null")){
                Toast.makeText(this, eventText, Toast.LENGTH_SHORT).show();
                mTts.speak(eventText, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
