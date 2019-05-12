package com.example.administrator.mynavigation;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.utils.BluetoothUtils;

import java.util.UUID;

public class DeviceDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private String mac;
    private static final String TAG = "MainActivity";
    private static final String HEART_RATE_SERVICE = "0000fff8-0000-1000-8000-00805f9b34fb";
    public final static UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_SERVICE = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_WRITE = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    public final static UUID UUID_LOST_ENABLE = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    private static final String MAC = "B4:0B:44:3E:23:35";

    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private BluetoothAdapter mBluetoothAdapter;
    private Button bluedetail_unlock,bluedetail_getnotify,bluedetail_setData;
    private TextView bluedetail_mac;

    private BluetoothGattServer gattServer;
    private BluetoothGattCharacteristic characterNotify;
    private BluetoothManager mBluetoothManager;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGattCharacteristic characterWrite;

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

        setServer();


    }

    /**
     * 添加服务，特征
     */
    private void setServer() {
        //读写特征
         characterWrite = new BluetoothGattCharacteristic(
                UUID_LOST_WRITE, BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_READ | BluetoothGattCharacteristic.PERMISSION_WRITE);
        //使能特征
        characterNotify = new BluetoothGattCharacteristic(UUID_LOST_ENABLE,
                BluetoothGattCharacteristic.PROPERTY_NOTIFY, BluetoothGattCharacteristic.PERMISSION_READ);
        characterNotify.addDescriptor(new BluetoothGattDescriptor(CLIENT_CHARACTERISTIC_CONFIG, BluetoothGattDescriptor.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ));
        //服务
        BluetoothGattService gattService = new BluetoothGattService(UUID_LOST_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);
        //为服务添加特征
        gattService.addCharacteristic(characterWrite);
        gattService.addCharacteristic(characterNotify);
        //管理服务，连接和数据交互回调
        gattServer = mBluetoothManager.openGattServer(this,
                new BluetoothGattServerCallback() {

                    @Override
                    public void onConnectionStateChange(final BluetoothDevice device,
                                                        final int status, final int newState) {
                        super.onConnectionStateChange(device, status, newState);
                        bluetoothDevice = device;
                        Log.d("MainActivity", "onConnectionStateChange:" + device + "    " + status + "   " + newState);
                        Log.e("MainActivity",device.getAddress() + "   " + device.getName() + "   " + status + "  " + newState);

                       /* runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                               Log.e("MainActivity",device.getAddress() + "   " + device.getName() + "   " + status + "  " + newState);
                            }
                        });*/
                    }

                    @Override
                    public void onServiceAdded(int status,
                                               BluetoothGattService service) {
                        super.onServiceAdded(status, service);
                        Log.d("MainActivity", "service added");
                    }

                    @Override
                    public void onCharacteristicReadRequest(
                            BluetoothDevice device, int requestId, int offset,
                            BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicReadRequest(device, requestId,
                                offset, characteristic);
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                        Log.d("MainActivity", "onCharacteristicReadRequest");
                    }

                    @Override
                    public void onCharacteristicWriteRequest(
                            BluetoothDevice device, int requestId,
                            BluetoothGattCharacteristic characteristic,
                            boolean preparedWrite, boolean responseNeeded,
                            int offset, final byte[] value) {
                        super.onCharacteristicWriteRequest(device, requestId,
                                characteristic, preparedWrite, responseNeeded,
                                offset, value);
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
                        Log.d("MainActivity", "onCharacteristicWriteRequest" + value[0]);
                       /* runOnUiThread(new Runnable() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void run() {

                            }
                        });*/
                    }

                    @Override
                    public void onNotificationSent(BluetoothDevice device, int status) {
                        super.onNotificationSent(device, status);
                        Log.i("MainActivity", "onNotificationSent: ");
                    }

                    @Override
                    public void onMtuChanged(BluetoothDevice device, int mtu) {
                        super.onMtuChanged(device, mtu);
                    }

                    @Override
                    public void onDescriptorReadRequest(BluetoothDevice device,
                                                        int requestId, int offset,
                                                        BluetoothGattDescriptor descriptor) {
                        super.onDescriptorReadRequest(device, requestId,
                                offset, descriptor);
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characterNotify.getValue());
                        Log.d("MainActivity", "onDescriptorReadRequest");
                    }

                    @Override
                    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                                         BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
                                                         int offset, byte[] value) {
                        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded,
                                offset, value);
                        gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
//                        characterNotify.setValue("HIHHHHH");
//                        gattServer.notifyCharacteristicChanged(bluetoothDevice, characterNotify, false);
                        Log.d("MainActivity", "onDescriptorWriteRequest");
                    }

                    @Override
                    public void onExecuteWrite(BluetoothDevice device,
                                               int requestId, boolean execute) {
                        super.onExecuteWrite(device, requestId, execute);
                        Log.e("MainActivity", "onExecuteWrite");
                    }
                });
        gattServer.addService(gattService);
    }



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
                Log.e(TAG, "数据大于31个字节");
            } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                Log.e(TAG, "未能开始广播，没有广播实例");
            } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
                Log.e(TAG, "正在连接的，无法再次连接");
            } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
                Log.e(TAG, "由于内部错误操作失败");
            } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
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
    public static AdvertiseData createAdvertiseData() {
        /*AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        //添加的数据
        mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());

        AdvertiseData mAdvertiseData = mDataBuilder.build();

        if (mAdvertiseData == null) {
            Log.e(TAG, "mAdvertiseSettings == null");
        }
        return mAdvertiseData;*/
        byte[] broadcastData ={0x34,0x56};
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
       // mDataBuilder.addServiceData(ParcelUuid.fromString(HEART_RATE_SERVICE), "eeeeeeeeee".getBytes());
        mDataBuilder.setIncludeDeviceName(true); //广播名称也需要字节长度
        mDataBuilder.setIncludeTxPowerLevel(true);
        mDataBuilder.addServiceData(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),new byte[]{1,2});
        mDataBuilder.addManufacturerData(0x01AC, broadcastData);
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
                //开启蓝牙广播  一个是广播设置参数，一个是广播数据，还有一个是Callback
               // mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 10), createAdvertiseData(), mAdvertiseCallback);
                //mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(true, 10), createAdvertiseData(), mAdvertiseCallback);


                 ClientManager.getClient().unlock(createAdvertiseData(),mAdvertiseCallback);

                Log.e(TAG, "开启广播");
              break;
            case R.id.bluedetail_getnotify:
               // characterNotify.setValue("HIHHHHH");
               // gattServer.notifyCharacteristicChanged(bluetoothDevice, characterNotify, false);
              ClientManager.getClient().read(MAC, UUID_LOST_SERVICE, UUID_LOST_WRITE, new BleReadResponse() {
                  @Override
                  public void onResponse(int i, byte[] bytes) {
                      Log.e("MainActivity","i=="+i);
                  }
              });

                break;
            case R.id.bluedetail_setData:
                 characterNotify.setValue("666");
                 gattServer.notifyCharacteristicChanged(bluetoothDevice, characterNotify, false);
                break;
        }

    }


}
