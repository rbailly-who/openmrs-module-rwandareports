/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.reporting.*;

/**
 *
 */
public class CleanReportingTablesAndRegisterAllReports {
	
	private static Log log = LogFactory.getLog(CleanReportingTablesAndRegisterAllReports.class);
	
	//public enum ReportCategories{ HIV,NCD,CENTRAL,SITE,ONCOLOGY,CHW}
	
	public static String classification= Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.REPORT_CLASSIFICATION);
	public static String scheduledclassification= Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.SCHEDULED_REPORT_CLASSIFICATION);


	public static void cleanTables() throws Exception {				
	
		
		ReportService rs=Context.getService(ReportService.class);
		List<ReportDesign> rDes=rs.getAllReportDesigns(true);		
		for (ReportDesign reportDesign : rDes) {
	        rs.purgeReportDesign(reportDesign);
        }
		
		ReportDefinitionService rds=Context.getService(ReportDefinitionService.class);
		List<ReportDefinition> rDefs=rds.getAllDefinitions(true);		
		for (ReportDefinition reportDefinition : rDefs) {		
			rds.purgeDefinition(reportDefinition);
        }
		
		for (ReportRequest request : rs.getReportRequests(null, null, null, Status.COMPLETED,Status.FAILED)) {
			try {
				rs.purgeReportRequest(request);
			}
			catch (Exception e) {
				log.warn("Unable to delete old report request: " + request, e);
			}
		}
		
			
    }
	
	public static void registerReports() throws Exception{
		
		String[] classifications=classification.split(",");
		
		for (String category : classifications) {
	       if(category.equalsIgnoreCase("HIV")) 
	    	   registerHIVReports();
	       else if(category.equalsIgnoreCase("NCD"))
	    	   registerNCDReports();
	       else if(category.equalsIgnoreCase("CENTRAL"))
	    	   registerCentralReports();
	       else if(category.equalsIgnoreCase("SITE"))
	    	   registerSiteReports();
	       else if(category.equalsIgnoreCase("ONCOLOGY"))
	    	   registerOncologyReports();
	       else if(category.equalsIgnoreCase("CHW"))
	    	   registerCHWReports();
	       else if(category.equalsIgnoreCase("PC"))
	       {
	    	   registerPCReports();
	       }
	       else if(category.equalsIgnoreCase("PDC"))
	       {
	    	   registerPDCReports();
	       }
        }
			
	}
	public static void registerHIVReports() throws Exception {
			////new SetupHivArtRegisterReport(false).setup();
			////new SetupHivArtRegisterReport(true).setup();
			
			new SetupCombinedHFCSPConsultationReport().setup();
//			new SetupPMTCTFoodDistributionReport().setup();
//			new SetupPMTCTFormulaDistributionReport().setup();
			new SetupPMTCTPregnancyConsultationReport().setup(); 
			new SetupPediHIVConsultationSheet().setup();
			new SetupAdultHIVConsultationSheet().setup();
			new SetupTBConsultationSheet().setup();
			new SetupAdultLateVisitAndCD4Report().setup();
//			new SetupPediatricLateVisitAndCD4Report().setup();
			new SetupHMISRwandaReportBySite().setup();
			//new SetupPBFReport().setup();
			new SetupPMTCTCombinedClinicMotherMonthlyReport().setup();
			new SetupPMTCTPregnancyMonthlyReport().setup();
			new SetupExposedClinicInfantMonthly().setup();
			new SetupCROWNReports().setup();
    }
	public static void registerNCDReports() throws Exception {
			new SetupAsthmaConsultationSheet().setup();  
		
			new SetupDiabetesConsultAndLTFU().setup();
			new SetupDiabetesQuarterlyAndMonthReport().setup();
			new SetupAsthmaQuarterlyAndMonthReport().setup();
			new SetupAsthmaLateVisit().setup();  
			
			new SetupEpilepsyConsultationSheet().setup();
			new SetupEpilepsyLateVisit().setup();
			new SetupHypertensionConsultationSheet().setup();
			new SetupHypertensionLateVisit().setup();
			new SetupHypertensionQuarterlyAndMonthlyReport().setup();
			
			new SetupHeartFailureConsultSheet().setup();
			new SetupHeartFailureLateVisit().setup();
			new SetupHeartFailureQuarterlyAndMonthlyReport().setup();

			new SetupCKDConsultationSheetReport().setup();
			new SetupCKDQuarterlyAndMonthlyReport();
			new SetupCKDMissedvisitReport().setup();
//			new SetupNCDConsultationSheet().setup();
//			new SetupNCDLateVisitandLTFUReport().setup();

    }
	public static void registerCentralReports() throws Exception {
			new SetupHIVResearchDataQualitySheet().setup();
			new SetupHIVResearchExtractionSheet().setup();
			new SetupIDProgramQuarterlyIndicatorReport().setup(); 
//			new SetupMonthlyCD4DeclineReport().setup();
//			new SetupMissingCD4Report().setup();
			new SetupQuarterlyCrossSiteIndicatorByDistrictReport().setup();	
			new SetupQuarterlyViralLoadReport().setup();
//			new SetupPMTCTFormCompletionSheet().setup();
			new SetupEligibleForViralLoadReport().setup();
			new SetupDataEntryQuantityReport().setup();
//			new SetupHMISMOHReport().setup();
			new SetupHMISIndicatorMonthlyReport().setup();
			new SetupMonthlyExecutiveDashboardMetricsReport().setup();

	}
	public static void registerSiteReports() throws Exception {
			new SetupDataQualityIndicatorReport().setup();
			new SetupDataEntryDelayReport().setup();
			new SetupGenericEncounterReport().setup();
			new SetupGenericPatientByProgramReport().setup();
			new SetupGenericDrugReport().setup();
    }
	public static void registerOncologyReports() throws Exception {
			new SetupOncologyTreatmentAdministrationPlan().setup();
			new SetupChemotherapyExpectedPatientList().setup();
			new SetupOncologyOutpatientExpectedPatientList().setup();
			new SetupChemotherapyDailyExpectedPatientList().setup();
			new SetupOncologyInpatientClinicMissedVisit().setup();
			new SetupOncologyDailyDrugList().setup();
			new SetupMissedChemotherapyPatientList().setup();
			new SetupOncologyOutpatientClinicPatientList().setup();
			new SetupOncologyOutpatientClinicMissedVisit().setup();
			new SetupOncologyOutpatientAppointmentList().setup();
			new SetupOncologyQuarterlyIndicatorReport().setup();
			new SetupOncologyTestPatientList().setup();
			new SetupOncologyExternalBiopsyContactList().setup();
			new SetupOncologyRegistry().setup();
    }
	public static void registerCHWReports() {
	    
    }
	public static void registerPCReports() throws Exception {
			new SetupRwandaPrimaryCareReport().setup();
	}
	public static void registerPDCReports() throws Exception {
		new SetupPDCWeeklyAlert().setup();
		new SetupPDCMonthlyAlert().setup();
		new SetupPDCIndicatorReport().setup();
		new SetupPDCMonthlyLTFU().setup();
		new SetupPDCMissedVisits().setup();
	}
	public static void registerMHReports() throws Exception{
		new SetupMentalHealthConsultationSheet().setup();
		new SetupMentalHealthLateVisit().setup();
		new SetupMentalHealthIndicatorReport().setup();
	}



	public static void registerScheduledReports() throws Exception {
		String[] Scheduledclassification=scheduledclassification.split(",");

		for (String category : Scheduledclassification) {
			if(category.equalsIgnoreCase("ALLSERVERS"))
				registerScheduledAllServersReports();
			else if(category.equalsIgnoreCase("ONCOLOGY"))
			{
				registerScheduledOncologyReports();
			}
			else if(category.equalsIgnoreCase("HOSPITAL"))
			{
				registerScheduledHospitalReports();
			}
		}
	}

	public static void registerScheduledAllServersReports() throws Exception{
		new SetupDataEntryQuantityReport().setup();
		new SetupHMISIndicatorMonthlyReport().setup();
		new SetupDataEntryDelayReport().setup();
		new SetupDataQualityIndicatorReport().setup();
		new SetupHIVResearchDataQualitySheet().setup();
		new SetupGenericDrugReport().setup();
		new SetupGenericEncounterReport().setup();
		new SetupGenericPatientByProgramReport().setup();
		new SetupAdultLateVisitAndCD4Report().setup();
		new SetupAdultHIVConsultationSheet().setup();
		new SetupHMISRwandaReportBySite().setup();
		new SetupIDProgramQuarterlyIndicatorReport().setup();
		new SetupPediHIVConsultationSheet().setup();
		new SetupCombinedHFCSPConsultationReport().setup();
		new SetupPMTCTCombinedClinicMotherMonthlyReport().setup();
		new SetupExposedClinicInfantMonthly().setup();
		new SetupPMTCTPregnancyConsultationReport().setup();
		new SetupPMTCTPregnancyMonthlyReport().setup();
		new SetupMentalHealthConsultationSheet().setup();
		new SetupMentalHealthLateVisit().setup();
		new SetupMentalHealthIndicatorReport().setup();
		new SetupMonthlyExecutiveDashboardMetricsReport().setup();
		new SetupAsthmaConsultationSheet().setup();
		new SetupAsthmaQuarterlyAndMonthReport().setup();
		new SetupAsthmaLateVisit().setup();
		new SetupCKDConsultationSheetReport().setup();
		new SetupCKDMissedvisitReport().setup();
		new SetupCKDQuarterlyAndMonthlyReport().setup();
		new SetupDiabetesConsultAndLTFU().setup();
		new SetupDiabetesQuarterlyAndMonthReport().setup();
		new SetupHypertensionConsultationSheet().setup();
		new SetupHypertensionQuarterlyAndMonthlyReport().setup();
		new SetupHypertensionLateVisit().setup();
		new SetupPDCMissedVisits().setup();
		new SetupPDCMonthlyLTFU().setup();
		new SetupPDCIndicatorReport().setup();
		new SetupPDCWeeklyAlert().setup();
		new SetupEligibleForViralLoadReport().setup();
	}
	public static void registerScheduledOncologyReports() throws Exception{
		new SetupOncologyTestPatientList().setup();
		new SetupOncologyDailyDrugList().setup();
		new SetupChemotherapyDailyExpectedPatientList().setup();
		new SetupMissedChemotherapyPatientList().setup();
		new SetupOncologyTreatmentAdministrationPlan().setup();
		new SetupOncologyExternalBiopsyContactList().setup();
		new SetupOncologyQuarterlyIndicatorReport().setup();
		new SetupOncologyDataExtractionSheet().setup();
		new SetupChemotherapyExpectedPatientList().setup();
		new SetupOncologyOutpatientExpectedPatientList().setup();
		new SetupOncologyOutpatientAppointmentList().setup();
		new SetupOncologyOutpatientClinicPatientList().setup();
		new SetupOncologyRegistry().setup();
		new SetupOncologyOutpatientClinicMissedVisit().setup();
		new SetupOncologyInpatientClinicMissedVisit().setup();
	}
	public static void registerScheduledHospitalReports() throws Exception{
		new SetupHeartFailureConsultSheet().setup();
		new SetupHeartFailureQuarterlyAndMonthlyReport().setup();
		new SetupQuarterlyCrossSiteIndicatorByDistrictReport().setup();
		new SetupHeartFailureLateVisit().setup();
		new SetupQuarterlyViralLoadReport().setup();
		new SetupOncologyOutpatientExpectedPatientList().setup();
		new SetupOncologyOutpatientClinicMissedVisit().setup();
		new SetupOncologyInpatientClinicMissedVisit().setup();
		new SetupOncologyOutpatientAppointmentList().setup();
		new SetupOncologyOutpatientClinicPatientList().setup();
		new SetupOncologyDailyDrugList().setup();
		new SetupChemotherapyDailyExpectedPatientList().setup();
		new SetupMissedChemotherapyPatientList().setup();

	}
	}
