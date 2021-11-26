package com.formacionjava.springboot.apirest.models.dao;

import org.springframework.data.repository.CrudRepository;

import com.formacionjava.springboot.apirest.models.entity.Region;

public interface RegionDao extends CrudRepository<Region, Long>{

}
