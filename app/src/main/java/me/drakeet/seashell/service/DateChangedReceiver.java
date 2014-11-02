package me.drakeet.seashell.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import me.drakeet.seashell.utils.MySharedpreference;
import me.drakeet.seashell.utils.ToastUtils;

/**
 * 准备用这个类来代替{@link me.drakeet.seashell.service.NotificatService} 中的某些功能，以求更好的性能和内存占有率
 * 准备启用新API
 * Created by drakeet on 10/22/14.
 */
public class DateChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MySharedpreference mySharedpreference = new MySharedpreference(context);
        int id1 = mySharedpreference.getCurrentWordId();
        mySharedpreference.updateWordId();
        int id2 = mySharedpreference.getCurrentWordId();
        ToastUtils.showShort("" + id1 + "-" + id2);
        // Intent serviceIntent = new Intent(context, NotificatService.class);
        // context.startService(serviceIntent);
        // 想到一种方式，在这边修改当前使用的 word id号，并标志『需要更新』，
        // 而 Service 那边不时检查一下是否『需要更新』，如果需要，根据 id 号去更新
    }
}
