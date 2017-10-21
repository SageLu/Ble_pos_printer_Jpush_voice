package com.sagelu.ble_pos_printer_jpush_voice.bleprint;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sagelu.ble_pos_printer_jpush_voice.BleStateListener;
import com.sagelu.ble_pos_printer_jpush_voice.R;
import com.sagelu.ble_pos_printer_jpush_voice.adapter.BleDevicesRecyclerViewAdapter;
import com.sagelu.ble_pos_printer_jpush_voice.receiver.BleStateReceiver;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.sagelu.ble_pos_printer_jpush_voice.utils.DividerItemDecoration.VERTICAL_LIST;


/**
 * 蓝牙打印连接activity
 */
public class ConnectBTPairedActivity extends AppCompatActivity {

    @BindView(R.id.btn_back)
    ImageView btnBack;
    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.tv_ok)
    TextView tvOk;
    @BindView(R.id.recyclerview_settingconnect)
    RecyclerView rvSettingconnect;
    private ProgressDialog dialog;
    public static final String ICON = "ICON";
    public static final String PRINTERNAME = "PRINTERNAME";
    public static final String PRINTERMAC = "PRINTERMAC";
    private static List<Map<String, Object>> boundedPrinters;

    private static Handler mHandler = null;
    private static String TAG = "ConnectBTMacActivity";

    private BleDevicesRecyclerViewAdapter adapter;


    private BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connectbtpaired2);
        ButterKnife.bind(this);
        tvTitle.setText("蓝牙连接");

        //得到BluetoothAdapter对象
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //注册蓝牙状态的广播--当时项目需要（可以忽略）
        registerBleReceiver();
        dialog = new ProgressDialog(this);
        //得到已经匹配的蓝牙的list集合
        boundedPrinters = getBoundedPrinters();
        //初始化已经匹配的蓝牙设备
        initRecyclerView();


        mHandler = new MHandler(this);
        WorkService.addHandler(mHandler);


    }


    BleStateReceiver bleStateReceiver;

    /**
     *  //注册蓝牙状态的广播--当时项目需要（可以忽略）
     */
    private void registerBleReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bleStateReceiver = new BleStateReceiver();
        registerReceiver(bleStateReceiver, intentFilter);
        bleStateReceiver.setmBleStateListener(new BleStateListener() {
            @Override
            public void bleClosed() {

            }

            @Override
            public void bleOpened() {

            }
        });
    }

    /**
     *  //反注册蓝牙状态的广播--当时项目需要（可以忽略）
     */
    private void unregisterBleReceiver() {
        if (bleStateReceiver != null) {
            unregisterReceiver(bleStateReceiver);
        }

    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        rvSettingconnect.setLayoutManager(new LinearLayoutManager(this));
        // 创建RecyclerView的数据适配器
        adapter = new BleDevicesRecyclerViewAdapter(this);
        //设置分割线
        rvSettingconnect.addItemDecoration(new DividerItemDecoration(this, VERTICAL_LIST));
        // 设置RecyclerView的数据适配器(适配器包装)
        rvSettingconnect.setAdapter(adapter);
        if (null != boundedPrinters) {
            adapter.setDatas(boundedPrinters);
        }
        //点击item连接蓝牙打印机
        adapter.setOnItemClickListener(new BleDevicesRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position, Map<String, Object> data) {

                String address = (String) boundedPrinters.get(position).get(PRINTERMAC);
                String name = (String) boundedPrinters.get(position).get(PRINTERNAME);
                dialog.setMessage(Global.toast_connecting + " " + name);
                dialog.setIndeterminate(true);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                if (WorkService.workThread != null) {
                    WorkService.workThread.connectBt(address);
                } else {
                    Toast.makeText(ConnectBTPairedActivity.this, "WorkService.workThread is null", Toast.LENGTH_SHORT).show();
                }


            }
        });
    }

    //打开蓝牙的对话框--当时项目的一些需求
    private void toDialog() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (null != adapter) {
            if (!adapter.isEnabled()) {
                toOpenAndCompareBLE("打开蓝牙");
            } else {
                if (getBoundedPrinters().size() > 0) {

                } else {
                    toOpenAndCompareBLE("匹配蓝牙打印机");
                }

            }
        }
    }

    /**
     * 链接蓝牙--当时项目的一些需求
     *
     * @param title
     */
    private void toOpenAndCompareBLE(String title) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                                adapter.enable();
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WorkService.delHandler(mHandler);
        mHandler = null;
        unregisterBleReceiver();
    }


    /**
     * 得到已经匹配的蓝牙的list集合
     * @return
     */
    private List<Map<String, Object>> getBoundedPrinters() {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            return list;
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a
                // ListView
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(ICON, android.R.drawable.stat_sys_data_bluetooth);
                // Toast.makeText(this,
                // ""+device.getBluetoothClass().getMajorDeviceClass(),
                // Toast.LENGTH_LONG).show();
                map.put(PRINTERNAME, device.getName());
                map.put(PRINTERMAC, device.getAddress());
                list.add(map);
            }
        }
        return list;
    }

    @OnClick({R.id.ll_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                finish();
                break;
        }
    }



    static class MHandler extends Handler {

        WeakReference<ConnectBTPairedActivity> mActivity;

        MHandler(ConnectBTPairedActivity activity) {
            mActivity = new WeakReference<ConnectBTPairedActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ConnectBTPairedActivity theActivity = mActivity.get();
            switch (msg.what) {
                /**
                 * DrawerService 的 onStartCommand会发送这个消息
                 */

                case Global.MSG_WORKTHREAD_SEND_CONNECTBTRESULT: {
                    int result = msg.arg1;
                    Toast.makeText(
                            theActivity,
                            (result == 1) ? Global.toast_success
                                    : Global.toast_fail, Toast.LENGTH_SHORT).show();
                    theActivity.dialog.cancel();
                    if (result == 1) {
                        theActivity.finish();
                    }

                    break;
                }

            }
        }
    }

}
