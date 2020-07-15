package com.aero51.springbootepdapi.db;

import org.springframework.data.repository.CrudRepository;

import com.aero51.springbootepdapi.model.gimmeproxy.GimmeProxyResponseModel;

public interface GimmeProxyDataRepository extends CrudRepository<GimmeProxyResponseModel, Integer> {

}
