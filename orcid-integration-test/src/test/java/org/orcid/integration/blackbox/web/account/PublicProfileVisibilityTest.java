/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.integration.blackbox.web.account;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;

import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ById;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.orcid.integration.blackbox.api.BBBUtil;
import org.orcid.integration.blackbox.api.v2.rc2.BlackBoxBaseRC2;
import org.orcid.jaxb.model.common_rc2.Iso3166Country;
import org.orcid.jaxb.model.common_rc2.Visibility;
import org.orcid.jaxb.model.groupid_rc2.GroupIdRecord;
import org.orcid.jaxb.model.message.ScopePathType;
import org.orcid.jaxb.model.record_rc1.WorkExternalIdentifierType;
import org.orcid.jaxb.model.record_rc2.ExternalID;
import org.orcid.jaxb.model.record_rc2.PeerReview;
import org.orcid.jaxb.model.record_rc2.Relationship;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Shobhit Tyagi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-memberV2-context.xml" })
public class PublicProfileVisibilityTest extends BlackBoxBaseRC2 {
    @BeforeClass
    public static void before() {
        signin();
    }

    @AfterClass
    public static void after() {
        signout();
    }

    @Test
    public void emailPrivacyTest() throws InterruptedException {
        //Add a public email
        String emailValue = "added.email." + System.currentTimeMillis() + "@test.com";
        showAccountSettingsPage();
        openEditEmailsSectionOnAccountSettingsPage();
        addEmail(emailValue, Visibility.PRIVATE);
        
        showPublicProfilePage(getUser1OrcidId());
        try {
            //Verify it doesn't appear in the public page
            emailAppearsInPublicPage(emailValue);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility to limited
        showAccountSettingsPage();
        openEditEmailsSectionOnAccountSettingsPage();
        updateEmailVisibility(emailValue, Visibility.LIMITED);
        showPublicProfilePage(getUser1OrcidId());
        try {
            //Verify it doesn't appear in the public page
            emailAppearsInPublicPage(emailValue);
            fail();
        } catch(Exception e) {
            
        }        
        
        //Change visibility to public
        showAccountSettingsPage();
        openEditEmailsSectionOnAccountSettingsPage();
        updateEmailVisibility(emailValue, Visibility.PUBLIC);
        //Verify it appears in the public page
        showPublicProfilePage(getUser1OrcidId());
        emailAppearsInPublicPage(emailValue);
        
        //Delete the new email
        showAccountSettingsPage();
        openEditEmailsSectionOnAccountSettingsPage();
        removeEmail(emailValue);
    }

    @Test
    public void otherNamesPrivacyTest() throws InterruptedException, JSONException {
        String otherNameValue = "added-other-name-" + System.currentTimeMillis();
                        
        //Create a new other name and set it to public        
        showMyOrcidPage();
        openEditOtherNamesModal();
        createOtherName(otherNameValue);
        changeOtherNamesVisibility(Visibility.PRIVATE);
        saveOtherNamesModal();
        
        //Verify it doesn't appear in the public page
        try {
            showPublicProfilePage(getUser1OrcidId());
            otherNamesAppearsInPublicPage(otherNameValue);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility to limited
        showMyOrcidPage();
        openEditOtherNamesModal();        
        changeOtherNamesVisibility(Visibility.LIMITED);
        saveOtherNamesModal();
        
        //Verify it doesn't appear in the public page
        try {
            showPublicProfilePage(getUser1OrcidId());
            otherNamesAppearsInPublicPage(otherNameValue);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility again to public
        showMyOrcidPage();
        openEditOtherNamesModal();        
        changeOtherNamesVisibility(Visibility.PUBLIC);
        saveOtherNamesModal();
        
        //Verify it appears again in the public page
        showPublicProfilePage(getUser1OrcidId());
        otherNamesAppearsInPublicPage(otherNameValue);
        
        //Delete it
        showMyOrcidPage();
        openEditOtherNamesModal();
        deleteOtherNames();
        saveOtherNamesModal();
    }    
        
    @Test
    public void addressPrivacyTest() throws InterruptedException, JSONException {
        showMyOrcidPage();
        openEditAddressModal();
        deleteAddresses();
        createAddress(Iso3166Country.ZW.name());
        changeAddressVisibility(Visibility.PRIVATE);  
        saveEditAddressModal();
      
        //Verify it doesn't appears again in the public page
        try {
            showPublicProfilePage(getUser1OrcidId());
            addressAppearsInPublicPage("Zimbabwe");
            fail();
        } catch(Exception e) {
            
        }
                
        //Change visibility to limited
        showMyOrcidPage();
        openEditAddressModal();
        changeAddressVisibility(Visibility.LIMITED);
        saveEditAddressModal();
        
        //Verify it doesn't appears again in the public page
        try {
            showPublicProfilePage(getUser1OrcidId());
            addressAppearsInPublicPage("Zimbabwe");
            fail();
        } catch(Exception e) {
            
        }
                
        //Change visibility to public again
        showMyOrcidPage();
        openEditAddressModal();
        changeAddressVisibility(Visibility.PUBLIC);
        saveEditAddressModal();
        
        //Verify it appears again in the public page
        showPublicProfilePage(getUser1OrcidId());
        addressAppearsInPublicPage("Zimbabwe");
        
        showMyOrcidPage();
        openEditAddressModal();
        deleteAddresses();
        saveEditAddressModal();
    }
    
    @Test
    public void keywordPrivacyTest() throws InterruptedException, JSONException {
        String keywordValue = "added-keyword-" + System.currentTimeMillis();
        //Create a new other name and set it to public
        showMyOrcidPage();
        openEditKeywordsModal();
        createKeyword(keywordValue);
        changeKeywordsVisibility(Visibility.PRIVATE);
        saveKeywordsModal();
        
        //Verify it doesn't appear in the public page
        try {
            showPublicProfilePage(getUser1OrcidId());
            keywordsAppearsInPublicPage(keywordValue);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility to limited
        showMyOrcidPage();
        openEditKeywordsModal();        
        changeKeywordsVisibility(Visibility.LIMITED);
        saveKeywordsModal();
        
        //Verify it doesn't appear in the public page
        try {
            showPublicProfilePage(getUser1OrcidId());
            keywordsAppearsInPublicPage(keywordValue);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility again to public
        showMyOrcidPage();
        openEditKeywordsModal();
        changeKeywordsVisibility(Visibility.PUBLIC);
        saveKeywordsModal();
        
        //Verify it appears again in the public page
        showPublicProfilePage(getUser1OrcidId());
        keywordsAppearsInPublicPage(keywordValue);
        
        //Delete it
        showMyOrcidPage();
        openEditKeywordsModal();        
        deleteKeywords();
        saveKeywordsModal();
    }

    @Test
    public void websitesPrivacyTest() throws InterruptedException, JSONException {
        String rUrl = "http://test.orcid.org/" + System.currentTimeMillis();        
        //Create a new other name and set it to public
        showMyOrcidPage();
        openEditResearcherUrlsModal();
        createResearcherUrl(rUrl);
        changeResearcherUrlsVisibility(Visibility.PRIVATE);
        saveResearcherUrlsModal();
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            researcherUrlAppearsInPublicPage(rUrl);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility to limited
        showMyOrcidPage();
        openEditResearcherUrlsModal();
        changeResearcherUrlsVisibility(Visibility.LIMITED);
        saveResearcherUrlsModal();
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            researcherUrlAppearsInPublicPage(rUrl);
            fail();
        } catch(Exception e) {
            
        }
          
        //Change visibility to public
        showMyOrcidPage();
        openEditResearcherUrlsModal();
        changeResearcherUrlsVisibility(Visibility.PUBLIC);
        saveResearcherUrlsModal();
        
        //Verify it appears again in the public page
        showPublicProfilePage(getUser1OrcidId());
        researcherUrlAppearsInPublicPage(rUrl);
        
        deleteResearcherUrls();
    }
    
    @Test
    public void externalIdentifiersPrivacyTest() throws InterruptedException, JSONException {
        String extId = "added-ext-id-" + System.currentTimeMillis();
        String accessToken = getAccessToken(getScopes(ScopePathType.PERSON_READ_LIMITED, ScopePathType.PERSON_UPDATE));
        //Create a new external identifier and set it to public
        createExternalIdentifier(extId, getUser1OrcidId(), accessToken);
        showMyOrcidPage();
        openEditExternalIdentifiersModal();
        changeExternalIdentifiersVisibility(Visibility.PRIVATE);
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            externalIdentifiersAppearsInPublicPage(extId);
            fail();
        } catch(Exception e) {
            
        }
        
        //Change visibility to limited
        showMyOrcidPage();
        openEditExternalIdentifiersModal();
        changeExternalIdentifiersVisibility(Visibility.LIMITED);
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            externalIdentifiersAppearsInPublicPage(extId);
            fail();
        } catch(Exception e) {
            
        }              
        
        //Change visibility back to public
        showMyOrcidPage();
        openEditExternalIdentifiersModal();
        changeExternalIdentifiersVisibility(Visibility.PUBLIC);
        
        //Verify it appears again in the public page
        showPublicProfilePage(getUser1OrcidId());
        externalIdentifiersAppearsInPublicPage(extId);
        
        deleteExternalIdentifiers();
    }

    @Test
    public void workPrivacyTest() throws InterruptedException, JSONException {
        String workTitle = "added-work-" + System.currentTimeMillis();
        showMyOrcidPage();
        openAddWorkModal();
        createWork(workTitle);
        changeWorksVisibility(workTitle, Visibility.PRIVATE);
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            workAppearsInPublicPage(workTitle);
            fail();
        } catch(Exception e) {
            
        }    
    
        //Change visibility to limited
        showMyOrcidPage();
        changeWorksVisibility(workTitle, Visibility.LIMITED);
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            workAppearsInPublicPage(workTitle);
            fail();
        } catch(Exception e) {
            
        }
        
        showMyOrcidPage();
        changeWorksVisibility(workTitle, Visibility.PUBLIC);
        
        //Verify it appear in the public page
        showPublicProfilePage(getUser1OrcidId());
        workAppearsInPublicPage(workTitle);
        
        showMyOrcidPage();
        deleteWork(workTitle);
    }
    
    @Test
    public void educationPrivacyTest() {
        String institutionName = "added-education-" + System.currentTimeMillis();
        showMyOrcidPage();
        openAddEducationModal();
        createEducation(institutionName);
        changeEducationVisibility(institutionName, Visibility.PRIVATE);        
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            educationAppearsInPublicPage(institutionName);
            fail();
        } catch(Exception e) {
            
        }   
        
        showMyOrcidPage();
        changeEducationVisibility(institutionName, Visibility.LIMITED);
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            educationAppearsInPublicPage(institutionName);
            fail();
        } catch(Exception e) {
            
        } 
        
        showMyOrcidPage();
        changeEducationVisibility(institutionName, Visibility.PUBLIC);
        
        //Verify it appears in the public page
        showPublicProfilePage(getUser1OrcidId());
        educationAppearsInPublicPage(institutionName);
        
        showMyOrcidPage();
        deleteEducation(institutionName);
    }
    
    @Test
    public void employmentPrivacyTest() {
        String institutionName = "added-employment-" + System.currentTimeMillis();
        showMyOrcidPage();
        openAddEmploymentModal();
        createEmployment(institutionName);
        changeEmploymentVisibility(institutionName, Visibility.PRIVATE);        
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            employmentAppearsInPublicPage(institutionName);
            fail();
        } catch(Exception e) {
            
        }
        
        showMyOrcidPage();
        changeEmploymentVisibility(institutionName, Visibility.LIMITED);
        
        try {
            //Verify it doesn't appear in the public page
            showPublicProfilePage(getUser1OrcidId());
            employmentAppearsInPublicPage(institutionName);
            fail();
        } catch(Exception e) {
            
        } 
        
        showMyOrcidPage();
        changeEmploymentVisibility(institutionName, Visibility.PUBLIC);
        
        //Verify it appears in the public page
        showPublicProfilePage(getUser1OrcidId());
        employmentAppearsInPublicPage(institutionName);
        
        showMyOrcidPage();
        deleteEmployment(institutionName);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Test
    public void fundingPrivacyTest() throws InterruptedException {
        
    }

    
    

    
    
    
    
    
    
    
    @Test
    public void peerReviewPrivacyTest() throws InterruptedException, JSONException, URISyntaxException {
        //TODO: refactor to match pattern used in bio sections
        // Create peer review group               
        String accessToken = getAccessToken(getScopes(ScopePathType.ACTIVITIES_UPDATE, ScopePathType.ACTIVITIES_READ_LIMITED));
        GroupIdRecord g1 = super.createGroupIdRecord();

        // Create peer review
        long time = System.currentTimeMillis();
        PeerReview peerReview = (PeerReview) unmarshallFromPath("/record_2.0_rc2/samples/peer-review-2.0_rc2.xml", PeerReview.class);
        peerReview.setPutCode(null);
        peerReview.setGroupId(g1.getGroupId());
        peerReview.getExternalIdentifiers().getExternalIdentifier().clear();
        ExternalID wExtId = new ExternalID();
        wExtId.setValue("Work Id " + time);
        wExtId.setType(WorkExternalIdentifierType.AGR.value());
        wExtId.setRelationship(Relationship.SELF);
        peerReview.getExternalIdentifiers().getExternalIdentifier().add(wExtId);

        ClientResponse postResponse = memberV2ApiClient.createPeerReviewXml(this.getUser1OrcidId(), peerReview, accessToken);
        assertNotNull(postResponse);
        assertEquals(Response.Status.CREATED.getStatusCode(), postResponse.getStatus());
        ClientResponse getResponse = memberV2ApiClient.viewLocationXml(postResponse.getLocation(), accessToken);
        assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus());
        peerReview = getResponse.getEntity(PeerReview.class);

        // Set it private
        showMyOrcidPage();
        BBBUtil.extremeWaitFor(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]")), webDriver);
        WebElement peerReviewElement = webDriver.findElement(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]"));
        BBBUtil.ngAwareClick(peerReviewElement.findElement(By.xpath(".//div[@id='privacy-bar']/ul/li[3]/a")), webDriver);
        BBBUtil.extremeWaitFor(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]")), webDriver);

        // Check the public page
        showPublicProfilePage(getUser1OrcidId());
        try {
            (new WebDriverWait(webDriver, BBBUtil.TIMEOUT_SECONDS, BBBUtil.SLEEP_MILLISECONDS))
            .until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]")));
            fail();
        } catch (Exception e) {

        }

        // Set it public
        showMyOrcidPage();
        BBBUtil.extremeWaitFor(ExpectedConditions.presenceOfElementLocated(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]")), webDriver);
        peerReviewElement = webDriver.findElement(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]"));
        BBBUtil.ngAwareClick(peerReviewElement.findElement(By.xpath(".//div[@id='privacy-bar']/ul/li[1]/a")), webDriver);
        BBBUtil.extremeWaitFor(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]")), webDriver);

        // Check the public page
        showPublicProfilePage(getUser1OrcidId());
        BBBUtil.extremeWaitFor(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[@orcid-put-code and descendant::span[text() = '" + g1.getName() + "']]")), webDriver);

        // Rollback
        ClientResponse deleteResponse = memberV2ApiClient.deletePeerReviewXml(this.getUser1OrcidId(), peerReview.getPutCode(), accessToken);
        assertNotNull(deleteResponse);
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), deleteResponse.getStatus());
    }
}
