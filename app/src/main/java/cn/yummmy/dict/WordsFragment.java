package cn.yummmy.dict;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WordsFragment extends Fragment {
    public WordsFragment(){}

    private ListView wordsList;
    private SQLiteDatabase database;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_words, null);
        wordsList = (ListView) view.findViewById(R.id.words_list);
        File wordsDatabase = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + getResources().getString(R.string.database_path)
                + "/" + getResources().getString(R.string.words_name));
        database = SQLiteDatabase.openOrCreateDatabase(wordsDatabase, null);

        List<String> words = new ArrayList<>();
        Cursor cursor = database.rawQuery("select * from words where sync = ?", new String[]{"0"});
        boolean result = cursor.moveToFirst();
        while (result) {
            String word = cursor.getString(cursor.getColumnIndex("word"));
            words.add(word);
            result = cursor.moveToNext();
        }
        WordsAdapter wordsAdapter = new WordsAdapter(getActivity(), words);
        wordsList.setAdapter(wordsAdapter);
        return view;
    }

    @Override
    public void onDestroyView() {
        database.close();
        super.onDestroyView();
    }
}
