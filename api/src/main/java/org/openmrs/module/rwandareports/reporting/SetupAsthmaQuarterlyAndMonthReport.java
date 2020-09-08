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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Program;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.RangeComparator;
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

public class SetupAsthmaQuarterlyAndMonthReport {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties
	private Program asthmaProgram;
	
	private List<Program> asthmaPrograms = new ArrayList<Program>();
	
	private EncounterType asthmaEncounterType;
	
	private Form DDBform;
	
	private List<EncounterType> patientsSeenEncounterTypes = new ArrayList<EncounterType>();
	
	private Form rendevousForm;
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private List<String> enrolledOnOrAfterOnOrBefore = new ArrayList<String>();
	
	//private List<Concept> peacFlowConcepts = new ArrayList<Concept>();

	List<Concept> DeathOutcomeResons=new  ArrayList<Concept>();
	
	private Concept peakFlowAfterSalbutamol;
	
	private Concept peakFlowBeforeSalbutamol;
	
	private Concept smokingHistory;
	
	private Concept salbutamol;
	
	private Concept beclomethasone;
	
	private Concept prednisolone;
	
	private Concept severePersistentAsthma;
	
	private Concept severeUncontrolledAsthma;
	
	private Concept asthmaclassification;
	
	private Concept returnVisitDate;
	
	private List<Form> DDBforms = new ArrayList<Form>();
	
	private List<Concept> asthmasMedications = new ArrayList<Concept>();
	
	private List<Concept> asthmasClassificationAnswers = new ArrayList<Concept>();
	
	private List<Concept> asthmasMedicationsWithoutSalbutamol = new ArrayList<Concept>();
	

	private Concept NCDSpecificOutcomes;
	private Concept NCDRelatedDeathOutcomes;
	private Concept unknownCauseDeathOutcomes;
	private Concept otherCauseOfDeathOutcomes;
	private Concept NCDLostToFolloUpOutCome;

	StringBuilder deathAndLostToFollowUpOutcomeString=new StringBuilder();

	private Concept exitReasonFromCare;

	private Concept HIVStatus;

	private Concept TBScreening;

	//private Concept hospitalAdmissionDate;

	private Concept diagnosisWhileHospitalized;

	private Concept asthmaExacerbation;

	private Concept intermittentasthma;


	private Concept moderatePersistentAsthma;
	private Concept mildPersistentAsthma;

	private Form Enrollmentform;
	private List<Form> DDBandEnrollmentforms = new ArrayList<Form>();

	private Form asthmaVisitForm;

	private List<Form> asthmaVisitForms = new ArrayList<Form>();





	public void setup() throws Exception {
		
		setUpProperties();
		
		//Monthly report set-up

		
		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");

		
		// Quarterly Report Definition: Start
		
		ReportDefinition quarterlyRd = new ReportDefinition();
		quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		quarterlyRd.setName("NCD-Asthma Indicator Report-Quarterly");
		
		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: End
		
		ProgramEnrollmentCohortDefinition patientEnrolledInAsthmaProgram = new ProgramEnrollmentCohortDefinition();
		patientEnrolledInAsthmaProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientEnrolledInAsthmaProgram.setPrograms(asthmaPrograms);
		

		
		quarterlyRd.setBaseCohortDefinition(patientEnrolledInAsthmaProgram,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));
		
		Helper.saveReportDefinition(quarterlyRd);
		

		ReportDesign quarterlyDesign = Helper.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
		    "Asthma_Indicator_Quarterly_Report.xls", "Asthma Indicator Quarterly Report (Excel)", null);
		Properties quarterlyProps = new Properties();
		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
		quarterlyProps.put("sortWeight","5000");
		quarterlyDesign.setProperties(quarterlyProps);
		Helper.saveReportDesign(quarterlyDesign);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Asthma Indicator Quarterly Report (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Asthma Indicator Report-Quarterly");
		
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
		//  A1: Total # of patient visits to DM clinic in the last quarter
		//==================================================================
		SqlEncounterQuery patientVisitsToAsthmaClinic = new SqlEncounterQuery();

		patientVisitsToAsthmaClinic
		        .setQuery("select encounter_id from encounter where encounter_type="+asthmaEncounterType.getEncounterTypeId()+" and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id");
		patientVisitsToAsthmaClinic.setName("patientVisitsToAsthmaClinic");
		patientVisitsToAsthmaClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToAsthmaClinic.addParameter(new Parameter("endDate", "endDate", Date.class));

		EncounterIndicator patientVisitsToAsthmaClinicQuarterlyIndicator = new EncounterIndicator();
		patientVisitsToAsthmaClinicQuarterlyIndicator.setName("patientVisitsToAsthmaClinicQuarterlyIndicator");
		patientVisitsToAsthmaClinicQuarterlyIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
		        patientVisitsToAsthmaClinic, ParameterizableUtil
		                .createParameterMappings("endDate=${endDate},startDate=${startDate}")));

		dsd.addColumn(patientVisitsToAsthmaClinicQuarterlyIndicator);

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
		
		//=======================================================================
		//  A2: Total # of patients seen in the last month/quarter
		//==================================================================



		EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
		    onOrAfterOnOrBefore, patientsSeenEncounterTypes);
		
		EncounterCohortDefinition patientWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
		    onOrAfterOnOrBefore, DDBforms);
		
		CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
		patientsSeenComposition.setName("patientsSeenComposition");
		patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenComposition.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsSeenComposition.setCompositionString("1 OR 2");
		
		CohortIndicator patientsSeenQuarterIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthOneIndicator = Indicators.newCountIndicator("patientsSeenMonthOneIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientsSeenMonthTwoIndicator = Indicators.newCountIndicator("patientsSeenMonthTwoIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
		CohortIndicator patientsSeenMonthThreeIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
		    patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));



		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn("A2Q", "Total # of patients seen in the last quarter", new Mapped(patientsSeenQuarterIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM1", "Total # of patients seen in the last month one", new Mapped(patientsSeenMonthOneIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM2", "Total # of patients seen in the last month two", new Mapped(patientsSeenMonthTwoIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A2QM3", "Total # of patients seen in the last month three", new Mapped(
		        patientsSeenMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



		//=======================================================================
		// A3: Total # of new patients enrolled in the last month/quarter
		//==================================================================

		ProgramEnrollmentCohortDefinition patientEnrolledInCRDP = Cohorts.createProgramEnrollmentParameterizedByStartEndDate("Enrolled In CRDP",
				asthmaProgram);

		ProgramEnrollmentCohortDefinition patientEnrolledInCRDPByEndDate = Cohorts.createProgramEnrollmentEverByEndDate("Enrolled In CRDP",
				asthmaProgram);



		CompositionCohortDefinition patientEnrolledInCRDPAndSeenInSameQuarter=new CompositionCohortDefinition();
		patientEnrolledInCRDPAndSeenInSameQuarter.setName("patientEnrolledInCRDPAndSeenInSameQuarter");/*
		patientEnrolledInCRDPAndSeenInSameQuarter.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientEnrolledInCRDPAndSeenInSameQuarter.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));*/
		patientEnrolledInCRDPAndSeenInSameQuarter.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientEnrolledInCRDPAndSeenInSameQuarter.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientEnrolledInCRDPAndSeenInSameQuarter.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientEnrolledInCRDP, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore},enrolledOnOrAfter=${enrolledOnOrAfter}")));
		patientEnrolledInCRDPAndSeenInSameQuarter.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(patientEnrolledInCRDPByEndDate, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		/*patientEnrolledInCRDPAndSeenInSameQuarter.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));*/

		//patientEnrolledInCRDPAndSeenInSameQuarter.setCompositionString("(1 and (not 2)) and 3");
		patientEnrolledInCRDPAndSeenInSameQuarter.setCompositionString("1 and (not 2)");


		CohortIndicator patientEnrolledInCRDPQuarterIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDPAndSeenInSameQuarter,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));
		CohortIndicator patientEnrolledInCRDPMonthOneIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-1m+1d},enrolledOnOrBefore=${endDate}"));
		CohortIndicator patientEnrolledInCRDPMonthTwooIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-2m+1d},enrolledOnOrBefore=${endDate-1m+1d}"));
		CohortIndicator patientEnrolledInCRDPMonthThreeIndicator = Indicators.newCountIndicator(
		    "patientEnrolledInCRDPQuarterIndicator", patientEnrolledInCRDP,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${endDate-3m+1d},enrolledOnOrBefore=${endDate-2m+1d}"));

		// A3 Review March 2017 (it was A3Q but now it will be E1D/A3QReview )

		//InProgramCohortDefinition inAsthmaProgramByEndDate=Cohorts.createInProgramParameterizableByDate("In CRDP by EndDate",asthmaProgram);
		SqlCohortDefinition inAsthmaProgramByEndDate=new SqlCohortDefinition();
		inAsthmaProgramByEndDate.setName("inAsthmaProgramByEndDate");
		inAsthmaProgramByEndDate.addParameter(new Parameter("onDate", "onDate", Date.class));
		inAsthmaProgramByEndDate.setQuery("select patient_id from patient_program where program_id="+asthmaProgram.getProgramId()+" and voided=0 and date_enrolled<= :onDate");

		//Cohorts.createInProgramParameterizableByDate("In CRDP by EndDate",asthmaProgram);

		//ProgramEnrollmentCohortDefinition completedInAsthamProgramByEndDate=Cohorts.createProgramCompletedByEndDate("Completed CRDP by EndDate",asthmaProgram);

		SqlCohortDefinition completedInAsthamProgramByEndDate=new SqlCohortDefinition();
		completedInAsthamProgramByEndDate.setName("completedInAsthamProgramByEndDate");
		completedInAsthamProgramByEndDate.addParameter(new Parameter("completedOnOrBefore", "completedOnOrBefore", Date.class));
		completedInAsthamProgramByEndDate.setQuery("select patient_id from patient_program where program_id="+asthmaProgram.getProgramId()+" and voided=0 and date_completed<= :completedOnOrBefore and date_completed is not null");

		//Cohorts.createProgramCompletedByEndDate("Completed CRDP by EndDate",asthmaProgram);

		SqlCohortDefinition currentlyInProgramAndNotCompleted=new SqlCohortDefinition();
		currentlyInProgramAndNotCompleted.setName("currentlyInProgramAndNotCompleted");
		currentlyInProgramAndNotCompleted.setQuery("select patient_id from patient_program where voided=0 and program_id="+asthmaProgram.getProgramId()+" and (date_completed> :endDate or date_completed is null) and date_enrolled<= :endDate");
		currentlyInProgramAndNotCompleted.addParameter(new Parameter("endDate", "endDate", Date.class));


		CohortIndicator currentlyInProgramAndNotCompletedIndicator=Indicators.newCountIndicator(
				"currentlyInProgramAndNotCompletedIndicator", currentlyInProgramAndNotCompleted,
				ParameterizableUtil.createParameterMappings("endDate=${endDate}"));

		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn("A3Q", "Total # of new patients enrolled in the last quarter", new Mapped(
		        patientEnrolledInCRDPQuarterIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")),
		    "");
		dsd.addColumn("A3QM1", "Total # of new patients enrolled in the month one", new Mapped(
		        patientEnrolledInCRDPMonthOneIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		dsd.addColumn("A3QM2", "Total # of new patients enrolled in the month two", new Mapped(
		        patientEnrolledInCRDPMonthTwooIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		dsd.addColumn(
		    "A3QM3",
		    "Total # of new patients enrolled in the month three",
		    new Mapped(patientEnrolledInCRDPMonthThreeIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");

		dsd.addColumn(
				"A3QReview",
				"Proportion of active asthma patients in currently enrolled patients",
				new Mapped(currentlyInProgramAndNotCompletedIndicator, ParameterizableUtil
						.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


		//=======================================================================
		// A4: Total # of new patients with RDV in the last month/quarter
		//==================================================================
		
		CohortIndicator patientRDVQuarterIndicator = Indicators.newCountIndicator("patientRDVQuarterIndicator", patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientRDVMonthOneIndicator = Indicators.newCountIndicator("patientRDVMonthOneIndicator",
		    patientSeen, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-1m+1d},onOrBefore=${endDate}"));
		CohortIndicator patientRDVMonthTwooIndicator = Indicators.newCountIndicator("patientRDVMonthTwooIndicator",
		    patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-2m+1d},onOrBefore=${endDate-1m+1d}"));
		CohortIndicator patientRDVMonthThreeIndicator = Indicators.newCountIndicator("patientRDVMonthThreeIndicator",
		    patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate-2m+1d}"));


		// A4 Review March 2017 (it was A4Q but now it will be A4QReview )
		ProgramEnrollmentCohortDefinition everInAsthamProgramByEndDate=Cohorts.createProgramEnrollmentEverByEndDate("Ever in CRDP by EndDate", asthmaProgram);

		CohortIndicator everEnrolledPatientIndicator = Indicators.newCountIndicator("everEnrolledPatientIndicator",
				everInAsthamProgramByEndDate,
				ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));




		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn("A4Q", "Total # of new patients with RDV in the last quarter", new Mapped(patientRDVQuarterIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM1", "Total # of new patients with RDV in the month one", new Mapped(patientRDVMonthOneIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM2", "Total # of new patients with RDV in the month two", new Mapped(patientRDVMonthTwooIndicator,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
		dsd.addColumn("A4QM3", "Total # of new patients with RDV in the month three", new Mapped(
		        patientRDVMonthThreeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

		dsd.addColumn("A4QReview", "# of patients ever enrolled", new Mapped(
				everEnrolledPatientIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


		//=======================================================================
		//A5: % of deaths which are disease related
		//==================================================================
		//CodedObsCohortDefinition NCDRelatedDeath = Cohorts.createCodedObsCohortDefinition("NCDRelatedDeath",onOrAfterOnOrBefore,Context.getConceptService().getConcept(8372),Context.getConceptService().getConcept(8370), SetComparator.IN,TimeModifier.ANY);
		SqlCohortDefinition programOutcomeNCDRelatedDeath=Cohorts.getPatientsWithCodedObservationsBetweenStartDateAndEndDate("NCDRelatedDeath",NCDSpecificOutcomes,NCDRelatedDeathOutcomes);

		SqlCohortDefinition obsNCDRelatedDeath=Cohorts.getPatientsWithCodedObservationsBetweenStartDateAndEndDate("obsNCDRelatedDeath",NCDSpecificOutcomes,NCDRelatedDeathOutcomes);



		CompositionCohortDefinition NCDRelatedDeath= new CompositionCohortDefinition();
		NCDRelatedDeath.setName("NCDRelatedDeath");
		NCDRelatedDeath.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		NCDRelatedDeath.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		NCDRelatedDeath.getSearches().put("1",new Mapped<CohortDefinition>(programOutcomeNCDRelatedDeath, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		NCDRelatedDeath.getSearches().put("2",new Mapped<CohortDefinition>(obsNCDRelatedDeath, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		NCDRelatedDeath.setCompositionString("1 OR 2");



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
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

		activePatientsInPatientDiedStateOrNCDRelatedDeath.setCompositionString("1 AND 3");




		CohortIndicator activePatientsInPatientDiedStateOrNCDRelatedDeathIndicator = Indicators.newCountIndicator(
				"NCDRelatedDeathIndicator", activePatientsInPatientDiedStateOrNCDRelatedDeath,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn("A5QN", "% of deaths which are disease related", new Mapped(
				activePatientsInPatientDiedStateOrNCDRelatedDeathIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");




		CohortIndicator patientsSeenInYearIndicator = Indicators.newCountIndicator("patientsSeenIndicator", patientSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate-9m},onOrBefore=${endDate}"));

		dsd.addColumn(
				"ActiveY",
				"Total active in Year patients",
				new Mapped(patientsSeenInYearIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		// A6

		SqlCohortDefinition patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes=new SqlCohortDefinition();
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.setName("patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes");
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.setQuery("select patient_id from patient_program where program_id="+asthmaProgram.getProgramId()+" and date_completed>= :onOrAfter and date_completed<= :onOrBefore and voided=0 and outcome_concept_id not in ("+deathAndLostToFollowUpOutcomeString.toString()+")");
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));

		SqlCohortDefinition patientWhoCompletedProgram=new SqlCohortDefinition();
		patientWhoCompletedProgram.setName("patientWhoCompletedProgram");
		patientWhoCompletedProgram.setQuery("select patient_id from patient_program where program_id="+asthmaProgram.getProgramId()+" and date_completed>= :onOrAfter and date_completed<= :onOrBefore and voided=0");
		patientWhoCompletedProgram.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWhoCompletedProgram.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));




		CohortIndicator patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator = Indicators.newCountIndicator("patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator", patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn(
				"A6N",
				"patient Who Completed Program Without Death And Lost To Followup Outcomes",
				new Mapped(patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		CohortIndicator patientWhoCompletedProgramIndicator = Indicators.newCountIndicator("patientWhoCompletedProgramIndicator", patientWhoCompletedProgram,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn(
				"A6D",
				"patient Who Completed Program ",
				new Mapped(patientWhoCompletedProgramIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");




		//=======================================================================
		//B1: Pediatric:  Of the new patients enrolled in the last quarter, % ≤15 years old at intake
		//  Review: % of all active patients that are age < 16 years old
		//==================================================================
		
		SqlCohortDefinition patientsUnderSixteenAtEnrollementDate = Cohorts.createUnder16AtEnrollmentCohort(
				"patientsUnderSixteenAtEnrollementDate", asthmaProgram);
		
		CompositionCohortDefinition patientsUnderFifteenComposition = new CompositionCohortDefinition();
		patientsUnderFifteenComposition.setName("patientsUnderFifteenComposition");
		patientsUnderFifteenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsUnderFifteenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));

		patientsUnderFifteenComposition.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		patientsUnderFifteenComposition.getSearches().put("2",
		    new Mapped<CohortDefinition>(patientsUnderSixteenAtEnrollementDate, null));
		patientsUnderFifteenComposition.setCompositionString("1 AND 2");
		
		CohortIndicator patientsUnderFifteenCountIndicator = Indicators.newCountIndicator(
		    "patientsUnderFifteenCountIndicator", patientsUnderFifteenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn(
		    "B1N",
		    "Pediatric: Of the new patients enrolled in the last quarter, number < 16 years old at intake",
		    new Mapped(patientsUnderFifteenCountIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate}")),
		    "");
		
		//====================================================================
		//B2: Gender: Of the new patients enrolled in the last quarter, % male
		// Review: % of all active patients that are Male
		//====================================================================
		
		GenderCohortDefinition malePatients = Cohorts.createMaleCohortDefinition("Male patients");
		
		CompositionCohortDefinition malePatientsEnrolledInCRDP = new CompositionCohortDefinition();
		malePatientsEnrolledInCRDP.setName("malePatientsEnrolledIn");
		malePatientsEnrolledInCRDP.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		malePatientsEnrolledInCRDP.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		malePatientsEnrolledInCRDP.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
					.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		malePatientsEnrolledInCRDP.getSearches().put("2", new Mapped<CohortDefinition>(malePatients, null));
		malePatientsEnrolledInCRDP.setCompositionString("1 AND 2");
		
		CohortIndicator malePatientsEnrolledInCRDPCountIndicator = Indicators.newCountIndicator(
		    "malePatientsEnrolledInDMCountIndicator", malePatientsEnrolledInCRDP,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//=================================================
		//     Adding columns to data set definition     //
		//=================================================
		
		dsd.addColumn(
		    "B2N",
		    "Gender: Of the new patients enrolled in the last quarter, number male",
		    new Mapped(malePatientsEnrolledInCRDPCountIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		//=======================================================
		// B3 removed: Of the new patients enrolled in the last month, % with documented peak flow taken both before and after salbutamol at intake
		// removed from quarterly excel sheet
		//=======================================================

		ProgramEnrollmentCohortDefinition enrolledInAsthmaProgram = Cohorts
		        .createProgramEnrollmentParameterizedByStartEndDate("enrolledInAthmaProgram", asthmaProgram);

		SqlCohortDefinition patientsWithBothPeakFlowInSameDDBForm = Cohorts
		        .getPatientsWithTwoObservationsBothInFormBetweenStartAndEndDate("patientsWithBothPeakFlowInSameDDBForm",
						DDBandEnrollmentforms, peakFlowAfterSalbutamol, peakFlowBeforeSalbutamol);


		CompositionCohortDefinition patientsWithBothPeakFlowInSameDDBFormComposition = new CompositionCohortDefinition();
		patientsWithBothPeakFlowInSameDDBFormComposition.setName("patientsWithBothPeakFlowInSameDDBFormComposition");
		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsWithBothPeakFlowInSameDDBFormComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientEnrolledInCRDP, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore},enrolledOnOrAfter=${enrolledOnOrAfter}")));
		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(patientEnrolledInCRDPByEndDate, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		patientsWithBothPeakFlowInSameDDBFormComposition.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientsWithBothPeakFlowInSameDDBForm,
						ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

		patientsWithBothPeakFlowInSameDDBFormComposition.setCompositionString("(1 and (not 2)) and 3");


		CohortIndicator enrolledInAsthmaProgramIndicator = Indicators.newCountIndicator("enrolledInAthmaProgramIndicator",
		    enrolledInAsthmaProgram,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsWithBothPeakFlowInSameDDBFormIndicator = Indicators.newCountIndicator(
		    "patientsWithBothPeakFlowInSameDDBFormIndicator", patientsWithBothPeakFlowInSameDDBFormComposition,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

		//=================================================
		//     Adding columns to data set definition     //
		//=================================================

		dsd.addColumn(
		    "B3Nremoved",
		    "patients with documented peak flow taken both before and after salbutamol at intake",
		    new Mapped(patientsWithBothPeakFlowInSameDDBFormIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		dsd.addColumn("B3D", "new patients enrolled in report period", new Mapped(enrolledInAsthmaProgramIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		//===============================================================================================
		// B5: Of the new patients enrolled in the last quarter, % with smoking status documented at intake
		// Removed from excel sheet
		//===============================================================================================

		SqlCohortDefinition patientsWithSmokingHistory = Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate(
		    "patientsWithSmokingHistory", DDBandEnrollmentforms, smokingHistory);



		CompositionCohortDefinition patientsWithSmokingHistoryComposition = new CompositionCohortDefinition();
		patientsWithSmokingHistoryComposition.setName("patientsWithBothPeakFlowInSameDDBFormComposition");
		patientsWithSmokingHistoryComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithSmokingHistoryComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithSmokingHistoryComposition.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(enrolledInAsthmaProgram,
		    		ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}")));
		patientsWithSmokingHistoryComposition.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithSmokingHistory,
				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		patientsWithSmokingHistoryComposition.setCompositionString("1 AND 2");

		CohortIndicator patientsWithSmokingHistoryIndicator = Indicators.newCountIndicator(
		    "patientsWithSmokingHistoryIndicator", patientsWithSmokingHistoryComposition,
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));

		//=================================================
		//     Adding columns to data set definition     //
		//=================================================

		dsd.addColumn(
		    "B5Nremoved",
		    "patients with smoking status documented at intake",
		    new Mapped(patientsWithSmokingHistoryIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		//===============================================================================================
		// B3: % of all active patients that have HIV status documented
		//
		//===============================================================================================

		SqlCohortDefinition patientsWithHIVStatusDocumented= Cohorts.getPatientsWithObsEver(
				"patientsWithHIVStatusDocumented", HIVStatus);

		CompositionCohortDefinition activePatientsWithHIVStatusDocumented = new CompositionCohortDefinition();
		activePatientsWithHIVStatusDocumented.setName("activePatientsWithHIVStatusDocumented");
		activePatientsWithHIVStatusDocumented.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientsWithHIVStatusDocumented.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientsWithHIVStatusDocumented.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientSeen,
						ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));
		activePatientsWithHIVStatusDocumented.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithHIVStatusDocumented,null));
		activePatientsWithHIVStatusDocumented.setCompositionString("1 AND 2");

		CohortIndicator activePatientsWithHIVStatusDocumentedIndicator = Indicators.newCountIndicator(
				"activePatientsWithHIVStatusDocumentedIndicator", activePatientsWithHIVStatusDocumented,
				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate}"));

		//=================================================
		//     Adding columns to data set definition     //
		//=================================================

		dsd.addColumn(
				"B3N",
				"patients with HIV status documented",
				new Mapped(activePatientsWithHIVStatusDocumentedIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		//=======================================================
		// B5New: Of the new patients enrolled in the last quarter, % qualitatively screened for TB
		//=======================================================

		SqlCohortDefinition patientsWithObsGreaterThan3timesByStartDateEndDate = Cohorts
				.getPatientsWithObsGreaterThanNtimesByStartDateEndDate("patientsWithObsGreaterThanNtimesByStartDateEndDate",
						TBScreening, 3);

		SqlCohortDefinition patientsWithObsGreaterThan1timeByStartDateEndDate = Cohorts
				.getPatientsWithObsGreaterThanNtimesByStartDateEndDate("patientsWithObsGreaterThan1timeByStartDateEndDate",
						TBScreening, 1);


		CompositionCohortDefinition patientsWithObsGreaterThan3timesByStartDateEndDateComposition = new CompositionCohortDefinition();
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.setName("patientsWithObsGreaterThan3timesByStartDateEndDateComposition");
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientEnrolledInCRDP, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore},enrolledOnOrAfter=${enrolledOnOrAfter}")));
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(patientEnrolledInCRDPByEndDate, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientsWithObsGreaterThan3timesByStartDateEndDate,
						ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

		patientsWithObsGreaterThan3timesByStartDateEndDateComposition.setCompositionString("(1 and (not 2)) and 3");





		CompositionCohortDefinition patientsWithObsGreaterThan1timeByStartDateEndDateComposition = new CompositionCohortDefinition();
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.setName("patientsWithObsGreaterThan1timeByStartDateEndDateComposition");
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));

		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientEnrolledInCRDP, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore},enrolledOnOrAfter=${enrolledOnOrAfter}")));
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(patientEnrolledInCRDPByEndDate, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientsWithObsGreaterThan1timeByStartDateEndDate,
						ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

		patientsWithObsGreaterThan1timeByStartDateEndDateComposition.setCompositionString("(1 and (not 2)) and 3");





		CohortIndicator patientsWithObsGreaterThan3timesByStartDateEndDateCompositionIndicator = Indicators.newCountIndicator("patientsWithObsGreaterThan3timesByStartDateEndDateComposition",
				patientsWithObsGreaterThan3timesByStartDateEndDateComposition,
				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

		CohortIndicator patientsWithObsGreaterThan1timeByStartDateEndDateCompositionIndicator = Indicators.newCountIndicator(
				"patientsWithBothPeakFlowInSameDDBFormIndicator", patientsWithObsGreaterThan1timeByStartDateEndDateComposition,
				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

		//=================================================
		//     Adding columns to data set definition     //
		//=================================================

		dsd.addColumn(
				"B5NNew",
				"patients with documented peak flow taken both before and after salbutamol at intake",
				new Mapped(patientsWithObsGreaterThan3timesByStartDateEndDateCompositionIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		dsd.addColumn("B5DNew", "new patients enrolled in report period", new Mapped(patientsWithObsGreaterThan1timeByStartDateEndDateCompositionIndicator,
				ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		//=======================================================
		// D2: Of total patients with a visit in the last quarter, % on Salbutamol alone at last visit
		// C1 Review: Of total active patients seen in the last quarter, % on Salbutamol at last visit
		//=======================================================
		SqlCohortDefinition patientsWithCurrentSalbutamolDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
		    "patientsWithCurrentSalbutamolDrugOrder", salbutamol);
		
		/*SqlCohortDefinition patientsWithAnyOtherCurrentAsthmaDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
		    "patientsWithAnyOtherCurrentAsthmaDrugOrder", asthmasMedicationsWithoutSalbutamol);
		*/
		CompositionCohortDefinition patientsSeenOnSalbutamol = new CompositionCohortDefinition();
		patientsSeenOnSalbutamol.setName("patientsSeenOnSalbutamol");
		patientsSeenOnSalbutamol.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenOnSalbutamol.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsSeenOnSalbutamol.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenOnSalbutamol.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

		patientsSeenOnSalbutamol.addSearch("1", patientSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		patientsSeenOnSalbutamol.addSearch("2", patientsWithCurrentSalbutamolDrugOrder, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		//patientsSeenOnSalbutamol.addSearch("3", patientsWithAnyOtherCurrentAsthmaDrugOrder, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		//patientsSeenOnSalbutamol.setCompositionString("1 AND 2 AND (NOT 3)");
		patientsSeenOnSalbutamol.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenOnSalbutamolIndicator = Indicators.newCountIndicator(
		    "patientsOnSalbutamolAloneIndicator", patientsSeenOnSalbutamol,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));

		CohortIndicator patientsActiveInQuarterIndicator = Indicators.newCountIndicator(
				"patientsActiveInQuarterIndicator", patientSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		dsd.addColumn(
		    "C1N",
		    "patients active in the last quarter, % on Salbutamol",
		    new Mapped(patientsSeenOnSalbutamolIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		dsd.addColumn(
				"ActiveInQ",
				"patients active in a quarter",
				new Mapped(patientsActiveInQuarterIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		//=======================================================
		// D3: Of total patients with a visit in the last quarter on Salbutamol, % also on Beclomethasone
		//C2 Review: Of total active patients seen in the last quarter, % on Beclomethasone at last visit
		//=======================================================
		SqlCohortDefinition patientsWithCurrentBeclomethasoneDrugOrder = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
		    "patientsWithCurrentBeclomethasoneDrugOrder", beclomethasone);
		
		CompositionCohortDefinition patientsSeenOnBeclomethasone = new CompositionCohortDefinition();
		patientsSeenOnBeclomethasone.setName("patientsOnBeclomethasone");
		patientsSeenOnBeclomethasone.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsSeenOnBeclomethasone.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenOnBeclomethasone.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenOnBeclomethasone.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenOnBeclomethasone.addSearch("1", patientSeen,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		patientsSeenOnBeclomethasone.addSearch("2",  patientsWithCurrentBeclomethasoneDrugOrder,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		//patientsSeenOnBeclomethasone.addSearch("3",patientsWithCurrentSalbutamolDrugOrder, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		//patientsSeenOnBeclomethasone.setCompositionString("1 AND 2 AND 3");
		patientsSeenOnBeclomethasone.setCompositionString("1 AND 2");

		CohortIndicator patientsSeenOnBeclomethasoneIndicator = Indicators.newCountIndicator(
		    "patientsSeenOnBeclomethasoneIndicator", patientsSeenOnBeclomethasone,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		dsd.addColumn(
		    "C2N",
		    "patients active in the last quarte, % on Beclomethasone",
		    new Mapped(patientsSeenOnBeclomethasoneIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		//=======================================================
		// D4: Of total patients with a visit in the last quarter, % prescribed oral Prednisolone in the last quarter
		// C3 Review: Of total active patients seen in the last quarter, % ever prescribed oral Prednisolone in the last quarter
		//=======================================================
		SqlCohortDefinition patientsPrescribedOralPrednisoloneInTheLastQuarter = Cohorts
		        .getPatientsOnCurrentRegimenBasedOnStartDateEndDate("patientsPrescribedOralPrednisoloner", prednisolone);
		
		CompositionCohortDefinition patientsSeenPrescribedOralPrednisolone = new CompositionCohortDefinition();
		patientsSeenPrescribedOralPrednisolone.setName("patientsOnSalbutamolAndBeclomethasone");
		patientsSeenPrescribedOralPrednisolone.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientsSeenPrescribedOralPrednisolone.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientsSeenPrescribedOralPrednisolone.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientsSeenPrescribedOralPrednisolone.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientsSeenPrescribedOralPrednisolone.addSearch("1", patientSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		patientsSeenPrescribedOralPrednisolone.addSearch("2", patientsPrescribedOralPrednisoloneInTheLastQuarter, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
		patientsSeenPrescribedOralPrednisolone.setCompositionString("1 AND 2");
		
		CohortIndicator patientsSeenPrescribedOralPrednisoloneIndicator = Indicators.newCountIndicator(
		    "patientsSeenPrescribedOralPrednisoloneIndicator", patientsSeenPrescribedOralPrednisolone,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		dsd.addColumn(
		    "C3N",
		    "patients active in the last quarter, % prescribed oral Prednisolone in the last quarter",
		    new Mapped(patientsSeenPrescribedOralPrednisoloneIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		//=======================================================================
		//E1Died: Of total active patients, % of patients died.
		//==================================================================

		SqlCohortDefinition patientHospitalized =Cohorts.getPatientsWithCodedObservationsBetweenStartDateAndEndDate("patientHospitalized",diagnosisWhileHospitalized,asthmaExacerbation);
		
		CompositionCohortDefinition activeAndHospitalizedPatients = new CompositionCohortDefinition();
		activeAndHospitalizedPatients.setName("activeAndHospitalizedPatients");
		activeAndHospitalizedPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeAndHospitalizedPatients.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeAndHospitalizedPatients.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m+1d}")));
		activeAndHospitalizedPatients.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientHospitalized, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activeAndHospitalizedPatients.setCompositionString("1 AND 2");
		
		CohortIndicator activeAndHospitalizedPatientsCountQuarterIndicator = Indicators
		        .newCountIndicator(
		            "activeAndHospitalizedPatientsCountQuarterIndicator",
		            activeAndHospitalizedPatients,
		            ParameterizableUtil
		                    .createParameterMappings("endDate=${endDate},startDate=${startDate},onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		CohortIndicator patientsSeenInOneYearCountIndicator = Indicators.newCountIndicator(
		    "patientsSeenInOneYearCountIndicator", patientsSeenComposition,
		    ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		
		dsd.addColumn(
		    "D1N",
		    "Total active patients, number with documented hospitalization (in flowsheet) in the last quarter (exclude hospitalization on DDB)",
		    new Mapped(activeAndHospitalizedPatientsCountQuarterIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");
		
		dsd.addColumn("E1D", "total patients seen in the last year", new Mapped(patientsSeenInOneYearCountIndicator,
		        ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		//========================================================================================
		//  Active patients with no exit in Reporting periode
		//=======================================================================================





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





		//=======================================================================
		//E2: Of total patients with a visit in the last 12 months, % with no visit  in 28  or more weeks
		// D2 Review: Of total active patients, % with no visit 6 months & <12 months past last visit date
		//==================================================================		
		
		/*EncounterCohortDefinition withAsthmaVisit = Cohorts.createEncounterParameterizedByDate("withAsthmaVisit",
		    onOrAfterOnOrBefore, asthmaEncounterType);
		*/
		CompositionCohortDefinition activeAndNotSeenIn6MonthsPatients = new CompositionCohortDefinition();
		activeAndNotSeenIn6MonthsPatients.setName("activeAndNotSeenIn6MonthsPatients");
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("endDate", "endDate", Date.class));

		activeAndNotSeenIn6MonthsPatients.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m}")));
		activeAndNotSeenIn6MonthsPatients.getSearches().put("2",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}")));

		activeAndNotSeenIn6MonthsPatients.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-3m}")));
		activeAndNotSeenIn6MonthsPatients.setCompositionString("1 AND 2 AND (NOT 3)");

		CohortIndicator activeAndNotSeenIn6MonthsPatientsIndicator = Indicators.newCountIndicator(
				"activeAndNotSeenIn6MonthsPatientsIndicator",
				activeAndNotSeenIn6MonthsPatients,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));

		dsd.addColumn("D2N", "Of total active patients, % with no visit 6 months & <12 months past last visit date", new Mapped(
				activeAndNotSeenIn6MonthsPatientsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

		//=======================================================================
		//E3: Of total active patients with ‘severe persistent’ or ‘severe uncontrolled’ asthma classification at last visit, % with next scheduled RDV visit 14 weeks or more past last visit date 
		//	D3 Review: Active asthma patients in the last quarter , disaggregated by level of severity
		// =======================================================================
		SqlCohortDefinition patientsWithAsthmaClassificationObsAnswer = Cohorts
        .getPatientsWithObservationInFormBetweenStartAndEndDate("patientsWithAsthmaClassificationObsAnswer",
            DDBforms, asthmaclassification, asthmasClassificationAnswers);

		SqlCohortDefinition patientWithSeverePersistentAsthma =Cohorts.getPatientsWithLastCodedObsEver("patientWithSeverePersistentAsthma", asthmaclassification, severePersistentAsthma);

		SqlCohortDefinition patientWithSevereUncontrolledAsthma =Cohorts.getPatientsWithLastCodedObsEver("patientWithSevereUncontrolledAsthma", asthmaclassification, severeUncontrolledAsthma);


		SqlCohortDefinition patientWithintermittentasthma =Cohorts.getPatientsWithLastCodedObsEver("patientWithintermittentasthma", asthmaclassification, intermittentasthma);


		SqlCohortDefinition patientWithmoderatePersistentAsthma =Cohorts.getPatientsWithLastCodedObsEver("moderatePersistentAsthma", asthmaclassification, moderatePersistentAsthma);


		SqlCohortDefinition patientWithmildPersistentAsthma =Cohorts.getPatientsWithLastCodedObsEver("mildPersistentAsthma", asthmaclassification, mildPersistentAsthma);


		CompositionCohortDefinition activeAndWithAsthmaSevereClassificationObsAnswer = new CompositionCohortDefinition();
		activeAndWithAsthmaSevereClassificationObsAnswer.setName("activeAndWithAsthmaSevereClassificationObsAnswer");
		activeAndWithAsthmaSevereClassificationObsAnswer.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndWithAsthmaSevereClassificationObsAnswer.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
		activeAndWithAsthmaSevereClassificationObsAnswer.getSearches().put("1",new Mapped<CohortDefinition>(patientWithSeverePersistentAsthma, null));
		activeAndWithAsthmaSevereClassificationObsAnswer.getSearches().put("2",new Mapped<CohortDefinition>(patientWithSevereUncontrolledAsthma, null));
		activeAndWithAsthmaSevereClassificationObsAnswer.getSearches().put("3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m-1d}")));
		activeAndWithAsthmaSevereClassificationObsAnswer.setCompositionString("(1 OR 2) and 3");


		CompositionCohortDefinition activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer = new CompositionCohortDefinition();
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.setName("activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer");
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.getSearches().put("1",new Mapped<CohortDefinition>(patientWithmoderatePersistentAsthma,null));
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.getSearches().put("2",new Mapped<CohortDefinition>(patientWithmildPersistentAsthma, null));
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.getSearches().put("3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m-1d}")));
		activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer.setCompositionString("(1 OR 2) and 3");

		CompositionCohortDefinition activeAndWithAsthmaIntermittentClassificationObsAnswer = new CompositionCohortDefinition();
		activeAndWithAsthmaIntermittentClassificationObsAnswer.setName("activeAndWithAsthmaIntermittentClassificationObsAnswer");
		activeAndWithAsthmaIntermittentClassificationObsAnswer.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndWithAsthmaIntermittentClassificationObsAnswer.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
		activeAndWithAsthmaIntermittentClassificationObsAnswer.getSearches().put("1",new Mapped<CohortDefinition>(patientWithintermittentasthma,null));
		activeAndWithAsthmaIntermittentClassificationObsAnswer.getSearches().put("2",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m-1d}")));
		activeAndWithAsthmaIntermittentClassificationObsAnswer.setCompositionString("1 and 2");


		CohortIndicator activeAndWithAsthmaIntermittentClassificationObsAnswerIndicator = Indicators
				.newCountIndicator("activeAndWithAsthmaIntermittentClassificationObsAnswerIndicator",
						activeAndWithAsthmaIntermittentClassificationObsAnswer,
						ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		CohortIndicator activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswerIndicator = Indicators
				.newCountIndicator("activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswerIndicator",
						activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswer,
						ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		CohortIndicator activeAndWithAsthmaSevereClassificationObsAnswerIndicator = Indicators
        .newCountIndicator("activeAndWithAsthmaSevereClassificationObsAnswerIndicator",
				activeAndWithAsthmaSevereClassificationObsAnswer,
            ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================	
		dsd.addColumn(
		    "D31",
		    "Total active patients, number with intermittent asthma classification",
		    new Mapped(activeAndWithAsthmaIntermittentClassificationObsAnswerIndicator, ParameterizableUtil
		            .createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		dsd.addColumn(
				"D32",
				"Total active patients, number with Mild and Modarate persistent asthma classification",
				new Mapped(activeAndWithAsthmaModerateAndMildPersistentClassificationObsAnswerIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

		dsd.addColumn(
				"D33",
				"Total active patients, number with ‘severe persistent’ or ‘severe uncontrolled’ asthma classification",
				new Mapped(activeAndWithAsthmaSevereClassificationObsAnswerIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		//=============================================
		//
		//=============================================

		SqlCohortDefinition patientWithAsthmaClassificationInLastFormSubmitted =Cohorts.getPatientsWithObsinLastFormSubmitted("mildPersistentAsthma", asthmaclassification, asthmaVisitForms);

		CompositionCohortDefinition activePatientWithAsthmaClassificationInLastFormSubmitted = new CompositionCohortDefinition();
		activePatientWithAsthmaClassificationInLastFormSubmitted.setName("activePatientWithAsthmaClassificationInLastFormSubmitted");
		activePatientWithAsthmaClassificationInLastFormSubmitted.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithAsthmaClassificationInLastFormSubmitted.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
		activePatientWithAsthmaClassificationInLastFormSubmitted.getSearches().put("1",new Mapped<CohortDefinition>(patientWithAsthmaClassificationInLastFormSubmitted,null));
		activePatientWithAsthmaClassificationInLastFormSubmitted.getSearches().put("2",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m-1d}")));
		activePatientWithAsthmaClassificationInLastFormSubmitted.setCompositionString("2 and (not 1)");


		CohortIndicator activePatientWithAsthmaClassificationInLastFormSubmittedIndicator = Indicators
				.newCountIndicator("activePatientWithAsthmaClassificationInLastFormSubmittedIndicator",
						activePatientWithAsthmaClassificationInLastFormSubmitted,
						ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));


		dsd.addColumn(
				"D4N",
				"Total active without classification documented",
				new Mapped(activePatientWithAsthmaClassificationInLastFormSubmittedIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		//===============================================================================
		// D5: Of patients currently enrolled, % Lost to follow up
		//===========================================================================


		CompositionCohortDefinition currentlyInProgramAndNotCompletedAndNotSeen= new CompositionCohortDefinition();
		currentlyInProgramAndNotCompletedAndNotSeen.setName("currentlyInProgramAndNotCompletedAndNotSeen");
		currentlyInProgramAndNotCompletedAndNotSeen.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentlyInProgramAndNotCompletedAndNotSeen.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		currentlyInProgramAndNotCompletedAndNotSeen.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

		currentlyInProgramAndNotCompletedAndNotSeen.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(currentlyInProgramAndNotCompleted, ParameterizableUtil
						.createParameterMappings("endDate=${endDate}")));
		currentlyInProgramAndNotCompletedAndNotSeen.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}")));

		currentlyInProgramAndNotCompletedAndNotSeen.setCompositionString("1 and (not 2)");

		CohortIndicator currentlyInProgramAndNotCompletedAndNotSeenIndicator=Indicators.newCountIndicator(
				"currentlyInProgramAndNotCompletedAndNotSeenIndicator", currentlyInProgramAndNotCompletedAndNotSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate},endDate=${endDate}"));


		dsd.addColumn(
				"D5N",
				"Of patients currently enrolled, % Lost to follow up",
				new Mapped(currentlyInProgramAndNotCompletedAndNotSeenIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		//==============================================================================

		//===============
		
		SqlCohortDefinition patientswithRDV14WeeksOrMorePastLastVisitDate  = new SqlCohortDefinition();
		patientswithRDV14WeeksOrMorePastLastVisitDate.setQuery("select last_rdv.person_id from (select person_id, value_datetime, obs_datetime, datediff(value_datetime,obs_datetime) from obs o, encounter e where o.encounter_id=e.encounter_id and e.encounter_type="
				+ asthmaEncounterType.getId()
				+ " and o.concept_id="
				+ returnVisitDate.getConceptId()
				+ " and datediff(o.value_datetime, o.obs_datetime) >= 98 order by o.value_datetime desc) as last_rdv group by last_rdv.person_id");
		patientswithRDV14WeeksOrMorePastLastVisitDate.setName("patientswithRDV14WeeksOrMorePastLastVisitDate");
		
		CompositionCohortDefinition activeAndNotwithAsthmaVisit14WeeksPatients = new CompositionCohortDefinition();
		activeAndNotwithAsthmaVisit14WeeksPatients.setName("activeAndNotwithAsthmaVisit14WeeksPatients");
		activeAndNotwithAsthmaVisit14WeeksPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndNotwithAsthmaVisit14WeeksPatients.addParameter(new Parameter("onOrBefore", "onOrBefore",Date.class));
		activeAndNotwithAsthmaVisit14WeeksPatients.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(patientsWithAsthmaClassificationObsAnswer, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		activeAndNotwithAsthmaVisit14WeeksPatients.getSearches().put(
		    "2",
		    new Mapped<CohortDefinition>(patientsSeenComposition, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		
		
		activeAndNotwithAsthmaVisit14WeeksPatients.getSearches().put(
		    "3",
		    new Mapped<CohortDefinition>(patientswithRDV14WeeksOrMorePastLastVisitDate, null));
		activeAndNotwithAsthmaVisit14WeeksPatients.setCompositionString("1 AND 2 AND 3");
		
		CohortIndicator activeAndNotwithAsthmaVisit14WeeksPatientsIndicator = Indicators
		        .newCountIndicator("activeAndNotwithAsthmaVisit14WeeksPatients",
		        	activeAndNotwithAsthmaVisit14WeeksPatients,
		            ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================	
		dsd.addColumn(
		    "E3N",
		    "Total active patients, number with no visit 14 weeks or more past last visit date",
		    new Mapped(activeAndNotwithAsthmaVisit14WeeksPatientsIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		//=======================================================
		// E4: Of adult male patients (age ≥15 years old) who had peak flow tested in the last quarter, % with last peak flow >580
		//=======================================================
		
		AgeCohortDefinition over15Cohort = Cohorts.createOver15AgeCohort("ageQD: Over 15");
		
		GenderCohortDefinition malesDefinition = Cohorts.createMaleCohortDefinition("malesDefinition");
		
		NumericObsCohortDefinition patientsTestedForpeakFlow = Cohorts.createNumericObsCohortDefinition(
		    "patientsTestedForpeakFlow", onOrAfterOnOrBefore, peakFlowAfterSalbutamol, 0, null, TimeModifier.LAST);
		
		CompositionCohortDefinition adultMalePatientsTestedForpeakFlow = new CompositionCohortDefinition();
		adultMalePatientsTestedForpeakFlow.setName("adultMalePatientsTestedForForpeakFlow");
		adultMalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		adultMalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		adultMalePatientsTestedForpeakFlow.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		adultMalePatientsTestedForpeakFlow.getSearches().put("1",new Mapped<CohortDefinition>(patientsTestedForpeakFlow, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		
		adultMalePatientsTestedForpeakFlow.getSearches().put("2", new Mapped<CohortDefinition>(malesDefinition, null));
		
		adultMalePatientsTestedForpeakFlow.getSearches().put("3",new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
		
		adultMalePatientsTestedForpeakFlow.setCompositionString("1 AND 2 AND 3");
		
		CohortIndicator adultMalePatientsTestedForpeakFlowIndicator = Indicators
		        .newCountIndicator("adultMalePatientsTestedForpeakFlowIndicator",
		        	adultMalePatientsTestedForpeakFlow,
		            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
		
		
		NumericObsCohortDefinition patientsWithLastPeakflowGreaterThan580 = Cohorts.createNumericObsCohortDefinition(
		    "patientsWithLastPeakflowGreaterThan580", peakFlowAfterSalbutamol, 580, RangeComparator.GREATER_THAN,
		    TimeModifier.LAST);
		
		CompositionCohortDefinition adultMalePatientsTestedForpeakFlowGreaterThan580 = new CompositionCohortDefinition();
		adultMalePatientsTestedForpeakFlowGreaterThan580.setName("adultMalePatientsTestedForpeakFlowGreaterThan580");
		adultMalePatientsTestedForpeakFlowGreaterThan580.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		adultMalePatientsTestedForpeakFlowGreaterThan580.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		adultMalePatientsTestedForpeakFlowGreaterThan580.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		adultMalePatientsTestedForpeakFlowGreaterThan580.getSearches().put(
		    "1",
		    new Mapped<CohortDefinition>(adultMalePatientsTestedForpeakFlow, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},effectiveDate=${effectiveDate}")));
		adultMalePatientsTestedForpeakFlowGreaterThan580.getSearches().put("2",
		    new Mapped<CohortDefinition>(patientsWithLastPeakflowGreaterThan580, null));
		adultMalePatientsTestedForpeakFlowGreaterThan580.setCompositionString("1 AND 2");
		
		CohortIndicator adultMalePatientsTestedForpeakFlowGreaterThan580Indicator = Indicators
        .newCountIndicator("adultMalePatientsTestedForpeakFlowGreaterThan580Indicator",
        	adultMalePatientsTestedForpeakFlowGreaterThan580,
            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		dsd.addColumn(
		    "E4D",
		    "Of adult male patients (age ≥15 years old) who had peak flow tested in the last quarter",
		    new Mapped(adultMalePatientsTestedForpeakFlowIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "E4N",
		    "Of adult male patients (age ≥15 years old) who had peak flow Greater Than 580 tested in the last quarter",
		    new Mapped(adultMalePatientsTestedForpeakFlowGreaterThan580Indicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		//=======================================================
		// E5: Of adult female patients (age ≥15 years old) who had peak flow tested in the last quarter, % with last peak flow >400
		//=======================================================
		
		GenderCohortDefinition femalesDefinition = Cohorts.createFemaleCohortDefinition("femalesDefinition");
		
		CompositionCohortDefinition adultFemalePatientsTestedForpeakFlow = new CompositionCohortDefinition();
		adultFemalePatientsTestedForpeakFlow.setName("adultFemalePatientsTestedForpeakFlow");
		adultFemalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		adultFemalePatientsTestedForpeakFlow.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		adultFemalePatientsTestedForpeakFlow.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		
		adultFemalePatientsTestedForpeakFlow.getSearches().put("1",new Mapped<CohortDefinition>(patientsTestedForpeakFlow, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		
		adultFemalePatientsTestedForpeakFlow.getSearches().put("2", new Mapped<CohortDefinition>(femalesDefinition, null));
		
		adultFemalePatientsTestedForpeakFlow.getSearches().put("3",new Mapped(over15Cohort, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
		
		adultFemalePatientsTestedForpeakFlow.setCompositionString("1 AND 2 AND 3");
		
		CohortIndicator adultFemalePatientsTestedForpeakFlowIndicator = Indicators
		        .newCountIndicator("adultFemalePatientsTestedForpeakFlowIndicator",
		        	adultFemalePatientsTestedForpeakFlow,
		            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
		
		
		NumericObsCohortDefinition patientsWithLastPeakflowGreaterThan400 = Cohorts.createNumericObsCohortDefinition(
		    "patientsWithLastPeakflowGreaterThan400", peakFlowAfterSalbutamol, 400, RangeComparator.GREATER_THAN,
		    TimeModifier.LAST);
		
		CompositionCohortDefinition adultFemalePatientsTestedForpeakFlowGreaterThan400 = new CompositionCohortDefinition();
		adultFemalePatientsTestedForpeakFlowGreaterThan400.setName("adultFemalePatientsTestedForpeakFlowGreaterThan400");
		adultFemalePatientsTestedForpeakFlowGreaterThan400.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		adultFemalePatientsTestedForpeakFlowGreaterThan400.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		adultFemalePatientsTestedForpeakFlowGreaterThan400.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		adultFemalePatientsTestedForpeakFlowGreaterThan400.getSearches().put("1",new Mapped<CohortDefinition>(adultFemalePatientsTestedForpeakFlow, ParameterizableUtil
		            .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},effectiveDate=${effectiveDate}")));
		
		adultFemalePatientsTestedForpeakFlowGreaterThan400.getSearches().put("2",
		    new Mapped<CohortDefinition>(patientsWithLastPeakflowGreaterThan400, null));
		adultFemalePatientsTestedForpeakFlowGreaterThan400.setCompositionString("1 AND 2");
		
		CohortIndicator adultFemalePatientsTestedForpeakFlowGreaterThan400Indicator = Indicators
        .newCountIndicator("adultFemalePatientsTestedForpeakFlowGreaterThan400Indicator",
        	adultFemalePatientsTestedForpeakFlowGreaterThan400,
            ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${endDate-3m+1d},effectiveDate=${endDate}"));
		
		//========================================================
		//        Adding columns to data set definition         //
		//========================================================
		dsd.addColumn(
		    "E5D",
		    "Of adult female patients (age ≥15 years old) who had peak flow tested in the last quarter",
		    new Mapped(adultFemalePatientsTestedForpeakFlowIndicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");
		
		dsd.addColumn(
		    "E5N",
		    "Of adult female patients (age ≥15 years old) who had peak flow Greater Than 400 tested in the last quarter",
		    new Mapped(adultFemalePatientsTestedForpeakFlowGreaterThan400Indicator, ParameterizableUtil
		            .createParameterMappings("endDate=${endDate}")), "");


	}
	

	
	private void setUpProperties() {
		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
		asthmaPrograms.add(asthmaProgram);
		asthmaEncounterType = gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
		DDBform = gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB);
		Enrollmentform=gp.getForm(GlobalPropertiesManagement.ASTHMA_ENROLLMENT_FORM);
		rendevousForm = gp.getForm(GlobalPropertiesManagement.ASTHMA_RENDEVOUS_VISIT_FORM);
		asthmaVisitForm = gp.getForm(GlobalPropertiesManagement.ASTHMA_VISIT_FORM);


		DDBforms.add(DDBform);
		DDBforms.add(rendevousForm);
		DDBforms.add(Enrollmentform);

		DDBandEnrollmentforms.add(DDBform);
		DDBandEnrollmentforms.add(Enrollmentform);

		asthmaVisitForms.add(DDBform);
		asthmaVisitForms.add(Enrollmentform);
		asthmaVisitForms.add(rendevousForm);
		asthmaVisitForms.add(asthmaVisitForm);



		patientsSeenEncounterTypes.add(asthmaEncounterType);
		
		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		
		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrAfter");
		enrolledOnOrAfterOnOrBefore.add("enrolledOnOrBefore");
		
		peakFlowAfterSalbutamol = gp.getConcept(GlobalPropertiesManagement.PEAK_FLOW_AFTER_SALBUTAMOL);
		peakFlowBeforeSalbutamol = gp.getConcept(GlobalPropertiesManagement.PEAK_FLOW_BEFORE_SALBUTAMOL);
		
		smokingHistory = gp.getConcept(GlobalPropertiesManagement.SMOKING_HISTORY);
		
		salbutamol = gp.getConcept(GlobalPropertiesManagement.SALBUTAMOL_DRUG);
		
		beclomethasone = gp.getConcept(GlobalPropertiesManagement.BECLOMETHASONE_DRUG);
		
		prednisolone = gp.getConcept(GlobalPropertiesManagement.PREDNISOLONE_DRUG);
		

		asthmasMedications = gp.getConceptsByConceptSet(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_DISEASE_TREATMENT_DRUGS);

		asthmasMedicationsWithoutSalbutamol = new ArrayList<Concept>(asthmasMedications);
		asthmasMedicationsWithoutSalbutamol.remove(salbutamol);

		severePersistentAsthma = gp.getConcept(GlobalPropertiesManagement.SEVERE_PERSISTENT_ASTHMA);
		
		severeUncontrolledAsthma = gp.getConcept(GlobalPropertiesManagement.SEVERE_UNCONTROLLED_ASTHMA);

		intermittentasthma= gp.getConcept(GlobalPropertiesManagement.INTERMITTENT_ASTHMA);
		
		asthmaclassification = gp.getConcept(GlobalPropertiesManagement.ASTHMA_CLASSIFICATION);
		
		asthmasClassificationAnswers.add(severePersistentAsthma);
		
		asthmasClassificationAnswers.add(severeUncontrolledAsthma);
		
		returnVisitDate = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);
		
		NCDSpecificOutcomes=gp.getConcept(GlobalPropertiesManagement.NCD_SPECIFIC_OUTCOMES);
		NCDRelatedDeathOutcomes= gp.getConcept(GlobalPropertiesManagement.NCD_RELATED_DEATH_OUTCOMES);
		unknownCauseDeathOutcomes =gp.getConcept(GlobalPropertiesManagement.UNKNOWN_CAUSE_OF_DEATH_OUTCOMES);
		otherCauseOfDeathOutcomes =gp.getConcept(GlobalPropertiesManagement.OTHER_CAUSE_OF_DEATH_OUTCOMES);


		DeathOutcomeResons.add(NCDRelatedDeathOutcomes);
		DeathOutcomeResons.add(unknownCauseDeathOutcomes);
		DeathOutcomeResons.add(otherCauseOfDeathOutcomes);

		NCDLostToFolloUpOutCome =gp.getConcept(GlobalPropertiesManagement.LOST_TO_FOLLOWUP_OUTCOME);
		deathAndLostToFollowUpOutcomeString.append(NCDRelatedDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(unknownCauseDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(otherCauseOfDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(NCDLostToFolloUpOutCome.getConceptId());



		exitReasonFromCare=gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);
		HIVStatus=gp.getConcept(GlobalPropertiesManagement.HIV_STATUS);
		TBScreening=gp.getConcept(GlobalPropertiesManagement.TB_SCREENING_TEST);

		//hospitalAdmissionDate=gp.getConcept(GlobalPropertiesManagement.HOSPITAL_ADMITTANCE);


		diagnosisWhileHospitalized=gp.getConcept(GlobalPropertiesManagement.DIAGNOSIS_WHILE_HOSPITALIZED);
		asthmaExacerbation=gp.getConcept(GlobalPropertiesManagement.ASTHMA_EXACERBATION);

		moderatePersistentAsthma=gp.getConcept(GlobalPropertiesManagement.MODERATE_PERSISTENT_ASTHMA);
		mildPersistentAsthma=gp.getConcept(GlobalPropertiesManagement.MILD_PERSISTENT_ASTHMA);

		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.POST_CARDIAC_SURGERY_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.CKD_ENCOUNTER_TYPE));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HF_HTN_CKD_ENCOUNTER_TYPE));

	}
}
