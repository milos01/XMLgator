<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="//akt">
		<h2>Akt: <xsl:value-of select="@naziv"></xsl:value-of></h2>
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
				<label>Datum donosenja: </label>
			</div>
			<div class="col-md-6">
				<label><xsl:value-of select="@datum_donosenja"></xsl:value-of></label>
			</div>
		</div>
		<div class="row">
			<div class="col-md-3">
				<label>Osnov donosenja: </label>
			</div>
			<div class="col-md-6">
				<label><xsl:value-of select="@osnov_donosenja"></xsl:value-of></label>
			</div>
		</div>
		<hr/>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//deo">
		<br/><br/>
		<h2>
			Deo <xsl:value-of select="@redni_broj"></xsl:value-of>
			<br/>
			<xsl:value-of select="@naziv"></xsl:value-of>
		</h2>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//glava">
		<br/><br/>
		<h3>
			Glava <xsl:value-of select="@redni_broj"></xsl:value-of>
		</h3>
		<br/>
		<xsl:value-of select="@naziv"></xsl:value-of>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//odeljak">
		<br/><br/>
		<h4>
			<xsl:value-of select="@redni_broj"></xsl:value-of>. 
			<xsl:value-of select="@naziv"></xsl:value-of>
		</h4>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//pododeljak">
		<br/><br/>
		<h4>
			<xsl:value-of select="@redni_broj"></xsl:value-of>) 
			<xsl:value-of select="@naziv"></xsl:value-of>
		</h4>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//clan">
		<h5>
			<xsl:value-of select="@naziv"></xsl:value-of><br/>
			Clan <xsl:value-of select="@redni_broj"></xsl:value-of>.
		</h5>
		<p>
			<xsl:apply-templates />
		</p>
		<br/><br/>
	</xsl:template>
	
	<xsl:template match="akt//text()">
		<xsl:copy-of select="."/>
	</xsl:template>
	
	<xsl:template match="//stav">
		<br/>
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//tacka">
		<br/>
		<xsl:value-of select="@redni_broj"></xsl:value-of>) 
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//podtacka">
		<br/>
		    (<xsl:value-of select="@redni_broj"></xsl:value-of>) 
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="//alineja">
		<br/>
		        -
		<xsl:apply-templates/>
	</xsl:template>
	
	
</xsl:stylesheet>