package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.DataEntryDelayDataSetDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

public class SetupDataEntryDelayReport {
	
	protected final static Log log = LogFactory.getLog(SetupDataEntryDelayReport.class);
	
	private GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	public void setup() throws Exception {
		
		ReportDefinition rd = createReportDefinition();
		ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "DataEntryDelay.xls", "XlsDataEntryDelay",
		    null);
		
		createDataSetDefinition(rd);
		
		Helper.saveReportDefinition(rd);
		
		Properties props = new Properties();
		props.put(
		    "repeatingSections",
		    "sheet:1,dataset:dataSet|sheet:1,row:9,dataset:summary|sheet:1,row:15,dataset:Adult HIV|sheet:1,row:21,dataset:Pediatric HIV|sheet:1,row:27,dataset:ASTHMA VISIT|sheet:1,row:33,dataset:DIABETES VISIT|sheet:1,row:40,dataset:EPILEPSY VISIT|sheet:1,row:46,dataset:HEART FAILURE VISIT|sheet:1,row:52,dataset:HYPERTENSION VISIT|sheet:1,row:58,dataset:Inpatient Oncology|sheet:1,row:64,dataset:Outpatient Oncology|sheet:1,row:70,dataset:PDC Visit|sheet:1,row:76,dataset:Exposed Infant|sheet:1,row:82,dataset:POST CARDIAC SURGERY VISIT|sheet:1,row:82,dataset:POST CARDIAC SURGERY VISIT|sheet:1,row:88,dataset:HF HTN CKD|sheet:1,row:94,dataset:CHRONIC KIDNEY DISEASE VISIT|sheet:1,row:100,dataset:Mental Health visit|sheet:1,row:106,dataset:Diagnosis|sheet:1,row:112,dataset:LAB TEST|sheet:1,row:118,dataset:PHARMACY|sheet:1,row:124,dataset:delayCases");
		
	
		props.put("sortWeight","5000");
		design.setProperties(props);
		Helper.saveReportDesign(design);
		
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("XlsDataEntryDelay".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("DQ-Data Entry Delay Report");
	}
	
	private ReportDefinition createReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("DQ-Data Entry Delay Report");

		Properties properties = new Properties();
		properties.setProperty("hierarchyFields", "countyDistrict:District");
		reportDefinition.addParameter(new Parameter("location", "Location", AllLocation.class, properties));
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		
		return reportDefinition;
	}
	
	private void createDataSetDefinition(ReportDefinition rd) {
		
		DataEntryDelayDataSetDefinition dataEntryDelay = new DataEntryDelayDataSetDefinition();
		dataEntryDelay.addParameter(new Parameter("startDate", "Start Date", Date.class));
		dataEntryDelay.addParameter(new Parameter("endDate", "End Date", Date.class));
		dataEntryDelay.addParameter(new Parameter("location", "District", LocationHierarchy.class));
		
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.ADULT_FLOWSHEET_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.PEDI_FLOWSHEET_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.EPILEPSY_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.INPATIENT_ONCOLOGY_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.OUTPATIENT_ONCOLOGY_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.PDC_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.EXPOSED_INFANT_ENCOUNTER));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.POST_CARDIAC_SURGERY_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.HF_HTN_CKD_ENCOUNTER_TYPE));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.CKD_ENCOUNTER_TYPE));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.MENTAL_HEALTH_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.OPD_VISIT));
		dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.LAB_ENCOUNTER_TYPE));
        dataEntryDelay.addEncounterType(gp.getEncounterType(GlobalPropertiesManagement.PHARMACY_VISIT));



        Map<String, Object> mappings = new HashMap<String, Object>();
		mappings.put("location", "${location}");
		mappings.put("endDate", "${endDate+1d}");
		mappings.put("startDate", "${startDate}");
		
		rd.addDataSetDefinition("dataSet", dataEntryDelay, mappings);
	}
}
