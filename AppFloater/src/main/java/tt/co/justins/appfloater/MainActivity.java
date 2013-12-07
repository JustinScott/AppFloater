package tt.co.justins.appfloater;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends ActionBarActivity {
    FloatService mService;
    boolean mbound = false;

    SharedPreferences mPreferences;
    Set<String> packageNameSet;
    List<String> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, FloatService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        Button stopButton = (Button) findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mbound) {
                    Log.d("AppFloater", "Calling remove icons on service");
                    mService.removeIconsFromScreen();
                } else {
                    Log.d("AppFloater", "Service isn't bound, can't call remove icons");
                }
            }
        });

        appList = getAppList();
        packageNameSet = new HashSet<String>();

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> rAppList = am.getRunningAppProcesses();

        //ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, rAppList);
        myArrayAdapter adapter = new myArrayAdapter(this, R.layout.list_row, R.id.rowText, appList);

        ListView listview = (ListView) findViewById(R.id.listView);
        listview.setAdapter(adapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                floatApp(appList.get(position));
                packageNameSet.add(appList.get(position));
            }
        });

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    @Override
    protected void onStop() {
        super.onStop();

        boolean savedApps = false;
        savedApps = mPreferences.getBoolean("pref_save", savedApps);

        if(mbound) {
            if (savedApps) {
                Log.d("AppFloater", "Clearing pref list");
                mService.clearPrefPackageList();
                Log.d("AppFloater", "Saving icons to prefs");
                mService.saveIconsToPref();
            }
        } else {
            Log.d("AppFloater", "onStop called, but service isn't bound");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("AppFloater", "Activity onDestroy called");
        if(mbound) {
            Log.d("AppFloater", "Unbinding from service");
            unbindService(mConnection);
            mbound = false;
        }
    }

    private ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("AppFloater", "Connected to service");
            mService = ((FloatService.FloatBinder) service).getService();
            mbound = true;

            boolean savedApps = false;
            savedApps = mPreferences.getBoolean("pref_save", savedApps);

            if (savedApps) {
                Log.d("AppFloater", "Floating saved preference apps");
                mService.floatSavedApps();
            } else {
                Log.d("AppFloater", "Not floating saved preference apps");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("AppFloat", "Disconnected from service");
            mbound = false;
        }
    };

    private void floatApp(String packageName) {
        floatApp(packageName, 0);
    }

    private void floatApp(String packageName, int resourceId) {
        if(mbound) {
            mService.floatApp(packageName, resourceId);
        }
    }

    private List<String> getAppList() {
        PackageManager pm = getPackageManager();
        // List<PackageInfo> appList = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
        List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List list = new ArrayList();

        for(ApplicationInfo item : appList) {
            if((item.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1 || (item.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                //list.add(pm.getApplicationLabel(item));
                list.add(item.packageName);
        }

        return list;
    }

    private byte[] encodeResourceToByteArray () {
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        final byte[] byteArray = stream.toByteArray();
        return byteArray;
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
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.about:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
