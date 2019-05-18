package cn.yummmy.dict;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Fragment> fragments;
    private int prePos;
    private SharedPreferences sharedPreferences;
    private SQLiteDatabase database;

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
        requestPermission();

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

        sharedPreferences = getSharedPreferences("account_info", 0);
        File wordsDatabase = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + getResources().getString(R.string.database_path)
                + "/" + getResources().getString(R.string.words_name));
        database = SQLiteDatabase.openOrCreateDatabase(wordsDatabase, null);
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
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.dialog_account, null);
            final EditText input1 = (EditText) textEntryView.findViewById(R.id.account);
            final EditText input2 = (EditText) textEntryView.findViewById(R.id.password);
            final String account = sharedPreferences.getString("account", "");
            final String password = input2.getText().toString();
            if(account != "") {
                input1.setText(account);
            }
            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("扇贝账号").setView(textEntryView).setPositiveButton("Save",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("account", input1.getText().toString());
                            editor.commit();

                            // Login to get Cookie
                            loginShanbay(account, password);
                        }
                    }).setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {}
                    });
            alert.show();

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
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    0);
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    0);
        }
    }

    private void loginShanbay(String account, String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore cookieStore = new PersistentCookieStore(getApplicationContext());
        client.setCookieStore(cookieStore);
        String url = "https://apiv3.shanbay.com/bayuser/login";
        client.addHeader("Accept", "application/json");
        client.addHeader("Content-Type", "application/json");
        client.addHeader("Origin", "https://web.shanbay.com");
        client.addHeader("Referer", "https://web.shanbay.com/web/account/login");
        client.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
        client.addHeader("X-CSRFToken", "Ww7i7eT0N9YmGqGOe9iSxB15esWldzV1");
        String json = "{\"account\":\"" + account + "\",\"password\":\""
                + password + "\",\"code_2fa\":\"\"}";
        try {
            StringEntity entity = new StringEntity(json);
            client.post(getApplicationContext(), url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String json = new String(responseBody, "utf-8");
                        JSONObject jsonObject = new JSONObject(json);
                        long user_id = jsonObject.getLong("id_int");
                        List<Cookie> cookies = cookieStore.getCookies();
                        for (Cookie cookie : cookies) {
                            String name = cookie.getName();
                            if (!name.equals("auth_token")) continue;
                            Date expiryDate = cookie.getExpiryDate();
                            String value = cookie.getValue();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong("expiry", expiryDate.getTime());
                            editor.putString("auth_token", value);
                            editor.putLong("user_id", user_id);
                            editor.commit();
                        }
                    }
                    catch (Exception e) {

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
