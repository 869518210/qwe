package mtpatent.ccenrun.com.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView mWebView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.web_view_invoke_js).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //调用JS方法，并传递参数
                mWebView.loadUrl("javascript:invokedByJava('java_data')");
            }
        });

        mWebView = (WebView)findViewById(R.id.web_view);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setJavaScriptEnabled(true);

        /*
          WebView默认用系统自带浏览器处理页面跳转。
                            为了让页面跳转在当前WebView中进行，重写WebViewClient。
                            但是按BACK键时，不会返回跳转前的页面，而是退出本Activity。重写onKeyDown()方法来解决此问题。
         */
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);//使用当前WebView处理跳转
                return true;//true表示此事件在此处被处理，不需要再广播
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //有页面跳转时被回调
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                //页面跳转结束后被回调
            }
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(MainActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        /*
                            当WebView内容影响UI时调用WebChromeClient的方法
         */
        mWebView.setWebChromeClient(new WebChromeClient() {
            /**
             * 处理JavaScript Alert事件
             */
            @Override
            public boolean onJsAlert(WebView view, String url,
                                     String message, final JsResult result) {
                //用Android组件替换
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("JS提示")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                        .setCancelable(false)
                        .create().show();
                return true;
            }
        });

        /*
                          绑定Java对象到WebView，这样可以让JS与Java通信(JS访问Java方法)
                          第一个参数是自定义类对象，映射成JS对象
                          第二个参数是第一个参数的JS别名
                          调用示例：
            mWebView.loadUrl("javascript:window.stub.jsMethod('param')");
         */
        mWebView.addJavascriptInterface(new JsToJava(), "stub");

        final EditText mEditText = (EditText)findViewById(R.id.web_view_text);

        findViewById(R.id.web_view_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = mEditText.getText().toString();
                if (url == null || "".equals(url)) {
                    Toast.makeText(MainActivity.this, "请输入URL", Toast.LENGTH_SHORT).show();
                } else {
                    if (!url.startsWith("http:") && !url.startsWith("file:")) {
                        url = "http://" + url;
                    }
                    mWebView.loadUrl(url);
                }
            }
        });
        //默认页面
//        默认操作
        mWebView.loadUrl("file:///android_asset/js_interact_demo.html");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //处理WebView跳转返回
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class JsToJava {
        public void jsMethod(String paramFromJS) {
            Log.i("CDH", paramFromJS);
        }
    }
}