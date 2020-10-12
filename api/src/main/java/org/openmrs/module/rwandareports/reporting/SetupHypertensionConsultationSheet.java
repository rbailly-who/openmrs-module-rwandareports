package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.SortCriteria.SortDirection;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.MostRecentObservation;
import org.openmrs.module.rwandareports.customcalculator.HypertensionAlerts;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageCurrentFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupHypertensionConsultationSheet {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program hypertensionProgram;
	
	/*private Form rendevousForm;

	private Form hypertensionDDBForm;

	private Form followUpForm;
	
	private Concept systolicBP;

	private Concept diastolicBP;
	*/
	private List<Form> DDBAndRendezvousForms=new ArrayList<Form>();

	List<EncounterType> hypertensionEncounter;

	private RelationshipType HBCP;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "HypertensionConsultationSheet.xls",
		    "HypertensionConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:7,dataset:dataSet");
		props.put("sortWeight","5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("HypertensionConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Hypertension Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Hypertension Consultation Sheet");
				
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),ParameterizableUtil.createParameterMappings("location=${location}"));
		reportDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		createDataSetDefinition(reportDefinition);
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition reportDefinition) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Hypertension Consultation Data Set");
		
		SortCriteria sortCriteria = new SortCriteria();
		sortCriteria.addSortElement("nextRDV", SortDirection.ASC);
		dataSetDefinition.setSortCriteria(sortCriteria);
		
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in " + hypertensionProgram.getName(), hypertensionProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		dataSetDefinition.addFilter(Cohorts.getMondayToSundayPatientReturnVisit(DDBAndRendezvousForms), ParameterizableUtil.createParameterMappings("end=${endDate+6d},start=${endDate}"));
		
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");
		
		//Add Columns
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextRDV", "yyyy/MM/dd", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", null, dateFilter), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());		
		
		MostRecentObservation systolic = RowPerPatientColumns.getMostRecentSystolicPB("systolic", "dd-MMM-yy");
		dataSetDefinition.addColumn(systolic, new HashMap<String, Object>());

		MostRecentObservation diastolic = RowPerPatientColumns.getMostRecentDiastolicPB("diastolic", "dd-MMM-yy");
		dataSetDefinition.addColumn(diastolic, new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientCurrentlyActiveOnDrugOrder("Regimen", null),
				new HashMap<String, Object>());
			
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"), new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP",HBCP.getRelationshipTypeId(),"A",null), new HashMap<String, Object>());


		//AllObservationValues allSystolicBP = RowPerPatientColumns.getAllObservationValues("systolicLastTwo", systolicBP, null, new LastTwoObsFilter(),
		//    null);
		
		//AllObservationValues allDiastolicBP = RowPerPatientColumns.getAllObservationValues("diastolicLastTwo", diastolicBP, null, new LastTwoObsFilter(),
		//    null);
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(systolic, new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(diastolic, new HashMap<String, Object>());
		//alert.addPatientDataToBeEvaluated(allSystolicBP, new HashMap<String, Object>());
		//alert.addPatientDataToBeEvaluated(allDiastolicBP, new HashMap<String, Object>());
		alert.setCalculator(new HypertensionAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());	
		
	
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		hypertensionProgram = gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM);
		
		/*rendevousForm=gp.getForm(GlobalPropertiesManagement.HYPERTENSION_FLOW_VISIT);
		
		hypertensionDDBForm=gp.getForm(GlobalPropertiesManagement.HYPERTENSION_DDB);
		
		followUpForm=gp.getForm(GlobalPropertiesManagement.NCD_FOLLOWUP_FORM);
		
		systolicBP = gp.getConcept(GlobalPropertiesManagement.SYSTOLIC_BLOOD_PRESSURE);
		diastolicBP = gp.getConcept(GlobalPropertiesManagement.DIASTOLIC_BLOOD_PRESSURE);*/
		
		//DDBAndRendezvousForms.add(rendevousForm);
		//DDBAndRendezvousForms.add(hypertensionDDBForm);
		//DDBAndRendezvousForms.add(followUpForm);
		DDBAndRendezvousForms=gp.getFormList(GlobalPropertiesManagement.HYPERTENSION_DDB_FLOW_VISIT);
		hypertensionEncounter = gp.getEncounterTypeList(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER);

		HBCP=gp.getRelationshipType(GlobalPropertiesManagement.HBCP_RELATIONSHIP);
		
	}
}
