package com.aero51.springbootepdapi.db;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.Program;

public interface ProgramRepository extends CrudRepository<Program, Integer> {
	List<Program> findBychannel(String channel);
}
