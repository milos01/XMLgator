<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="//akt">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:pred="http://ftn.uns.ac.rs/XML/tim12/predikati/">
			<rdf:Description>
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="@ID"></xsl:value-of>
				</xsl:attribute>
				<pred:akt_za rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="@za"></xsl:value-of></pred:akt_za>
				<pred:akt_protiv rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="@protiv"></xsl:value-of></pred:akt_protiv>
				<pred:akt_uzdrzano rdf:datatype="http://www.w3.org/2001/XMLSchema#int"><xsl:value-of select="@uzdrzano"></xsl:value-of></pred:akt_uzdrzano>
				<pred:akt_procenat rdf:datatype="http://www.w3.org/2001/XMLSchema#double"><xsl:value-of select="@procenat"></xsl:value-of></pred:akt_procenat>
				<pred:akt_osnov_donosenja><xsl:value-of select="@osnov_donosenja"></xsl:value-of></pred:akt_osnov_donosenja>
				<pred:akt_datum_donosenja rdf:datatype="http://www.w3.org/2001/XMLSchema#date"><xsl:value-of select="@datum_donosenja"></xsl:value-of></pred:akt_datum_donosenja>
			</rdf:Description>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>