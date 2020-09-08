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
package org.openmrs.module.rwandareports.reporting;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

import java.util.*;

public class SetupCKDQuarterlyAndMonthlyReport {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties
	private Program CKDProgram;

	private Program DMProgram;

	private Program asthmaProgram;

	private Program heartFailureProgram;

	private Program hypertensionProgram;

	private List<Program> CKDPrograms = new ArrayList<Program>();

	private List<Program> NCDPrograms = new ArrayList<Program>();


	private EncounterType CKDEncounterType;

	private EncounterType HFHTNCKDEncounterType;

	private List<EncounterType> patientsSeenEncounterTypes = new ArrayList<EncounterType>();
	
	private Form rendevousForm;

	private Form CKDEnrollmentForm;

	private List<Form> cardConsultForm = new ArrayList<Form>();

	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private List<String> enrolledOnOrAfterOnOrBefore = new ArrayList<String>();
	
	private List<Form> DDBAndRendezvousForms = new ArrayList<Form>();

	private List<Form> EnrollmentForms = new ArrayList<Form>();


	private Concept NCDSpecificOutcomes;
	private Concept NCDRelatedDeathOutcomes;

	private Concept exitReasonFromCare;

	List<Concept> DeathOutcomeResons=new  ArrayList<Concept>();

	private Concept unknownCauseDeathOutcomes;
	private Concept otherCauseOfDeathOutcomes;
	private Concept NCDLostToFolloUpOutCome;

	StringBuilder deathAndLostToFollowUpOutcomeString=new StringBuilder();

	
	public void setup() throws Exception {
		
		setUpProperties();

		// Quarterly Report Definition: Start
		
		ReportDefinition quarterlyRd = new ReportDefinition();
		quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));

		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");

		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		quarterlyRd.setName("NCD-CKD Indicator Report-Quarterly");
		
		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: End
		
		ProgramEnrollmentCohortDefinition patientEnrolledInCKDProgram = new ProgramEnrollmentCohortDefinition();
		patientEnrolledInCKDProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore",
				Date.class));
		patientEnrolledInCKDProgram.setPrograms(CKDPrograms);
		

		quarterlyRd.setBaseCohortDefinition(patientEnrolledInCKDProgram,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));

		Helper.saveReportDefinition(quarterlyRd);

		
		ReportDesign quarterlyDesign = Helper.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
		    "CKD_Indicator_Quarterly_Report.xls", "CKD Quarterly Indicator Report (Excel)", null);
		Properties quarterlyProps = new Properties();
		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
		quarterlyProps.put("sortWeight","5000");
		quarterlyDesign.setProperties(quarterlyProps);
		Helper.saveReportDesign(quarterlyDesign);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("CKD Quarterly Indicator Report (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-CKD Indicator Report-Quarterly");

	}
	
	//Create Quarterly Encounter Data set
	
	public LocationHierachyIndicatorDataSetDefinition createQuarterlyLocationDataSet() {
		
		LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
		        createEncounterQuarterlyBaseDataSet());
		ldsd.addBaseDefinition(createQuarterlyBaseDataSet());
		ldsd.setName("Encounter Quarterly Data Set");
		ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		return ldsd;
	}
	
	private EncounterIndicatorDataSetDefinition createEncounterQuarterlyBaseDataSet() {
		
		EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
		
		eidsd.setName("eidsd");
		eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		createQuarterlyIndicators(eidsd);
		return eidsd;
	}
	
	private void createQuarterlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
		
		//=======================================================================
		//  A1: Total # of patient visits to CKD clinic in the last quarter
		//==================================================================
		SqlEncounterQuery patientVisitsToCKDClinic = new SqlEncounterQuery();
		
		patientVisitsToCKDClinic
		        .setQuery("select encounter_id from encounter where encounter_type in ("
						+ CKDEncounterType.getEncounterTypeId() + ","
						+ HFHTNCKDEncounterType.getEncounterTypeId()
						+ ") and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id");
		patientVisitsToCKDClinic.setName("patientVisitsToCKDClinic");
		patientVisitsToCKDClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToCKDClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator patientVisitsToCKDClinicQuarterlyIndicator = new EncounterIndicator();
		patientVisitsToCKDClinicQuarterlyIndicator.setName("patientVisitsToCKDClinicQuarterlyIndicator");
		patientVisitsToCKDClinicQuarterlyIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
				patientVisitsToCKDClinic, ParameterizableUtil
				.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(patientVisitsToCKDClinicQuarterlyIndicator);

		
	}
	
	// create quarterly cohort Data set
	private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Quarterly Cohort Data Set");
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		createQuarterlyIndicators(dsd);
		return dsd;
	}
	
	private void createQuarterlyIndicators(CohortIndicatorDataSetDefinition dsd) {
		
		EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("patientsWithCKDVisit",
			    onOrAfterOnOrBefore, patientsSeenEncounterTypes);

		//=================================================
		//  A2: % of newly enrolled patients in the past quarter
		//=================================================

		//========================================================================================
		//  Active patients with no exit in Reporting periode
		//=======================================================================================

		SqlCohortDefinition currentlyInProgramAndNotCompleted=new SqlCohortDefinition();
		currentlyInProgramAndNotCompleted.setName("currentlyInProgramAndNotCompleted");
		currentlyInProgramAndNotCompleted.setQuery("select patient_id from patient_program where voided=0 and program_id="+CKDProgram.getProgramId()+" and (date_completed> :endDate or date_completed is null) and date_enrolled<= :endDate");
		currentlyInProgramAndNotCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));


		SqlCohortDefinition programOutcomeNCDRelatedDeath=Cohorts.getPatientsWithCodedObservationsBetweenStartDateAndEndDate("NCDRelatedDeath",NCDSpecificOutcomes,NCDRelatedDeathOutcomes);

		SqlCohortDefinition obsNCDRelatedDeath=Cohorts.getPatientsWithCodedObservationsBetweenStartDateAndEndDate("obsNCDRelatedDeath",NCDSpecificOutcomes,NCDRelatedDeathOutcomes);



		CompositionCohortDefinition NCDRelatedDeath= new CompositionCohortDefinition();
		NCDRelatedDeath.setName("NCDRelatedDeath");
		NCDRelatedDeath.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		NCDRelatedDeath.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		NCDRelatedDeath.getSearches().put("1",new Mapped<CohortDefinition>(programOutcomeNCDRelatedDeath, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		NCDRelatedDeath.getSearches().put("2",new Mapped<CohortDefinition>(obsNCDRelatedDeath, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		NCDRelatedDeath.setCompositionString("1 OR 2");


		SqlCohortDefinition patientWithAnyReasonForExitingFromCare=Cohorts.getPatientsWithObsGreaterThanNtimesByEndDate("patientWithAnyReasonForExitingFromCare", exitReasonFromCare, 1);

		CompositionCohortDefinition activePatientWithNoExitBeforeQuarterStart = new CompositionCohortDefinition();
		activePatientWithNoExitBeforeQuarterStart.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientWithNoExitBeforeQuarterStart.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithNoExitBeforeQuarterStart.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientWithNoExitBeforeQuarterStart.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithNoExitBeforeQuarterStart.getSearches().put("1",new Mapped<CohortDefinition>(currentlyInProgramAndNotCompleted, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientWithNoExitBeforeQuarterStart.getSearches().put("2",new Mapped<CohortDefinition>(patientWithAnyReasonForExitingFromCare, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientWithNoExitBeforeQuarterStart.getSearches().put("3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${endDate-12m}")));
		activePatientWithNoExitBeforeQuarterStart.setCompositionString("1 and 3 and (not 2)");


		CohortIndicator activePatientIndicator = Indicators.newCountIndicator("activePatientIndicator",
				activePatientWithNoExitBeforeQuarterStart,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("Active", "Total # of Active patients : seen in the last 12 months", new Mapped(
				activePatientIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


		//=========================================
		//  # of newly enrolled CKD patients
		//========================================


		ProgramEnrollmentCohortDefinition patientEnrolledInCKDProgram = Cohorts.createProgramEnrollmentParameterizedByStartEndDate(
				"Enrolled In CKDProgram", CKDProgram);

		ProgramEnrollmentCohortDefinition patientEnrolledInCKDProgramByEndDate = Cohorts.createProgramEnrollmentEverByEndDate("Enrolled Ever In CKDProgram", CKDProgram);

		CompositionCohortDefinition patientCurrentEnrolledInCKDInSameQuarter=new CompositionCohortDefinition();
		patientCurrentEnrolledInCKDInSameQuarter.setName("patientCurrentEnrolledInCKDInSameQuarter");/*
		patientCurrentEnrolledInCKDInSameQuarter.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientCurrentEnrolledInCKDInSameQuarter.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));*/
		patientCurrentEnrolledInCKDInSameQuarter.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientCurrentEnrolledInCKDInSameQuarter.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientCurrentEnrolledInCKDInSameQuarter.getSearches().put(
				"1",new Mapped<CohortDefinition>(patientEnrolledInCKDProgram, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore},enrolledOnOrAfter=${enrolledOnOrAfter}")));
		patientCurrentEnrolledInCKDInSameQuarter.getSearches().put(
				"2",new Mapped<CohortDefinition>(patientEnrolledInCKDProgramByEndDate, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		/*patientCurrentEnrolledInCKDInSameQuarter.getSearches().put(
				"3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
*/
		//patientCurrentEnrolledInCKDInSameQuarter.setCompositionString("(1 and (not 2)) and 3");
		patientCurrentEnrolledInCKDInSameQuarter.setCompositionString("1 and (not 2)");




		CohortIndicator patientCurrentEnrolledInCKDAndSeenInSameQuarterIndicator = Indicators.newCountIndicator(
				"patientCurrentEnrolledInCKDAndSeenInSameQuarterIndicator", patientCurrentEnrolledInCKDInSameQuarter,
				ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

		dsd.addColumn(
				"NewInQ",
				"Total # of new patients enrolled in the last quarter",
				new Mapped(patientCurrentEnrolledInCKDAndSeenInSameQuarterIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



//============================================
// A3: Percentage of active patients
//============================================
		InProgramCohortDefinition inCKDProgramByEndDate=Cohorts.createInProgramParameterizableByDate("In CKDProgram by EndDate",CKDProgram);
		ProgramEnrollmentCohortDefinition completedInCKDProgramByEndDate=Cohorts.createProgramCompletedByEndDate("Completed CKDProgram by EndDate",CKDProgram);




		CohortIndicator currentlyInProgramAndNotCompletedIndicator=Indicators.newCountIndicator(
				"currentlyInProgramAndNotCompletedIndicator", currentlyInProgramAndNotCompleted,
				ParameterizableUtil.createParameterMappings("endDate=${endDate}"));

		dsd.addColumn(
				"CurEnrol",
				"Percentage of active patients)",
				new Mapped(currentlyInProgramAndNotCompletedIndicator, ParameterizableUtil
						.createParameterMappings("endDate=${endDate}")), "");
//==============================================
// A4:% of all active patients that are age <=15 years old
//===============================================


		AgeCohortDefinition under16=Cohorts.createUnderAgeCohort("under16",15);

		CompositionCohortDefinition activePatientUnder16 = new CompositionCohortDefinition();
		activePatientUnder16.setName("activePatientUnder16");
		activePatientUnder16.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientUnder16.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientUnder16.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientUnder16.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientUnder16.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		activePatientUnder16.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		activePatientUnder16.getSearches().put("2",new Mapped<CohortDefinition>(under16, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
		activePatientUnder16.setCompositionString("1 and 2");


		CohortIndicator activePatientUnder16Indicator = Indicators.newCountIndicator("activePatientUnder16Indicator",
				activePatientUnder16,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},effectiveDate=${endDate}"));


		dsd.addColumn("ActiveUnder16", "% of all active patients that are age <=15 years old", new Mapped(
				activePatientUnder16Indicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

//==============================================
// A5: % of all active patients that are Male
//===============================================

		GenderCohortDefinition malePatients = Cohorts.createMaleCohortDefinition("Male patients");



		CompositionCohortDefinition activeMalePatients = new CompositionCohortDefinition();
		activeMalePatients.setName("activeMalePatients");
		activeMalePatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeMalePatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeMalePatients.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeMalePatients.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeMalePatients.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));
		activeMalePatients.getSearches().put("2",new Mapped<CohortDefinition>(malePatients, null));
		activeMalePatients.setCompositionString("1 and 2");


		CohortIndicator activeMalePatientsIndicator = Indicators.newCountIndicator("activeMalePatientsIndicator",
				activeMalePatients,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("ActiveMale", "% of all active patients that are Male", new Mapped(
				activeMalePatientsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


//=============================================
// A6: Proportion of patients ever enrolled who are currently enrolled
//=============================================
		CohortIndicator patientEverEnrolledInCKDIndicator = Indicators.newCountIndicator(
				"patientEverEnrolledInCKDIndicator", patientEnrolledInCKDProgramByEndDate,
				ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate+1d}"));

		dsd.addColumn(
				"EverEnrolled",
				"Total # of patients evere enrolled in the last quarter",
				new Mapped(patientEverEnrolledInCKDIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



//=================================================================================
//  A7: % of deaths which are disease related
//=================================================================================
//Numerator

		SqlCohortDefinition patientsDied= Cohorts.getPatientsDiedByStartDateAndEndDate("patientsDied");


		CompositionCohortDefinition activePatientsInPatientDiedStateOrNCDRelatedDeath = new CompositionCohortDefinition();
		activePatientsInPatientDiedStateOrNCDRelatedDeath.setName("activePatientsInPatientDiedState");
		activePatientsInPatientDiedStateOrNCDRelatedDeath.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientsInPatientDiedStateOrNCDRelatedDeath.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientsInPatientDiedStateOrNCDRelatedDeath.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientsInPatientDiedStateOrNCDRelatedDeath.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m+1d}")));
		activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientsDied, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore+1d},onOrAfter=${onOrAfter}")));

		activePatientsInPatientDiedStateOrNCDRelatedDeath.setCompositionString("1 AND 3");


		CohortIndicator activePatientsInPatientDiedStateOrNCDRelatedDeathIndicator = Indicators.newCountIndicator(
				"NCDRelatedDeathIndicator", activePatientsInPatientDiedStateOrNCDRelatedDeath,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn("A7N", "% of deaths which are disease related", new Mapped(
				activePatientsInPatientDiedStateOrNCDRelatedDeathIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		CohortIndicator patientsSeenInYearIndicator = Indicators.newCountIndicator("patientsSeenIndicator", patientSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate-9m},onOrBefore=${endDate}"));

		dsd.addColumn(
				"ActiveY",
				"Total active in Year patients",
				new Mapped(patientsSeenInYearIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		// A8

		SqlCohortDefinition patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes=new SqlCohortDefinition();
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.setName("patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes");
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.setQuery("select patient_id from patient_program where program_id="+CKDProgram.getProgramId()+" and date_completed>= :onOrAfter and date_completed<= :onOrBefore and voided=0 and outcome_concept_id not in ("+deathAndLostToFollowUpOutcomeString.toString()+")");
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));

		SqlCohortDefinition patientWhoCompletedProgram=new SqlCohortDefinition();
		patientWhoCompletedProgram.setName("patientWhoCompletedProgram");
		patientWhoCompletedProgram.setQuery("select patient_id from patient_program where program_id="+CKDProgram.getProgramId()+" and date_completed>= :onOrAfter and date_completed<= :onOrBefore and voided=0");
		patientWhoCompletedProgram.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWhoCompletedProgram.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));




		CohortIndicator patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator = Indicators.newCountIndicator("patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator", patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn(
				"A8N",
				"patient Who Completed Program Without Death And Lost To Followup Outcomes",
				new Mapped(patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		CohortIndicator patientWhoCompletedProgramIndicator = Indicators.newCountIndicator("patientWhoCompletedProgramIndicator", patientWhoCompletedProgram,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn(
				"A8D",
				"patient Who Completed Program ",
				new Mapped(patientWhoCompletedProgramIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");




// B Active in other NCD program

int i=1;
		for(Program p:NCDPrograms){


			InProgramCohortDefinition inProgramByEndDate=Cohorts.createInProgramParameterizableByDate("In Program by EndDate",p);
			ProgramEnrollmentCohortDefinition completedInProgramByEndDate=Cohorts.createProgramCompletedByEndDate("Completed Program by EndDate",p);

			CompositionCohortDefinition activeCKDPatientsWithEnrolledInOtherProg = new CompositionCohortDefinition();
			activeCKDPatientsWithEnrolledInOtherProg.setName("activeCKDPatientsWithEnrolledInOtherProg");
			activeCKDPatientsWithEnrolledInOtherProg.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
			activeCKDPatientsWithEnrolledInOtherProg.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
			activeCKDPatientsWithEnrolledInOtherProg.addParameter(new Parameter("startDate", "startDate", Date.class));
			activeCKDPatientsWithEnrolledInOtherProg.addParameter(new Parameter("endDate", "endDate", Date.class));
			activeCKDPatientsWithEnrolledInOtherProg.addParameter(new Parameter("onDate", "onDate", Date.class));
			activeCKDPatientsWithEnrolledInOtherProg.addParameter(new Parameter("completedOnOrBefore", "completedOnOrBefore", Date.class));
			activeCKDPatientsWithEnrolledInOtherProg.getSearches().put("1",new Mapped<CohortDefinition>(inProgramByEndDate, ParameterizableUtil.createParameterMappings("onDate=${onDate}")));
			activeCKDPatientsWithEnrolledInOtherProg.getSearches().put("2",new Mapped<CohortDefinition>(completedInProgramByEndDate, ParameterizableUtil.createParameterMappings("completedOnOrBefore=${completedOnOrBefore}")));
			activeCKDPatientsWithEnrolledInOtherProg.getSearches().put("3",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},startDate=${startDate},endDate=${endDate}")));
			activeCKDPatientsWithEnrolledInOtherProg.setCompositionString("(1 and (not 2)) and 3");


			CohortIndicator activeCKDPatientsWithEnrolledInOtherProgIndicator=Indicators.newCountIndicator(
					"activeCKDPatientsWithEnrolledInOtherProgIndicator", activeCKDPatientsWithEnrolledInOtherProg,
					ParameterizableUtil.createParameterMappings("onDate=${endDate},completedOnOrBefore=${endDate},onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


			dsd.addColumn("B"+i, "Number of Active CKD patients who is in"+p.getName(), new Mapped(
					activeCKDPatientsWithEnrolledInOtherProgIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
				i++;
		}






	}

	
	private void setUpProperties() {
		CKDProgram = gp.getProgram(GlobalPropertiesManagement.CKD_PROGRAM);

		DMProgram = gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);

		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);

		heartFailureProgram = gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM_NAME);

		hypertensionProgram = gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM);


		NCDPrograms.add(heartFailureProgram);
		NCDPrograms.add(hypertensionProgram);
		NCDPrograms.add(DMProgram);
		NCDPrograms.add(asthmaProgram);


		CKDPrograms.add(CKDProgram);
		
		CKDEncounterType = gp.getEncounterType(GlobalPropertiesManagement.CKD_ENCOUNTER_TYPE);

		HFHTNCKDEncounterType=gp.getEncounterType(GlobalPropertiesManagement.HF_HTN_CKD_ENCOUNTER_TYPE);


		patientsSeenEncounterTypes.add(CKDEncounterType);
		patientsSeenEncounterTypes.add(HFHTNCKDEncounterType);
		CKDEnrollmentForm =gp.getForm(GlobalPropertiesManagement.CKD_ENROLLMENT_FORM);
		
		rendevousForm = gp.getForm(GlobalPropertiesManagement.CKD_RDV_FORM);
		
		DDBAndRendezvousForms.add(rendevousForm);
		
		DDBAndRendezvousForms.add(CKDEnrollmentForm);

		EnrollmentForms.add(CKDEnrollmentForm);


		onOrAfterOnOrBefore.add("onOrAfter");
		
		onOrAfterOnOrBefore.add("onOrBefore");
		
		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrAfter");
		
		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrBefore");
		
		NCDSpecificOutcomes=gp.getConcept(GlobalPropertiesManagement.NCD_SPECIFIC_OUTCOMES);
		NCDRelatedDeathOutcomes= gp.getConcept(GlobalPropertiesManagement.NCD_RELATED_DEATH_OUTCOMES);

		exitReasonFromCare=gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);

		unknownCauseDeathOutcomes =gp.getConcept(GlobalPropertiesManagement.UNKNOWN_CAUSE_OF_DEATH_OUTCOMES);
		otherCauseOfDeathOutcomes =gp.getConcept(GlobalPropertiesManagement.OTHER_CAUSE_OF_DEATH_OUTCOMES);


		DeathOutcomeResons.add(NCDRelatedDeathOutcomes);
		DeathOutcomeResons.add(unknownCauseDeathOutcomes);
		DeathOutcomeResons.add(otherCauseOfDeathOutcomes);

		cardConsultForm.add(gp.getForm(GlobalPropertiesManagement.CARDIOLOGY_CONSULT_FORM));

		NCDLostToFolloUpOutCome =gp.getConcept(GlobalPropertiesManagement.LOST_TO_FOLLOWUP_OUTCOME);
		deathAndLostToFollowUpOutcomeString.append(NCDRelatedDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(unknownCauseDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(otherCauseOfDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(NCDLostToFolloUpOutCome.getConceptId());


		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.POST_CARDIAC_SURGERY_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.CKD_ENCOUNTER_TYPE));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HF_HTN_CKD_ENCOUNTER_TYPE));




	}
}