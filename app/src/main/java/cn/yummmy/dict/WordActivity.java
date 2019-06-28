package cn.yummmy.dict;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import cn.yummmy.dict.util.ECDict;

public class WordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word);
        final CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        ECDict ecDict = new ECDict(this);
        final String result = ecDict.searchWordSql(text.toString());
        TextView textView = findViewById(R.id.translateTextView);
        setTitle(text.toString());
        textView.setText(result);
        ecDict.closeLink();

        File wordsDatabase = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + getResources().getString(R.string.database_path)
                + "/" + getResources().getString(R.string.words_name));
        final SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(wordsDatabase, null);

        // create table if not exist
        String tableSql = "create table if not exists words(" +
                "ID integer primary key autoincrement not null," +
                "word varchar(30) not null," +
                "sync integer not null" +
                ");";
        database.execSQL(tableSql);

        findViewById(R.id.okButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database.close();
                finish();
            }
        });

        findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sql = "insert into words (word, sync) values (\"" + text.toString() + "\" , 0);";
                database.execSQL(sql);
                database.close();
                finish();
            }
        });
    }
}
