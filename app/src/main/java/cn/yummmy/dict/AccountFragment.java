package cn.yummmy.dict;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.cookie.Cookie;
import cz.msebera.android.httpclient.entity.StringEntity;

public class AccountFragment extends Fragment {

    private EditText accountEditText;
    private EditText passwordEditText;
    private Button button;
    private SharedPreferences accountInfo;

    public AccountFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, null);
        accountInfo = getActivity().getSharedPreferences("account_info", 0);
        String account = accountInfo.getString("account", "");
        String password = accountInfo.getString("password", "");
        accountEditText = view.findViewById(R.id.edittext_account);
        passwordEditText = view.findViewById(R.id.edittext_password);
        if (!account.equals("")) {
            accountEditText.setText(account);
        }
        if (!password.equals("")) {
            passwordEditText.setText(password);
        }

        button = view.findViewById(R.id.button_account);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String account = accountEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                SharedPreferences.Editor editor = accountInfo.edit();
                editor.putString("account", account);
                editor.putString("password", password);
                editor.commit();

                loginShanbay(account, password);
            }
        });
        return view;
    }

    private void loginShanbay(String account, String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        final PersistentCookieStore cookieStore = new PersistentCookieStore(getActivity());
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
            client.post(getActivity(), url, entity, "application/json", new AsyncHttpResponseHandler() {
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
                            SharedPreferences.Editor editor = accountInfo.edit();
                            editor.putLong("expiry", expiryDate.getTime());
                            editor.putString("auth_token", value);
                            editor.putLong("user_id", user_id);
                            editor.putBoolean("have_login", true);
                            editor.commit();
                        }
                        AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("Tips")
                                .setMessage("登录成功")
                                .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {}
                                })
                                .create();
                        dialog.show();
                    }
                    catch (Exception e) {

                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle("Tips")
                            .setMessage("账号或者密码错误")
                            .setNegativeButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .create();
                    dialog.show();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
