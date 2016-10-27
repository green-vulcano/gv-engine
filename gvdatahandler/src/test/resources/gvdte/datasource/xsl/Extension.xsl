<?xml version="1.0"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0"
    xmlns:java="http://xml.apache.org/xalan/java"
    exclude-result-prefixes="java">

  <xsl:template match="/date">
    <out>
      <xsl:value-of select="java:it.greenvulcano.util.txt.DateUtils.convertString(., @formatIn, @formatOut)"/>
    </out>
  </xsl:template>
</xsl:stylesheet>
