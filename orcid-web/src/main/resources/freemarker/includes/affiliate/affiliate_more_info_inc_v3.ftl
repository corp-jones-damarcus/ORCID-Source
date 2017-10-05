<#--

    =============================================================================

    ORCID (R) Open Source
    http://orcid.org

    Copyright (c) 2012-2014 ORCID, Inc.
    Licensed under an MIT-Style License (MIT)
    http://orcid.org/open-source-license

    This copyright and license information (including a link to the full license)
    shall be included in its entirety in all copies or substantial portion of
    the software.

    =============================================================================

-->
<div class="more-info content" ng-if="moreInfo[group.groupId]">
	<div class="row bottomBuffer">
		<div class="col-md-12"></div>
	</div>
	<span class="dotted-bar"></span>	
	<div class="row">
		<div ng-if="group.getActive().orgDisambiguatedId.value">
            <div class="col-md-12 bottomBuffer">   
                <strong><@orcid.msg 'workspace_affiliations.organization_id'/></strong><br>
                <span bind-html-compile='group.getActive().disambiguatedAffiliationSourceId.value | orgIdentifierHtml:group.getActive().disambiguationSource.value:group.getActive().putCode.value:group.getActive().disambiguationSource' class="url-popover"> 
                </span>
            </div>
            <div class="col-md-11 col-md-offset-1 bottomBuffer">
                <span ng-bind="group.getActive().orgDisambiguatedValue"></span>: <span ng-bind="group.getActive().orgDisambiguatedCity"></span>, <span ng-if="group.getActive().orgDisambiguatedRegion" ng-cloak><span ng-bind="group.getActive().disambiguatedAffiliationRegion"></span>, </span><span ng-bind="group.getActive().orgDisambiguatedCountry"></span><br>
                <span ng-if="group.getActive().orgDisambiguatedUrl">
                <a href="group.getActive().orgDisambiguatedUrl" target=""><span ng-bind="group.getActive().orgDisambiguatedUrl" ng-cloak></span></a>
                </span>
            </div>
        </div>
        <div class="col-md-6" ng-if="group.getActive().url.value" ng-cloak>
        	<div class="bottomBuffer">
				<strong><@orcid.msg 'common.url'/></strong><br> 
				<a href="{{group.getActive().url.value}}" target="affiliation.url.value">{{group.getActive().url.value}}</a>
			</div>
		</div>	
        <div class="col-md-12">
        	<div class="bottomBuffer">
				<strong><@orcid.msg 'groups.common.created'/></strong><br> 
				<span ng-bind="group.getActive().createdDate | ajaxFormDateToISO8601"></span>
			</div>
		</div>	
	</div>			
</div>
 