package com.aero51.springbootepdapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.aero51.springbootepdapi.db.ChannelRepository;
import com.aero51.springbootepdapi.db.DescRepository;
import com.aero51.springbootepdapi.db.ProgramRepository;
import com.aero51.springbootepdapi.model.Category;
import com.aero51.springbootepdapi.model.Channel;
import com.aero51.springbootepdapi.model.Desc;
import com.aero51.springbootepdapi.model.Program;
import com.aero51.springbootepdapi.model.Programme;
import com.aero51.springbootepdapi.model.Tv;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Component
public class InitialLoadApplicationRunner implements ApplicationRunner {

	@Autowired
	private DescRepository descRepo;
	@Autowired
	private ChannelRepository channelsRepo;
	@Autowired
	private ProgramRepository programRepo;
	@Autowired
	private DownloadEpgService service;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("InitialLoad");
		// Thread.sleep(5000);
		initiateDownload();
		System.out.println("InitialLoad complete");
	}

	private void initiateDownload() {
		TheMovieDbApi api = RetrofitInstance.getApiService();
		Call<Tv> call = api.getEpg();
		call.enqueue(new Callback<Tv>() {
			@Override
			public void onResponse(Call<Tv> call, Response<Tv> response) {

				System.out.println("Response ok: " + response.code() + " ,:" + response.body().getProgramme().size());

				try {

				} catch (ArrayIndexOutOfBoundsException e) {
					e.printStackTrace();
				}
				//

				// }
				// textView.setText(program);
				saveTodb(response);

			}

			@Override
			public void onFailure(Call<Tv> call, Throwable t) {
				System.out.println(t.getMessage());
			}
		});

	}

	private void saveTodb(Response<Tv> response) {

//		@JsonInclude()
//		@Transient
		Tv tv = response.body();
		List<Channel> channelList = tv.getChannel();
		for (Channel channel : channelList) {
			channel.setDisplay_name(channel.getDisplayName().getContent());

		}
		channelsRepo.saveAll(channelList);

		List<Programme> programmeList = tv.getProgramme();

		List<Program> programList = new ArrayList<Program>();
		for (Programme programme : programmeList) {

			Program program = new Program();
			program.setChannel(programme.getChannel());
			program.setTitle(programme.getTitle().getContent());
			program.setStart(programme.getStart());
			program.setStop(programme.getStop());
			if (programme.getSubTitle() != null) {
				program.setSubTitle(programme.getSubTitle().getContent());
			}

			program.setDate(programme.getDate());
			if (programme.getIcon() != null) {
				program.setIcon(programme.getIcon().getSrc());
			}

			if (programme.getDesc().size() > 0) {
				// programmeList.get(i).getDesc().size() > 0
				program.setDesc(programme.getDesc().get(0).getContent());
			} else {
				program.setDesc("Opis nije dostupan");
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
					program.setCredits(json);
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
					program.setCategory(json);
				} catch (JsonProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

			programList.add(program);
		}
		programRepo.saveAll(programList);

		List<Desc> descList = new ArrayList<>();
		for (Programme programme : programmeList) {
			if (programme.getDesc().size() > 0) {
				// programmeList.get(i).getDesc().size() > 0
				Desc desc = programme.getDesc().get(0);
				desc.setChannel(programme.getChannel());
				descList.add(desc);

			} else {
				Desc desc = new Desc();
				desc.setContent("Opis nije dostupan");
				// desc.setLang("hr");
				descList.add(desc);
			}

		}
		System.out.println("before insert");
		// descRepo.saveAll(descList);
		System.out.println("after insert");

	}
}
/*
 * </programme><programme start="20200605080000 +0200"
 * stop="20200605090000 +0200" channel="FACETV"> <title lang="hr">Muzièki
 * program</title> <desc lang="hr">¤ Category: Glazba. </desc> <desc lang="hr"/>
 * </programme>
 */