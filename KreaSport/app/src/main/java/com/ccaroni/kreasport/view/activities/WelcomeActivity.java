package com.ccaroni.kreasport.view.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ccaroni.kreasport.R;
import com.ccaroni.kreasport.utils.CustomViewPager;
import com.ccaroni.kreasport.utils.PermissionsManager;
import com.ccaroni.kreasport.utils.PreferenceManager;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class WelcomeActivity extends AppCompatActivity {

    private final static String PREF_NAME = "intro_slider-welcome";
    private static final String LOG = WelcomeActivity.class.getSimpleName();

    private CustomViewPager viewPager;
    private MyViewPagerAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private PreferenceManager prefManager;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidThreeTen.init(this);

        // Checking for first time launch - before calling setContentView()
        prefManager = new PreferenceManager(this, PREF_NAME);
        permissionsManager = new PermissionsManager(this);
        boolean writeAccess = permissionsManager.isPermissionGranted(PermissionsManager.WRITE_EXTERNAL_STORAGE);
        boolean locationAccess = permissionsManager.isPermissionGranted(PermissionsManager.ACCESS_FINE_LOCATION);
        if (!prefManager.isFirstTimeLaunch() && writeAccess && locationAccess) {
            launchHomeScreen();
            return;
        }

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        setContentView(R.layout.activity_welcome);

        viewPager = (CustomViewPager) findViewById(R.id.view_pager);
        dotsLayout = (LinearLayout) findViewById(R.id.layoutDots);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnNext = (Button) findViewById(R.id.btn_next);

        // layouts of all welcome sliders
        // add few more layouts if you want
        layouts = new int[]{
                R.layout.slide_screen1,
                R.layout.slide_screen2,
                R.layout.slide_screen3,
                R.layout.slide_screen4,
                R.layout.slide_screen5,
                R.layout.slide_screen6};

        // adding bottom dots
        addBottomDots(0);

        // making notification bar transparent
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPagerAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                launchHomeScreen();
//                Go to first permission request screen
                viewPager.setCurrentItem(layouts.length - 3);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checking for last page
                // if second to last or last, will ask for permission
                int current = getItem(0);
                if (current < layouts.length - 3) {
                    // move to next screen
                    viewPager.setCurrentItem(current + 1);
                } else if (current == layouts.length - 3) {
                    boolean granted = permissionsManager.isPermissionGranted(PermissionsManager.WRITE_EXTERNAL_STORAGE);
                    if (!granted)
                        permissionsManager.requestPermission(PermissionsManager.WRITE_EXTERNAL_STORAGE);
                    else
                        viewPager.setCurrentItem(current + 1);
                } else if (current == layouts.length - 2) {
                    boolean granted = permissionsManager.isPermissionGranted(PermissionsManager.ACCESS_FINE_LOCATION);
                    if (!granted)
                        permissionsManager.requestPermission(PermissionsManager.ACCESS_FINE_LOCATION);
                    else
                        viewPager.setCurrentItem(current + 1);
                } else if (current == layouts.length - 1) {
                    launchHomeScreen();
                }
            }
        });

        if (!prefManager.isFirstTimeLaunch()) {
            if (!writeAccess) {
                viewPager.setCurrentItem(layouts.length - 3);
            } else {
                viewPager.setCurrentItem(layouts.length - 2);
            }
        }
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];

        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if (dots.length > 0)
            dots[currentPage].setTextColor(colorsActive[currentPage]);
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        finish();
    }

    //  viewpager change listener
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            addBottomDots(position);

            if (position == layouts.length - 1) {
                btnNext.setText(getString(R.string.start));
            } else if (position == layouts.length - 2) {
                btnSkip.setVisibility(View.GONE);
                boolean granted = permissionsManager.isPermissionGranted(PermissionsManager.ACCESS_FINE_LOCATION);
                if (!granted) {
                    btnNext.setText(getString(R.string.grant));
                    viewPager.setPagingEnabled(false);
                } else {
                    btnNext.setText(getString(R.string.next));
                    viewPager.setPagingEnabled(true);
                }
            } else if (position == layouts.length - 3) {
                btnSkip.setVisibility(View.GONE);
                boolean granted = permissionsManager.isPermissionGranted(PermissionsManager.WRITE_EXTERNAL_STORAGE);
                if (!granted) {
                    btnNext.setText(getString(R.string.grant));
                    viewPager.setPagingEnabled(false);
                } else {
                    btnNext.setText(getString(R.string.next));
                    viewPager.setPagingEnabled(true);
                }
            } else {
                // still pages are left
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * View pager adapter
     */
    public class MyViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;

        public MyViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted();
                }
            }
            case PermissionsManager.WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionGranted();
                }
            }
        }
    }

    public void onPermissionGranted() {
        viewPager.setPagingEnabled(true);
        btnNext.setText(getString(R.string.next));
    }
}
