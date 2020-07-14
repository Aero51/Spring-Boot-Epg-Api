package com.aero51.springbootepdapi.db;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.output.OutputChannel;

public interface OutputChannelListRepository extends CrudRepository<OutputChannel, Integer> {
	List<OutputChannel> findByName(String name);

	// OutputChannel findByName(String name);
	List<OutputChannel> findByNameIn(List<String> name);

	List<OutputChannel> findAllByOrderByIdAsc();

}
