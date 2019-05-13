package com.hostkarle.hostkarle;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    WebView webView;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    String url = "https://www.hostkarle.in";
    Activity activity;
    private ProgressDialog progDailog;


    @Override
    protected void onPause()
    {
        super.onPause();
        if (webView != null)
        {
            webView.onPause();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (webView != null)
        {
            webView.onResume();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        webView = new WebView(this);
        setContentView(webView);
        activity = this;
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            webView.getSettings().setSafeBrowsingEnabled(false);
        }
        progDailog = ProgressDialog.show(activity, "Loading", "Please wait...", true);
        progDailog.setCancelable(true);

        if (!haveNetworkConnection())
        {
            Toast.makeText(getActivity(),
                    "You are offline ",
                    Toast.LENGTH_SHORT)
                    .show();
        }
        load(url);
    }

    private void load(final String url)
    {
        if (webView != null)
        {
            final WebSettings webSettings = webView.getSettings();
            //webSettings.setLoadWithOverviewMode(true);
            //webSettings.setUseWideViewPort(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            }
            webSettings.setJavaScriptEnabled(true);
            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
            webSettings.setDomStorageEnabled(true); // for enabling website menus and other dynamic

            webSettings.setAppCachePath(getFilesDir().getPath() + getPackageName() + "/cache");
            webSettings.setAppCacheEnabled(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webSettings.setSupportZoom(true);
            webSettings.setLoadsImagesAutomatically(true);


            webView.setWebViewClient(new WebViewClient()
            {

                @Override
                public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail)
                {
                    return super.onRenderProcessGone(view, detail);
                }

                @Override
                public void onReceivedSslError(final WebView view, final SslErrorHandler handler, SslError error)
                {
                    if (progDailog != null)
                    {
                        progDailog.dismiss();
                    }


                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    String message = "SSL Certificate error.";
                    switch (error.getPrimaryError())
                    {
                        case SslError.SSL_UNTRUSTED:
                            message += "\nThe certificate authority is not trusted.";
                            break;
                        case SslError.SSL_EXPIRED:
                            message += "\nThe certificate has expired.";
                            break;
                        case SslError.SSL_IDMISMATCH:
                            message += "\nThe certificate Hostname mismatch.";
                            break;
                        case SslError.SSL_NOTYETVALID:
                            message += "\nThe certificate is not yet valid.";
                            break;
                    }
                    message += "\nDo you want to continue anyway?";

                    builder.setTitle("SSL Certificate Error");
                    builder.setMessage(message);
                    builder.setPositiveButton("continue", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            handler.proceed();
                        }
                    });
                    builder.setNegativeButton("cancel", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            view.goBack();
                            dialog.dismiss();
                            handler.cancel();
                        }
                    });
                    final AlertDialog dialog = builder.create();
                    dialog.show();
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
                {
                    Toast.makeText(getActivity(), "Your Internet Connection May not be active Or " + description, Toast.LENGTH_LONG).show();
                    view.loadUrl("file:///android_asset/error.html");
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request,
                                            WebResourceError error)
                {

                    if (progDailog != null && progDailog.isShowing())
                    {
                        progDailog.dismiss();
                    }
                }


                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon)
                {
                    super.onPageStarted(view, url, favicon);

                    if (progDailog != null)
                    {
                        progDailog.show();
                    }
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url)
                {
                    if (!haveNetworkConnection())
                    {
                        if (progDailog != null)
                        {
                            progDailog.cancel();
                        }
                        view.loadUrl("file:///android_asset/error.html");
                        return false;
                    }

                    if (!(url.contains("hostkarle.in")) || url.contains("mailto"))
                    {
                        Toast.makeText(MainActivity.this, "Opening", Toast.LENGTH_SHORT).show();
                        try
                        {

                            view.getContext().startActivity(
                                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            return true;
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(MainActivity.this, "Error\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                    else
                    {
                        view.loadUrl(url);
                        return true;
                    }

                }

                @Override
                public void onPageFinished(WebView view, final String url)
                {
                    super.onPageFinished(view, url);
                    if (progDailog.isShowing() && progDailog != null)
                    {
                        progDailog.dismiss();
                    }

                }
            });

            webView.loadUrl(url);
        }
    }

    public void onBackPressed()
    {

        if (webView != null)
        {
            if (webView.canGoBack())
            {
                webView.goBack();
            }
            else
            {
                builder = new AlertDialog.Builder(getActivity());
                LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                final View view = layoutInflater.inflate(R.layout.sample, null);
                builder.setView(view);
                int love = 0x2764;
                builder.setNegativeButton("No!! I love hostkarle " + getEmoji(love) + " Stay here.", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.cancel();
                    }
                }).setCancelable(false);
                dialog = builder.show();
                TextView messageView = dialog.findViewById(android.R.id.message);
                if (messageView != null)
                {
                    messageView.setGravity(Gravity.CENTER);
                }
            }
        }
    }

    String getEmoji(int unicode)
    {
        return new String(Character.toChars(unicode));
    }

    public void exit(View view)
    {
        if (dialog != null && dialog.isShowing())
        {
            dialog.dismiss();
        }

        finish();

    }

    public Activity getActivity()
    {
        return activity;
    }


    private boolean haveNetworkConnection()
    {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = new NetworkInfo[0];
        if (cm != null)
        {
            netInfo = cm.getAllNetworkInfo();
        }

        for (NetworkInfo ni : netInfo)
        {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
            {
                if (ni.isConnected())
                {
                    haveConnectedWifi = true;
                }
            }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
            {
                if (ni.isConnected())
                {
                    haveConnectedMobile = true;
                }
            }
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


}