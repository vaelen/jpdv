<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
(defstruct chunk :id :link :relationship :score :head :func :tokens)
(defstruct token :id :reading :base :pos :ctype :cform :ne)
<xsl:apply-templates select="sentences/sentence"/>
</xsl:template>

<xsl:template match="sentence">
  (
  <xsl:for-each select="chunk">
    (struct chunk <xsl:value-of select="@id"/> <xsl:value-of select="@link"/> &quot;<xsl:value-of select="@rel"/>&quot; <xsl:value-of select="@score"/> <xsl:value-of select="head"/> <xsl:value-of select="@func"/> [<xsl:for-each select="tok">
      (struct token <xsl:value-of select="@id"/> &quot;<xsl:value-of select="@read"/>&quot; &quot;<xsl:value-of select="@base"/>&quot; &quot;<xsl:value-of select="@pos"/>&quot; &quot;<xsl:value-of select="@ctype"/>&quot; &quot;<xsl:value-of select="cform"/>&quot; <xsl:value-of select="@ne"/>)</xsl:for-each>
    ])
  </xsl:for-each>)
</xsl:template>

<!--
<xsl:template match="/">
  <html>
  <body>
    <h2>My CD Collection</h2>
    <table border="1">
      <tr bgcolor="#9acd32">
        <th>Title</th>
        <th>Artist</th>
      </tr>
      <xsl:for-each select="catalog/cd">
        <tr>
          <td><xsl:value-of select="title"/></td>
          <td><xsl:value-of select="artist"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </body>
  </html>
</xsl:template>
-->
</xsl:stylesheet>
