package com.aero51.springbootepdapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aero51.springbootepdapi.db.DescRepository;

@Service
public class EpgService {

	@Autowired
	private DescRepository databaseRepository;

}
