package org.openmrs.module.rwandareports.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.DateResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rwandareports.definition.DateOfFirstOrLastDrugOrderRestrictedByConcept;

import java.util.Date;

@Handler(supports={DateOfFirstOrLastDrugOrderRestrictedByConcept.class})
public class DateOfFirstOrLastOrdersetsRestrictedByConceptEvaluator implements RowPerPatientDataEvaluator {

    protected Log log = LogFactory.getLog(this.getClass());

    public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {

        DateResult par = new DateResult(patientData, context);
        DateOfFirstOrLastDrugOrderRestrictedByConcept pd = (DateOfFirstOrLastDrugOrderRestrictedByConcept)patientData;
        Date startDate = pd.getStartDate();
        Date endDate = pd.getEndDate();
        if(pd.getStartDate() == null)
            startDate = (Date)context.getParameterValue("startDate");
        if(pd.getEndDate() == null)
            endDate = (Date)context.getParameterValue("endDate");
        par.setFormat(pd.getDateFormat());

        return par;
    }
}
