package tt.co.justins.appfloater;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin on 10/31/13.
 */
public class Float extends Service {

    List<ImageView> viewList = new ArrayList<ImageView>();
    WindowManager windowManager;
    PackageManager packageManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        packageManager = getPackageManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(viewList.size() >= 5)
            return START_NOT_STICKY;

        final ImageView iconView = new ImageView(this);
        viewList.add(iconView);

        final Bundle bundle = intent.getExtras();

        //Bitmap bmp = decodeBundleImage(bundle);
        //iconView.setImageBitmap(bmp);

        Drawable draw = getPackageImage(bundle);
        iconView.setImageDrawable(draw);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        addViewToScreen(params, iconView);

        iconView.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams paramsF = params;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(iconView, paramsF);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = packageManager.getLaunchIntentForPackage(bundle.getString("appPackage"));
                if(intent != null)
                    startActivity(intent);
            }
        });

        return START_NOT_STICKY;
    }

    private void addViewToScreen(WindowManager.LayoutParams params, ImageView iconView) {
//      final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//      WindowManager.LayoutParams.WRAP_CONTENT,
//      WindowManager.LayoutParams.WRAP_CONTENT,
//      WindowManager.LayoutParams.TYPE_PHONE,
//      WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//      PixelFormat.TRANSLUCENT);

        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.y = dpToPx(100) * viewList.size();

        windowManager.addView(iconView, params);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }
    private Drawable getPackageImage(Bundle bundle) {
        String appPackage = bundle.getString("appPackage");
        Integer appResId = bundle.getInt("appResId");
        Drawable draw = packageManager.getDrawable(appPackage, appResId, null);
        return draw;
    }

    private Bitmap decodeBundleImage(Bundle bundle) {
        byte[] byteArray = bundle.getByteArray("appIcon");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bmp;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        for(ImageView view : viewList ) {
            windowManager.removeView(view);
        }
    }
}
