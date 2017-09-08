<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="//akt">
		<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:pred="http://ftn.uns.ac.rs/XML/tim12/predikati/">
			<rdf:Description>
				<xsl:attribute name="rdf:about">
					<xsl:value-of select="@ID"></xsl:value-of>
				</xsl:attribute>
				<pred:akt_naziv><xsl:value-of select="@naziv"></xsl:value-of></pred:akt_naziv>
				<pred:akt_status><xsl:value-of select="@status"></xsl:value-of></pred:akt_status>
				<pred:akt_raw_id><xsl:value-of select="@raw_id"></xsl:value-of></pred:akt_raw_id>
				<pred:akt_odbornik><xsl:value-of select="@odbornik"></xsl:value-of></pred:akt_odbornik>
			</rdf:Description>
			<xsl:for-each select="//clan">
				<rdf:Description>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="@ID"></xsl:value-of>
					</xsl:attribute>
					<pred:clan_naziv><xsl:value-of select="@naziv"></xsl:value-of></pred:clan_naziv>
					<pred:clan_redni_broj><xsl:value-of select="@redni_broj"></xsl:value-of></pred:clan_redni_broj>
				</rdf:Description>
			</xsl:for-each>
			<xsl:for-each select="//stav">
				<rdf:Description>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="@ID"></xsl:value-of>
					</xsl:attribute>
					<pred:stav_redni_broj><xsl:value-of select="@redni_broj"></xsl:value-of></pred:stav_redni_broj>
				</rdf:Description>
			</xsl:for-each>
			<xsl:for-each select="//tacka">
				<rdf:Description>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="@ID"></xsl:value-of>
					</xsl:attribute>
					<pred:tacka_redni_broj><xsl:value-of select="@redni_broj"></xsl:value-of></pred:tacka_redni_broj>
				</rdf:Description>
			</xsl:for-each>
			<xsl:for-each select="//podtacka">
				<rdf:Description>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="@ID"></xsl:value-of>
					</xsl:attribute>
					<pred:podtacka_redni_broj><xsl:value-of select="@redni_broj"></xsl:value-of></pred:podtacka_redni_broj>
				</rdf:Description>
			</xsl:for-each>
			<xsl:for-each select="//alineja">
				<rdf:Description>
					<xsl:attribute name="rdf:about">
						<xsl:value-of select="@ID"></xsl:value-of>
					</xsl:attribute>
					<pred:alineja_redni_broj><xsl:value-of select="@redni_broj"></xsl:value-of></pred:alineja_redni_broj>
				</rdf:Description>
			</xsl:for-each>
		</rdf:RDF>
	</xsl:template>
</xsl:stylesheet>