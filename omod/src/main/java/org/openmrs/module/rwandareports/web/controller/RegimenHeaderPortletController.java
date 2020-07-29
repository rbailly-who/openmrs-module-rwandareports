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
package org.openmrs.module.rwandareports.web.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.orderextension.DrugRegimen;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.web.controller.PortletController;
import org.springframework.stereotype.Controller;

/**
 * The main controller.
 */
@Controller
public class RegimenHeaderPortletController extends PortletController {
	
	GlobalPropertiesManagement gp = new GlobalPropertiesManagement();
	/**
	 * @see PortletController#populateModel(HttpServletRequest, Map)
	 */
	@Override
	protected void populateModel(HttpServletRequest request, Map<String, Object> model) {

		List<Concept> iv = gp.getConceptList(GlobalPropertiesManagement.IV_CONCEPT);
		Concept chemotherapy = gp.getConcept(GlobalPropertiesManagement.CHEMOTHERAPY);
		Patient patient = Context.getPatientService().getPatient((Integer) model.get("patientId"));
    	
		List<DrugOrder> allDrugOrders = Context.getOrderService().getDrugOrdersByPatient(patient);
		List<DrugRegimen> regimens = new ArrayList<DrugRegimen>();
		List<DrugRegimen> allRegimens = new ArrayList<DrugRegimen>();
		
		Calendar compareDate = Calendar.getInstance();
		compareDate.add(Calendar.DAY_OF_YEAR, -7);
		

		
		if(regimens.size() > 0)
		{
			List<RegimenDTO> regimenDTOs = new ArrayList<RegimenDTO>(); 
			for(DrugRegimen reg: regimens)
			{
				RegimenDTO dto = new RegimenDTO();
				dto.setDrugRegimen(reg);
				dto.setStartDates(getRegimenCycleDays(reg));
				regimenDTOs.add(dto);
			}
			model.put("regimens", regimenDTOs);
		}

		
		if(allRegimens.size() > 0)
		{
			List<RegimenDTO> regimenDTOs = new ArrayList<RegimenDTO>(); 
			for(DrugRegimen reg: allRegimens)
			{
				RegimenDTO dto = new RegimenDTO();
				dto.setDrugRegimen(reg);
				dto.setStartDates(getRegimenCycleDays(reg));
				regimenDTOs.add(dto);
			}
			model.put("allRegimens", regimenDTOs);
		}
		
    	model.put("patient", patient);
	}
	
	private List<StartDateDTO> getRegimenCycleDays(DrugRegimen regimen)
	{
		List<Concept> iv = gp.getConceptList(GlobalPropertiesManagement.IV_CONCEPT);
		
		Set<Date> ivDates = new HashSet<Date>();


		List<StartDateDTO> cycleDays = new ArrayList<StartDateDTO>();

		return cycleDays;
	}
}
