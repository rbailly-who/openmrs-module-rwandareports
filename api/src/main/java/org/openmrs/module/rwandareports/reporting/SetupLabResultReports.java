package org.openmrs.module.rwandareports.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

import java.util.Date;

public class SetupLabResultReports {
		
	protected final static Log log = LogFactory.getLog(SetupLabResultReports.class);
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

		
	public void setup() throws Exception {		
		 
		ReportDefinition rd = createLabResultReportDefinition();
		ReportDesign designCSV = Helper.createCsvReportDesign(rd,"Lab - Results Report.csv_");
		Helper.saveReportDesign(designCSV);
			
		ReportDefinition rd2 =createLabExamReportDefinition();
		ReportDesign designCSV2 = Helper.createCsvReportDesign(rd2,"Lab - Exam Report Exam.csv_");
		Helper.saveReportDesign(designCSV2);

		/*ReportDefinition rd3 =createReportDefinitionByProgramAndDates();
		ReportDesign designCSV3 = Helper.createCsvReportDesign(rd3,"Generic Drug Report.csv_");
		Helper.saveReportDesign(designCSV3);
			  	*/
			
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Lab - Results Report.csv_".equals(rd.getName()) || "Lab - Exam Report Exam.csv_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Lab - Results Report");
		Helper.purgeReportDefinition("Lab - Exam Report");

		//	Helper.purgeReportDefinition("Lab - Results Report by Lab Exam");
		//Helper.purgeReportDefinition("Generic Drug Report by Dates and program");
	}
	
	private ReportDefinition createLabResultReportDefinition() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Lab - Results Report");
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));

		createLabResultDataSetDefinition(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}



	private ReportDefinition createLabExamReportDefinition() {

		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Lab - Exam Report");
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));

		createLabExamDataSetDefinition(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}





	private void createLabResultDataSetDefinition(ReportDefinition reportDefinition) {

		Parameter concept = new Parameter("concept", "Exam Name", Concept.class);
		concept.setRequired(false);
		Parameter location = new Parameter("location", "Location of result", Location.class);
		location.setRequired(false);
		/*Parameter locationOfPatient = new Parameter("location", "Location of Patients", Location.class);
		locationOfPatient.setRequired(false);*/
		reportDefinition.addParameter(location);
		//reportDefinition.addParameter(locationOfPatient);
		reportDefinition.addParameter(concept);


		//reportDefinition.addParameter(new Parameter("location", "Location of result:", Location.class));

		OrderType labOrder=gp.getOrderType("laboratorymanagement.orderType.labOrderTypeId");
		PersonAttributeType healthFacilityAttributeType=gp.getPersonAttributeType(GlobalPropertiesManagement.FACILITY_PERSON_ATTRIBUTE_TYPE_ID);

		SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
		sqldsd.setSqlQuery("select " +
				"(select identifier from patient_identifier where patient_id=o.person_id limit 1) as 'Identifier'," +
				"(select family_name from person_name where person_id=o.person_id and voided=0 limit 1) as 'Family name'," +
				"(select given_name from person_name where person_id=o.person_id and voided=0 limit 1) as 'Given name'," +
				"(select (TIMESTAMPDIFF(YEAR,birthdate,ods.start_date)) from person where person_id=o.person_id and voided=0) as 'Age'," +
				"(select gender from person where person_id=o.person_id and voided=0) as 'Gender'," +
				"(select l.name from person_attribute pa, location l where person_attribute_type_id="+healthFacilityAttributeType.getPersonAttributeTypeId()+" and  voided=0 and pa.value=l.location_id and pa.person_id=o.person_id limit 1) as 'Patient Location', " +
				"(select name from location where location_id=o.location_id) as 'Result done at', " +
				"ods.start_date as 'Sample date'," +
				"ods.accession_number as 'Sample Code'," +
				"o.obs_datetime as 'Date of result' ," +
				"(select name from concept_name where concept_id=ods.concept_id limit 1) as 'Name'," +
				"CONCAT_WS(',',o.value_boolean,(select name from concept_name where concept_id=o.value_coded limit 1),o.value_numeric,o.value_text) as 'Result'" +
				"from orders ods,obs o " +
				"where ods.order_id=o.order_id and ods.order_type_id="+labOrder.getOrderTypeId()+" and o.obs_datetime >= :startDate and o.obs_datetime <= :endDate and o.voided=0 and (:location is null or o.location_id=:location) and (:concept is null or ods.concept_id=:concept) and ods.concept_id not in (select concept_set from concept_set)");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));
		sqldsd.addParameter(location);
		sqldsd.addParameter(concept);

		reportDefinition.addDataSetDefinition("dsd",Mapped.mapStraightThrough(sqldsd));


	}

	private void createLabExamDataSetDefinition(ReportDefinition reportDefinition) {

		Parameter concept = new Parameter("concept", "Exam Name", Concept.class);
		concept.setRequired(false);
		Parameter location = new Parameter("location", "Location of Patient", Location.class);
		location.setRequired(false);
		/*Parameter locationOfPatient = new Parameter("location", "Location of Patients", Location.class);
		locationOfPatient.setRequired(false);*/
		reportDefinition.addParameter(location);
		//reportDefinition.addParameter(locationOfPatient);
		reportDefinition.addParameter(concept);


		//reportDefinition.addParameter(new Parameter("location", "Location of result:", Location.class));

		OrderType labOrder=gp.getOrderType("laboratorymanagement.orderType.labOrderTypeId");
		PersonAttributeType healthFacilityAttributeType=gp.getPersonAttributeType(GlobalPropertiesManagement.FACILITY_PERSON_ATTRIBUTE_TYPE_ID);

		SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
		sqldsd.setSqlQuery("select " +
				"(select identifier from patient_identifier where patient_id=ods.patient_id limit 1) as 'Identifier'," +
				"(select family_name from person_name where person_id=ods.patient_id and voided=0 limit 1) as 'Family name'," +
				"(select given_name from person_name where person_id=ods.patient_id and voided=0 limit 1) as 'Given name'," +
				"(select (TIMESTAMPDIFF(YEAR,birthdate,ods.start_date)) from person where person_id=ods.patient_id and voided=0) as 'Age'," +
				"(select gender from person where person_id=ods.patient_id and voided=0) as 'Gender'," +
				"(select l.name from person_attribute pa, location l where person_attribute_type_id="+healthFacilityAttributeType.getPersonAttributeTypeId()+" and  voided=0 and pa.value=l.location_id and pa.person_id=ods.patient_id limit 1) as 'Patient Location', " +
				"(select name from location where location_id=o.location_id) as 'Result done at', " +
				"ods.start_date as 'Sample date'," +
				"ods.accession_number as 'Sample Code'," +
				"o.obs_datetime as 'Date of result' ," +
				"(select name from concept_name where concept_id=ods.concept_id limit 1) as 'Name'," +
				"CONCAT_WS(',',o.value_boolean,(select name from concept_name where concept_id=o.value_coded limit 1),o.value_numeric,o.value_text) as 'Result'" +
				"from orders ods " +
				"left join obs o on o.order_id=ods.order_id and o.concept_id=ods.concept_id and o.voided=0 " +
				"where ods.order_type_id="+labOrder.getOrderTypeId()+" and ods.start_date >= :startDate and ods.start_date <= :endDate and ods.voided=0 and (:location is null or (ods.patient_id in (select person_id from person_attribute where person_attribute_type_id="+healthFacilityAttributeType.getPersonAttributeTypeId()+" and  voided=0 and value=:location ))) and (:concept is null or ods.concept_id=:concept) and ods.concept_id not in (select concept_set from concept_set)");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));
		sqldsd.addParameter(location);
		sqldsd.addParameter(concept);

		reportDefinition.addDataSetDefinition("dsd",Mapped.mapStraightThrough(sqldsd));


	}


}

