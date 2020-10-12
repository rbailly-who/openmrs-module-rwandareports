package org.openmrs.module.rwandareports.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Program;
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
import org.openmrs.module.rowperpatientreports.patientdata.definition.ObservationInMostRecentEncounterOfType;
import org.openmrs.module.rwandareports.customcalculator.EpilepsyAlerts;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageCurrentFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupEpilepsyConsultationSheet {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program epilepsyProgram;
	private Form epilepsyrendevousForm;
	private Form epilepsyDDBForm;
	private Form followUpForm;
	private List<Form> DDBAndRendezvousForms=new ArrayList<Form>();
	private EncounterType epilepsyVisit;
	List<EncounterType> epilepsyVisitEncounter;
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "EpilepsyConsultationSheet.xls",
		    "EpilepsyConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		props.put("sortWeight","5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("EpilepsyConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Epilepsy Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Epilepsy Consultation Sheet");
				
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
		dataSetDefinition.setName("Epilepsy Consultation Data Set");
		
		SortCriteria sortCriteria = new SortCriteria();
		sortCriteria.addSortElement("nextRDV", SortDirection.ASC);
		dataSetDefinition.setSortCriteria(sortCriteria);
		
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+epilepsyProgram.getName(), epilepsyProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
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
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientCurrentlyActiveOnDrugOrder("Regimen", null),
				new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getSeizureInMostRecentEncounterOfType(
			    "seizure", epilepsyVisit, new ObservationInMostRecentEncounterOfType()), new HashMap<String, Object>());
		
		
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentSeizure("RecentSeizure", "dd-MMM-yy"), new HashMap<String, Object>());
		alert.setCalculator(new EpilepsyAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());
		
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		epilepsyProgram = gp.getProgram(GlobalPropertiesManagement.EPILEPSY_PROGRAM);
		epilepsyrendevousForm=gp.getForm(GlobalPropertiesManagement.EPILEPSY_RENDEVOUS_VISIT_FORM);
		epilepsyDDBForm=gp.getForm(GlobalPropertiesManagement.EPILEPSY_DDB);
		followUpForm=gp.getForm(GlobalPropertiesManagement.NCD_FOLLOWUP_FORM);
		DDBAndRendezvousForms.add(epilepsyrendevousForm);
		DDBAndRendezvousForms.add(epilepsyDDBForm);
		DDBAndRendezvousForms.add(followUpForm);
		epilepsyVisit = gp.getEncounterType(GlobalPropertiesManagement.EPILEPSY_VISIT);
		epilepsyVisitEncounter = gp.getEncounterTypeList(GlobalPropertiesManagement.EPILEPSY_VISIT);
	}
	
}
