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
package org.kuali.module.purap.rules;

import java.util.List;

import org.kuali.core.bo.user.UniversalUser;
import org.kuali.core.exceptions.UserNotFoundException;
import org.kuali.core.service.UniversalUserService;
import org.kuali.core.util.GlobalVariables;
import org.kuali.kfs.KFSKeyConstants;
import org.kuali.kfs.util.SpringServiceLocator;
import org.kuali.module.purap.PurapConstants;
import org.kuali.module.purap.PurapKeyConstants;
import org.kuali.module.purap.PurapParameterConstants;
import org.kuali.module.purap.PurapPropertyConstants;
import org.kuali.module.purap.bo.PurchaseOrderItem;
import org.kuali.module.purap.bo.PurchasingApItem;
import org.kuali.module.purap.document.PurchasingAccountsPayableDocument;


public class PurchaseOrderAmendmentDocumentRule extends PurchaseOrderDocumentRule {

    @Override
    public boolean processValidation(PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = super.processValidation(purapDocument);
        // Check that the user is in purchasing workgroup.
        String initiatorNetworkId = purapDocument.getDocumentHeader().getWorkflowDocument().getInitiatorNetworkId();
        UniversalUserService uus = SpringServiceLocator.getUniversalUserService();
        UniversalUser user = null;
        try {
            user = uus.getUniversalUserByAuthenticationUserId(initiatorNetworkId);
            String purchasingGroup = SpringServiceLocator.getKualiConfigurationService().getApplicationParameterValue(PurapParameterConstants.PURAP_ADMIN_GROUP, PurapConstants.Workgroups.WORKGROUP_PURCHASING);
            if (!uus.isMember(user, purchasingGroup)) {
                valid = false;
                GlobalVariables.getErrorMap().putError(PurapPropertyConstants.PURAP_DOC_ID , KFSKeyConstants.AUTHORIZATION_ERROR_DOCUMENT, initiatorNetworkId, "amend", PurapConstants.PurchaseOrderDocTypes.PURCHASE_ORDER_DOCUMENT);
            }
        }
        catch (UserNotFoundException ue) {
            valid = false;
        }
        valid &= validateContainsAtLeastOneActiveItem(purapDocument);
        return valid;
    }
    
    private boolean validateContainsAtLeastOneActiveItem(PurchasingAccountsPayableDocument purapDocument) {
        List<PurchasingApItem> items = purapDocument.getItems();
        for (PurchasingApItem item : items) {
            if (((PurchaseOrderItem)item).isItemActiveIndicator() && (!((PurchaseOrderItem)item).isEmpty() && item.getItemType().isItemTypeAboveTheLineIndicator())) {
                return true;
            }
        }
        String documentType = SpringServiceLocator.getDataDictionaryService().getDataDictionary().getDocumentEntry(purapDocument.getDocumentHeader().getWorkflowDocument().getDocumentType()).getLabel();
        
        GlobalVariables.getErrorMap().putError("newPurchasingItemLine", PurapKeyConstants.ERROR_ITEM_REQUIRED, documentType);
        return false;
    }

}
