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

import org.joda.time.DateTime;
import org.joda.time.Duration;

import ru.constant.kidhealth.R;
import ru.constant.kidhealth.activity.MainActivity;
import ru.constant.kidhealth.utils.AppUtils;

;

/**
 * Created by 0shad on 01.03.2016.
 */
public class ParentReminderJob extends Job {

    private static NotificationChannel mChannel_dayActions = null;

    private static final String TAG = ParentReminderJob.class.getSimpleName();
    public static int jobId = -1;
    public final static int NOTIFICATION_REMINDER_ID = 1100;
    public final static String NOTIFICATION_ACTION_NAME = "reminder";

    public ParentReminderJob() {
    }

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        Context context = getContext();
        if (isCanceled() || context == null) {
            return Result.SUCCESS;
        }
        try {
            if(!AppUtils.isLoggedOnce()) {
                sendActionNotification(context);
                startSchedule();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unknown exception", e);
            return Result.FAILURE;
        }
        return Result.SUCCESS;
    }

    public static void startSchedule() {
        if(!AppUtils.isLoggedOnce()) {
            AppJobCreator.cancelJobs(JobType.PARENT_REMINDER);
            AppJobCreator.cancelJobRequests(JobType.PARENT_REMINDER);
            jobId = AppJobCreator.request(JobType.PARENT_REMINDER)
                    .setExact(new Duration(DateTime.now(), DateTime.now().plusDays(1).withTime(20,0,0, 0)).getMillis())
                    .setUpdateCurrent(true)
                    .build()
                    .schedule();
        }
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     */
    public static void sendActionNotification(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setAction(NOTIFICATION_ACTION_NAME);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.reminder_title))
                .setContentText(context.getString(R.string.reminder_text))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        notificationBuilder.setSmallIcon(R.drawable.ic_reminder);
        notificationBuilder.setColor(context.getResources().getColor(R.color.md_yellow_400));
        //notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.running_rabbit_96));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(getNotificationChannel(context).getId());
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        try {
            notificationManager.cancel(NOTIFICATION_REMINDER_ID);
        } catch (Throwable e) {
        }
        notificationManager.notify(NOTIFICATION_REMINDER_ID, notificationBuilder.build());
    }

    public static NotificationChannel getNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mChannel_dayActions == null) {
                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String CHANNEL_ID = "kidhealth_channel_reminder";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                mChannel_dayActions = new NotificationChannel(CHANNEL_ID, "Kidhealth channel reminder", importance);
                mNotificationManager.createNotificationChannel(mChannel_dayActions);
            }
            return mChannel_dayActions;
        } else {
            return null;
        }
    }

}
