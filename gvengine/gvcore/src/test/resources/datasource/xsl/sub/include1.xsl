<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template mode="mode1" match="child">
    <node1><xsl:value-of select="."/></node1>
  </xsl:template>
</xsl:stylesheet>
