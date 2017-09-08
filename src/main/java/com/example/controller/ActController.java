package com.example.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.PretragaDTO;
import com.example.dto.RezultatiGlasanjaDTO;
import com.example.model.akt.Akt;
import com.example.model.korisnik.Korisnik;
import com.example.service.AktService;
import com.example.service.KorisnikService;


@SuppressWarnings("rawtypes")
@RestController
@RequestMapping(value = "/api/akt")
public class ActController {

	@Autowired 
	private AktService aktService;
	
	@Autowired
	private KorisnikService korisnikService; 
	
	@RequestMapping(value = "/predlozi", method = RequestMethod.POST, consumes = "application/xml")
	public ResponseEntity<Akt> predlozi(@RequestBody Akt akt) throws Exception, Exception{
		Akt created = aktService.predlozi(akt, korisnikService.getLoggedUser());
		if (created != null)
			return new ResponseEntity<>(created, HttpStatus.CREATED);
		else
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value = "/findAll", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> findAll(){
		String lista = aktService.findAll();
		return new ResponseEntity<>(lista, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/findAllXML", method = RequestMethod.GET, produces = "application/xml")
	public ResponseEntity<String> findAllXML(){
		return new ResponseEntity<>(aktService.findAllXML(), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = "application/html")
	public ResponseEntity<String> findOne(@PathVariable("id") String id) throws Exception{
		String html = aktService.getOneXHTML(id);
		return new ResponseEntity<>(html, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/xml/{id}", method = RequestMethod.GET, produces = "application/xml")
	public ResponseEntity<Akt> findOneXml(@PathVariable("id") String id) throws Exception{
		Akt akt = aktService.findOne(id);
		return new ResponseEntity<>(akt, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/pdf/{id}", method = RequestMethod.GET)
	public void findOnePDF(@PathVariable("id") String id, HttpServletResponse response) throws Exception {
		try {
			aktService.getOnePDF(id, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/povuci/{id}", method = RequestMethod.GET)
	public ResponseEntity povuci(@PathVariable("id") String id) throws Exception{
		aktService.povuci(id, korisnikService.getLoggedUser());
		return new ResponseEntity(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/findMyAkts", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> findMyAkts(){
		Korisnik korisnik = korisnikService.getLoggedUser();
		return new ResponseEntity<>(aktService.findMyAkts(korisnik), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/findByParameters", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<String> findByParameters(@RequestBody PretragaDTO pretraga){
		return new ResponseEntity<>(aktService.findByParameters(pretraga), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/findByText", method = RequestMethod.POST, produces = "application/json")
	public ResponseEntity<String> findByText(@RequestBody String text){
		return new ResponseEntity<>(aktService.findByText(text), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/naziv/{id}", method = RequestMethod.GET)
	public ResponseEntity<String> getNaziv(@PathVariable("id") String id) throws Exception{
		return new ResponseEntity<>(aktService.getNaziv(id), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/naziviPoIDu", method = RequestMethod.POST)
	public ResponseEntity<String> naziviPoIDu(@RequestBody List<String> ids){
		return new ResponseEntity<>(aktService.naziviPoIDu(ids), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/glasaj", method = RequestMethod.POST)
	public ResponseEntity glasaj(@RequestBody RezultatiGlasanjaDTO rezultati) throws Exception{
		aktService.glasajZaAkt(rezultati);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
