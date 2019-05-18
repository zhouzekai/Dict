//package cn.yummmy.dict;
//
//import android.app.Service;
//import android.content.ClipData;
//import android.content.ClipboardManager;
//import android.content.Context;
//import android.content.Intent;
//import android.database.sqlite.SQLiteDatabase;
//import android.os.Binder;
//import android.os.Environment;
//import android.os.IBinder;
//
//public class ClipboardService extends Service {
//
//    class ClipboardBinder extends Binder {
//        // Query a word, show in dialog, add to word list
//        public void addListener() {
//        }
//    }
//
//    private ClipboardManager clipboardManager;
//
//    public ClipboardService() {
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        // clipboard manager and listener
//        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//        listener = new ClipboardManager.OnPrimaryClipChangedListener() {
//            @Override
//            public void onPrimaryClipChanged() {
//                ClipData clipData = clipboardManager.getPrimaryClip();
//                ClipData.Item item = clipData.getItemAt(0);
//                String content = item.getText().toString();
//                myBinder.queryWord(content);
//            }
//        };
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        return new ClipboardService.ClipboardBinder();
//    }
//
//}
