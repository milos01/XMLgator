<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:akt="http://www.ftn.uns.ac.rs/XML/tim12/akt">
	<xsl:template match="//amandman">
		<h2>Amandman: <xsl:value-of select="@naziv"></xsl:value-of></h2>
		<div class="row">
			<div class="col-md-3">
				<label>ID: </label>
			</div>
			<div class="col-md-6">
				<label><xsl:value-of select="@ID"></xsl:value-of></label>
			</div>
		</div>
		<div class="row">
			<div class="col-md-3">
				<label>Akt: </label>
			</div>
			<div class="col-md-6">
				<a>
					<xsl:attribute name="href">
						<xsl:value-of select="concat('/#/akt/', @akt_raw_id)"></xsl:value-of>
					</xsl:attribute>
					<xsl:value-of select="@akt"></xsl:value-of>
				</a>
			</div>
		</div>
		<div class="row">
			<div class="col-md-3">
				<label>Status: </label>
			</div>
			<div class="col-md-6">
				<label><xsl:value-of select="@status"></xsl:value-of></label>
			</div>
		</div>
		<div class="row">
			<div class="col-md-3">
				<label>Odbornik: </label>
			</div>
			<div class="col-md-6">
				<label><xsl:value-of select="@odbornik"></xsl:value-of></label>
			</div>
		</div>
		<div class="row">
			<div class="col-md-3">
				<label>Pravni osnov: </label>
			</div>
			<div class="col-md-6">
				<label><xsl:value-of select="@pravni_osnov"></xsl:value-of></label>
			</div>
		</div>
		<hr/>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//promena">
		<br/><br/>
		<h4><xsl:value-of select="@tip"></xsl:value-of>, <xsl:value-of select="@odredba"></xsl:value-of></h4>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//akt:clan">
		<h5>
			<xsl:value-of select="@naziv"></xsl:value-of><br/>
			Clan <xsl:value-of select="@redni_broj"></xsl:value-of>.
		</h5>
		<p>
			<xsl:apply-templates />
		</p>
		<br/><br/>
	</xsl:template>
	
	<xsl:template match="promena//text()">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<xsl:template match="//akt:stav">
		<br/>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//akt:tacka">
		<br/>
		<xsl:value-of select="@redni_broj"></xsl:value-of>) 
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//akt:podtacka">
		<br/>
		    (<xsl:value-of select="@redni_broj"></xsl:value-of>) 
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//akt:alineja">
		<br/>
		        -
		<xsl:apply-templates/>
	</xsl:template>
	
</xsl:stylesheet>