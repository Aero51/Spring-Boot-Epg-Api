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

import com.aero51.springbootepdapi.db.GimmeProxyDataRepository;
import com.aero51.springbootepdapi.db.OutputChannelListRepository;
import com.aero51.springbootepdapi.db.ProgramRepository;
import com.aero51.springbootepdapi.db.PubProxyDataRepository;
import com.aero51.springbootepdapi.model.gimmeproxy.GimmeProxyResponseModel;
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
	private GimmeProxyDataRepository gimmeProxyRepo;
	@Autowired
	private DownloadEpgService service;

	private Integer pubProxyFailcount = 0;
	private Integer gimmeProxyFailcount = 0;
	private Integer epgFailcount = 0;
	private List<String> croChannelList = createCroChannelsList();

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

		// fetchNewGimmeProxy();
		if (pubProxyFailcount < 11) {
			List<Data> dataList = new ArrayList<Data>();
			pubProxyRepo.findAll().forEach(dataList::add);
			if (dataList.size() > 0) {
				Data data = dataList.get(0);
				proxyHost = data.getIp();
				proxyPort = data.getPort();
			}
		} else {
			List<GimmeProxyResponseModel> gimmeProxyList = new ArrayList<GimmeProxyResponseModel>();
			gimmeProxyRepo.findAll().forEach(gimmeProxyList::add);
			if (gimmeProxyList.size() > 0) {
				GimmeProxyResponseModel gimmeProxyResponseModel = gimmeProxyList.get(0);
				proxyHost = gimmeProxyResponseModel.getIp();
				proxyPort = gimmeProxyResponseModel.getPort();
			}

		}
		System.out.println("initiateEpgDownload proxyHost : " + proxyHost + " ,proxyPort: " + proxyPort);
		RetrofitApi epdRetrofitApi = RetrofitInstance.getEpdApi(proxyHost, proxyPort);
		Call<Tv> call = epdRetrofitApi.getEpg();

		call.enqueue(new Callback<Tv>() {
			@Override
			public void onResponse(Call<Tv> call, Response<Tv> response) {
				if (!response.isSuccessful()) {
					System.out
							.println("epd Tv Response not ok: " + response.code() + " ,message:" + response.message());
					epgFailcount = epgFailcount + 1;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (pubProxyFailcount < 11) {
						fetchNewPubProxy();
					} else {
						fetchNewGimmeProxy();
					}

				} else {
					System.out.println(
							"epd Tv Response ok: " + response.code() + " ,:" + response.body().getProgramme().size());
					saveTodb(response);
				}

			}

			@Override
			public void onFailure(Call<Tv> call, Throwable t) {
				System.out.println("epd Throwable: " + t.getMessage() + " ,pubProxy failcount: " + pubProxyFailcount);
				System.out.println("epd stack trace: " + t.getStackTrace().toString());

				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				t.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				// System.out.println("epd stack trace: " + sStackTrace);
				epgFailcount = epgFailcount + 1;
				System.out.println("phoenixrebornbuild epgFailcount: " + epgFailcount);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// if (epgFailcount < 5) {
				if (pubProxyFailcount < 11) {
					fetchNewPubProxy();
				} else {
					fetchNewGimmeProxy();
				}
				// }
			}
		});

	}

	private void fetchNewPubProxy() {
		RetrofitApi pubProxyRetrofitApi = RetrofitInstance.getPubProxyApi();
		Call<PubProxyResponseModel> call = pubProxyRetrofitApi.getPubProxy();
		call.enqueue(new Callback<PubProxyResponseModel>() {

			@Override
			public void onResponse(Call<PubProxyResponseModel> call, Response<PubProxyResponseModel> response) {
				if (!response.isSuccessful()) {
					System.out.println("PubProxy  Response not ok: " + response.code() + " ,message:"
							+ response.message() + " ,pubProxyFailcount: " + pubProxyFailcount);
					pubProxyFailcount = pubProxyFailcount + 1;
					if (pubProxyFailcount < 11) {
						try {
							Thread.sleep(1000);
							fetchNewPubProxy();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						fetchNewGimmeProxy();
					}
				} else {
					System.out.println(
							"PubProxy Response ok: " + response.code() + " ,size :" + response.body().getData().size());
					pubProxyRepo.deleteAll();
					Data data = response.body().getData().get(0);
					pubProxyRepo.save(data);
					initiateEpgDownload();
					pubProxyFailcount = pubProxyFailcount + 1;
				}

			}

			@Override
			public void onFailure(Call<PubProxyResponseModel> call, Throwable t) {
				// TODO Auto-generated method stub
				System.out.println("PubProxy onFailure Throwable: " + t.getMessage());
				System.out.println("PubProxy stack trace: " + t.getStackTrace().toString());
				pubProxyFailcount = pubProxyFailcount + 1;
				if (pubProxyFailcount < 11) {
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

	private void fetchNewGimmeProxy() {
		RetrofitApi gimmeProxyRetrofitApi = RetrofitInstance.getGimmeProxyApi();
		Call<GimmeProxyResponseModel> call = gimmeProxyRetrofitApi.getGimmeProxy();
		call.enqueue(new Callback<GimmeProxyResponseModel>() {

			@Override
			public void onResponse(Call<GimmeProxyResponseModel> call, Response<GimmeProxyResponseModel> response) {
				if (!response.isSuccessful()) {
					System.out.println("GimmeProxy  Response not ok: " + response.code() + " ,message:"
							+ response.message() + " ,gimmeProxyFailcount: " + gimmeProxyFailcount);
					gimmeProxyFailcount = gimmeProxyFailcount + 1;
					if (gimmeProxyFailcount < 11) {
						try {
							Thread.sleep(1000);
							fetchNewGimmeProxy();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				} else {
					System.out.println("GimmeProxy Response ok: " + response.code() + " ,ip: " + response.body().getIp()
							+ " ,protocol: " + response.body().getProtocol());
					// pubProxyRepo.deleteAll();
					// Data data = response.body().getData().get(0);
					// pubProxyRepo.save(data);
					// initiateEpgDownload();
					gimmeProxyFailcount = gimmeProxyFailcount + 1;
					gimmeProxyRepo.deleteAll();
					gimmeProxyRepo.save(response.body());
					initiateEpgDownload();

				}

			}

			@Override
			public void onFailure(Call<GimmeProxyResponseModel> call, Throwable t) {
				// TODO Auto-generated method stub
				System.out.println("PubProxy onFailure Throwable: " + t.getMessage());
				System.out.println("PubProxy stack trace: " + t.getStackTrace().toString());
				gimmeProxyFailcount = gimmeProxyFailcount + 1;
				if (gimmeProxyFailcount < 11) {
					try {
						Thread.sleep(1000);
						fetchNewGimmeProxy();
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
		List<OutputChannel> unsortedChannelList = new ArrayList<OutputChannel>();
		for (Channel channel : channelList) {
			String channelId = channel.getId();
			if (isExcluded(channelId)) {
				channel.setDisplay_name(channel.getDisplayName().getContent());
				OutputChannel outputChannel = new OutputChannel();
				outputChannel.setName(channel.getId());
				outputChannel.setDisplay_name(channel.getDisplay_name());
				unsortedChannelList.add(outputChannel);

			}
		}

		channelsRepo.deleteAll();

		List<OutputChannel> sortedOutputChannelList = new ArrayList<OutputChannel>();
		channelsRepo.saveAll(unsortedChannelList);
		for (int i = 0; i < croChannelList.size(); i++) {

			// sortedOutputChannelList.add(channelsRepo.findByName(croChannelList.get(i)).get(0));
		}

		// channelsRepo.deleteAll();
		for (OutputChannel outputChannel : unsortedChannelList) {
			if (isSortedExcluded(outputChannel.getName())) {
				sortedOutputChannelList.add(outputChannel);
			}

		}
		// channelsRepo.saveAll(sortedOutputChannelList);

		System.out.println("number of channels after process unsorted: " + unsortedChannelList.size());
		// System.out.println("number of channels after process sorted: " +
		// sortedOutputChannelList.size());
		// System.out.println("number of channels after process: " +
		// unsortedChannelList.size());
	}

	private void processProgrammes(List<Programme> programmeList) {
		System.out.println("number of programs before process: " + programmeList.size());
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
						System.out.println("Credits converting to json error! ");
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
						System.out.println("Category converting to json error! ");
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
						System.out.println("Category Bez kategorije converting to json error! ");
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

	}

	private boolean isExcluded(String channel) {

		// crtici: boomerang , cartoonnetwork, nickelodeon, nickjr, disney
		return !channel.equals("hrt5") && !channel.equals("mtvadria") && !channel.equals("mezzo")
				&& !channel.equals("pinkextra") && !channel.equals("ducktv") && !channel.equals("ginx")
				// empty

				&& !channel.equals("sportdigital") && !channel.equals("skysportaustria") && !channel.equals("filmklub")
				&& !channel.equals("filmklubextra") && !channel.equals("zenskatv") && !channel.equals("bundesliga1")
				&& !channel.equals("bundesliga2") && !channel.equals("bundesliga3") && !channel.equals("bundesliga4")
				&& !channel.equals("bundesliga5") && !channel.equals("bundesliga6") && !channel.equals("bundesliga7")
				&& !channel.equals("bundesliga8") && !channel.equals("bundesliga9") && !channel.equals("bundesliga10")
				&& !channel.equals("automototv.it") && !channel.equals("rai1.it") && !channel.equals("rai2.it")
				&& !channel.equals("rai3.it") && !channel.equals("rai5.it") && !channel.equals("rai4.it")
				&& !channel.equals("raipremium.it") && !channel.equals("raimovie.it") && !channel.equals("raistoria.it")
				&& !channel.equals("Italia 1") && !channel.equals("Italia 2") && !channel.equals("canale5.it")
				&& !channel.equals("rete4.it") && !channel.equals("mediasetextra.it") && !channel.equals("iris.it")
				&& !channel.equals("cielo.it") && !channel.equals("la5.it") && !channel.equals("canale8.it")
				&& !channel.equals("la7.it") && !channel.equals("rsila1.it") && !channel.equals("rsila2.it")
				&& !channel.equals("fox.it") && !channel.equals("foxlife.it") && !channel.equals("foxscrime.it")
				&& !channel.equals("foxanimation.it") && !channel.equals("axn.it")
				&& !channel.equals("paramountchannel.it") && !channel.equals("studiouniversal.it")
				&& !channel.equals("topcrime.it") && !channel.equals("raigulp.it") && !channel.equals("raiyoyo.it")
				&& !channel.equals("boing.it") && !channel.equals("k2.it") && !channel.equals("cartoonito.it")
				&& !channel.equals("frisbee.it") && !channel.equals("supertv.it") && !channel.equals("disneyxd.it")
				&& !channel.equals("cartoonnetwork.it") && !channel.equals("skycinemauno.it")
				&& !channel.equals("skycinema24.it") && !channel.equals("skycinemahits.it")
				&& !channel.equals("skycinemafamily.it") && !channel.equals("skycinemacomedy.it")
				&& !channel.equals("skycinemacult.it") && !channel.equals("skycinemaclassics.it")
				&& !channel.equals("skyatlantic.it") && !channel.equals("premiumcinema.it")
				&& !channel.equals("premiumcinema24.it") && !channel.equals("premiumaction.it")
				&& !channel.equals("premiumcinemacomedy.it") && !channel.equals("premiumcinemaenergy.it")
				&& !channel.equals("premiumcinemaemotion.it") && !channel.equals("premiumjoi.it")
				&& !channel.equals("premiumstories.it") && !channel.equals("premiumcrime.it")
				&& !channel.equals("realtime.it") && !channel.equals("iddiscovery.it")
				&& !channel.equals("discoverychannel.it") && !channel.equals("discoveryscience.it")
				&& !channel.equals("nationalgeographic.it") && !channel.equals("natgeopeople.it")
				&& !channel.equals("natgeowild.it") && !channel.equals("focustv.it")
				&& !channel.equals("animalplanet.it") && !channel.equals("dmax.it") && !channel.equals("qvc.it")
				&& !channel.equals("gameroroso.it") && !channel.equals("alice.it") && !channel.equals("rainews24.it")
				&& !channel.equals("skytg24.it") && !channel.equals("tgnorba24.it") && !channel.equals("tgcom24.it")
				&& !channel.equals("skysport24.it") && !channel.equals("skysport1.it")
				&& !channel.equals("skysportf1.it") && !channel.equals("milantv.it") && !channel.equals("interts.it")
				&& !channel.equals("romatv.it") && !channel.equals("juventustv.it") && !channel.equals("supertennis.it")
				&& !channel.equals("sportitalia.it") && !channel.equals("raisport1.it")

				&& !channel.equals("skysportnews") && !channel.equals("nova24") && !channel.equals("goldtv")
				&& !channel.equals("hbo1.slo") && !channel.equals("hbo2.slo") && !channel.equals("hbo3.slo")
				&& !channel.equals("tv3medias") && !channel.equals("foxtv.slo") && !channel.equals("foxcrime.slo")
				&& !channel.equals("foxcrime.slo") && !channel.equals("foxlife.slo") && !channel.equals("foxmovies.slo")
				&& !channel.equals("cinestarpremiere1.slo") && !channel.equals("amc.slo")
				&& !channel.equals("tv1000.slo") && !channel.equals("24kitchen.slo") && !channel.equals("tlc.slo")
				&& !channel.equals("davinci.slo") && !channel.equals("travel.slo") && !channel.equals("travel.slo")
				&& !channel.equals("viasathis.slo") && !channel.equals("history.slo") && !channel.equals("natgeo.slo")
				&& !channel.equals("natgeowild.slo") && !channel.equals("animalplanet.slo")
				&& !channel.equals("ginx.slo") && !channel.equals("liverpooltv") && !channel.equals("mutv")
				&& !channel.equals("Italia 1") && !channel.equals("Italia 2") && !channel.equals("tgcom24.it")
				&& !channel.equals("comedycentral.de") && !channel.equals("skynostalgie.de")
				&& !channel.equals("planet.de") && !channel.equals("rbb.de") && !channel.equals("hr1.de")
				&& !channel.equals("daserste.de") && !channel.equals("zdf.de") && !channel.equals("zdfinfo.de")
				&& !channel.equals("mdr.de") && !channel.equals("mdr.de") && !channel.equals("wdr.de")
				&& !channel.equals("br.de") && !channel.equals("swr.de") && !channel.equals("3sat.de")
				&& !channel.equals("arte.de") && !channel.equals("ndr.de") && !channel.equals("rtl.de")
				&& !channel.equals("rtl2.de") && !channel.equals("rtlplus.de") && !channel.equals("rtlliving.de")
				&& !channel.equals("rtlpassion.de") && !channel.equals("rtlcrime.de") && !channel.equals("nitro.de")
				&& !channel.equals("vox.de") && !channel.equals("pro7.de") && !channel.equals("pro7maxx.de")
				&& !channel.equals("pro7fun.de") && !channel.equals("kabel1.de") && !channel.equals("kabel1classics.de")
				&& !channel.equals("kabel1doku.de") && !channel.equals("sat1.de") && !channel.equals("sat1gold.de")
				&& !channel.equals("sat1emotions.de") && !channel.equals("servustv.de") && !channel.equals("eotv.de")
				&& !channel.equals("sixx.de") && !channel.equals("ntv.de") && !channel.equals("tele5.de")
				&& !channel.equals("heimatkanal.de") && !channel.equals("sonnenklar.de")
				&& !channel.equals("eentertainment.de") && !channel.equals("atv.de") && !channel.equals("atv2.de")
				&& !channel.equals("puls4.de") && !channel.equals("orf1.de") && !channel.equals("orf2.de")
				&& !channel.equals("orf3.de") && !channel.equals("srf1.de") && !channel.equals("srf2.de")
				&& !channel.equals("skycinema.de") && !channel.equals("skycinema24.de") && !channel.equals("skyhits.de")
				&& !channel.equals("skyfamily.de") && !channel.equals("skyaction.de") && !channel.equals("skycomedy.de")
				&& !channel.equals("sky1.de") && !channel.equals("skyatlantic.de") && !channel.equals("skykrimi.de")
				&& !channel.equals("13thstreet.de") && !channel.equals("tntfilm.de") && !channel.equals("tntserie.de")
				&& !channel.equals("tntcomedy.de") && !channel.equals("axn.de") && !channel.equals("tlc.de")
				&& !channel.equals("fox.de") && !channel.equals("syfy.de") && !channel.equals("sonytv.de")
				&& !channel.equals("universalchannel.de") && !channel.equals("romancetv.de")
				&& !channel.equals("kinowelt.de") && !channel.equals("superrtl.de") && !channel.equals("toggoplus.de")
				&& !channel.equals("kika.de") && !channel.equals("babytv.de") && !channel.equals("disneychannel.de")
				&& !channel.equals("disneyjunior.de") && !channel.equals("cartoonnetwork.de")
				&& !channel.equals("cartoonnetwork.de") && !channel.equals("nickelodeon.de")
				&& !channel.equals("nickjr.de") && !channel.equals("n24doku.de")
				&& !channel.equals("spiegelgeschichte.de") && !channel.equals("spiegelwissen.de")
				&& !channel.equals("spiegelwissen.de") && !channel.equals("natgeo.de")
				&& !channel.equals("natgeowild.de") && !channel.equals("weltderwunder.de")
				&& !channel.equals("discoverychannel.de") && !channel.equals("historychannel.de")
				&& !channel.equals("animalplanet.de") && !channel.equals("dmax.de") && !channel.equals("anixe.de")
				&& !channel.equals("phoenix.de") && !channel.equals("mtvgermany.de") && !channel.equals("jukebox.de")
				&& !channel.equals("jukebox.de");

	}

	private boolean isExcludedOld(String channel) {
		// 193 channels
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
				&& !channel.equals("BTSPORT1") && !channel.equals("BTSPORT2") && !channel.equals("A1BIH")
				&& !channel.equals("ALFATV") && !channel.equals("ARENASPORT6") && !channel.equals("AURORATV")
				&& !channel.equals("AUTOMOTOTVIT") && !channel.equals("BABYTV") && !channel.equals("BANOVINA")
				&& !channel.equals("BBCEARTH") && !channel.equals("BDCTV") && !channel.equals("BELLEAMIE")
				&& !channel.equals("BNMUSIC") && !channel.equals("CCEXTRA") && !channel.equals("CITYTV")
				&& !channel.equals("DELUXEMUSIC") && !channel.equals("DISCOVERYSCI") && !channel.equals("DISCOVERYSHOW")
				&& !channel.equals("DOCUBOX") && !channel.equals("DOKUTV") && !channel.equals("DTX")
				&& !channel.equals("DTXDISCOVERY") && !channel.equals("DUGATV") && !channel.equals("ELTA")
				&& !channel.equals("ESPN") && !channel.equals("ESPN2") && !channel.equals("EUROCHANNEL")
				&& !channel.equals("FACE") && !channel.equals("FIGHTBOX") && !channel.equals("FIGHTCHANNELWORLD")
				&& !channel.equals("FILMKLUB") && !channel.equals("FILMKLUBEXTRA") && !channel.equals("HAYATFOLK")
				&& !channel.equals("HAYATMUSIC") && !channel.equals("HAYATOVCI") && !channel.equals("HAYATPLUS")
				&& !channel.equals("HBO") && !channel.equals("HITBRCKO") && !channel.equals("HRAM")
				&& !channel.equals("HRT5") && !channel.equals("IDINVESTIGATION") && !channel.equals("IDXTRA")
				&& !channel.equals("INTV") && !channel.equals("IZVORNATV") && !channel.equals("JIMJAM")
				&& !channel.equals("K3PRNJAVOR") && !channel.equals("KANAL3") && !channel.equals("KANAL6")
				&& !channel.equals("LOTELTV") && !channel.equals("LUXE") && !channel.equals("M1GOLD")
				&& !channel.equals("MEKATV") && !channel.equals("MEZZOLIVE") && !channel.equals("MOTORVISION")
				&& !channel.equals("MTVDANCE") && !channel.equals("MTVIGMAN") && !channel.equals("NASATVBA")
				&& !channel.equals("NATIONALGEOGRAPHIC") && !channel.equals("NOVA24SLO") && !channel.equals("NOVAWORLD")
				&& !channel.equals("NOVIPAZAR") && !channel.equals("NOVOSADSKATV") && !channel.equals("NTV101")
				&& !channel.equals("NTVJASMIN") && !channel.equals("ORBIS") && !channel.equals("ORFSPORTPLUS")
				&& !channel.equals("ORLANDO") && !channel.equals("OSMTV") && !channel.equals("OTVVALENTINO")
				&& !channel.equals("OUTDOOR") && !channel.equals("PICKBOX") && !channel.equals("PINKFOLK1")
				&& !channel.equals("PINKFOLK2") && !channel.equals("PINKKONCERT") && !channel.equals("PLANETSPORT3")
				&& !channel.equals("PLANETSPORT4") && !channel.equals("PLANETSPORT5") && !channel.equals("POSAVINATV")
				&& !channel.equals("RASTV") && !channel.equals("RTDOCUMENTARY") && !channel.equals("RTLWORLD")
				&& !channel.equals("RTRSPLUS") && !channel.equals("RTS3") && !channel.equals("RTSDRAMA")
				&& !channel.equals("RTSKOLO") && !channel.equals("RTSMUZIKA") && !channel.equals("RTSTREZOR")
				&& !channel.equals("RTSZIVOT") && !channel.equals("RTVBPK") && !channel.equals("RTVKISS")
				&& !channel.equals("RTVNIS") && !channel.equals("RTVSABAC") && !channel.equals("RTVVISOKO")
				&& !channel.equals("RTVZENICA") && !channel.equals("SEVDAHTV") && !channel.equals("SK4RS")
				&& !channel.equals("SK5") && !channel.equals("SK5RS") && !channel.equals("SK6")
				&& !channel.equals("SK6RS") && !channel.equals("SKYSPORT1") && !channel.equals("SKYSPORT2")
				&& !channel.equals("SKYSPORTF1") && !channel.equals("SKYSPORTSARENA")
				&& !channel.equals("SKYSPORTSFOOTBALL") && !channel.equals("SKYSPORTSMAIN") && !channel.equals("SLON")
				&& !channel.equals("SMARTTVBIH") && !channel.equals("SPORTKLUB") && !channel.equals("SPORTKLUB10")
				&& !channel.equals("SPORTKLUB4") && !channel.equals("SPORTKLUB5") && !channel.equals("SPORTKLUB6")
				&& !channel.equals("SPORTKLUB7") && !channel.equals("SPORTKLUB8") && !channel.equals("SPORTKLUB9")
				&& !channel.equals("SPORTTV1") && !channel.equals("SPORTTV2") && !channel.equals("SPORTTV3")
				&& !channel.equals("SRCE") && !channel.equals("TELEVIZIJA5") && !channel.equals("TOPTV")
				&& !channel.equals("TVIRISMK") && !channel.equals("TVKAKANJ") && !channel.equals("TVKISSMK")
				&& !channel.equals("TVMOST") && !channel.equals("TVSARAJEVO") && !channel.equals("TVVUJIC")
				&& !channel.equals("USK") && !channel.equals("VALENTINOETNO") && !channel.equals("VALENTINOMUSIC")
				&& !channel.equals("VASKANAL") && !channel.equals("VIKOM") && !channel.equals("ZENSKATV")
				&& !channel.equals("PINK3") && !channel.equals("PRVAPLUS") && !channel.equals("DISCOVERYWORLDIT")
				&& !channel.equals("STARTVTR") && !channel.equals("PINKHITS1") && !channel.equals("BLOOMBERG");

		// A1BIH,BOSNATV1,BOXNATION,BTSPORT1,BTSPORT2 uopce nema programa
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

	private boolean isSortedExcluded(String channel) {
		// 55 channels
		return !channel.equals("HRT1") && !channel.equals("HRT2") && !channel.equals("HRT3")
				&& !channel.equals("NOVATV") && !channel.equals("RTLTELEVIZIJA") && !channel.equals("RTL2")
				&& !channel.equals("DOMATV") && !channel.equals("RTLKOCKICA") && !channel.equals("HRT4");

	}

	private List<String> createCroChannelsList() {
		List<String> croChannels = new ArrayList<String>();
		croChannels.add("HRT1");
		croChannels.add("HRT2");
		croChannels.add("HRT3");
		croChannels.add("HRT4");
		croChannels.add("NOVATV");
		croChannels.add("RTLTELEVIZIJA");
		croChannels.add("RTL2");
		croChannels.add("RTLKOCKICA");
		croChannels.add("DOMATV");
		return croChannels;
	}

}
/*
 * </programme><programme start="20200605080000 +0200"
 * stop="20200605090000 +0200" channel="FACETV"> <title lang="hr">Muzicki
 * program</title> <desc lang="hr"> Category: Glazba. </desc> <desc lang="hr"/>
 * </programme>
 */