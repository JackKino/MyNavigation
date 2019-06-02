package com.example.administrator.mynavigation;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String MAC = "B0:D5:9D:6F:E7:A5";


    private PullToRefreshListView mListView;
    private DeviceListAdapter mAdapter;
    private TextView mTvTitle;
    String[] permissions={Manifest.permission.BLUETOOTH,Manifest.permission.ACCESS_FINE_LOCATION};

    private List<SearchResult> mDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private TextView my_mac;
    private Button main_unlock;
    private static final String TAG="MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDevices = new ArrayList<SearchResult>();

        mTvTitle = (TextView) findViewById(R.id.title);
        my_mac=findViewById(R.id.my_mac);

        main_unlock=(Button) findViewById(R.id.main_unlock);
        main_unlock.setOnClickListener(this);

        mListView = (PullToRefreshListView) findViewById(R.id.pulllayout);

//**使用此检查确定设备是否支持BLE。 然后你可以有选择地禁用与BLE相关的功能。*/
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "该设备不支持ble蓝牙", Toast.LENGTH_SHORT).show();
            Log.e("mSearchResponse","该设备不支持ble蓝牙");
            finish();
        }else{
            Log.e("mSearchResponse","该设备支持ble蓝牙");
        }
        mAdapter = new DeviceListAdapter(this);
        mListView.setAdapter(mAdapter);

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                searchDevice();
            }
        });
        Log.e("mSearchResponse","oncreate");
// Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 200);
        }else {

            if (Build.VERSION.SDK_INT >= 23) {
                PermissionUtil.checkAndRequestMorePermissions(this, permissions, 100, new PermissionUtil.PermissionRequestSuccessCallBack() {
                    @Override
                    public void onHasPermission() {
                        searchDevice();
                    }
                });
                String mac=CommonUtils.getBtAddressViaReflection();
                my_mac.setText(mac);
            } else {
                Log.e("mSearchResponse", "onHasPermission22222");
                String mac=mBluetoothAdapter.getAddress();
                my_mac.setText(mac);
                searchDevice();
            }

            ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(boolean openOrClosed) {
                    BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));
                }
            });


        }

    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         PermissionUtil.onRequestMorePermissionsResult(this, permissions, new PermissionUtil.PermissionCheckCallBack() {
             @Override
             public void onHasPermission() {
                 Log.e("mSearchResponse","result  onHasPermission");
                 searchDevice();

             }

             @Override
             public void onUserHasAlreadyTurnedDown(String... permission) {

             }

             @Override
             public void onUserHasAlreadyTurnedDownAndDontAsk(String... permission) {

             }
         });

    }



    private void searchDevice() {
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(10000, 1).build();

        ClientManager.getClient().search(request, mSearchResponse);
    }

    private final SearchResponse mSearchResponse = new SearchResponse() {
        @Override
        public void onSearchStarted() {
            Log.e("mSearchResponse","started");
            BluetoothLog.w("MainActivity.onSearchStarted");
            mTvTitle.setText(R.string.string_refreshing);
            mDevices.clear();
        }

        @Override
        public void onDeviceFounded(SearchResult device) {
            Log.e("mSearchResponse","onDeviceFounded"+device.getName()+" address=="+device.getAddress());
//            BluetoothLog.w("MainActivity.onDeviceFounded " + device.device.getAddress());
            if (!mDevices.contains(device)) {
                mDevices.add(device);
                mAdapter.setDataList(mDevices);

//                Beacon beacon = new Beacon(device.scanRecord);
//                BluetoothLog.v(String.format("beacon for %s\n%s", device.getAddress(), beacon.toString()));

//                BeaconItem beaconItem = null;
//                BeaconParser beaconParser = new BeaconParser(beaconItem);
//                int firstByte = beaconParser.readByte(); // 读取第1个字节
//                int secondByte = beaconParser.readByte(); // 读取第2个字节
//                int productId = beaconParser.readShort(); // 读取第3,4个字节
//                boolean bit1 = beaconParser.getBit(firstByte, 0); // 获取第1字节的第1bit
//                boolean bit2 = beaconParser.getBit(firstByte, 1); // 获取第1字节的第2bit
//                beaconParser.setPosition(0); // 将读取起点设置到第1字节处
            }
            Log.e("mSearchResponse","mDevices=="+mDevices.size());
            if (mDevices.size() > 0) {
               mListView.setAdapter(mAdapter);
            }
        }

        @Override
        public void onSearchStopped() {
            Log.e("mSearchResponse","onSearchStopped");

            BluetoothLog.w("MainActivity.onSearchStopped");
            mListView.onRefreshComplete();
            mTvTitle.setText(R.string.devices);
            mDevices.clear();
        }

        @Override
        public void onSearchCanceled() {
            Log.e("mSearchResponse","onSearchCanceled");

            BluetoothLog.w("MainActivity.onSearchCanceled");
            mListView.onRefreshComplete();
            mTvTitle.setText(R.string.devices);
            mDevices.clear();
        }

        @Override
        public void onResponseAck(int i, int i1) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        ClientManager.getClient().stopSearch();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==200&&resultCode==RESULT_OK){

            if (Build.VERSION.SDK_INT >= 23) {
                PermissionUtil.checkAndRequestMorePermissions(this, permissions, 100, new PermissionUtil.PermissionRequestSuccessCallBack() {
                    @Override
                    public void onHasPermission() {
                        searchDevice();
                    }
                });
                String mac=CommonUtils.getBtAddressViaReflection();
                my_mac.setText(mac);
            } else {
                Log.e("mSearchResponse", "onHasPermission22222");

                searchDevice();
                String mac=CommonUtils.getBluetoothAddress();
                my_mac.setText(mac);
            }

            ClientManager.getClient().registerBluetoothStateListener(new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(boolean openOrClosed) {
                    BluetoothLog.v(String.format("onBluetoothStateChanged %b", openOrClosed));
                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_unlock:
               /* ClientManager.getClient().unlock(createAdvertiseData(),mAdvertiseCallback);

                Log.e(TAG, "开启广播");*/
                Intent intent = new Intent();
                intent.setClass(this, DeviceDetailActivity.class);
                intent.putExtra("mac", MAC);
                startActivity(intent);
                break;
        }
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
     //   mDataBuilder.setIncludeTxPowerLevel(true);
     //   mDataBuilder.addServiceData(ParcelUuid.fromString("0000fff0-0000-1000-8000-00805f9b34fb"),new byte[]{1,2});
        mDataBuilder.addManufacturerData(0x01AC, broadcastData);
        AdvertiseData mAdvertiseData = mDataBuilder.build();
        return mAdvertiseData;
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
}
