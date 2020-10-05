package org.openmrs.module.rwandareports.reporting;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Drug;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.definition.SqlDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

public class SetupGenericDrugReport {
		
	protected final static Log log = LogFactory.getLog(SetupGenericDrugReport.class);
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

		
	public void setup() throws Exception {		
		 
		ReportDefinition rd =createReportDefinitionByDate();		
		ReportDesign designCSV = Helper.createCsvReportDesign(rd,"Generic Drug Report.csv_");
		Helper.saveReportDesign(designCSV);
			
		ReportDefinition rd2 =createReportDefinitionByDrugAndDates();		
		ReportDesign designCSV2 = Helper.createCsvReportDesign(rd2,"Generic Drug Report.csv_");
		Helper.saveReportDesign(designCSV2);
			
		ReportDefinition rd3 =createReportDefinitionByProgramAndDates();		
		ReportDesign designCSV3 = Helper.createCsvReportDesign(rd3,"Generic Drug Report.csv_");
		Helper.saveReportDesign(designCSV3);
			  	
			
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("Generic Drug Report.xls_".equals(rd.getName()) || "Generic Drug Report.csv_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Generic Drug Report by Dates");
		Helper.purgeReportDefinition("Generic Drug Report by Dates and drug");
		Helper.purgeReportDefinition("Generic Drug Report by Dates and program");
	}
	
	private ReportDefinition createReportDefinitionByDate() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Generic Drug Report by Dates");	
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));
				
		createDataSetDefinitionByDate(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private ReportDefinition createReportDefinitionByDrugAndDates() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Generic Drug Report by Dates and drug");	
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));
				
		createDataSetDefinitionByDrugAndDates(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
  private ReportDefinition createReportDefinitionByProgramAndDates() {
		
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Generic Drug Report by Dates and program");	
		reportDefinition.addParameter(new Parameter("startDate", "From:", Date.class));	
		reportDefinition.addParameter(new Parameter("endDate", "To:", Date.class));
				
		createDataSetDefinitionByProgramAndDates(reportDefinition);

		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	private void createDataSetDefinitionByDate(ReportDefinition reportDefinition) {
		
		SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
		sqldsd.setSqlQuery("select o.patient_id,d.name,dro.dose,d.units,o.start_date,o.discontinued_date,o.auto_expire_date,d.route,o.voided from orders o " +
				"inner join drug_order dro on o.order_id=dro.order_id " +
				"left join drug d on dro.drug_inventory_id=d.drug_id " +
				"left join patient P on o.patient_id=P.patient_id" +
				" where o.start_date>=:startDate and o.start_date<=:endDate and P.voided=0 and o.voided=0");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));		
		
		
		reportDefinition.addDataSetDefinition("dsd",Mapped.mapStraightThrough(sqldsd));
		
		
	}
	
private void createDataSetDefinitionByDrugAndDates(ReportDefinition reportDefinition) {
		
	Parameter drug = new Parameter("Drug", "Drug", Drug.class);
	drug.setRequired(false);	
	reportDefinition.addParameter(drug);	
	
	SqlDataSetDefinition sqldsd=new SqlDataSetDefinition();
		sqldsd.setSqlQuery("select o.patient_id,d.name,dro.dose,d.units,o.start_date,o.discontinued_date,o.auto_expire_date,d.route,o.voided from orders o " +
				"inner join drug_order dro on o.order_id=dro.order_id " +
				"left join drug d on dro.drug_inventory_id=d.drug_id " +
				"left join patient P on o.patient_id=P.patient_id" +
				" where o.start_date>=:startDate and o.start_date<=:endDate and d.drug_id=:Drug " +
				"and P.voided=0 and o.voided=0");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));		
		sqldsd.addParameter(drug);		
		
		
		
		reportDefinition.addDataSetDefinition("dsddrug",Mapped.mapStraightThrough(sqldsd));
		
		
	}

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
				"and P.voided=0 and o.voided=0 and pp.voided=0");
		sqldsd.addParameter(new Parameter("startDate", "From:", Date.class));
		sqldsd.addParameter(new Parameter("endDate", "To:", Date.class));		
		sqldsd.addParameter(prog);		
		
		
		
		reportDefinition.addDataSetDefinition("dsdprogram",Mapped.mapStraightThrough(sqldsd));
		
		
	}

	
}

