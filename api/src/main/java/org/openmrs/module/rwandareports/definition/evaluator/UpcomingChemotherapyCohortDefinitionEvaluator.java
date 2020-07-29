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

import java.util.List;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.rwandareports.definition.UpcomingChemotherapyCohortDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

/**
 * 
 */
@Handler(supports={UpcomingChemotherapyCohortDefinition.class})
public class UpcomingChemotherapyCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

	/**
	 * Default Constructor
	 */
	public UpcomingChemotherapyCohortDefinitionEvaluator() {}
	
	/**
     * @see CohortDefinitionEvaluator#evaluateCohort(CohortDefinition, EvaluationContext)
     */
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {
    	UpcomingChemotherapyCohortDefinition definition = (UpcomingChemotherapyCohortDefinition) cohortDefinition;
		

    	Cohort cohort = new Cohort();
    	

    	return new EvaluatedCohort(cohort, cohortDefinition, context);
    }
}
