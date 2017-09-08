package com.example.service;

import java.io.OutputStream;

import com.example.dto.RezultatiGlasanjaDTO;
import com.example.model.amandman.Amandman;
import com.example.model.korisnik.Korisnik;

public interface AmandmanService {

	Amandman predlozi(Amandman amandman, Korisnik korisnik) throws Exception;
	
	String amandmaniZaAkt(String aktId);
	
	String getOneXHTML(String id) throws Exception;
	
	Amandman findOne(String id) throws Exception;
	
	String sviMojiAmandmani(Korisnik korisnik);
	
	void povuci(String id, Korisnik korisnik) throws Exception;
	
	void getOnePDF(String id, OutputStream os) throws Exception;
	
	String findAll();
	
	String findAllXML();
	
	void glasajZaAmandman(RezultatiGlasanjaDTO rezultati) throws Exception;
}
