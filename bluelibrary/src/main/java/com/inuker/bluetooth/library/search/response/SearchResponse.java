package com.inuker.bluetooth.library.search.response;

import com.inuker.bluetooth.library.search.SearchResult;

import org.json.JSONObject;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public interface SearchResponse {

    void onSearchStarted();

    void onDeviceFounded(SearchResult device);

    void onSearchStopped();

    void onSearchCanceled();

    void onResponseAck(JSONObject jsonObject);

}
