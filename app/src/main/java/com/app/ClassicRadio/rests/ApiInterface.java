package com.app.ClassicRadio.rests;

import com.app.ClassicRadio.callbacks.CallbackAlbumArt;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: RADIO_APP";

    @Headers({CACHE, AGENT})
    @GET("search")
    Call<CallbackAlbumArt> getAlbumArt(
            @Query("term") String term,
            @Query("media") String media,
            @Query("limit") int limit
    );

}
