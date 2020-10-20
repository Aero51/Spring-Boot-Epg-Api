package com.aero51.springbootepdapi.retrofit;

import com.aero51.springbootepdapi.model.gimmeproxy.GimmeProxyResponseModel;
import com.aero51.springbootepdapi.model.input.Tv;
import com.aero51.springbootepdapi.model.pubproxy.PubProxyResponseModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Streaming;

public interface RetrofitApi {
	// "Content-Type: application/gzip;charset=utf-8",
	// @Headers({ "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64)
	// Accept-Encoding: identity
	// AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36" })
	// "Content-Type: application/gzip",
	// "Accept-Encoding: gzip",
	// "Connection: close",
	// @Headers({ "Content-Type: application/gzip", "User-Agent:
	// PostmanRuntime/7.26.5" })
	@Streaming
	@Headers({ "User-Agent: PostmanRuntime/7.26.5" })
	@GET("/")
	Call<Tv> getEpg();

	@GET("proxy?type=http")
	Call<PubProxyResponseModel> getPubProxy();

	@GET("getProxy?get=true&supportsHttps=true?anonymityLevel=1?protocol=http")
	Call<GimmeProxyResponseModel> getGimmeProxy();
}
