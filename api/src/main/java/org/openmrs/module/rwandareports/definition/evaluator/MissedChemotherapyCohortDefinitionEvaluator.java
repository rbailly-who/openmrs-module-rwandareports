/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.rwandareports.definition.evaluator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.Form;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rwandareports.definition.MissedChemotherapyCohortDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

/**
 * 
 */
@Handler(supports = { MissedChemotherapyCohortDefinition.class })
public class MissedChemotherapyCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	/**
	 * Default Constructor
	 */
	public MissedChemotherapyCohortDefinitionEvaluator() {
	}
	
	/**
	 * @see CohortDefinitionEvaluator#evaluateCohort(CohortDefinition, EvaluationContext)
	 */
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {
		
		Form treatmentAdminForm = gp.getForm(GlobalPropertiesManagement.CHEMOTHERAPY_TREATMENT_SUMMARY_FORM);
		Form treatmentAdminFormShort = gp.getForm(GlobalPropertiesManagement.CHEMOTHERAPY_TREATMENT_SUMMARY_FORM_SHORT);
		
		List<Form> forms = new ArrayList<Form>();
		forms.add(treatmentAdminForm);
		forms.add(treatmentAdminFormShort);
		
		MissedChemotherapyCohortDefinition definition = (MissedChemotherapyCohortDefinition) cohortDefinition;
		
		Calendar from = Calendar.getInstance();
		if(definition.getBeforeDate() != null)
		{
			from.setTime(definition.getBeforeDate());
		}
		from.add(Calendar.MONTH, -6);
		

		Cohort cohort = new Cohort();

		Calendar formEndDate = Calendar.getInstance();
		formEndDate.setTime(definition.getBeforeDate());
		formEndDate.add(Calendar.DAY_OF_YEAR, 1);


		return new EvaluatedCohort(cohort, cohortDefinition, context);
	}
}
