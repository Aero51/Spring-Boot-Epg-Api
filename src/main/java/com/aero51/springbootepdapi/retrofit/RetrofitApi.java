package com.aero51.springbootepdapi.retrofit;

import com.aero51.springbootepdapi.model.input.Tv;
import com.aero51.springbootepdapi.model.pubproxy.PubProxyResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Streaming;

public interface RetrofitApi {

	@Streaming
	@Headers({ "Content-Type: application/gzip;charset=utf-8", })
	@GET("/")
	Call<Tv> getEpg();

	@GET("proxy")
	Call<PubProxyResponseModel> getPubProxy();

}