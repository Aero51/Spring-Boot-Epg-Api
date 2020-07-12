package com.aero51.springbootepdapi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.aero51.springbootepdapi.db.OutputChannelListRepository;
import com.aero51.springbootepdapi.db.ProgramRepository;
import com.aero51.springbootepdapi.db.PubProxyDataRepository;
import com.aero51.springbootepdapi.model.input.Category;
import com.aero51.springbootepdapi.model.input.Channel;
import com.aero51.springbootepdapi.model.input.Programme;
import com.aero51.springbootepdapi.model.input.Tv;
import com.aero51.springbootepdapi.model.output.OutputChannel;
import com.aero51.springbootepdapi.model.output.OutputProgram;
import com.aero51.springbootepdapi.model.pubproxy.Data;
import com.aero51.springbootepdapi.model.pubproxy.PubProxyResponseModel;
import com.aero51.springbootepdapi.retrofit.RetrofitApi;
import com.aero51.springbootepdapi.retrofit.RetrofitInstance;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Component
public class InitialLoadApplicationRunner implements ApplicationRunner {

	// @Autowired
	// private DescRepository descRepo;
	@Autowired
	private OutputChannelListRepository channelsRepo;
	@Autowired
	private ProgramRepository programRepo;
	@Autowired
	private PubProxyDataRepository pubProxyRepo;
	@Autowired
	private DownloadEpgService service;

	private Integer failcount = 1;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("InitialLoad");
		// Thread.sleep(5000);
		initiateEpgDownload();
		System.out.println("InitialLoad complete");
	}

	private void initiateEpgDownload() {
		String proxyHost = "191.37.49.226";
		Integer proxyPort = 3128;
		List<Data> dataList = new ArrayList<Data>();
		pubProxyRepo.findAll().forEach(dataList::add);
		if (dataList.size() > 0) {
			Data data = dataList.get(0);
			proxyHost = data.getIp();
			proxyPort = data.getPort();
		}

		RetrofitApi epdRetrofitApi = RetrofitInstance.getEpdApi(proxyHost, proxyPort);
		Call<Tv> call = epdRetrofitApi.getEpg();

		call.enqueue(new Callback<Tv>() {
			@Override
			public void onResponse(Call<Tv> call, Response<Tv> response) {
				if (!response.isSuccessful()) {
					System.out
							.println("epd Tv Response not ok: " + response.code() + " ,message:" + response.message());
				} else {
					System.out.println(
							"epd Tv Response ok: " + response.code() + " ,:" + response.body().getProgramme().size());
					saveTodb(response);
				}

			}

			@Override
			public void onFailure(Call<Tv> call, Throwable t) {
				System.out.println("epd Throwable: " + t.getMessage() + " ,failcount: " + failcount);
				System.out.println("epd stack trace: " + t.getStackTrace().toString());

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				// System.out.println("epd stack trace: " + sStackTrace);

				if (failcount < 51) {
					fetchNewPubProxy();
				}
			}
		});

	}

	private void fetchNewPubProxy() {
		RetrofitApi pubProxyRetrofitApi = RetrofitInstance.getpubProxyApi();
		Call<PubProxyResponseModel> call = pubProxyRetrofitApi.getPubProxy();
		call.enqueue(new Callback<PubProxyResponseModel>() {

			@Override
			public void onResponse(Call<PubProxyResponseModel> call, Response<PubProxyResponseModel> response) {
				if (!response.isSuccessful()) {
					System.out.println(
							"PubProxy  Response not ok: " + response.code() + " ,message:" + response.message());
					failcount = failcount + 1;
					if (failcount < 51) {
						fetchNewPubProxy();
					}
				} else {
					System.out.println(
							"PubProxy Response ok: " + response.code() + " ,size :" + response.body().getData().size());
					pubProxyRepo.deleteAll();
					Data data = response.body().getData().get(0);
					pubProxyRepo.save(data);
					initiateEpgDownload();
					failcount = failcount + 1;
				}

			}

			@Override
			public void onFailure(Call<PubProxyResponseModel> call, Throwable t) {
				// TODO Auto-generated method stub
				System.out.println("PubProxy Throwable: " + t.getMessage());
				System.out.println("PubProxy stack trace: " + t.getStackTrace().toString());
				failcount = failcount + 1;
				if (failcount < 51) {
					try {
						Thread.sleep(1000);
						fetchNewPubProxy();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
	}

	private void saveTodb(Response<Tv> response) {

		Tv tv = response.body();
		List<Channel> channelList = tv.getChannel();
		processChannels(channelList);
		List<Programme> programmeList = tv.getProgramme();

		processProgrammes(programmeList);

	}

	private void processChannels(List<Channel> channelList) {
		System.out.println("number of channels before process: " + channelList.size());
		List<OutputChannel> outputChannelList = new ArrayList<OutputChannel>();
		for (Channel channel : channelList) {
			String channelId = channel.getId();
			if (isExcluded(channelId)) {
				channel.setDisplay_name(channel.getDisplayName().getContent());
				OutputChannel outputChannel = new OutputChannel();
				outputChannel.setName(channel.getId());
				outputChannel.setDisplay_name(channel.getDisplay_name());
				outputChannelList.add(outputChannel);
			}
		}
		channelsRepo.deleteAll();
		channelsRepo.saveAll(outputChannelList);
		System.out.println("number of channels after process: " + outputChannelList.size());

	}

	private void processProgrammes(List<Programme> programmeList) {

		List<OutputProgram> outputProgramList = new ArrayList<OutputProgram>();
		for (Programme programme : programmeList) {
			String channel = programme.getChannel();
			if (isExcluded(channel)) {
				OutputProgram outputProgram = new OutputProgram();

				outputProgram.setChannel(channel);

				String channel_display_name = channelsRepo.findByName(channel).get(0).getDisplay_name();
				outputProgram.setChannel_display_name(channel_display_name);
				String title = programme.getTitle().getContent();
				if (title.matches(".*[(][?][)].*")) {
					title = title.replace("(?)", "");
				}

				outputProgram.setTitle(title);
				outputProgram.setStart(programme.getStart());
				outputProgram.setStop(programme.getStop());
				if (programme.getSubTitle() != null) {
					outputProgram.setSubTitle(programme.getSubTitle().getContent());
				}

				outputProgram.setDate(programme.getDate());
				if (programme.getIcon() != null) {
					String url = programme.getIcon().getSrc();
					String prefix = "https:";
					if (url.startsWith("//")) {
						url = prefix + url;
					}
					if (url.startsWith("http:")) {
						url = prefix + url.substring(5);
					}
					outputProgram.setIcon(url);
				}

				if (programme.getDesc().size() > 0) {
					// programmeList.get(i).getDesc().size() > 0
					outputProgram.setDesc(programme.getDesc().get(0).getContent());
				} else {
					outputProgram.setDesc("Opis nije dostupan");
				}

				if (programme.getCredits() != null) {
					List<String> directors = programme.getCredits().getDirector();
					List<String> actors = programme.getCredits().getActor();
					List<String> writers = programme.getCredits().getWriter();
					Map<String, List<String>> map = new HashMap<>();
					map.put("Writers", writers);
					map.put("Actors", actors);
					map.put("Directors", directors);

					ObjectMapper gson = new ObjectMapper();
					try {
						String json = gson.writeValueAsString(map);
						outputProgram.setCredits(json);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (programme.getCategory() != null) {
					List<Category> categories = programme.getCategory();
					List<String> content = new ArrayList<String>();
					for (Category category : categories) {
						content.add(category.getContent());
					}

					Map<String, List<String>> map = new HashMap<>();
					map.put("Category", content);

					ObjectMapper gson = new ObjectMapper();
					try {
						String json = gson.writeValueAsString(map);
						outputProgram.setCategory(json);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					Map<String, List<String>> map = new HashMap<>();
					ArrayList<String> cat = new ArrayList<>();
					cat.add("Bez kategorije");
					map.put("Category", cat);
					ObjectMapper gson = new ObjectMapper();
					try {
						String json = gson.writeValueAsString(map);
						outputProgram.setCategory(json);
					} catch (JsonProcessingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}

				outputProgramList.add(outputProgram);
			}
		}
		System.out.println("before insert, output program list size: " + outputProgramList.size());
		programRepo.deleteAll();
		programRepo.saveAll(outputProgramList);
		System.out.println("after insert");

		/*
		 * for (OutputProgram program : outputProgramList) { if
		 * (program.getChannel().equals("NOVATV")) {
		 * System.out.println(program.getTitle()); if
		 * (program.getTitle().equals("Dnevnik Nove TV")) {
		 * 
		 * } } }
		 */
	}

	private boolean isExcluded(String channel) {
		// 55 channels
		return !channel.equals("ALJAZEERA") && !channel.equals("24KITCHEN") && !channel.equals("KITCHENTV")
				&& !channel.equals("EENTERTAINMENT") && !channel.equals("ARENASPORT1") && !channel.equals("ARENASPORT2")
				&& !channel.equals("ARENASPORT3") && !channel.equals("ARENASPORT4") && !channel.equals("ARENASPORT5")
				&& !channel.equals("ARENASPORT1BH") && !channel.equals("NOVASPORT") && !channel.equals("SSMAINEVENT")
				&& !channel.equals("SSPORTSFOOTBALL") && !channel.equals("SSPORTSPREMIERLEAGUE")
				&& !channel.equals("SSPORTSACTION") && !channel.equals("SSPORTSARENA") && !channel.equals("SKYSPORTSF1")
				&& !channel.equals("SKYSPORTSMIX") && !channel.equals("SKYSPORTSCRICKET") && !channel.equals("SSGOLF")
				&& !channel.equals("BTSPORT3") && !channel.equals("ESPNUK") && !channel.equals("VARAZDINSKATV")
				&& !channel.equals("RTBTV") && !channel.equals("O2TV") && !channel.equals("PRVASRPSKATELEVIZIJA")
				&& !channel.equals("HAPPYTV") && !channel.equals("TVCGSAT") && !channel.equals("ATV")
				&& !channel.equals("FACETV") && !channel.equals("NOVABH") && !channel.equals("TVALFASARAJEVO")
				&& !channel.equals("PINK") && !channel.equals("PINKSCIFIANDFANTASY")
				&& !channel.equals("PINKCRIMEANDMYSTERY") && !channel.equals("PINKWORLDCINEMA")
				&& !channel.equals("PINKLOL") && !channel.equals("HBOCOMEDY") && !channel.equals("FILMBOX")
				&& !channel.equals("UNIVERSALCHANNEL") && !channel.equals("SCIFICHANNEL")
				&& !channel.equals("DISCOVERYCHANNEL") && !channel.equals("DISCOVERYSCIENCE")
				&& !channel.equals("HISTORYCHANNEL") && !channel.equals("VIASATEXPLORE")
				&& !channel.equals("VIASATHISTORY") && !channel.equals("VIASATNATURE")
				&& !channel.equals("TRAVELCHANNEL") && !channel.equals("INVESTIGATIONDISCOVERY")
				&& !channel.equals("DAVINCILEARNING") && !channel.equals("COMEDYCENTRALEXTRA")
				&& !channel.equals("PINKFOLK") && !channel.equals("GRANDNARODNATV") && !channel.equals("PINKSUPERKIDS")
				&& !channel.equals("DISNEYCHANNEL") && !channel.equals("BOSNATV1") && !channel.equals("BOXNATION")
				&& !channel.equals("BTSPORT1") && !channel.equals("BTSPORT2");

		// BOSNATV1,BOXNATION,BTSPORT1,BTSPORT2 uopce nema programa
		// ALJAZEERA, KITCHENTV,EENTERTAINMENT,ARENASPORT1,ARENASPORT2,ARENASPORT3 imaju
		// programe stare 20 dana 24KITCHEN
		// ARENASPORT1,ARENASPORT2,ARENASPORT3,ARENASPORT4,ARENASPORT5,ARENASPORT1BH,NOVASPORT,
		// SSMAINEVENT,SSPORTSFOOTBALL,SSPORTSPREMIERLEAGUE,SSPORTSACTION,SSPORTSARENA,SKYSPORTSF1
		// SKYSPORTSMIX,SKYSPORTSCRICKET,SSGOLF,BTSPORT3,ESPNUK,VARAZDINSKATV,RTBTV,O2TV,
		// PRVASRPSKATELEVIZIJA,HAPPYTV,TVCGSAT,ATV,FACETV,NOVABH,TVALFASARAJEVO
		// PINK,PINKSCIFIANDFANTASY,PINKCRIMEANDMYSTERY,PINKWORLDCINEMA,PINKLOL,HBOCOMEDY
		// FILMBOX,UNIVERSALCHANNEL,SCIFICHANNEL,DISCOVERYCHANNEL,DISCOVERYSCIENCE,HISTORYCHANNEL,
		// VIASATEXPLORE,VIASATHISTORY,VIASATNATURE,TRAVELCHANNEL,INVESTIGATIONDISCOVERY,
		// DAVINCILEARNING,COMEDYCENTRALEXTRA,PINKFOLK,GRANDNARODNATV,PINKSUPERKIDS,DISNEYCHANNEL
		// stari 20 dana
	}

}
/*
 * </programme><programme start="20200605080000 +0200"
 * stop="20200605090000 +0200" channel="FACETV"> <title lang="hr">Muzicki
 * program</title> <desc lang="hr"> Category: Glazba. </desc> <desc lang="hr"/>
 * </programme>
 */