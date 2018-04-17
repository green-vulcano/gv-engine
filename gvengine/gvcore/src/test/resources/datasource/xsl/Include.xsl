<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>

  <xsl:include href="sub/include1.xsl"/>
  <xsl:include href="sub/include2.xsl"/>

  <xsl:template match="/doc">
    <out>
      <xsl:apply-templates mode="mode1" select="child"/>
      <xsl:apply-templates mode="mode2" select="child"/>
    </out>
  </xsl:template>
</xsl:stylesheet>
