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

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherPayeeDetail;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.sys.service.ParameterEvaluator;
import org.kuali.kfs.sys.service.ParameterService;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.kns.bo.user.PersonTaxId;
import org.kuali.rice.kns.bo.user.UniversalUser;
import org.kuali.rice.kns.exception.UserNotFoundException;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.UniversalUserService;
import org.kuali.rice.kns.util.ErrorMap;
import org.kuali.rice.kns.util.GlobalVariables;

public class DisbursementVoucherVendorInformationValidation extends GenericValidation implements DisbursementVoucherRuleConstants {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisbursementVoucherPaymentReasonValidation.class);

    private ParameterService parameterService;
    private AccountingDocument accountingDocumentForValidation;

    public static final String DV_PAYEE_ID_NUMBER_PROPERTY_PATH = KFSPropertyConstants.DV_PAYEE_DETAIL + "." + KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER;

    public boolean validate(AttributedDocumentEvent event) {
        DisbursementVoucherDocument document = (DisbursementVoucherDocument) accountingDocumentForValidation;
        DisbursementVoucherPayeeDetail payeeDetail = document.getDvPayeeDetail();

        if (!payeeDetail.isVendor()) {
            return true;
        }

        if (StringUtils.isBlank(payeeDetail.getDisbVchrPayeeIdNumber())) {
            return false;
        }

        VendorDetail vendor = retrieveVendorDetail(payeeDetail.getDisbVchrVendorHeaderIdNumberAsInteger(), payeeDetail.getDisbVchrVendorDetailAssignedIdNumberAsInteger());
        ErrorMap errors = GlobalVariables.getErrorMap();

        /* Retrieve Vendor */
        if (vendor == null) {
            errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, KFSKeyConstants.ERROR_EXISTENCE, SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(DisbursementVoucherPayeeDetail.class, KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER));
            return false;
        }

        /* DV Vendor Detail must be active */
        if (!vendor.isActiveIndicator()) {
            errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, KFSKeyConstants.ERROR_INACTIVE, SpringContext.getBean(DataDictionaryService.class).getAttributeLabel(DisbursementVoucherPayeeDetail.class, KFSPropertyConstants.DISB_VCHR_PAYEE_ID_NUMBER));
            return false;
        }

        /* for vendors with tax type ssn, check employee restrictions */
        if (TAX_TYPE_SSN.equals(vendor.getVendorHeader().getVendorTaxTypeCode())) {
            if (isActiveEmployeeSSN(vendor.getVendorHeader().getVendorTaxNumber())) {
                // determine if the rule is flagged off in the param setting
                boolean performPrepaidEmployeeInd = parameterService.getIndicatorParameter(DisbursementVoucherDocument.class, PERFORM_PREPAID_EMPL_PARM_NM);

                if (performPrepaidEmployeeInd) {
                    /* active vendor employees cannot be paid for prepaid travel */
                    ParameterEvaluator travelPrepaidPaymentReasonCodeEvaluator = parameterService.getParameterEvaluator(DisbursementVoucherDocument.class, PREPAID_TRAVEL_PAY_REASONS_PARM_NM, payeeDetail.getDisbVchrPaymentReasonCode());
                    if (travelPrepaidPaymentReasonCodeEvaluator.evaluationSucceeds()) {
                        errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, KFSKeyConstants.ERROR_DV_ACTIVE_EMPLOYEE_PREPAID_TRAVEL);
                    }

                }
            }
            else if (isEmployeeSSN(vendor.getVendorHeader().getVendorTaxNumber())) {
                // check param setting for paid outside payroll check
                boolean performPaidOutsidePayrollInd = parameterService.getIndicatorParameter(DisbursementVoucherDocument.class, DisbursementVoucherRuleConstants.CHECK_EMPLOYEE_PAID_OUTSIDE_PAYROLL_PARM_NM);

                if (performPaidOutsidePayrollInd) {
                    /* If vendor is type employee, vendor record must be flagged as paid outside of payroll */
                    if (!SpringContext.getBean(VendorService.class).isVendorInstitutionEmployee(vendor.getVendorHeaderGeneratedIdentifier())) {
                        errors.putError(DV_PAYEE_ID_NUMBER_PROPERTY_PATH, KFSKeyConstants.ERROR_DV_EMPLOYEE_PAID_OUTSIDE_PAYROLL);
                    }
                }
            }
        }

        return false;
    }

    /**
     * Retrieves the VendorDetail object from the vendor id number.
     * 
     * @param vendorIdNumber vendor ID number
     * @param vendorDetailIdNumber vendor detail ID number
     * @return <code>VendorDetail</code>
     */
    private VendorDetail retrieveVendorDetail(Integer vendorIdNumber, Integer vendorDetailIdNumber) {
        return SpringContext.getBean(VendorService.class).getVendorDetail(vendorIdNumber, vendorDetailIdNumber);
    }

    /**
     * Retrieves UniversalUser from SSN
     * 
     * @param ssnNumber social security number
     * @return <code>UniversalUser</code>
     */
    private UniversalUser retrieveEmployeeBySSN(String ssnNumber) {
        PersonTaxId personTaxId = new PersonTaxId(ssnNumber);
        UniversalUser user = null;
        try {
            user = SpringContext.getBean(UniversalUserService.class).getUniversalUser(personTaxId);
        }
        catch (UserNotFoundException e) {
            LOG.error("User Not Found", e);
        }
        return user;
    }

    /**
     * Confirms that the SSN provided is associated with an employee.
     * 
     * @param ssnNumber social security number
     * @return true if the ssn number is a valid employee ssn
     */
    private boolean isEmployeeSSN(String ssnNumber) {
        return retrieveEmployeeBySSN(ssnNumber) != null;
    }

    /**
     * Performs a lookup on universal users for the given ssn number.
     * 
     * @param ssnNumber social security number
     * @return true if the ssn number is a valid employee ssn and the employee is active
     */
    private boolean isActiveEmployeeSSN(String ssnNumber) {
        UniversalUser employee = retrieveEmployeeBySSN(ssnNumber);
        return employee != null && KFSConstants.EMPLOYEE_ACTIVE_STATUS.equals(employee.getEmployeeStatusCode());
    }

    /**
     * Sets the parameterService attribute value.
     * 
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * 
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setAccountingDocumentForValidation(AccountingDocument accountingDocumentForValidation) {
        this.accountingDocumentForValidation = accountingDocumentForValidation;
    }
}
