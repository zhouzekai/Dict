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

import com.nightonke.jellytogglebutton.JellyToggleButton;
import com.nightonke.jellytogglebutton.State;

import static android.content.Context.BIND_AUTO_CREATE;

public class HomeFragment extends Fragment {

    private JellyToggleButton jellyToggleButton;
    private ClipboardManager clipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener listener;
    private boolean listenerInstalled = false;
    private DictConn myConn;
    private DictService.DictBinder myBinder;
    private SharedPreferences accountInfo;

    private class DictConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myBinder = (DictService.DictBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, null);
        accountInfo = getActivity().getSharedPreferences("account_info", 0);
        jellyToggleButton = (JellyToggleButton) view.findViewById(R.id.jellyToggleButton);
        jellyToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean haveLogin = accountInfo.getBoolean("have_login", false);
                if (!haveLogin) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("Tips")
                            .setMessage("请登录，否则无法进行同步\n拉开侧边栏，进入扇贝账号")
                            .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .create();
                    dialog.show();
                }
            }
        });
        jellyToggleButton.setOnStateChangeListener(new JellyToggleButton.OnStateChangeListener() {
            @Override
            public void onStateChange(float process, State state, JellyToggleButton jtb) {
                if (state.equals(State.RIGHT_TO_LEFT)) {
                    if (listenerInstalled) {
                        listenerInstalled = false;
                        clipboardManager.removePrimaryClipChangedListener(listener);
                    }
                }
                if (state.equals(State.LEFT_TO_RIGHT)) {
                    if (!listenerInstalled) {
                        listenerInstalled = true;
                        clipboardManager.addPrimaryClipChangedListener(listener);
                    }
                }
            }
        });

        // Start service
        Intent startDictService = new Intent(getActivity(), DictService.class);
        getActivity().startService(startDictService);
        // Bind service
        myConn = new DictConn();
        Intent connectIntent = new Intent(getActivity(), DictService.class);
        getActivity().bindService(connectIntent, myConn, BIND_AUTO_CREATE);
        // Get ClipboardManager
        clipboardManager = (ClipboardManager) getActivity().
                getSystemService(Context.CLIPBOARD_SERVICE);
        listener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                ClipData clipData = clipboardManager.getPrimaryClip();
                ClipData.Item item = clipData.getItemAt(0);
                if (item != null && item.getText() != null) {
                    String content = item.getText().toString();
                    if (!content.equals("")) {
                        myBinder.queryWord(content);
                    }
                }
            }
        };

        return view;
    }

    @Override
    public void onDestroy() {
        if (listenerInstalled) {
            clipboardManager.removePrimaryClipChangedListener(listener);
        }
        super.onDestroy();
    }
}
