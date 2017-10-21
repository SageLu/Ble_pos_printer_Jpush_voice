package com.sagelu.ble_pos_printer_jpush_voice.receiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sagelu.ble_pos_printer_jpush_voice.BleStateListener;


/**
 * Created by Administrator on 2017/6/29.
 * 蓝牙状态的广播
 */

public class BleStateReceiver extends BroadcastReceiver {

    private BleStateListener mBleStateListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d("aaa", "STATE_OFF 手机蓝牙关闭");
//                    if(mBleStateListener!=null) {
//                        mBleStateListener.bleClosed();
//                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d("aaa", "STATE_TURNING_OFF 手机蓝牙正在关闭");
                    if(mBleStateListener!=null) {
                        mBleStateListener.bleClosed();
                    }
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d("aaa", "STATE_ON 手机蓝牙开启");
                    if(mBleStateListener!=null) {
                        mBleStateListener.bleOpened();
                    }
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d("aaa", "STATE_TURNING_ON 手机蓝牙正在开启");
                    break;
            }
        }
    }



    public void setmBleStateListener(BleStateListener mBleStateListener) {
        this.mBleStateListener = mBleStateListener;
    }
}
