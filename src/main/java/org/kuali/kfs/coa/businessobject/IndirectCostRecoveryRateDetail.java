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

package org.kuali.kfs.coa.businessobject;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import org.kuali.kfs.sys.businessobject.FiscalYearBasedBusinessObject;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * 
 */
public class IndirectCostRecoveryRateDetail extends PersistableBusinessObjectBase implements MutableInactivatable, FiscalYearBasedBusinessObject {

    /**
     * Default no-arg constructor.
     */
    public IndirectCostRecoveryRateDetail() {
    }

    private Integer universityFiscalYear;
    private String financialIcrSeriesIdentifier;
    private Integer awardIndrCostRcvyEntryNbr;
    private String transactionDebitIndicator;
    private BigDecimal awardIndrCostRcvyRatePct;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subAccountNumber;
    private String financialObjectCode;
    private String financialSubObjectCode;
    private boolean active;

    private SystemOptions universityFiscal;
    private IndirectCostRecoveryRate indirectCostRecoveryRate;
    
    /*
     * Don't use reference objects because Chart, Account, Sub-Account, etc. contain special characters. RO 2/8/06 private Chart
     * chartOfAccounts; private Account account; private SubAccount subAccount; private ObjectCode financialObject; private SubObjCd
     * financialSubObject; private ObjectCode offsetBalanceSheetObjectCode;
     */

    /*
     * public Account getAccount() { return account; } public void setAccount(Account account) { this.account = account; }
     */
    public String getAccountNumber() {
        return accountNumber;
    }


    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


    public Integer getAwardIndrCostRcvyEntryNbr() {
        return awardIndrCostRcvyEntryNbr;
    }


    public void setAwardIndrCostRcvyEntryNbr(Integer awardIndrCostRcvyEntryNbr) {
        this.awardIndrCostRcvyEntryNbr = awardIndrCostRcvyEntryNbr;
    }


    public BigDecimal getAwardIndrCostRcvyRatePct() {
        return awardIndrCostRcvyRatePct;
    }


    public void setAwardIndrCostRcvyRatePct(BigDecimal awardIndrCostRcvyRatePct) {
        this.awardIndrCostRcvyRatePct = awardIndrCostRcvyRatePct;
    }

    /*
     * public Chart getChartOfAccounts() { return chartOfAccounts; } public void setChartOfAccounts(Chart chartOfAccounts) {
     * this.chartOfAccounts = chartOfAccounts; }
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }


    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }


    public String getFinancialIcrSeriesIdentifier() {
        return financialIcrSeriesIdentifier;
    }


    public void setFinancialIcrSeriesIdentifier(String financialIcrSeriesIdentifier) {
        this.financialIcrSeriesIdentifier = financialIcrSeriesIdentifier;
    }


    /*
     * public ObjectCode getFinancialObject() { return financialObject; } public void setFinancialObject(ObjectCode financialObject) {
     * this.financialObject = financialObject; }
     */
    public String getFinancialObjectCode() {
        return financialObjectCode;
    }


    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    /*
     * public SubObjCd getFinancialSubObject() { return financialSubObject; } public void setFinancialSubObject(SubObjCd
     * financialSubObject) { this.financialSubObject = financialSubObject; }
     */

    public String getFinancialSubObjectCode() {
        return financialSubObjectCode;
    }


    public void setFinancialSubObjectCode(String financialSubObjectCode) {
        this.financialSubObjectCode = financialSubObjectCode;
    }

    /*
     * public SubAccount getSubAccount() { return subAccount; } public void setSubAccount(SubAccount subAccount) { this.subAccount =
     * subAccount; }
     */

    public String getSubAccountNumber() {
        return subAccountNumber;
    }


    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }


    public String getTransactionDebitIndicator() {
        return transactionDebitIndicator;
    }


    public void setTransactionDebitIndicator(String transactionDebitIndicator) {
        this.transactionDebitIndicator = transactionDebitIndicator;
    }


    public SystemOptions getUniversityFiscal() {
        return universityFiscal;
    }


    public void setUniversityFiscal(SystemOptions universityFiscal) {
        this.universityFiscal = universityFiscal;
    }


    public Integer getUniversityFiscalYear() {
        return universityFiscalYear;
    }


    public void setUniversityFiscalYear(Integer universityFiscalYear) {
        this.universityFiscalYear = universityFiscalYear;
    }


    /**
     * @see org.kuali.rice.krad.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {

        LinkedHashMap m = new LinkedHashMap();
        m.put("universityFiscal", this.universityFiscalYear);
        m.put("financialIcrSeriesIdentifier", this.financialIcrSeriesIdentifier);
        m.put("awardIndrCostRcvyEntryNbr", this.awardIndrCostRcvyEntryNbr);

        return m;
    }


    public boolean isActive() {
        return active;
    }


    public void setActive(boolean active) {
        this.active = active;
    }

    public IndirectCostRecoveryRate getIndirectCostRecoveryRate() {
        return indirectCostRecoveryRate;
    }

    public void setIndirectCostRecoveryRate(IndirectCostRecoveryRate indirectCostRecoveryRate) {
        this.indirectCostRecoveryRate = indirectCostRecoveryRate;
    }
}
