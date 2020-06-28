package com.aero51.springbootepdapi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

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
			// String proxyHost = "149.248.52.102";
			// int proxyPort = 8080;
			String proxyHost = "185.198.184.14";
			int proxyPort = 48122;

			Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));

			// .proxy(proxy)
			// addInterceptor(REWRITE_CONTENT_LENGTH_INTERCEPTOR)
			// builder.connectTimeout(30, TimeUnit.SECONDS);
			// builder.readTimeout(30, TimeUnit.SECONDS);
			OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS).proxy(proxy).retryOnConnectionFailure(true)
					.addInterceptor(loggingInterceptor).addNetworkInterceptor(new Interceptor() {
						@NotNull
						@Override
						public Response intercept(@NotNull Chain chain) throws IOException {
							Request req = chain.request();
							Headers.Builder headersBuilder = req.headers().newBuilder();

							String credential = Credentials.basic("test", "password");
							headersBuilder.set("Authorization", credential);

							Response res = chain.proceed(req.newBuilder().headers(headersBuilder.build()).build());
//.header("Connection", "close")
							return res.newBuilder().header("Content-Encoding", "gzip")
									.header("Content-Type", "application/xml").build();
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