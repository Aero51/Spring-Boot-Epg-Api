package com.aero51.springbootepdapi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.aero51.springbootepdapi.db.OutputChannelListRepository;
import com.aero51.springbootepdapi.db.ProgramRepository;
import com.aero51.springbootepdapi.model.output.OutputChannel;
import com.aero51.springbootepdapi.model.output.OutputProgram;

@RestController
public class EpgRestController {
	// @Autowired // Let Spring inject the queue

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	// @Autowired
	// private DescRepository descRepo;
	@Autowired
	private OutputChannelListRepository channelsRepo;
	@Autowired
	private ProgramRepository programRepo;
	@Autowired
	private EpgService epgService;

	private List<String> croChannels = createCroChannelsList();

	// @GetMapping("/greeting")
	// public Greeting greeting(@RequestParam(value = "name", defaultValue =
	// "World") String name) {
	// return new Greeting(counter.incrementAndGet(), String.format(template,
	// name));
	// }
	// @RequestMapping
	// @GetMapping("/greeting")
	@RequestMapping(value = "/greeting", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTv() {

		return "Hello";
	}

	/*
	 * @RequestMapping(value = "/descriptions", method = RequestMethod.GET, produces
	 * = MediaType.APPLICATION_JSON_VALUE) public List<Desc> getDesc() { List<Desc>
	 * descList = new ArrayList<Desc>(); descRepo.findAll().forEach(descList::add);
	 * return descList; }
	 */
	/*
	 * @RequestMapping(value = "/channel/{channel_id}", method = RequestMethod.GET,
	 * produces = MediaType.APPLICATION_JSON_VALUE) public List<Desc>
	 * getChannels(@PathVariable("channel_id") String channel_id) { List<Desc>
	 * descList = new ArrayList<Desc>(); descList =
	 * descRepo.findBychannel(channel_id); // channelsRepo.fin //
	 * channelsRepo.findAll().forEach(channelList::add); return descList; }
	 */
	@RequestMapping(value = "/channels", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OutputChannel> getChannels() {
		// List<OutputChannel> channelsList = new ArrayList<OutputChannel>();
		// channelsRepo.findAll().forEach(channelsList::add);
		return channelsRepo.findAllByOrderByIdAsc();
	}

	@RequestMapping(value = "/program/{channel_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OutputProgram> getPrograms(@PathVariable("channel_id") String channel_id) {
		return programRepo.findBychannelOrderByIdAsc(channel_id);
	}

	@RequestMapping(value = "/programs/cro", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OutputProgram> getCroPrograms() {
		return programRepo.findByChannelInOrderByIdAsc(croChannels);
	}

	@RequestMapping(value = "/programs/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<OutputProgram> getAllPrograms() {
		List<OutputProgram> programsList = new ArrayList<OutputProgram>();
		programRepo.findAll().forEach(programsList::add);
		return programsList;

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
