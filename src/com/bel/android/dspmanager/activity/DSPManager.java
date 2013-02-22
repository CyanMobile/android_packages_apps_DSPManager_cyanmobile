package com.bel.android.dspmanager.activity;

import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.os.SystemProperties;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

import com.bel.android.dspmanager.utils.MathUtils;

import com.bel.android.dspmanager.HeadsetService;
import com.bel.android.dspmanager.R;

/**
 * Setting utility for CyanogenMod's DSP capabilities.
 * This page is displays the top-level configurations menu.
 * 
 * @author alankila
 */
public final class DSPManager extends TabActivity {
    public static final String SHARED_PREFERENCES_BASENAME = "com.bel.android.dspmanager";
    public static final String ACTION_UPDATE_PREFERENCES = "com.bel.android.dspmanager.UPDATE";
    public static final int NOTIFY_FOREGROUND_ID = 1;

    private Intent intent;
    private ActionBar mActionBar;
    private static HorizontalScrollView mHorizontalScrollView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        if ("1".equals(SystemProperties.get("ro.squadzone.build", "0"))) {
           setContentView(R.xml.main_preferences);
        } else {
           return;
        }
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setTitle(R.string.app_name);
        mActionBar.setHomeLogo(R.drawable.icon, new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        DSPManager.this.finish();
                  }
        });

        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);

		intent = new Intent("com.bel.android.dspmanager.HEADSET");
		setupTab(new TextView(this), getString(R.string.headset_title), intent);

		intent = new Intent("com.bel.android.dspmanager.SPEAKER");
		setupTab(new TextView(this), getString(R.string.speaker_title), intent);

		intent = new Intent("com.bel.android.dspmanager.BLUETOOTH");
		setupTab(new TextView(this), getString(R.string.bluetooth_title), intent);

         startService(new Intent(HeadsetService.NAME));
    }

    public static class FlingableTabHost extends TabHost implements TabHost.OnTabChangeListener {
        private GestureDetector mGestureDetector;
        private static final int MAJOR_MOVE = 60;
        private Animation mRightInAnimation;
        private Animation mRightOutAnimation;
        private Animation mLeftInAnimation;
        private Animation mLeftOutAnimation;

        public FlingableTabHost(Context context, AttributeSet attrs) {
            super(context, attrs);

            mRightInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_in);
            mRightOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_right_out);
            mLeftInAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_in);
            mLeftOutAnimation = AnimationUtils.loadAnimation(context, R.anim.slide_left_out);

            setOnTabChangedListener(this);

            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                        float velocityY) {
                    int tabCount = getTabWidget().getTabCount();
                    int currentTab = getCurrentTab();
                    int dx = (int) (e2.getX() - e1.getX());

                    // don't accept the fling if it's too short
                    // as it may conflict with tracking move
                    if (Math.abs(dx) > MAJOR_MOVE && Math.abs(velocityX) > Math.abs(velocityY)) {

                        final boolean right = velocityX < 0;
                        final int newTab = MathUtils.constrain(currentTab + (right ? 1 : -1),
                                0, tabCount - 1);
                        if (newTab != currentTab) {
                            // Somewhat hacky, depends on current implementation of TabHost:
                            // http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;
                            // f=core/java/android/widget/TabHost.java
                            View currentView = getCurrentView();
                            setCurrentTab(newTab);
                            View newView = getCurrentView();

                            newView.startAnimation(right ? mRightInAnimation : mLeftInAnimation);
                            currentView.startAnimation(
                                    right ? mRightOutAnimation : mLeftOutAnimation);
                        }
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        @Override
        public void onTabChanged(String tabId) {
            View tabView = getCurrentTabView();
            final int width = mHorizontalScrollView.getWidth();
            final int scrollPos = tabView.getLeft() - (width - tabView.getWidth()) / 2; 
            mHorizontalScrollView.scrollTo(scrollPos, 0);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            mGestureDetector.onTouchEvent(event);
            return true;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    }

	private void setupTab(final View view, final String tag, final Intent myIntent) {

                final TabHost mTabHost = getTabHost();

		View tabview = createTabView(mTabHost.getContext(), tag);
		TabSpec setContent =  mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(myIntent);
		mTabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {

		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
}
