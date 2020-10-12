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
import org.openmrs.module.rowperpatientreports.patientdata.definition.AllObservationValues;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rwandareports.customcalculator.AsthmaClassificationAlerts;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageCurrentFilter;
import org.openmrs.module.rwandareports.filter.LastTwoObsFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupAsthmaConsultationSheet {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	//properties retrieved from global variables
	private Program asthmaProgram;
	
	List<EncounterType> asthmaEncounter;
	private Form rendevousForm;
	private Form asthmaDDBForm;
	private Form followUpForm;

	private RelationshipType HBCP;


	//private Concept returnVisitDate;
	
	private List<Form> DDBAndRendezvousForms=new ArrayList<Form>();
	
	public void setup() throws Exception {
		
		setupProperties();
		
		ReportDefinition rd = createReportDefinition();
		
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "AsthmaConsultationSheet.xls",
		    "AsthmaConsultationSheet.xls_", null);
		
		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
		props.put("sortWeight","5000");
		design.setProperties(props);
		
		Helper.saveReportDesign(design);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("AsthmaConsultationSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Asthma Consultation Sheet");
	}
	
	private ReportDefinition createReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Asthma Consultation Sheet");
				
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
		dataSetDefinition.setName("Asthma Consultation Data Set");
		
		SortCriteria sortCriteria = new SortCriteria();
		sortCriteria.addSortElement("nextRDV", SortDirection.ASC);
		dataSetDefinition.setSortCriteria(sortCriteria);
		
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+asthmaProgram.getName(), asthmaProgram), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		dataSetDefinition.addFilter(Cohorts.getMondayToSundayPatientReturnVisit(DDBAndRendezvousForms), ParameterizableUtil.createParameterMappings("end=${endDate+6d},start=${endDate}"));
		
		//dataSetDefinition.addFilter(getMondayToSundayPatientReturnVisit(), ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		
		DateFormatFilter dateFilter = new DateFormatFilter();
		dateFilter.setFinalDateFormat("dd-MMM-yyyy");		
		
		
		//Add Columns
		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextRDV", "yyyy/MM/dd", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", "dd-MMM-yyyy", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPeakFlow("Last peak flow", "dd-MMM-yyyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientCurrentlyActiveOnDrugOrder("Regimen", null),
				new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"), new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP",HBCP.getRelationshipTypeId(),"A",null), new HashMap<String, Object>());


		AllObservationValues asthmaClassification = RowPerPatientColumns.getAllAsthmaClassificationValues("asthmaClassification", null, new LastTwoObsFilter(),
		    null);
		
		
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(asthmaClassification, new HashMap<String, Object>());
		alert.setCalculator(new AsthmaClassificationAlerts());
		dataSetDefinition.addColumn(alert, new HashMap<String, Object>());	
		
	
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		
		reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
	}
	
	private void setupProperties() {
		asthmaProgram = gp.getProgram(GlobalPropertiesManagement.CHRONIC_RESPIRATORY_PROGRAM);
		asthmaEncounter = gp.getEncounterTypeList(GlobalPropertiesManagement.ASTHMA_VISIT);
		rendevousForm=gp.getForm(GlobalPropertiesManagement.ASTHMA_RENDEVOUS_VISIT_FORM);
		asthmaDDBForm=gp.getForm(GlobalPropertiesManagement.ASTHMA_DDB);
		//followUpForm=gp.getForm(GlobalPropertiesManagement.NCD_FOLLOWUP_FORM);
		DDBAndRendezvousForms=gp.getFormList(GlobalPropertiesManagement.ASTHMA_DDB_RENDEVOUS_VISIT_FORMS);
		//DDBAndRendezvousForms.add(rendevousForm);
		//DDBAndRendezvousForms.add(asthmaDDBForm);
		//DDBAndRendezvousForms.add(followUpForm);
		HBCP=gp.getRelationshipType(GlobalPropertiesManagement.HBCP_RELATIONSHIP);

	}
	
	/*
	private SqlCohortDefinition getMondayToSundayPatientReturnVisit() {		
		
	    SqlCohortDefinition cohortquery=new SqlCohortDefinition();
	    //cohortquery.setQuery("select o.person_id from obs o,(select * from (select * from encounter where (form_id="+asthmaDDBFormId+" or encounter_type="+flowsheetAsthmas.getEncounterTypeId()+") order by encounter_datetime desc) as ordred_enc group by ordred_enc.patient_id) as last_enc where o.encounter_id=last_enc.encounter_id and last_enc.voided=0 and o.voided=0 and o.concept_id="+returnVisitDate.getConceptId()+" and o.value_datetime>=(select DATE_FORMAT(CURDATE()+(- (select IF(DAYOFWEEK(CURDATE())=1,6,DAYOFWEEK(CURDATE())-2) as sun)),'%Y-%m-%d')) and o.value_datetime<=(select DATE_FORMAT(CURDATE()+(- (select IF(DAYOFWEEK(CURDATE())=1,6,DAYOFWEEK(CURDATE())-2) as sun)+6),'%Y-%m-%d')) order by o.value_datetime");
	    cohortquery.setQuery("select o.person_id from obs o,(select * from (select * from encounter where (form_id="+asthmaDDBFormId+" or form_id="+rendevousForm+") order by encounter_datetime desc) as ordred_enc group by ordred_enc.patient_id) as last_enc where o.encounter_id=last_enc.encounter_id and last_enc.voided=0 and o.voided=0 and o.concept_id="+returnVisitDate.getConceptId()+" and o.value_datetime>= :start and o.value_datetime<= :end order by o.value_datetime");
	    cohortquery.addParameter(new Parameter("start","start",Date.class));
	    cohortquery.addParameter(new Parameter("end","end",Date.class));	    
	    //cohortquery.addParameter(new Parameter("endDate","endDate",Date.class));
	    return cohortquery;
    }
	*/
}
