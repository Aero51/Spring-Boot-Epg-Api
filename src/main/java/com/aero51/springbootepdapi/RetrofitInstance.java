package com.aero51.springbootepdapi;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;

import okhttp3.Credentials;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jaxb.JaxbConverterFactory;

public class RetrofitInstance {

	private static Retrofit retrofit = null;
	private static final String BASE_URL = "https://epg.phoenixrebornbuild.com.hr/";

	public static synchronized TheMovieDbApi getApiService() {
		if (retrofit == null) {

			HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
			loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

			OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(loggingInterceptor)
					.addNetworkInterceptor(new Interceptor() {
						@NotNull
						@Override
						public Response intercept(@NotNull Chain chain) throws IOException {
							Request req = chain.request();
							Headers.Builder headersBuilder = req.headers().newBuilder();

							String credential = Credentials.basic("test", "password");
							headersBuilder.set("Authorization", credential);

							Response res = chain.proceed(req.newBuilder().headers(headersBuilder.build()).build());

							return res.newBuilder().header("Content-Encoding", "gzip")
									.header("Content-Type", "application/xml").build();
						}
					}).build();
			retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(JaxbConverterFactory.create())
					.client(okHttpClient).build();

		}
		return retrofit.create(TheMovieDbApi.class);
	}
}