<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="//amandman">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:pred="http://ftn.uns.ac.rs/XML/tim12/predikati/">
			<rdf:Description>
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="@ID"></xsl:value-of>
				</xsl:attribute>
				<pred:amandman_naziv><xsl:value-of select="@naziv"></xsl:value-of></pred:amandman_naziv>
				<pred:amandman_pravni_osnov><xsl:value-of select="@pravni_osnov"></xsl:value-of></pred:amandman_pravni_osnov>
				<pred:amandman_akt>
					<xsl:attribute name="rdf:resource">
						<xsl:value-of select="@akt"></xsl:value-of>
					</xsl:attribute>
				</pred:amandman_akt>
				<pred:amandman_akt_raw_id><xsl:value-of select="@akt_raw_id"></xsl:value-of></pred:amandman_akt_raw_id>
				<pred:amandman_odbornik>
					<xsl:attribute name="rdf:resource">
						<xsl:value-of select="@odbornik"></xsl:value-of>
					</xsl:attribute>
				</pred:amandman_odbornik>
				<pred:amandman_status><xsl:value-of select="@status"></xsl:value-of></pred:amandman_status>
				<pred:amandman_raw_id><xsl:value-of select="@raw_id"></xsl:value-of></pred:amandman_raw_id>
			</rdf:Description>
			<xsl:for-each select="//promena">
				<rdf:Description>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="@ID"></xsl:value-of>
					</xsl:attribute>
					<pred:promena_odredba>
						<xsl:attribute name="rdf:resource">
							<xsl:value-of select="@odredba"></xsl:value-of>
						</xsl:attribute>
					</pred:promena_odredba>
					<pred:promena_obrazlozenje><xsl:value-of select="@obrazlozenje"></xsl:value-of></pred:promena_obrazlozenje>
					<pred:promena_tip><xsl:value-of select="@tip"></xsl:value-of></pred:promena_tip>
					<pred:promena_sluzbeno_glasilo><xsl:value-of select="@sluzbeno_glasilo"></xsl:value-of></pred:promena_sluzbeno_glasilo>
				</rdf:Description>
			</xsl:for-each>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>