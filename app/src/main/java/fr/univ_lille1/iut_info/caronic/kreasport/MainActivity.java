package fr.univ_lille1.iut_info.caronic.kreasport;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.univ_lille1.iut_info.caronic.kreasport.fragments.BottomSheet;
import fr.univ_lille1.iut_info.caronic.kreasport.fragments.ExploreFragment;
import fr.univ_lille1.iut_info.caronic.kreasport.fragments.HomeFragment;
import fr.univ_lille1.iut_info.caronic.kreasport.fragments.OnFragmentInteractionListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnFragmentInteractionListener {

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private static final String BACKSTACK_REPLACE_WITH_FRAG_EXPLORE = "mapsv3.backstack.replace_with_frag_explore";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        selectDrawerItem(navigationView.getMenu().getItem(0));
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void selectDrawerItem(MenuItem menuItem) {

        Fragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_home:
                fragment = HomeFragment.newInstance("", "");

                getSupportFragmentManager().popBackStack(BACKSTACK_REPLACE_WITH_FRAG_EXPLORE, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_main_frame_layout, fragment)
                        .commit();
                break;
            case R.id.nav_explore:
                fragment = ExploreFragment.newInstance("", "");
                BottomSheet bottomSheet = BottomSheet.newInstance("", "");

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content_main_frame_layout, fragment)
                        .add(R.id.fragment_explore_root_coordlayout, bottomSheet)
                        .addToBackStack(BACKSTACK_REPLACE_WITH_FRAG_EXPLORE)
                        .commit();
                break;
            case R.id.nav_share:
                break;
            case R.id.nav_send:
                break;
            default:
                break;
        }

        if (fragment != null) {
            completeDrawerAction(menuItem);
        }
    }

    private void completeDrawerAction(MenuItem menuItem) {
        menuItem.setChecked(true);

        setTitle(menuItem.getTitle());
    }

    @Override
    public void onFragmentInteraction(String origin) {
        Toast.makeText(this, "used callback from " + origin, Toast.LENGTH_SHORT).show();
    }
}
