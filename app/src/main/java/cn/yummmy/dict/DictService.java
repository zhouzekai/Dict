package cn.yummmy.dict;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.view.WindowManager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.File;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class DictService extends Service {

    class DictBinder extends Binder {
        // Query a word, show in dialog, add to word list
        public void queryWord(String word) {
            String content = searchWordSql(word);
            showDialog(word, content);
        }
    }

    // Sql
    private SQLiteDatabase database;

    public DictService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + getResources().getString(R.string.database_path)
                + "/" + getResources().getString(R.string.database_name));
        database = SQLiteDatabase.openOrCreateDatabase(file, null);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        database.close();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new DictBinder();
    }

    private void showDialog(String title, String message) {

        if(!Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        AlertDialog dialog = new AlertDialog.Builder(getApplicationContext()).setTitle(title)
                .setMessage(message)
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        dialog.show();
    }

    private void addWord(String wordId) {
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader(":authority", "www.shanbay.com");
            client.addHeader(":method", "POST");
            client.addHeader(":path", "/api/v1/bdc/learning/");
            client.addHeader(":scheme", "https");
            client.addHeader("accept", "application/json, text/javascript, */*; q=0.01");
            client.addHeader("accept-encoding", "zh-CN,zh;q=0.9,zh-TW;q=0.8,en;q=0.7");
            client.addHeader("content-type", "application/json");
            client.addHeader("cookie", "csrftoken=DGojASqnaJGWNPfOEg0n1RPZbPzCaA5l; _ga=GA1.2.779543603.1557886651; locale=zh-cn; userid=60647236; __utmc=183787513; __utmz=183787513.1557886685.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); userid=60647236; language_code=zh-CN; __utma=183787513.779543603.1557886651.1557886685.1557893669.2; __utmt=1; auth_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpZCI6NjA2NDcyMzYsImV4cCI6MTU1ODc2MDY5MiwiZGV2aWNlIjoiIiwidXNlcm5hbWUiOiJ6emsyMzMiLCJpc19zdGFmZiI6MH0.2SnSmPKRPipuhwkVAV6Bs-ixqXw-G6V9b7Ga-SBs6og; __utmb=183787513.6.10.1557893669");
            client.addHeader("origin", "https://www.shanbay.com");
            client.addHeader("referer", "https://www.shanbay.com/bdc/review/");
            client.addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
            client.addHeader("X-Requested-With", "XMLHttpRequest");

            final String json = "{\"id\":" + wordId + ",\"content_type\":\"vocabulary\"}\n";
            StringEntity entity = new StringEntity(json);
            String url = "https://www.shanbay.com/api/v1/bdc/learning/";

            client.post(getApplicationContext(), url, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String body = new String(responseBody, "utf-8");
                        JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
                        System.out.println(jsonObject);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {

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

    private String cookieData(String mAuthToken, String mUserId) {
        String result = "";

        String csrftoken = "csrftoken=DGojASqnaJGWNPfOEg0n1RPZbPzCaA5l; ";
        String _ga = "_ga=GA1.2.779543603.1557886651; ";
        String locale = "locale=zh-cn; ";
        String userId = "userid=" + mUserId + "; ";
        String __utmc = "__utmc=183787513; ";
        String __utmz = "__utmz=183787513.1557886685.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); ";
        String language_code = "language_code=zh-CN; ";
        String __utma = "__utma=183787513.779543603.1557886651.1557886685.1557893669.2; ";
        String __utmt = "__utmt=1; ";
        String auth_token = "auth_token=" + mAuthToken;

        result = csrftoken + _ga + locale + userId + __utmc + __utmz +
                userId + language_code + __utma + __utmt + auth_token;

        return result;
    }

    private void searchWordShanbay(final String word) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        client.addHeader("Referer", "https://www.shanbay.com/");
        client.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36");
        client.addHeader("X-Requested-With", "XMLHttpRequest");
        client.setTimeout(10000);

        String url = "https://www.shanbay.com/api/v1/bdc/search/?word=" + word;

        client.get(url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String body = new String(responseBody, "utf-8");
                    JsonObject jsonObject = new JsonParser().parse(body).getAsJsonObject();
                    String msg = jsonObject.get("msg").getAsString();
                    if(msg.equals("SUCCESS")) {
                        int content_id = jsonObject.get("data").getAsJsonObject().
                                get("content_id").getAsInt();
                        String pron = jsonObject.get("data").getAsJsonObject().
                                get("pronunciations").getAsJsonObject().
                                get("uk").getAsString();
                        String definition = jsonObject.get("data").getAsJsonObject().
                                get("definition").getAsString();
//                        myBinder.showDialog(word, "[" + pron + "]\n" + definition);
                    }
                    else {
//                        myBinder.showDialog("Tips", "No such word!");
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
//                myBinder.showDialog("Tips", "Check network");
            }
        });
    }

    private String searchWordSql(String word) {
        Cursor cursor = database.rawQuery("select * from stardict where word = ?", new String[]{word});
        boolean result = cursor.moveToFirst();
        if (result) {
            String translation = cursor.getString(cursor.getColumnIndex("translation"));
            String pron = cursor.getString(cursor.getColumnIndex("phonetic"));
            return "[" + pron + "]\n" + translation;
        }
        else {
            return "";
        }
    }
}
