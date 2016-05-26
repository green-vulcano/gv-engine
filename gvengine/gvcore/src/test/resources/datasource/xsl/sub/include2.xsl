<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template mode="mode2" match="child">
    <node2><xsl:value-of select="."/></node2>
  </xsl:template>
</xsl:stylesheet>
