/*
 * Copyright 2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.fp.web.struts;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.businessobject.CashDrawer;
import org.kuali.kfs.fp.document.CashManagementDocument;
import org.kuali.kfs.fp.service.CashDrawerService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DocumentTypeService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.UrlFactory;
import org.kuali.rice.kns.web.struts.action.KualiAction;

public class CashDrawerCorrectionAction extends KualiAction {
    public static final String CASH_MANAGEMENT_FORWARD = "cashManagementReturn";
    public static final String TAB_ERROR_KEY = "cashDrawerErrors";

    public CashDrawerCorrectionAction() {
        super();
    }

    /**
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#execute(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward dest = super.execute(mapping, form, request, response);

        return dest;
    }

    public ActionForward startCorrections(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashDrawerCorrectionForm cdcForm = (CashDrawerCorrectionForm) form;
        if (cdcForm.getCashDrawer().getCampusCode() == null) {
            String workgroupName = request.getParameter("campusCode");
            cdcForm.setCashDrawer(SpringContext.getBean(CashDrawerService.class).getByCampusCode(workgroupName, false));
        }
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    public ActionForward saveCashDrawer(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashDrawerCorrectionForm cdcForm = (CashDrawerCorrectionForm) form;
        CashDrawer drawer = cdcForm.getCashDrawer();

        // validate cash drawer
        if (drawer.getFinancialDocumentHundredDollarAmount() != null && drawer.getFinancialDocumentHundredDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "hundred dollar", drawer.getFinancialDocumentHundredDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentFiftyDollarAmount() != null && drawer.getFinancialDocumentFiftyDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "fifty dollar", drawer.getFinancialDocumentFiftyDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentTwentyDollarAmount() != null && drawer.getFinancialDocumentTwentyDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "twenty dollar", drawer.getFinancialDocumentTwentyDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentTenDollarAmount() != null && drawer.getFinancialDocumentTenDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "ten dollar", drawer.getFinancialDocumentTenDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentFiveDollarAmount() != null && drawer.getFinancialDocumentFiveDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "five dollar", drawer.getFinancialDocumentFiveDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentTwoDollarAmount() != null && drawer.getFinancialDocumentTwoDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "two dollar", drawer.getFinancialDocumentTwoDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentOneDollarAmount() != null && drawer.getFinancialDocumentOneDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "one dollar", drawer.getFinancialDocumentOneDollarAmount().toString() });
        }
        if (drawer.getFinancialDocumentOtherDollarAmount() != null && drawer.getFinancialDocumentHundredDollarAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "other dollar", drawer.getFinancialDocumentOtherDollarAmount().toString() });
        }

        if (drawer.getFinancialDocumentHundredCentAmount() != null && drawer.getFinancialDocumentHundredCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "hundred cent", drawer.getFinancialDocumentHundredCentAmount().toString() });
        }
        if (drawer.getFinancialDocumentFiftyCentAmount() != null && drawer.getFinancialDocumentFiftyCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "fifty cent", drawer.getFinancialDocumentFiftyCentAmount().toString() });
        }
        if (drawer.getFinancialDocumentTwentyFiveCentAmount() != null && drawer.getFinancialDocumentTwentyFiveCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "twenty five cent", drawer.getFinancialDocumentTwentyFiveCentAmount().toString() });
        }
        if (drawer.getFinancialDocumentTenCentAmount() != null && drawer.getFinancialDocumentTenCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "ten cent", drawer.getFinancialDocumentTenCentAmount().toString() });
        }
        if (drawer.getFinancialDocumentFiveCentAmount() != null && drawer.getFinancialDocumentFiveCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "five cent", drawer.getFinancialDocumentFiveCentAmount().toString() });
        }
        if (drawer.getFinancialDocumentOneCentAmount() != null && drawer.getFinancialDocumentOneCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "one cent", drawer.getFinancialDocumentOneCentAmount().toString() });
        }
        if (drawer.getFinancialDocumentOtherCentAmount() != null && drawer.getFinancialDocumentOtherCentAmount().isLessThan(KualiDecimal.ZERO)) {
            GlobalVariables.getErrorMap().putError(TAB_ERROR_KEY, KFSKeyConstants.CashManagement.ERROR_CASH_DRAWER_CORRECTION_NEGATIVE_AMOUNT, new String[] { "other cent", drawer.getFinancialDocumentOtherCentAmount().toString() });
        }

        if (!GlobalVariables.getErrorMap().isEmpty()) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }
        // save the drawer
        SpringContext.getBean(BusinessObjectService.class).save(drawer);
        return returnToSender();
    }

    public ActionForward cancelCorrections(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return returnToSender();
    }

    private ActionForward returnToSender() {
        String cmDocTypeName = SpringContext.getBean(DocumentTypeService.class).getDocumentTypeNameByClass(CashManagementDocument.class);

        Properties params = new Properties();
        params.setProperty("methodToCall", "docHandler");
        params.setProperty("command", "initiate");
        params.setProperty("docTypeName", "CashManagementDocument");

        String cmActionUrl = UrlFactory.parameterizeUrl(KFSConstants.CASH_MANAGEMENT_DOCUMENT_ACTION, params);

        return new ActionForward(cmActionUrl, true);
    }
}
