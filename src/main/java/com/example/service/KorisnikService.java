package com.example.service;

import java.util.List;

import com.example.model.korisnik.Korisnik;

public interface KorisnikService {

	Korisnik findByUsername(String username) throws Exception;
	
	Korisnik create(Korisnik korisnik) throws Exception;
	
	Korisnik update(Korisnik korisnik) throws Exception;
	
	void delete(String username) throws Exception;
	
	Korisnik getLoggedUser();
	
	void login(String username, String password);
}
