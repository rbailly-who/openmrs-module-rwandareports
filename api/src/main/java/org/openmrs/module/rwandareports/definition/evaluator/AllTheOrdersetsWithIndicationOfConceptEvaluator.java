package org.openmrs.module.rwandareports.definition.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rowperpatientreports.patientdata.definition.RowPerPatientData;
import org.openmrs.module.rowperpatientreports.patientdata.evaluator.RowPerPatientDataEvaluator;
import org.openmrs.module.rowperpatientreports.patientdata.result.PatientDataResult;
import org.openmrs.module.rowperpatientreports.patientdata.result.StringResult;
import org.openmrs.module.rwandareports.definition.AllTheOrdersetsWithIndicationOfConcept;

import java.util.Date;

@Handler(supports = {AllTheOrdersetsWithIndicationOfConcept.class})
public class AllTheOrdersetsWithIndicationOfConceptEvaluator implements RowPerPatientDataEvaluator {

    protected Log log = LogFactory.getLog(this.getClass());

    public PatientDataResult evaluate(RowPerPatientData patientData, EvaluationContext context) {

        StringResult par = new StringResult(patientData, context);

        AllTheOrdersetsWithIndicationOfConcept pd = (AllTheOrdersetsWithIndicationOfConcept)patientData;

        Concept IndicationConcept = pd.getIndicationConcept();
        Date beforeDate = pd.getBeforeDate();
        Date afterDate = pd.getAfterDate();


                return par;
    }


}
