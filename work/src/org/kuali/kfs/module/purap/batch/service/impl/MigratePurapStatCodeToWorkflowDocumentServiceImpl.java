/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.purap.batch.service.impl;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.purap.batch.service.MigratePurapStatCodeToWorkflowDocumentService;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderVendorQuote;
import org.kuali.kfs.module.purap.dataaccess.PurapDocumentsStatusCodeMigrationDao;
import org.kuali.kfs.module.purap.dataaccess.StatusCodeAndDescriptionForPurapDocumentsDao;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.workflow.service.WorkflowDocumentService;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class implements the MigratePurapStatCodeToWorkflowDocumentService batch job.
 */
@Transactional
public class MigratePurapStatCodeToWorkflowDocumentServiceImpl implements MigratePurapStatCodeToWorkflowDocumentService {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(MigratePurapStatCodeToWorkflowDocumentServiceImpl.class);
    public static final String WORKFLOW_DOCUMENT_HEADER_ID_SEARCH_RESULT_KEY = "routeHeaderId";
    
    protected WorkflowDocumentService workflowDocumentService;
    protected StatusCodeAndDescriptionForPurapDocumentsDao statusCodeAndDescriptionForPurapDocumentsDao;
    protected ReportWriterService migratePurapStatCodeReportService;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    
    /**
     * Constructs a AutoDisapproveDocumentsServiceImpl instance
     */
    public MigratePurapStatCodeToWorkflowDocumentServiceImpl() {
        
    }

    /**
     * 
     * @see org.kuali.kfs.module.purap.batch.service.MigratePurapStatCodeToWorkflowDocumentService#migratePurapStatCodeToWorkflowDocuments()
     */
    public boolean migratePurapStatCodeToWorkflowDocuments() {
        LOG.debug("migratePurapStatCodeToWorkflowDocuments() started");
        
        boolean success = true ;
        
        migratePurapStatCodeReportService.writeFormattedMessageLine("******************** Migration of StatusCode to Workflow document started ********************\n");

        //Step 1: Go through REQ documents and move the status code to workflow documents
        success &= processRequisitionDocumentsForStatusCodeMigration();
        
        success &= processPurchaseOrderDocumentsForStatusCodeMigration();

        success &= processPurchaseOrderVendorQuotesForStatusCodeMigration();
        
        success &= processPaymentRequestDocumentsForStatusCodeMigration();
        
        success &= processVendorCreditMemoDocumentsForStatusCodeMigration();
        
        LOG.debug("migratePurapStatCodeToWorkflowDocuments() completed");
        
        return success;
    }
    
    /**
     * Processes the requisitions for status code migration.  Creates a list of documents numbers
     * and current status code where status code needs migration and using that list the workflow documents
     * will be retrieved.  Each workflow document then will be updated with the corresponding
     * application document status that is new in KFS 5.0
     * 
     * @return true if the requisitions status code successfully migrates else return false.
     */
    protected boolean processRequisitionDocumentsForStatusCodeMigration() {
        LOG.debug("processRequisitionDocumentsForStatusCodeMigration() started");
        
        boolean success = true;

        migratePurapStatCodeReportService.writeFormattedMessageLine("********** Migration of Requistions StatusCode to Workflow document started **********\n");
        
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        
        //get the status code/descriptions from the table pur_reqs_stat_t
        Map<String, String> requisitionStatusMap = getStatusCodeAndDescriptionForPurapDocumentsDao().getRequisitionDocumentStatuses();
        
        //get the requisitions where REQS_STAT_CD is not null....
        List<RequisitionDocument> reqDocs = (List<RequisitionDocument>) SpringContext.getBean(PurapDocumentsStatusCodeMigrationDao.class).getRequisitionDocumentsForStatusCodeMigration();
        
        for (RequisitionDocument reqDoc : reqDocs) {
            String newApplicationDocumentStatus = requisitionStatusMap.get(reqDoc.getStatusCode());
            migratePurapStatCodeReportService.writeFormattedMessageLine("\t\tREQ Doc: " + reqDoc.getDocumentNumber() + " Status Code: " + reqDoc.getStatusCode() + " Status Description: " + newApplicationDocumentStatus);
            
            //find the workflow document and update the app_doc_stat and app_doc_stat_mdfn_dt columns.
            getStatusCodeAndDescriptionForPurapDocumentsDao().updateAndSaveMigratedApplicationDocumentStatuses(reqDoc.getDocumentNumber(), newApplicationDocumentStatus, getDateTimeService().getCurrentTimestamp());

            //now reindex the requistion for document search...
            documentAttributeIndexingQueue.indexDocument(reqDoc.getDocumentNumber());
        }
        
        migratePurapStatCodeReportService.writeFormattedMessageLine("\n********** Migration of Requistions StatusCode to Workflow document completed **********\n\n");
        
        LOG.debug("processRequisitionDocumentsForStatusCodeMigration() completed");
        
        return success;
    }
    
    /**
     * Processes the payment request documents for status code migration.  Creates a list of documents numbers
     * and current status code where status code needs migration and using that list the workflow documents
     * will be retrieved.  Each workflow document then will be updated with the corresponding
     * application document status that is new in KFS 5.0
     * 
     * @return true if the payment request documents status code successfully migrates else return false.
     */
    protected boolean processPaymentRequestDocumentsForStatusCodeMigration() {
        LOG.debug("processPaymentRequestDocumentsForStatusCodeMigration() started");
        
        boolean success = true;

        migratePurapStatCodeReportService.writeFormattedMessageLine("********** Migration of Payment Requests StatusCode to Workflow document started **********\n");
        
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
 
        //get the status code/descriptions from the table ap_pmt_rqst_stat_t
        Map<String, String> purchaseOrderStatusMap = getStatusCodeAndDescriptionForPurapDocumentsDao().getPaymentRequestDocumentStatuses();
        
        //get the payment requests where PMT_RQST_STAT_CD is not null....
        List<PaymentRequestDocument> preqDocs = (List<PaymentRequestDocument>) SpringContext.getBean(PurapDocumentsStatusCodeMigrationDao.class).getPaymentRequestDocumentsForStatusCodeMigration();
        
        for (PaymentRequestDocument preqDoc : preqDocs) {
            String newApplicationDocumentStatus = purchaseOrderStatusMap.get(preqDoc.getStatusCode());
            migratePurapStatCodeReportService.writeFormattedMessageLine("\t\tPREQ Doc: " + preqDoc.getDocumentNumber() + " Status Code: " + preqDoc.getStatusCode() + " Status Description: " + newApplicationDocumentStatus);
            
            //find the workflow document and update the app_doc_stat and app_doc_stat_mdfn_dt columns.
            getStatusCodeAndDescriptionForPurapDocumentsDao().updateAndSaveMigratedApplicationDocumentStatuses(preqDoc.getDocumentNumber(), newApplicationDocumentStatus, getDateTimeService().getCurrentTimestamp());

            //now reindex the payment request for document search...
            documentAttributeIndexingQueue.indexDocument(preqDoc.getDocumentNumber());
        }
        
        migratePurapStatCodeReportService.writeFormattedMessageLine("\n********** Migration of Payment Requests StatusCode to Workflow document completed **********\n\n");
        
        LOG.debug("processPaymentRequestDocumentsForStatusCodeMigration() completed");
        
        return success;
    }
    
    /**
     * Processes the purchase orders vendor quote for status code migration.  Creates a list of documents numbers
     * and current status code where status code needs migration and using that list the workflow documents
     * will be retrieved.  Each workflow document then will be updated with the corresponding
     * application document status that is new in KFS 5.0
     * 
     * @return true if the po vendor quote status code successfully migrates else return false.
     */
    protected boolean processPurchaseOrderVendorQuotesForStatusCodeMigration() {
        LOG.debug("processPurchaseOrderVendorQuotesForStatusCodeMigration() started");
        
        boolean success = true;

        migratePurapStatCodeReportService.writeFormattedMessageLine("********** Migration of Purchase Orders StatusCode to Workflow document started **********\n");
        
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
 
        //get the status code/descriptions from the table pur_po_qt_stat_t
        Map<String, String> purchaseOrderVendorQuoteStatusMap = getStatusCodeAndDescriptionForPurapDocumentsDao().getPurchaseOrderVendorQuoteDocumentStatuses();
        
        //get the purchase order vendor quote documents where PO_QT_STAT_CD is not null....
        List<PurchaseOrderVendorQuote> poVendorQuoteDocs = (List<PurchaseOrderVendorQuote>) SpringContext.getBean(PurapDocumentsStatusCodeMigrationDao.class).getPurchaseOrderVendorQuoteDocumentsForStatusCodeMigration();
        
        for (PurchaseOrderVendorQuote poVendorQuoteDoc : poVendorQuoteDocs) {
            String newApplicationDocumentStatus = purchaseOrderVendorQuoteStatusMap.get(poVendorQuoteDoc.getPurchaseOrderQuoteStatusCode());
            migratePurapStatCodeReportService.writeFormattedMessageLine("\t\tPO Vendor Quote Doc: " + poVendorQuoteDoc.getDocumentNumber() + " Status Code: " + poVendorQuoteDoc.getPurchaseOrderQuoteStatusCode() + " Status Description: " + newApplicationDocumentStatus);
            
            //find the workflow document and update the app_doc_stat and app_doc_stat_mdfn_dt columns.
            getStatusCodeAndDescriptionForPurapDocumentsDao().updateAndSaveMigratedApplicationDocumentStatuses(poVendorQuoteDoc.getDocumentNumber(), newApplicationDocumentStatus, getDateTimeService().getCurrentTimestamp());

            //now reindex the payment request for document search...
            documentAttributeIndexingQueue.indexDocument(poVendorQuoteDoc.getDocumentNumber());
        }
        
        migratePurapStatCodeReportService.writeFormattedMessageLine("\n********** Migration of Purchase Orders StatusCode to Workflow document completed **********\n\n");
        
        LOG.debug("processPurchaseOrderVendorQuotesForStatusCodeMigration() completed");
        
        return success;
    }
    
    /**
     * Processes the payment request for status code migration.  Creates a list of documents numbers
     * and current status code where status code needs migration and using that list the workflow documents
     * will be retrieved.  Each workflow document then will be updated with the corresponding
     * application document status that is new in KFS 5.0
     * 
     * @return true if the payment request status code successfully migrates else return false.
     */
    protected boolean processPurchaseOrderDocumentsForStatusCodeMigration() {
        LOG.debug("processPurchaseOrderDocumentsForStatusCodeMigration() started");
        
        boolean success = true;

        migratePurapStatCodeReportService.writeFormattedMessageLine("********** Migration of Purchase Order StatusCode to Workflow document started **********\n");
        
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
 
        //get the status code/descriptions from the table ap_pmt_rqst_stat_t
        Map<String, String> paymentRequestStatusMap = getStatusCodeAndDescriptionForPurapDocumentsDao().getPurchaseOrderDocumentStatuses();
        
        //get the purchase orders where PO_STAT_CD is not null....
        List<PurchaseOrderDocument> poDocs = (List<PurchaseOrderDocument>) SpringContext.getBean(PurapDocumentsStatusCodeMigrationDao.class).getPurchaseOrderDocumentsForStatusCodeMigration();
        
        for (PurchaseOrderDocument poDoc : poDocs) {
            String newApplicationDocumentStatus = paymentRequestStatusMap.get(poDoc.getStatusCode());
            migratePurapStatCodeReportService.writeFormattedMessageLine("\t\tPO Doc: " + poDoc.getDocumentNumber() + " Status Code: " + poDoc.getStatusCode() + " Status Description: " + newApplicationDocumentStatus);
            
            //find the workflow document and update the app_doc_stat and app_doc_stat_mdfn_dt columns.
            getStatusCodeAndDescriptionForPurapDocumentsDao().updateAndSaveMigratedApplicationDocumentStatuses(poDoc.getDocumentNumber(), newApplicationDocumentStatus, getDateTimeService().getCurrentTimestamp());

            //now reindex the purchase order for document search...
            documentAttributeIndexingQueue.indexDocument(poDoc.getDocumentNumber());
        }
        
        migratePurapStatCodeReportService.writeFormattedMessageLine("\n********** Migration of Purchase Order StatusCode to Workflow document completed **********\n\n");
        
        LOG.debug("processPurchaseOrderDocumentsForStatusCodeMigration() completed");
        
        return success;
    }

    
    /**
     * Processes the vendor credit memos for status code migration.  Creates a list of documents numbers
     * and current status code where status code needs migration and using that list the workflow documents
     * will be retrieved.  Each workflow document then will be updated with the corresponding
     * application document status that is new in KFS 5.0
     * 
     * @return true if the vendor credit memo status code successfully migrates else return false.
     */
    protected boolean processVendorCreditMemoDocumentsForStatusCodeMigration() {
        LOG.debug("processVendorCreditMemoDocumentsForStatusCodeMigration() started");
        
        boolean success = true;

        migratePurapStatCodeReportService.writeFormattedMessageLine("********** Migration of Vendor Credit Memo StatusCode to Workflow document started **********\n");
        
        final DocumentAttributeIndexingQueue documentAttributeIndexingQueue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
 
        //get the status code/descriptions from the table ap_crdt_memo_stat_t
        Map<String, String> vendorCreditMemoStatusMap = getStatusCodeAndDescriptionForPurapDocumentsDao().getVendorCreditMemoDocumentStatuses();
        
        //get the vendor credit memo where CRDT_MEMO_STAT_CD is not null....
        List<VendorCreditMemoDocument> vcmDocs = (List<VendorCreditMemoDocument>) SpringContext.getBean(PurapDocumentsStatusCodeMigrationDao.class).getVendorCreditMemoDocumentsForStatusCodeMigration();
        
        for (VendorCreditMemoDocument vcmDoc : vcmDocs) {
            String newApplicationDocumentStatus = vendorCreditMemoStatusMap.get(vcmDoc.getStatusCode());
            migratePurapStatCodeReportService.writeFormattedMessageLine("\t\tVendor Credit Memo Doc: " + vcmDoc.getDocumentNumber() + " Status Code: " + vcmDoc.getStatusCode() + " Status Description: " + newApplicationDocumentStatus);
            
            //find the workflow document and update the app_doc_stat and app_doc_stat_mdfn_dt columns.
            getStatusCodeAndDescriptionForPurapDocumentsDao().updateAndSaveMigratedApplicationDocumentStatuses(vcmDoc.getDocumentNumber(), newApplicationDocumentStatus, getDateTimeService().getCurrentTimestamp());

            //now reindex the purchase order for document search...
            documentAttributeIndexingQueue.indexDocument(vcmDoc.getDocumentNumber());
        }
        
        migratePurapStatCodeReportService.writeFormattedMessageLine("\n********** Migration of Vendor Credit Memo StatusCode to Workflow document completed **********\n\n");
        
        LOG.debug("processVendorCreditMemoDocumentsForStatusCodeMigration() completed");
        
        return success;
    }
    
    /**
     * Gets the migratePurapStatCodeReportService attribute. 
     * @return Returns the migratePurapStatCodeReportService.
     */
    protected ReportWriterService getMigratePurapStatCodeReportService() {
        return migratePurapStatCodeReportService;
    }
    
    /**
     * Sets the migratePurapStatCodeReportService attribute value.
     * @param migratePurapStatCodeReportService The autoDisapproveErrorReportWriterService to set.
     */
    public void setMigratePurapStatCodeReportService(ReportWriterService migratePurapStatCodeReportService) {
        this.migratePurapStatCodeReportService = migratePurapStatCodeReportService;
    }
    
    /**
     * Gets the businessObjectService attribute.
     * 
     * @return Returns the businessObjectService
     */
    
    public BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService =  SpringContext.getBean(BusinessObjectService.class);
        }
        
        return businessObjectService;
    }

    /** 
     * Sets the businessObjectService attribute.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
    /**
     * Gets the statusCodeAndDescriptionForPurapDocumentsDao attribute.
     * 
     * @return Returns the statusCodeAndDescriptionForPurapDocumentsDao
     */
    
    public StatusCodeAndDescriptionForPurapDocumentsDao getStatusCodeAndDescriptionForPurapDocumentsDao() {
        if (statusCodeAndDescriptionForPurapDocumentsDao == null) {
            statusCodeAndDescriptionForPurapDocumentsDao = SpringContext.getBean(StatusCodeAndDescriptionForPurapDocumentsDao.class);
        }
        
        return statusCodeAndDescriptionForPurapDocumentsDao;
    }

    /** 
     * Sets the statusCodeAndDescriptionForPurapDocumentsDao attribute.
     * 
     * @param statusCodeAndDescriptionForPurapDocumentsDao The statusCodeAndDescriptionForPurapDocumentsDao to set.
     */
    public void setStatusCodeAndDescriptionForPurapDocumentsDao(StatusCodeAndDescriptionForPurapDocumentsDao statusCodeAndDescriptionForPurapDocumentsDao) {
        this.statusCodeAndDescriptionForPurapDocumentsDao = statusCodeAndDescriptionForPurapDocumentsDao;
    }
    /**
     * Gets the dateTimeService attribute.
     * 
     * @return Returns the dateTimeService
     */
    
    public DateTimeService getDateTimeService() {
        if (dateTimeService == null) {
            dateTimeService = SpringContext.getBean(DateTimeService.class);
        }
        
        return dateTimeService;
    }

    /** 
     * Sets the dateTimeService attribute.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }
}