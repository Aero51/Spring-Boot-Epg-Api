package com.aero51.springbootepdapi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;

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
			String proxyHost = "212.15.184.190";
			int proxyPort = 4145;
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
//.proxy(proxy)
			OkHttpClient okHttpClient = new OkHttpClient.Builder().proxy(proxy).retryOnConnectionFailure(true)
					.addInterceptor(loggingInterceptor).addInterceptor(REWRITE_CONTENT_LENGTH_INTERCEPTOR)
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
									.header("Content-Type", "application/xml").header("Connection", "close").build();
						}
					}).build();
			retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(JaxbConverterFactory.create())
					.client(okHttpClient).build();

		}
		return retrofit.create(TheMovieDbApi.class);
	}

	private static final Interceptor REWRITE_CONTENT_LENGTH_INTERCEPTOR = new Interceptor() {
		@Override
		public Response intercept(Interceptor.Chain chain) throws IOException {
			Response originalResponse = chain.proceed(chain.request());
			return originalResponse.newBuilder().removeHeader("Content-Length").build();
		}
	};
}