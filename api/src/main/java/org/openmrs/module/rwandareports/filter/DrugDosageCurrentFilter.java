package org.openmrs.module.rwandareports.filter;


import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.rowperpatientreports.patientdata.definition.ResultFilter;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;


public class DrugDosageCurrentFilter implements ResultFilter {
	
	private String finalDateFormat = null;
	private List<EncounterType> heartFailureEncounter;
	protected Log log = LogFactory.getLog(this.getClass());
	GlobalPropertiesManagement gp=new GlobalPropertiesManagement();
	
	public DrugDosageCurrentFilter(List<EncounterType> heartFailureEncounter) {
		heartFailureEncounter = gp.getEncounterTypeList(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTERS,"");
		this.heartFailureEncounter =heartFailureEncounter;
		
	}

	
	public Object filter(Object value) {
		DrugOrder drugOrder = (DrugOrder)value;
	
		StringBuilder result = new StringBuilder();
			
		if(drugOrder.getDiscontinuedDate() != null){
		  if(returnVisitDates().compareTo(drugOrder.getDiscontinuedDate()) < 0){
			if(drugOrder != null && drugOrder.getDrug() != null){  
			  result.append(drugOrder.getDrug().getName());
			  result.append(" ");
			  result.append(drugOrder.getDose());
			  result.append(drugOrder.getUnits());
			  result.append(" ");
			  String freq = drugOrder.getFrequency();
			  if(freq != null)
			    {
				 if(freq.indexOf("x") > 1)
				 {   
					result.append("\n");
					result.append(freq);
					result.append("\n");
				}
				else
				{   
					result.append("\n");
					result.append(freq);
					result.append("\n");
				}
			   }
			 }
		    }
		  }
		 else if(drugOrder.getDiscontinuedDate() == null){
				if(drugOrder != null && drugOrder.getDrug() != null){  
				result.append(drugOrder.getDrug().getName());
				result.append(" ");
				result.append(drugOrder.getDose());
				result.append(drugOrder.getUnits());
				result.append(" ");
				String freq = drugOrder.getFrequency();
				if(freq != null)
				{
					 if(freq.indexOf("x") > 1)
					{   
						result.append("\n");
						result.append(freq);
						result.append("\n");
					}
					else
					{   
						result.append("\n");
						result.append(freq);
						result.append("\n");
					}
				   }
				 }
			  
			  }
		 
		return result.toString();
	}


	public String getFinalDateFormat() {
		return finalDateFormat;
	}

	public void setFinalDateFormat(String finalDateFormat) {
		this.finalDateFormat = finalDateFormat;
	}


	public Object filterWhenNull() {
	    // TODO Auto-generated method stub
	    return null;
    }
	
     public Date returnVisitDates(){
    	 DrugOrder drugOrder=new DrugOrder();
    	 Date retunv=null;
    	 Patient patient = drugOrder.getPatient();
    	 List<Encounter> patientEncounters = Context.getEncounterService().getEncounters(patient, null, null, null, null, heartFailureEncounter, null, false);

    	 if (patientEncounters.size() > 0 && !drugOrder.isDiscontinuedRightNow() ) {
         Encounter recentEncounter = patientEncounters.get(patientEncounters.size() - 1); //the last encounter in the List should be the most recent one.

         for (Obs obs: recentEncounter.getObs()){
            if(obs.getConcept().getConceptId() == 5096){
    	      retunv = obs.getValueDatetime();
    	 
    	   }
    	 }
      }

    	 return retunv;
    	 
    	}
    
	 	
}
