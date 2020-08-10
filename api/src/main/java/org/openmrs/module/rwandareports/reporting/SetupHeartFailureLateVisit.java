package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateOfBirthShowingEstimation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientAddress;
import org.openmrs.module.rowperpatientreports.patientdata.definition.PatientProperty;
import org.openmrs.module.rwandareports.customcalculator.DaysLate;
import org.openmrs.module.rwandareports.customcalculator.OnWarfarin;
import org.openmrs.module.rwandareports.filter.AccompagnateurDisplayFilter;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupHeartFailureLateVisit {

	protected final static Log log = LogFactory
			.getLog(SetupHeartFailureLateVisit.class);
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

	// Properties retrieved from global variables
	private Program heartFailureProgram;
	private EncounterType heartFailureVisit;
	private List<EncounterType> heartFailureEncounters;
    private Form heartFailureRDVForm;
    private Form heartFailureDDBForm;
    private Form followUpForm;
	private List<Form> DDBAndRendezvousForms = new ArrayList<Form>();
	private RelationshipType HBCP;
	public void setup() throws Exception {

		setupProperties();

		ReportDefinition rd = createReportDefinition();
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd,"HeartFailureLateVisit.xls", 
				"XlsHeartFailureLateVisit.xls_", null);

		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:8,dataset:dataSet");
		props.put("sortWeight","5000");
		design.setProperties(props);
		Helper.saveReportDesign(design);
	}

	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsHeartFailureLateVisit.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Heart Failure Late Visit");
	}

	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Heart Failure Late Visit");
		reportDefinition.addParameter(new Parameter("location", "Location",
				Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date",
				Date.class));

		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
		ParameterizableUtil.createParameterMappings("location=${location}"));

		createDataSetDefinition(reportDefinition);
		Helper.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}

	private void createDataSetDefinition(ReportDefinition reportDefinition) {

		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");

		// in PMTCT Program dataset definition
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Patients Who have missed their visit by more than a week dataSetDefinition");

		SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
		dataSetDefinition.addFilter(patientsNotVoided,new HashMap<String, Object>());

		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+ heartFailureProgram.getName(), heartFailureProgram),ParameterizableUtil.createParameterMappings("onDate=${endDate}"));

		dataSetDefinition.addFilter(Cohorts.createPatientsLateForVisitINDifferentEncounterTypes(DDBAndRendezvousForms, heartFailureEncounters), ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		  
		// ==================================================================
		// Columns of report settings
		// ==================================================================

		MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
		dataSetDefinition.addColumn(imbType, new HashMap<String, Object>());

		PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("familyName");
		dataSetDefinition.addColumn(givenName, new HashMap<String, Object>());

		PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("givenName");
		dataSetDefinition.addColumn(familyName, new HashMap<String, Object>());

		MostRecentObservation lastphonenumber = RowPerPatientColumns.getMostRecentPatientPhoneNumber("telephone", null);
		dataSetDefinition.addColumn(lastphonenumber,new HashMap<String, Object>());

		PatientProperty gender = RowPerPatientColumns.getGender("Sex");
		dataSetDefinition.addColumn(gender, new HashMap<String, Object>());

		DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
		dataSetDefinition.addColumn(birthdate, new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getNextVisitMostRecentEncounterOfTheTypes("nextVisit",heartFailureEncounters,new ObservationInMostRecentEncounterOfType(), null),
				new HashMap<String, Object>());
		
		MostRecentObservation systolic = RowPerPatientColumns.getMostRecentSystolicPB("systolic", "@ddMMMyy");
		dataSetDefinition.addColumn(systolic, new HashMap<String, Object>());
		
		MostRecentObservation diastolic = RowPerPatientColumns.getMostRecentDiastolicPB("diastolic", "@ddMMMyy");
		dataSetDefinition.addColumn(diastolic, new HashMap<String, Object>());


		CustomCalculationBasedOnMultiplePatientDataDefinitions numberofdaysLate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		numberofdaysLate.addPatientDataToBeEvaluated(RowPerPatientColumns.getNextVisitMostRecentEncounterOfTheTypes("nextVisit",heartFailureEncounters,new ObservationInMostRecentEncounterOfType(),dateFilter),new HashMap<String, Object>());
		numberofdaysLate.setName("numberofdaysLate");
		numberofdaysLate.setCalculator(new DaysLate());
		numberofdaysLate.addParameter(new Parameter("endDate","endDate",Date.class));
		dataSetDefinition.addColumn(numberofdaysLate,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getSeizureInMostRecentEncounterOfType("seizure",heartFailureEncounters,new ObservationInMostRecentEncounterOfType()),new HashMap<String, Object>());

		PatientAddress address = RowPerPatientColumns.getPatientAddress("Address", true, true, true, true);
		dataSetDefinition.addColumn(address, new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Has accompagnateur", new AccompagnateurDisplayFilter()), new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP",HBCP.getRelationshipTypeId(),"A",null), new HashMap<String, Object>());


		CustomCalculationBasedOnMultiplePatientDataDefinitions onWarfarinOrder = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		onWarfarinOrder.addPatientDataToBeEvaluated(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		onWarfarinOrder.setName("onWarfarin");
		onWarfarinOrder.setCalculator(new OnWarfarin());
		dataSetDefinition.addColumn(onWarfarinOrder, new HashMap<String, Object>());
    
		dataSetDefinition.addParameter(new Parameter("location", "Location",Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "End Date",Date.class));

		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");

		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition,mappings);

	}

	private void setupProperties() {

        heartFailureProgram = gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM_NAME);
//        heartFailureVisit = gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER);

		heartFailureEncounters = gp.getEncounterTypeList(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTERS);
		heartFailureRDVForm=gp.getForm(GlobalPropertiesManagement.HEARTFAILURE_FLOW_VISIT);
		heartFailureDDBForm=gp.getForm(GlobalPropertiesManagement.HEARTFAILURE_DDB);
		//followUpForm=gp.getForm(GlobalPropertiesManagement.NCD_FOLLOWUP_FORM);

		DDBAndRendezvousForms=gp.getFormList(GlobalPropertiesManagement.HEARTFAILURE_DDB_RDV_FORMS);


		//DDBAndRendezvousForms.add(heartFailureRDVForm);
		//DDBAndRendezvousForms.add(heartFailureDDBForm);
		//DDBAndRendezvousForms.add(followUpForm);

		HBCP=gp.getRelationshipType(GlobalPropertiesManagement.HBCP_RELATIONSHIP);
		
	}

}