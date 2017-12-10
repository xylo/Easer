package ryey.easer.plugins.operation.ringer_mode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class InterruptionFilterSwitcherService extends NotificationListenerService {
    private static final String ACTION_CHANGE = "ryey.easer.plugins.operation.ringer_mode.action.CHANGE";
    private static final String EXTRA_MODE = "ryey.easer.plugins.operation.ringer_mode.extra.MODE";

    private static boolean is_running = false;

    private ConditionVariable cv = new ConditionVariable();

    private final IntentFilter mFilter = new IntentFilter(ACTION_CHANGE);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null || !action.equals(ACTION_CHANGE))
                throw new IllegalAccessError();
            int mode = intent.getIntExtra(EXTRA_MODE, -1);
            cv.block();
            requestInterruptionFilter(mode);
        }
    };

    static void setSilentMode(Context context) {
        setInterruptionFilter(context, INTERRUPTION_FILTER_NONE);
    }

    static void setInterruptionFilter(Context context, int mode) {
        Intent intent = new Intent(ACTION_CHANGE);
        intent.putExtra(EXTRA_MODE, mode);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    static boolean isRunning() {
        return is_running;
    }

    @Override
    public void onListenerConnected() {
        cv.open();
    }

    @Override
    public IBinder onBind(Intent intent) {
        is_running = true;
        return super.onBind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        is_running = false;
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mReceiver, mFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mReceiver);
    }
}
