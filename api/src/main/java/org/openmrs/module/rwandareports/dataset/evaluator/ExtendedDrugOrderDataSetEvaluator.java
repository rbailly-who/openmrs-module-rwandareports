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
package org.openmrs.module.rwandareports.dataset.evaluator;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rwandareports.dataset.ExtendedDrugOrderDataSetDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;


/**
 * The logic that evaluates a {@link EncounterDataSetDefinition} and produces an {@link DataSet}
 */
@Handler(supports = ExtendedDrugOrderDataSetDefinition.class)
public class ExtendedDrugOrderDataSetEvaluator implements DataSetEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	/**
	 * Public constructor
	 */
	public ExtendedDrugOrderDataSetEvaluator() {
	}
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 */
	@SuppressWarnings("unchecked")
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		Concept bsa = gp.getConcept(GlobalPropertiesManagement.BSA_CONCEPT);
		Concept weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		
		ExtendedDrugOrderDataSetDefinition dsd = (ExtendedDrugOrderDataSetDefinition) dataSetDefinition;
		context = ObjectUtil.nvl(context, new EvaluationContext());
		
		SimpleDataSet dataSet = new SimpleDataSet(dsd, context);

			
			DataSetColumn start = new DataSetColumn("startDate", "startDate", Date.class);
			dataSet.getMetaData().addColumn(start);
			
			/*DataSetColumn discoDate = new DataSetColumn("discoDate", "discoDate", Date.class);
			dataSet.getMetaData().addColumn(discoDate);	
			*/
			/*DataSetColumn discoReasonAndDate = new DataSetColumn("discoReasonAndDate", "discoReasonAndDate", String.class);
			dataSet.getMetaData().addColumn(discoReasonAndDate);	
			*/
			DataSetColumn drug = new DataSetColumn("drug", "drug", String.class);
			dataSet.getMetaData().addColumn(drug);
			
			DataSetColumn doseReduction = new DataSetColumn("doseReduction", "doseReduction", String.class);
			dataSet.getMetaData().addColumn(doseReduction);
			
			DataSetColumn dose = new DataSetColumn("dose", "dose", String.class);
			dataSet.getMetaData().addColumn(dose);
			
			DataSetColumn actualDose = new DataSetColumn("actualDose", "actualDose", String.class);
			dataSet.getMetaData().addColumn(actualDose);
			
			DataSetColumn route = new DataSetColumn("route", "route", String.class);
			dataSet.getMetaData().addColumn(route);
			
			DataSetColumn infInst = new DataSetColumn("infusionInstructions", "infusionInstructions", String.class);
			dataSet.getMetaData().addColumn(infInst);
			
			DataSetColumn freq = new DataSetColumn("frequency", "frequency", String.class);
			dataSet.getMetaData().addColumn(freq);
			
			DataSetColumn instructions = new DataSetColumn("instructions", "instructions", String.class);
			dataSet.getMetaData().addColumn(instructions);
			
			DataSetColumn indication = new DataSetColumn("indication", "indication", String.class);
			dataSet.getMetaData().addColumn(indication);
			
			DataSetColumn discontuedReason = new DataSetColumn("discontuedReason", "discontuedReason", String.class);
			dataSet.getMetaData().addColumn(discontuedReason);
			
			

		return dataSet;
	}
	
	private int calculateDaysDifference(Date observation, Date startingDate) {
		long milis1 = observation.getTime();
		long milis2 = startingDate.getTime();
		
		long diff = milis1 - milis2;
		
		long diffDays = diff / (24 * 60 * 60 * 1000);
		
		return (int) diffDays + 1;
	}
}
