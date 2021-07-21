package com.example.hybridapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private Button refreshBtn;
    private Button showBtn;
    private EditText editText;
    private MainActivity self = this;
    private Button showBtn2;
    private NativeSDk nativeSDk = new NativeSDk(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        refreshBtn = findViewById(R.id.refreshBtn);
        showBtn = findViewById(R.id.showBtn);
        editText = findViewById(R.id.editText);
        showBtn2 = findViewById(R.id.showBtn2);

        webView.loadUrl("http://192.168.1.4:8080/?timestamp" + new Date().getTime());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient()
//            {
//                @Override
//                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                    if(!message.startsWith("jsBridge")){
//                        return super.onJsAlert(view, url, message, result);
//                    }
//
//                    String Text = message.substring(message.indexOf("=") + 1);
//                    self.showNativeDialog(Text);
//                    result.confirm();
//                    return true;
//                }
//            }
        );
        webView.addJavascriptInterface(new NativeBridge(this), "nativeBridge");

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.loadUrl("http://192.168.1.4:8080/?timestamp" + new Date().getTime());
            }
        });

        showBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputValue = editText.getText().toString();
                self.showWebDialog(inputValue);
            }
        });

        showBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nativeSDk.getWebEditTextValue(new Callback() {
                    @Override
                    public void invoke(String value) {
                        new AlertDialog.Builder(self)
                                .setMessage("Web 输入值："+value).create().show();
                    }
                });
            }
        });
    }

    private void showWebDialog(String text) {
        String jsCode = String.format("window.showWebDialog('%s')", text);
        webView.evaluateJavascript(jsCode, null);
    }

//    private void showNativeDialog(String text) {
//        new AlertDialog.Builder(this).setMessage(text).create().show();
//    }

    interface Callback {
        void invoke(String value);
    }

    class NativeSDk {
        private int id = 1;
        private Map<Integer, Callback> callbackMap = new HashMap();
        private Context ctx;
        NativeSDk(Context ctx) {
            this.ctx = ctx;
        }
        void getWebEditTextValue(Callback callback){
            int callbackId = id++;
            callbackMap.put(callbackId, callback);
            final String jsCode = String.format("window.JSSDK.getWebEditTextValue(%s)", callbackId);
            ((MainActivity)ctx).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((MainActivity)ctx).webView.evaluateJavascript(jsCode, null);
                }
            });
        }
        void receiveMessage(int callbackId, String value){
            if(callbackMap.containsKey(callbackId)){
                callbackMap.get(callbackId).invoke(value);
            }
        }
    }

    class NativeBridge {
        private Context ctx;
        NativeBridge(Context ctx){
            this.ctx = ctx;
        }
        @JavascriptInterface
        public void showNativeDialog (String text) {
            new AlertDialog.Builder(ctx).setMessage(text).create().show();
        }

        @JavascriptInterface
        public void getNativeEditTextValue(int callbackId){
            final MainActivity mainActivity = (MainActivity)ctx;
            String value = mainActivity.editText.getText().toString();
            final String jsCode = String.format("window.JSSDK.receiveMessage(%s, '%s')", callbackId, value);
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.webView.evaluateJavascript(jsCode, null);
                }
            });
        }

        @JavascriptInterface
        public void receiveMessage(int callbackId, String value){
            ((MainActivity)ctx).nativeSDk.receiveMessage(callbackId, value);
        }
    }
}