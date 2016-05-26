<?xml version="1.0"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

  <xsl:template match="/bib">
    <xsl:element name="math">
        <xsl:element name="sum">
            <xsl:value-of select="format-number(sum(book/price),'$####.00')"/>
        </xsl:element>
        <xsl:element name="mean">
            <xsl:value-of select="format-number(sum(book/price) div count(book),'$####.00')"/>
        </xsl:element>
        <xsl:element name="min">
            <xsl:value-of select="format-number(min(book/price),'$####.00')"/>
        </xsl:element>
        <xsl:element name="max">
            <xsl:value-of select="format-number(max(book/price),'$####.00')"/>
        </xsl:element>
    </xsl:element>
  </xsl:template>
  
</xsl:stylesheet>
