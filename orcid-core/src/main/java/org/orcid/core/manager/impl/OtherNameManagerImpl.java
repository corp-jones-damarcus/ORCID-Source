package org.orcid.core.manager.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.orcid.core.exception.ApplicationException;
import org.orcid.core.exception.OrcidDuplicatedElementException;
import org.orcid.core.manager.OrcidSecurityManager;
import org.orcid.core.manager.OtherNameManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.SourceManager;
import org.orcid.core.manager.read_only.impl.OtherNameManagerReadOnlyImpl;
import org.orcid.core.manager.validator.PersonValidator;
import org.orcid.core.utils.DisplayIndexCalculatorHelper;
import org.orcid.core.utils.SourceEntityUtils;
import org.orcid.jaxb.model.common_v2.Visibility;
import org.orcid.jaxb.model.record_v2.OtherName;
import org.orcid.jaxb.model.record_v2.OtherNames;
import org.orcid.persistence.jpa.entities.OtherNameEntity;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.persistence.jpa.entities.SourceEntity;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.springframework.transaction.annotation.Transactional;

public class OtherNameManagerImpl extends OtherNameManagerReadOnlyImpl implements OtherNameManager {

    @Resource
    private OrcidSecurityManager orcidSecurityManager;

    @Resource
    private SourceManager sourceManager;

    @Resource
    private ProfileEntityCacheManager profileEntityCacheManager;

    @Override
    @Transactional
    public boolean deleteOtherName(String orcid, Long putCode, boolean checkSource) {
        OtherNameEntity otherNameEntity = otherNameDao.getOtherName(orcid, putCode);

        if (checkSource) {
            orcidSecurityManager.checkSource(otherNameEntity);
        }

        try {
            otherNameDao.deleteOtherName(otherNameEntity);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public OtherName createOtherName(String orcid, OtherName otherName, boolean isApiRequest) {
        SourceEntity sourceEntity = sourceManager.retrieveSourceEntity();
        PersonValidator.validateOtherName(otherName, sourceEntity, true, isApiRequest, null);

        // Validate it is not duplicated
        List<OtherNameEntity> existingOtherNames = otherNameDao.getOtherNames(orcid, getLastModified(orcid));
        for (OtherNameEntity existing : existingOtherNames) {
            if (isDuplicated(existing, otherName, sourceEntity)) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "other-name");
                params.put("value", otherName.getContent());
                throw new OrcidDuplicatedElementException(params);
            }
        }

        OtherNameEntity newEntity = jpaJaxbOtherNameAdapter.toOtherNameEntity(otherName);
        ProfileEntity profile = profileEntityCacheManager.retrieve(orcid);
        newEntity.setOrcid(orcid);
        
        // Set the source
        if (sourceEntity.getSourceProfile() != null) {
            newEntity.setSourceId(sourceEntity.getSourceProfile().getId());
        }

        if (sourceEntity.getSourceClient() != null) {
            newEntity.setClientSourceId(sourceEntity.getSourceClient().getId());
        }

        setIncomingPrivacy(newEntity, profile);
        DisplayIndexCalculatorHelper.setDisplayIndexOnNewEntity(newEntity, isApiRequest);
        otherNameDao.persist(newEntity);
        return jpaJaxbOtherNameAdapter.toOtherName(newEntity);
    }

    @Override
    @Transactional
    public OtherName updateOtherName(String orcid, Long putCode, OtherName otherName, boolean isApiRequest) {
        SourceEntity sourceEntity = sourceManager.retrieveSourceEntity();
        OtherNameEntity updatedOtherNameEntity = otherNameDao.getOtherName(orcid, putCode);
        Visibility originalVisibility = Visibility.fromValue(updatedOtherNameEntity.getVisibility());
        // Save the original source
        String existingSourceId = updatedOtherNameEntity.getSourceId();
        String existingClientSourceId = updatedOtherNameEntity.getClientSourceId();
        // Validate the other name
        PersonValidator.validateOtherName(otherName, sourceEntity, false, isApiRequest, originalVisibility);

        // Validate it is not duplicated
        List<OtherNameEntity> existingOtherNames = otherNameDao.getOtherNames(orcid, getLastModified(orcid));
        for (OtherNameEntity existing : existingOtherNames) {
            if (isDuplicated(existing, otherName, sourceEntity)) {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type", "otherName");
                params.put("value", otherName.getContent());
                throw new OrcidDuplicatedElementException(params);
            }
        }

        orcidSecurityManager.checkSource(updatedOtherNameEntity);
        jpaJaxbOtherNameAdapter.toOtherNameEntity(otherName, updatedOtherNameEntity);
        // Be sure it doesn't overwrite the source
        updatedOtherNameEntity.setSourceId(existingSourceId);
        updatedOtherNameEntity.setClientSourceId(existingClientSourceId);

        otherNameDao.merge(updatedOtherNameEntity);
        return jpaJaxbOtherNameAdapter.toOtherName(updatedOtherNameEntity);
    }

    @Override
    @Transactional
    public OtherNames updateOtherNames(String orcid, OtherNames otherNames) {
        List<OtherNameEntity> existingOtherNamesEntityList = otherNameDao.getOtherNames(orcid, getLastModified(orcid));
        // Delete the deleted ones
        for (OtherNameEntity existingOtherName : existingOtherNamesEntityList) {
            boolean deleteMe = true;
            if (otherNames.getOtherNames() != null) {
                for (OtherName updatedOrNew : otherNames.getOtherNames()) {
                    if (existingOtherName.getId().equals(updatedOrNew.getPutCode())) {
                        deleteMe = false;
                        break;
                    }
                }
            }

            if (deleteMe) {
                try {
                    otherNameDao.deleteOtherName(existingOtherName);
                } catch (Exception e) {
                    throw new ApplicationException("Unable to delete other name " + existingOtherName.getId(), e);
                }
            }
        }

        if (otherNames != null && otherNames.getOtherNames() != null) {
            for (OtherName updatedOrNew : otherNames.getOtherNames()) {
                if (updatedOrNew.getPutCode() != null) {
                    // Update the existing ones
                    for (OtherNameEntity existingOtherName : existingOtherNamesEntityList) {
                        if (existingOtherName.getId().equals(updatedOrNew.getPutCode())) {
                            existingOtherName.setVisibility(updatedOrNew.getVisibility().name());
                            existingOtherName.setDisplayName(updatedOrNew.getContent());
                            existingOtherName.setDisplayIndex(updatedOrNew.getDisplayIndex());
                            otherNameDao.merge(existingOtherName);
                        }
                    }
                } else {
                    // Add the new ones
                    OtherNameEntity newOtherName = jpaJaxbOtherNameAdapter.toOtherNameEntity(updatedOrNew);
                    SourceEntity sourceEntity = sourceManager.retrieveSourceEntity();
                    newOtherName.setOrcid(orcid);
                    // Set the source
                    if (sourceEntity.getSourceProfile() != null) {
                        newOtherName.setSourceId(sourceEntity.getSourceProfile().getId());
                    }
                    if (sourceEntity.getSourceClient() != null) {
                        newOtherName.setClientSourceId(sourceEntity.getSourceClient().getId());
                    }
                    newOtherName.setVisibility(updatedOrNew.getVisibility().name());
                    newOtherName.setDisplayIndex(updatedOrNew.getDisplayIndex());
                    otherNameDao.persist(newOtherName);

                }
            }
        }
        return otherNames;
    }

    private boolean isDuplicated(OtherNameEntity existing, OtherName otherName, SourceEntity source) {
        if (!existing.getId().equals(otherName.getPutCode())) {
            String existingSourceId = existing.getElementSourceId();
            if (!PojoUtil.isEmpty(existingSourceId) && existingSourceId.equals(SourceEntityUtils.getSourceId(source))) {
                if (existing.getDisplayName() != null && existing.getDisplayName().equals(otherName.getContent())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setIncomingPrivacy(OtherNameEntity entity, ProfileEntity profile) {
        String incomingOtherNameVisibility = entity.getVisibility();
        String defaultOtherNamesVisibility = (profile.getActivitiesVisibilityDefault() == null) ? org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name()
                : profile.getActivitiesVisibilityDefault();
        if (profile.getClaimed()) {
            entity.setVisibility(defaultOtherNamesVisibility);
        } else if (incomingOtherNameVisibility == null) {
            entity.setVisibility(org.orcid.jaxb.model.common_v2.Visibility.PRIVATE.name());
        }
    }

    @Override
    public void removeAllOtherNames(String orcid) {
        otherNameDao.removeAllOtherNames(orcid);
    }
}
