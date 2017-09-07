package com.example.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import com.example.model.korisnik.Korisnik;
import com.example.service.KorisnikService;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.JAXBHandle;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class KorisnikServiceXML implements KorisnikService {

    @Autowired
    protected XMLDocumentManager xmlDocumentManager;
    
    private static final String DOC_ID_BASE = "http://www.ftn.uns.ac.rs/XML/tim8/korisnik/";
    
    @Autowired
	private UserDetailsService userDetailsService;
	
	@Autowired
    private AuthenticationManager authenticationManager;

	@Override
	public Korisnik findByUsername(String username) throws Exception {
		JAXBHandle contentHandle = getKorisnikHandle();
		try{
			JAXBHandle result = xmlDocumentManager.read(DOC_ID_BASE + username, contentHandle);
			Korisnik korisnik = (Korisnik) result.get(Korisnik.class);
			if (korisnik == null)
				throw new Exception("Korisnik nije pronadjen sa email-om: " + username);
			return korisnik;
		} catch (ResourceNotFoundException ex){
			throw new Exception("Korisnik nije pronadjen sa email-om: " + username);
		}		
	}

	@Override
	public Korisnik create(Korisnik korisnik) throws Exception {
		// Provera da li vec postoji korisnik sa tim email-om
		try{
			Korisnik found = findByUsername(korisnik.getUsername());
			if (found != null)
				throw new Exception("Vec je registrovan korisnik sa email-om: " + korisnik.getUsername());
		} catch (Exception e){ }
		
		JAXBHandle contentHandle = getKorisnikHandle();
		contentHandle.set(korisnik);
		korisnik.setID(DOC_ID_BASE + korisnik.getUsername());
		xmlDocumentManager.write(DOC_ID_BASE + korisnik.getUsername(), contentHandle);
		
		return korisnik;
	}
	
	@Override
	public Korisnik update(Korisnik korisnik) throws Exception {
		Korisnik found = findByUsername(korisnik.getUsername());
		found.setIme(korisnik.getIme());
		found.setPrezime(korisnik.getPrezime());
		JAXBHandle contentHandle = getKorisnikHandle();
		contentHandle.set(found);
		xmlDocumentManager.write(DOC_ID_BASE + found.getUsername(), contentHandle);
		
		return found;
	}
	
	@Override
	public void delete(String username) throws Exception {
		findByUsername(username);
		xmlDocumentManager.delete(DOC_ID_BASE + username);
	}
	
	private JAXBHandle getKorisnikHandle(){
		try {
            JAXBContext context = JAXBContext.newInstance(Korisnik.class);
            return new JAXBHandle(context);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to create korisnik JAXB context", e);
        }
	}
	
	@Override
	public Korisnik getLoggedUser() {
		//Object userDetails = SecurityContextHolder.getContext().getAuthentication().getDetails();
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (!(obj instanceof UserDetails))
			return null;
		UserDetails userDetails = (UserDetails) obj; 
        if (userDetails instanceof UserDetails) {
            String username = ((UserDetails)userDetails).getUsername();
            System.out.println("This is the username: " + username);
            Korisnik korisnik;
			try {
				korisnik = this.findByUsername(username);
				return korisnik;
			} catch (Exception e) {
				return null;
			}
        }
        return null;
	}

	@Override
	public void login(String username, String password) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        authenticationManager.authenticate(usernamePasswordAuthenticationToken);

        if (usernamePasswordAuthenticationToken.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }
	}

}
