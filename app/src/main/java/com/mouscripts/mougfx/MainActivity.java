package com.mouscripts.mougfx;

import static android.webkit.WebView.RENDERER_PRIORITY_BOUND;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Locale;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {
    private WebView webView;

    Context mContext = this;
    private UnrealSaveEditor editor;
    private Uri currentFileUri;
    private String savePath;
    private String UserCustomIniPath;
    private String sys_lang = "en";

    private final String TAG = "MOU_GFX";
//    private Button btnSelectFile; // تعريف الزر على مستوى الكلاس
//    private TextView LobbyFPSTextView, BattleFPSTextView,BattleRenderQualityTextView,LobbyRenderQualityTextView;

    private String IntentExtra_string = "";
    private String Share_data = "";
    private String Share_or_notify = "0";
    private FrameLayout fullScreenContainer;
    private String country;
    public static boolean e_m = true;
    private boolean shizukuBinderReceived = false;
    private boolean shizukuPermissionRequested = false;
    private boolean autoLoadAttempted = false;
    public static boolean get_em(){
        return e_m;
    }
    private View floatingPanelView;
    public static void setLocaleToEnglish(Context context) {
        Locale locale = new Locale("en");
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    private final Shizuku.OnRequestPermissionResultListener mPermissionReceiver = (requestCode, grantResult) -> {
        if (grantResult == PackageManager.PERMISSION_GRANTED) {
            shizukuPermissionRequested = true;
            Toast.makeText(this, "تم منح صلاحية Shizuku، جاري المحاولة مرة أخرى", Toast.LENGTH_SHORT).show();
            retryAutoLoad();
        }
    };

    private final Shizuku.OnBinderReceivedListener mBinderReceivedListener = () -> {
        shizukuBinderReceived = true;
        retryAutoLoad();
    };

    private void retryAutoLoad() {
        if (autoLoadAttempted && editor.getFullFileData() == null) {
            tryAutoLoad();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Shizuku.addRequestPermissionResultListener(mPermissionReceiver);
        Shizuku.addBinderReceivedListener(mBinderReceivedListener);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        MainActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        getWindow().setStatusBarColor(android.graphics.Color.BLACK);
        getWindow().setNavigationBarColor(android.graphics.Color.BLACK);



        editor = new UnrealSaveEditor();
        savePath = Environment.getExternalStorageDirectory() + "/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/SaveGames/active.sav";
        UserCustomIniPath = Environment.getExternalStorageDirectory() + "/Android/data/com.tencent.ig/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Config/Android/UserCustom.ini";
//        savePath = Environment.getExternalStorageDirectory() + "/Download/active.sav";
//        UserCustomIniPath = Environment.getExternalStorageDirectory() + "/Download/UserCustom.ini";

//        Log.d(TAG, "onCreate: savePath => "+ savePath);



        Locale currentLocale = Locale.getDefault();
        sys_lang = currentLocale.getLanguage(); // e.g., "en" for English
        country = currentLocale.getCountry();   // e.g., "US" for United States

        // Force WebView locale to English so digits always render in English
        Configuration webViewCfg = new Configuration(getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webViewCfg.setLocale(Locale.ENGLISH);
        } else {
            webViewCfg.locale = Locale.ENGLISH;
        }
        getResources().updateConfiguration(webViewCfg, getResources().getDisplayMetrics());

        final String databasePath = MainActivity.this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        webView = findViewById(R.id.WebView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setHapticFeedbackEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.getSettings().setTextZoom(100);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webView.setRendererPriorityPolicy(RENDERER_PRIORITY_BOUND, true);
        }
        webSettings.setDatabasePath(databasePath);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setAllowFileAccess(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "mouscripts");
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setSupportZoom(false);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setDisplayZoomControls(false);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setSaveFormData(true);
        if (Build.VERSION.SDK_INT >= 19) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.requestFocus();
        webView.clearCache(true);
//        PackageInfo mWebViewInfo = WebViewCompat.getCurrentWebViewPackage(getApplicationContext());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                        .setOnDismissListener((DialogInterface dialog) -> result.confirm())
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(view.getContext())
                        .setTitle("")
                        .setMessage(message)
                        .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                        .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                        .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                        .create()
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                final EditText input = new EditText(view.getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(defaultValue);
                new AlertDialog.Builder(view.getContext())
                        .setTitle("")
                        .setMessage(message)
                        .setView(input)
                        .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm(input.getText().toString()))
                        .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                        .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                        .create()
                        .show();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onExceededDatabaseQuota(String url,
                                                String databaseIdentifier, long currentQuota,
                                                long estimatedSize, long totalUsedQuota,
                                                WebStorage.QuotaUpdater quotaUpdater) {
                quotaUpdater.updateQuota(5 * 1024 * 1024);
            }


        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if(errorCode == -1){
                    Log.d(TAG, "onReceivedError: Oh no! " + errorCode);
                    webView.loadUrl("file:///android_asset/index.html");

                }
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                runJsCode("set_app_language('"+ sys_lang +"')");
                runJsCode("set_e_m(" + e_m + ")");
                tryAutoLoad();
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(mPermissionReceiver);
        Shizuku.removeBinderReceivedListener(mBinderReceivedListener);
    }
    private void tryAutoLoad() {
        new Thread(() -> {
            Log.d(TAG, "محاولة تحميل الملف: " + savePath);
            autoLoadAttempted = true;

            // 1. Root
            if (isRootAvailable()) {
                Log.d(TAG, "محاولة القراءة عبر Root...");
                runOnUiThread(() -> Toast.makeText(this, "جاري محاولة الوصول عبر Root...", Toast.LENGTH_SHORT).show());
                byte[] rootData = readViaRoot(savePath);
                if (rootData != null) {
                    try {
                        editor.loadFromBytes(rootData);
                        showToastAndExit("تم التحميل عبر Root");
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "فشل تحليل البيانات من Root: " + e.getMessage());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(this,
                        "لم يتم الرد على طلب صلاحية Root. افتح Magick ومنح الصلاحية للتطبيق",
                        Toast.LENGTH_LONG).show());
                }
            }

            // 2. Shizuku
            boolean shizukuRunning = Shizuku.pingBinder();
            if (shizukuRunning) {
                shizukuBinderReceived = true;
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "محاولة القراءة عبر Shizuku...");
                    try {
                        editor.load(savePath);
                        if (editor.getFullFileData() != null) {
                            showToastAndExit("تم التحميل عبر Shizuku");
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "فشل التحميل عبر Shizuku: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "طلب صلاحية Shizuku...");
                    shizukuPermissionRequested = true;
                    runOnUiThread(() -> {
                        Shizuku.requestPermission(1001);
                        Toast.makeText(this, "افتح تطبيق Shizuku ومنح الصلاحية لهذا التطبيق", Toast.LENGTH_LONG).show();
                    });
                    waitForShizukuPermission(3);
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "محاولة القراءة عبر Shizuku بعد منح الصلاحية...");
                        try {
                            editor.load(savePath);
                            if (editor.getFullFileData() != null) {
                                showToastAndExit("تم التحميل عبر Shizuku");
                                return;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Shizuku load after permission failed: " + e.getMessage());
                        }
                    }
                }
            }

            // 3. Shizuku غير شغال - نحاول نشغله
            if (!shizukuBinderReceived) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Shizuku غير شغال، جاري فتحه...", Toast.LENGTH_LONG).show();
                    try {
                        Intent intent = getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");
                        if (intent != null) startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "فشل فتح Shizuku: " + e.getMessage());
                    }
                });
                for (int i = 0; i < 6; i++) {
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                    if (Shizuku.pingBinder()) {
                        shizukuBinderReceived = true;
                        break;
                    }
                }
                if (shizukuBinderReceived && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "محاولة القراءة عبر Shizuku بعد الانتظار...");
                    try {
                        editor.load(savePath);
                        if (editor.getFullFileData() != null) {
                            showToastAndExit("تم التحميل عبر Shizuku");
                            return;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Shizuku load after wait failed: " + e.getMessage());
                    }
                }
            }

            // 4. المسار المباشر
            Log.d(TAG, "محاولة الوصول عبر المسار التقليدي: " + savePath);
            File saveFile = new File(savePath);
            if (saveFile.exists() && saveFile.canRead()) {
                try (FileInputStream fis = new FileInputStream(saveFile)) {
                    byte[] data = inputStreamToBytes(fis);
                    editor.loadFromBytes(data);
                    showToastAndExit("تم التحميل من المسار التلقائي");
                    return;
                } catch (Exception e) {
                    Log.e(TAG, "فشل القراءة من المسار التقليدي رغم وجوده: " + e.getMessage());
                }
            }

            // 5. URI المخزن
            if (currentFileUri != null && loadByUri(currentFileUri)) {
                return;
            }

            // 6. الاختيار اليدوي
            runOnUiThread(this::showManualSelectionUI);
        }).start();
    }

    private void waitForShizukuPermission(int timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) return;
            try { Thread.sleep(300); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return; }
        }
    }
    // دالة مساعدة للقراءة عبر الـ URI
    private boolean loadByUri(Uri uri) {
        try (InputStream is = getContentResolver().openInputStream(uri)) {
            byte[] data = inputStreamToBytes(is);
            editor.loadFromBytes(data);
            showToastAndExit("تم التحميل باستخدام الصلاحية المخزنة");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "فشلت الصلاحية المخزنة: " + e.getMessage());
            return false;
        }
    }

    private void showToastAndExit(String msg) {
        runOnUiThread(() -> {
            updateUI();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });
    }
    private void showManualSelectionUI() {
        runOnUiThread(() -> {
            // إظهار الزر للمستخدم ليقوم بالاختيار بنفسه
//            btnSelectFile.setVisibility(android.view.View.VISIBLE);
            Toast.makeText(this, "فشل الوصول التلقائي، اختر الملف يدويًا", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // لضمان ظهور كافة التطبيقات التي تدير الملفات
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // نطلب من الأندرويد إعطاءنا صلاحية دائمة للملف
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // هذا الكود يفتح نافذة تطلب من المستخدم اختيار التطبيق (ومن ضمنهم ZArchiver)
            startActivityForResult(Intent.createChooser(intent, "اختر الملف باستخدام ZArchiver أو مدير الملفات"), 100);
        });
    }
    private void loadIniSettings(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n"); // هنا الـ \n موجودة فعلاً
            }

            String allIniText = sb.toString();

            // تحويل النص إلى Base64 لضمان سلامته أثناء النقل
            byte[] data = allIniText.getBytes(StandardCharsets.UTF_8);
            String base64Ini = Base64.encodeToString(data, Base64.NO_WRAP);

            runOnUiThread(() -> {
                // نرسل النص مشفراً بـ Base64 للـ JS
                runJsCode("handleIniData('" + base64Ini + "')");
            });
        } catch (IOException e) {
            Log.e(TAG, "خطأ في قراءة ملف الـ INI: " + e.getMessage());
        }
    }
    private void openFilePicker() {
        Toast.makeText(this, "يرجى اختيار ملف active.sav يدويًا", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            currentFileUri = data.getData();
            processUriFile(currentFileUri);
        }
    }

    private void processUriFile(Uri uri) {
        new Thread(() -> {
            try (InputStream is = getContentResolver().openInputStream(uri)) {
                byte[] data = inputStreamToBytes(is);
                editor.loadFromBytes(data);
                runOnUiThread(this::updateUI);
            } catch (Exception e) {
                Log.e(TAG, "خطأ في اختيار الملف: " + e.getMessage());
            }
        }).start();
    }

    private byte[] inputStreamToBytes(InputStream is) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int read;
        while ((read = is.read(temp)) != -1) {
            buffer.write(temp, 0, read);
        }
        return buffer.toByteArray();
    }

    private void updateUI() {
        // استخراج القيم

//        Integer LobbyFPS = (Integer) editor.readValue("LobbyFPS");
//        Integer BattleFPS = (Integer) editor.readValue("BattleFPS");
//        Integer BattleRenderQuality = (Integer) editor.readValue("BattleRenderQuality");
//        Integer LobbyRenderQuality = (Integer) editor.readValue("LobbyRenderQuality");

        String file_props = editor.getPropertiesAsJson();
        String base64Data = Base64.encodeToString(file_props.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
//        Log.d(TAG, "updateUI: file_props => " + file_props);
        runJsCode("load_file_props('"+ base64Data + "')");

//        if (LobbyFPS != null) LobbyFPSTextView.setText("LobbyFPS : " + LobbyFPS);
//        if (BattleFPS != null) BattleFPSTextView.setText("BattleFPS : " + BattleFPS);
//        if (BattleRenderQuality != null)
//            BattleRenderQualityTextView.setText("BattleRenderQuality : " + BattleRenderQuality);
//        if (LobbyRenderQuality != null)
//            LobbyRenderQualityTextView.setText("LobbyRenderQuality : " + LobbyRenderQuality);
    }

    private void saveModifiedFile() {
        new Thread(() -> {
            try {
                byte[] data = editor.getFullFileData();
                if (data == null || data.length == 0) {
                    throw new IOException("لا توجد بيانات في الذاكرة للحفظ");
                }

                // 1. Root
                if (isRootAvailable()) {
                    Log.d(TAG, "محاولة الحفظ عبر Root...");
                    if (saveViaRoot(data, savePath)) {
                        Log.d(TAG, "تم الحفظ عبر Root");
                        runOnUiThread(() -> Toast.makeText(this, "تم الحفظ بنجاح عبر Root", Toast.LENGTH_SHORT).show());
                        return;
                    }
                }

                // 2. Shizuku
                if (Shizuku.pingBinder()) {
                    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "محاولة الحفظ عبر Shizuku...");
                        if (saveViaShizukuPrivileged(data, savePath)) {
                            Log.d(TAG, "تم الحفظ عبر Shizuku");
                            runOnUiThread(() -> Toast.makeText(this, "تم الحفظ بنجاح عبر Shizuku", Toast.LENGTH_SHORT).show());
                            return;
                        }
                    } else if (!shizukuPermissionRequested) {
                        shizukuPermissionRequested = true;
                        runOnUiThread(() -> Shizuku.requestPermission(1001));
                    }
                }

                // 3. URI (SAF)
                if (currentFileUri != null) {
                    Log.d(TAG, "محاولة الحفظ عبر الـ Uri...");
                    try (OutputStream os = getContentResolver().openOutputStream(currentFileUri)) {
                        editor.save(os);
                        Log.d(TAG, "تم الحفظ عبر الـ Uri");
                        runOnUiThread(() -> Toast.makeText(this, "تم الحفظ بنجاح!", Toast.LENGTH_SHORT).show());
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "فشل الحفظ عبر URI: " + e.getMessage());
                    }
                }

                // 4. المسار المباشر
                File file = new File(savePath);
                if (file.exists() && file.canWrite()) {
                    try (OutputStream os = new java.io.FileOutputStream(file)) {
                        editor.save(os);
                        Log.d(TAG, "تم الحفظ في المسار المباشر");
                        runOnUiThread(() -> Toast.makeText(this, "تم الحفظ بنجاح!", Toast.LENGTH_SHORT).show());
                        return;
                    } catch (Exception e) {
                        Log.e(TAG, "فشل الحفظ المباشر: " + e.getMessage());
                    }
                }

                // 5. فشل كل الطرق - فتح اختيار يدوي
                runOnUiThread(() -> {
                    Toast.makeText(this, "فشلت جميع محاولات الحفظ، اختر الملف يدويًا", Toast.LENGTH_LONG).show();
                    openFilePicker();
                });

            } catch (Exception e) {
                Log.e(TAG, "خطأ أثناء الحفظ: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "فشل الحفظ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    @JavascriptInterface // Required for security
    public void showToast(String toast) {

        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();

        // Optional: Call JS back from Java
        runOnUiThread(() -> {
            webView.loadUrl("javascript:updateFromAndroid('Java received: " + toast + "')");
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }



    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        webView.loadUrl("file:///android_asset/index.html");
    }
    public void runJsCode(String Js_code) {
        webView.post(() -> {
            webView.evaluateJavascript(Js_code, value -> {
                // 'value' is the result returned from your JS code (as a JSON string)
                Log.d("WebView", "JS Result: " + value);
            });
        });
    }
    public class WebAppInterface {
        Context mContext;

        WebAppInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast) {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, toast, Toast.LENGTH_SHORT).show();
                }
            });
        }
        @JavascriptInterface
        public void exit_app()
        {
            MainActivity.this.finish();
            System.exit(0);
        }
        @JavascriptInterface
        public String get_sys_lang()
        {
            return  sys_lang;
        }
        @JavascriptInterface
        public void share_text_to_apps(String Title ,String text){
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, Title);
            startActivity(Intent.createChooser(intent, text));
        }
        @JavascriptInterface
        public void remove_ads(){
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {

                }
            });
        }
        @JavascriptInterface
        public void open_external_link(String link) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        }
        @JavascriptInterface
        public String GetPackageName() {
            return getPackageName();
        }
        @JavascriptInterface
        public boolean is_network_available() {
            return isNetworkAvailable(mContext);
        }
        @JavascriptInterface
        public void InitMobileAds() {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
//                    init_mobile_ads();
                }
            });
        }
        @JavascriptInterface
        public void ShowAdmobInterstitial() {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
//                    show_admob_Interstitial();
                }
            });
        }

        @JavascriptInterface
        public String get_file_props() {
            String props = editor.getAllPropertiesJson();
            return Base64.encodeToString(props.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        }

        private int tryEntryChainLen(byte[] data, int start) {
            int pos = start;
            int count = 0;
            while (pos + 8 <= data.length) {
                int nameLen = ByteBuffer.wrap(data, pos, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                if (nameLen > 0 && nameLen < 200 && pos + 4 + nameLen + 4 <= data.length) {
                    byte[] nb = new byte[nameLen];
                    System.arraycopy(data, pos + 4, nb, 0, nameLen);
                    String name = new String(nb, StandardCharsets.UTF_8).replace("\0", "");
                    boolean printable = name.length() > 0;
                    for (int ci = 0; printable && ci < name.length(); ci++) {
                        if (name.charAt(ci) < 32 || name.charAt(ci) > 126) printable = false;
                    }
                    if (printable) {
                        count++;
                        pos = pos + 4 + nameLen + 4;
                        continue;
                    }
                }
                break;
            }
            return count;
        }

        private int findLastPropertyTypeEnd(byte[] data) {
            int best = -1;
            String text = new String(data, StandardCharsets.UTF_8);
            int searchFrom = 3;
            while (true) {
                int idx = text.indexOf("Property\0", searchFrom);
                if (idx < 0) break;
                // Walk backwards to find the start of the type name
                int nameStart = idx - 1;
                while (nameStart >= 0) {
                    char c = text.charAt(nameStart);
                    if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) break;
                    nameStart--;
                }
                nameStart++;
                // name must be at least 3 chars (like "Int")
                if (idx - nameStart >= 3) {
                    best = idx + "Property\0".length();
                }
                searchFrom = idx + 1;
            }
            return best;
        }

        private int indexOfBytes(byte[] haystack, byte[] needle, int start) {
            for (int i = start; i <= haystack.length - needle.length; i++) {
                boolean match = true;
                for (int j = 0; j < needle.length; j++) {
                    if (haystack[i + j] != needle[j]) { match = false; break; }
                }
                if (match) return i;
            }
            return -1;
        }

        @JavascriptInterface
        public String parseBlobProperty(String key) {
            try {
                UnrealSaveEditor.UEProperty prop = editor.getProperty(key);
                if (prop == null || !(prop.value instanceof byte[])) return "{\"parsed\":false}";

                byte[] data = (byte[]) prop.value;

                // --- Try name-value pairs format ---
                int maxScan = Math.min(data.length, 512);
                int bestStart = -1;
                int bestLen = 0;
                for (int s = 0; s < maxScan; s++) {
                    int cnt = tryEntryChainLen(data, s);
                    if (cnt > bestLen) {
                        bestLen = cnt;
                        bestStart = s;
                    }
                }

                if (bestLen >= 2) {
                    JSONArray entries = new JSONArray();
                    int pos = bestStart;
                    while (pos + 8 <= data.length) {
                        int nameLen = ByteBuffer.wrap(data, pos, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                        if (nameLen > 0 && nameLen < 200 && pos + 4 + nameLen + 4 <= data.length) {
                            byte[] nb = new byte[nameLen];
                            System.arraycopy(data, pos + 4, nb, 0, nameLen);
                            String name = new String(nb, StandardCharsets.UTF_8).replace("\0", "");
                            boolean printable = name.length() > 0;
                            for (int ci = 0; printable && ci < name.length(); ci++) {
                                if (name.charAt(ci) < 32 || name.charAt(ci) > 126) printable = false;
                            }
                            if (printable) {
                                int value = ByteBuffer.wrap(data, pos + 4 + nameLen, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
                                JSONObject entry = new JSONObject();
                                entry.put("name", name);
                                entry.put("value", value);
                                entry.put("pos", pos);
                                entry.put("nameLen", nameLen);
                                entries.put(entry);
                                pos = pos + 4 + nameLen + 4;
                                continue;
                            }
                        }
                        break;
                    }
                    JSONObject result = new JSONObject();
                    result.put("parsed", true);
                    result.put("mode", "pairs");
                    result.put("entries", entries);
                    return result.toString();
                }

                // --- Try raw array format (Int32, Float, etc.) ---
                if (data.length >= 8) {
                    int lastPropEnd = findLastPropertyTypeEnd(data);
                    if (lastPropEnd > 0) {
                        int elemSize = 4;
                        int pos = lastPropEnd;
                        while (pos < data.length && data[pos] == 0) pos++;
                        int remaining = data.length - pos;
                        JSONArray values = new JSONArray();
                        int valuePos = pos;
                        int valuesCount = 0;

                        if (remaining >= 4) {
                            int firstInt = ByteBuffer.wrap(data, pos, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();

                            if (firstInt > 0 && firstInt < 5000 &&
                                Math.abs(firstInt * elemSize - (remaining - 4)) <= 4) {
                                valuePos = pos + 4;
                                valuesCount = firstInt;
                            } else {
                                valuesCount = remaining / elemSize;
                            }

                            for (int i = 0; i < valuesCount; i++) {
                                if (valuePos + i * elemSize + elemSize <= data.length) {
                                    int v = ByteBuffer.wrap(data, valuePos + i * elemSize, elemSize)
                                        .order(ByteOrder.LITTLE_ENDIAN).getInt();
                                    values.put(v);
                                }
                            }
                        } else if (remaining > 0) {
                            // Single truncated value
                            byte[] padded = new byte[4];
                            System.arraycopy(data, pos, padded, 0, remaining);
                            int v = ByteBuffer.wrap(padded).order(ByteOrder.LITTLE_ENDIAN).getInt();
                            values.put(v);
                            valuesCount = 1;
                        }

                        if (values.length() > 0) {
                            JSONObject result = new JSONObject();
                            result.put("parsed", true);
                            result.put("mode", "array");
                            result.put("values", values);
                            result.put("valuePos", valuePos);
                            result.put("valueLen", elemSize);
                            result.put("count", valuesCount);
                            return result.toString();
                        }
                    }
                }

                // fallback: return hex dump
                JSONObject fallback = new JSONObject();
                fallback.put("parsed", false);
                StringBuilder hex = new StringBuilder();
                for (byte b : data) hex.append(String.format("%02X", b));
                fallback.put("hex", hex.toString());
                return fallback.toString();
            } catch (Exception e) {
                Log.e(TAG, "parseBlobProperty error: " + e.getMessage());
                return "{\"parsed\":false}";
            }
        }

        @JavascriptInterface
        public void updateBlobArray(String key, String valuesJson, int valuePos, int valueLen) {
            try {
                UnrealSaveEditor.UEProperty prop = editor.getProperty(key);
                if (prop == null) return;

                byte[] fileData = editor.getFullFileData();
                byte[] rawData = prop.rawData;
                JSONArray values = new JSONArray(valuesJson);

                for (int i = 0; i < values.length(); i++) {
                    int newValue = values.getInt(i);
                    int writeOffset = prop.offset + valuePos + i * valueLen;
                    ByteBuffer bb = ByteBuffer.allocate(valueLen).order(ByteOrder.LITTLE_ENDIAN);
                    if (valueLen == 4) bb.putInt(newValue);
                    else if (valueLen == 2) bb.putShort((short) newValue);
                    else if (valueLen == 1) bb.put((byte) newValue);
                    byte[] valBytes = bb.array();
                    System.arraycopy(valBytes, 0, fileData, writeOffset, valueLen);
                    System.arraycopy(valBytes, 0, rawData, valuePos + i * valueLen, valueLen);
                }
                Log.d(TAG, "updateBlobArray: تم تحديث " + key);
            } catch (Exception e) {
                Log.e(TAG, "updateBlobArray error: " + e.getMessage());
            }
        }

        @JavascriptInterface
        public void updateBlobProperty(String key, String entriesJson) {
            try {
                UnrealSaveEditor.UEProperty prop = editor.getProperty(key);
                if (prop == null) return;

                byte[] fileData = editor.getFullFileData();
                byte[] rawData = prop.rawData;
                JSONArray entries = new JSONArray(entriesJson);

                for (int i = 0; i < entries.length(); i++) {
                    JSONObject entry = entries.getJSONObject(i);
                    int entryPos = entry.getInt("pos");
                    int nameLen = entry.getInt("nameLen");
                    int newValue = entry.getInt("value");

                    int valueOffset = prop.offset + entryPos + 4 + nameLen;
                    ByteBuffer valBb = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
                    valBb.putInt(newValue);
                    byte[] valBytes = valBb.array();

                    // Update full file data
                    System.arraycopy(valBytes, 0, fileData, valueOffset, 4);
                    // Also update raw data in-memory so next get_file_props reflects changes
                    System.arraycopy(valBytes, 0, rawData, entryPos + 4 + nameLen, 4);
                }

                Log.d(TAG, "updateBlobProperty: تم تحديث " + key);
            } catch (Exception e) {
                Log.e(TAG, "updateBlobProperty error: " + e.getMessage());
            }
        }

        @JavascriptInterface
        public void apply_file_props(String new_props) {
            try {
                // 1. تحويل النص القادم من JS إلى كائن JSON
                JSONObject jsonObject = new JSONObject(new_props);

                // 2. الحصول على جميع المفاتيح (مثل LobbyFPS, BattleFPS...)
                Iterator<String> keys = jsonObject.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = jsonObject.get(key);

                    // 3. تحديث القيم في الـ Editor
                    // ملاحظة: سنقوم بتحديث الـ Editor في الذاكرة أولاً
                    editor.updateValue(key, value);

                    Log.d("MOU_GFX", "تم تحديث " + key + " إلى " + value);
                }

                // 4. تنفيذ عملية الحفظ الفعلية في الملف
                runOnUiThread(() -> {
                    saveModifiedFile(); // استدعاء دالة الحفظ التي كتبناها سابقاً
                    Toast.makeText(mContext, "تم تطبيق الإعدادات وحفظ الملف!", Toast.LENGTH_SHORT).show();
                });

            } catch (JSONException e) {
                Log.e("MOU_GFX", "خطأ في تحليل بيانات JSON القادمة: " + e.getMessage());
            }
        }
        @JavascriptInterface
        public void launchGame() {
            String packageName = "com.tencent.ig"; // النسخة العالمية
            // ملاحظة: لنسخة فيتنام (com.vng.pubgmobile)، كوريا (com.pubg.krmobile)

            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } else {
                Toast.makeText(mContext, "اللعبة غير مثبتة على هذا الجهاز!", Toast.LENGTH_SHORT).show();
            }
        }
        @JavascriptInterface
        public void saveIniFile(String base64Data) {
            try {
                // 1. فك الـ Base64
                byte[] decodedBytes = Base64.decode(base64Data, Base64.DEFAULT);
                String finalContent = new String(decodedBytes, StandardCharsets.UTF_8);

                // 2. فتح الملف وحفظ النص الجديد
                File file = new File(UserCustomIniPath);
                file.setWritable(true); // فتح القفل للكتابة

                FileWriter fw = new FileWriter(file, false); // false تعني مسح القديم وكتابة الجديد
                fw.write(finalContent);
                fw.close();

                // 3. إعادة القفل (Read-Only)
                file.setWritable(false);

                try {
                    Runtime.getRuntime().exec("chmod 444 " + UserCustomIniPath);
                } catch (IOException ignored) {}

//                runOnUiThread(() -> Toast.makeText(mContext, "تم حفظ الإعدادات وقفل الملف!", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                Log.e("MOU_GFX", "Error saving INI: " + e.getMessage());
            }
        }
        @JavascriptInterface
        public void applyResLaunchAndReset(int w, int h, int dpi) {
            runOnUiThread(() -> {
                applyResLaunchAndResett(w, h, dpi);
            });
        }


        @JavascriptInterface
        public void reset_phone_reselution() {
            runOnUiThread(() -> {
                resetResolution();
            });
        }

        @JavascriptInterface
        public void applyResolution(int w, int h, int dpi) {
            runOnUiThread(() -> {
                new Thread(() -> {
                    try {
                        if (Shizuku.pingBinder()) {
                            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                                executeShellCommand(String.format("wm size %dx%d && wm density %d", w, h, dpi));
                                Thread.sleep(500);
                                String gamePackage = "com.tencent.ig";
                                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(gamePackage);
                                if (launchIntent != null) {
                                    startActivity(launchIntent);
                                }
                                runOnUiThread(() -> {
                                    showFloatingOverlay(w, h, dpi);
                                });
                            } else {
                                Shizuku.requestPermission(1002);
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "يرجى تشغيل خادم Shizuku أولاً", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "applyResolution error: " + e.getMessage());
                    }
                }).start();
            });
        }

        @JavascriptInterface
        public void showFloatingPanel(int w, int h, int dpi) {
            runOnUiThread(() -> {
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    showFloatingOverlay(w, h, dpi);
                } else {
                    Toast.makeText(MainActivity.this, "يرجى منح صلاحية الظهور فوق التطبيقات أولاً", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            });
        }

        @JavascriptInterface
        public void hideAndResetPanel() {
            runOnUiThread(() -> {
                resetResolution();
                hideFloatingOverlay();
            });
        }

        @JavascriptInterface
        public void dismissPanelOnly() {
            runOnUiThread(() -> {
                hideFloatingOverlay();
            });
        }

        @JavascriptInterface
        public boolean canDrawOverlays() {
            return Settings.canDrawOverlays(MainActivity.this);
        }

        @JavascriptInterface
        public void requestOverlayPermission() {
            runOnUiThread(() -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            });
        }

    }

    private void showFloatingOverlay(int w, int h, int dpi) {
        hideFloatingOverlay();
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setPadding(24, 16, 24, 16);
        layout.setElevation(20);
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setCornerRadius(24);
        bg.setColor(0xDD000000);
        layout.setBackground(bg);

        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        TextView infoText = new TextView(this);
        infoText.setText(String.format(Locale.ENGLISH, "المنظور: %dx%d  DPI: %d", w, h, dpi));
        infoText.setTextColor(0xFFE53935);
        infoText.setTextSize(16);
        infoText.setLayoutParams(textLp);

        Button resetBtn = new Button(this);
        resetBtn.setText("إعادة");
        resetBtn.setTextColor(0xFFFFFFFF);
        resetBtn.setBackgroundColor(0xFFE53935);
        resetBtn.setPadding(20, 8, 20, 8);
        resetBtn.setAllCaps(false);
        resetBtn.setTextSize(14);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnLp.setMarginStart(20);
        resetBtn.setLayoutParams(btnLp);
        resetBtn.setOnClickListener(v -> {
            resetResolution();
            hideFloatingOverlay();
        });

        layout.addView(infoText);
        layout.addView(resetBtn);

        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                android.graphics.PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 20;
        params.y = 150;

        layout.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float touchX, touchY;
            private boolean isMoving;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        isMoving = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - touchX;
                        float dy = event.getRawY() - touchY;
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isMoving = true;
                        }
                        params.x = initialX + (int) dx;
                        params.y = initialY + (int) dy;
                        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                        wm.updateViewLayout(layout, params);
                        return true;
                    case MotionEvent.ACTION_UP:
                        return isMoving;
                }
                return false;
            }
        });

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.addView(layout, params);
        floatingPanelView = layout;
    }

    private void hideFloatingOverlay() {
        if (floatingPanelView != null) {
            try {
                WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
                wm.removeView(floatingPanelView);
            } catch (Exception ignored) {}
            floatingPanelView = null;
        }
    }

    public void applyResLaunchAndResett(int width, int height, int density) {
        runOnUiThread(() -> {
            new Thread(() -> {
                try {

                    if (Shizuku.pingBinder()) {
                        // إذا كان يعمل، نطلب الصلاحية أو ننفذ الأمر
                        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                            executeShellCommand(String.format("wm size %dx%d && wm density %d", width, height, density));
                            // تأخير بسيط للتأكد من استقرار النظام
                            Thread.sleep(1000);

                            // 2. فتح اللعبة (ببجي العالمية كمثال)
                            // استبدل com.tencent.ig باسم حزمة اللعبة التي تريدها
                            String gamePackage = "com.tencent.ig";
                            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(gamePackage);
                            if (launchIntent != null) {
                                startActivity(launchIntent);
                            } else {
                                runOnUiThread(() -> Toast.makeText(this, "اللعبة غير مثبتة!", Toast.LENGTH_SHORT).show());
                            }

                            // 3. الانتظار لمدة 5 ثوانٍ وهي اللعبة تفتح
                            Thread.sleep(5000);

                            // 4. إعادة الضبط للوضع الطبيعي
                            executeShellCommand("wm size reset && wm density reset");

                            runOnUiThread(() -> Toast.makeText(this, "تمت إعادة الأبعاد لوضعها الطبيعي ✅", Toast.LENGTH_SHORT).show());

                        } else {
                            Shizuku.requestPermission(1002);
                        }
                    } else {
                        // إذا لم يكن يعمل، نوجه المستخدم لفتح التطبيق
                        Toast.makeText(this, "يرجى تشغيل خادم Shizuku أولاً", Toast.LENGTH_LONG).show();
                        try {
                            Intent intent = getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");
                            if (intent != null) {
                                startActivity(intent);
                            } else {
                                // إذا لم يكن التطبيق مثبتاً، نفتح المتجر
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=moe.shizuku.privileged.api")));
                            }
                        } catch (Exception e) {
                            Log.e("Shizuku", "فشل فتح التطبيق: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.e("Shizuku", "Error: " + e.getMessage());
                }
            }).start();
        });
    }
    private void executeShellCommand(String command) {
        // تشغيل العملية في خيط منفصل تماماً عن خيط الـ UI
        AsyncTask.execute(() -> {
            try {
                if (!Shizuku.pingBinder()) return;

                // استخدام "sh -c" مع وسيط واحد لضمان استقرار الإرسال
                String[] args = new String[]{"sh", "-c", command};
                java.lang.Process process = Shizuku.newProcess(args, null, null);

                // انتظر انتهاء العملية
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    runOnUiThread(() -> Toast.makeText(this, "تم بنجاح ✅", Toast.LENGTH_SHORT).show());
                } else {
                    Log.e("Shizuku", "فشل الأمر، كود الخروج: " + exitCode);
                }

                process.destroy(); // تنظيف العملية يدوياً
            } catch (Exception e) {
                Log.e("Shizuku", "خطأ في الـ Binder: " + e.getMessage());
            }
        });
    }
    // دالة لإعادة الأبعاد لوضعها الطبيعي
    public void resetResolution() {
        runOnUiThread(() -> {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                executeShellCommand("wm size reset && wm density reset");
                Toast.makeText(this, "تمت العودة للأبعاد الأصلية", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        final NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isRootAvailable() {
        // 1. File check first (for non-Magisk root)
        String[] paths = {"/system/xbin/su", "/system/bin/su", "/sbin/su", "/system/sd/xbin/su",
            "/data/adb/magisk/su", "/data/local/xbin/su", "/data/local/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) {
                Log.d(TAG, "تم العثور على su في: " + path);
                return true;
            }
        }

        // 2. Try running su with a short timeout.
        //    If it returns 0 -> root available + granted.
        //    If it times out -> su exists but Magisk prompt is blocking -> root is available.
        //    If it throws -> su not found.
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "id"});
            if (waitForProcess(process, 2)) {
                boolean hasRoot = process.exitValue() == 0;
                if (hasRoot) Log.d(TAG, "Root confirmed via su -c id");
                return hasRoot;
            } else {
                // Timed out => Magisk prompt is showing => su exists
                Log.d(TAG, "su موجود لكنه ينتظر القبول من Magisk");
                return true;
            }
        } catch (Exception e) {
            // su not found or other error
            return false;
        }
    }

    private byte[] readViaRoot(String path) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c", "cat \"" + path + "\""});
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] temp = new byte[4096];
            int nRead;
            try (InputStream is = process.getInputStream()) {
                while ((nRead = is.read(temp, 0, temp.length)) != -1) {
                    buffer.write(temp, 0, nRead);
                }
            }
            if (!waitForProcess(process, 5)) {
                Log.e(TAG, "Root read: su timed out (Magisk prompt not answered?)");
                return null;
            }
            if (process.exitValue() == 0 && buffer.size() > 0) {
                Log.d(TAG, "Root read success: " + buffer.size() + " bytes");
                return buffer.toByteArray();
            }
            Log.e(TAG, "Root read: su exit code " + process.exitValue());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Root read failed: " + e.getMessage());
            return null;
        }
    }

    private boolean saveViaRoot(byte[] data, String targetPath) {
        try {
            File tempFile = new File(getCacheDir(), "root_temp.sav");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
            }
            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c",
                "cp \"" + tempFile.getAbsolutePath() + "\" \"" + targetPath + "\" && chmod 644 \"" + targetPath + "\""});
            if (!waitForProcess(process, 5)) {
                Log.e(TAG, "Root save timed out");
                if (!tempFile.delete()) Log.w(TAG, "Failed to delete temp file");
                return false;
            }
            boolean success = process.exitValue() == 0;
            if (!tempFile.delete()) Log.w(TAG, "Failed to delete temp file");
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Root save failed: " + e.getMessage());
            return false;
        }
    }

    private boolean waitForProcess(Process process, int timeoutSec) {
        long deadline = System.currentTimeMillis() + timeoutSec * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                process.exitValue();
                return true;
            } catch (IllegalThreadStateException e) {
                try { Thread.sleep(200); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); return false; }
            }
        }
        process.destroyForcibly();
        return false;
    }

    private boolean saveViaShizukuPrivileged(byte[] data, String targetPath) {
        try {
            File tempFile = new File(getCacheDir(), "shizuku_temp.sav");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(data);
            }
            Process process = Shizuku.newProcess(new String[]{"sh", "-c",
                "cp \"" + tempFile.getAbsolutePath() + "\" \"" + targetPath + "\" && chmod 644 \"" + targetPath + "\""}, null, null);
            if (!waitForProcess(process, 10)) {
                Log.e(TAG, "Shizuku save timed out");
                if (!tempFile.delete()) Log.w(TAG, "Failed to delete temp file");
                return false;
            }
            int exitCode = process.exitValue();
            if (!tempFile.delete()) Log.w(TAG, "Failed to delete temp file");
            return exitCode == 0;
        } catch (Exception e) {
            Log.e(TAG, "Shizuku save failed: " + e.getMessage());
            return false;
        }
    }
    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                    .setOnDismissListener((DialogInterface dialog) -> result.confirm())
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                    .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                    .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            final EditText input = new EditText(view.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(defaultValue);
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm(input.getText().toString()))
                    .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                    .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                    .create()
                    .show();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
        }

        @Override
        public void onExceededDatabaseQuota(String url,
                                            String databaseIdentifier, long currentQuota,
                                            long estimatedSize, long totalUsedQuota,
                                            WebStorage.QuotaUpdater quotaUpdater) {
            quotaUpdater.updateQuota(5 * 1024 * 1024);
        }

    }

}
