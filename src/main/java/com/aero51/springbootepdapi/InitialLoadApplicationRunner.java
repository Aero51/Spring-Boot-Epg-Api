package com.aero51.springbootepdapi;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.aero51.springbootepdapi.model.input.Category;
import com.aero51.springbootepdapi.model.input.Channel;
import com.aero51.springbootepdapi.model.input.Programme;
import com.aero51.springbootepdapi.model.input.Tv;
import com.aero51.springbootepdapi.model.output.OutputChannel;
import com.aero51.springbootepdapi.model.output.OutputProgram;
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

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("InitialLoad");
		initiateEpgDownload();
		System.out.println("InitialLoad complete");
	}

	private void initiateEpgDownload() {

		System.out.println("initiateEpgDownload ");
		RetrofitApi epdRetrofitApi = RetrofitInstance.getEpdApi();
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
					if (epgFailcount < 11) {
						initiateEpgDownload();
					} else {
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
				if (epgFailcount < 11) {
					initiateEpgDownload();
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

		System.out.println("number of channels after process unsorted: " + unsortedChannelList.size());

	}

	private void processProgrammes(List<Programme> programmeList) {
		System.out.println("number of programs before process: " + programmeList.size());
		List<OutputProgram> outputProgramList = new ArrayList<OutputProgram>();
		SimpleDateFormat fromUser = new SimpleDateFormat("yyyyMMddHHmmSS");
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
					String category = "";
					for (Category cat : categories) {
						// content.add(category.getContent());
						category = category + cat.getContent() + " ";
					}
					outputProgram.setCategory(category);

					/*
					 * Map<String, List<String>> map = new HashMap<>(); map.put("Category",
					 * content);
					 * 
					 * ObjectMapper gson = new ObjectMapper(); try { String json =
					 * gson.writeValueAsString(map); outputProgram.setCategory(json); } catch
					 * (JsonProcessingException e) {
					 * System.out.println("Category converting to json error! ");
					 * e.printStackTrace(); }
					 */
				} else {/*
						 * Map<String, List<String>> map = new HashMap<>(); ArrayList<String> cat = new
						 * ArrayList<>(); cat.add("Bez kategorije"); map.put("Category", cat);
						 * ObjectMapper gson = new ObjectMapper(); try { String json =
						 * gson.writeValueAsString(map); outputProgram.setCategory(json); } catch
						 * (JsonProcessingException e) {
						 * System.out.println("Category Bez kategorije converting to json error! ");
						 * e.printStackTrace(); }
						 */
					outputProgram.setCategory("");
				}

				Date startDate = null;
				Date stopDate = null;
				Date currentDate = null;
				try {
					startDate = fromUser.parse(programme.getStart());
					stopDate = fromUser.parse(programme.getStop());

				} catch (ParseException e) {
					e.printStackTrace();
				}
				long startTime = startDate.getTime();
				long stopTime = stopDate.getTime();
				long diff = stopTime - startTime;
				long diffMinutes = diff / (60 * 1000);
				outputProgram.setProgramDuration((int) diffMinutes);
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
		return !channel.equals("hrt5") && !channel.equals("pinkextra") && !channel.equals("ducktv")
				&& !channel.equals("ginx")
				// empty
				&& !channel.equals("dtx") && !channel.equals("discoveryshow") && !channel.equals("ccextra")
				&& !channel.equals("idinvestigation") && !channel.equals("idxtra") && !channel.equals("bbcearth")
				&& !channel.equals("banovina") && !channel.equals("skysportaustria") && !channel.equals("mtvdance")
				&& !channel.equals("mtvrocks") && !channel.equals("thevoice") && !channel.equals("deluxemusic")
				&& !channel.equals("bnmusic") && !channel.equals("hayatmusic") && !channel.equals("hayatfolk")
				&& !channel.equals("grand1") && !channel.equals("grand2") && !channel.equals("idjtv")
				&& !channel.equals("pinkfolk1") && !channel.equals("pinkfolk2") && !channel.equals("pinknroll")
				&& !channel.equals("pinkmusic") && !channel.equals("ultra") && !channel.equals("miniultra")
				&& !channel.equals("hayatovci") && !channel.equals("orlando") && !channel.equals("jimjam")
				&& !channel.equals("babytv") && !channel.equals("hayatplus") && !channel.equals("face")
				&& !channel.equals("elta") && !channel.equals("tvsarajevo") && !channel.equals("alfatv")
				&& !channel.equals("rtvkiss") && !channel.equals("ntv101") && !channel.equals("mtvigman")
				&& !channel.equals("rtvkiss") && !channel.equals("slon") && !channel.equals("rtvzenica")
				&& !channel.equals("usk") && !channel.equals("bosnatv1") && !channel.equals("televizija5")
				&& !channel.equals("rts3") && !channel.equals("rtrsplus") && !channel.equals("prvaplus")
				&& !channel.equals("nova.rs") && !channel.equals("kcn1") && !channel.equals("studiob")
				&& !channel.equals("nova.rs") && !channel.equals("kcn1") && !channel.equals("novipazar")
				&& !channel.equals("palmaplus") && !channel.equals("srpskanaucna") && !channel.equals("soskanal")
				&& !channel.equals("rtcgSat") && !channel.equals("pink3") && !channel.equals("pinkbravo")
				&& !channel.equals("pinkkoncert") && !channel.equals("sporttv1") && !channel.equals("sporttv2")
				&& !channel.equals("sporttv3") && !channel.equals("sk4.rs") && !channel.equals("sk5.rs")
				&& !channel.equals("sk9.rs") && !channel.equals("sk8.rs") && !channel.equals("sk7.rs")

				&& !channel.equals("sk6.rs") && !channel.equals("sport1") && !channel.equals("skysportsmain")
				&& !channel.equals("skysportsarena") && !channel.equals("skysportsfootball")
				&& !channel.equals("skysport1") && !channel.equals("skysport2") && !channel.equals("skysportf1")
				&& !channel.equals("fightchannelworld") && !channel.equals("movistarmotogp")
				&& !channel.equals("topchannel") && !channel.equals("vizionplus") && !channel.equals("rtsh")
				&& !channel.equals("koha") && !channel.equals("era") && !channel.equals("klantv")
				&& !channel.equals("tiptv") && !channel.equals("albmusic") && !channel.equals("filmhits")
				&& !channel.equals("klanplus") && !channel.equals("filmhits1") && !channel.equals("filmkomedi")
				&& !channel.equals("tvshenja") && !channel.equals("tringworld") && !channel.equals("tringsuper")
				&& !channel.equals("tringlife") && !channel.equals("tringcomedy") && !channel.equals("tringmax")
				&& !channel.equals("tringfantazi") && !channel.equals("tringplanet") && !channel.equals("tringshqip")
				&& !channel.equals("tringtring") && !channel.equals("tringjolly") && !channel.equals("tring3plus")
				&& !channel.equals("digigold1") && !channel.equals("vikom") && !channel.equals("tvkakanj")
				&& !channel.equals("bdctv") && !channel.equals("citytv") && !channel.equals("mekatv")
				&& !channel.equals("posavinatv") && !channel.equals("izvornatv") && !channel.equals("otvvalentino")
				&& !channel.equals("rtsdrama") && !channel.equals("belleamie") && !channel.equals("kanal3")
				&& !channel.equals("comedy.bg") && !channel.equals("moviestar.bg") && !channel.equals("action.bg")
				&& !channel.equals("karusel") && !channel.equals("outdoor") && !channel.equals("luxe")
				&& !channel.equals("worldfashion") && !channel.equals("dugatv") && !channel.equals("valentinomusic")
				&& !channel.equals("classica") && !channel.equals("tracesportstars") && !channel.equals("valentinoetno")
				&& !channel.equals("mezzolive") && !channel.equals("motorsporttv") && !channel.equals("srce")
				&& !channel.equals("pinkhits1") && !channel.equals("pinkhits2") && !channel.equals("pinkmusic2")
				&& !channel.equals("rtshd") && !channel.equals("popoto") && !channel.equals("popbrio")
				&& !channel.equals("popkino") && !channel.equals("nettv") && !channel.equals("planet")
				&& !channel.equals("golica") && !channel.equals("koper") && !channel.equals("sk1.slo")
				&& !channel.equals("sk2.slo") && !channel.equals("sk3.slo") && !channel.equals("sk4.slo")
				&& !channel.equals("rtsmaribor") && !channel.equals("edgesport") && !channel.equals("motorvision")
				&& !channel.equals("rtszivot") && !channel.equals("rtstrezor") && !channel.equals("rtvnis")
				&& !channel.equals("intv") && !channel.equals("rtvvisoko") && !channel.equals("nasatv.ba")
				&& !channel.equals("osmtv") && !channel.equals("kanal6") && !channel.equals("mrt1")
				&& !channel.equals("mrt2") && !channel.equals("sobranskikanal") && !channel.equals("telma")
				&& !channel.equals("24vesti") && !channel.equals("sitel") && !channel.equals("kanal5")
				&& !channel.equals("kanal5plus") && !channel.equals("alfatv.mk") && !channel.equals("tvnova.mk")
				&& !channel.equals("nasatv.mk") && !channel.equals("mtm") && !channel.equals("tera")
				&& !channel.equals("orbis") && !channel.equals("sonce") && !channel.equals("tvvizija.mk")
				&& !channel.equals("edo") && !channel.equals("alsatm") && !channel.equals("hearttv")
				&& !channel.equals("retrotv") && !channel.equals("nrjhits") && !channel.equals("sevdahtv")
				&& !channel.equals("novaworld") && !channel.equals("rtlworld") && !channel.equals("orfsportplus")
				&& !channel.equals("chelseatv") && !channel.equals("sevillatv") && !channel.equals("insajdertv")
				&& !channel.equals("rtvbpk") && !channel.equals("ntvjasmin") && !channel.equals("k3prnjavor")
				&& !channel.equals("smarttvbih") && !channel.equals("a1bih") && !channel.equals("rtskolo")
				&& !channel.equals("rtsmuzika") && !channel.equals("novosadskatv") && !channel.equals("loteltv")
				&& !channel.equals("eurochannel") && !channel.equals("tvmost") && !channel.equals("rtvsabac")
				&& !channel.equals("tvvujic") && !channel.equals("rastv") && !channel.equals("tvkiss.mk")
				&& !channel.equals("tviris.mk") && !channel.equals("nova24.slo") && !channel.equals("vaskanal")
				&& !channel.equals("m1gold") && !channel.equals("cbsdrama") && !channel.equals("rtdocumentary")
				&& !channel.equals("espn") && !channel.equals("espn2") && !channel.equals("btsport1")
				&& !channel.equals("btsport2") && !channel.equals("boxnation") && !channel.equals("wwe")
				&& !channel.equals("citytv") && !channel.equals("hram") && !channel.equals("planetsport3")
				&& !channel.equals("planetsport4") && !channel.equals("planetsport5")

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
				&& !channel.equals("raiscuola.it") && !channel.equals("primafila10.it")
				&& !channel.equals("primafila9.it") && !channel.equals("primafila8.it")
				&& !channel.equals("primafila7.it") && !channel.equals("primafila6.it")
				&& !channel.equals("primafila5.it") && !channel.equals("primafila4.it")
				&& !channel.equals("primafila3.it") && !channel.equals("primafila2.it")
				&& !channel.equals("primafila1.it")

				&& !channel.equals("skysportnews") && !channel.equals("nova24") && !channel.equals("goldtv")

				&& !channel.equals("hbo1.slo") && !channel.equals("hbo2.slo") && !channel.equals("hbo3.slo")
				&& !channel.equals("tv3medias") && !channel.equals("foxtv.slo") && !channel.equals("foxcrime.slo")
				&& !channel.equals("foxcrime.slo") && !channel.equals("foxlife.slo") && !channel.equals("foxmovies.slo")
				&& !channel.equals("cinestarpremiere1.slo") && !channel.equals("amc.slo")
				&& !channel.equals("tv1000.slo") && !channel.equals("24kitchen.slo") && !channel.equals("tlc.slo")
				&& !channel.equals("davinci.slo") && !channel.equals("travel.slo") && !channel.equals("travel.slo")
				&& !channel.equals("viasathis.slo") && !channel.equals("history.slo") && !channel.equals("natgeo.slo")
				&& !channel.equals("natgeowild.slo") && !channel.equals("animalplanet.slo")
				&& !channel.equals("ginx.slo") && !channel.equals("slo1") && !channel.equals("slo2")
				&& !channel.equals("slo3") && !channel.equals("poptv") && !channel.equals("akanal")
				&& !channel.equals("tvmaribor.slo")

				&& !channel.equals("liverpooltv") && !channel.equals("mutv") && !channel.equals("Italia 1")
				&& !channel.equals("Italia 2") && !channel.equals("tgcom24.it")

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
				&& !channel.equals("jukebox.de") && !channel.equals("viva.de") && !channel.equals("jukebox.de")
				&& !channel.equals("blizz.de") && !channel.equals("zeeone.de") && !channel.equals("skycinema1.de")
				&& !channel.equals("skyemotion.de") && !channel.equals("skywestern.de") && !channel.equals("skyarts.de")
				&& !channel.equals("disneycinemagic.de") && !channel.equals("disneyxd.de")
				&& !channel.equals("boomerang.de") && !channel.equals("anixeserie.de") && !channel.equals("welt.de")
				&& !channel.equals("ardone")

				// not empty
				&& !channel.equals("oyocinema") && !channel.equals("oyofamily") && !channel.equals("zdravatv")
				&& !channel.equals("cinestartv.rs") && !channel.equals("cinestaraction.rs")
				&& !channel.equals("cinestarpremiere1.rs") && !channel.equals("cinestarpremiere2.rs")
				&& !channel.equals("cinestarfantasy.rs") && !channel.equals("cinestarcomedy.rs")

				&& !channel.equals("24kitchen") && !channel.equals("foodnetwork") && !channel.equals("docubox")
				&& !channel.equals("natgeors") && !channel.equals("davinci") && !channel.equals("fashion")

				&& !channel.equals("balkanika") && !channel.equals("davinci") && !channel.equals("fashion")
				&& !channel.equals("zapadtv") && !channel.equals("planetsport2") && !channel.equals("planetsport1")

				// not empty music channels

				&& !channel.equals("mtvadria") && !channel.equals("mtvhits") && !channel.equals("mtvclassic")
				&& !channel.equals("vh1") && !channel.equals("vh1classic") && !channel.equals("magictv")
				&& !channel.equals("dmsat") && !channel.equals("mezzo") && !channel.equals("narodnatv")
				&& !channel.equals("topsound") && !channel.equals("pinkextra") && !channel.equals("hitmix")

				// children not empty
				&& !channel.equals("nickelodeon.rs") && !channel.equals("pinksuper") && !channel.equals("minimax")
				&& !channel.equals("pinkkids")
				// ostalo not empty
				&& !channel.equals("n1") && !channel.equals("n1.ba") && !channel.equals("n1.rs")
				&& !channel.equals("tv1") && !channel.equals("alternativna") && !channel.equals("a1balkan")
				&& !channel.equals("prva.cg") && !channel.equals("prvamax") && !channel.equals("prvaworld")
				&& !channel.equals("o2") && !channel.equals("kcn2") && !channel.equals("kcn3")
				&& !channel.equals("vijesticg") && !channel.equals("777") && !channel.equals("pinkkuvar")
				&& !channel.equals("hntv") && !channel.equals("arena1.rs") && !channel.equals("arena2.rs")
				&& !channel.equals("arena3.rs") && !channel.equals("arena4.rs") && !channel.equals("arena5.rs")
				&& !channel.equals("sk5") && !channel.equals("sk6") && !channel.equals("sk1.rs")
				&& !channel.equals("sk2.rs") && !channel.equals("sk3.rs") && !channel.equals("skgolf")
				&& !channel.equals("tdc") && !channel.equals("disneyjunior.hr") && !channel.equals("ringbg")
				&& !channel.equals("parovi1") && !channel.equals("zadruga1") && !channel.equals("zadruga2")
				&& !channel.equals("zadruga3") && !channel.equals("zadruga4")

				// albanian channels
				&& !channel.equals("diemasport1") && !channel.equals("rtv21") && !channel.equals("21popullore")
				&& !channel.equals("rtk") && !channel.equals("oranews") && !channel.equals("ktv")
				&& !channel.equals("abcnews") && !channel.equals("klankosova") && !channel.equals("bangbang")
				&& !channel.equals("filmthriller") && !channel.equals("filmnje") && !channel.equals("filmaksion")
				&& !channel.equals("filmdrame") && !channel.equals("filmdy") && !channel.equals("expshkence")
				&& !channel.equals("expnatyra") && !channel.equals("tiankovmusic") && !channel.equals("moviestar.bg")
				&& !channel.equals("film.bg")

				&& !channel.equals("tiankovmusic") && !channel.equals("moviestar.bg") && !channel.equals("film.bg")
				&& !channel.equals("pink2") && !channel.equals("pinkpedia") && !channel.equals("pinkromance")
				&& !channel.equals("pinkscifi") && !channel.equals("diemasport2") && !channel.equals("hram")

				&& !channel.equals("hitbrcko") && !channel.equals("tvnovapula") && !channel.equals("jabukatv")
				&& !channel.equals("topkanal") && !channel.equals("discoveryturbo") && !channel.equals("nbatv")
				&& !channel.equals("mtvbrandnew") && !channel.equals("france24") && !channel.equals("france24.en")
				&& !channel.equals("france24.fr");

	}

}
