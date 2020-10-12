package org.openmrs.module.rwandareports.reporting;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageCurrentFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

import java.util.*;

/**
 * Created by josua on 10/15/18.
 */
public class SetupMentalHealthConsultationSheet {
    GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

    private Program MentalHealth;
    EncounterType MentalHealthEncounter;
    private EncounterType nonClinicalEncounter;
    List<EncounterType> MHRelatedNextVisitEncTypes = new ArrayList<EncounterType>();
    List<EncounterType> mentalHealthEncounterTypeList;
    private Form mentalHealthMissedVisitForm;

    private List<Form> InitialAndRoutineEncounters = new ArrayList<Form>();
    private List<Form> MHNextVisitForms = new ArrayList<Form>();
    private RelationshipType HBCP;


    private Concept mentalHealthDiagnosis;
    private Concept OldSymptoms;
    private Concept NewSymptoms;
    private Concept currentMedicalDiagnosis;
    private Concept accompPhoneNumberConcept;
    private Concept PrimaryDiagnosisConcept;
    private Concept MentalHealthDiagnosisStoppingReasonConcept;

    private Form MHDiagnosisForm;
    List<Form> MHDiagnosisformList = new ArrayList<Form>();

    public void setup() throws Exception {

        setupProperties();

        ReportDefinition rd = createReportDefinition();

        ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "MentalHealthConsultationSheet.xls",
                "MentalHealthConsultationSheet.xls_", null);

        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:9,dataset:dataSet");
        props.put("sortWeight","5000");
        design.setProperties(props);

        Helper.saveReportDesign(design);
    }
    public void delete() {
        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rd : rs.getAllReportDesigns(false)) {
            if ("MentalHealthConsultationSheet.xls_".equals(rd.getName())) {
                rs.purgeReportDesign(rd);
            }
        }
        Helper.purgeReportDefinition("Mental Health Consultation Sheet");
    }
    private ReportDefinition createReportDefinition() {

        ReportDefinition reportDefinition = new ReportDefinition();
        reportDefinition.setName("Mental Health Consultation Sheet");

        reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
        reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"), ParameterizableUtil.createParameterMappings("location=${location}"));
        reportDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));
        createDataSetDefinition(reportDefinition);

        Helper.saveReportDefinition(reportDefinition);

        return reportDefinition;
    }
    private void createDataSetDefinition(ReportDefinition reportDefinition) {
        // Create new dataset definition
        RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
        dataSetDefinition.setName("Mental Health Consultation Data Set");

        SortCriteria sortCriteria = new SortCriteria();
        sortCriteria.addSortElement("nextRDV", SortCriteria.SortDirection.ASC);
        dataSetDefinition.setSortCriteria(sortCriteria);

        dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition.addParameter(new Parameter("endDate", "Monday", Date.class));

        //Add filters
        dataSetDefinition.addFilter(Cohorts.createInProgramParameterizableByDate("Patients in "+MentalHealth.getName(), MentalHealth), ParameterizableUtil.createParameterMappings("onDate=${endDate}"));

        dataSetDefinition.addFilter(Cohorts.getMondayToSundayPatientReturnVisit(MHNextVisitForms), ParameterizableUtil.createParameterMappings("end=${endDate+6d},start=${endDate}"));


        DateFormatFilter dateFilter = new DateFormatFilter();
        dateFilter.setFinalDateFormat("dd-MMM-yyyy");


        //Add Columns


        dataSetDefinition.addColumn(RowPerPatientColumns.getDateOfMostRecentEncounterType("LastVisitDate", mentalHealthEncounterTypeList, "yyyy/MM/dd"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentReturnVisitDate("nextVisit", "dd-MMM-yyyy", null), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getIMBId("Id"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getFirstNameColumn("givenName"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getFamilyNameColumn("familyName"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());

//        dataSetDefinition.addColumn(RowPerPatientColumns.getGender("Sex"), new HashMap<String, Object>());

//        dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPeakFlow("Last peak flow", "dd-MMM-yyyy"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAllObsValuesByRemovingUnwantedEncounters("Diagnoses", mentalHealthDiagnosis, MentalHealthDiagnosisStoppingReasonConcept, MHDiagnosisformList), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getPatientCurrentlyActiveOnDrugOrder("Regimen", null),
                new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("Accompagnateur"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("AccompPhoneNumber",accompPhoneNumberConcept,null,null,null ), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getMostRecentPatientPhoneNumber("patientPhone", "dd-MMM-yyyy"), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("patientAddress", false, false, false, true), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getObsAtLastEncounter("NewSymptoms", NewSymptoms, MentalHealthEncounter), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getObsAtLastEncounter("OldSymptoms", OldSymptoms, MentalHealthEncounter), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP",HBCP.getRelationshipTypeId(),"A",null), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("CurrentMedicalDiagnosis",currentMedicalDiagnosis,null,null,null ), new HashMap<String, Object>());


//        AllObservationValues asthmaClassification = RowPerPatientColumns.getAllAsthmaClassificationValues("asthmaClassification", null, new LastTwoObsFilter(),
//                null);



//        CustomCalculationBasedOnMultiplePatientDataDefinitions alert = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
//        alert.setName("alert");
//        alert.addPatientDataToBeEvaluated(asthmaClassification, new HashMap<String, Object>());
//        alert.setCalculator(new AsthmaClassificationAlerts());
//        dataSetDefinition.addColumn(alert, new HashMap<String, Object>());


        Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("location", "${location}");
        mappings.put("endDate", "${endDate}");


        reportDefinition.addDataSetDefinition("dataSet", dataSetDefinition, mappings);
    }


    private void setupProperties() {

        MentalHealth = gp.getProgram(GlobalPropertiesManagement.MENTAL_HEALTH_PROGRAM);
        MentalHealthEncounter = gp.getEncounterType(GlobalPropertiesManagement.MENTAL_HEALTH_VISIT);
        mentalHealthEncounterTypeList = gp.getEncounterTypeList(GlobalPropertiesManagement.MENTAL_HEALTH_VISIT);
        mentalHealthDiagnosis = gp.getConcept(GlobalPropertiesManagement.MENTAL_HEALTH_DIAGNOSIS_CONCEPT);
        currentMedicalDiagnosis = gp.getConcept(GlobalPropertiesManagement.CURRENT_MEDICAL_DIAGNOSIS_CONCEPT);
        NewSymptoms = gp.getConcept(GlobalPropertiesManagement.New_Symptom);
        OldSymptoms = gp.getConcept(GlobalPropertiesManagement.OLD_SYMPTOM);
        accompPhoneNumberConcept = gp.getConcept(GlobalPropertiesManagement.ACCOMPAGNATEUR_PHONE_NUMBER_CONCEPT);
        MentalHealthDiagnosisStoppingReasonConcept = gp.getConcept(GlobalPropertiesManagement.Mental_Health_Diagnosis_Stopping_Reason_Concept);



        nonClinicalEncounter = gp.getEncounterType(GlobalPropertiesManagement.NON_CLINICAL_ENCOUNTER);
        MHRelatedNextVisitEncTypes.add(nonClinicalEncounter);
        MHRelatedNextVisitEncTypes.add(MentalHealthEncounter);

        mentalHealthMissedVisitForm= gp.getForm(GlobalPropertiesManagement.MENTAL_HEALTH_MISSED_VISIT_FORM);

        InitialAndRoutineEncounters=gp.getFormList(GlobalPropertiesManagement.MENTAL_HEALTH_NEXT_VISIT_FORMS);
        MHNextVisitForms = gp.getFormList(GlobalPropertiesManagement.MENTAL_HEALTH_NEXT_VISIT_FORMS);
        MHNextVisitForms.add(mentalHealthMissedVisitForm);

        HBCP=gp.getRelationshipType(GlobalPropertiesManagement.HBCP_RELATIONSHIP);

        PrimaryDiagnosisConcept = gp.getConcept(GlobalPropertiesManagement.Primary_Diagnosis_Concept);


        MHDiagnosisForm = gp.getForm(GlobalPropertiesManagement.MH_Diagnosis_Form);
        MHDiagnosisformList.add(MHDiagnosisForm);




    }
}
