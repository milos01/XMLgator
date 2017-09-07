package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.KorisnikDTO;
import com.example.dto.LoginDTO;
import com.example.model.korisnik.Korisnik;
import com.example.service.KorisnikService;


@RestController
@RequestMapping("/api/korisnik")
public class UserController {

	@Autowired
	private KorisnikService korisnikService;
	
	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public ResponseEntity<KorisnikDTO> registration(@RequestBody Korisnik noviKorisnik) throws Exception{
		Korisnik created = korisnikService.create(noviKorisnik);
		if (created != null){
			korisnikService.login(created.getUsername(), created.getPassword());
			KorisnikDTO dto = new KorisnikDTO(created.getID(), created.getIme(), created.getPrezime(), created.getUsername(), created.getTip());
			return new ResponseEntity<>(dto, HttpStatus.CREATED);
		} else
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<KorisnikDTO> login(@RequestBody LoginDTO loginUser) throws Exception{
		Korisnik korisnik = korisnikService.findByUsername(loginUser.getUsername());
		if (korisnik != null && korisnik.getPassword().equals(loginUser.getPassword())){
			korisnikService.login(korisnik.getUsername(), korisnik.getPassword());
			KorisnikDTO dto = new KorisnikDTO(korisnik.getID(), korisnik.getIme(), korisnik.getPrezime(), korisnik.getUsername(), korisnik.getTip());
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@RequestMapping(value = "/logged", method = RequestMethod.GET)
	public ResponseEntity<KorisnikDTO> getLoggedInUser(){
		Korisnik logged = korisnikService.getLoggedUser();
		KorisnikDTO dto = new KorisnikDTO(logged.getID(), logged.getIme(), logged.getPrezime(), logged.getUsername(), logged.getTip());
		return new ResponseEntity<>(dto, HttpStatus.OK);
	}
}
