package cn.yummmy.dict;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class WordsAdapter extends BaseAdapter {
    private List<String> data;
    private LayoutInflater layoutInflater;
    private Context context;

    public WordsAdapter(Context context, List<String> data) {
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.data = data;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    private class Word {
        public TextView wordEntry;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.item_word, null);
        Word item = new Word();
        item.wordEntry = convertView.findViewById(R.id.word_entry);
        item.wordEntry.setText(data.get(position));
        return convertView;
    }
}
