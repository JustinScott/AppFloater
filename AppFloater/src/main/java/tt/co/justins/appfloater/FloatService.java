package tt.co.justins.appfloater;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Justin on 10/31/13.
 */
public class FloatService extends Service {

    List<ImageView> viewList = new ArrayList<ImageView>();
    WindowManager windowManager;
    PackageManager packageManager;

    Binder binder = new Floatbinder();

    class IconHolder {
        public ImageView view;
        public Drawable defaultIcon;
        public Drawable statusIcon;
        public int statusId;

        IconHolder(ImageView view, Drawable baseIcon) {
            this.view = view;
            this.defaultIcon = baseIcon;
            this.statusIcon = baseIcon;
            this.statusId = 0;
        }
    }

    public class Floatbinder extends Binder {
        FloatService getService() {
            return FloatService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        packageManager = getPackageManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(viewList.size() >= 5) {
            return START_NOT_STICKY;
        }

        final ImageView iconView = new ImageView(this);
        viewList.add(iconView);

        final Bundle bundle = intent.getExtras();

        //Bitmap bmp = decodeBundleImage(bundle);
        //iconView.setImageBitmap(bmp);

        Drawable draw = getIcon(bundle);
        iconView.setImageDrawable(draw);

        IconHolder iconHolder = new IconHolder(iconView, draw);
        iconView.setTag(iconHolder);

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        setWindowParams(params);
        windowManager.addView(iconView, params);

        ViewConfiguration vc = ViewConfiguration.get(iconView.getContext());
        final int mScaledTouchSlop = vc.getScaledTouchSlop();
        final int mLongPressTimeOut = vc.getLongPressTimeout();
        final int mTapTimeOut = vc.getTapTimeout();

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
                        Log.d("AppFloat", "Action Down");
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        Log.d("AppFloat", "Action Move");
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(iconView, paramsF);
                        return false;
                    case MotionEvent.ACTION_UP:
                        Log.d("AppFloat", "Action Up");
                        Log.d("AppFloat", "DistanceX: " + Math.abs(initialTouchX - event.getRawX()));
                        Log.d("AppFloat", "DistanceY: " + Math.abs(initialTouchY - event.getRawY()));
                        Log.d("AppFloat", "elapsed gesture time: " + (event.getEventTime() - event.getDownTime()));
                        if((Math.abs(initialTouchX - event.getRawX()) <= mScaledTouchSlop) && (Math.abs(initialTouchY - event.getRawY()) <= mScaledTouchSlop)) {
                            if((event.getEventTime() - event.getDownTime()) < mTapTimeOut ) {
                                Log.d("AppFloat", "Click Detected");
                                startApp(bundle);
                            } else if((event.getEventTime() - event.getDownTime()) >= mLongPressTimeOut) {
                                Log.d("AppFloat", "Long Click Detected");
                            }
                        }
                    default:
                        Log.d("AppFloat", "Action Default");
                        break;
                }
                return false;
            }

        });
        return START_NOT_STICKY;
    }

    private void startApp(Bundle bundle) {
        Intent intent = packageManager.getLaunchIntentForPackage(bundle.getString("appPackage"));
            if(intent != null) {
                    startActivity(intent);
            }
    }

    private void setWindowParams(WindowManager.LayoutParams params) {
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.y = dpToPx(100) * viewList.size();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
    }

    private Drawable getIcon(Bundle bundle) {
        String appPackage = bundle.getString("appPackage");
        int appResId = bundle.getInt("appResId");
        Drawable icon = null;

        if(appResId == 0) {
            Log.v("AppFloat", "Resource ID not found, attempting to load package icon");
            try {
                icon = getPackageIcon(bundle, appPackage);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.v("AppFloat", "Loading image using Resource ID");
            icon = getPackageImage(bundle, appPackage, appResId);
        }

        if(icon == null) {
            Log.v("AppFloat", "Using default image for icon");
            icon = getResources().getDrawable(R.drawable.ic_launcher);
        }

        return icon;
    }

    private Drawable getPackageImage(Bundle bundle, String appPackage, int appResId) {
        return packageManager.getDrawable(appPackage, appResId, null);
    }

    private Drawable getPackageIcon(Bundle bundle, String appPackage) throws PackageManager.NameNotFoundException {
        Drawable draw = null;
        try {
            draw = packageManager.getApplicationIcon(appPackage);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return draw;
    }

    public void updateIconStatus(int statusId) {
        updateIconStatus(statusId, 0);
    }

    public void updateIconStatus(int statusId, int iconArrayIndex) {
        Drawable statusIcon;
        IconHolder holder = (IconHolder) viewList.get(iconArrayIndex).getTag();

        Log.d("AppFloat", "updateIcon position: " + statusId);
        switch(statusId) {
            case 1:
                statusIcon = getResources().getDrawable(R.drawable.one);
                break;
            case 2:
                statusIcon = getResources().getDrawable(R.drawable.two);
                break;
            case 3:
                statusIcon = getResources().getDrawable(R.drawable.three);
                break;
            case 4:
                statusIcon =  getResources().getDrawable(R.drawable.four);
                break;
            case 5:
                statusIcon = getResources().getDrawable(R.drawable.five);
                break;
            case 6:
                statusIcon = getResources().getDrawable(R.drawable.six);
                break;
            case 7:
                statusIcon = getResources().getDrawable(R.drawable.seven);
                break;
            case 8:
                statusIcon = getResources().getDrawable(R.drawable.eight);
                break;
            case 9:
                statusIcon = getResources().getDrawable(R.drawable.nine);
                break;
            default:
                statusIcon = getResources().getDrawable(R.drawable.nineplus);
        }

        holder.statusId = statusId;
        holder.statusIcon = mergeBitmap(holder.defaultIcon, statusIcon);
        holder.view.setImageDrawable(holder.statusIcon);
    }

    private Drawable mergeBitmap(Drawable base, Drawable status) {
        Bitmap bitmap = Bitmap.createBitmap(base.getIntrinsicWidth(), base.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);

        int top = base.getIntrinsicHeight() - status.getIntrinsicHeight();

        base.setBounds(0, 0, base.getIntrinsicWidth(), base.getIntrinsicHeight());
        status.setBounds(0, 70, 30, 100);

        base.draw(c);
        status.draw(c);

        Drawable draw = new BitmapDrawable(getResources(), bitmap);
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
