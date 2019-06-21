package cn.yummmy.dict;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import cn.yummmy.dict.util.Scraper;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.CLIPBOARD_SERVICE;

public class HomeFragment extends Fragment {

    private SharedPreferences accountInfo;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);
        Button scrapeButton = view.findViewById(R.id.scrapeButton);
        scrapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Scraper.scrape("https://www.ldoceonline.com/dictionary/outlineasdf");
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
}
