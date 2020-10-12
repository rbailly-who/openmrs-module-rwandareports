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
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.common.SortCriteria.SortDirection;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.CustomCalculationBasedOnMultiplePatientDataDefinitions;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff;
import org.openmrs.module.rowperpatientreports.patientdata.definition.DateDiff.DateDiffType;
import org.openmrs.module.rwandareports.customcalculator.DiabetesAlerts;
import org.openmrs.module.rwandareports.customcalculator.OnInsulin;
import org.openmrs.module.rwandareports.filter.AccompagnateurDisplayFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageCurrentFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

public class SetupDiabetesConsultAndLTFU {
	protected final static Log log = LogFactory.getLog(SetupDiabetesConsultAndLTFU.class);
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	private List<Form> diabetesRendezvousForms = new ArrayList<Form>(); //Diabetes forms which can be used to set a next return visit date
	
	//properties retrieved from global variables
	private Program diabetesProgram;
	private List<EncounterType> diabetesEncouters;
	private ProgramWorkflow homeGlucometerStudyWorkflow;

	private ProgramWorkflowState inStudyState;
	private ProgramWorkflowState notInstudy;

	private RelationshipType HBCP;
		
	public void setup() throws Exception {
		setupPrograms();
		
		ReportDefinition consultReportDefinition = createConsultReportDefinition();	
		ReportDefinition ltfuReportDefinition = createLTFUReportDefinition();
		
		ReportDesign consultReporDesign = Helper.createRowPerPatientXlsOverviewReportDesign(consultReportDefinition, "DiabetesConsultSheet.xls","DiabetesConsultSheet.xls_", null);
		ReportDesign ltfuReporDesign = Helper.createRowPerPatientXlsOverviewReportDesign(ltfuReportDefinition, "DiabetesLTFUSheet.xls","DiabetesLTFUSheet.xls_", null);	
		
		Properties consultProps = new Properties();
		consultProps.put("repeatingSections", "sheet:1,row:9,dataset:dataset1");
		consultProps.put("sortWeight","5000");
		
		Properties ltfuProps = new Properties();
		ltfuProps.put("repeatingSections", "sheet:1,row:8,dataset:dataset2");
		ltfuProps.put("sortWeight","5000");
		
		consultReporDesign.setProperties(consultProps);
		ltfuReporDesign.setProperties(ltfuProps);
		
		Helper.saveReportDesign(consultReporDesign);
		Helper.saveReportDesign(ltfuReporDesign);
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("DiabetesConsultSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
			if ("DiabetesLTFUSheet.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("NCD-Diabetes Consultation Sheet");
		Helper.purgeReportDefinition("NCD-Diabetes Late Visit");
		
	}
	
	private ReportDefinition createConsultReportDefinition() {

		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Diabetes Consultation Sheet");	
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		reportDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
			    ParameterizableUtil.createParameterMappings("location=${location}"));

		createConsultDataSetDefinition (reportDefinition,diabetesProgram);	
		Helper.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}
	
	private ReportDefinition createLTFUReportDefinition() {

		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("NCD-Diabetes Late Visit");	
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));	
		reportDefinition.addParameter(new Parameter("endDate", "Date", Date.class));

		reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
				ParameterizableUtil.createParameterMappings("location=${location}"));

		createLTFUDataSetDefinition(reportDefinition,diabetesProgram);	

		Helper.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}
	
	private void createConsultDataSetDefinition(ReportDefinition reportDefinition,Program program) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Diabetes Consult Dataset");
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		//Add filters
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName()+"Cohort", program), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		
		dataSetDefinition.addFilter(Cohorts.getMondayToSundayPatientReturnVisit(diabetesRendezvousForms), ParameterizableUtil.createParameterMappings("end=${endDate+6d},start=${endDate}"));
				
		//Add Columns
         
        addCommonColumns(dataSetDefinition);
        
		dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientCurrentlyActiveOnDrugOrder("Regimen", null),
				new HashMap<String, Object>());
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Has accompagnateur", new AccompagnateurDisplayFilter()), new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP",HBCP.getRelationshipTypeId(),"A",null), new HashMap<String, Object>());
		dataSetDefinition.addColumn(RowPerPatientColumns.getStateOfPatient("SMBG", diabetesProgram, homeGlucometerStudyWorkflow, null),new HashMap<String, Object>());

		//Calculation definitions
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		alert.setName("alert");
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentHbA1c("RecentHbA1c", "dd-MMM-yy"), new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentCreatinine("RecentCreatinine", "@ddMMMyy"), new HashMap<String, Object>());
		alert.addPatientDataToBeEvaluated(RowPerPatientColumns.getMostRecentSBP("RecentSBP", "dd-MMM-yy"), new HashMap<String, Object>());
		alert.setCalculator(new DiabetesAlerts());
		alert.addParameter(new Parameter("endDate","endDate",Date.class));
		dataSetDefinition.addColumn(alert,ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
				
		CustomCalculationBasedOnMultiplePatientDataDefinitions onInsulin = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
		onInsulin.addPatientDataToBeEvaluated(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());
		onInsulin.setName("onInsuline");
		onInsulin.setCalculator(new OnInsulin(diabetesEncouters));
		dataSetDefinition.addColumn(onInsulin, new HashMap<String, Object>());
								
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataset1", dataSetDefinition, mappings);
		
		
	}
	
	private void createLTFUDataSetDefinition(ReportDefinition reportDefinition,Program program) {
		// Create new dataset definition 
		RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
		dataSetDefinition.setName("Diabetes LTFU");
		dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
		dataSetDefinition.addParameter(new Parameter("endDate", "enDate", Date.class));
		
		SortCriteria sortCriteria = new SortCriteria();
		sortCriteria.addSortElement("nextRDV", SortDirection.ASC);
		dataSetDefinition.setSortCriteria(sortCriteria);
		
		//Add filters (patients enrolled in the diabetes program)
		dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate(program.getName()+"Cohort", program), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));
		//patients with late visits
		dataSetDefinition.addFilter(Cohorts.getPatientsWithLateVisitBasedOnReturnDateConcept("patientsWithoutDiabetesEncounters", gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT)),ParameterizableUtil.createParameterMappings("endDate=${endDate-7d}"));	
		
		//Add Columns
		addCommonColumns(dataSetDefinition);
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextRDV", "yyyy/MM/dd", null), new HashMap<String, Object>());
		
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPatientPhoneNumber("Phone Number",null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("Date of missed appointment", null), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("Address", true, true, true, true), new HashMap<String, Object>());
						
		dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName", new AccompagnateurDisplayFilter()), new HashMap<String, Object>());

		dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP",HBCP.getRelationshipTypeId(),"A",null), new HashMap<String, Object>());


		DateDiff daysSinceLastVisit = RowPerPatientColumns.getDifferenceSinceLastEncounter("Days since last Visit", diabetesEncouters, DateDiffType.DAYS);
			daysSinceLastVisit.addParameter(new Parameter("endDate", "endDate", Date.class));
		
		dataSetDefinition.addColumn(daysSinceLastVisit, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));
		
		Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate}");
		
		reportDefinition.addDataSetDefinition("dataset2", dataSetDefinition, mappings);
		
	}


	private void setupPrograms() {
		//diabetesRendezvousForms.add(gp.getForm(GlobalPropertiesManagement.DIABETES_DDB_FORM)) ;
		//diabetesRendezvousForms.add(gp.getForm(GlobalPropertiesManagement.DIABETES_RDV_FORM)) ;
		//diabetesRendezvousForms.add(gp.getForm(GlobalPropertiesManagement.NCD_FOLLOWUP_FORM));

		diabetesRendezvousForms=gp.getFormList(GlobalPropertiesManagement.DIABETES_DDB_FLOW_VISIT);

		diabetesProgram = gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
		diabetesEncouters = gp.getEncounterTypeList(GlobalPropertiesManagement.DIABETES_VISIT);

		HBCP=gp.getRelationshipType(GlobalPropertiesManagement.HBCP_RELATIONSHIP);

		homeGlucometerStudyWorkflow = gp.getProgramWorkflow(GlobalPropertiesManagement.HOMEGLUCOMETERSTUDYWORKFLOW,GlobalPropertiesManagement.DM_PROGRAM);

		inStudyState = gp.getProgramWorkflowState(GlobalPropertiesManagement.INSTUDYSTATE,GlobalPropertiesManagement.HOMEGLUCOMETERSTUDYWORKFLOW,GlobalPropertiesManagement.DM_PROGRAM);

		notInstudy = gp.getProgramWorkflowState(GlobalPropertiesManagement.NOTINSTUDY,GlobalPropertiesManagement.HOMEGLUCOMETERSTUDYWORKFLOW,GlobalPropertiesManagement.DM_PROGRAM);


	}
	
	//Add common columns for the two datasets
	private void addCommonColumns(RowPerPatientDataSetDefinition dataSetDefinition){
		
        dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());
     	
     	dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());
     	
		dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentHbA1c("RecentHbA1c", "@ddMMMyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentCreatinine("RecentCreatinine", "@ddMMMyy"), new HashMap<String, Object>());
		
		dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", "dd-MMM-yyyy", null), new HashMap<String, Object>());


	}
	
}
