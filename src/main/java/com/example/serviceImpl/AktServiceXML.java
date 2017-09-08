package com.example.serviceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.validation.ValidationException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.example.dto.PretragaDTO;
import com.example.dto.RezultatiGlasanjaDTO;
import com.example.model.akt.Akt;
import com.example.model.akt.Alineja;
import com.example.model.akt.Clan;
import com.example.model.akt.Podtacka;
import com.example.model.akt.Stav;
import com.example.model.akt.Tacka;
import com.example.model.korisnik.Korisnik;
import com.example.service.AktService;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.marklogic.client.ResourceNotFoundException;
import com.marklogic.client.document.DocumentPatchBuilder;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.io.DOMHandle;
import com.marklogic.client.io.DocumentMetadataHandle;
import com.marklogic.client.io.JAXBHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.SearchHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.io.marker.DocumentPatchHandle;
import com.marklogic.client.query.MatchDocumentSummary;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.query.StringQueryDefinition;
import com.marklogic.client.semantics.GraphManager;
import com.marklogic.client.semantics.RDFMimeTypes;
import com.marklogic.client.semantics.SPARQLMimeTypes;
import com.marklogic.client.semantics.SPARQLQueryDefinition;
import com.marklogic.client.semantics.SPARQLQueryManager;
import com.marklogic.client.util.EditableNamespaceContext;
import com.example.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component
public class AktServiceXML implements AktService {

	
	@Autowired
    protected QueryManager queryManager;

    @Autowired
    protected XMLDocumentManager xmlDocumentManager;
    
    @Autowired
    private GraphManager graphManager;
     
    @Autowired
    private SPARQLQueryManager sparqlQueryManager;
    
    private static final String DOC_ID_BASE = "http://www.ftn.uns.ac.rs/XML/tim8/akt/";
    private static final String KORISNIK_ID_BASE = "http://www.ftn.uns.ac.rs/XML/tim8/korisnik/";
    private static final String METADATA_URI = "http://www.ftn.uns.ac.rs/XML/tim8/metadata/akt";
    
    private static final String STATUS_PREDLOZEN = "PREDLOZEN";
    private static final String STATUS_USVOJEN = "USVOJEN";
    private static final String STATUS_ODBIJEN = "ODBIJEN";
    private static final String STATUS_POVUCEN = "POVUCEN";
    
    private static final String AKT_RDF_XSL_PATH = "data/xslt/aktToRDF.xsl";
    private static final String AKT_XHTML_XSL_PATH = "data/xslt/aktToXHTML.xsl";
    
    private static final String FIND_ALL_SPARQL_PATH = "data/sparql/sviAkti.rq";
    private static final String FIND_BY_USER_SPARQL_PATH = "data/sparql/sviMojiAkti.rq";
    private static final String POVUCI_AKT_SPARQL_PATH = "data/sparql/povuciAkt.rq";
    private static final String SVI_AKTI_PO_IDU_SPARQL_PATH = "data/sparql/aktiPoIDu.rq";
    private static final String NAZIV_AKTA_SPARQL_PATH = "data/sparql/nazivAkta.rq";
    private static final String PROMENI_STATUS_AKTA_SPARQL_PATH = "data/sparql/promeniStatusAkta.rq";
    private static final String PRIHVATI_AKT_XSLT_PATH = "data/xslt/prihvatiAktRDF.xsl";

    
    
	@Override
	public Akt predlozi(Akt akt, Korisnik korisnik) throws Exception, ValidationException{
		String id = Util.nextID();
		akt.setID(DOC_ID_BASE + id);
		akt.setRawId(id);
		setIDsForSubElements(akt);
		akt.setStatus(STATUS_PREDLOZEN);
		akt.setOdbornik(korisnik.getUsername());
		akt.setDatumDonosenja(null);
		JAXBHandle contentHandle = getAktHandle();
		contentHandle.set(akt);
		
		//validacija
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
	        Schema schema = sf.newSchema(new File(System.getProperty("user.dir") + "/data/schema/akt.xsd")); 
	        JAXBContext jc = JAXBContext.newInstance(Akt.class);
			Marshaller marshaller = jc.createMarshaller();
	        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        marshaller.setSchema(schema);
	       // marshaller.setEventHandler(new MyValidationEventHandler());
	        marshaller.marshal(akt, System.out);
		} catch (JAXBException e) {
			throw new ValidationException("Dokument nije validan!");
		}

		DocumentMetadataHandle metadataHandle = new DocumentMetadataHandle();
		metadataHandle.getCollections().add(METADATA_URI);
		
		xmlDocumentManager.write(DOC_ID_BASE + id, metadataHandle, contentHandle);
		
		createRDF(akt);
		
		return akt;
	}

	@Override
	public void delete(String id) throws Exception {
		findOne(id);
		xmlDocumentManager.delete(DOC_ID_BASE + id);
	}

	@Override
	public Akt update(Akt akt) throws Exception {
		Akt found = findOne(akt.getID());
		akt.setID(found.getID());
		JAXBHandle contentHandle = getAktHandle();
		contentHandle.set(akt);
		xmlDocumentManager.write(DOC_ID_BASE + found.getID(), contentHandle);
		
		return akt;
	}

	@Override
	public Akt findOne(String id) throws Exception {
		JAXBHandle contentHandle = getAktHandle();
		try{
			JAXBHandle result = xmlDocumentManager.read(DOC_ID_BASE + id, contentHandle);
			Akt akt = (Akt) result.get(Akt.class);
			if (akt == null)
				throw new Exception("Akt nije pronadjen sa ID-om: " + id);
			return akt;
		} catch (ResourceNotFoundException ex){
			throw new Exception("Akt nije pronadjen sa ID-om: " + id);
		}
	}
	
	
	@Override
	public String getOneXHTML(String id) throws Exception {
		Akt akt = findOne(id);
		try{
			JAXBContext context = JAXBContext.newInstance(Akt.class);
			Marshaller marshaller = context.createMarshaller();
			OutputStream stream = new ByteArrayOutputStream();
			marshaller.marshal(akt, stream);
			String inputXML = stream.toString();
			
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			StreamSource transformSource = new StreamSource(new File(AKT_XHTML_XSL_PATH));
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
			return null;
		}
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
	public String findAll() {
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
	
	private JAXBHandle getAktHandle() {
        try {
            JAXBContext context = JAXBContext.newInstance(Akt.class);
            return new JAXBHandle(context);
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to create akt JAXB context", e);
        }
    }

	@Override
	public void povuci(String raw_id, Korisnik korisnik) throws Exception {
		Akt akt = findOne(raw_id);
		if (!akt.getStatus().equals(STATUS_PREDLOZEN))
			throw new Exception("Ne mozete povuci ovaj akt.");
		if (akt.getOdbornik().equals(korisnik.getUsername())){
			// Promeni XML
			EditableNamespaceContext namespaces = new EditableNamespaceContext();
			namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
			DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
			builder.setNamespaces(namespaces);
			DocumentPatchHandle xmlPatch = builder.replaceValue("//akt:akt/@status", STATUS_POVUCEN).build();
			xmlDocumentManager.patch(akt.getID(), xmlPatch);
			
			// Promeni RDF
			String raw_query = Util.readFileString(POVUCI_AKT_SPARQL_PATH);
			String query = String.format(raw_query, akt.getID());
			SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
			sparqlQueryManager.executeUpdate(queryDefinition);
			
		} else
			throw new Exception("Nemate dozvolu da povucete ovaj akt.");
	}
	
	@Override
	public void promeniElement(String path, String aktId, Object replacement){
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.replaceFragment(path, replacement).build();
		xmlDocumentManager.patch(aktId, xmlPatch);
	}
	
	@Override
	public void dodajElement(String path, String aktId, Object inserted){
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.insertFragment(path, DocumentPatchBuilder.Position.AFTER, inserted).build();
		xmlDocumentManager.patch(aktId, xmlPatch);
	}
	
	@Override
	public void obrisiElement(String path, String aktId){
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.delete(path).build();
		xmlDocumentManager.patch(aktId, xmlPatch);
	}
	
	private void createRDF(Akt akt){
		String outputXML = extractRDF(akt);
		if (outputXML == null)
			return;
		// Now save the RDFXML to Marklogic
		StringHandle handle = new StringHandle().with(outputXML).withMimetype(RDFMimeTypes.RDFXML);
		graphManager.merge(METADATA_URI, handle);
	}
	
	// Funkcija ekstraktuje RDF iz XML-a
	private String extractRDF(Akt akt){
		try {
			//Marshall the Akt object into xml
			JAXBContext context = JAXBContext.newInstance(Akt.class);
			Marshaller marshaller = context.createMarshaller();
			OutputStream stream = new ByteArrayOutputStream();
			marshaller.marshal(akt, stream);
			String inputXML = stream.toString();
			// Now transform it to RDFXML using XSLT
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			StreamSource transformSource = new StreamSource(new File(AKT_RDF_XSL_PATH));
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
	
	private void setIDsForSubElements(Akt akt){
		String aktBase = akt.getID();
		for (Akt.Deo deo : akt.getDeo()){
			//deo.setID(aktBase + "/deo/" + IDGenerator.nextID()); 
			for (Akt.Deo.Glava glava : deo.getGlava()){
				setIDsForClans(glava.getClan(), aktBase);
				for (Akt.Deo.Glava.Odeljak odeljak : glava.getOdeljak()){
					setIDsForClans(odeljak.getClan(), aktBase);
					for (Akt.Deo.Glava.Odeljak.Pododeljak pododeljak : odeljak.getPododeljak()){
						setIDsForClans(pododeljak.getClan(), aktBase);
					}
				}
			}
		}
		setIDsForClans(akt.getClan(), aktBase);
	}
	
	private void setIDsForClans(List<Clan> clanovi, String aktBase){
		for (Clan clan : clanovi){
			clan.setID(aktBase + "/clan/" + Util.nextID());
			for (Object obj_stav : clan.getContent()){
				if (obj_stav instanceof Stav){
					Stav stav = (Stav) obj_stav;
					stav.setID(aktBase + "/stav/" + Util.nextID());
					for (Object obj_tacka : stav.getContent()){
						if (obj_tacka instanceof Tacka){
							Tacka tacka = (Tacka) obj_tacka;
							tacka.setID(aktBase + "/tacka/" + Util.nextID());
							for (Object obj_podtacka : tacka.getContent()){
								if (obj_podtacka instanceof Podtacka){
									Podtacka podtacka = (Podtacka) obj_podtacka;
									podtacka.setID(aktBase + "/podtacka/" + Util.nextID());
									for (Object obj_alineja : podtacka.getContent()){
										if (obj_alineja instanceof Alineja){
											Alineja alineja = (Alineja) obj_alineja;
											alineja.setID(aktBase + "/alineja/" + Util.nextID());
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public String findMyAkts(Korisnik korisnik) {
		String request = Util.readFileString(FIND_BY_USER_SPARQL_PATH);
		String finalString = String.format(request, korisnik.getUsername());
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
		
		SPARQLQueryDefinition query = sparqlQueryManager.newQueryDefinition(finalString);
		resultsHandle = sparqlQueryManager.executeSelect(query, resultsHandle);
		
		return resultsHandle.toString();
	}

	@Override
	public String findByParameters(PretragaDTO pretraga) {
		String start = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> PREFIX pred: <http://ftn.uns.ac.rs/XML/tim8/predikati/> SELECT * FROM <http://www.ftn.uns.ac.rs/XML/tim8/metadata/akt> WHERE { ";
		String middle = "";
		String filters = "";
		int filterNo = 0;
		if (pretraga.getOdbornik() != null && !pretraga.getOdbornik().equals("")){
			String odbornik = pretraga.getOdbornik();
			middle += " ?id pred:akt_odbornik \"" + odbornik + "\". ";
		} else {
			middle += " ?id pred:akt_odbornik ?odbornik. ";
		}
		middle += " ?id pred:akt_naziv ?naziv. ";
		if (pretraga.getNaziv() != null && !pretraga.getNaziv().equals("")){
			String[] words = pretraga.getNaziv().split("(\\s)+");
			for (String word : words){
				if (filterNo > 0)
					filters += " && ";
				filters += " CONTAINS( ?naziv, \"" + word + "\" )";
				filterNo++;
			}
		}
		/*
		if (pretraga.getNaziv() != null && !pretraga.getNaziv().equals("")){
			middle += " ?id pred:akt_naziv \"" + pretraga.getNaziv() + "\". ";
		} else {
			middle += " ?id pred:akt_naziv ?naziv. ";
		}
		*/
		if (pretraga.getStatus() != null && !pretraga.getStatus().equals("")){
			middle += "?id pred:akt_status \"" + pretraga.getStatus() + "\". ";
			if (pretraga.getStatus().equals(STATUS_USVOJEN)){
				if (pretraga.getProcenat() > 0 && pretraga.getProcenat() <= 100){
					middle += " ?id pred:akt_procenat ?procenat. ";
					if (filterNo > 0)
						filters += " && ";
					filters += "?procenat > " + pretraga.getProcenat();
					filterNo++;
				} else {
					middle += " ?id pred:akt_procenat ?procenat. ";
				}
				DateFormat inputFormat = new SimpleDateFormat("dd.mm.yyyy");
				SimpleDateFormat xsdFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
				middle += " ?id pred:akt_datum_donosenja ?datum. ";
				if (pretraga.getDatumOd() != null && !pretraga.getDatumOd().equals("")){
					try {
						Date datumOd = inputFormat.parse(pretraga.getDatumOd());
						String datumOdString = xsdFormat.format(datumOd);
						if (filterNo > 0)
							filters += " && ";

						filters += " ?datum > \"" + datumOdString + "\"^^xsd:dateTime";
						filterNo++;
					} catch (ParseException e) { e.printStackTrace(); }
				}
				if (pretraga.getDatumDo() != null && !pretraga.getDatumDo().equals("")){
					try {
						Date datumDo = inputFormat.parse(pretraga.getDatumDo());
						String datumDoString = xsdFormat.format(datumDo);
						if (filterNo > 0)
							filters += " && ";
						filters += " ?datum < \"" + datumDoString + "\"^^xsd:dateTime";
						filterNo++;
					} catch (ParseException e) { e.printStackTrace(); }
				}
			}
		} else {
			middle += " ?id pred:akt_status ?status. ";
		}
		if (!filters.equals("")){
			filters = " FILTER( " + filters + " ). ";
		}
		
		middle += " ?id pred:akt_raw_id ?raw_id. ";
		String query = start + middle + filters + " }";
		System.out.println(query);
		
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
		
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		resultsHandle = sparqlQueryManager.executeSelect(queryDefinition, resultsHandle);
		
		return resultsHandle.toString();
	}

	@Override
	public String findByText(String text) {
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
		
		StringQueryDefinition queryDefinition = queryManager.newStringDefinition();
		String query = text.replaceAll("(\\s)+", " AND ");
		queryDefinition.setCriteria(query);
		queryDefinition.setCollections(METADATA_URI);
		SearchHandle results = queryManager.search(queryDefinition, new SearchHandle());
		MatchDocumentSummary matches[] = results.getMatchResults();
		
		List<String> resultUris = new ArrayList<>();
		for (MatchDocumentSummary match : matches)
			resultUris.add(match.getUri());
		
		return naziviPoIDu(resultUris);
	}

	@Override
	public String getNaziv(String id) throws Exception {
		String rawQuery = Util.readFileString(NAZIV_AKTA_SPARQL_PATH);
		String query = String.format(rawQuery, DOC_ID_BASE + id);
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		resultsHandle = sparqlQueryManager.executeSelect(queryDefinition, resultsHandle);
		
		return resultsHandle.toString();
	}

	@Override
	public String naziviPoIDu(List<String> ids) {
		String values = "";
		for (String id : ids){
			values += " <" + id + "> ";
		}
		String rawQuery = Util.readFileString(SVI_AKTI_PO_IDU_SPARQL_PATH);
		String finalQuery = String.format(rawQuery, values);
		
		JacksonHandle resultsHandle = new JacksonHandle();
		resultsHandle.setMimetype(SPARQLMimeTypes.SPARQL_JSON);
		
		SPARQLQueryDefinition sparqlQueryDefinition = sparqlQueryManager.newQueryDefinition(finalQuery);
		resultsHandle = sparqlQueryManager.executeSelect(sparqlQueryDefinition, resultsHandle);
		
		return resultsHandle.toString();
	}

	private void promeniStatusAkta(Akt akt, String noviStatus){
		String stariStatus = akt.getStatus();
		
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.replaceValue("//akt:akt/@status", noviStatus).build();
		xmlDocumentManager.patch(akt.getID(), xmlPatch);
		
		String raw_query = Util.readFileString(PROMENI_STATUS_AKTA_SPARQL_PATH);
		String query = String.format(raw_query, akt.getID(), stariStatus, noviStatus);
		SPARQLQueryDefinition queryDefinition = sparqlQueryManager.newQueryDefinition(query);
		sparqlQueryManager.executeUpdate(queryDefinition);
	}

	@Override
	public void glasajZaAkt(RezultatiGlasanjaDTO rezultati) throws Exception {
		Akt akt = findOne(rezultati.getRaw_id());
		if (rezultati.getProcenat() > 50){
			akt.setProcenat(rezultati.getProcenat());
			akt.setProtiv(rezultati.getProtiv());
			akt.setZa(rezultati.getZa());
			akt.setUzdrzano(rezultati.getUzdrzano());
			akt.setOsnovDonosenja(rezultati.getOsnovDonosenja());
			GregorianCalendar c = new GregorianCalendar();
			c.setTime(new Date());
			XMLGregorianCalendar date2;
			try {
				date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
				akt.setDatumDonosenja(date2);
			} catch (DatatypeConfigurationException e) {
				e.printStackTrace();
			}
			
			prihvatiAkt(akt);
		} else {
			promeniStatusAkta(akt, STATUS_ODBIJEN);
		}
	}
	
	private void prihvatiAkt(Akt akt){
		EditableNamespaceContext namespaces = new EditableNamespaceContext();
		namespaces.put("akt", "http://www.ftn.uns.ac.rs/XML/tim8/akt");
		DocumentPatchBuilder builder = xmlDocumentManager.newPatchBuilder();
		builder.setNamespaces(namespaces);
		DocumentPatchHandle xmlPatch = builder.replaceValue("//akt:akt/@procenat", akt.getProcenat())
				.replaceValue("//akt:akt/@za", akt.getZa())
				.replaceValue("//akt:akt/@protiv", akt.getProtiv())
				.replaceValue("//akt:akt/@uzdrzano", akt.getUzdrzano())
				.replaceValue("//akt:akt/@osnov_donosenja", akt.getOsnovDonosenja())
				.replaceValue("//akt:akt/@datum_donosenja", akt.getDatumDonosenja()).build();
		xmlDocumentManager.patch(akt.getID(), xmlPatch);
		
		try {
			
			JAXBContext context = JAXBContext.newInstance(Akt.class);
			Marshaller marshaller = context.createMarshaller();
			OutputStream stream = new ByteArrayOutputStream();
			marshaller.marshal(akt, stream);
			String inputXML = stream.toString();
			// Now transform it to RDFXML using XSLT
			DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			
			StreamSource transformSource = new StreamSource(new File(PRIHVATI_AKT_XSLT_PATH));
			Transformer transformer = transformerFactory.newTransformer(transformSource);
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			DocumentBuilder builder2 = documentFactory.newDocumentBuilder();
			Document document = builder2.parse(new InputSource(new ByteArrayInputStream(inputXML.getBytes()))); 
			DOMSource source = new DOMSource(document);
			OutputStream stream2 = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(stream2);
			transformer.transform(source, result);
			
			StringHandle handle = new StringHandle().with(result.getOutputStream().toString()).withMimetype(RDFMimeTypes.RDFXML);
			System.out.println(handle);
			System.out.println(handle.toString());
			graphManager.merge(METADATA_URI, handle);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		promeniStatusAkta(akt, STATUS_USVOJEN);
	}
	
}
