package tt.co.justins.appfloater;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button stopButton = (Button) findViewById(R.id.stop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(MainActivity.this, Float.class));
            }
        });

        final List<String> appList = getAppList();

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
            }
        });
    }

    private void floatApp(String packageName) {
        floatApp(packageName, 0);
    }

    private void floatApp(String packageName, int resourceId) {
        Intent intent = new Intent(MainActivity.this, Float.class);
        if(resourceId != 0)
            intent.putExtra("appResId", resourceId);
        intent.putExtra("appPackage", packageName);
        startService(intent);
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
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
