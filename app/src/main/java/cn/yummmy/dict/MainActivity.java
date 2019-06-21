package cn.yummmy.dict;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import org.json.JSONObject;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.yummmy.dict.util.Scraper;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.entity.StringEntity;

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
    private static final int READ_WRITE_PERM = 2333;

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

        if (!(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) ||
                !(ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    READ_WRITE_PERM);
        }
        else {
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
                WordsFragment wordsFragment = (WordsFragment) getSupportFragmentManager().findFragmentByTag(TAGS[1]);
                AboutFragment aboutFragment = (AboutFragment) getSupportFragmentManager().findFragmentByTag(TAGS[2]);
                fragments.add(homeFragment);
                fragments.add(wordsFragment);
                fragments.add(aboutFragment);
            }
            setDefaultFragment(prePos);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_WRITE_PERM) {
            boolean granted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }

            if (granted) {
                fragments = new ArrayList<>();
                fragments.add(new HomeFragment());
                fragments.add(new WordsFragment());
                fragments.add(new AboutFragment());
                setDefaultFragment(HOME);
            }
            else {
                AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Tips")
                        .setMessage("没有给予一定的权限，程序将会终结")
                        .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PRE, prePos);
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
}
