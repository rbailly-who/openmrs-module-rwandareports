package org.openmrs.module.rwandareports.reporting;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rowperpatientreports.dataset.definition.RowPerPatientDataSetDefinition;
import org.openmrs.module.rowperpatientreports.patientdata.definition.*;
import org.openmrs.module.rwandareports.filter.AccompagnateurDisplayFilter;
import org.openmrs.module.rwandareports.filter.DateFormatFilter;
import org.openmrs.module.rwandareports.filter.DrugDosageCurrentFilter;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.RowPerPatientColumns;

import java.util.*;

/**
 * Created by josua on 10/17/18.
 */
public class SetupMentalHealthLateVisit {

    protected final static Log log = LogFactory.getLog(SetupMentalHealthLateVisit.class);

    GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

    //Properties retrieved from global variables
    private Program MentalHealth;

    private Concept nextVisitConcept;
    private Concept OldSymptoms;
    private Concept NewSymptoms;
    private Concept mentalHealthDiagnosis;
    private Concept currentMedicalDiagnosis;
    private Concept accompPhoneNumberConcept;
    private Concept PrimaryDiagnosisConcept;
    private Concept MentalHealthDiagnosisStoppingReasonConcept;

    private Form MHDiagnosisForm;
    List<Form> MHDiagnosisformList = new ArrayList<Form>();



    private EncounterType MentalHealthEncounter;
    private EncounterType nonClinicalEncounter;
    List<EncounterType> mentalHealthEncounterTypeList;
    List<EncounterType> MHRelatedNextVisitEncTypes = new ArrayList<EncounterType>();


//    private Form asthmaRDVForm;

//    private Form asthmaDDBForm;

    private Form mentalHealthMissedVisitForm;

    private List<Form> InitialAndRoutineEncounters = new ArrayList<Form>();
    private List<Form> MHNextVisitForms = new ArrayList<Form>();


    private RelationshipType HBCP;

    public void setup() throws Exception {

        setupProperties();

        ReportDefinition rd = createReportDefinition();
        ReportDesign design = Helper.createRowPerPatientXlsOverviewReportDesign(rd, "MentalHealthLateVisitTemplate.xls",
                "MentalHealthLateVisitTemplate", null);

        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:dataset");
        props.put("sortWeight","5000");
        design.setProperties(props);
        Helper.saveReportDesign(design);
    }

    public void delete() {
        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rd : rs.getAllReportDesigns(false)) {
            if ("MentalHealthLateVisitTemplate".equals(rd.getName())) {
                rs.purgeReportDesign(rd);
            }
        }
        Helper.purgeReportDefinition("Mental Health Late Visit");
    }

    private ReportDefinition createReportDefinition() {
        ReportDefinition reportDefinition = new ReportDefinition();
        reportDefinition.setName("Mental Health Late Visit");
        reportDefinition.addParameter(new Parameter("location", "Location", Location.class));
        reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));

        reportDefinition.setBaseCohortDefinition(Cohorts.createParameterizedLocationCohort("At Location"),
                ParameterizableUtil.createParameterMappings("location=${location}"));

        createDataSetDefinition(reportDefinition);
        Helper.saveReportDefinition(reportDefinition);

        return reportDefinition;
    }

    private void createDataSetDefinition(ReportDefinition reportDefinition) {

        DateFormatFilter dateFilter = new DateFormatFilter();
        dateFilter.setFinalDateFormat("dd/MM/yy");

        // in Mental Health Program  dataset definition
        RowPerPatientDataSetDefinition dataSetDefinition = new RowPerPatientDataSetDefinition();
        dataSetDefinition.setName("Patients Who have missed their visit by more than a week dataSetDefinition");

        SqlCohortDefinition patientsNotVoided = Cohorts.createPatientsNotVoided();
        dataSetDefinition.addFilter(patientsNotVoided, new HashMap<String, Object>());

        dataSetDefinition.addFilter(
                Cohorts.createInProgramParameterizableByDate("Patients in " + MentalHealth.getName(), MentalHealth),
                ParameterizableUtil.createParameterMappings("onDate=${endDate}"));

        dataSetDefinition.addFilter(Cohorts.createPatientsLateForVisitINDifferentEncounterTypes(MHNextVisitForms, MHRelatedNextVisitEncTypes),
                ParameterizableUtil.createParameterMappings("endDate=${endDate}"));

        //==================================================================
        //                 Columns of report settings
        //==================================================================

        MultiplePatientDataDefinitions imbType = RowPerPatientColumns.getIMBId("IMB ID");
        dataSetDefinition.addColumn(imbType, new HashMap<String, Object>());

        PatientProperty givenName = RowPerPatientColumns.getFirstNameColumn("familyName");
        dataSetDefinition.addColumn(givenName, new HashMap<String, Object>());

        PatientProperty familyName = RowPerPatientColumns.getFamilyNameColumn("givenName");
        dataSetDefinition.addColumn(familyName, new HashMap<String, Object>());

        MostRecentObservation lastphonenumber = RowPerPatientColumns.getMostRecentPatientPhoneNumber("telephone", null);
        dataSetDefinition.addColumn(lastphonenumber, new HashMap<String, Object>());

//        PatientProperty gender = RowPerPatientColumns.getGender("Sex");
//        dataSetDefinition.addColumn(gender, new HashMap<String, Object>());

//        DateOfBirthShowingEstimation birthdate = RowPerPatientColumns.getDateOfBirth("Date of Birth", null, null);
//        dataSetDefinition.addColumn(birthdate, new HashMap<String, Object>());
        dataSetDefinition.addColumn(RowPerPatientColumns.getAge("age"), new HashMap<String, Object>());


        dataSetDefinition.addColumn(RowPerPatientColumns.getNextVisitMostRecentEncounterOfTheTypes("nextVisit",
                MHRelatedNextVisitEncTypes, new ObservationInMostRecentEncounterOfType(), null), new HashMap<String, Object>());

//        CustomCalculationBasedOnMultiplePatientDataDefinitions numberofdaysLate = new CustomCalculationBasedOnMultiplePatientDataDefinitions();
//        numberofdaysLate.addPatientDataToBeEvaluated(RowPerPatientColumns.getNextVisitInMostRecentEncounterOfTypes(
//                        "nextVisit", MentalHealthEncounter, new ObservationInMostRecentEncounterOfType(), datenuFilter),
//                new HashMap<String, Object>());
//        numberofdaysLate.setName("numberofdaysLate");
//        numberofdaysLate.setCalculator(new DaysLate());
//        numberofdaysLate.addParameter(new Parameter("endDate","endDate",Date.class));
//        dataSetDefinition.addColumn(numberofdaysLate, ParameterizableUtil.createParameterMappings("endDate=${endDate}"));

//        MostRecentObservation lastpeakflow = RowPerPatientColumns.getMostRecentPeakFlow("Most recent peakflow", "@ddMMMyy");
//        dataSetDefinition.addColumn(lastpeakflow, new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getPatientAddress("patientAddress", false, false, false, true), new HashMap<String, Object>());


        dataSetDefinition.addColumn(RowPerPatientColumns.getAccompRelationship("AccompName",
                new AccompagnateurDisplayFilter()), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("AccompPhoneNumber",accompPhoneNumberConcept,null,null,null ), new HashMap<String, Object>());


        dataSetDefinition.addColumn(RowPerPatientColumns.getPatientRelationship("HBCP", HBCP.getRelationshipTypeId(), "A", null), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getObsAtLastEncounter("NewSymptoms", NewSymptoms, MentalHealthEncounter), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getObsAtLastEncounter("OldSymptoms", OldSymptoms, MentalHealthEncounter), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAllObsValuesByRemovingUnwantedEncounters("Diagnoses", mentalHealthDiagnosis, MentalHealthDiagnosisStoppingReasonConcept, MHDiagnosisformList), new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getPatientCurrentlyActiveOnDrugOrder("Regimen", null),
                new HashMap<String, Object>());

        dataSetDefinition.addColumn(RowPerPatientColumns.getAllObservationValues("CurrentMedicalDiagnosis",currentMedicalDiagnosis,null,null,null ), new HashMap<String, Object>());


        dataSetDefinition.addParameter(new Parameter("location", "Location", Location.class));
        dataSetDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));

        Map<String, Object> mappings = new HashMap<String, Object>();
        mappings.put("location", "${location}");
        mappings.put("endDate", "${endDate}");

        reportDefinition.addDataSetDefinition("dataset", dataSetDefinition, mappings);

    }

    private void setupProperties() {

        MentalHealth = gp.getProgram(GlobalPropertiesManagement.MENTAL_HEALTH_PROGRAM);

        MentalHealthEncounter = gp.getEncounterType(GlobalPropertiesManagement.MENTAL_HEALTH_VISIT);

        nonClinicalEncounter = gp.getEncounterType(GlobalPropertiesManagement.NON_CLINICAL_ENCOUNTER);

        nextVisitConcept = gp.getConcept(GlobalPropertiesManagement.RETURN_VISIT_DATE);

        NewSymptoms = gp.getConcept(GlobalPropertiesManagement.New_Symptom);
        OldSymptoms = gp.getConcept(GlobalPropertiesManagement.OLD_SYMPTOM);
        currentMedicalDiagnosis = gp.getConcept(GlobalPropertiesManagement.CURRENT_MEDICAL_DIAGNOSIS_CONCEPT);
        accompPhoneNumberConcept = gp.getConcept(GlobalPropertiesManagement.ACCOMPAGNATEUR_PHONE_NUMBER_CONCEPT);
        MentalHealthDiagnosisStoppingReasonConcept = gp.getConcept(GlobalPropertiesManagement.Mental_Health_Diagnosis_Stopping_Reason_Concept);



        mentalHealthEncounterTypeList = gp.getEncounterTypeList(GlobalPropertiesManagement.MENTAL_HEALTH_VISIT);
        mentalHealthDiagnosis = gp.getConcept(GlobalPropertiesManagement.MENTAL_HEALTH_DIAGNOSIS_CONCEPT);


        MHRelatedNextVisitEncTypes.add(nonClinicalEncounter);
        MHRelatedNextVisitEncTypes.add(MentalHealthEncounter);


        mentalHealthMissedVisitForm= gp.getForm(GlobalPropertiesManagement.MENTAL_HEALTH_MISSED_VISIT_FORM);

        InitialAndRoutineEncounters =gp.getFormList(GlobalPropertiesManagement.MENTAL_HEALTH_NEXT_VISIT_FORMS);

        MHNextVisitForms = gp.getFormList(GlobalPropertiesManagement.MENTAL_HEALTH_NEXT_VISIT_FORMS);
        MHNextVisitForms.add(mentalHealthMissedVisitForm);

        HBCP=gp.getRelationshipType(GlobalPropertiesManagement.HBCP_RELATIONSHIP);

        PrimaryDiagnosisConcept = gp.getConcept(GlobalPropertiesManagement.Primary_Diagnosis_Concept);


        MHDiagnosisForm = gp.getForm(GlobalPropertiesManagement.MH_Diagnosis_Form);
        MHDiagnosisformList.add(MHDiagnosisForm);



    }
}
