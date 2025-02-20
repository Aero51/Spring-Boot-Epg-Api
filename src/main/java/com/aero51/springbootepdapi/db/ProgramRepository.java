package com.aero51.springbootepdapi.db;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.output.OutputProgram;

public interface ProgramRepository extends CrudRepository<OutputProgram, Integer> {
	List<OutputProgram> findBychannel(String channel);

	List<OutputProgram> findBychannelIn(List<String> channel);

	@Cacheable("program")
	List<OutputProgram> findBychannelOrderByIdAsc(String channel);

	List<OutputProgram> findByChannelInOrderByIdAsc(List<String> channel);
}
