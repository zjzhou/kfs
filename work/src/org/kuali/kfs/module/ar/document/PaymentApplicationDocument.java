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
package org.kuali.kfs.module.ar.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.OffsetDefinition;
import org.kuali.kfs.coa.service.BalanceTypService;
import org.kuali.kfs.coa.service.OffsetDefinitionService;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.InvoicePaidApplied;
import org.kuali.kfs.module.ar.businessobject.NonAppliedDistribution;
import org.kuali.kfs.module.ar.businessobject.NonAppliedHolding;
import org.kuali.kfs.module.ar.businessobject.NonInvoiced;
import org.kuali.kfs.module.ar.businessobject.NonInvoicedDistribution;
import org.kuali.kfs.module.ar.businessobject.ReceivableCustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.SystemInformation;
import org.kuali.kfs.module.ar.document.service.PaymentApplicationDocumentService;
import org.kuali.kfs.module.ar.document.service.SystemInformationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocumentBase;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.rule.event.KualiDocumentEvent;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

public class PaymentApplicationDocument extends GeneralLedgerPostingDocumentBase implements GeneralLedgerPendingEntrySource {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentApplicationDocument.class);

    private String hiddenFieldForErrors;
    private List<InvoicePaidApplied> invoicePaidApplieds;
    private List<NonInvoiced> nonInvoiceds;
    private Collection<NonInvoicedDistribution> nonInvoicedDistributions;
    private Collection<NonAppliedDistribution> nonAppliedDistributions;
    private NonAppliedHolding nonAppliedHolding;
    private AccountsReceivableDocumentHeader accountsReceivableDocumentHeader;
    private boolean checkedForCashControlDocument;
    
    private transient PaymentApplicationDocumentService paymentApplicationDocumentService;
    private transient CashControlDetail cashControlDetail;
    private transient FinancialSystemUserService fsUserService;
    
    public PaymentApplicationDocument() {
        super();
        this.invoicePaidApplieds = new ArrayList<InvoicePaidApplied>();
        this.nonInvoiceds = new ArrayList<NonInvoiced>();
        this.nonInvoicedDistributions = new ArrayList<NonInvoicedDistribution>();
        this.nonAppliedDistributions = new ArrayList<NonAppliedDistribution>();
        checkedForCashControlDocument = false;
    }

    /**
     * Returns the PaymentMediumIdentifier on the associated CashControlDetail, 
     * if one exists, otherwise returns null.
     * @return CustomerPaymentMediumIdentifier from the associated CashControlDetail if 
     *         one exists, otherwise null.
     */
    public String getPaymentNumber() {
        return hasCashControlDocument() ? getCashControlDetail().getCustomerPaymentMediumIdentifier() : null;
    }

    /**
     * @return
     * @throws WorkflowException
     */
    public CashControlDocument getCashControlDocument() {
        CashControlDetail cashControlDetail = getCashControlDetail();
        if(ObjectUtils.isNull(cashControlDetail)) {
            return null;
        }
        return cashControlDetail.getCashControlDocument();
    }

    public boolean hasCashControlDocument() {
        return (null != getCashControlDocument());
    }
    
    /**
     * @return
     * @throws WorkflowException
     */
    public CashControlDetail getCashControlDetail() {
        // the 'checkedForCashControlDocument' is a micro-optimization to have it not 
        // have to hit the db every time this method is hit when its a non-cash-control 
        // type of payapp
        if (cashControlDetail == null && !checkedForCashControlDocument) {
            cashControlDetail = getPaymentApplicationDocumentService().getCashControlDetailForPayAppDocNumber(getDocumentNumber());
            checkedForCashControlDocument = true;
        }
        return cashControlDetail;
    }
    
    public void setCashControlDetail(CashControlDetail cashControlDetail) {
        this.cashControlDetail = cashControlDetail;
    }
    
    /**
     * @return
     * @throws WorkflowException
     */
    public KualiDecimal getTotalFromCashControl() {
        return hasCashControlDocument() ? KualiDecimal.ZERO : getCashControlDetail().getFinancialDocumentLineAmount();
    }
    
    /**
     * @return the sum of all invoice paid applieds.
     */
    public KualiDecimal getSumOfInvoicePaidApplieds() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for(InvoicePaidApplied payment : getInvoicePaidApplieds()) {
            KualiDecimal _amount = payment.getInvoiceItemAppliedAmount();
            if (null == _amount) { _amount = KualiDecimal.ZERO; }
            amount = amount.add(_amount);
        }
        return amount;
    }
    
    /**
     * @return the sum of all non-invoiced amounts
     */
    public KualiDecimal getSumOfNonInvoiceds() {
        KualiDecimal total = KualiDecimal.ZERO;
        for(NonInvoiced payment : getNonInvoiceds()) {
            total = total.add(payment.getFinancialDocumentLineAmount());
        }
        return total;
    }
    
    /**
     * @return the sum of all non-invoiced distributions
     */
    public KualiDecimal getSumOfNonInvoicedDistributions() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for(NonInvoicedDistribution nonInvoicedDistribution : getNonInvoicedDistributions()) {
            amount = amount.add(nonInvoicedDistribution.getFinancialDocumentLineAmount());
        }
        return amount;
    }
    
    /**
     * @return the sum of all non-applied distributions
     */
    public KualiDecimal getSumOfNonAppliedDistributions() {
        KualiDecimal amount = KualiDecimal.ZERO;
        for(NonAppliedDistribution nonAppliedDistribution : getNonAppliedDistributions()) {
            amount = amount.add(nonAppliedDistribution.getFinancialDocumentLineAmount());
        }
        return amount;
    }
    
    /**
     * @return the non-applied holding total.
     */
    public KualiDecimal getNonAppliedHoldingAmount() {
        if(ObjectUtils.isNull(getNonAppliedHolding())) {
            return KualiDecimal.ZERO;
        }
        if(ObjectUtils.isNull(getNonAppliedHolding().getFinancialDocumentLineAmount())) {
            return KualiDecimal.ZERO;
        }
        return getNonAppliedHolding().getFinancialDocumentLineAmount();
    }
    
    /**
     * This method returns the total amount allocated against the cash
     * control total.
     * 
     * @return
     */
    public KualiDecimal getTotalApplied() {
        KualiDecimal amount = KualiDecimal.ZERO;

        // The amount received via the cash control document
        KualiDecimal ccta = getTotalFromCashControl();
        
        // The amount received via the cash control document minus the amount applied.
        KualiDecimal btba = getUnallocatedBalance();
        
        // The difference between the two is the amount applied.
        amount = ccta.subtract(btba);
        return amount;
    }
    
    /**
     * This method subtracts the sum of the invoice paid applieds, non-ar and 
     * unapplied totals from the outstanding amount received via the cash
     * control document.
     * 
     * @return
     * @throws WorkflowException
     */
    public KualiDecimal getUnallocatedBalance() {
        
        //  if this payapp doc isnt based on a cash control doc, then there 
        // will be no cash control details, so no balance to be applied
        //TODO Andrew - this will have to be updated for non-cash-control docs
        if (!hasCashControlDocument()) {
            return KualiDecimal.ZERO;
        }
        
        // KULAR-504: "Balance" must equal line item amount from Cash Control - not the total Cash Control
        KualiDecimal amount = getCashControlDetail().getFinancialDocumentLineAmount();
        
        KualiDecimal subtrahend = getSumOfInvoicePaidApplieds();
        if(ObjectUtils.isNotNull(subtrahend)) {
            amount = amount.subtract(subtrahend);
        }
        subtrahend = getSumOfNonInvoiceds();
        if(ObjectUtils.isNotNull(subtrahend)) {
            amount = amount.subtract(subtrahend);
        }
        subtrahend = getNonAppliedHoldingAmount();
        if(ObjectUtils.isNotNull(subtrahend)) {
            amount = amount.subtract(subtrahend);
        }
        return amount;
    }
    
    public KualiDecimal getNonArTotal() {
        KualiDecimal total = KualiDecimal.ZERO;
        for (NonInvoiced item : getNonInvoiceds()) {
            total = total.add(item.getFinancialDocumentLineAmount());
        }
        return total;
    }

    public List<InvoicePaidApplied> getInvoicePaidApplieds() {
        return invoicePaidApplieds;
    }

    public void setInvoicePaidApplieds(List<InvoicePaidApplied> appliedPayments) {
        this.invoicePaidApplieds = appliedPayments;
    }

    public List<NonInvoiced> getNonInvoiceds() {
        return nonInvoiceds;
    }

    public void setNonInvoiceds(List<NonInvoiced> nonInvoiceds) {
        this.nonInvoiceds = nonInvoiceds;
    }

    public Collection<NonInvoicedDistribution> getNonInvoicedDistributions() {
        return nonInvoicedDistributions;
    }

    public void setNonInvoicedDistributions(Collection<NonInvoicedDistribution> nonInvoicedDistributions) {
        this.nonInvoicedDistributions = nonInvoicedDistributions;
    }

    public Collection<NonAppliedDistribution> getNonAppliedDistributions() {
        return nonAppliedDistributions;
    }

    public void setNonAppliedDistributions(Collection<NonAppliedDistribution> nonAppliedDistributions) {
        this.nonAppliedDistributions = nonAppliedDistributions;
    }

    public NonAppliedHolding getNonAppliedHolding() {
        return nonAppliedHolding;
    }

    public void setNonAppliedHolding(NonAppliedHolding nonAppliedHolding) {
        this.nonAppliedHolding = nonAppliedHolding;
    }

    public AccountsReceivableDocumentHeader getAccountsReceivableDocumentHeader() {
        return accountsReceivableDocumentHeader;
    }

    public void setAccountsReceivableDocumentHeader(AccountsReceivableDocumentHeader accountsReceivableDocumentHeader) {
        this.accountsReceivableDocumentHeader = accountsReceivableDocumentHeader;
    }

    /**
     * This method retrieves a specific applied payment from the list, by array index
     * 
     * @param index the index of the applied payment to retrieve
     * @return an InvoicePaidApplied
     */
    public InvoicePaidApplied getInvoicePaidApplied(int index) {
        
        return index < getInvoicePaidApplieds().size() ? getInvoicePaidApplieds().get(index) : new InvoicePaidApplied();
        //TODO (Andrew) I dont think we need to do this stuff below here, as 
        //     we can just return a new empty PaidApplied if out of index, I 
        //     dont see why we'd want to add it in.  But I'm not sure, so thats 
        //     why I'm leaving it here commented out, and this comment.
        //if (index >= invoicePaidApplieds.size()) {
        //    for (int i = invoicePaidApplieds.size(); i <= index; i++) {
        //        invoicePaidApplieds.add(new InvoicePaidApplied());
        //    }
        //}
        //return invoicePaidApplieds.get(index);
    }

    /**
     * This method retrieves a specific non invoiced payment from the list, by array index
     * 
     * @param index the index of the non invoiced payment to retrieve
     * @return an NonInvoiced
     */
    public NonInvoiced getNonInvoiced(int index) {
        return index < getNonInvoiceds().size() ? getNonInvoiceds().get(index) : new NonInvoiced();
        //TODO (Andrew) see my comment above on getInvoicePaidApplied(int index)
//        if (index >= nonInvoiceds.size()) {
//            for (int i = nonInvoiceds.size(); i <= index; i++) {
//                nonInvoiceds.add(new NonInvoiced());
//            }
//        }
//        return nonInvoiceds.get(index);
    }

    //TODO Andrew - Nothing is using this, so its queued for removal
    //public ObjectCode getUnappliedCashObjectCode(InvoicePaidApplied ipa) {
    //    return ipa.getSystemInformation().getUniversityClearingObject();
    //}

    //TODO Andrew - Nothing is using this, so its queued for removal
    //public ObjectCode getCreditCardChargesObjectCode(InvoicePaidApplied ipa) {
    //    return ipa.getSystemInformation().getCreditCardFinancialObject();
    //}
    
    //TODO Andrew - Nothing is using this, so its queued for removal
    //private ObjectCode getCashObjectCode(InvoicePaidApplied invoicePaidApplied) {
    //    invoicePaidApplied.refresh();
    //    CustomerInvoiceDetail detail = invoicePaidApplied.getInvoiceDetail();
    //    detail.refresh();
    //    Chart chart = detail.getChart();
    //    chart.refresh();
    //    chart.refreshReferenceObject("financialCashObject");
    //    return chart.getFinancialCashObject();
    //}
    
    /**
     * This method gets an ObjectCode from an invoice document.
     * 
     * @param invoicePaidApplied
     * @return
     * @throws WorkflowException
     */
    private ObjectCode getInvoiceReceivableObjectCode(InvoicePaidApplied invoicePaidApplied) throws WorkflowException {
        CustomerInvoiceDocument customerInvoiceDocument = invoicePaidApplied.getCustomerInvoiceDocument();
        CustomerInvoiceDetail customerInvoiceDetail = invoicePaidApplied.getInvoiceDetail();
        ReceivableCustomerInvoiceDetail receivableInvoiceDetail = new ReceivableCustomerInvoiceDetail(customerInvoiceDetail, customerInvoiceDocument);
        ObjectCode objectCode = null;
        if(ObjectUtils.isNotNull(receivableInvoiceDetail) && ObjectUtils.isNotNull(receivableInvoiceDetail.getFinancialObjectCode())) {
            objectCode = receivableInvoiceDetail.getObjectCode();
        }
        return objectCode;
    }
    
    /**
     * This method returns the CashControlDocument for a PaymentApplicationDocument.
     * If the PaymentApplicationDocument does not have a CashControlDocument itself,
     * it will walk up through the tree of parent PaymentApplicationDocuments in
     * order to find one that does. The only way for a PaymentApplicationDocument to
     * be created without a CashControlDocument is via an unapplied line on 
     * another PaymentApplicationDocument. So we have to find that one.
     * 
     * @param paymentApplicationDocument
     * @return
     * @throws WorkflowException
     */
    //TODO Vivek - Can you please look at this, and if you dont need it, get rid of it?
    private CashControlDocument getCashControlDocument(PaymentApplicationDocument paymentApplicationDocument) throws WorkflowException {
        CashControlDocument cashControlDocument = paymentApplicationDocument.getCashControlDocument();
        if(null == cashControlDocument) {
            // FIXME Add in the recursive parent search to find a CashControlDocument 
            // if one isn't present on the PaymentApplicationDocument passed in.
        }
        return cashControlDocument;
    }
    
    /**
     * @param sequenceHelper
     * @return the pending entries for the document
     */
    //TODO Andrew - Move this to the PayApp doc service, it belongs there
    private List<GeneralLedgerPendingEntry> createPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) throws WorkflowException {
        
        // Collection of all generated entries
        List<GeneralLedgerPendingEntry> generatedEntries = new ArrayList<GeneralLedgerPendingEntry>();
        
        // Get handles to the services we need
        GeneralLedgerPendingEntryService glpeService = SpringContext.getBean(GeneralLedgerPendingEntryService.class);
        BalanceTypService balanceTypeService = SpringContext.getBean(BalanceTypService.class);
        UniversityDateService universityDateService = SpringContext.getBean(UniversityDateService.class);
        SystemInformationService systemInformationService = SpringContext.getBean(SystemInformationService.class);
        OffsetDefinitionService offsetDefinitionService = SpringContext.getBean(OffsetDefinitionService.class);
        ParameterService parameterService = SpringContext.getBean(ParameterService.class);
        DataDictionaryService dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        
        // Current fiscal year
        Integer currentFiscalYear = universityDateService.getCurrentFiscalYear();
        
        // Document type codes
        String cashControlDocumentTypeCode = dataDictionaryService.getDocumentTypeNameByClass(CashControlDocument.class);
        String paymentApplicationDocumentTypeCode = dataDictionaryService.getDocumentTypeNameByClass(PaymentApplicationDocument.class); 
        
        // The processing chart and org comes from the the cash control document if there is one.
        // If the payment application document is created from scratch though, then we pull it 
        // from the current user.  Note that we're not checking here that the user actually belongs 
        // to a billing or processing org, we're assuming that is handled elsewhere.
        String processingChartCode = null;
        String processingOrganizationCode = null;
        if (hasCashControlDocument()) {
            processingChartCode = getCashControlDocument().getAccountsReceivableDocumentHeader().getProcessingChartOfAccountCode();
            processingOrganizationCode = getCashControlDocument().getAccountsReceivableDocumentHeader().getProcessingOrganizationCode();
        }
        else {
            Person currentUser = GlobalVariables.getUserSession().getPerson();
            ChartOrgHolder userOrg = getFsUserService().getPrimaryOrganization(currentUser.getPrincipalId(), ArConstants.AR_NAMESPACE_CODE);
            processingChartCode = userOrg.getChartOfAccountsCode();
            processingOrganizationCode = userOrg.getOrganizationCode();
        }

        // Some information comes from the cash control document
        CashControlDocument cashControlDocument = getCashControlDocument();
        
        // Get the university clearing account
        SystemInformation unappliedSystemInformation = 
            systemInformationService.getByProcessingChartOrgAndFiscalYear(
                    processingChartCode, processingOrganizationCode, currentFiscalYear);
        
        // Get the university clearing account
        unappliedSystemInformation.refreshReferenceObject("universityClearingAccount");
        Account universityClearingAccount = unappliedSystemInformation.getUniversityClearingAccount();
        
        // Get the university clearing sub-object code
        String unappliedSubObjectCode = unappliedSystemInformation.getUniversityClearingSubObjectCode();
        
        // Get the object code for the university clearing account.
        SystemInformation universityClearingAccountSystemInformation = 
            systemInformationService.getByProcessingChartOrgAndFiscalYear(
                    processingChartCode, processingOrganizationCode, currentFiscalYear);
        String universityClearingAccountObjectCode = universityClearingAccountSystemInformation.getUniversityClearingObjectCode();
        
        // Generate glpes for unapplied
        NonAppliedHolding holding = getNonAppliedHolding();
        if(ObjectUtils.isNotNull(holding)) {
            GeneralLedgerPendingEntry actualCreditUnapplied = new GeneralLedgerPendingEntry();
            actualCreditUnapplied.setUniversityFiscalYear(getPostingYear());
            actualCreditUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            actualCreditUnapplied.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualCreditUnapplied.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualCreditUnapplied.setFinancialObjectCode(unappliedSystemInformation.getUniversityClearingObjectCode());
            actualCreditUnapplied.setFinancialObjectTypeCode(unappliedSystemInformation.getUniversityClearingObject().getFinancialObjectTypeCode());
            actualCreditUnapplied.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            actualCreditUnapplied.setTransactionLedgerEntryAmount(holding.getFinancialDocumentLineAmount());
            actualCreditUnapplied.setFinancialSubObjectCode(unappliedSubObjectCode);
            actualCreditUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualCreditUnapplied);
            sequenceHelper.increment();
            
            GeneralLedgerPendingEntry actualDebitUnapplied = new GeneralLedgerPendingEntry();
            actualDebitUnapplied.setUniversityFiscalYear(getPostingYear());
            actualDebitUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            actualDebitUnapplied.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualDebitUnapplied.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualDebitUnapplied.setFinancialObjectCode(universityClearingAccountObjectCode);
            actualDebitUnapplied.setFinancialObjectTypeCode(unappliedSystemInformation.getUniversityClearingObject().getFinancialObjectTypeCode());
            actualDebitUnapplied.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            actualDebitUnapplied.setTransactionLedgerEntryAmount(holding.getFinancialDocumentLineAmount());
            // Only need the sub object code on the debit if the payment application document was not created via a cash control document
            if(null == cashControlDocument) {
                actualDebitUnapplied.setFinancialSubObjectCode(unappliedSubObjectCode);
            }
            actualDebitUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualDebitUnapplied);
            sequenceHelper.increment();
            
            // Offsets for unapplied entries are just offsets to themselves, same info.
            // So set the values into the offsets based on the values in the actuals.
            GeneralLedgerPendingEntry offsetCreditUnapplied = new GeneralLedgerPendingEntry();
            offsetCreditUnapplied.setUniversityFiscalYear(actualDebitUnapplied.getUniversityFiscalYear());
            offsetCreditUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            offsetCreditUnapplied.setChartOfAccountsCode(actualDebitUnapplied.getChartOfAccountsCode());
            offsetCreditUnapplied.setAccountNumber(actualDebitUnapplied.getAccountNumber());
            offsetCreditUnapplied.setFinancialObjectCode(actualDebitUnapplied.getFinancialObjectCode());
            offsetCreditUnapplied.setFinancialObjectTypeCode(actualDebitUnapplied.getFinancialObjectTypeCode());
            offsetCreditUnapplied.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            offsetCreditUnapplied.setTransactionLedgerEntryAmount(actualDebitUnapplied.getTransactionLedgerEntryAmount());
            offsetCreditUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(offsetCreditUnapplied);
            sequenceHelper.increment();
            
            GeneralLedgerPendingEntry offsetDebitUnapplied = new GeneralLedgerPendingEntry();
            offsetDebitUnapplied.setUniversityFiscalYear(actualCreditUnapplied.getUniversityFiscalYear());
            offsetDebitUnapplied.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            offsetDebitUnapplied.setChartOfAccountsCode(actualCreditUnapplied.getChartOfAccountsCode());
            offsetDebitUnapplied.setAccountNumber(actualCreditUnapplied.getAccountNumber());
            offsetDebitUnapplied.setFinancialObjectCode(actualCreditUnapplied.getFinancialObjectCode());
            offsetDebitUnapplied.setFinancialObjectTypeCode(actualCreditUnapplied.getFinancialObjectTypeCode());
            offsetDebitUnapplied.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            offsetDebitUnapplied.setTransactionLedgerEntryAmount(actualCreditUnapplied.getTransactionLedgerEntryAmount());
            offsetDebitUnapplied.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(offsetDebitUnapplied);
            sequenceHelper.increment();
        }
        
        // Generate glpes for non-ar
        for(NonInvoiced nonInvoiced : getNonInvoiceds()) {
            // Actual entries
            GeneralLedgerPendingEntry actualCreditEntry = new GeneralLedgerPendingEntry();
            actualCreditEntry.setUniversityFiscalYear(getPostingYear());
            actualCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            actualCreditEntry.setChartOfAccountsCode(nonInvoiced.getChartOfAccountsCode());
            actualCreditEntry.setAccountNumber(nonInvoiced.getAccountNumber());
            actualCreditEntry.setFinancialObjectCode(nonInvoiced.getFinancialObjectCode());
            nonInvoiced.refreshReferenceObject("financialObject");
            actualCreditEntry.setFinancialObjectTypeCode(nonInvoiced.getFinancialObject().getFinancialObjectTypeCode());
            actualCreditEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            actualCreditEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount());
            actualCreditEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualCreditEntry);
            sequenceHelper.increment();
            
            GeneralLedgerPendingEntry actualDebitEntry = new GeneralLedgerPendingEntry();
            actualDebitEntry.setUniversityFiscalYear(getPostingYear());
            actualDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            actualDebitEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualDebitEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualDebitEntry.setFinancialObjectCode(universityClearingAccountObjectCode);
            actualDebitEntry.setFinancialObjectTypeCode(nonInvoiced.getFinancialObject().getFinancialObjectTypeCode());
            actualDebitEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            actualDebitEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount());
            actualDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualDebitEntry);
            sequenceHelper.increment();

            // Offset entries
            GeneralLedgerPendingEntry offsetDebitEntry = new GeneralLedgerPendingEntry();
            offsetDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            offsetDebitEntry.setChartOfAccountsCode(nonInvoiced.getChartOfAccountsCode());
            offsetDebitEntry.setAccountNumber(nonInvoiced.getAccountNumber());
            offsetDebitEntry.setUniversityFiscalYear(getPostingYear());
            OffsetDefinition debitOffsetDefinition = 
                offsetDefinitionService.getByPrimaryId(
                    getPostingYear(), nonInvoiced.getChartOfAccountsCode(), 
                    paymentApplicationDocumentTypeCode, ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            debitOffsetDefinition.refreshReferenceObject("financialObject");
            offsetDebitEntry.setFinancialObjectCode(debitOffsetDefinition.getFinancialObjectCode());
            offsetDebitEntry.setFinancialObjectTypeCode(debitOffsetDefinition.getFinancialObject().getFinancialObjectTypeCode());
            offsetDebitEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            offsetDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetDebitEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount());
            offsetDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(offsetDebitEntry);
            sequenceHelper.increment();
            
            GeneralLedgerPendingEntry offsetCreditEntry = new GeneralLedgerPendingEntry();
            offsetCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            offsetCreditEntry.setUniversityFiscalYear(getPostingYear());
            offsetCreditEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            offsetCreditEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            Integer fiscalYearForCreditOffsetDefinition = null == cashControlDocument ? currentFiscalYear : cashControlDocument.getUniversityFiscalYear();
            OffsetDefinition creditOffsetDefinition = 
                offsetDefinitionService.getByPrimaryId(
                        fiscalYearForCreditOffsetDefinition, processingChartCode, 
                        cashControlDocumentTypeCode, ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            creditOffsetDefinition.refreshReferenceObject("financialObject");
            offsetCreditEntry.setFinancialObjectCode(creditOffsetDefinition.getFinancialObjectCode());
            offsetCreditEntry.setFinancialObjectTypeCode(creditOffsetDefinition.getFinancialObject().getFinancialObjectTypeCode());
            offsetCreditEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            offsetCreditEntry.setFinancialDocumentTypeCode(cashControlDocumentTypeCode);
            offsetCreditEntry.setTransactionLedgerEntryAmount(nonInvoiced.getFinancialDocumentLineAmount());
            offsetCreditEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(offsetCreditEntry);
            sequenceHelper.increment();
        }
        
        // Generate GLPEs for applied payments
        List<InvoicePaidApplied> appliedPayments = getInvoicePaidApplieds();
        for(InvoicePaidApplied ipa : appliedPayments) {
            
            // Skip payments for 0 dollar amount
            if(KualiDecimal.ZERO.equals(ipa.getInvoiceItemAppliedAmount())) {
                continue;
            }
            
            ipa.refreshNonUpdateableReferences();
            Account billingOrganizationAccount = ipa.getInvoiceDetail().getAccount();
            ObjectCode invoiceObjectCode = getInvoiceReceivableObjectCode(ipa);
            ObjectUtils.isNull(invoiceObjectCode); // Refresh 
            ObjectCode accountsReceivableObjectCode = ipa.getAccountsReceivableObjectCode();
            ObjectCode unappliedCashObjectCode = ipa.getSystemInformation().getUniversityClearingObject();
            
            GeneralLedgerPendingEntry actualDebitEntry = new GeneralLedgerPendingEntry();
            actualDebitEntry.setUniversityFiscalYear(getPostingYear());
            actualDebitEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualDebitEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            actualDebitEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount());
            actualDebitEntry.setFinancialObjectCode(unappliedCashObjectCode.getFinancialObjectCode());
            actualDebitEntry.setFinancialObjectTypeCode(unappliedCashObjectCode.getFinancialObjectTypeCode());
            actualDebitEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            actualDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            actualDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualDebitEntry);
            sequenceHelper.increment();
            
            GeneralLedgerPendingEntry actualCreditEntry = new GeneralLedgerPendingEntry();
            actualCreditEntry.setUniversityFiscalYear(getPostingYear());
            actualCreditEntry.setChartOfAccountsCode(universityClearingAccount.getChartOfAccountsCode());
            actualCreditEntry.setAccountNumber(universityClearingAccount.getAccountNumber());
            actualCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            actualCreditEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount());
            actualCreditEntry.setFinancialObjectCode(invoiceObjectCode.getFinancialObjectCode());
            actualCreditEntry.setFinancialObjectTypeCode(invoiceObjectCode.getFinancialObjectTypeCode());
            actualCreditEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            actualCreditEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), actualDebitEntry, sequenceHelper, actualCreditEntry);
            actualCreditEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(actualCreditEntry);
            sequenceHelper.increment();
            
            GeneralLedgerPendingEntry offsetDebitEntry = new GeneralLedgerPendingEntry();
            offsetDebitEntry.setUniversityFiscalYear(getPostingYear());
            offsetDebitEntry.setAccountNumber(billingOrganizationAccount.getAccountNumber());
            offsetDebitEntry.setChartOfAccountsCode(billingOrganizationAccount.getChartOfAccountsCode());
            offsetDebitEntry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            offsetDebitEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount());
            offsetDebitEntry.setFinancialObjectCode(invoiceObjectCode.getFinancialObjectCode());
            offsetDebitEntry.setFinancialObjectTypeCode(invoiceObjectCode.getFinancialObjectTypeCode());
            offsetDebitEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            offsetDebitEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetDebitEntry.setTransactionLedgerEntrySequenceNumber(sequenceHelper.getSequenceCounter());
            generatedEntries.add(offsetDebitEntry);
            sequenceHelper.increment();

            GeneralLedgerPendingEntry offsetCreditEntry = new GeneralLedgerPendingEntry();
            offsetCreditEntry.setUniversityFiscalYear(getPostingYear());
            offsetCreditEntry.setAccountNumber(billingOrganizationAccount.getAccountNumber());
            offsetCreditEntry.setChartOfAccountsCode(billingOrganizationAccount.getChartOfAccountsCode());
            offsetCreditEntry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            offsetCreditEntry.setTransactionLedgerEntryAmount(ipa.getInvoiceItemAppliedAmount());
            offsetCreditEntry.setFinancialObjectCode(accountsReceivableObjectCode.getFinancialObjectCode());
            offsetCreditEntry.setFinancialObjectTypeCode(accountsReceivableObjectCode.getFinancialObjectTypeCode());
            offsetCreditEntry.setFinancialBalanceTypeCode(ArConstants.ACTUALS_BALANCE_TYPE_CODE);
            offsetCreditEntry.setFinancialDocumentTypeCode(paymentApplicationDocumentTypeCode);
            offsetCreditEntry.refreshNonUpdateableReferences();
            glpeService.populateOffsetGeneralLedgerPendingEntry(getPostingYear(), offsetDebitEntry, sequenceHelper, offsetCreditEntry);
            generatedEntries.add(offsetCreditEntry);
            sequenceHelper.increment();
        }
        
        // Set the origination code for all entries.
        for(GeneralLedgerPendingEntry entry : generatedEntries) {
            entry.setFinancialSystemOriginationCode("01");
        }
        
        return generatedEntries;
    }
    
    /**
     * @see org.kuali.kfs.sys.document.GeneralLedgerPendingEntrySource#generateDocumentGeneralLedgerPendingEntries(org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper)
     */
    public boolean generateDocumentGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        try {
            List<GeneralLedgerPendingEntry> entries = createPendingEntries(sequenceHelper);
            for(GeneralLedgerPendingEntry entry : entries) {
                addPendingEntry(entry);
            }
        } catch(Throwable t) {
            LOG.error("Exception encountered while generating pending entries.",t);
            return false;
        }
        
        return true;
    }
    
    public boolean generateGeneralLedgerPendingEntries(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        return true;
    }

    public KualiDecimal getGeneralLedgerPendingEntryAmountForDetail(GeneralLedgerPendingEntrySourceDetail glpeSourceDetail) {
        return null;
    }

    public List<GeneralLedgerPendingEntrySourceDetail> getGeneralLedgerPendingEntrySourceDetails() {
        return new ArrayList<GeneralLedgerPendingEntrySourceDetail>();
    }

    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        return false;
    }

    @Override
    public void handleRouteStatusChange() {
        super.handleRouteStatusChange();
        
        if(getDocumentHeader().getWorkflowDocument().stateIsApproved()) {
            DocumentService documentService = SpringContext.getBean(DocumentService.class);
            DateTimeService dateTimeService = SpringContext.getBean(DateTimeService.class);
            
            //  get the now time to stamp invoices with
            java.sql.Date today = new java.sql.Date(dateTimeService.getCurrentDate().getTime());
            
            for(InvoicePaidApplied ipa : getInvoicePaidApplieds()) {
                String invoiceDocumentNumber = ipa.getFinancialDocumentReferenceInvoiceNumber();
                CustomerInvoiceDocument invoice = null;
                
                //  attempt to retrieve the invoice doc
                try {
                     invoice = (CustomerInvoiceDocument) documentService.getByDocumentHeaderId(invoiceDocumentNumber);
                } catch(WorkflowException we) {
                    LOG.error("Failed to load the Invoice document due to a WorkflowException.", we);
                }
                if (invoice == null) {
                    throw new RuntimeException("DocumentService returned a Null CustomerInvoice Document for Doc# " + invoiceDocumentNumber + ".");
                }
                
                // KULAR-384 - close the invoice if the open amount is zero
                if (KualiDecimal.ZERO.equals(invoice.getOpenAmount())) {
                    invoice.setClosedDate(today);
                    invoice.setOpenInvoiceIndicator(false);
                    documentService.updateDocument(invoice);
                }
            }
        }
    }

    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        super.prepareForSave(event);
        
        // set primary key for NonAppliedHolding if data entered
        if (ObjectUtils.isNotNull(this.nonAppliedHolding)) {
            if (ObjectUtils.isNull(this.nonAppliedHolding.getReferenceFinancialDocumentNumber())) {
                this.nonAppliedHolding.setReferenceFinancialDocumentNumber(this.documentNumber);
            }
        }
        // generate GLPEs
        GeneralLedgerPendingEntryService generalLedgerPendingEntryService = 
            SpringContext.getBean(GeneralLedgerPendingEntryService.class); 
        if (!generalLedgerPendingEntryService.generateGeneralLedgerPendingEntries(this)) {
            logErrors();
            throw new ValidationException("general ledger GLPE generation failed");
        }
    }

    public PaymentApplicationDocumentService getPaymentApplicationDocumentService() {
        if(null == paymentApplicationDocumentService) {
            paymentApplicationDocumentService = SpringContext.getBean(PaymentApplicationDocumentService.class);
        }
        return paymentApplicationDocumentService;
    }

    private FinancialSystemUserService getFsUserService() {
        if (fsUserService == null) {
            fsUserService = SpringContext.getBean(FinancialSystemUserService.class);
        }
        return fsUserService;
    }
    
    public String getHiddenFieldForErrors() {
        return hiddenFieldForErrors;
    }

    public void setHiddenFieldForErrors(String hiddenFieldForErrors) {
        this.hiddenFieldForErrors = hiddenFieldForErrors;
    }

}

