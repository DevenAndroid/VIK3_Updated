package com.vik3.ui.activities;

import static com.vik3.utils.CommonMethods.showAlert;
import static com.vik3.utils.Constants.AUDIO_STREAMING_URL;

import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.play.core.internal.ca;
import com.judemanutd.autostarter.AutoStartPermissionHelper;
import com.vik3.BroadcastReceiverBluetooth;
import com.vik3.R;
import com.vik3.apiClient.RetrofitClient;
import com.vik3.databinding.ActivitySplashScreenBinding;
import com.vik3.prefraceMaager.PreferenceManagerRadioFragment;
import com.vik3.ui.dashboard.RadioFragmentNew;
import com.vik3.ui.home.HomeFragment;
import com.vik3.ui.models.Audio;
import com.vik3.ui.models.ModelStations;
import com.vik3.ui.notifications.MoreFragment;
import com.vik3.utils.StorageUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    ActivitySplashScreenBinding binding;

    public static boolean isFirst = false;
    public static boolean isFirstClicked = false;
    boolean connected = false;

    @RequiresApi(api = VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.colorStatusBar));
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(binding.getRoot());
        Objects.requireNonNull(getSupportActionBar()).hide();

//        AutoStartPermissionHelper.Companion.getInstance().isAutoStartPermissionAvailable(this, true);




//        PackageManager p = getPackageManager();
//        ComponentName componentName = new ComponentName(this, com.vik3.ui.activities.SplashScreen.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
//        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

//        BroadcastReceiverBluetooth broadcastReceiverBluetooth = new BroadcastReceiverBluetooth();


        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
//        wl.acquire();
//        if(wl.isHeld()){
//            wl.release();
//        }
//        AutoStartPermissionHelper.Companion.getInstance().getAutoStartPermission(this, true, true);
/*
        //Setting the proper lockMode depending on the android version:
        int wifiLockMode = WifiManager.WIFI_MODE_FULL;

        int sdkVersion = Build.VERSION.SDK_INT;

//WIFI_MODE_FULL_HIGH_PERF was added on Android 3.1 so
//I need to implement this to make sure the wifi will execute on its full power(even if it consumes more battery)
        if (sdkVersion >= VERSION_CODES.HONEYCOMB_MR1) {
            wifiLockMode = WifiManager.WIFI_MODE_FULL_HIGH_PERF;
        }
        AutoStartPermissionHelper.Companion.getInstance().getAutoStartPermission(this, true, true);

        AutoStartPermissionHelper.Companion.getInstance().isAutoStartPermissionAvailable(this, true);

//Setting the WifiLock
        WifiManager wm = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock mWiFiLock = wm.createWifiLock(wifiLockMode, "MyFlag");
        mWiFiLock.acquire();

//Releasing the WifiLock
        if (mWiFiLock.isHeld()) {
            mWiFiLock.release();
        }*/


        binding.videoView.setOnPreparedListener(mp -> {
            float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
            float screenRatio = binding.videoView.getWidth() / (float)
                    binding.videoView.getHeight();
            float scaleX = videoRatio / screenRatio;
            if (scaleX >= 1f) {
                binding.videoView.setScaleX(scaleX);
            } else {
                binding.videoView.setScaleY(1f / scaleX);
            }
        });

        if (getSharedPreferences("THEME_MODE", MODE_PRIVATE).getBoolean("isFrench", false)) {
            Locale myLocale = new Locale("fr");
            Resources res = getResources();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, res.getDisplayMetrics());
        } else {
            Locale myLocale = new Locale("en");
            Resources res = getResources();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, res.getDisplayMetrics());
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        } else
            connected = false;
        getVideo();
        getStations();
    }

    private static final Intent[] POWERMANAGER_INTENTS = {
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity")),
            new Intent().setComponent(new ComponentName("com.transsion.phonemanager", "com.itel.autobootmanager.activity.AutoBootMgrActivity"))
    };

    private void getVideo() {
        String uri = "android.resource://" + getPackageName() + "/" + R.raw.intro_video;
        binding.videoView.setVideoURI(Uri.parse(uri));
        binding.videoView.requestFocus();

        if (getIntent() != null && getIntent().getBooleanExtra("skipPlayer", false)){
            boolean skipPlayer = getIntent().getBooleanExtra("skipPlayer", false);
            if (skipPlayer) {
                if (connected) {
                    binding.container.setVisibility(View.VISIBLE);
                } else {
                    showAlert(this, "Oops. No Internet connection. Please connect either Wifi or Mobile internet connection for continue the VIK3");
                }
            }
        } else {
            binding.videoView.start();
        }
        addBottomNavigationMenu();

        binding.videoView.setOnCompletionListener(mp -> {
            binding.container.setVisibility(View.VISIBLE);
            if (connected) {
                binding.container.setVisibility(View.VISIBLE);
            } else {
                showAlert(this, "Oops. No Internet connection. Please connect either Wifi or Mobile internet connection for continue the VIK3");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm1 = (PowerManager) getSystemService(POWER_SERVICE);
                if (!pm1.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                }
            }
            try {
                PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
//                PowerManager.WakeLock wakeLock = (PowerManager.WakeLock) powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp-cast-server-cpu1");
//                wakeLock.acquire();
            } catch (Exception e) {
            }


            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiManager.WifiLock wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "myapp-cast-server-net");
            wifiLock.acquire();

            for (Intent intent : POWERMANAGER_INTENTS)
                if (getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    // show dialog to ask user action
                    startActivity(intent);
                    break;
                }
            try {
                final Intent intent = new Intent();
                String manufacturer = Build.MANUFACTURER;
                if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
                } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
                    //intent.setComponent(new ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"));
                } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
                } else if ("huawei".equalsIgnoreCase(manufacturer)) {
                    intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
                } else {
                    // applySubmit(false);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    private void getStations() {
        Call<ModelStations> call = RetrofitClient
                .getInstance()
                .getApiService()
                .getArtist();
        call.enqueue(new Callback<ModelStations>() {
            @Override
            public void onResponse(@NotNull Call<ModelStations> call, @NotNull Response<ModelStations> response) {
                setServerStationsData(response);
            }

            @Override
            public void onFailure(@NotNull Call<ModelStations> call, @NotNull Throwable t) {
                Toast.makeText(SplashScreen.this, "Failed : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setServerStationsData(@NonNull Response<ModelStations> response) {
        try {
            new PreferenceManagerRadioFragment(this).setPrefData(response.body());
            addAudioList(response);
            addBottomNavigationMenu();
        } catch (Exception e) {
            Toast.makeText(SplashScreen.this, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addAudioList(@NonNull Response<ModelStations> response) {
        ArrayList<Audio> audioList = new ArrayList<>();
        audioList.add(new Audio(AUDIO_STREAMING_URL, Objects.requireNonNull(response.body()).getCurrentTrack().getTitle().split("- ")[1], response.body().getCurrentTrack().getTitle().split("- ")[0], response.body().getCurrentTrack().getTitle().split("- ")[0]));
        StorageUtil storage = new StorageUtil(SplashScreen.this);
        storage.storeAudio(audioList);
        storage.storeAudioIndex(0);
    }

    private void addBottomNavigationMenu() {
        BottomNavigationView navView = findViewById(R.id.nav_view);
        getFragment(new RadioFragmentNew());
        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_radio) {
//                if (navView.getSelectedItemId() != R.id.navigation_radio) {
//                }
                getFragment(new RadioFragmentNew());
            } else if (item.getItemId() == R.id.navigation_home) {
                getFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.navigation_more) {
                getFragment(new MoreFragment());
            }
            return true;
        });

        if (getIntent() != null && getIntent().getStringExtra("menu") != null && getIntent().getStringExtra("menu").equals("more")) {
            navView.setSelectedItemId(R.id.navigation_more);
        }
    }

    public void getFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.nav_host_fragment_activity_main, fragment)
                .commit();
    }
}