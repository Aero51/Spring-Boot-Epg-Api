package com.aero51.springbootepdapi.db;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.Channel;

public interface ChannelRepository extends CrudRepository<Channel, Integer> {

}
