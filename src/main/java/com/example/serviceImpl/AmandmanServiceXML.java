package com.example.serviceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.example.dto.RezultatiGlasanjaDTO;
import com.example.model.akt.Akt;
import com.example.model.akt.Akt.Deo;
import com.example.model.akt.Akt.Deo.Glava;
import com.example.model.akt.Akt.Deo.Glava.Odeljak;
import com.example.model.akt.Akt.Deo.Glava.Odeljak.Pododeljak;
import com.example.model.akt.Alineja;
import com.example.model.akt.Clan;
import com.example.model.akt.Podtacka;
import com.example.model.akt.Stav;
import com.example.model.akt.Tacka;
import com.example.model.amandman.Amandman;
import com.example.model.amandman.Promena;
import com.example.model.korisnik.Korisnik;
import com.example.service.AktService;
import com.example.service.AmandmanService;
import com.example.util.Util;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.document.DocumentPatchBuilder;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JAXBHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.DocumentPatchHandle;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.semantics.GraphManager;
import com.marklogic.client.semantics.RDFMimeTypes;
import com.marklogic.client.semantics.SPARQLMimeTypes;
import com.marklogic.client.semantics.SPARQLQueryDefinition;
import com.marklogic.client.semantics.SPARQLQueryManager;
import com.marklogic.client.util.EditableNamespaceContext;


public class AmandmanServiceXML implements AmandmanService {

	
	@Autowired
    protected QueryManager queryManager;

    @Autowired
    protected XMLDocumentManager xmlDocumentManager;
    
    @Autowired
    private GraphManager graphManager;
    
    @Autowired
    private SPARQLQueryManager sparqlQueryManager;
    
    @Autowired
    private AktService aktService;
    
    private static final String DOC_ID_BASE = "http://www.ftn.uns.ac.rs/XML/tim8/amandman/";
    private static final String METADATA_URI = "http://www.ftn.uns.ac.rs/XML/tim8/metadata/amandman";
    private static final String AKT_ID_BASE = "http://www.ftn.uns.ac.rs/XML/tim8/akt/";
    
    private static final String STATUS_PREDLOZEN = "PREDLOZEN";
    private static final String STATUS_USVOJEN = "USVOJEN";
    private static final String STATUS_ODBIJEN = "ODBIJEN";
    private static final String STATUS_POVUCEN = "POVUCEN";
    
    private static final String AMANDMAN_RDF_XSL_PATH = "data/xslt/amandmanToRDF.xsl";
    private static final String AMANDMANI_ZA_AKT_SPARQL_PATH = "data/sparql/amandmaniZaAkt.rq";
    private static final String AMANDMAN_XHTML_XSL_PATH = "data/xslt/amandmanToXHTML.xsl";
    private static final String SVI_MOJI_AMANDMANI_SPARQL_PATH = "data/sparql/sviMojiAmandmani.rq";
    private static final String POVUCI_AMANDMAN_SPARQL_PATH = "data/sparql/povuciAmandman.rq";
    private static final String FIND_ALL_SPARQL_PATH = "data/sparql/sviAmandmani.rq";
    private static final String PROMENI_STATUS_AMANDMANA_SPARQL_PATH = "data/sparql/promeniStatusAmandmana.rq";
    private static final String PRIHVATI_AMANDMAN_SPARQL_PATH = "data/sparql/prihvatiAmandman.rq";

    
    
	@Override
	public Amandman predlozi(Amandman amandman, Korisnik korisnik) throws Exception{
		
		for (int i = 0; i < amandman.getPromena().size(); i++) {
			if(amandman.getPromena().get(i).getOdredba().equalsIgnoreCase(""))
				throw new Exception("Morate izabrati odredbu!");
		}
		
		String id = Util.nextID();
		amandman.setID(DOC_ID_BASE + id);
		amandman.setRawId(id);
		amandman.setAkt(AKT_ID_BASE + amandman.getAkt());
		amandman.setStatus(STATUS_PREDLOZEN);
		amandman.setOdbornik(korisnik.getID());
		String[] tokens = amandman.getAkt().split("/");
		amandman.setAktRawId(tokens[tokens.length - 1]);
		setIDsForSubelements(amandman.getPromena(), amandman.getAkt(), id);
		JAXBHandle contentHandle = getAmandmanHandle();
		contentHandle.set(amandman);
		
		//validacija
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
	        Schema schema = sf.newSchema(new File(System.getProperty("user.dir") + "/data/schema/amandman.xsd")); 
	        JAXBContext jc = JAXBContext.newInstance(Amandman.class);
			Marshaller marshaller = jc.createMarshaller();
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        marshaller.setSchema(schema);
	       // marshaller.setEventHandler(new MyValidationEventHandler());
	        marshaller.marshal(amandman, System.out);
		} catch (JAXBException e) {
			throw new Exception("Dokument nije validan!");
		}
		
		
		DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
		metadataHandle.getCollections().add(METADATA_URI);
		xmlDocumentManager.write(DOC_ID_BASE + id, metadataHandle, contentHandle);
		
		createRDF(amandman);
		
		return amandman;
	}

	
	private JAXBHandle getAmandmanHandle() {
        try {
            JAXBContext context = JAXBContext.newInstance(Amandman.class);
            return new JAXBHandle(context);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to create amandman JAXB context", e);
        }
    }

	private void createRDF(Amandman amandman){
		String outputXML = extractRDF(amandman);
		if (outputXML == null)
			return;
		// Now save the RDFXML to Marklogic
		StringHandle handle = new StringHandle().with(outputXML).withMimetype(RDFMimeTypes.RDFXML);
		graphManager.merge(METADATA_URI, handle);
	}
	
	// Funkcija ekstraktuje RDF iz XML-a
	private String extractRDF(Amandman amandman){
		try {
			//Marshall the Amandman object into xml
			JAXBContext context = JAXBContext.newInstance(Amandman.class);
			Marshaller marshaller = context.createMarshaller();
			OutputStream stream = new ByteArrayOutputStream();
			marshaller.marshal(amandman, stream);
			String inputXML = stream.toString();
			// Now transform it to RDFXML using XSLT
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			StreamSource transformSource = new StreamSource(new File(AMANDMAN_RDF_XSL_PATH));
			Transformer transformer = transformerFactory.newTransformer(transformSource);
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			DocumentBuilder builder = documentFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new ByteArrayInputStream(inputXML.getBytes()))); 
			DOMSource source = new DOMSource(document);
			
			OutputStream stream2 = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(stream2);
			transformer.transform(source, result);
			
			return result.getOutputStream().toString();
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}


	@Override
	public String amandmaniZaAkt(String aktId) {
		String rawQuery = Util.readFileString(AMANDMANI_ZA_AKT_SPARQL_PATH);
		String query = String.format(rawQuery, AKT_ID_BASE + aktId);
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		resultsHandle = sparqlQueryManager.executeSelect(queryDefinition, resultsHandle);
		
		return resultsHandle.toString();
	}


	@Override
	public String getOneXHTML(String id) throws Exception {
		Amandman amandman = findOne(id);
		try {
			JAXBContext context = JAXBContext.newInstance(Amandman.class);
			Marshaller marshaller = context.createMarshaller();
			OutputStream stream = new ByteArrayOutputStream();
			marshaller.marshal(amandman, stream);
			String inputXML = stream.toString();
			
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			StreamSource transformSource = new StreamSource(new File(AMANDMAN_XHTML_XSL_PATH));
			Transformer transformer = transformerFactory.newTransformer(transformSource);
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			DocumentBuilder builder = documentFactory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new ByteArrayInputStream(inputXML.getBytes()))); 
			DOMSource source = new DOMSource(document);
			OutputStream stream2 = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(stream2);
			transformer.transform(source, result);
			
			return result.getOutputStream().toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Amandman findOne(String id) throws Exception {
		JAXBHandle handle = getAmandmanHandle();
		try {
			JAXBHandle result = xmlDocumentManager.read(DOC_ID_BASE + id, handle);
			Amandman amandman = (Amandman) result.get(Amandman.class);
			if (amandman == null)
				throw new Exception("Amandman nije pronadjen sa ID-om: " + id);
			return amandman;
		} catch (ResourceNotFoundException ex){
			throw new Exception("Amandman nije pronadjen sa ID-om: " + id);
		}
	}


	@Override
	public String sviMojiAmandmani(Korisnik korisnik) {
		String rawQuery = Util.readFileString(SVI_MOJI_AMANDMANI_SPARQL_PATH);
		String query = String.format(rawQuery, korisnik.getID());
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		resultsHandle = sparqlQueryManager.executeSelect(queryDefinition, resultsHandle);
		
		return resultsHandle.toString();
	}


	@Override
	public void povuci(String id, Korisnik korisnik) throws Exception {
		Amandman amandman = findOne(id);
		if (!amandman.getStatus().equals(STATUS_PREDLOZEN))
			throw new Exception("Ne mozete povuci ovaj amandman.");
		if (amandman.getOdbornik().equals(korisnik.getID())){
			// Promeni XML
			EditableNamespaceContext namespaces = new EditableNamespaceContext();
			namespaces.put("amandman", "http://www.ftn.uns.ac.rs/XML/tim8/amandman");
			DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
			builder.setNamespaces(namespaces);
			DocumentPatchHandle xmlPatch = builder.replaceValue("//amandman:amandman/@status", STATUS_POVUCEN).build();
			xmlDocumentManager.patch(amandman.getID(), xmlPatch);
						
			// Promeni RDF
			String raw_query = Util.readFileString(POVUCI_AMANDMAN_SPARQL_PATH);
			String query = String.format(raw_query, amandman.getID());
			SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
			sparqlQueryManager.executeUpdate(queryDefinition);
		} else 
			throw new Exception("Nemate dozvolu da povucete ovaj amandman.");
	}


	@Override
	public void getOnePDF(String id, OutputStream os) throws Exception {
		try {
			String innerHTML = getOneXHTML(id);
			String fullHTML = "<html><head></head><body>" + innerHTML + "</body></html>";
			//OutputStream file = new FileOutputStream(new File("data/gen/pdf/output.pdf"));
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
		    PdfWriter writer = PdfWriter.getInstance(document, os);
		    document.open();
		    InputStream is = new ByteArrayInputStream(fullHTML.getBytes());
		    XMLWorkerHelper.getInstance().parseXHtml(writer, document, is);
		    document.close();
		    //file.close();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public String findAll(){
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
			
		String queryString = Util.readFileString(FIND_ALL_SPARQL_PATH);
		SPARQLQueryDefinition query = sparqlQueryManager.newQueryDefinition(queryString);
		resultsHandle = sparqlQueryManager.executeSelect(query, resultsHandle);
		
		return resultsHandle.toString();
	}
	
	@Override
	public String findAllXML() {
		try{
			DOMHandle domHandle = new DOMHandle();
			graphManager.read(METADATA_URI, domHandle).withMimetype(RDFMimeTypes.RDFXML);
			Document document = domHandle.get();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			OutputStream out = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(out);
			transformer.transform(source, result);
			
			return out.toString();
		} catch (Exception e){
			return null;
		}
	}
	
	private void primeniAmandman(Amandman amandman) throws Exception {
		Akt akt = aktService.findOne(amandman.getAktRawId());
		for (Promena promena : amandman.getPromena()){
			if (pronadjiElementZaID(akt, promena.getOdredba()))
				primeniPromenu(promena, akt);
		}
	}
	
	private void primeniPromenu(Promena promena, Akt akt){
		String path = "//*[@akt:ID='" + promena.getOdredba() + "']";
		switch (promena.getTip()){
			case "brisanje":
				aktService.obrisiElement(path, akt.getID());
				break;
			case "izmena":
				Object replacement = promena.getContent().get(0);
				aktService.promeniElement(path, akt.getID(), replacement);
				break;
			case "dopuna":
				Object inserted = promena.getContent().get(0);
				aktService.dodajElement(path, akt.getID(), inserted);
				break;
		}
	}
	
	private void promeniStatusAmandmana(Amandman amandman, String noviStatus){
		String stariStatus = amandman.getStatus();
		
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("amandman", "http://www.ftn.uns.ac.rs/XML/tim8/amandman");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.replaceValue("//amandman:amandman/@status", noviStatus).build();
		xmlDocumentManager.patch(amandman.getID(), xmlPatch);
		
		String raw_query = Util.readFileString(PROMENI_STATUS_AMANDMANA_SPARQL_PATH);
		String query = String.format(raw_query, amandman.getID(), stariStatus, noviStatus);
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		sparqlQueryManager.executeUpdate(queryDefinition);
	}
	
	private void prihvatiAmandman(Amandman amandman){
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("amandman", "http://www.ftn.uns.ac.rs/XML/tim8/amandman");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.replaceValue("//amandman:amandman/@procenat", amandman.getProcenat())
				.replaceValue("//amandman:amandman/@za", amandman.getZa())
				.replaceValue("//amandman:amandman/@protiv", amandman.getProtiv())
				.replaceValue("//amandman:amandman/@uzdrzano", amandman.getUzdrzano()).build();
		xmlDocumentManager.patch(amandman.getID(), xmlPatch);
		
		String raw_query = Util.readFileString(PRIHVATI_AMANDMAN_SPARQL_PATH);
		String query = String.format(raw_query, amandman.getID(), amandman.getProcenat(), amandman.getZa(),
				amandman.getProtiv(), amandman.getUzdrzano());
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		sparqlQueryManager.executeUpdate(queryDefinition);
		
		promeniStatusAmandmana(amandman, STATUS_USVOJEN);
	}
	
	@Override
	public void glasajZaAmandman(RezultatiGlasanjaDTO rezultati) throws Exception {
		Amandman amandman = findOne(rezultati.getRaw_id());
		if (rezultati.getProcenat() > 50){
			amandman.setProcenat(rezultati.getProcenat());
			amandman.setProtiv(rezultati.getProtiv());
			amandman.setZa(rezultati.getZa());
			amandman.setUzdrzano(rezultati.getUzdrzano());
			
			prihvatiAmandman(amandman);
			primeniAmandman(amandman);
		} else {
			promeniStatusAmandmana(amandman, STATUS_ODBIJEN);
		}
	}
	
	//////////////
	
	private boolean pronadjiElementZaID(Akt akt, String id){
		for (Clan c : akt.getClan()){
			if (pronadjiUClanu(c, id))
				return true;
		}
		for (Deo d : akt.getDeo()){
			for (Glava g : d.getGlava()){
				for (Clan c : g.getClan()){
					if (pronadjiUClanu(c, id))
						return true;
				}
				for (Odeljak o : g.getOdeljak()){
					for (Clan c : o.getClan()){
						if (pronadjiUClanu(c, id))
							return true;
					}
					for (Pododeljak p : o.getPododeljak()){
						for (Clan c : p.getClan()){
							if (pronadjiUClanu(c, id))
								return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean pronadjiUClanu(Clan clan, String id){
		if (clan.getID().equals(id))
			return true;
		for (Object obj_stav : clan.getContent()){
			if (obj_stav instanceof Stav){
				Stav stav = (Stav) obj_stav;
				if (stav.getID().equals(id))
					return true;
				for (Object obj_tacka : stav.getContent()){
					if (obj_tacka instanceof Tacka){
						Tacka tacka = (Tacka) obj_tacka;
						if (tacka.getID().equals(id))
							return true;
						for (Object obj_podtacka : tacka.getContent()){
							if (obj_podtacka instanceof Podtacka){
								Podtacka podtacka = (Podtacka) obj_podtacka;
								if (podtacka.getID().equals(id))
									return true;
								for (Object obj_alineja : podtacka.getContent()){
									if (obj_alineja instanceof Alineja){
										Alineja alineja = (Alineja) obj_alineja;
										if (alineja.getID().equals(id))
											return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private void setIDsForSubelements(List<Promena> promene, String id, String amandmanId){
		for (Promena promena : promene){
			promena.setID(DOC_ID_BASE + amandmanId + "/promena/" + Util.nextID());
			if (promena.getContent().isEmpty())
				continue;
			Object obj = promena.getContent().get(0);
			if (obj instanceof Clan){
				Clan clan = (Clan) obj;
				setIDsForClan(clan, id);
			} else if (obj instanceof Stav){
				Stav stav = (Stav) obj;
				setIDsForStav(stav, id);
			} else if (obj instanceof Tacka){
				Tacka tacka = (Tacka) obj;
				setIDsForTacka(tacka, id);
			} else if (obj instanceof Podtacka){
				Podtacka podtacka = (Podtacka) obj;
				setIDsForPodtacka(podtacka, id);
			} else if (obj instanceof Alineja){
				Alineja alineja = (Alineja) obj;
				alineja.setID(id + "/alineja/" + Util.nextID());
			}
		}
	}
	
	private void setIDsForClan(Clan clan, String id){
		clan.setID(id + "/clan/" + Util.nextID());
		for (Object obj : clan.getContent()){
			if (obj instanceof Stav){
				Stav stav = (Stav) obj;
				setIDsForStav(stav, clan.getID());
			}
		}
	}
	
	private void setIDsForStav(Stav stav, String id){
		stav.setID(id + "/stav/" + Util.nextID());
		for (Object obj : stav.getContent()){
			if (obj instanceof Tacka){
				Tacka tacka = (Tacka) obj;
				setIDsForTacka(tacka, stav.getID());
			}
		}
	}
	
	private void setIDsForTacka(Tacka tacka, String id){
		tacka.setID(id + "/tacka/" + Util.nextID());
		for (Object obj : tacka.getContent()){
			if (obj instanceof Podtacka){
				Podtacka podtacka = (Podtacka) obj;
				setIDsForPodtacka(podtacka, tacka.getID());
			}
		}
	}
	
	private void setIDsForPodtacka(Podtacka podtacka, String id){
		podtacka.setID(id + "/podtacka/" + Util.nextID());
		for (Object obj : podtacka.getContent()){
			if (obj instanceof Alineja){
				Alineja alineja = (Alineja) obj;
				alineja.setID(podtacka.getID() + "/alineja/" + Util.nextID());
			}
		}
	}
}
