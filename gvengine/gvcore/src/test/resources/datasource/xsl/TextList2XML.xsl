<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/xml">
        <xsl:element name="list">
            <!-- replace string terminators whith ~ -->
            <xsl:variable name="rows" select="translate(., '&#xA;&#xD;', '~')"/>

            <xsl:call-template name="splitOnBlank" >
                <xsl:with-param name="string" select="$rows"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template match="node()" mode="splitOnBlank" name="splitOnBlank">
        <xsl:param name="string" select="." />
        <xsl:choose>
            <!-- if the string contains a ~... -->
            <xsl:when test="contains($string, '~')">
                <!-- give the part before the ~... -->
                <xsl:call-template name="makeEntry">
                    <xsl:with-param name="tRow" select="substring-before($string, '~')" />
                </xsl:call-template>
                <!-- and then call the template recursively on the rest of the string -->
                <xsl:call-template name="splitOnBlank">
                    <xsl:with-param name="string" select="substring-after($string, '~')" />
                </xsl:call-template>
            </xsl:when>
            <!-- if the string doesn't contain a ~, just give its value, followed by a br element -->
            <xsl:otherwise>
                <xsl:call-template name="makeEntry">
                    <xsl:with-param name="tRow" select="$string" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="makeEntry">
        <xsl:param name="tRow" select="''"/>

        <xsl:element name="entry">
            <xsl:attribute name="name"><xsl:value-of select="normalize-space(substring-before($tRow, ';'))"/></xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="normalize-space(substring-after($tRow, ';'))"/></xsl:attribute>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
