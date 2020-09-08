package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.openmrs.Concept;
import org.openmrs.Drug;
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
import org.openmrs.module.rwandareports.util.RwandaReportsUtil;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupDiabetesQuarterlyAndMonthReport {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	// properties
	private Program DMProgram;
	
	private List<Program> DMPrograms = new ArrayList<Program>();
	
	private int DMEncounterTypeId;
	
	private Form DDBform;

	private Form DMEnrollmentForm;
	
	private Form DiabetesFlowVisit;
	
	private List<Form> DDBforms = new ArrayList<Form>();

	private List<Form> enrollForms = new ArrayList<Form>();
	
	private List<EncounterType> patientsSeenEncounterTypes = new ArrayList<EncounterType>();
	
	private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
	
	private List<String> onOrBefOnOrAf = new ArrayList<String>();
	
	private Concept weight;
	
	private Concept height;
	
	private Concept diastolicBP;
	
	private Concept systolicBP;
	
	private Concept hbA1c;
	
	private Concept sensationInRightFoot;
	
	private Concept sensationInLeftFoot;
	
	private Concept creatinine;
	
	private Concept insulin7030;
	
	private Concept insulinLente;
	
	private Concept insulinRapide;
	
	private Concept lisinopril;
	
	private Concept captopril;
	
	private List<Concept> lisinoprilCaptopril = new ArrayList<Concept>();
	
	private Concept metformin;
	
	private Concept glibenclimide;
	
	private List<Drug> onAceInhibitorsDrugs = new ArrayList<Drug>();
	
	List<Concept> neuropathyConcepts = new ArrayList<Concept>();
	
	List<Concept> insulinConcepts = new ArrayList<Concept>();
	
	List<Concept> metforminAndGlibenclimideConcepts = new ArrayList<Concept>();

	private Concept NCDSpecificOutcomes;
	private Concept NCDRelatedDeathOutcomes;

	private Concept exitReasonFromCare;

	List<Concept> DeathOutcomeResons=new  ArrayList<Concept>();

	private Concept unknownCauseDeathOutcomes;
	private Concept otherCauseOfDeathOutcomes;
	private Concept NCDLostToFolloUpOutCome;

	StringBuilder deathAndLostToFollowUpOutcomeString=new StringBuilder();

	private Concept urinaryAlbumin;



	public void setup() throws Exception {
		
		setUpProperties();

		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");

		// Quarterly Report Definition: Start
		
		ReportDefinition quarterlyRd = new ReportDefinition();
		quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
		quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));


		quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		
		quarterlyRd.setName("NCD-Diabetes Indicator Report-Quarterly");
		
		quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
		    ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));
		
		// Quarterly Report Definition: End
		
		ProgramEnrollmentCohortDefinition patientEnrolledInDM = new ProgramEnrollmentCohortDefinition();
		patientEnrolledInDM.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientEnrolledInDM.setPrograms(DMPrograms);
		
		quarterlyRd.setBaseCohortDefinition(patientEnrolledInDM,
		    ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));

		Helper.saveReportDefinition(quarterlyRd);
		
		ReportDesign quarterlyDesign = Helper.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
		    "DM_Quarterly_Indicator_Report.xls", "Diabetes Quarterly Indicator Report (Excel)", null);
		Properties quarterlyProps = new Properties();
		quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
		quarterlyProps.put("sortWeight","5000");
		quarterlyDesign.setProperties(quarterlyProps);
		Helper.saveReportDesign(quarterlyDesign);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Diabetes Quarterly Indicator Report (Excel)".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Diabetes Indicator Report-Quarterly");

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
		SqlEncounterQuery patientVisitsToDMClinic = new SqlEncounterQuery();
		
		patientVisitsToDMClinic.setQuery("select e.encounter_id from encounter e where e.encounter_type="
		        + DMEncounterTypeId +" and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.voided=0 group by encounter_datetime, patient_id");
		patientVisitsToDMClinic.setName("patientVisitsToDMClinic");
		patientVisitsToDMClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientVisitsToDMClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		EncounterIndicator patientVisitsToDMClinicQuarterIndicator = new EncounterIndicator();
		patientVisitsToDMClinicQuarterIndicator.setName("patientVisitsToDMClinicQuarterIndicator");
		patientVisitsToDMClinicQuarterIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToDMClinic,
		        ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
		
		dsd.addColumn(patientVisitsToDMClinicQuarterIndicator);
		
	}
	
	// create quarterly cohort Data set
	private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
		CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
		dsd.setName("Quarterly Cohort Data Set");
		dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
		dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
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



		//========================================================================================
		//  Active patients with no exit in Reporting periode
		//=======================================================================================

		SqlCohortDefinition currentlyInProgramAndNotCompleted=new SqlCohortDefinition();
		currentlyInProgramAndNotCompleted.setName("currentlyInProgramAndNotCompleted");
		currentlyInProgramAndNotCompleted.setQuery("select patient_id from patient_program where voided=0 and program_id="+DMProgram.getProgramId()+" and (date_completed> :endDate or date_completed is null) and date_enrolled<= :endDate");
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
		activePatientWithNoExitBeforeQuarterStart.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithNoExitBeforeQuarterStart.getSearches().put("1",new Mapped<CohortDefinition>(currentlyInProgramAndNotCompleted, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientWithNoExitBeforeQuarterStart.getSearches().put("2",new Mapped<CohortDefinition>(patientWithAnyReasonForExitingFromCare, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientWithNoExitBeforeQuarterStart.getSearches().put("3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${endDate-12m}")));
		activePatientWithNoExitBeforeQuarterStart.setCompositionString("1 and 3 and (not 2)");


		CohortIndicator activePatientIndicator = Indicators.newCountIndicator("activePatientIndicator",
				activePatientWithNoExitBeforeQuarterStart,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},endDate=${endDate}"));


		dsd.addColumn("Active", "Total # of Active patients : seen in the last 12 months", new Mapped(
				activePatientIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



		ProgramEnrollmentCohortDefinition patientEnrolledInDMProgram = Cohorts.createProgramEnrollmentParameterizedByStartEndDate(
				"Enrolled In DM", DMProgram);

		ProgramEnrollmentCohortDefinition patientEnrolledInDMProgramByEndDate = Cohorts.createProgramEnrollmentEverByEndDate("Enrolled Ever In DM", DMProgram);

		CompositionCohortDefinition patientCurrentEnrolledInDMAndSeenInSameQuarter=new CompositionCohortDefinition();
		patientCurrentEnrolledInDMAndSeenInSameQuarter.setName("patientCurrentEnrolledInDMAndSeenInSameQuarter");/*
		patientCurrentEnrolledInDMAndSeenInSameQuarter.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientCurrentEnrolledInDMAndSeenInSameQuarter.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));*/
		patientCurrentEnrolledInDMAndSeenInSameQuarter.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		patientCurrentEnrolledInDMAndSeenInSameQuarter.addParameter(new Parameter("enrolledOnOrAfter", "enrolledOnOrAfter", Date.class));
		patientCurrentEnrolledInDMAndSeenInSameQuarter.getSearches().put(
				"1",new Mapped<CohortDefinition>(patientEnrolledInDMProgram, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore},enrolledOnOrAfter=${enrolledOnOrAfter}")));
		patientCurrentEnrolledInDMAndSeenInSameQuarter.getSearches().put(
				"2",new Mapped<CohortDefinition>(patientEnrolledInDMProgramByEndDate, ParameterizableUtil
						.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		patientCurrentEnrolledInDMAndSeenInSameQuarter.getSearches().put(
				"3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

		//patientCurrentEnrolledInDMAndSeenInSameQuarter.setCompositionString("(1 and (not 2)) and 3");
		patientCurrentEnrolledInDMAndSeenInSameQuarter.setCompositionString("1 and (not 2)");




		CohortIndicator patientCurrentEnrolledInDMAndSeenInSameQuarterIndicator = Indicators.newCountIndicator(
				"patientCurrentEnrolledInDMAndSeenInSameQuarterIndicator", patientCurrentEnrolledInDMAndSeenInSameQuarter,
				ParameterizableUtil.createParameterMappings("enrolledOnOrAfter=${startDate},enrolledOnOrBefore=${endDate}"));

		dsd.addColumn(
				"NewInQ",
				"Total # of new patients enrolled in the last quarter",
				new Mapped(patientCurrentEnrolledInDMAndSeenInSameQuarterIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

//=============================================
// A3: Total # of patients ever enrolled
//=============================================
		CohortIndicator patientEverEnrolledInDMIndicator = Indicators.newCountIndicator(
				"patientEverEnrolledInDMIndicator", patientEnrolledInDMProgramByEndDate,
				ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate+1d}"));

		dsd.addColumn(
				"EverEnrolled",
				"Total # of patients evere enrolled in the last quarter",
				new Mapped(patientEverEnrolledInDMIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

//============================================
// A4: Percentage of active patients
//============================================
		InProgramCohortDefinition inDMProgramByEndDate=Cohorts.createInProgramParameterizableByDate("In DM by EndDate",DMProgram);
		ProgramEnrollmentCohortDefinition completedInDMProgramByEndDate=Cohorts.createProgramCompletedByEndDate("Completed DM by EndDate",DMProgram);




		/*CompositionCohortDefinition currentlyInProgramAndNotCompleted= new CompositionCohortDefinition();
		currentlyInProgramAndNotCompleted.setName("currentlyInProgramAndNotCompleted");
		currentlyInProgramAndNotCompleted.addParameter(new Parameter("onDate", "onDate", Date.class));
		currentlyInProgramAndNotCompleted.addParameter(new Parameter("completedOnOrBefore", "completedOnOrBefore", Date.class));
		currentlyInProgramAndNotCompleted.getSearches().put(
				"1",
				new Mapped<CohortDefinition>(inDMProgramByEndDate, ParameterizableUtil
						.createParameterMappings("onDate=${onDate}")));
		currentlyInProgramAndNotCompleted.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(completedInDMProgramByEndDate, ParameterizableUtil
						.createParameterMappings("completedOnOrBefore=${completedOnOrBefore}")));
		currentlyInProgramAndNotCompleted.setCompositionString("1 and (not 2)");*/

		CohortIndicator currentlyInProgramAndNotCompletedIndicator=Indicators.newCountIndicator(
				"currentlyInProgramAndNotCompletedIndicator", currentlyInProgramAndNotCompleted,
				ParameterizableUtil.createParameterMappings("endDate=${endDate}"));

		dsd.addColumn(
				"CurEnrol",
				"Percentage of active patients)",
				new Mapped(currentlyInProgramAndNotCompletedIndicator, ParameterizableUtil
						.createParameterMappings("endDate=${endDate}")), "");

//==============================================
// A5: Proportion of all active patients with age<27
//===============================================


		AgeCohortDefinition under27=Cohorts.createUnderAgeCohort("under27",26);


		CompositionCohortDefinition activePatientUnder27 = new CompositionCohortDefinition();
		activePatientUnder27.setName("activePatientUnder27");
		activePatientUnder27.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientUnder27.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientUnder27.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientUnder27.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientUnder27.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		activePatientUnder27.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientUnder27.getSearches().put("2",new Mapped<CohortDefinition>(under27, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
		activePatientUnder27.setCompositionString("1 and 2");


		CohortIndicator activePatientUnder26Indicator = Indicators.newCountIndicator("activePatientUnder26Indicator",
				activePatientUnder27,
				ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},effectiveDate=${endDate}"));


		dsd.addColumn("ActiveUnder26", "Total # of Active patients : seen in the last 12 months Under 26", new Mapped(
				activePatientUnder26Indicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");




//==============================================
// A6: Proportion of all active patients with age< 16
//===============================================


		AgeCohortDefinition under16=Cohorts.createUnderAgeCohort("under16",15);

		CompositionCohortDefinition activePatientUnder16 = new CompositionCohortDefinition();
		activePatientUnder16.setName("activePatientUnder16");
		activePatientUnder16.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientUnder16.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientUnder16.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientUnder16.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientUnder16.addParameter(new Parameter("effectiveDate", "effectiveDate", Date.class));
		activePatientUnder16.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientUnder16.getSearches().put("2",new Mapped<CohortDefinition>(under16, ParameterizableUtil.createParameterMappings("effectiveDate=${effectiveDate}")));
		activePatientUnder16.setCompositionString("1 and 2");


		CohortIndicator activePatientUnder16Indicator = Indicators.newCountIndicator("activePatientUnder16Indicator",
				activePatientUnder16,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},effectiveDate=${endDate}"));


		dsd.addColumn("ActiveUnder16", "Total # of Active patients : seen in the last 12 months Under 16", new Mapped(
				activePatientUnder16Indicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


//==============================================
// A7: Of all active patients, % male
//===============================================

		GenderCohortDefinition malePatients = Cohorts.createMaleCohortDefinition("Male patients");



		CompositionCohortDefinition activeMalePatients = new CompositionCohortDefinition();
		activeMalePatients.setName("activeMalePatients");
		activeMalePatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeMalePatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeMalePatients.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeMalePatients.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeMalePatients.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${onOrBefore},endDate=${endDate}")));
		activeMalePatients.getSearches().put("2",new Mapped<CohortDefinition>(malePatients, null));
		activeMalePatients.setCompositionString("1 and 2");


		CohortIndicator activeMalePatientsIndicator = Indicators.newCountIndicator("activeMalePatientsIndicator",
				activeMalePatients,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("ActiveMale", "Total # of Active patients : seen in the last 12 months who are Male", new Mapped(
				activeMalePatientsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



//=======================================
//  A8: Total active patients disaggregated by BMI category
//=======================================

		SqlCohortDefinition patientsWithBMILessThan18=Cohorts.getPatientsWithBMI("patientsWithBMILessThan18","< 18",weight,height);

		SqlCohortDefinition patientsWithBMIBetween18And25=Cohorts.getPatientsWithBMI("patientsWithBMIBetween18And25"," between 18.0 and 25.0",weight,height);

		SqlCohortDefinition patientsWithBMIAbove25AndLessThan30=Cohorts.getPatientsWithBMI("patientsWithBMIAbove25AndLessThan30"," between 25.1 and 29.9",weight,height);

		SqlCohortDefinition patientsWithBMIGreaterThanOrEqual30=Cohorts.getPatientsWithBMI("patientsWithBMILessThan18",">= 30.0",weight,height);


		CompositionCohortDefinition activeUnderweight = new CompositionCohortDefinition();
		activeUnderweight.setName("activeUnderweight");
		activeUnderweight.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeUnderweight.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeUnderweight.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeUnderweight.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeUnderweight.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activeUnderweight.getSearches().put("2",new Mapped<CohortDefinition>(patientsWithBMILessThan18, null));
		activeUnderweight.setCompositionString("1 and 2");

		CohortIndicator activeUnderweightIndicator = Indicators.newCountIndicator("activeUnderweightIndicator",
				activeUnderweight, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("Underweight", "Total # of Active patients : Underweight", new Mapped(
				activeUnderweightIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



		CompositionCohortDefinition activeNormal = new CompositionCohortDefinition();
		activeNormal.setName("activeNormal");
		activeNormal.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeNormal.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeNormal.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeNormal.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeNormal.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activeNormal.getSearches().put("2",new Mapped<CohortDefinition>(patientsWithBMIBetween18And25, null));
		activeNormal.setCompositionString("1 and 2");

		CohortIndicator activeNormalIndicator = Indicators.newCountIndicator("activeNormalIndicator",
				activeNormal, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("Normal", "Total # of Active patients : Normal", new Mapped(
				activeNormalIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


		CompositionCohortDefinition activeOverweight = new CompositionCohortDefinition();
		activeOverweight.setName("activeOverweight");
		activeOverweight.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeOverweight.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeOverweight.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeOverweight.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeOverweight.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activeOverweight.getSearches().put("2",new Mapped<CohortDefinition>(patientsWithBMIAbove25AndLessThan30, null));
		activeOverweight.setCompositionString("1 and 2");

		CohortIndicator activeOverweightIndicator = Indicators.newCountIndicator("activeOverweightIndicator",
				activeOverweight, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("Overweight", "Total # of Active patients : Overweight", new Mapped(
				activeOverweightIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

		CompositionCohortDefinition activeObese = new CompositionCohortDefinition();
		activeObese.setName("activeObese");
		activeObese.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeObese.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeObese.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeObese.addParameter(new Parameter("endDate", "endDate", Date.class));
		activeObese.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activeObese.getSearches().put("2",new Mapped<CohortDefinition>(patientsWithBMIGreaterThanOrEqual30, null));
		activeObese.setCompositionString("1 and 2");

		CohortIndicator activeObeseIndicator = Indicators.newCountIndicator("activeObeseIndicator",
				activeObese, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("Obese", "Total # of Active patients : Obese", new Mapped(
				activeObeseIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");




		//=================================================================================
		//  A10: % of deaths which are disease related
		//=================================================================================
//Numerator

		CohortIndicator NCDRelatedDeathIndicator = Indicators.newCountIndicator(
				"NCDRelatedDeathIndicator", NCDRelatedDeath,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));


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


		CohortIndicator activePatientsInPatientDiedStateInQuarterIndicator = Indicators
				.newCountIndicator(
						"activePatientsInPatientDiedStateInQuarterIndicator",
						activePatientsInPatientDiedStateOrNCDRelatedDeath,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));


		dsd.addColumn("A10N", "Number of all patients who were seen in the last year who died in the reporting period", new Mapped(
				activePatientsInPatientDiedStateInQuarterIndicator, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

//Denominator

		//SqlCohortDefinition patientDiedOfNCDRelatedDeath= Cohorts.getPatientsWithOutcomeprogramEndReasons("patientDiedOfNCDRelatedDeath",NCDSpecificOutcomes,DeathOutcomeResons);

/*

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
		*//*activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"2",
				new Mapped<CohortDefinition>(patientsInPatientDiedState, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));*//*
		activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientsDied, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

		*//*activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"3",
				new Mapped<CohortDefinition>(patientDiedOfNCDRelatedDeath, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		*//*

		*//*activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"4",
				new Mapped<CohortDefinition>(obsPatientDiedReasonForExitingFromCare, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
		activePatientsInPatientDiedStateOrNCDRelatedDeath.getSearches().put(
				"5",
				new Mapped<CohortDefinition>(NCDRelatedDeath, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

		activePatientsInPatientDiedStateOrNCDRelatedDeath.setCompositionString("1 AND (2 OR 3 OR 4 OR 5)");*//*

		activePatientsInPatientDiedStateOrNCDRelatedDeath.setCompositionString("1 AND 2");

		CohortIndicator activePatientsInPatientDiedStateInQuarterIndicator = Indicators
				.newCountIndicator(
						"activePatientsInPatientDiedStateInQuarterIndicator",
						activePatientsInPatientDiedStateOrNCDRelatedDeath,
						ParameterizableUtil
								.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));*/


		CohortIndicator patientsSeenInYearIndicator = Indicators.newCountIndicator("patientsSeenIndicator", patientSeen,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate-9m},onOrBefore=${endDate}"));

		dsd.addColumn(
				"ActiveY",
				"Total active in Year patients",
				new Mapped(patientsSeenInYearIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		//========================================================
		//        Adding columns to data set definition         //
		//========================================================


		dsd.addColumn(
				"DiedQ",
				"Total active patients, number who Died in quarter",
				new Mapped(activePatientsInPatientDiedStateInQuarterIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");


		// A11

		SqlCohortDefinition patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes=new SqlCohortDefinition();
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.setName("patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes");
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.setQuery("select patient_id from patient_program where program_id="+DMProgram.getProgramId()+" and date_completed>= :onOrAfter and date_completed<= :onOrBefore and voided=0 and outcome_concept_id not in ("+deathAndLostToFollowUpOutcomeString.toString()+")");
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));

		SqlCohortDefinition patientWhoCompletedProgram=new SqlCohortDefinition();
		patientWhoCompletedProgram.setName("patientWhoCompletedProgram");
		patientWhoCompletedProgram.setQuery("select patient_id from patient_program where program_id="+DMProgram.getProgramId()+" and date_completed>= :onOrAfter and date_completed<= :onOrBefore and voided=0");
		patientWhoCompletedProgram.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientWhoCompletedProgram.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));




		CohortIndicator patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator = Indicators.newCountIndicator("patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator", patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomes,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn(
				"A11N",
				"patient Who Completed Program Without Death And Lost To Followup Outcomes",
				new Mapped(patientWhoCompletedProgramWithoutDeathAndLostToFollowupOutcomesIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		CohortIndicator patientWhoCompletedProgramIndicator = Indicators.newCountIndicator("patientWhoCompletedProgramIndicator", patientWhoCompletedProgram,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

		dsd.addColumn(
				"A11D",
				"patient Who Completed Program ",
				new Mapped(patientWhoCompletedProgramIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");






//===================================
// B1: Of total active patients seen in the last quarter, % who had Urinary Albumin tested at least within 12 months of the end of the reporting period.
//======================================
		SqlCohortDefinition patientWithurinaryAlbumin=Cohorts.getPatientsWithObservationsByStartDateAndEndDate("patientWithurinaryAlbumin", urinaryAlbumin);

		CompositionCohortDefinition activePatientWithurinaryAlbumin = new CompositionCohortDefinition();
		activePatientWithurinaryAlbumin.setName("activePatientWithurinaryAlbumin");
		activePatientWithurinaryAlbumin.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientWithurinaryAlbumin.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithurinaryAlbumin.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientWithurinaryAlbumin.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithurinaryAlbumin.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientWithurinaryAlbumin.getSearches().put("2",new Mapped<CohortDefinition>(patientWithurinaryAlbumin, ParameterizableUtil.createParameterMappings("startDate=${endDate-12m},endDate=${endDate}")));
		activePatientWithurinaryAlbumin.setCompositionString("1 and 2");

		CohortIndicator activePatientWithurinaryAlbuminIndicator = Indicators.newCountIndicator("activePatientWithurinaryAlbuminIndicator",
				activePatientWithurinaryAlbumin, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("B1", "Total # of Active patients : Albumin documented in 12 months", new Mapped(
				activePatientWithurinaryAlbuminIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



//======================================================
// B2:  Of total active patients seen in the last quarter, % who had Creatinine tested at least within 12 months of the reporting period
//======================================================

		SqlCohortDefinition patientWithCreatinine=Cohorts.getPatientsWithObservationsByStartDateAndEndDate("patientWithCreatinine", creatinine);

		CompositionCohortDefinition activePatientWithCreatinine = new CompositionCohortDefinition();
		activePatientWithCreatinine.setName("activePatientWithCreatinine");
		activePatientWithCreatinine.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientWithCreatinine.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithCreatinine.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientWithCreatinine.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithCreatinine.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientWithCreatinine.getSearches().put("2",new Mapped<CohortDefinition>(patientWithCreatinine, ParameterizableUtil.createParameterMappings("startDate=${endDate-12m},endDate=${endDate}")));
		activePatientWithCreatinine.setCompositionString("1 and 2");

		CohortIndicator activePatientWithCreatinineIndicator = Indicators.newCountIndicator("activePatientWithCreatinineIndicator",
				activePatientWithCreatinine, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("B2", "Total # of Active patients : Creatinine documented in 12 months", new Mapped(
				activePatientWithCreatinineIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


//======================================================
// B3: Total active Diabetes patients who enrolled at least 3 months before the end of the reporting period, % of who had HbA1C documented  in the Q
// ======================================================

		SqlCohortDefinition patientWithHBA1C=Cohorts.getPatientsWithObservationsByStartDateAndEndDate("patientWithCreatinine", hbA1c);

		CompositionCohortDefinition activePatientWithHBA1C = new CompositionCohortDefinition();
		activePatientWithHBA1C.setName("activePatientWithHBA1C");
		activePatientWithHBA1C.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientWithHBA1C.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithHBA1C.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientWithHBA1C.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithHBA1C.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		activePatientWithHBA1C.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientWithHBA1C.getSearches().put("2",new Mapped<CohortDefinition>(patientWithHBA1C, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
		activePatientWithHBA1C.getSearches().put("3",new Mapped<CohortDefinition>(patientEnrolledInDMProgramByEndDate, ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		activePatientWithHBA1C.setCompositionString("1 and 2 and 3");

		CohortIndicator activePatientWithHBA1CIndicator = Indicators.newCountIndicator("activePatientWithHBA1CIndicator",
				activePatientWithHBA1C, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},enrolledOnOrBefore=${endDate}"));


		dsd.addColumn("B3N", "Total # of Active patients Enrolled before the Q : HbA1C documented  in the Q", new Mapped(
				activePatientWithHBA1CIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



		CompositionCohortDefinition activePatientEnrolledBeforeTheQ = new CompositionCohortDefinition();
		activePatientEnrolledBeforeTheQ.setName("activePatientEnrolledBeforeTheQ");
		activePatientEnrolledBeforeTheQ.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientEnrolledBeforeTheQ.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientEnrolledBeforeTheQ.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientEnrolledBeforeTheQ.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientEnrolledBeforeTheQ.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		activePatientEnrolledBeforeTheQ.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientEnrolledBeforeTheQ.getSearches().put("2",new Mapped<CohortDefinition>(patientEnrolledInDMProgramByEndDate, ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${enrolledOnOrBefore-3m}")));
		activePatientEnrolledBeforeTheQ.setCompositionString("1 and 2");

		CohortIndicator activePatientEnrolledBeforeTheQIndicator = Indicators.newCountIndicator("activePatientEnrolledBeforeTheQIndicator",
				activePatientEnrolledBeforeTheQ, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},enrolledOnOrBefore=${endDate}"));


		dsd.addColumn("B3D", "Total # of Active patients Enrolled before the Q ", new Mapped(
				activePatientEnrolledBeforeTheQIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


//===============================================
// B4: % of active patients on oral therapy only
//===============================================

		SqlCohortDefinition patientOnInsulin = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
				"patientOnInsulinAtLastVist", insulinConcepts);

		SqlCohortDefinition onMetforminOrGlibenclimide = Cohorts.getPatientsOnCurrentRegimenBasedOnEndDate(
				"onMetforminOrGlibenclimide", metforminAndGlibenclimideConcepts);

		CompositionCohortDefinition activePatientOnOralTherapyOnly = new CompositionCohortDefinition();
		activePatientOnOralTherapyOnly.setName("activePatientOnOralTherapyOnly");
		activePatientOnOralTherapyOnly.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientOnOralTherapyOnly.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientOnOralTherapyOnly.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientOnOralTherapyOnly.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientOnOralTherapyOnly.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientOnOralTherapyOnly.getSearches().put("2",new Mapped<CohortDefinition>(onMetforminOrGlibenclimide, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientOnOralTherapyOnly.getSearches().put("3",new Mapped<CohortDefinition>(patientOnInsulin, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		activePatientOnOralTherapyOnly.setCompositionString("1 and 2 and (not 3)");

		CohortIndicator activePatientOnOralTherapyOnlyIndicator = Indicators.newCountIndicator("activePatientOnOralTherapyOnlyIndicator",
				activePatientOnOralTherapyOnly, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn("B4N", "Total # of active patients on oral therapy only ", new Mapped(
				activePatientOnOralTherapyOnlyIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");





		//=======================================================================
		// C1: Of total DM active patients, % with a visit in last 12 months but no visit within last 6 months.
		//==================================================================

		CompositionCohortDefinition activeAndNotSeenIn6MonthsPatients = new CompositionCohortDefinition();
		activeAndNotSeenIn6MonthsPatients.setName("activeAndNotSeenIn6MonthsPatients");
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("startDate", "startDate", Date.class));
		activeAndNotSeenIn6MonthsPatients.addParameter(new Parameter("endDate", "endDate", Date.class));

		activeAndNotSeenIn6MonthsPatients.getSearches().put("1",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore-12m}")));
		activeAndNotSeenIn6MonthsPatients.getSearches().put("2",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activeAndNotSeenIn6MonthsPatients.getSearches().put("3",new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
						.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore-6m}")));
		activeAndNotSeenIn6MonthsPatients.setCompositionString("1 AND 2 AND (NOT 3)");

		CohortIndicator activeAndNotSeenIn6MonthsPatientsIndicator = Indicators.newCountIndicator(
				"activeAndNotSeenIn6MonthsPatientsIndicator",
				activeAndNotSeenIn6MonthsPatients,
				ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));

		dsd.addColumn(
				"C1",
				"Of total DM active patients, % with a visit in last 12 months but no visit within last 6 months",
				new Mapped(activeAndNotSeenIn6MonthsPatientsIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");

//=======================================================
// Of all active patients with HbA1c tested in the last 6 months, % with last HbA1c <=8 ///of all active for the Q, % hbA1C <=8%
//=======================================================

		CompositionCohortDefinition activePatientWithHBA1CIn6Months = new CompositionCohortDefinition();
		activePatientWithHBA1CIn6Months.setName("activePatientWithHBA1CIn6Months");
		activePatientWithHBA1CIn6Months.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientWithHBA1CIn6Months.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithHBA1CIn6Months.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientWithHBA1CIn6Months.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithHBA1CIn6Months.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		activePatientWithHBA1CIn6Months.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientWithHBA1CIn6Months.getSearches().put("2",new Mapped<CohortDefinition>(patientWithHBA1C, ParameterizableUtil.createParameterMappings("startDate=${endDate-6m},endDate=${endDate}")));
		activePatientWithHBA1CIn6Months.setCompositionString("1 and 2");

		CohortIndicator activePatientWithHBA1CIn6MonthsIndicator = Indicators.newCountIndicator("activePatientWithHBA1CIn6MonthsIndicator",
				activePatientWithHBA1CIn6Months, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},enrolledOnOrBefore=${endDate}"));


		dsd.addColumn("C2D", "Total # Of all active patients with HbA1c tested in the last 6 months", new Mapped(
				activePatientWithHBA1CIn6MonthsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

		NumericObsCohortDefinition patientsWithHbA1cLessThanOrEqualTo8=Cohorts.createNumericObsCohortDefinition("patientsWithHbA1cLessThanOrEqualTo8",onOrAfterOnOrBefore,hbA1c,8.0,RangeComparator.LESS_EQUAL,TimeModifier.LAST);

		CompositionCohortDefinition activePatientWithHBA1CIn6MonthsLessEqual8 = new CompositionCohortDefinition();
		activePatientWithHBA1CIn6MonthsLessEqual8.setName("activePatientWithHBA1CIn6MonthsLessEqual8");
		activePatientWithHBA1CIn6MonthsLessEqual8.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		activePatientWithHBA1CIn6MonthsLessEqual8.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		activePatientWithHBA1CIn6MonthsLessEqual8.addParameter(new Parameter("startDate", "startDate", Date.class));
		activePatientWithHBA1CIn6MonthsLessEqual8.addParameter(new Parameter("endDate", "endDate", Date.class));
		activePatientWithHBA1CIn6MonthsLessEqual8.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
		activePatientWithHBA1CIn6MonthsLessEqual8.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrAfter=${onOrAfter},onOrBefore=${endDate},endDate=${endDate}")));
		activePatientWithHBA1CIn6MonthsLessEqual8.getSearches().put("2",new Mapped<CohortDefinition>(patientWithHBA1C, ParameterizableUtil.createParameterMappings("startDate=${endDate-6m},endDate=${endDate}")));
		activePatientWithHBA1CIn6MonthsLessEqual8.getSearches().put("3",new Mapped<CohortDefinition>(patientsWithHbA1cLessThanOrEqualTo8, ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-6m},onOrBefore=${onOrBefore}")));
		activePatientWithHBA1CIn6MonthsLessEqual8.setCompositionString("1 and 2 and 3");

		CohortIndicator activePatientWithHBA1CIn6MonthsLessEqual8Indicator = Indicators.newCountIndicator("activePatientWithHBA1CIn6MonthsLessEqual8Indicator",
				activePatientWithHBA1CIn6MonthsLessEqual8, ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate},enrolledOnOrBefore=${endDate}"));


		dsd.addColumn("C2N", "Total # Of all active patients with HbA1c tested in the last 6 months, % with last HbA1c <=8", new Mapped(
				activePatientWithHBA1CIn6MonthsLessEqual8Indicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");




		//====================================================
		// C3: of active patients with creatinine check in past 12 months, % with latest Cr > 200
		//=====================================================


		NumericObsCohortDefinition patientsWithCreatinineGreaterThan200 = Cohorts.createNumericObsCohortDefinition(
				"patientsWithSRGreaterThan200", onOrAfterOnOrBefore, creatinine, 200, RangeComparator.GREATER_THAN,
				TimeModifier.LAST);

		NumericObsCohortDefinition patientWithCreatinineChecked = Cohorts.createNumericObsCohortDefinition("patientWithCreatinineChecked", onOrAfterOnOrBefore,creatinine, 0.0, RangeComparator.GREATER_EQUAL, TimeModifier.ANY);

		CompositionCohortDefinition patientActiveWithCreatinineCheckedIn12Months = new CompositionCohortDefinition();
		patientActiveWithCreatinineCheckedIn12Months.setName("patientActiveWithCheckedIn12Months");
		patientActiveWithCreatinineCheckedIn12Months.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientActiveWithCreatinineCheckedIn12Months.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientActiveWithCreatinineCheckedIn12Months.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientActiveWithCreatinineCheckedIn12Months.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientActiveWithCreatinineCheckedIn12Months.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},endDate=${endDate}")));
		patientActiveWithCreatinineCheckedIn12Months.getSearches().put("2", new Mapped<CohortDefinition>(patientWithCreatinineChecked, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore-12m}")));
		patientActiveWithCreatinineCheckedIn12Months.setCompositionString("1 AND 2");


		CohortIndicator patientActiveWithCreatinineCheckedIn12MonthsIndicator = Indicators.newCountIndicator(
				"patientActiveWithCheckedIn12MonthsIndicator", patientActiveWithCreatinineCheckedIn12Months,ParameterizableUtil
						.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn(
				"C3D",
				"of active patients with creatinine check in past 12 months",
				new Mapped(patientActiveWithCreatinineCheckedIn12MonthsIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");




		CompositionCohortDefinition patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200 = new CompositionCohortDefinition();
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.setName("patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200");
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},endDate=${endDate}")));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.getSearches().put("2", new Mapped<CohortDefinition>(patientWithCreatinineChecked, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m}")));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.getSearches().put("3", new Mapped<CohortDefinition>(patientsWithCreatinineGreaterThan200, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter-9m}")));
		patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200.setCompositionString("1 AND 2 AND 3");


		CohortIndicator patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200Indicator = Indicators.newCountIndicator(
				"patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200Indicator", patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200,ParameterizableUtil
						.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn(
				"C3N",
				"of active patients with creatinine check in past 12 months, % with latest Cr > 200",
				new Mapped(patientActiveWithCreatinineCheckedIn12MonthsAndGreaterThan200Indicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");



		//====================================================
		// C4: Of total DM active patients with blood pressure checked at last visit date, % with BP<=130/80 mmHg
		//=====================================================


		NumericObsCohortDefinition patientsWithSystolicBPLessEqual130 = Cohorts.createNumericObsCohortDefinition(
				"patientsWithSystolicLessEqual", systolicBP, 130.0, RangeComparator.LESS_EQUAL,
				TimeModifier.LAST);
		NumericObsCohortDefinition patientsWithDiastolicBPLessEqual80 = Cohorts.createNumericObsCohortDefinition(
				"patientsWithDiastolicBPLessEqual", diastolicBP, 80.0, RangeComparator.LESS_EQUAL,
				TimeModifier.LAST);

		NumericObsCohortDefinition patientWithSystolicBPChecked = Cohorts.createNumericObsCohortDefinition("patientWithSystolicBPChecked",systolicBP, 0, RangeComparator.GREATER_EQUAL, TimeModifier.LAST);
		NumericObsCohortDefinition patientWithDiastolicBPChecked = Cohorts.createNumericObsCohortDefinition("patientWithDiastolicBPChecked",diastolicBP, 0, RangeComparator.GREATER_EQUAL, TimeModifier.LAST);

		CompositionCohortDefinition patientActiveWithBPCheckedIn12Months = new CompositionCohortDefinition();
		patientActiveWithBPCheckedIn12Months.setName("patientActiveWithBPCheckedIn12Months");
		patientActiveWithBPCheckedIn12Months.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientActiveWithBPCheckedIn12Months.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientActiveWithBPCheckedIn12Months.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientActiveWithBPCheckedIn12Months.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientActiveWithBPCheckedIn12Months.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},endDate=${endDate}")));
		patientActiveWithBPCheckedIn12Months.getSearches().put("2", new Mapped<CohortDefinition>(patientWithSystolicBPChecked, null));
		patientActiveWithBPCheckedIn12Months.getSearches().put("3", new Mapped<CohortDefinition>(patientWithDiastolicBPChecked, null));
		patientActiveWithBPCheckedIn12Months.setCompositionString("1 AND 2 and 3");


		CohortIndicator patientActiveWithBPCheckedIn12MonthsIndicator = Indicators.newCountIndicator(
				"patientActiveWithBPCheckedIn12MonthsIndicator", patientActiveWithBPCheckedIn12Months,ParameterizableUtil
						.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn(
				"C4D",
				"of active patients with creatinine check in past 12 months",
				new Mapped(patientActiveWithBPCheckedIn12MonthsIndicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");




		CompositionCohortDefinition patientActiveWithBPLessEqualThan130Over80 = new CompositionCohortDefinition();
		patientActiveWithBPLessEqualThan130Over80.setName("patientActiveWithBPLessEqualThan130Over80");
		patientActiveWithBPLessEqualThan130Over80.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		patientActiveWithBPLessEqualThan130Over80.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		patientActiveWithBPLessEqualThan130Over80.addParameter(new Parameter("startDate", "startDate", Date.class));
		patientActiveWithBPLessEqualThan130Over80.addParameter(new Parameter("endDate", "endDate", Date.class));
		patientActiveWithBPLessEqualThan130Over80.getSearches().put("1",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},endDate=${endDate}")));
		patientActiveWithBPLessEqualThan130Over80.getSearches().put("2", new Mapped<CohortDefinition>(patientsWithSystolicBPLessEqual130, null));
		patientActiveWithBPLessEqualThan130Over80.getSearches().put("3", new Mapped<CohortDefinition>(patientsWithDiastolicBPLessEqual80, null));
		patientActiveWithBPLessEqualThan130Over80.setCompositionString("1 AND 2 and 3");



		CohortIndicator patientActiveWithBPLessEqualThan130Over80Indicator = Indicators.newCountIndicator(
				"patientActiveWithBPLessEqualThan130Over80Indicator", patientActiveWithBPLessEqualThan130Over80,ParameterizableUtil
						.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


		dsd.addColumn(
				"C4N",
				"of active patients with creatinine check in past 12 months, % with latest Cr > 200",
				new Mapped(patientActiveWithBPLessEqualThan130Over80Indicator, ParameterizableUtil
						.createParameterMappings("startDate=${startDate},endDate=${endDate}")), "");




		//==============================================================
		// D3: Of patients currently enrolled, % Lost to follow up
		//==============================================================

		CompositionCohortDefinition currentlyInProgramButLost= new CompositionCohortDefinition();
		currentlyInProgramButLost.setName("currentlyInProgramButLost");
		//currentlyInProgramButLost.addParameter(new Parameter("onDate", "onDate", Date.class));
		currentlyInProgramButLost.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
		currentlyInProgramButLost.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
		currentlyInProgramButLost.addParameter(new Parameter("startDate", "startDate", Date.class));
		currentlyInProgramButLost.addParameter(new Parameter("endDate", "endDate", Date.class));
		currentlyInProgramButLost.getSearches().put("1",new Mapped<CohortDefinition>(currentlyInProgramAndNotCompleted, ParameterizableUtil.createParameterMappings("endDate=${endDate}")));
		currentlyInProgramButLost.getSearches().put("2",new Mapped<CohortDefinition>(activePatientWithNoExitBeforeQuarterStart, ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter},endDate=${endDate}")));
		currentlyInProgramButLost.setCompositionString("1 and (not 2)");

		CohortIndicator currentlyInProgramButLostIndicator=Indicators.newCountIndicator(
				"currentlyInProgramButLostIndicator", currentlyInProgramButLost,
				ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate},endDate=${endDate},onOrBefore=${endDate},onOrAfter=${startDate}"));

		dsd.addColumn(
				"Lost",
				"Percentage of active HF patients in the last Quarter(active/ currently enrolled)",
				new Mapped(currentlyInProgramButLostIndicator, ParameterizableUtil
						.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
	}
	

	
	private void setUpProperties() {
		DMProgram = gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
		DMPrograms.add(DMProgram);
		//DMEncounterTypeId = Integer.parseInt(Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.DIABETES_VISIT));
		DMEncounterTypeId=(gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT)).getEncounterTypeId();
		DDBform = gp.getForm(GlobalPropertiesManagement.DIABETES_DDB_FORM);
		DiabetesFlowVisit = gp.getForm(GlobalPropertiesManagement.DIABETES_FLOW_VISIT);
		DMEnrollmentForm=gp.getForm(GlobalPropertiesManagement.DM_ENROLL_FORM);

		DDBforms.add(DDBform);
		DDBforms.add(DiabetesFlowVisit);
		DDBforms.add(DMEnrollmentForm);

		enrollForms.add(DDBform);
		enrollForms.add(DMEnrollmentForm);

		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.POST_CARDIAC_SURGERY_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.CKD_ENCOUNTER_TYPE));
		patientsSeenEncounterTypes.add(gp.getEncounterType(GlobalPropertiesManagement.HF_HTN_CKD_ENCOUNTER_TYPE));


		onOrAfterOnOrBefore.add("onOrAfter");
		onOrAfterOnOrBefore.add("onOrBefore");
		weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		height = gp.getConcept(GlobalPropertiesManagement.HEIGHT_CONCEPT);
		diastolicBP = gp.getConcept(GlobalPropertiesManagement.DIASTOLIC_BLOOD_PRESSURE);
		systolicBP = gp.getConcept(GlobalPropertiesManagement.SYSTOLIC_BLOOD_PRESSURE);
		hbA1c = gp.getConcept(GlobalPropertiesManagement.HBA1C);
		onOrBefOnOrAf.add("onOrBef");
		onOrBefOnOrAf.add("onOrAf");
		sensationInRightFoot = gp.getConcept(GlobalPropertiesManagement.SENSATION_IN_RIGHT_FOOT);
		sensationInLeftFoot = gp.getConcept(GlobalPropertiesManagement.SENSATION_IN_LEFT_FOOT);
		
		neuropathyConcepts.add(sensationInLeftFoot);
		neuropathyConcepts.add(sensationInRightFoot);
		
		lisinopril = gp.getConcept(GlobalPropertiesManagement.LISINOPRIL);
		captopril = gp.getConcept(GlobalPropertiesManagement.CAPTOPRIL);
		
		lisinoprilCaptopril.add(lisinopril);
		lisinoprilCaptopril.add(captopril);
		
		onAceInhibitorsDrugs.addAll(RwandaReportsUtil.getDrugs(lisinopril));
		onAceInhibitorsDrugs.addAll(RwandaReportsUtil.getDrugs(captopril));
		
		creatinine = gp.getConcept(GlobalPropertiesManagement.SERUM_CREATININE);
		insulin7030 = gp.getConcept(GlobalPropertiesManagement.INSULIN_70_30);
		insulinLente = gp.getConcept(GlobalPropertiesManagement.INSULIN_LENTE);
		insulinRapide = gp.getConcept(GlobalPropertiesManagement.INSULIN_RAPIDE);
		
		insulinConcepts.add(insulin7030);
		insulinConcepts.add(insulinLente);
		insulinConcepts.add(insulinRapide);
		

		metformin = gp.getConcept(GlobalPropertiesManagement.METFORMIN_DRUG);
		glibenclimide = gp.getConcept(GlobalPropertiesManagement.GLIBENCLAMIDE_DRUG);
		
		metforminAndGlibenclimideConcepts.add(metformin);
		metforminAndGlibenclimideConcepts.add(glibenclimide);
		


		
		NCDSpecificOutcomes=gp.getConcept(GlobalPropertiesManagement.NCD_SPECIFIC_OUTCOMES);
		NCDRelatedDeathOutcomes= gp.getConcept(GlobalPropertiesManagement.NCD_RELATED_DEATH_OUTCOMES);

		exitReasonFromCare=gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);

		unknownCauseDeathOutcomes =gp.getConcept(GlobalPropertiesManagement.UNKNOWN_CAUSE_OF_DEATH_OUTCOMES);
		otherCauseOfDeathOutcomes =gp.getConcept(GlobalPropertiesManagement.OTHER_CAUSE_OF_DEATH_OUTCOMES);


		DeathOutcomeResons.add(NCDRelatedDeathOutcomes);
		DeathOutcomeResons.add(unknownCauseDeathOutcomes);
		DeathOutcomeResons.add(otherCauseOfDeathOutcomes);

		urinaryAlbumin=gp.getConcept(GlobalPropertiesManagement.URINARY_ALBUMIN);
		NCDLostToFolloUpOutCome =gp.getConcept(GlobalPropertiesManagement.LOST_TO_FOLLOWUP_OUTCOME);
		deathAndLostToFollowUpOutcomeString.append(NCDRelatedDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(unknownCauseDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(otherCauseOfDeathOutcomes.getConceptId());
		deathAndLostToFollowUpOutcomeString.append(",");
		deathAndLostToFollowUpOutcomeString.append(NCDLostToFolloUpOutCome.getConceptId());


	}
}
