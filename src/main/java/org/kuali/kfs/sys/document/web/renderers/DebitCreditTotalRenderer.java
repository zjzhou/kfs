/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.document.web.renderers;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.struts.taglib.bean.WriteTag;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

public class DebitCreditTotalRenderer extends TotalRendererBase {
    private String debitTotalProperty;
    private String creditTotalProperty;
    private String representedProperty;
    
    private String debitTotalLabelProperty = "accounting.line.group.debitTotal.label";
    private String creditTotalLabelProperty = "accounting.line.group.creditTotal.label";
    
    private String formName = "KualiForm";   
    
    private WriteTag debitWriteTag = new WriteTag();
    private WriteTag creditWriteTag = new WriteTag();
    
    /**
     * Constructs a GroupTotalRenderer, setting permanent values on the writeTag tag
     */
    public DebitCreditTotalRenderer() {
        debitWriteTag.setName(formName);
        creditWriteTag.setName(formName);
    }

    /**
     * Clears out the totalProperty
     * @see org.kuali.kfs.sys.document.web.renderers.Renderer#clear()
     */
    public void clear() {
        super.clear();
        debitTotalProperty = null;
        creditTotalProperty = null;
        representedProperty = null;
        
        debitWriteTag.setPageContext(null);
        debitWriteTag.setParent(null);
        debitWriteTag.setProperty(null);
        
        creditWriteTag.setPageContext(null);
        creditWriteTag.setParent(null);
        creditWriteTag.setProperty(null);
    }

    /**
     * Uses a Struts write tag to dump out the total
     * @see org.kuali.kfs.sys.document.web.renderers.Renderer#render(javax.servlet.jsp.PageContext, javax.servlet.jsp.tagext.Tag)
     */
    public void render(PageContext pageContext, Tag parentTag) throws JspException {
        JspWriter out = pageContext.getOut();
        
        try {
            out.write("<tr>");
            
            final int emptyCellSpanBefore = this.getColumnNumberOfRepresentedCell() - 1;
            out.write("<td  class=\"total-line\" colspan=\"");
            out.write(Integer.toString(emptyCellSpanBefore));
            out.write("\">&nbsp;</td>");
            
            out.write("<td class=\"total-line\" style=\"border-left: 0px; white-space:nowrap;\">");            
            out.write("<strong>");
            
            out.write(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(debitTotalLabelProperty));
            out.write("&nbsp;");
            
            debitWriteTag.setPageContext(pageContext);
            debitWriteTag.setParent(parentTag);
            debitWriteTag.setProperty(debitTotalProperty);
            debitWriteTag.doStartTag();
            debitWriteTag.doEndTag();
            
            out.write("</strong>");            
            out.write("</td>");
            
            out.write("<td class=\"total-line\" style=\"border-left: 0px; white-space:nowrap;\">");            
            out.write("<strong>");
            
            out.write(SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(creditTotalLabelProperty));
            out.write("&nbsp;");
            
            creditWriteTag.setPageContext(pageContext);
            creditWriteTag.setParent(parentTag);
            creditWriteTag.setProperty(creditTotalProperty);
            creditWriteTag.doStartTag();
            creditWriteTag.doEndTag();
            
            out.write("</strong>");            
            out.write("</td>");
            
            final int emptyCellSpanAfter = this.getCellCount() - this.getColumnNumberOfRepresentedCell() - 1;
            if(emptyCellSpanAfter > 0) {
                out.write("<td class=\"total-line\" style=\"border-left: 0px;\" colspan=\"");
                out.write(Integer.toString(emptyCellSpanAfter));
                out.write("\">&nbsp;</td>");
            }
            
            out.write("</tr>");
        }
        catch (IOException ioe) {
            throw new JspException("Difficulty rendering debit credit totals", ioe);
        }
    }

    /**
     * Gets the debitTotalProperty attribute. 
     * @return Returns the debitTotalProperty.
     */
    public String getDebitTotalProperty() {
        return debitTotalProperty;
    }

    /**
     * Sets the debitTotalProperty attribute value.
     * @param debitTotalProperty The debitTotalProperty to set.
     */
    public void setDebitTotalProperty(String debitTotalProperty) {
        this.debitTotalProperty = debitTotalProperty;
    }

    /**
     * Gets the creditTotalProperty attribute. 
     * @return Returns the creditTotalProperty.
     */
    public String getCreditTotalProperty() {
        return creditTotalProperty;
    }

    /**
     * Sets the creditTotalProperty attribute value.
     * @param creditTotalProperty The creditTotalProperty to set.
     */
    public void setCreditTotalProperty(String creditTotalProperty) {
        this.creditTotalProperty = creditTotalProperty;
    }

    /**
     * Gets the debitTotalLabelProperty attribute. 
     * @return Returns the debitTotalLabelProperty.
     */
    public String getDebitTotalLabelProperty() {
        return debitTotalLabelProperty;
    }

    /**
     * Sets the debitTotalLabelProperty attribute value.
     * @param debitTotalLabelProperty The debitTotalLabelProperty to set.
     */
    public void setDebitTotalLabelProperty(String debitTotalLabelProperty) {
        this.debitTotalLabelProperty = debitTotalLabelProperty;
    }

    /**
     * Gets the creditTotalLabelProperty attribute. 
     * @return Returns the creditTotalLabelProperty.
     */
    public String getCreditTotalLabelProperty() {
        return creditTotalLabelProperty;
    }

    /**
     * Sets the creditTotalLabelProperty attribute value.
     * @param creditTotalLabelProperty The creditTotalLabelProperty to set.
     */
    public void setCreditTotalLabelProperty(String creditTotalLabelProperty) {
        this.creditTotalLabelProperty = creditTotalLabelProperty;
    }
}
