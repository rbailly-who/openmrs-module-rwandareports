package org.openmrs.module.rwandareports.definition.evaluator;


import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;

import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.definition.DrugRegimenInformation;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

@Handler(supports = { DrugRegimenInformation.class })
public class DrugRegimenInformationEvaluator implements RowPerPatientDataEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {
		
		StringResult par = new StringResult(patientData, context);
		DrugRegimenInformation pd = (DrugRegimenInformation) patientData;
		
		GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
		List<Concept> iv = gp.getConceptList(GlobalPropertiesManagement.IV_CONCEPT);


		
		if (pd.getRegimen() != null) {
			String[] regimenInfo = pd.getRegimen().split(":");
			Integer regimenId = Integer.parseInt(regimenInfo[0]);
			
			if (regimenInfo.length > 1) {
				Integer regOffset = Integer.parseInt(regimenInfo[1]);
				Calendar offset = Calendar.getInstance();
				offset.add(Calendar.DAY_OF_YEAR, regOffset);
				pd.setAsOfDate(offset.getTime());
				pd.setUntilDate(offset.getTime());
				pd.setShowStartDate(true);
			}
		}

		return par;
	}
}
