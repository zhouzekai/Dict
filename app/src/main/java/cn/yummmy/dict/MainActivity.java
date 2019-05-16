package cn.yummmy.dict;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Fragment> fragments;
    private int prePos;

    // Constant
    private static final String[] TAGS = {"home", "words", "about"};
    private static final String PRE = "PREPOS";
    private static final int HOME = 0;
    private static final int WORDS = 1;
    private static final int ABOUT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize the fragments
        if(savedInstanceState == null) {
            prePos = 0;
            fragments = new ArrayList<>();
            fragments.add(new HomeFragment());
            fragments.add(new WordsFragment());
            fragments.add(new AboutFragment());
        }
        else {
            prePos = savedInstanceState.getInt(PRE);
            fragments = new ArrayList<>();
            HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(TAGS[0]);
            WordsFragment accountFragment = (WordsFragment) getSupportFragmentManager().findFragmentByTag(TAGS[1]);
            AboutFragment aboutFragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag(TAGS[2]);
            fragments.add(homeFragment);
            fragments.add(accountFragment);
            fragments.add(aboutFragment);
        }
        setDefaultFragment(prePos);

        requestPermission();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
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
        if (id == R.id.action_sync) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_main) {
            switchFragment(HOME);
        } else if (id == R.id.nav_words) {
            switchFragment(WORDS);
        } else if (id == R.id.nav_about) {
            switchFragment(ABOUT);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setDefaultFragment(int pos){
        Fragment fragment = fragments.get(pos);
        if(fragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(fragment).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, fragments.get(prePos), TAGS[pos]).commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PRE, prePos);
    }

    private void switchFragment(int pos) {
        Fragment currentFragment = fragments.get(pos);
        Fragment previousFragment = fragments.get(prePos);
        getSupportFragmentManager().beginTransaction().hide(previousFragment).commit();
        if(currentFragment.isAdded()) {
            getSupportFragmentManager().beginTransaction().show(currentFragment).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction().add(R.id.frameLayout, currentFragment, TAGS[pos]).commit();
        }
        prePos = pos;
    }

    // request write and read permission for sqlite
    public void requestPermission() {
        if (!(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ||
                !(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    0);
        }
    }

}
