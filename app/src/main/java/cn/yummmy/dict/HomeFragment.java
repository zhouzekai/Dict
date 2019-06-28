package cn.yummmy.dict;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ichi2.anki.api.AddContentApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import cn.yummmy.dict.util.ECDict;

public class HomeFragment extends Fragment {

    private TextView textView;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);
        textView = view.findViewById(R.id.hintTextView);
        Button scrapeButton = view.findViewById(R.id.scrapeButton);
        scrapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File wordsDatabase = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/" + getResources().getString(R.string.database_path)
                                + "/" + getResources().getString(R.string.words_name));
                        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase(wordsDatabase, null);
                        ECDict dict = new ECDict(getActivity());

                        Cursor cursor = database.rawQuery("select * from words where 1=1", null);
                        final int count = cursor.getCount();
                        boolean result = cursor.moveToFirst();
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("0/" + count);
                            }
                        });
                        while (result) {
                            final String word = cursor.getString(cursor.getColumnIndex("word"));

                            // card
                            String front = word + "[sound:" + word + ".mp3]";
                            String translate = dict.searchWordSql(word);
                            StringBuilder backStringBuilder = new StringBuilder();
                            for (int i = 0; i < translate.length(); i++) {
                                char currentChar = translate.charAt(i);
                                if (currentChar == '\n') {
                                    backStringBuilder.append("<br>");
                                }
                                else {
                                    backStringBuilder.append(currentChar);
                                }
                            }
                            try {
                                sendCard(front, backStringBuilder.toString());
                            }
                            catch (NullPointerException e) {
                                Toast.makeText(getActivity(), "请打开Anki的安卓应用", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception e) {
                                break;
                            }

                            // audio
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    String url = "http://dict.youdao.com/dictvoice?audio=";
                                    downloadAudio(url, word);
                                }
                            }).start();

                            // update
                            result = cursor.moveToNext();
                            textView.post(new Runnable() {
                                @Override
                                public void run() {
                                    String text = getNextViewText(textView.getText().toString());
                                    textView.setText(text);
                                }
                            });
                        }
                        database.execSQL("delete from words where 1=1");
                        database.close();
                    }
                }).start();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    private void sendCard(String front, String back) throws Exception {
        if (AddContentApi.getAnkiDroidPackageName(getActivity()) != null) {
            // API available: Add deck and model if required, then add your note
            final AddContentApi api = new AddContentApi(getActivity());
            Map<Long, String> deckList = api.getDeckList();
            Map<Long, String> modelList = api.getModelList();
            long deckId = 0;
            long modelId = 0;
            for (Map.Entry<Long, String> entry : deckList.entrySet()) {
                if (entry.getValue().equals("单词")) {
                    deckId = entry.getKey();
                }
            }
            for (Map.Entry<Long, String> entry : modelList.entrySet()) {
                if (entry.getValue().equals("Basic")) {
                    modelId = entry.getKey();
                }
            }
            if (deckId == 0 || modelId == 0) {
                return;
            }

            api.addNote(modelId, deckId, new String[] {front, back}, null);
        }
    }

    private String getNextViewText(String viewText) {
        int val = 0;
        int i = 0;
        for (; i < viewText.length(); i++) {
            if (viewText.charAt(i) == '/') {
                break;
            }
            val = val * 10 + viewText.charAt(i) - '0';
        }
        return (val + 1) + viewText.substring(i);
    }

    private void downloadAudio(String url, String word) {
        try{
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AnkiDroid/collection.media";
            String filename = word + ".mp3";

            URL myURL = new URL(url + word);
            URLConnection conn = myURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            int fileSize = conn.getContentLength();//根据响应获取文件大小
            if (fileSize <= 0) throw new RuntimeException("无法获知文件大小 ");
            if (is == null) throw new RuntimeException("stream is null");
            File file1 = new File(path);
            if(!file1.exists()){
                file1.mkdirs();
            }
            //把数据存入路径+文件名
            FileOutputStream fos = new FileOutputStream(path + "/" + filename);
            byte buf[] = new byte[1024];
            do {
                int numread = is.read(buf);
                if (numread == -1)
                {
                    break;
                }
                fos.write(buf, 0, numread);
            } while (true);
            is.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

// 以下的代码是爬取朗文的字典，将它加入到释义中。
//    String word = cursor.getString(cursor.getColumnIndex("word"));
//    result = cursor.moveToNext();
//    EnglishWord englishWord = Scraper.scrape("https://www.ldoceonline.com/dictionary/" + word);
//    if (englishWord != null) {
//        try {
//            String front = englishWord.hyphenation + "<br>[" + englishWord.pronCodes + "] [sound:inaugurate0205.mp3]";
//            StringBuilder backStringBuilder = new StringBuilder();
//            int senseCount = 1;
//            String translate = dict.searchWordSql(word);
//            backStringBuilder.append(translate);
//            for (Sense sense : englishWord.senses) {
//                backStringBuilder.append("<br><br>" + senseCount + ". " + sense.def + "<br><br>");
//                for (int i = 0; i < sense.examples.size(); i++) {
//                    backStringBuilder.append("(" + (i + 1) + ")[sound:inaugurate0205.mp3]  " + sense.examples.get(i) + "<br><br>");
//                }
//            }
//            sendCard(front, backStringBuilder.toString());
//            String updateSql = "update words set sync = 1 where word = \"" + word + "\";";
//            database.execSQL(updateSql);
//        }
//        catch (NullPointerException e) {
//            Toast.makeText(getActivity(), "请打开Anki的安卓应用", Toast.LENGTH_SHORT).show();
//            break;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            break;
//        }
//    }
