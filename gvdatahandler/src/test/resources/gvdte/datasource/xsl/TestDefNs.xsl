<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gv="http://www.greenvulcano.it/greenvulcano" exclude-result-prefixes="gv">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

  <xsl:template match="/gv:doc">
    <xsl:element name="out">
      <xsl:apply-templates mode="child" select="gv:child"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template mode="child" match="gv:child">
    <xsl:element name="node"><xsl:value-of select="."/></xsl:element>
  </xsl:template>
  
</xsl:stylesheet>
