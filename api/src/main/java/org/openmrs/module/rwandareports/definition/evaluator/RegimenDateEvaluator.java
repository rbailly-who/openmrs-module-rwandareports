package org.openmrs.module.rwandareports.definition.evaluator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.orderextension.api.OrderExtensionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rwandareports.definition.RegimenDateInformation;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

@Handler(supports = { RegimenDateInformation.class })
public class RegimenDateEvaluator implements RowPerPatientDataEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {
		
		DateResult par = new DateResult(patientData, context);
		
		GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
		List<Concept> iv = gp.getConceptList(GlobalPropertiesManagement.IV_CONCEPT);
		
		RegimenDateInformation pd = (RegimenDateInformation) patientData;
		par.setFormat(pd.getDateFormat());
		
		DrugRegimen regimen = null;
		
		if (pd.getRegimen() != null) {
			Integer regimenId = Integer.parseInt(pd.getRegimen());
			regimen = Context.getService(OrderExtensionService.class).getDrugRegimen(regimenId);
		}

		return par;
	}
}
