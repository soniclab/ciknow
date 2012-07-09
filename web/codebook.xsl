<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="/">
	  <html>
	  <head>  	
	  	<style type="text/css">
	  		body{
	  			font-family: Verdana, Arial, sans-serif;
	  			font-size: 1em;
	  		}
	  		
	  		td{
	  			vertical-align: top;
	  		}
	  		
	  		.question{
	  			font-weight: bold;
	  			background-color: 	#EEE8AA;
	  		}
	  		
	  		.field{
	  			font-weight: bold;
	  		}
	  		
	  		.innerTable{
	  			font-size: 0.8em;
	  			border-spacing: 10px;
	  			border-spacing: 10px 2px;
	  		}
	  		
	  		.innerHeader{
	  			font-weight: bold;
				font-style: italic;
	  		}
	  		
	  		.odd{
	  			
	  		}
	  		
	  		.even{
	  			background-color: #ffffcc;
	  		}
	  	</style>
	  </head>
	  <body>
	    <h2><xsl:value-of select="ciknow/survey/@name"/></h2>
	    <p><xsl:value-of select="ciknow/survey/description"/></p>
	    
	    <xsl:for-each select="ciknow/survey/pages/page/questions/question">
		    
		    <table>
		    	<tr class="question">
		    		<td colspan="2">Question <xsl:value-of select="@sequenceNumber"/></td>
		    	</tr>
		    			    
		    	<tr>
		    		<td class="field">Short Name</td>
		    		<td><xsl:value-of select="@shortName"/></td>
		    	</tr>
		    	
		    	<tr>
		    		<td class="field">Label</td>
		    		<td><xsl:value-of select="@label"/></td>
		    	</tr>
		    	
		    	<tr>
		    		<td class="field">Type</td>
		    		<td><xsl:value-of select="@type"/></td>
		    	</tr>
		    	
		    	<xsl:if test="rowPerPage != '0'">
		    	<tr>
		    		<td class="field">Row Per Page</td>
		    		<td><xsl:value-of select="@rowPerPage"/></td>
		    	</tr>
		    	</xsl:if>
		    	
		    	<xsl:if test="htmlInstruction != ''">
		    	<tr>
		    		<td class="field">Instruction</td>
		    		<td><xsl:value-of select="htmlInstruction"/></td>
		    	</tr>			    		
		    	</xsl:if>
		    	
		    	<xsl:if test="fields != ''">
		    	<tr>
		    		<td class="field">Fields</td>		    		
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Label</td>
		    			</tr>		    			
		    			<xsl:for-each select="fields/field">		    				
		    					<tr>
		    						<td><xsl:value-of select="@label"/></td>		    						
		    					</tr>		    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>			    		
		    	</xsl:if>	  
		    			    	
		    	<xsl:if test="textFields != ''">
		    	<tr>
		    		<td class="field">Text Fields</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Label</td>
		    				<td>Large</td>
		    			</tr>		    			
		    			<xsl:for-each select="textFields/textField">		    				
		    					<tr>
		    						<td><xsl:value-of select="@label"/></td>	
	    							<td align="center">
		    							<xsl:if test="@large = 'true'">X</xsl:if>	
	    							</td>		    							    						
		    					</tr>		    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>			    		
		    	</xsl:if>
		    			    	
		    	<xsl:if test="scales != ''">
		    	<tr>
		    		<td class="field">Scales</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Label</td>
		    			</tr>		    			
		    			<xsl:for-each select="scales/scale">		    				
		    					<tr>
		    						<td><xsl:value-of select="@label"/></td>
		    					</tr>		    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>			    		
		    	</xsl:if>	
		    			
		    	<xsl:if test="contactFields != ''">
		    	<tr>
		    		<td class="field">Contact Fields</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Label</td>
		    				<td>Selected</td>
		    			</tr>
		    			<xsl:for-each select="contactFields/contactField">		  				
	    					<tr>
	    						<td><xsl:value-of select="@label"/></td>	    						
	    						<td align="center">
		    						<xsl:if test="@selected = 'true'">X</xsl:if>	
	    						</td>
	    					</tr>    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>			    			    		
		    	</xsl:if>

		    	<tr>
		    		<td class="field">Respondent Groups</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Name</td>
		    			</tr>
		    			<xsl:for-each select="respondentGroups/group">		  				
	    					<tr>
	    						<td><xsl:value-of select="@name"/></td>
	    					</tr>    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>	
		    		
		    	<tr>
		    		<td class="field">Row Groups</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Name</td>
		    			</tr>
		    			<xsl:for-each select="rowGroups/group">		  				
	    					<tr>
	    						<td><xsl:value-of select="@name"/></td>
	    					</tr>    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>	

		    	<tr>
		    		<td class="field">Column Groups</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Name</td>
		    			</tr>
		    			<xsl:for-each select="columnGroups/group">		  				
	    					<tr>
	    						<td><xsl:value-of select="@name"/></td>
	    					</tr>    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>	
		    			    			    	
		    	<xsl:if test="attributes != ''">
		    	<tr>
		    		<td class="field">Attributes</td>
		    		<td>
		    			<table class="innerTable">
		    			<tr class="innerHeader">
		    				<td>Key</td>
		    				<td>Value</td>
		    			</tr>		    			
		    			<xsl:for-each select="attributes/attribute">		    				
		    					<tr>
		    						<td><xsl:value-of select="@key"/></td>
		    						<td><xsl:value-of select="@value"/></td>
		    					</tr>		    				
		    			</xsl:for-each>	
						</table>    		
		    		</td>
		    	</tr>			    		
		    	</xsl:if>			    	
		    				    			    	  			    			    			    			    	
		    </table>
	    </xsl:for-each>
	    
	    <br/><hr/><br/>
    
	    <table>
	    	<tr><th>Groups</th></tr>
	    	<xsl:for-each select="ciknow/groups/group">
	    		<xsl:if test="@private = '0'">
			    	<tr>
			    		<td><xsl:value-of select="@name"/></td>
			    	</tr> 	    			
	    		</xsl:if>   		    	
	    	</xsl:for-each>
	    </table>
   
	  </body>
	  </html>
	</xsl:template>

</xsl:stylesheet>