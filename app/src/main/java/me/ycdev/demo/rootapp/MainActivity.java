package me.ycdev.demo.rootapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import eu.chainfire.libsuperuser.Debug;
import eu.chainfire.libsuperuser.Shell;
import me.ycdev.demo.rootapp.utils.IoUtils;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String APK_PACKAGE_NAME = "com.example.android.supportv4";
    private static final String ASSETS_APK_FILENAME = "Support4Demos-debug-r19.1.0.apk";

    private static final int MSG_APK_COPY_SUCCESS = 100;
    private static final int MSG_APK_COPY_FAILED = 101;

    private Button mRootShellInstallBtn;
    private Button mRootShellUninstallBtn;
    private Button mAppProcessRebootBtn;

    private BroadcastReceiver mPackageChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            String pkgName = URI.create(intent.getDataString()).getSchemeSpecificPart();
            if (!replacing && action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                String msg = context.getString(R.string.tips_app_added, pkgName);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            } else if (!replacing && action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
                String msg = context.getString(R.string.tips_app_removed, pkgName);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_APK_COPY_SUCCESS: {
                    enableRootShellButtons(true);
                    break;
                }
                case MSG_APK_COPY_FAILED: {
                    Toast.makeText(MainActivity.this, R.string.tips_copy_apk_failed, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Debug.setDebug(true);

        initViews();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mPackageChangeReceiver, filter);

        enableRootShellButtons(false);
        extractFilesFromAssets();
    }

    private void initViews() {
        mRootShellInstallBtn = (Button) findViewById(R.id.root_shell_install_app);
        mRootShellInstallBtn.setOnClickListener(this);
        mRootShellUninstallBtn = (Button) findViewById(R.id.root_shell_uninstall_app);
        mRootShellUninstallBtn.setOnClickListener(this);
        mAppProcessRebootBtn = (Button) findViewById(R.id.app_process_reboot);
        mAppProcessRebootBtn.setOnClickListener(this);
    }

    private void enableRootShellButtons(boolean enable) {
        mRootShellInstallBtn.setEnabled(enable);
        mRootShellUninstallBtn.setEnabled(enable);
    }

    private void extractFilesFromAssets() {
        final Context appContext = getApplicationContext();
        new Thread() {
            @Override
            public void run() {
                // copy APK
                InputStream is = null;
                OutputStream os = null;
                boolean success = false;
                try {
                    is = appContext.getAssets().open(ASSETS_APK_FILENAME);
                    os = appContext.openFileOutput(ASSETS_APK_FILENAME, Context.MODE_WORLD_READABLE);
                    IoUtils.copyStream(is, os);
                    success = true;
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    IoUtils.closeQuietly(is);
                    IoUtils.closeQuietly(os);
                }
                if (success) {
                    mHandler.obtainMessage(MSG_APK_COPY_SUCCESS).sendToTarget();
                } else {
                    mHandler.obtainMessage(MSG_APK_COPY_FAILED).sendToTarget();
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mPackageChangeReceiver);
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        if (view == mRootShellInstallBtn) {
            installApkByRootShell();
        } else if (view == mRootShellUninstallBtn) {
            uninstallApkByRootShell();
        } else if (view == mAppProcessRebootBtn) {
            rebootByAppProcess();
        }
    }

    private void installApkByRootShell() {
        final File apkFile = getFileStreamPath(ASSETS_APK_FILENAME);
        new MyTask(getString(R.string.tips_app_installing, apkFile.getAbsoluteFile()), new Runnable() {
            @Override
            public void run() {
                String[] cmds = new String[] {
                        "pm install -r \"" + apkFile.getAbsolutePath() + "\""
                };
                Shell.SU.run(cmds);
            }
        }).execute();
    }

    private void uninstallApkByRootShell() {
        new MyTask(getString(R.string.tips_app_uninstalling, APK_PACKAGE_NAME), new Runnable() {
            @Override
            public void run() {
                String[] cmds = new String[] {
                        "pm uninstall \"" + APK_PACKAGE_NAME + "\""
                };
                Shell.SU.run(cmds);
            }
        }).execute();
    }

    private void rebootByAppProcess() {
        final Context appContext = getApplicationContext();
        new MyTask(getString(R.string.tips_rebooting), new Runnable() {
            @Override
            public void run() {
                String destFile = appContext.getPackageCodePath();
                String[] cmds = new String[] {
                        "export CLASSPATH=" + destFile,
                        "/system/bin/app_process /system/bin me.ycdev.demo.rootapp.jar.TaskExecutor reboot"
                };
                Shell.SU.run(cmds);
            }
        }).execute();
    }

    private class MyTask extends AsyncTask<Void, Void, Void> {
        private String mTips;
        private Runnable mTargetTask;
        private ProgressDialog mDialog;

        public MyTask(String tips, Runnable task) {
            mTips = tips;
            mTargetTask = task;
        }

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage(mTips);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mTargetTask.run();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mDialog.dismiss();
        }
    }
}
