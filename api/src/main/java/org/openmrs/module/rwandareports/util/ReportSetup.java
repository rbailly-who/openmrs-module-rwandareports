/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.util;

import java.util.Arrays;
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
import org.openmrs.module.rwandareports.reporting.SetupAdultHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAdultLateVisitAndCD4Report;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupAsthmaQuarterlyAndMonthReport;
import org.openmrs.module.rwandareports.reporting.SetupCKDConsultationSheetReport;
import org.openmrs.module.rwandareports.reporting.SetupCKDMissedvisitReport;
import org.openmrs.module.rwandareports.reporting.SetupCKDQuarterlyAndMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupChemotherapyDailyExpectedPatientList;
import org.openmrs.module.rwandareports.reporting.SetupChemotherapyExpectedPatientList;
import org.openmrs.module.rwandareports.reporting.SetupCombinedHFCSPConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupDataEntryDelayReport;
import org.openmrs.module.rwandareports.reporting.SetupDataEntryQuantityReport;
import org.openmrs.module.rwandareports.reporting.SetupDataQualityIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupDiabetesConsultAndLTFU;
import org.openmrs.module.rwandareports.reporting.SetupDiabetesQuarterlyAndMonthReport;
import org.openmrs.module.rwandareports.reporting.SetupExposedClinicInfantMonthly;
import org.openmrs.module.rwandareports.reporting.SetupGenericDrugReport;
import org.openmrs.module.rwandareports.reporting.SetupGenericEncounterReport;
import org.openmrs.module.rwandareports.reporting.SetupGenericPatientByProgramReport;
import org.openmrs.module.rwandareports.reporting.SetupHMISIndicatorMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupHMISRwandaReportBySite;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailureConsultSheet;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailureLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupHeartFailureQuarterlyAndMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupHypertensionConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupHypertensionLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupHypertensionQuarterlyAndMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupIDProgramQuarterlyIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupMentalHealthConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupMentalHealthIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupMentalHealthLateVisit;
import org.openmrs.module.rwandareports.reporting.SetupMissedChemotherapyPatientList;
import org.openmrs.module.rwandareports.reporting.SetupMonthlyExecutiveDashboardMetricsReport;
import org.openmrs.module.rwandareports.reporting.SetupOncologyExternalBiopsyContactList;
import org.openmrs.module.rwandareports.reporting.SetupOncologyInpatientClinicMissedVisit;
import org.openmrs.module.rwandareports.reporting.SetupOncologyOutpatientAppointmentList;
import org.openmrs.module.rwandareports.reporting.SetupOncologyOutpatientClinicMissedVisit;
import org.openmrs.module.rwandareports.reporting.SetupOncologyOutpatientClinicPatientList;
import org.openmrs.module.rwandareports.reporting.SetupOncologyOutpatientExpectedPatientList;
import org.openmrs.module.rwandareports.reporting.SetupOncologyQuarterlyIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupOncologyRegistry;
import org.openmrs.module.rwandareports.reporting.SetupOncologyTestPatientList;
import org.openmrs.module.rwandareports.reporting.SetupPDCIndicatorReport;
import org.openmrs.module.rwandareports.reporting.SetupPDCMissedVisits;
import org.openmrs.module.rwandareports.reporting.SetupPDCMonthlyAlert;
import org.openmrs.module.rwandareports.reporting.SetupPDCMonthlyLTFU;
import org.openmrs.module.rwandareports.reporting.SetupPDCWeeklyAlert;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTCombinedClinicMotherMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyConsultationReport;
import org.openmrs.module.rwandareports.reporting.SetupPMTCTPregnancyMonthlyReport;
import org.openmrs.module.rwandareports.reporting.SetupPediHIVConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SetupReport;
import org.openmrs.module.rwandareports.reporting.SetupTBConsultationSheet;
import org.openmrs.module.rwandareports.reporting.SingleSetupReport;

/**
 * Setups up all reports by category
 */
public class ReportSetup {

	private static final Log log = LogFactory.getLog(ReportSetup.class);

	//public enum ReportCategories{ HIV,NCD,CENTRAL,SITE,ONCOLOGY,CHW}

	public static String classification = Context.getAdministrationService().getGlobalProperty(
			GlobalPropertiesManagement.REPORT_CLASSIFICATION
	);

	public static void cleanTables() {

		ReportService rs = Context.getService(ReportService.class);
		List<ReportDesign> rDes = rs.getAllReportDesigns(true);
		for (ReportDesign reportDesign : rDes) {
			rs.purgeReportDesign(reportDesign);
		}

		ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
		List<ReportDefinition> rDefs = rds.getAllDefinitions(true);
		for (ReportDefinition reportDefinition : rDefs) {
			rds.purgeDefinition(reportDefinition);
		}

		for (ReportRequest request : rs.getReportRequests(null, null, null, Status.COMPLETED, Status.FAILED)) {
			try {
				rs.purgeReportRequest(request);
			}
			catch (Exception e) {
				log.warn("Unable to delete old report request: " + request, e);
			}
		}
	}

	public static void registerReports() {

		String[] classifications = classification.split(",");

		for (String category : classifications) {
			if (category.equalsIgnoreCase("HIV"))
				registerHIVReports();
			else if (category.equalsIgnoreCase("NCD"))
				registerNCDReports();
			else if (category.equalsIgnoreCase("CENTRAL"))
				registerCentralReports();
			else if (category.equalsIgnoreCase("SITE"))
				registerSiteReports();
			else if (category.equalsIgnoreCase("ONCOLOGY"))
				registerOncologyReports();
			else if (category.equalsIgnoreCase("CHW"))
				registerCHWReports();
//			else if (category.equalsIgnoreCase("PC")) {
//				registerPCReports();
			else if (category.equalsIgnoreCase("PDC")) {
				registerPDCReports();
			}
		}
	}

	public static void setupReport(SetupReport report) {
		try {
			log.info("Setting up " + report);
			report.setup();
		}
		catch (Exception e) {
			log.error("Error setting up " + report, e);
		}
	}

	public static List<SetupReport> HIV_REPORTS = Arrays.asList(
		//new SetupHivArtRegisterReport(false)
		//new SetupHivArtRegisterReport(true)
		new SetupCombinedHFCSPConsultationReport(),
		//new SetupPMTCTFoodDistributionReport().
		//new SetupPMTCTFormulaDistributionReport()
		new SetupPMTCTPregnancyConsultationReport(),
		new SetupPediHIVConsultationSheet(),
		new SetupAdultHIVConsultationSheet(),
		new SetupTBConsultationSheet(),
		new SetupAdultLateVisitAndCD4Report(),
		//new SetupPediatricLateVisitAndCD4Report()
		new SetupHMISRwandaReportBySite(),
		//new SetupPBFReport()
		new SetupPMTCTCombinedClinicMotherMonthlyReport(),
		new SetupPMTCTPregnancyMonthlyReport(),
		new SetupExposedClinicInfantMonthly()
	);

	public static void registerHIVReports() {
		for (SetupReport report : HIV_REPORTS) {
			setupReport(report);
		}
	}

	public static List<SetupReport> NCD_REPORTS = Arrays.asList(
		new SetupAsthmaConsultationSheet(),
		new SetupDiabetesConsultAndLTFU(),
		new SetupDiabetesQuarterlyAndMonthReport(),
		new SetupAsthmaQuarterlyAndMonthReport(),
		new SetupAsthmaLateVisit(),
		new SetupHypertensionConsultationSheet(),
		new SetupHypertensionLateVisit(),
		new SetupHypertensionQuarterlyAndMonthlyReport(),
		new SetupHeartFailureConsultSheet(),
		new SetupHeartFailureLateVisit(),
		new SetupHeartFailureQuarterlyAndMonthlyReport(),
		new SetupCKDConsultationSheetReport(),
		new SetupCKDQuarterlyAndMonthlyReport(),
		new SetupCKDMissedvisitReport()
		//new SetupNCDConsultationSheet(),
		//new SetupNCDLateVisitandLTFUReport(),
	);

	public static void registerNCDReports() {
		for (SetupReport report : NCD_REPORTS) {
			setupReport(report);
		}
	}

	public static List<SetupReport> CENTRAL_REPORTS = Arrays.asList(

		new SetupIDProgramQuarterlyIndicatorReport(),
		//new SetupMonthlyCD4DeclineReport(),
		//new SetupMissingCD4Report(),
		//new SetupPMTCTFormCompletionSheet(),
		new SetupDataEntryQuantityReport(),
		//new SetupHMISMOHReport(),
		new SetupHMISIndicatorMonthlyReport(),
		new SetupMonthlyExecutiveDashboardMetricsReport()
	);

	public static void registerCentralReports() {
		for (SetupReport report : CENTRAL_REPORTS) {
			setupReport(report);
		}
	}

	public static List<SetupReport> SITE_REPORTS = Arrays.asList(
		new SetupDataQualityIndicatorReport(),
		new SetupDataEntryDelayReport(),
		new SetupGenericEncounterReport(),
		new SetupGenericPatientByProgramReport(),
		new SetupGenericDrugReport()
	);

	public static void registerSiteReports() {
		for (SetupReport report : SITE_REPORTS) {
			setupReport(report);
		}
	}

	public static List<SetupReport> ONCOLOGY_REPORTS = Arrays.asList(
		new SetupChemotherapyExpectedPatientList(),
		new SetupOncologyOutpatientExpectedPatientList(),
		new SetupChemotherapyDailyExpectedPatientList(),
		new SetupOncologyInpatientClinicMissedVisit(),
		new SetupMissedChemotherapyPatientList(),
		new SetupOncologyOutpatientClinicPatientList(),
		new SetupOncologyOutpatientClinicMissedVisit(),
		new SetupOncologyOutpatientAppointmentList(),
		new SetupOncologyQuarterlyIndicatorReport(),
		new SetupOncologyTestPatientList(),
		new SetupOncologyExternalBiopsyContactList(),
		new SetupOncologyRegistry()
	);

	public static void registerOncologyReports() {
		for (SetupReport report : ONCOLOGY_REPORTS) {
			setupReport(report);
		}
	}

	public static void registerCHWReports() {
	}

//	public static void registerPCReports() {
//		setupReport(new SetupRwandaPrimaryCareReport());
//	}

	public static List<SingleSetupReport> PDC_REPORTS = Arrays.asList(
		new SetupPDCWeeklyAlert(),
		new SetupPDCMonthlyAlert(),
		new SetupPDCIndicatorReport(),
		new SetupPDCMonthlyLTFU(),
		new SetupPDCMissedVisits()
	);

	public static void registerPDCReports() {
		for (SetupReport report : PDC_REPORTS) {
			setupReport(report);
		}
	}

	public static List<SingleSetupReport> MH_REPORTS = Arrays.asList(
		new SetupMentalHealthConsultationSheet(),
		new SetupMentalHealthLateVisit(),
		new SetupMentalHealthIndicatorReport()
	);

	public static void registerMHReports() {
		for (SetupReport report : MH_REPORTS) {
			setupReport(report);
		}
	}

}
