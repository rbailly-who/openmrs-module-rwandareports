package org.openmrs.module.rwandareports.dataset.evaluator;

import org.openmrs.*;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.encounter.EncounterQueryResult;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.query.encounter.service.EncounterQueryService;
import org.openmrs.module.rwandareports.dataset.DataEntryQuantityReport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Handler(supports = {DataEntryQuantityReport.class})
public class DataEntryQuantityReportEvaluator implements DataSetEvaluator {
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        context = ObjectUtil.nvl(context, new EvaluationContext());
        if (context == null) {
            context = new EvaluationContext();
        }

        DataEntryQuantityReport lhdsd = (DataEntryQuantityReport) dataSetDefinition;


        Date startDate = (Date) context.getParameterValue("startDate");
        Date endDate = (Date) context.getParameterValue("endDate");
        if(((Program) context.getParameterValue("program"))!=null) {
            lhdsd.setProgram((Program) context.getParameterValue("program"));
        }
        if(((EncounterType) context.getParameterValue("encounterType"))!=null) {
            lhdsd.addEncounterType((EncounterType) context.getParameterValue("encounterType"));
        }

        //get base cohort
        SqlEncounterQuery encounterCohort = getEncountersCohort(lhdsd.getProgram(),lhdsd.getEncounterTypes(),startDate,endDate);

        SimpleDataSet ret = new SimpleDataSet(dataSetDefinition, context);
        //set columns
        DataSetColumn formName = new DataSetColumn("form name","form name",Integer.class);
        ret.getMetaData().addColumn(formName);

        DataSetColumn location = new DataSetColumn("location","location",Integer.class);
        ret.getMetaData().addColumn(location);

        DataSetColumn creator = new DataSetColumn("creator","creator",Integer.class);
        ret.getMetaData().addColumn(creator);

        DataSetColumn provider = new DataSetColumn("provider","provider",Integer.class);
        ret.getMetaData().addColumn(provider);

        DataSetColumn monthOfEncounter = new DataSetColumn("month of encounter","month of encounter",Integer.class);
        ret.getMetaData().addColumn(monthOfEncounter);

        DataSetColumn noOfEncounters = new DataSetColumn("NoOfEncounters"," # of encounters that meet these criteria",Integer.class);
        ret.getMetaData().addColumn(noOfEncounters);

        if (encounterCohort != null) {
            List<Encounter> encs = new ArrayList<Encounter>();
            try {
                EncounterQueryService eqs = Context.getService(EncounterQueryService.class);

                EncounterQueryResult eqr = eqs.evaluate(encounterCohort, context);

                Set<Integer> encounters = eqr.getMemberIds();

                encs = getEncounters(encounters);

                List<Integer> encounterMonths = new ArrayList<Integer>();
                List<String> encFormNames = new ArrayList<String>();
                List<Location> encLocations = new ArrayList<Location>();
                List<Person> encProviders = new ArrayList<Person>();
                List<User> encCreators = new ArrayList<User>();
                //getting base value criteria
                for(Encounter enc:encs){
                    int encMonth = enc.getEncounterDatetime().getMonth();
                    String encFormName = enc.getForm().getName();
                    Location encLocation =  enc.getLocation();
                    Person encProvider = enc.getProvider();
                    User encCreator = enc.getCreator();
                    if(!encounterMonths.contains(encMonth)){
                        encounterMonths.add(encMonth);
                    }
                    if(!encFormNames.contains(encFormName)){
                        encFormNames.add(encFormName);
                    }
                    if(!encLocations.contains(encLocation)){
                        encLocations.add(encLocation);
                    }
                    if(!encProviders.contains(encProvider)){
                        encProviders.add(encProvider);
                    }
                    if(!encCreators.contains(encCreator)){
                        encCreators.add(encCreator);
                    }
                }

                for(String form:encFormNames){
                    for(Location theLocation:encLocations){
                        for(User theCreator: encCreators){
                            for(Person theProvider : encProviders) {
                                for (Integer Month:encounterMonths) {
                                    int numberOfOccurrence =getNoOfOccurancesGivenVariables(encs, Month, theCreator, form, theLocation, theProvider);
                                    if(numberOfOccurrence>=1) {
                                        DataSetRow row = new DataSetRow();
                                        row.addColumnValue(formName, form);
                                        row.addColumnValue(location, theLocation);
                                        row.addColumnValue(creator, theCreator.getNames());
                                        row.addColumnValue(provider, theProvider.getNames());
                                        row.addColumnValue(monthOfEncounter, Month + 1);
                                        row.addColumnValue(noOfEncounters,numberOfOccurrence );
                                        ret.addRow(row);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex) {

                throw new EvaluationException("baseCohort", ex);
            }


        }

        return ret;
    }
    private SqlEncounterQuery getEncountersCohort(Program program, List<EncounterType> encounterTypes, Date startDate, Date endDate)
    {


        SqlEncounterQuery encounterCohort = new SqlEncounterQuery();

        String sql = "select encounter_id from encounter where encounter_datetime >= :startDate and encounter_datetime <= :endDate and voided=0 and form_id is not null ";
        if(!encounterTypes.isEmpty())
        {
            sql = sql + " and encounter_type in (" + getCommaSeparatedEncounterTypes(encounterTypes) + ")";
        }
        if(program != null){
            sql = sql + " and patient_id in ("+getCommaSeparatedPatientInProgram(program,endDate) + ")";

        }
        encounterCohort.setQuery(sql);
        encounterCohort.addParameter(new Parameter("startDate", "startDate", Date.class));
        encounterCohort.addParameter(new Parameter("endDate", "endDate", Date.class));

        return encounterCohort;

    }

    private String getCommaSeparatedEncounterTypes(List<EncounterType> encounterTypes)
    {
        StringBuilder result = new StringBuilder();
        for(EncounterType et: encounterTypes)
        {
            if(result.length() > 0)
            {
                result.append(",");
            }
            result.append(et.getId());
        }
        return result.toString();
    }
    private String getCommaSeparatedPatientInProgram(Program program,Date endDate)
    {

        Set<Integer> patientIds = Context.getPatientSetService().getPatientsByProgramAndState(program,null,null,endDate).getPatientIds();

        StringBuilder result = new StringBuilder();
        for(Integer pat: patientIds)
        {
            if(result.length() > 0)
            {
                result.append(",");
            }
            result.append(pat);
        }
        return result.toString();
    }
    private List<Encounter> getEncounters(Set<Integer> encounterIds)
    {
        List<Encounter> encs = new ArrayList<Encounter>();

        for (Integer eId : encounterIds) {
            Encounter e = Context.getEncounterService().getEncounter(eId);

            if (e != null) {
                encs.add(e);
            }
        }

        return encs;
    }
    private Integer getNoOfOccurancesGivenVariables(List<Encounter> encounters,Integer Month,User theCreator,String form,Location theLocation,Person theProvider)
    {

        int numberOfOccurences=0;
        for(Encounter enc: encounters){
            if(enc.getEncounterDatetime().getMonth() == Month && enc.getCreator()==theCreator && enc.getForm().getName()==form
                    && enc.getLocation()==theLocation && enc.getProvider()==theProvider && !enc.getPatient().isVoided()){
                numberOfOccurences++;
            }
        }
        return numberOfOccurences;
    }
}
