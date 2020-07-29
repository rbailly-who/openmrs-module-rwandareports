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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.rwandareports.dataset.DrugOrderDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.DrugOrderTotalDataSetDefinition;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;

/**
 * The logic that evaluates a {@link EncounterDataSetDefinition} and produces an {@link DataSet}
 */
@Handler(supports = DrugOrderTotalDataSetDefinition.class)
public class DrugOrderTotalDataSetEvaluator implements DataSetEvaluator {
	
	protected Log log = LogFactory.getLog(this.getClass());
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	
	/**
	 * Public constructor
	 */
	public DrugOrderTotalDataSetEvaluator() {
	}
	
	/**
	 * @see DataSetEvaluator#evaluate(DataSetDefinition, EvaluationContext)
	 */
	@SuppressWarnings("unchecked")
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		
		Concept bsa = gp.getConcept(GlobalPropertiesManagement.BSA_CONCEPT);
		Concept weight = gp.getConcept(GlobalPropertiesManagement.WEIGHT_CONCEPT);
		
		Concept oral = gp.getConcept(GlobalPropertiesManagement.ORAL_ROUTE);
		
		Concept tabs = gp.getConcept(GlobalPropertiesManagement.TABLET_FORM);
		
		Map<Concept, Double> vialSizes = gp.getVialSizes();
		
		Cohort cohort = context.getBaseCohort();
		
		SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
		
		DataSetColumn drug = new DataSetColumn("drug", "drug", String.class);
		dataSet.getMetaData().addColumn(drug);
		
		DataSetColumn dose = new DataSetColumn("dose", "dose", String.class);
		dataSet.getMetaData().addColumn(dose);
		
		DataSetColumn route = new DataSetColumn("route", "route", String.class);
		dataSet.getMetaData().addColumn(route);
		
		DataSetColumn vial = new DataSetColumn("vial", "vial", String.class);
		dataSet.getMetaData().addColumn(vial);
		
		DrugOrderTotalDataSetDefinition drugDSD = (DrugOrderTotalDataSetDefinition) dataSetDefinition;
		
		if (cohort != null) {
			DecimalFormat f = new DecimalFormat("0.#");
			
			Map<Drug, Double> drugTotal = new HashMap<Drug, Double>();
			for (Integer pId : cohort.getMemberIds()) {
				Patient patient = Context.getPatientService().getPatient(pId);
				List<DrugOrder> allDrugOrders = Context.getOrderService().getDrugOrdersByPatient(patient);

			}
			Map<Concept, DrugTotalPOJO> combinedTotals = new HashMap<Concept, DrugTotalPOJO>();
			for (Drug d : drugTotal.keySet()) {
				if (d.getRoute().equals(oral)) {
					
					String doseInfo = f.format(drugTotal.get(d));
					if (d.getRoute() != null && d.getRoute().equals(oral)) {
						doseInfo = doseInfo + " x " + f.format(d.getDoseStrength());
					}
					if (d.getUnits() != null && d.getUnits().indexOf("/") > 0) {
						doseInfo = doseInfo + d.getUnits().substring(0, d.getUnits().indexOf("/"));
					} else if (d.getUnits().contains("AUC")) {
						doseInfo = "";
					} else if (d.getUnits() != null) {
						doseInfo = doseInfo + d.getUnits();
					}
					
					String vials = "";
					
					
					dataSet.addColumnValue(d.getId(), drug, d.getName());
					dataSet.addColumnValue(d.getId(), dose, doseInfo);
					dataSet.addColumnValue(d.getId(), vial, vials);
					dataSet.addColumnValue(d.getId(), route, d.getRoute().getDisplayString());
				}
				else {
					
					if(combinedTotals.containsKey(d.getConcept()))
					{
						DrugTotalPOJO drugT = combinedTotals.get(d.getConcept());
						Double newTotal = drugT.getDose() + drugTotal.get(d);
						drugT.setDose(newTotal);
					}
					else
					{
						DrugTotalPOJO drugT = new DrugTotalPOJO();
						drugT.setDose(drugTotal.get(d));
						
						String drugName = d.getName().substring(0, d.getName().indexOf("("));
						drugT.setName(drugName);
						
						drugT.setRoute(d.getRoute());
						drugT.setUnits(d.getUnits());
						
						combinedTotals.put(d.getConcept(), drugT);
					}
				}
			}
			for (Concept c : combinedTotals.keySet()) {
				DrugTotalPOJO drugT = combinedTotals.get(c);
				
				String doseInfo = f.format(drugT.getDose());
				
				if (drugT.getUnits() != null && drugT.getUnits().indexOf("/") > 0) {
					doseInfo = doseInfo + drugT.getUnits().substring(0, drugT.getUnits().indexOf("/"));
				} else if (drugT.getUnits().contains("AUC")) {
					doseInfo = "";
				} else if (drugT.getUnits() != null) {
					doseInfo = doseInfo + drugT.getUnits();
				}
				
				String vials = "";
				if (vialSizes.containsKey(c)) {
					Double v = drugT.getDose() / vialSizes.get(c);
					v = Math.ceil(v);
					
					vials = f.format(v);
				}
				
				dataSet.addColumnValue(c.getId(), drug, drugT.getName());
				dataSet.addColumnValue(c.getId(), dose, doseInfo);
				dataSet.addColumnValue(c.getId(), vial, vials);
				dataSet.addColumnValue(c.getId(), route, drugT.getRoute().getDisplayString());
				
			}
		}
		return dataSet;
	}
}
