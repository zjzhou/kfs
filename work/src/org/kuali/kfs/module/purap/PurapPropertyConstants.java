/*
 * Copyright 2006-2007 The Kuali Foundation.
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
package org.kuali.kfs.module.purap;

import org.kuali.rice.kns.util.KualiDecimal;

/**
 * Property name constants.
 */
public class PurapPropertyConstants {

    public static final String VENDOR_HEADER_GENERATED_ID = "vendorHeaderGeneratedIdentifier";
    public static final String VENDOR_DETAIL_ASSIGNED_ID = "vendorDetailAssignedIdentifier";

    // ITEM
    public static final String ITEM_IDENTIFIER = "itemIdentifier";
    public static final String ACCOUNTS = "sourceAccountingLines";
    public static final String QUANTITY = "itemQuantity";
    public static final String EXTENDED_PRICE = "extendedPrice";
    public static final String ITEM_TYPE = "itemType";
    public static final String ITEM = "item";
    public static final String ITEMS = "items";
    public static final String ITEM_CATALOG_NUMBER = "itemCatalogNumber";
    public static final String ITEM_DESCRIPTION = "itemDescription";
    public static final String ITEM_QUANTITY = "itemQuantity";
    public static final String ITEM_UNIT_OF_MEASURE_CODE = "itemUnitOfMeasureCode";
    public static final String ITEM_UNIT_OF_MEASURE_DESCRIPTION = "itemUnitOfMeasureDescription";
    public static final String ITEM_UNIT_PRICE = "itemUnitPrice";
    public static final String ITEM_COMMODITY_CODE = "purchasingCommodityCode";
    public static final String NEW_PURCHASING_ITEM_LINE = "newPurchasingItemLine";
    public static final String ITEM_CAPITAL_ASSET_TRANSACTION_TYPE = "capitalAssetTransactionType";
    public static final String ITEM_CAPITAL_ASSET_TRANSACTION_TYPE_CODE = "capitalAssetTransactionTypeCode";
    public static final String CAPITAL_ASSET_ITEM_IDENTIFIER = "capitalAssetItemIdentifier";
    public static final String CAPITAL_ASSET_SYSTEM_IDENTIFIER = "capitalAssetSystemIdentifier";
    
    // accounting line
    public static final String ACCOUNT_DISTRIBUTION_NEW_SRC_LINE = "accountDistributionnewSourceLine";
    public static final String ACCOUNT_IDENTIFIER = "accountIdentifier";
    public static final String ACCOUNT_LINE_PERCENT = "accountLinePercent";
    public static final String FINANCIAL_OBJECT_CODE = "financialObjectCode";
    public static final String SUB_FUND_GROUP_CODE = "subFundGroupCode";
    public static final String ACCOUNT_TYPE_CODE = "accountTypeCode";
    public static final String ORGANIZATION_CODE = "organizationCode";
    public static final String CHART_OF_ACCOUNTS_CODE = "chartOfAccountsCode";    
    public static final String ACCOUNT_NUMBER = "accountNumber";
    public static final String DATA_OBJ_MAINT_CD_ACTIVE_IND = "dataObjectMaintenanceCodeActiveIndicator";

    // document fields
    public static final String GENERAL_LEDGER_PENDING_ENTRIES = "generalLedgerPendingEntries";

    // PURCHASING AND ACCOUNTS PAYABLE DOCUMENT BASE
    public static final String PURAP_DOC = "purapDocument";
    public static final String PURAP_DOC_ID = "purapDocumentIdentifier";
    public static final String PURAP_ITEM = "purapItem";
    public static final String OLD_STATUS = "oldStatus";
    public static final String NEW_STATUS = "newStatus";
    public static final String STATUS_CODE = "statusCode";
    public static final String STATUS = "status";
    public static final String VENDOR_NUMBER = "vendorNumber";
    public static final String VENDOR_POSTAL_CODE = "vendorPostalCode";
    public static final String VENDOR_COUNTRY_CODE = "vendorCountryCode";
    public static final String VENDOR_STATE_CODE = "vendorStateCode";
    public static final String VENDOR_ADDRESS_ID = "vendorAddressGeneratedIdentifier";
    public static final String DELIVERY_CAMPUS_CODE = "deliveryCampusCode";
    
    // PURCHASING DOCUMENT BASE
    public static final String RECURRING_PAYMENT_TYPE_CODE = "recurringPaymentTypeCode";
    public static final String PURCHASE_ORDER_BEGIN_DATE = "purchaseOrderBeginDate";
    public static final String PURCHASE_ORDER_END_DATE = "purchaseOrderEndDate";
    public static final String PURCHASE_ORDER_TOTAL_LIMIT = "purchaseOrderTotalLimit";
    public static final String VENDOR_FAX_NUMBER = "vendorFaxNumber";
    public static final String CONTRACT_MANAGER_CODE = "contractManagerCode";
    public static final String VENDOR_CONTRACT_ID = "vendorContractGeneratedIdentifier";
    public static final String PURCHASE_ORDER = "purchaseOrder";
    public static final String PURCHASE_ORDER_COST_SOURCE = "purchaseOrderCostSource";
    public static final String DELIVERY_REQUIRED_DATE = "deliveryRequiredDate";
    public static final String COMMODITY_CODE = "commodityCode";
    public static final String RECEIVING_DOCUMENT_REQUIRED_ID = "receivingDocumentRequiredIndicator";
    public static final String CAPITAL_ASSET_SYSTEM_TYPE = "capitalAssetManagementSystemType";
    public static final String CAPITAL_ASSET_TRANSACTION_TYPE = "capitalAssetTransactionType";
    public static final String CAPITAL_ASSET_TRANSACTION_TYPE_CODE = "capitalAssetTransactionTypeCode";
    public static final String PURCHASING_CAPITAL_ASSET_ITEMS = "purchasingCapitalAssetItems";
    public static final String PURCHASING_CAPITAL_ASSET_SYSTEMS = "purchasingCapitalAssetSystems";
    public static final String PURCHASING_CAPITAL_ASSET_SYSTEM = "purchasingCapitalAssetSystem";
    public static final String CAPITAL_ASSET_LOCATIONS = "capitalAssetLocations";
    public static final String CAPITAL_ASSET_NUMBER = "capitalAssetNumber";
    
    // Capital Asset Locations
    public static final String CAPITAL_ASSET_LOCATION_ADDRESS_LINE1 = "capitalAssetLine1Address";
    public static final String CAPITAL_ASSET_LOCATION_CITY = "capitalAssetCityName";
    public static final String CAPITAL_ASSET_LOCATION_STATE = "capitalAssetStateCode";
    public static final String CAPITAL_ASSET_LOCATION_POSTAL_CODE = "capitalAssetPostalCode";
    public static final String CAPITAL_ASSET_LOCATION_COUNTRY = "capitalAssetCountryCode";
    public static final String CAPITAL_ASSET_LOCATION_CAMPUS = "campusCode";
    public static final String CAPITAL_ASSET_LOCATION_BUILDING = "buildingCode";
    public static final String CAPITAL_ASSET_LOCATION_ROOM = "buildingRoomNumber";
    
    // AP DOCUMENT BASE
    public static final String PURCHASE_ORDER_IDENTIFIER = "purchaseOrderIdentifier";

    // REQUISITION DOCUMENT

    // PURCHASE ORDER DOCUMENT
    public static final String PURCHASE_ORDER_CURRENT_INDICATOR = "purchaseOrderCurrentIndicator";
    public static final String VENDOR_STIPULATION = "purchaseOrderVendorStipulations";
    public static final String QUOTE_TRANSMITTED = "purchaseOrderQuoteTransmitted";
    public static final String VENDOR_QUOTES = "purchaseOrderVendorQuotes";
    public static final String ALTERNATE_VENDOR_NAME = "alternateVendorName";
    public static final String PURCHASE_ORDER_STATUS_CODE = "purchaseOrderStatusCode";
    public static final String TOTAL_ENCUMBRANCE = "totalEncumbrance";
    public static final String VENDOR_CHOICE_CODE = "vendorChoiceCode";
    // Quote Language
    public static final String PURCHASE_ORDER_QUOTE_LANGUAGE_ID = "purchaseOrderQuoteLanguageIdentifier";
    // vendor quote
    public static final String PURCHASE_ORDER_VENDOR_QUOTE_IDENTIFIER = "purchaseOrderVendorQuoteIdentifier";
    public static final String PURCHASE_ORDER_QUOTE_STATUS ="purchaseOrderQuoteStatus";
    public static final String PURCHASE_ORDER_VENDOR_QUOTES = "purchaseOrderVendorQuotes";
    public static final String PURCHASE_ORDER_QUOTE_INITIALIZATION_DATE = "purchaseOrderQuoteInitializationDate";
    public static final String PURCHASE_ORDER_QUOTE_AWARDED_DATE = "purchaseOrderQuoteAwardedDate";
    public static final String PURCHASE_ORDER_QUOTE_DUE_DATE = "purchaseOrderQuoteDueDate";
    public static final String PURCHASE_ORDER_QUOTE_TYPE_CODE = "purchaseOrderQuoteTypeCode";
    public static final String PURCHASE_ORDER_QUOTE_VENDOR_NOTE_TEXT = "purchaseOrderQuoteVendorNoteText";
    public static final String NEW_PURCHASE_ORDER_VENDOR_QUOTE_VENDOR_NAME = "newPurchaseOrderVendorQuote.vendorName";
    public static final String NEW_PURCHASE_ORDER_VENDOR_QUOTE_VENDOR_LINE_1_ADDR = "newPurchaseOrderVendorQuote.vendorLine1Address";
    public static final String NEW_PURCHASE_ORDER_VENDOR_QUOTE_VENDOR_CITY_NAME = "newPurchaseOrderVendorQuote.vendorCityName";
    
    // Stipulations
    public static final String VENDOR_STIPULATION_DESCRIPTION = "vendorStipulationDescription";
    // Receiving Address
    public static final String RCVNG_ADDR_DFLT_IND = "defaultIndicator";
    public static final String RCVNG_ADDR_ACTIVE = "active";

    // PAYMENT REQUEST DOCUMENT
    public static final String INVOICE_DATE = "invoiceDate";
    public static final String INVOICE_NUMBER = "invoiceNumber";
    public static final String VENDOR_INVOICE_AMOUNT = "vendorInvoiceAmount";
    public static final String PAYMENT_REQUEST_PAY_DATE = "paymentRequestPayDate";
    public static final String GRAND_TOTAL = "grandTotal";
    public static final String PAYMENT_REQUEST = "paymentRequest";
    public static final String RECURRING_PAYMENT_TYPE = "recurringPaymentType";
    // tax tab fields
    public static final String TAX_CLASSIFICATION_CODE = "taxClassificationCode";
    public static final String FEDERAL_TAX_PERCENT = "federalTaxPercent";
    public static final String STATE_TAX_PERCENT = "stateTaxPercent";
    public static final String TAX_COUNTRY_CODE = "taxCountryCode";
    public static final String TAX_NQI_ID = "taxNQIId";
    public static final String GROSS_UP_INDICATOR = "grossUpIndicator";
    public static final String TAX_EXEMPT_TREATY_INDICATOR = "taxExemptTreatyIndicator";
    public static final String FOREIGN_SOURCE_INDICATOR = "foreignSourceIndicator";
    public static final String TAX_USAID_PER_DIEM_INDICATOR = "taxUSAIDPerDiemIndicator";
    public static final String OTHER_TAX_EXEMPT_INDICATOR = "otherTaxExemptIndicator";
    public static final String TAX_SPECIAL_W4_AMOUNT = "taxSpecialW4Amount";

    // CREDIT MEMO DOCUMENT
    public static final String PAYMENT_REQUEST_ID = "paymentRequestIdentifier";
    public static final String CREDIT_MEMO_DATE = "creditMemoDate";
    public static final String CREDIT_MEMO_NUMBER = "creditMemoNumber";
    public static final String CREDIT_MEMO_AMOUNT = "creditMemoAmount";

    // RECEIVING LINE DOCUMENT
    public static final String LINE_ITEM_RECEIVING_DOCUMENT_NUMBER = "lineItemReceivingDocumentNumber";
    public static final String SHIPMENT_RECEIVED_DATE = "shipmentReceivedDate";
    public static final String SHIPMENT_PACKING_SLIP_NUMBER = "shipmentPackingSlipNumber";
    public static final String SHIPMENT_BILL_OF_LADING_NUMBER = "shipmentBillOfLadingNumber";
    
    // Sensitive Data
    public static final String WORKGROUP_NAME = "sensitiveDataWorkgroupName";
    public static final String SENSITIVE_DATA_CODE = "sensitiveDataCode";
    
    //Assign Contract Manager
    public static final String REQUISITION_IDENTIFIER = "requisitionIdentifier";
    public static final String VENDOR_NAME = "vendorName";
    
    //Bulk Receiving
    public static final String VENDOR_DATE = "vendorName";
    
}