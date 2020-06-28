package com.aero51.springbootepdapi.db;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.pubproxy.Data;

public interface PubProxyDataRepository extends CrudRepository<Data, Integer> {

}
