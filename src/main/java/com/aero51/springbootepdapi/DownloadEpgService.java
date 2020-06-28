package com.aero51.springbootepdapi;

import org.springframework.stereotype.Service;

import com.aero51.springbootepdapi.model.input.Tv;

import retrofit2.Response;

@Service
public class DownloadEpgService {
	private Response<Tv> mresponse;

	public DownloadEpgService() {
		super();
		getResponse();
	}

	private void getResponse() {

	}
}
