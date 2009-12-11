<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>

  <xsl:template match="/">
    <xsl:apply-templates select="/corpus/article/sentence"/>
  </xsl:template>

  <xsl:template match="sentence">
    <xsl:apply-templates select="mor|*/mor"/><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="mor">
    <xsl:value-of select="text()"/>
  </xsl:template>

</xsl:stylesheet>
