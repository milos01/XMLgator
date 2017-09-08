package com.example.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.RezultatiGlasanjaDTO;
import com.example.model.amandman.Amandman;
import com.example.model.korisnik.Korisnik;
import com.example.service.AmandmanService;
import com.example.service.KorisnikService;

@SuppressWarnings("rawtypes")
@RestController
@RequestMapping(value = "/api/amandman")
public class AmendmentController {

	
	
	@Autowired
	private AmandmanService amandmanService;
	
	@Autowired
	private KorisnikService korisnikService; 
	
	@RequestMapping(value = "/predlozi", method = RequestMethod.POST, consumes = "application/xml")
	public ResponseEntity<Amandman> predlozi(@RequestBody Amandman amandman) throws Exception{
		Amandman created = amandmanService.predlozi(amandman, korisnikService.getLoggedUser());
		if (created != null)
			return new ResponseEntity<>(created, HttpStatus.CREATED);
		else
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value = "/amandmaniZaAkt/{id}", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> amandmaniZaAkt(@PathVariable("id") String id){
		return new ResponseEntity<>(amandmanService.amandmaniZaAkt(id), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/html")
	public ResponseEntity<String> findOne(@PathVariable("id") String id) throws Exception{
		String html = amandmanService.getOneXHTML(id);
		return new ResponseEntity<>(html, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/sviMojiAmandmani", method = RequestMethod.GET)
	public ResponseEntity<String> sviMojiAmandmani(){
		Korisnik korisnik = korisnikService.getLoggedUser();
		return new ResponseEntity<>(amandmanService.sviMojiAmandmani(korisnik), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/povuciAmandman/{id}", method = RequestMethod.GET)
	public ResponseEntity povuciAmandman(@PathVariable("id") String id) throws Exception{
		amandmanService.povuci(id, korisnikService.getLoggedUser());
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/pdf/{id}", method = RequestMethod.GET)
	public void findOnePDF(@PathVariable("id") String id, HttpServletResponse response) throws Exception {
		try {
			amandmanService.getOnePDF(id, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/findAll", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> findAll(){
		String lista = amandmanService.findAll();
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/findAllXML", method = RequestMethod.GET, produces = "application/xml")
	public ResponseEntity<String> findAllXML(){
		return new ResponseEntity<>(amandmanService.findAllXML(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/glasaj", method = RequestMethod.POST)
	public ResponseEntity glasaj(@RequestBody RezultatiGlasanjaDTO rezultati) throws Exception{
		amandmanService.glasajZaAmandman(rezultati);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
