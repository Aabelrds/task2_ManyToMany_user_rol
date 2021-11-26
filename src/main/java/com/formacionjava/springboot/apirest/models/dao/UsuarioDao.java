package com.formacionjava.springboot.apirest.models.dao;


import org.springframework.data.repository.CrudRepository;

import com.formacionjava.springboot.apirest.models.entity.Usuario;


public interface UsuarioDao extends CrudRepository<Usuario, Long>{
	
	/*
	public Usuario findByUserName(String username);
	
	@Query("select u from Usuario u where u.userName=?")
	public Usuario findByUserName2(String username);
	*/
}
