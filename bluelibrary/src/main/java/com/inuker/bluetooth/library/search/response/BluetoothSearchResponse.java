package com.inuker.bluetooth.library.search.response;

import com.inuker.bluetooth.library.search.SearchResult;

import org.json.JSONObject;

public interface BluetoothSearchResponse {
    void onSearchStarted();

    void onDeviceFounded(SearchResult device);

    void onSearchStopped();

    void onSearchCanceled();
    void onResponseAck(JSONObject jsonObject);
}
