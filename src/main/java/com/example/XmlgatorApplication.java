package com.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.document.XMLDocumentManager;
import com.marklogic.client.query.QueryManager;
import com.marklogic.client.semantics.GraphManager;
import com.marklogic.client.semantics.RDFMimeTypes;
import com.marklogic.client.semantics.SPARQLQueryManager;

@SpringBootApplication
public class XmlgatorApplication {

	@Value("${marklogic.host}")
    private String host;

    @Value("${marklogic.port}")
    private int port;
    
    @Value("${marklogic.database}")
    private String database;

    @Value("${marklogic.username}")
    private String username;

    @Value("${marklogic.password}")
    private String password;

    @Bean
    public DatabaseClient getDatabaseClient() {
    	/*
        try {
            // TODO: is this really (still) required?
            // configure once before creating a client
            DatabaseClientFactory.getHandleRegistry().register(
                    JAXBHandle.newFactory(Akt.class)
            );
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return DatabaseClientFactory.newClient(host, port, username, password,
                DatabaseClientFactory.Authentication.DIGEST);
        */
    	DatabaseClient client = DatabaseClientFactory.newClient(host, port, database, username, password,
    			DatabaseClientFactory.Authentication.DIGEST);
    	return client;
    }

    @Bean
    public QueryManager getQueryManager() {
        return getDatabaseClient().newQueryManager();
    }

    @Bean
    public XMLDocumentManager getXMLDocumentManager() {
        return getDatabaseClient().newXMLDocumentManager();
    }
    
    @Bean
    public GraphManager getGraphManager(){
    	GraphManager graphManager = getDatabaseClient().newGraphManager();
    	graphManager.setDefaultMimetype(RDFMimeTypes.RDFXML);
    	return graphManager;
    }
    
    @Bean
    public SPARQLQueryManager getSPARQLQueryManager(){
    	return getDatabaseClient().newSPARQLQueryManager();
    }

    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(XmlgatorApplication.class);
    }
	
	public static void main(String[] args) {
		SpringApplication.run(XmlgatorApplication.class, args);
	}
}
