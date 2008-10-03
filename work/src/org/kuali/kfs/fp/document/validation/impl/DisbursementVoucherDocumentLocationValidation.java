/*
 * Copyright 2008 The Kuali Foundation.
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
package org.kuali.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.ParameterService;
import org.kuali.rice.kns.bo.user.UniversalUser;
import org.kuali.rice.kns.exception.UserNotFoundException;
import org.kuali.rice.kns.service.UniversalUserService;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;

public class DisbursementVoucherDocumentLocationValidation extends GenericValidation implements DisbursementVoucherRuleConstants {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherDocumentLocationValidation.class);

    private ParameterService parameterService;
    private AccountingDocument accountingDocumentForValidation;

    /**
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        DisbursementVoucherDocument document = (DisbursementVoucherDocument) accountingDocumentForValidation;
        DisbursementVoucherPayeeDetail payeeDetail = document.getDvPayeeDetail();
        String documentationLocationCode = document.getDisbursementVoucherDocumentationLocationCode();

        ErrorMap errors = GlobalVariables.getErrorMap();

        // payment reason restrictions
        if (ObjectUtils.isNotNull(payeeDetail.getDisbVchrPaymentReasonCode())) {
            parameterService.getParameterEvaluator(document.getClass(), DisbursementVoucherRuleConstants.VALID_DOC_LOC_BY_PAYMENT_REASON_PARM, DisbursementVoucherRuleConstants.INVALID_DOC_LOC_BY_PAYMENT_REASON_PARM, payeeDetail.getDisbVchrPaymentReasonCode(), documentationLocationCode).evaluateAndAddError(document.getClass(), KFSPropertyConstants.DISBURSEMENT_VOUCHER_DOCUMENTATION_LOCATION_CODE);
        }

        // alien indicator restrictions
        if (payeeDetail.isDisbVchrAlienPaymentCode()) {
            parameterService.getParameterEvaluator(DisbursementVoucherDocument.class, ALIEN_INDICATOR_CHECKED_PARM_NM, documentationLocationCode).evaluateAndAddError(document.getClass(), KFSPropertyConstants.DISBURSEMENT_VOUCHER_DOCUMENTATION_LOCATION_CODE);
        }

        ChartOrgHolder chartOrg = SpringContext.getBean(FinancialSystemUserService.class).getOrganizationByModuleId(getInitiator(document), KFSConstants.Modules.CHART);
        String locationCode = (chartOrg == null || chartOrg.getOrganization() == null) ? null : chartOrg.getOrganization().getOrganizationPhysicalCampusCode();

        // initiator campus code restrictions
        parameterService.getParameterEvaluator(DisbursementVoucherDocument.class, DisbursementVoucherRuleConstants.VALID_DOC_LOC_BY_CAMPUS_PARM, DisbursementVoucherRuleConstants.INVALID_DOC_LOC_BY_CAMPUS_PARM, locationCode, documentationLocationCode).evaluateAndAddError(document.getClass(), KFSPropertyConstants.DISBURSEMENT_VOUCHER_DOCUMENTATION_LOCATION_CODE);

        return errors.isEmpty();
    }

    /**
     * Returns the initiator of the document as a KualiUser
     * 
     * @param document submitted document
     * @return <code>KualiUser</code>
     */
    private UniversalUser getInitiator(AccountingDocument document) {
        UniversalUser initUser = null;
        try {
            initUser = SpringContext.getBean(UniversalUserService.class).getUniversalUserByAuthenticationUserId(document.getDocumentHeader().getWorkflowDocument().getInitiatorNetworkId());
        }
        catch (UserNotFoundException e) {
            throw new RuntimeException("Document Initiator not found " + e.getMessage());
        }

        return initUser;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * 
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }

    /**
     * Sets the parameterService attribute value.
     * 
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
