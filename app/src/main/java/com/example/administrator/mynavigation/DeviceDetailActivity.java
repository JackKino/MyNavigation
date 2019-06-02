package com.example.administrator.mynavigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.beacon.Beacon;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.BluetoothUtils;
import com.inuker.bluetooth.library.utils.ByteUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private String mac;
    private static final String TAG = "MainActivity";
    private static final String HEART_RATE_SERVICE = "0000fff8-0000-1000-8000-00805f9b34fb";
    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_WRITE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_ENABLE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    //private static final String MAC = "B4:0B:44:3E:23:35";

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothAdapter mBluetoothAdapter;
    private Button bluedetail_unlock,bluedetail_getnotify,bluedetail_setData,bluedetail_bind;
    private TextView bluedetail_mac;

    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic characterNotify;
    private BluetoothManager mBluetoothManager;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic characterWrite;
    private AlertDialog dialog;
    private boolean isShowDialog1=true;
    private boolean isShowDialog2=true;
    private Button bluedetail_closeadvertise;
    private EditText set_pwd,put_pwd;
    private Handler handler=new Handler();
    private String save_pwd;
    private boolean isUnlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        bluedetail_unlock=this.findViewById(R.id.bluedetail_unlock);
        bluedetail_unlock.setOnClickListener(this);
        bluedetail_mac=findViewById(R.id.bluedetail_mac);

        bluedetail_getnotify=findViewById(R.id.bluedetail_getnotify);
        bluedetail_getnotify.setOnClickListener(this);
        bluedetail_setData=findViewById(R.id.bluedetail_setData);
        bluedetail_setData.setOnClickListener(this);
        bluedetail_closeadvertise=findViewById(R.id.bluedetail_closeadvertise);
        bluedetail_closeadvertise.setOnClickListener(this);
        bluedetail_bind=findViewById(R.id.bluedetail_bind);
        bluedetail_bind.setOnClickListener(this);


        set_pwd=this.findViewById(R.id.set_pwd);
        put_pwd=this.findViewById(R.id.put_pwd);


        mac=getIntent().getExtras().getString("mac");
        bluedetail_mac.setText(mac);
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
       // mBluetoothAdapter =BluetoothAdapter.getDefaultAdapter();
        //mBluetoothLeAdvertiser=mBluetoothAdapter.getBluetoothLeAdvertiser();
        mBluetoothLeAdvertiser= BluetoothUtils.getBluetoothLeAdvertiser();//判断你的设备到底支持不支持BLE Peripheral。假如此返回值非空，你才可以继续有机会开发
        if (mBluetoothLeAdvertiser == null) {
            Toast.makeText(this, "不支持BLE Peripheral", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "不支持BLE Peripheral");
           // finish();
        }else{
            Log.e(TAG, "支持BLE Peripheral");
        }

        save_pwd= (String) SharedPreferencesUtils.getString(this,"pwd","pwd");
        searchDevice();







    }

    private void searchDevice() {
        isShowDialog1=true;
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(60000, 1).build();

        ClientManager.getClient().search(request, mSearchResponse);
    }




    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            Log.e("onDeviceFounded","started");
            BluetoothLog.w("MainActivity.onSearchStarted");

        }

        @Override
        public void onDeviceFounded(SearchResult device) {

//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());


                Beacon beacon = new Beacon(device.scanRecord);
               // BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));
                byte[] mBytes = ByteUtils.trimLast(device.scanRecord);
            Log.e("onDeviceFounded","onDeviceFounded"+device.getName()+" address=="+device.getAddress()+" scanRecord==  "+ByteUtils.byteToString(mBytes));

            if(ByteUtils.byteToString(mBytes).contains("0201011BFF")){
                SharedPreferencesUtils.putString(DeviceDetailActivity.this, "pwd", ByteUtils.byteToString(mBytes));
                Log.e("onDeviceFounded","onDeviceFounded"+device.getName()+" address=="+device.getAddress()+"   "+ByteUtils.byteToString(mBytes));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
                            builder.setMessage("收到设置bind命令");
                            // builder.setIcon(R.mipmap.ic_launcher_round);
                            //点击对话框以外的区域是否让对话框消失
                            builder.setCancelable(false);
                            //设置正面按钮
                            builder.setPositiveButton("ACK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(DeviceDetailActivity.this, "设置BIND ACK", Toast.LENGTH_SHORT).show();
                                  //  ClientManager.getClient().unlock(setPwdData_ack(),mAdvertiseCallback);
                                    if(mBluetoothLeAdvertiser==null)
                                        mBluetoothLeAdvertiser= BluetoothUtils.getBluetoothLeAdvertiser();//判断你的设备到底支持不支持BLE Peripheral。假如此返回值非空，你才可以继续有机会开发

                                    mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 10),setPwdData_ack(), mAdvertiseCallback);



                                    dialog.dismiss();
                                    dialog=null;
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            stopAdvertise();
                                        }
                                    },5000);
                                    //isShowDialog1=true;
                                }
                            });
                            if(dialog==null) {
                                dialog = builder.create();
                            }
                            if(!dialog.isShowing()) {
                                if(isShowDialog1){
                                    dialog.show();
                                    isShowDialog1=false;
                                }

                            }
                        }
                    });


                }

            if(ByteUtils.byteToString(mBytes).endsWith("4567")){

                Log.e("onDeviceFounded","onDeviceFounded"+device.getName()+" address=="+device.getAddress()+"   "+ByteUtils.byteToString(mBytes));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopAdvertise();
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
                        builder.setMessage("设置密码成功");
                        // builder.setIcon(R.mipmap.ic_launcher_round);
                        //点击对话框以外的区域是否让对话框消失
                        builder.setCancelable(false);
                        //设置正面按钮
                        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                dialog=null;
                                //isShowDialog2=true;
                            }
                        });
                        if(dialog==null) {
                            dialog = builder.create();
                        }
                        if(!dialog.isShowing()) {
                            if(isShowDialog2){
                                dialog.show();
                                isShowDialog2=false;
                            }
                        }
                    }
                });


            }


            if(ByteUtils.byteToString(mBytes).contains("ACAC")) {
                isUnlock=false;
                if (save_pwd.equals("") || save_pwd == null) {
                    Toast.makeText(DeviceDetailActivity.this, "你还没有设置密码，请先设置密码！", Toast.LENGTH_SHORT).show();

                } else {
                    String value=ByteUtils.byteToString(mBytes);
                    value=value.replace("ACAC","ABAB");
                    if(save_pwd.equals(value)) {
                        isUnlock=true;
                        SharedPreferencesUtils.putString(DeviceDetailActivity.this, "pwd", ByteUtils.byteToString(mBytes));
                        Log.e("onDeviceFounded", "onDeviceFounded" + device.getName() + " address==" + device.getAddress() + "   " + ByteUtils.byteToString(mBytes));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
                                builder.setTitle("收到开锁命令");
                                builder.setMessage("开锁成功");
                                // builder.setIcon(R.mipmap.ic_launcher_round);
                                //点击对话框以外的区域是否让对话框消失
                                builder.setCancelable(false);
                                //设置正面按钮
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(DeviceDetailActivity.this, "开锁ACK", Toast.LENGTH_SHORT).show();
                                       // ClientManager.getClient().unlock(unLockData_ack(isUnlock), mAdvertiseCallback);
                                        dialog.dismiss();
                                        dialog=null;
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                stopAdvertise();
                                            }
                                        }, 3000);
                                        //isShowDialog1=true;
                                    }
                                });
                                if (dialog == null) {
                                    dialog = builder.create();
                                }
                                if (!dialog.isShowing()) {
                                    if (isShowDialog1) {
                                        dialog.show();
                                        isShowDialog1 = false;
                                    }

                                }
                            }
                        });

                    }else{
                        isUnlock=false;
                        //Toast.makeText(DeviceDetailActivity.this, "密码错误请重新输入！", Toast.LENGTH_SHORT).show();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
                                builder.setTitle("收到开锁命令");
                                builder.setMessage("开锁失败");
                                // builder.setIcon(R.mipmap.ic_launcher_round);
                                //点击对话框以外的区域是否让对话框消失
                                builder.setCancelable(false);
                                //设置正面按钮
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(DeviceDetailActivity.this, "开锁ACK", Toast.LENGTH_SHORT).show();
                                       // ClientManager.getClient().unlock(unLockData_ack(isUnlock), mAdvertiseCallback);
                                        dialog.dismiss();
                                        dialog=null;
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                stopAdvertise();
                                            }
                                        }, 3000);
                                        //isShowDialog1=true;
                                    }
                                });
                                if (dialog == null) {
                                    dialog = builder.create();
                                }
                                if (!dialog.isShowing()) {
                                    if (isShowDialog1) {
                                        dialog.show();
                                        isShowDialog1 = false;
                                    }

                                }
                            }
                        });
                    }
                }

            }


            if(ByteUtils.byteToString(mBytes).endsWith("1234")){

                Log.e("onDeviceFounded","onDeviceFounded"+device.getName()+" address=="+device.getAddress()+"   "+ByteUtils.byteToString(mBytes));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopAdvertise();
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
                        builder.setMessage("开锁成功");
                        // builder.setIcon(R.mipmap.ic_launcher_round);
                        //点击对话框以外的区域是否让对话框消失
                        builder.setCancelable(false);
                        //设置正面按钮
                        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                dialog=null;
                                //isShowDialog2=true;
                            }
                        });
                        if(dialog==null) {
                            dialog = builder.create();
                        }
                        if(!dialog.isShowing()) {
                            if(isShowDialog2){
                                dialog.show();
                                isShowDialog2=false;
                            }
                        }
                    }
                });

            }

            if(ByteUtils.byteToString(mBytes).endsWith("2345")){

                Log.e("onDeviceFounded","onDeviceFounded"+device.getName()+" address=="+device.getAddress()+"   "+ByteUtils.byteToString(mBytes));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopAdvertise();
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceDetailActivity.this);
                        builder.setMessage("开锁失败");
                        // builder.setIcon(R.mipmap.ic_launcher_round);
                        //点击对话框以外的区域是否让对话框消失
                        builder.setCancelable(false);
                        //设置正面按钮
                        builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                dialog=null;
                                //isShowDialog2=true;
                            }
                        });
                        if(dialog==null) {
                            dialog = builder.create();
                        }
                        if(!dialog.isShowing()) {
                            if(isShowDialog2){
                                dialog.show();
                                isShowDialog2=false;
                            }
                        }
                    }
                });


            }


        }

        @Override
        public void onSearchStopped() {
            Log.e("onDeviceFounded","onSearchStopped");

        }

        @Override
        public void onSearchCanceled() {
            Log.e("onDeviceFounded","onSearchCanceled");


        }

        @Override
        public void onResponseAck(int ack, int status) {
            Log.e(TAG,"ACK=="+ack+"  status=="+status);
            Toast.makeText(DeviceDetailActivity.this,"Bind成功",Toast.LENGTH_LONG).show();

        }
    };







    //回调
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        //成功
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            if (settingsInEffect != null) {
                Log.d(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Log.e(TAG, "onStartSuccess, settingInEffect is null");
            }
            Log.e(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);
        }

        //失败
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            Log.e(TAG, "onStartFailure errorCode" + errorCode);//返回的错误码
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                Toast.makeText(DeviceDetailActivity.this,"数据大于31个字节",Toast.LENGTH_LONG).show();
                Log.e(TAG, "数据大于31个字节");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                Toast.makeText(DeviceDetailActivity.this,"未能开始广播，没有广播实例",Toast.LENGTH_LONG).show();
                Log.e(TAG, "未能开始广播，没有广播实例");
            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                Toast.makeText(DeviceDetailActivity.this,"正在连接的，无法再次连接",Toast.LENGTH_LONG).show();
                Log.e(TAG, "正在连接的，无法再次连接");
            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                Toast.makeText(DeviceDetailActivity.this,"由于内部错误操作失败",Toast.LENGTH_LONG).show();
                Log.e(TAG, "由于内部错误操作失败");
            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                Toast.makeText(DeviceDetailActivity.this,"不支持此功能",Toast.LENGTH_LONG).show();
                Log.e(TAG, "不支持此功能");
            }
        }
    };

    /**
     * 初始化蓝牙类
     * AdvertisingSettings.Builder 用于创建AdvertiseSettings
     * AdvertiseSettings中包含三种数据：AdvertiseMode, Advertise TxPowerLevel和AdvertiseType，其测试结果如下：
     * AdvertiseMode:
     * Advertise Mode                           Logcat频率                   检测到的频率
     * ADVERTISE_MODE_LOW_LATENCY          1/1600 milliseconds                1/1068 milliseconds
     * ADVERTISE_MODE_BALANCED             1/400 milliseconds                 1/295 milliseconds
     * ADVERTISE_MODE_LOW_POWER            1/160 milliseconds                 1/142 milliseconds
     */
    public static AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        //设置广播的模式,应该是跟功耗相关
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        AdvertiseSettings mAdvertiseSettings = mSettingsbuilder.build();
        if (mAdvertiseSettings == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseSettings;
    }

    //设置一下FMP广播数据
    public static AdvertiseData setPwdData(String pwd,String mac) {
        /*AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        //添加的数据
        mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());

        AdvertiseData mAdvertiseData = mDataBuilder.build();

        if (mAdvertiseData == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseData;*/
        StringBuilder sb=new StringBuilder();
        sb.append(pwd).append(mac);
      //  byte[] broadcastData ={0x34,0x56}
        byte[] broadcastData=sb.toString().getBytes();
        Log.e(TAG, "broadcastData="+sb.toString()+" broadcastData=="+broadcastData.toString());
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
       // mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());
        //mDataBuilder.setIncludeDeviceName(true); //广播名称也需要字节长度
       // mDataBuilder.setIncludeTxPowerLevel(true);
        //mDataBuilder.addServiceData(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),new byte[]{1,2});
        mDataBuilder.addManufacturerData(0x6DAC, broadcastData);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
    }

    //设置一下FMP广播数据
    public static AdvertiseData unlocakData(String pwd,String mac) {
        /*AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        //添加的数据
        mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());

        AdvertiseData mAdvertiseData = mDataBuilder.build();

        if (mAdvertiseData == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseData;*/
        StringBuilder sb=new StringBuilder();
        sb.append(pwd).append(mac);
        //  byte[] broadcastData ={0x34,0x56}
        byte[] broadcastData=sb.toString().getBytes();
        Log.e(TAG, "broadcastData="+sb.toString()+" broadcastData=="+broadcastData.toString());
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        // mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());
        //mDataBuilder.setIncludeDeviceName(true); //广播名称也需要字节长度
        // mDataBuilder.setIncludeTxPowerLevel(true);
        //mDataBuilder.addServiceData(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),new byte[]{1,2});
        mDataBuilder.addManufacturerData(0xACAC, broadcastData);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
    }


    //设置一下FMP广播数据
    public static AdvertiseData setPwdData_ack() {
        /*AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        //添加的数据
        mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());

        AdvertiseData mAdvertiseData = mDataBuilder.build();

        if (mAdvertiseData == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseData;*/
        byte[] broadcastData ={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x05};
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        // mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());
        //mDataBuilder.setIncludeDeviceName(true); //广播名称也需要字节长度
        // mDataBuilder.setIncludeTxPowerLevel(true);
        //mDataBuilder.addServiceData(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),new byte[]{1,2});
        mDataBuilder.addManufacturerData(0x0005, broadcastData);
        AdvertiseData mAdvertiseData = mDataBuilder.build();

        byte[] broadcastData2 ={0x05,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x05};
        List<Integer> acks=new ArrayList<>();
        for(int i=0;i<9;i++){
            acks.add((int) broadcastData2[i]);
        }
        int status=StringUtils.andor(acks);
        Log.e("status","status=="+status+"   "+broadcastData2[9]);
        if(broadcastData2[9]== StringUtils.andor(acks)){

        }








        return mAdvertiseData;
    }

    //设置一下FMP广播数据
    public static AdvertiseData unLockData_ack(boolean isUnlock) {
        /*AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        //添加的数据
        mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());

        AdvertiseData mAdvertiseData = mDataBuilder.build();

        if (mAdvertiseData == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseData;*/
        byte[] broadcastData;
        if(isUnlock) {
            //密码正确
            broadcastData= new byte[]{0x12, 0x34};
        }else{
         //密码错误
           broadcastData = new byte[]{0x23, 0x45};
        }
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        // mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());
        //mDataBuilder.setIncludeDeviceName(true); //广播名称也需要字节长度
        // mDataBuilder.setIncludeTxPowerLevel(true);
        //mDataBuilder.addServiceData(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),new byte[]{1,2});
        mDataBuilder.addManufacturerData(0x6E, broadcastData);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
    }

    //注销
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //停止蓝牙广播
        stopAdvertise();
    }

    private void stopAdvertise() {
        if (mBluetoothLeAdvertiser != null) {

            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
            mBluetoothLeAdvertiser = null;

          //  mAdvertiseCallback=null;
            Log.e(TAG, "停止广播");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.bluedetail_unlock:

                if(mBluetoothLeAdvertiser==null)
                    mBluetoothLeAdvertiser= BluetoothUtils.getBluetoothLeAdvertiser();//判断你的设备到底支持不支持BLE Peripheral。假如此返回值非空，你才可以继续有机会开发

               /* String pwds=set_pwd.getText().toString().trim();
                if(pwds==""||pwds==null||pwds.equals("")){
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_LONG).show();
                    return;
                }*/
                // mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 10), setPwdData(pwds,mac), mAdvertiseCallback);
                ClientManager.getClient().unlock(mac,"",mAdvertiseCallback);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopAdvertise();
                    }
                },5000);

                /*//开启蓝牙广播  一个是广播设置参数，一个是广播数据，还有一个是Callback
                String unloca_pwds=put_pwd.getText().toString().trim();
                if(unloca_pwds==""||unloca_pwds==null||unloca_pwds.equals("")){
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_LONG).show();
                    return;
                }
                if(mBluetoothLeAdvertiser==null)
                    mBluetoothLeAdvertiser= BluetoothUtils.getBluetoothLeAdvertiser();//判断你的设备到底支持不支持BLE Peripheral。假如此返回值非空，你才可以继续有机会开发

                mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 10), unlocakData(unloca_pwds,mac), mAdvertiseCallback);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopAdvertise();
                    }
                },3000);
                Log.e(TAG, "开启广播");*/
              break;
            case R.id.bluedetail_getnotify:

                searchDevice();

                break;
            case R.id.bluedetail_setData:
               /* ClientManager.getClient().unlock(createAdvertiseData(),mAdvertiseCallback);
                searchDevice2();*/
                break;
            case R.id.bluedetail_bind:
                if(mBluetoothLeAdvertiser==null)
                    mBluetoothLeAdvertiser= BluetoothUtils.getBluetoothLeAdvertiser();//判断你的设备到底支持不支持BLE Peripheral。假如此返回值非空，你才可以继续有机会开发

               /* String pwds=set_pwd.getText().toString().trim();
                if(pwds==""||pwds==null||pwds.equals("")){
                    Toast.makeText(this,"请输入密码",Toast.LENGTH_LONG).show();
                    return;
                }*/
               // mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 10), setPwdData(pwds,mac), mAdvertiseCallback);
             //  ClientManager.getClient().bind(mac,"",mAdvertiseCallback);

               /* handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopAdvertise();
                    }
                },5000);*/

                break;
            case R.id.bluedetail_closeadvertise:
                stopAdvertise();
                break;
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);

    }
}
