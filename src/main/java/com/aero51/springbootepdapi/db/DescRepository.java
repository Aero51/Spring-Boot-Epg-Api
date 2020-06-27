package com.aero51.springbootepdapi.db;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.Desc;

public interface DescRepository extends CrudRepository<Desc, Integer> {

	List<Desc> findBychannel(String channel);

}
