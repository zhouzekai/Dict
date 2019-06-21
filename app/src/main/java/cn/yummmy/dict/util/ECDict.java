package cn.yummmy.dict.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;

import cn.yummmy.dict.R;

public class ECDict {

    private Context context;
    private SQLiteDatabase database;

    public ECDict(Context context) {
        this.context = context;
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + context.getResources().getString(R.string.database_path)
                + "/" + context.getResources().getString(R.string.database_name));
        database = SQLiteDatabase.openOrCreateDatabase(file, null);
    }

    public String searchWordSql(String word) {
        Cursor cursor = database.rawQuery("select * from stardict where word = ?", new String[]{word});
        boolean result = cursor.moveToFirst();
        if (result) {
            String translation = cursor.getString(cursor.getColumnIndex("translation"));
            String pron = cursor.getString(cursor.getColumnIndex("phonetic"));
            if (pron.equals("")) return translation;
            return "[" + pron + "]\n" + translation;
        }
        else {
            return "";
        }
    }

    public void closeLink() {
        database.close();
    }
}
