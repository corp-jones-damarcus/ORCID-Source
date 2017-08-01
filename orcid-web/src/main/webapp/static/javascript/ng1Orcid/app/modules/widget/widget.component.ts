declare var getBaseUri: any;
declare var scriptTmpl: any;
declare var orcidVar: any;

import * as angular from 'angular';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';  
import { NgModule } from '@angular/core';
/*
//Ng1 hybrid syntax
export class widgetCtrl {

}

export const widgetCmp = {
    controller: widgetCtrl,
    controllerAs: 'ctrl'
};
*/

import {Component, Input} from '@angular/core';
@Component({
    selector: 'widget-ng2',
    //template: 'asdfgg'
    template:  scriptTmpl("widget-ng2-template")
})
export class WidgetComponent {
    hash: any;
    showCode: any;
    widgetURLND: any;
    
    constructor() {
        this.hash = orcidVar.orcidIdHash.substr(0, 6);
        this.showCode = false;
        this.widgetURLND = '<a href="'+ getBaseUri() + '/' + orcidVar.orcidId + '" target="orcid.widget" rel="noopener noreferrer" style="vertical-align:top;"><img src="https://orcid.org/sites/default/files/images/orcid_16x16.png" style="width:1em;margin-right:.5em;" alt="ORCID iD icon">' + orcidVar.baseDomainRmProtocall + '/' + orcidVar.orcidId + '</a>';
    }

    hideWidgetCode(): void{
        this.showCode = false;
    };

    inputTextAreaSelectAll($event): any{
        $event.target.select();
    };
        
    toggleCopyWidget(): void{
        this.showCode = !this.showCode;
    };
}