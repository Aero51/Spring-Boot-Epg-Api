package com.aero51.springbootepdapi;

import com.aero51.springbootepdapi.model.Tv;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Streaming;

public interface TheMovieDbApi {

	@Streaming
	@Headers({ "Content-Type: application/gzip;charset=utf-8", })
	@GET("/")
	Call<Tv> getEpg();

}