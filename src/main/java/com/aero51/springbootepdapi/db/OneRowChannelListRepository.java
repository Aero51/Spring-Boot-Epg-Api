package com.aero51.springbootepdapi.db;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.input.Channel;
import com.aero51.springbootepdapi.model.output.OutputChannel;

public interface OneRowChannelListRepository extends CrudRepository<OutputChannel, Integer> {

}
