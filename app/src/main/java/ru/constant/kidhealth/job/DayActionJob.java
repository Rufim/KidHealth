package ru.constant.kidhealth.job;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.constant.kidhealth.App;
import ru.constant.kidhealth.R;
import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.domain.models.DayAction;
import ru.constant.kidhealth.service.DatabaseService;
import ru.kazantsev.template.util.TextUtils;

;

/**
 * Created by 0shad on 01.03.2016.
 */
public class DayActionJob extends Job {

    public static final String DAY_ACTION_ID = "action_id";
    public static final String DAY_ACTION_TITLE = "title";
    public static final String DAY_ACTION_MESSAGE = "message";
    private static NotificationChannel mChannel_dayActions = null;

    @Inject
    DatabaseService databaseService;

    private static final String TAG = DayActionJob.class.getSimpleName();
    public static int jobId = -1;

    public DayActionJob() {
        App.getAppComponent().inject(this);
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context context = getContext();
        if (isCanceled() || context == null) {
            return Result.SUCCESS;
        }
        try {
            String id = params.getExtras().getString(DAY_ACTION_ID, "");
            DayAction next = null;
            if(TextUtils.notEmpty(id)) {
                DayAction action = databaseService.getDayAction(id);
                if(action != null && action.isValid()) {
                    if(!action.getNotified()) {
                        sendActionNotification(context, action);
                        databaseService.notifyDayAction(action);
                    }
                    next = databaseService.nextDayAction(DateTime.now());
                } else {
                    Log.e(TAG, "action not valid or null" + action);
                }
            }
            if(next == null) {
                next = databaseService.nextDayAction(DateTime.now());
            }
            startSchedule(next);
    } catch (Exception e) {
            Log.e(TAG, "Unknown exception", e);
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    public static void stop() {
        if (jobId > 0) {
            JobManager.instance().cancel(jobId);
        }
    }

    public static void startSchedule(DayAction dayAction) {
        if(dayAction != null && dayAction.isValid() && !dayAction.getNotified()) {
            AppJobCreator.cancelJobs(JobType.FIRE_DAY_ACTION);
            PersistableBundleCompat bundleCompat = new PersistableBundleCompat();
            bundleCompat.putString(DAY_ACTION_ID, dayAction.getId());
            jobId = AppJobCreator.request(JobType.FIRE_DAY_ACTION)
                    .setExact(dayAction.getStart().getMillis())
                    .setExtras(bundleCompat)
                    .build()
                    .schedule();
        }  else {
            Log.e(TAG, "action not valid or null " + dayAction);
        }
    }

    public static void sendActionNotification(Context context, DayAction action) {
        Map<String, String> dayActionMap = new HashMap<>();
        dayActionMap.put(DAY_ACTION_ID, action.getId());
        dayActionMap.put(DAY_ACTION_TITLE, action.getTitle());
        dayActionMap.put(DAY_ACTION_MESSAGE, action.getComment());
        sendActionNotification(context, dayActionMap);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     */
    public static void sendActionNotification(Context context, Map<String, String> data) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(DAY_ACTION_ID, data.get(DAY_ACTION_ID));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(data.get(DAY_ACTION_TITLE))
                .setContentText(data.get(DAY_ACTION_MESSAGE))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        notificationBuilder.setSmallIcon(R.drawable.running_rabbit_96);
        notificationBuilder.setColor(0x2ECC71);
        //notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.running_rabbit_96));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(getNotificationChannel(context).getId());
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(data.hashCode(), notificationBuilder.build());
    }

    public static NotificationChannel getNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mChannel_dayActions == null) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String CHANNEL_ID = "kidhealth_channel_01";
                int importance = NotificationManager.IMPORTANCE_LOW;
                mChannel_dayActions = new NotificationChannel(CHANNEL_ID, "Kidhealth channel", importance);
                mNotificationManager.createNotificationChannel(mChannel_dayActions);
            }
            return mChannel_dayActions;
        } else {
            return null;
        }
    }

}
