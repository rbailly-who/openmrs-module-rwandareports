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
		 
		ReportDefinition rd =createReportDefinitionByDate();		
		ReportDesign designCSV = Helper.createCsvReportDesign(rd,"Lab - Results Report.csv_");
		Helper.saveReportDesign(designCSV);
			
		/*ReportDefinition rd2 =createReportDefinitionByLabExamAndDates();
		ReportDesign designCSV2 = Helper.createCsvReportDesign(rd2,"Lab - Results Report By Lab Exam.csv_");
		Helper.saveReportDesign(designCSV2);
		*/
		/*ReportDefinition rd3 =createReportDefinitionByProgramAndDates();
		ReportDesign designCSV3 = Helper.createCsvReportDesign(rd3,"Generic Drug Report.csv_");
		Helper.saveReportDesign(designCSV3);
			  	*/
			
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Lab - Results Report.csv_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Lab - Results Report");
	//	Helper.purgeReportDefinition("Lab - Results Report by Lab Exam");
		//Helper.purgeReportDefinition("Generic Drug Report by Dates and program");
	}
	
	private ReportDefinition createReportDefinitionByDate() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Lab - Results Report");
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));

		createDataSetDefinitionByDate(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
/*	private ReportDefinition createReportDefinitionByLabExamAndDates() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Lab - Results Report by Lab Exam");
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));

		createDataSetDefinitionByLabExamAndDates(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}*/
	/*
  private ReportDefinition createReportDefinitionByProgramAndDates() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Generic Drug Report by Dates and program");	
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));
				
		createDataSetDefinitionByProgramAndDates(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}*/
	
	private void createDataSetDefinitionByDate(ReportDefinition reportDefinition) {

		Parameter concept = new Parameter("concept", "Exam Name", Concept.class);
		concept.setRequired(false);
		reportDefinition.addParameter(concept);
		reportDefinition.addParameter(new Parameter("location", "Location:", Location.class));

		OrderType labOrder=gp.getOrderType("laboratorymanagement.orderType.labOrderTypeId");
		SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
		sqldsd.setSqlQuery("select " +
				"(select identifier from patient_identifier where patient_id=o.person_id limit 1) as 'Identifier'," +
				"(select family_name from person_name where person_id=o.person_id and voided=0 limit 1) as 'Family name'," +
				"(select given_name from person_name where person_id=o.person_id and voided=0 limit 1) as 'Given name'," +
				"(select (TIMESTAMPDIFF(YEAR,birthdate,ods.start_date)) from person where person_id=o.person_id and voided=0) as 'Age'," +
				"(select gender from person where person_id=o.person_id and voided=0) as 'Gender'," +
				"(select name from location where location_id=o.location_id) as 'Location', " +
				"ods.start_date as 'Sample date'," +
				"ods.accession_number as 'Sample Code'," +
				"o.obs_datetime as 'Date of result' ," +
				"(select name from concept_name where concept_id=ods.concept_id limit 1) as 'Name'," +
				"CONCAT_WS(',',o.value_boolean,(select name from concept_name where concept_id=o.value_coded limit 1),o.value_numeric,o.value_text) as 'Result'" +
				"from orders ods,obs o " +
				"where ods.order_id=o.order_id and ods.order_type_id="+labOrder.getOrderTypeId()+" and o.obs_datetime >= :startDate and o.obs_datetime <= :endDate and o.voided=0 and o.location_id=:location and (:concept is null or ods.concept_id=:concept) and ods.concept_id not in (select concept_set from concept_set)");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));
		sqldsd.addParameter(new Parameter("location", "Location:", Location.class));
		sqldsd.addParameter(concept);

		reportDefinition.addDataSetDefinition("dsd",Mapped.mapStraightThrough(sqldsd));


	}

/*
private void createDataSetDefinitionByLabExamAndDates(ReportDefinition reportDefinition) {

	Parameter concept = new Parameter("concept", "Exam Name", Concept.class);
	concept.setRequired(false);
	reportDefinition.addParameter(concept);
	reportDefinition.addParameter(new Parameter("location", "Location:", Location.class));

	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	OrderType labOrder=gp.getOrderType("laboratorymanagement.orderType.labOrderTypeId");
	SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
	sqldsd.setSqlQuery("select " +
			"(select identifier from patient_identifier where patient_id=o.person_id limit 1) as 'Identifier'," +
			"(select family_name from person_name where person_id=o.person_id and voided=0 limit 1) as 'Family name'," +
			"(select given_name from person_name where person_id=o.person_id and voided=0 limit 1) as 'Given name'," +
			"(select (TIMESTAMPDIFF(YEAR,birthdate,ods.start_date)) from person where person_id=o.person_id and voided=0) as 'Age'," +
			"(select gender from person where person_id=o.person_id and voided=0) as 'Gender'," +
			"(select name from location where location_id=o.location_id) as 'Location', " +
			"ods.start_date as 'Sample date'," +
			"ods.accession_number as 'Sample Code'," +
			"o.obs_datetime as 'Date of result' ," +
			"(select name from concept_name where concept_id=ods.concept_id limit 1) as 'Name'," +
			"CONCAT_WS(',',o.value_boolean,(select name from concept_name where concept_id=o.value_coded limit 1),o.value_numeric,o.value_text) as 'Result'" +
			"from orders ods,obs o " +
			"where ods.order_id=o.order_id and ods.order_type_id="+labOrder.getOrderTypeId()+" and o.obs_datetime >= :startDate and o.obs_datetime <= :endDate and o.voided=0 and o.location_id=:location and (:concept is null or ods.concept_id=:concept) and ods.concept_id not in (select concept_set from concept_set)");
	sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
	sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));
	sqldsd.addParameter(new Parameter("location", "Location:", Location.class));
	sqldsd.addParameter(concept);

	reportDefinition.addDataSetDefinition("dsd",Mapped.mapStraightThrough(sqldsd));


}
*/
/*
private void createDataSetDefinitionByProgramAndDates(ReportDefinition reportDefinition) {
	
	Parameter prog=new Parameter("programs", "Program",Program.class);
	prog.setRequired(false);
	reportDefinition.addParameter(prog);
	
	SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
		sqldsd.setSqlQuery("select o.patient_id,d.name,dro.dose,d.units,o.start_date,o.discontinued_date,o.auto_expire_date,d.route,o.voided from orders o " +
				"inner join drug_order dro on o.order_id=dro.order_id " +
				"inner join patient_program pp on o.patient_id=pp.patient_id " +
				"left join drug d on dro.drug_inventory_id=d.drug_id " +
				"left join patient P on o.patient_id=P.patient_id" +
				" where o.start_date>=:startDate and o.start_date<=:endDate and pp.program_id=:programs " +
				"and P.voided=0 and o.voided=0");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));		
		sqldsd.addParameter(prog);		
		
		
		
		reportDefinition.addDataSetDefinition("dsdprogram",Mapped.mapStraightThrough(sqldsd));
		
		
	}
*/
	
}

