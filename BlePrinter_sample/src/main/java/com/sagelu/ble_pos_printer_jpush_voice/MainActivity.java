package com.sagelu.ble_pos_printer_jpush_voice;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.sagelu.ble_pos_printer_jpush_voice.bleprint.ConnectBTPairedActivity;
import com.sagelu.ble_pos_printer_jpush_voice.bleprint.Global;
import com.sagelu.ble_pos_printer_jpush_voice.bleprint.WorkService;
import com.sagelu.ble_pos_printer_jpush_voice.utils.DataUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static Handler mHandler = null;
    private Intent intent;
    private Button btn_printer_text;
    private Button btn_connect_printer;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        //初始化蓝牙
        initBlePrint();
        btn_connect_printer = (Button) findViewById(R.id.btn_connect_printer);
        btn_printer_text = (Button) findViewById(R.id.btn_printer_text);
        btn_printer_text.setOnClickListener(this);
        btn_connect_printer.setOnClickListener(this);
    }

    private void initBlePrint() {
        // 初始化字符串资源
        InitGlobalString();
        mHandler = new MHandler(MainActivity.this);
        WorkService.addHandler(mHandler);
        if (null == WorkService.workThread) {
            intent = new Intent(MainActivity.this, WorkService.class);
            startService(intent);
        }

    }

    private void InitGlobalString() {
        Global.toast_success = getString(R.string.toast_success);
        Global.toast_fail = getString(R.string.toast_fail);
        Global.toast_notconnect = getString(R.string.toast_notconnect);
        Global.toast_usbpermit = getString(R.string.toast_usbpermit);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_connect_printer:
                Intent intent = new Intent(this, ConnectBTPairedActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_printer_text:
                //打印文字
                toDialogActivity();
                break;
        }


    }

    /**
     * 打印dialog
     */
    Dialog dialog;

    private void toDialogActivity() {
        View view = LayoutInflater.from(context).inflate(R.layout.print_dialog, null);
        // 对话框
        dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        // 设置宽度为屏幕的宽度
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.width = (int) (display.getWidth()); // 设置宽度
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setContentView(view);

        Button btn_cancle_schexiao = (Button) view.findViewById(R.id.btn_cancle_schexiao);
        Button btn_ok_schexiao = (Button) view.findViewById(R.id.btn_ok_schexiao);
        btn_cancle_schexiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //取消打印
                dialog.hide();

            }
        });
        btn_ok_schexiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.hide();
                printText();

            }
        });

    }

    String payMethod = "对账单";

    public void printText() {
        // 不要直接和Pos打交道，要通过workThread来交流

        if (WorkService.workThread.isConnected()) {
            String text = "null";
            text = "=============================" + "\n"
                    + "交易金额:" + "12.5元" + "\n"
                    + "支付状态:" + "给我退款" + "\n"
                    + "消费用户:" + "Sage" + "\n"
                    + "支付时间:" + "2017-06-06" + "\n"
                    + "收 银 员:" + "RNG" + "\n"
                    + "订单编号:" + "1231521591596" + "\n"
                    + "退款金额:" + "50W" + "\n"
                    + "退款时间:" + "2012-06-23" + "\n"
                    + "-----------------------------" + "\n"
                    + "此小票不需要顾客签字"
                    + "\r\n\r\n\r\n"; // 加三行换行，避免走纸


            byte header[] = null;
            byte strbuf[] = null;


            // 设置GBK编码
            // Android手机默认也是UTF8编码
            header = new byte[]{0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39,
                    00};
            try {
                strbuf = text.getBytes("GBK");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //打印订单头部--字体大小以及居中sage...
            String headText = "付款" + "\r\n\r\n";
            Bundle dataAlign = new Bundle();
            dataAlign.putInt(Global.INTPARA1, 1);
            Bundle dataTextOut = new Bundle();
            dataTextOut.putString(Global.STRPARA1, headText);
            dataTextOut.putString(Global.STRPARA2, "GBK");
            dataTextOut.putInt(Global.INTPARA1, 0);
            dataTextOut.putInt(Global.INTPARA2, 1);
            dataTextOut.putInt(Global.INTPARA3, 1);
            WorkService.workThread.handleCmd(Global.CMD_POS_SALIGN,
                    dataAlign);
            WorkService.workThread.handleCmd(Global.CMD_POS_STEXTOUT,
                    dataTextOut);

            //打印订单详情body
            byte buffer[] = DataUtils.byteArraysToBytes(new byte[][]{
                    header, strbuf});
            Bundle data = new Bundle();
            data.putByteArray(Global.BYTESPARA1, buffer);
            data.putInt(Global.INTPARA1, 0);
            data.putInt(Global.INTPARA2, buffer.length);
            WorkService.workThread.handleCmd(Global.CMD_POS_WRITE, data);


        } else {
            Toast.makeText(context, Global.toast_notconnect,
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, ConnectBTPairedActivity.class);
            startActivity(intent);
//            toDialog();
        }

    }

    static class MHandler extends Handler {

        WeakReference<MainActivity> mActivity;

        MHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity theActivity = mActivity.get();
            switch (msg.what) {

            }
        }
    }
}
