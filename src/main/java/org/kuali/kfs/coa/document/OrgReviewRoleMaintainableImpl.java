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
package org.kuali.kfs.coa.document;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.identity.OrgReviewRole;
import org.kuali.kfs.coa.service.OrgReviewRoleService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.maintenance.Maintainable;
import org.kuali.rice.kns.web.ui.Field;
import org.kuali.rice.kns.web.ui.Row;
import org.kuali.rice.kns.web.ui.Section;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.maintenance.MaintenanceLock;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;


public class OrgReviewRoleMaintainableImpl extends FinancialSystemMaintainable {

    private transient static OrgReviewRoleService orgReviewRoleService;

    @Override
    public boolean isExternalBusinessObject(){
        return true;
    }

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        return Collections.emptyList();
    }

    @Override
    public void prepareBusinessObject(BusinessObject businessObject){
        OrgReviewRole orr = (OrgReviewRole)businessObject;
        //Assuming that this is the condition when the document is loaded on edit or copy

        // The links on the lookup set barious variables, including the methodToCall on the "bo" class
        // and the delegate or role IDs being edited/copied

        if( (KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL.equals(orr.getMethodToCall()) ||
                KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL.equals(orr.getMethodToCall()))
                &&
                (StringUtils.isNotEmpty(orr.getODelMId()) || StringUtils.isNotEmpty(orr.getORMId())) ){
            // check if we have the information to create a delegation
            if(StringUtils.isNotEmpty(orr.getODelMId()) && !orr.isCreateDelegation()){
                getOrgReviewRoleService().populateOrgReviewRoleFromDelegationMember(orr, orr.getORMId(), orr.getODelMId());

                orr.setDelegate(true);
                if ( KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL.equals(orr.getMethodToCall()) ) {
                    orr.setDelegationMemberId("");
                }
            } else if(StringUtils.isNotEmpty(orr.getORMId())){
                getOrgReviewRoleService().populateOrgReviewRoleFromRoleMember(orr, orr.getORMId());

                if(orr.isCreateDelegation()) {
                    orr.setDelegate(true);
                    if ( KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL.equals(orr.getMethodToCall()) ) {
                        orr.setDelegationMemberId("");
                    }
                } else {
                    orr.setDelegate(false);
                    if ( KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL.equals(orr.getMethodToCall()) ) {
                        orr.setRoleMemberId("");
                    }
                }
            }
            // blank these out, since they are flags to init the object
            orr.setORMId("");
            orr.setODelMId("");
        }
        setBusinessObject(orr);
    }

//    public List<RoleResponsibilityAction> getRoleRspActions(String roleMemberId){
//        return KimApiServiceLocator.getRoleService().getRoleMemberResponsibilityActions(roleMemberId);
//    }

    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String,String[]> parameters){
        super.processAfterEdit(document, parameters);
        OrgReviewRole orr = (OrgReviewRole)document.getOldMaintainableObject().getBusinessObject();
        orr.setEdit(true);
        orr = (OrgReviewRole) document.getNewMaintainableObject().getBusinessObject();
        orr.setEdit(true);
        if ( orr.isCreateDelegation() ) {
            orr.setPerson(null);
            orr.setRole(null);
            orr.setGroup(null);
        }
    }

    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String,String[]> parameters){
        super.processAfterCopy(document, parameters);
        OrgReviewRole orr = (OrgReviewRole)document.getOldMaintainableObject().getBusinessObject();
        if(orr.isDelegate() || orr.isCreateDelegation()) {
            orr.setDelegationMemberId("");
        } else {
            orr.setRoleMemberId("");
        }
        orr.setCopy(true);
        orr = (OrgReviewRole) document.getNewMaintainableObject().getBusinessObject();
        if(orr.isDelegate() || orr.isCreateDelegation()) {
            orr.setDelegationMemberId("");
        } else {
            orr.setRoleMemberId("");
        }
        orr.setCopy(true);
    }

    /**
     * Override the getSections method on this maintainable so that the document type name field
     * can be set to read-only for
     *
     * KRAD Conversion: Inquirable performs conditionally preparing the fields for different role modes
     * or to display/hide fields on the inquiry.
     * The field definitions are NOT declared in data dictionary.
     */
    @Override
    public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        List<Section> sections = super.getSections(document, oldMaintainable);
        OrgReviewRole orr = (OrgReviewRole)document.getNewMaintainableObject().getBusinessObject();

        String closestOrgReviewRoleParentDocumentTypeName = getOrgReviewRoleService().getClosestOrgReviewRoleParentDocumentTypeName(orr.getFinancialSystemDocumentTypeCode());
        boolean isFSTransDoc = StringUtils.equals( orr.getFinancialSystemDocumentTypeCode(), KFSConstants.FINANCIAL_SYSTEM_TRANSACTIONAL_DOCUMENT)
                || StringUtils.equals( KFSConstants.FINANCIAL_SYSTEM_TRANSACTIONAL_DOCUMENT, closestOrgReviewRoleParentDocumentTypeName);
        boolean hasAccountingOrganizationHierarchy = isFSTransDoc || getOrgReviewRoleService().hasAccountingOrganizationHierarchy(orr.getFinancialSystemDocumentTypeCode());

        boolean shouldReviewTypesFieldBeReadOnly = isFSTransDoc
                || getOrgReviewRoleService().hasOrganizationHierarchy(orr.getFinancialSystemDocumentTypeCode())
                || (StringUtils.isNotBlank(closestOrgReviewRoleParentDocumentTypeName)
                        && StringUtils.equals(closestOrgReviewRoleParentDocumentTypeName, KFSConstants.FINANCIAL_SYSTEM_COMPLEX_MAINTENANCE_DOCUMENT));

        //If oldMaintainable is null, it means we are trying to get sections for the old part
        //If oldMaintainable is not null, it means we are trying to get sections for the new part
        //Refer to KualiMaintenanceForm lines 288-294
        if(oldMaintainable!=null){
            if(orr.isCreateRoleMember() || orr.isCopyRoleMember()){
                for (Section section : sections) {
                    for (Row row : section.getRows()) {
                        for (Field field : row.getFields()) {
                            prepareFieldsForCreateRoleMemberMode(field);
                            prepareFieldsCommon(field, shouldReviewTypesFieldBeReadOnly, hasAccountingOrganizationHierarchy );
                        }
                    }
                }
            } else if(orr.isDelegate() && (orr.isCopy() || StringUtils.isBlank( orr.getDelegationMemberId() )) ){
                for (Section section : sections) {
                    for (Row row : section.getRows()) {
                        for (Field field : row.getFields()) {
                            prepareFieldsForCreateDelegationMode(field);
                            prepareFieldsCommon(field, shouldReviewTypesFieldBeReadOnly, hasAccountingOrganizationHierarchy );
                        }
                    }
                }
            } else if(orr.isEditRoleMember()){
                for (Section section : sections) {
                    for (Row row : section.getRows()) {
                        for (Field field : row.getFields()) {
                            prepareFieldsForEditRoleMember(field);
                            prepareFieldsCommon(field, shouldReviewTypesFieldBeReadOnly, hasAccountingOrganizationHierarchy );
                        }
                    }
                }
            } else if(orr.isEditDelegation()){
                for (Section section : sections) {
                    for (Row row : section.getRows()) {
                        for (Field field : row.getFields()) {
                            prepareFieldsForEditDelegation(field);
                            prepareFieldsCommon(field, shouldReviewTypesFieldBeReadOnly, hasAccountingOrganizationHierarchy );
                        }
                    }
                }
            }
        } else if ( orr.isCreateRoleMember() || orr.isCopyRoleMember() || orr.isEditRoleMember()) {
            // If the member being edited is not a delegate, do not show the delegation type code
            for (Section section : sections) {
                for (Row row : section.getRows()) {
                    for (Field field : row.getFields()) {
                        if(OrgReviewRole.DELEGATION_TYPE_CODE.equals(field.getPropertyName())){
                            field.setFieldType(Field.HIDDEN);
                        }
                    }
                }
            }
        }
        return sections;
    }

    protected void prepareFieldsCommon(Field field, boolean shouldReviewTypesFieldBeReadOnly, boolean hasAccountingOrganizationHierarchy){
        if ( field == null ) {
            throw new IllegalArgumentException( "The Field parameter may not be null." );
        }

        if(!shouldReviewTypesFieldBeReadOnly) {
            return; // nothing to make read only
        }

        if(OrgReviewRole.REVIEW_ROLES_INDICATOR_FIELD_NAME.equals(field.getPropertyName())) {
            field.setReadOnly(true);
        } else if( !hasAccountingOrganizationHierarchy
                && (OrgReviewRole.FROM_AMOUNT_FIELD_NAME.equals(field.getPropertyName()) ||
                        OrgReviewRole.TO_AMOUNT_FIELD_NAME.equals(field.getPropertyName()) ||
                        OrgReviewRole.OVERRIDE_CODE_FIELD_NAME.equals(field.getPropertyName()))) {
            field.setReadOnly(true);
        }
    }

    protected void setCommonFieldsToReadOnlyOnEdit( Field field ) {
        if(OrgReviewRole.CHART_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ORG_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.DOC_TYPE_NAME_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.REVIEW_ROLES_INDICATOR_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.PRINCIPAL_NAME_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ROLE_NAME_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ROLE_NAME_FIELD_NAMESPACE_CODE.equals(field.getPropertyName()) ||
                OrgReviewRole.GROUP_NAME_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.GROUP_NAME_FIELD_NAMESPACE_CODE.equals(field.getPropertyName())){
            field.setReadOnly(true);
        }
    }

    protected void prepareFieldsForEditRoleMember(Field field){
        setCommonFieldsToReadOnlyOnEdit(field);
        //If the member being edited is not a delegate, do not show the delegation type code
        if(OrgReviewRole.DELEGATION_TYPE_CODE.equals(field.getPropertyName())){
            field.setFieldType(Field.HIDDEN);
        }
    }

    protected void prepareFieldsForEditDelegation(Field field){
        setCommonFieldsToReadOnlyOnEdit(field);
        if(OrgReviewRole.ACTION_POLICY_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ACTION_TYPE_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.PRIORITY_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.FORCE_ACTION_FIELD_NAME.equals(field.getPropertyName())){
            field.setReadOnly(true);
        }
    }

    protected void prepareFieldsForCreateRoleMemberMode(Field field){
        //If a role member (i.e. not a delegate) is being created, do not show the delegation type code
        if(OrgReviewRole.DELEGATION_TYPE_CODE.equals(field.getPropertyName())){
            field.setFieldType(Field.HIDDEN);
        }
    }

    protected void prepareFieldsForCreateDelegationMode(Field field){
        //TODO: in prepareBusinessObject, populate these fields for create delegation
        if(OrgReviewRole.CHART_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ORG_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.DOC_TYPE_NAME_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.REVIEW_ROLES_INDICATOR_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ACTION_POLICY_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.ACTION_TYPE_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.PRIORITY_CODE_FIELD_NAME.equals(field.getPropertyName()) ||
                OrgReviewRole.FORCE_ACTION_FIELD_NAME.equals(field.getPropertyName())){
            field.setReadOnly(true);
        }
    }

    /**
     *
     * @see org.kuali.rice.kns.maintenance.Maintainable#saveBusinessObject()
     */
    @Override
    public void saveBusinessObject() {
        getOrgReviewRoleService().saveOrgReviewRoleToKim((OrgReviewRole)getBusinessObject());
    }

    protected OrgReviewRoleService getOrgReviewRoleService(){
        if(orgReviewRoleService==null){
            orgReviewRoleService = SpringContext.getBean( OrgReviewRoleService.class );
        }
        return orgReviewRoleService;
    }

    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        super.refresh(refreshCaller, fieldValues, document);
        String docTypeName = ((OrgReviewRole)document.getNewMaintainableObject().getBusinessObject()).getFinancialSystemDocumentTypeCode();
        if(StringUtils.isNotBlank(docTypeName) &&
           !getOrgReviewRoleService().isValidDocumentTypeForOrgReview(docTypeName) ){

            GlobalVariables.getMessageMap().putErrorWithoutFullErrorPath(KFSConstants.MAINTENANCE_NEW_MAINTAINABLE + OrgReviewRole.DOC_TYPE_NAME_FIELD_NAME, KFSKeyConstants.ERROR_DOCUMENT_ORGREVIEW_INVALID_DOCUMENT_TYPE, docTypeName);
            ((OrgReviewRole)document.getNewMaintainableObject().getBusinessObject()).setFinancialSystemDocumentTypeCode("");
        }
    }

}
