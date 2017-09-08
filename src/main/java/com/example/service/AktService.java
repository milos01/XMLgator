package com.example.service;

import java.io.OutputStream;
import java.util.List;

import javax.validation.ValidationException;

import com.example.dto.PretragaDTO;
import com.example.dto.RezultatiGlasanjaDTO;
import com.example.model.akt.Akt;
import com.example.model.korisnik.Korisnik;

public interface AktService {

    Akt predlozi(Akt akt, Korisnik korisnik) throws Exception, ValidationException;
	
	void delete(String id) throws Exception;
	
	Akt update(Akt akt) throws Exception;
	
	Akt findOne(String id) throws Exception;
	
	String getOneXHTML(String id) throws Exception;
	
	void getOnePDF(String id, OutputStream os) throws Exception;
	
	String findAll();
	
	String findAllXML();
	
	void povuci(String id, Korisnik korisnik) throws Exception;
	
	String findMyAkts(Korisnik korisnik);
	
	String findByParameters(PretragaDTO pretraga);
	
	String findByText(String text);
	
	String getNaziv(String id) throws Exception;
	
	String naziviPoIDu(List<String> ids);
	
	void promeniElement(String path, String aktId, Object replacement);
	
	void dodajElement(String path, String aktId, Object inserted);
	
	void obrisiElement(String path, String aktId);
	
	void glasajZaAkt(RezultatiGlasanjaDTO rezultati) throws Exception;
}
