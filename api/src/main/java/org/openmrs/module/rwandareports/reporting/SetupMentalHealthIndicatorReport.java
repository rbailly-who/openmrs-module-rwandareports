package org.openmrs.module.rwandareports.reporting;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.query.encounter.definition.EncounterQuery;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.indicator.EncounterIndicator;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

import java.util.*;

/**
 * Created by josua on 10/22/18.
 */
public class SetupMentalHealthIndicatorReport {

    GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

    private Program mentalhealthProgram;

    private List<Program> mentalHealthPrograms = new ArrayList<Program>();

    private EncounterType mentalHealthEncounterType;
    private List<EncounterType> mentalHealthEncounterTypes = new ArrayList<EncounterType>();

    private List<String> onOrAfterOnOrBefore = new ArrayList<String>();

    private List<String> enrolledOnOrAfterOnOrBefore = new ArrayList<String>();

    List<Form> InitialAndRoutineEncounters;

    private Concept MHExitReasons;
    private Concept LostToFolloUpOutCome;

//    private Concept PrimaryDiagnosisConcept;

    private Concept SomatoformDisorderTroubleSomatoformConcept;
    private Concept EPILEPSYConcept;
    private Concept BipolarDisorderConcept;
    private Concept DepressionDueToOtherMedicalConditionConcept;
    private Concept DepressionWithPsychoticFeaturesConcept;
    private Concept DepressionUnspecifiedConcept;
    private Concept MajorDepressiveDisorderConcept;
    private Concept PSYCHOSISConcept;
    private Concept PsychosisDueToOtherMedicalConditionConcept;
    private Concept SCHIZOPHRENIAConcept;
    private Concept MentalHealthDiagnosisStoppingReasonConcept;
    private Concept mentalHealthDiagnosis;
    private Concept SOMATOFORMDISORDERSF45;
    private Concept EpilepsyandrecurrentseizuresG40;
    private Concept BipolardisorderF31;
    private Concept MajorDepressiveDisorderRecurrentSevereWithPsychoticsymptomsF333;
    private Concept MajorDepressiveDisorderSingleEpisodeF32;
    private Concept MajorDepressiveDisorderRecurrentF33;
    private Concept UnspecifiedPsychosisNotDueToaSubstanceOrKnownPsychologicalConditionF29;
    private Concept SchizophreniaF20;


    private List<Concept> SomatoformDisorderTroubleSomatoformList =  new ArrayList<Concept>() ;
    private List<Concept> EPILEPSYList =  new ArrayList<Concept>() ;
    private List<Concept> BipolarDisorderList =  new ArrayList<Concept>() ;
    private List<Concept> DepressionList =  new ArrayList<Concept>() ;
    private List<Concept> PsychosisorSchizophreniaList =  new ArrayList<Concept>() ;
    private List<Concept> PsychosisList = new ArrayList<Concept>();
    private List<Concept> SchizophreniaList = new ArrayList<Concept>();

    private List<Concept> BifferStateList =  new ArrayList<Concept>() ;


    public void setup() throws Exception {

        setUpProperties();

        //Monthly report set-up


        Properties properties = new Properties();
        properties.setProperty("hierarchyFields", "countyDistrict:District");


        // Quarterly Report Definition: Start

        ReportDefinition quarterlyRd = new ReportDefinition();
        quarterlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        quarterlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));

        quarterlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));

        quarterlyRd.setName("MentalHealth Indicator Report-Quarterly");

        quarterlyRd.addDataSetDefinition(createQuarterlyLocationDataSet(),
                ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));

        // Quarterly Report Definition: End

        ProgramEnrollmentCohortDefinition patientEnrolledInMentalHealthProgram = new ProgramEnrollmentCohortDefinition();
        patientEnrolledInMentalHealthProgram.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
        patientEnrolledInMentalHealthProgram.setPrograms(mentalHealthPrograms);



        quarterlyRd.setBaseCohortDefinition(patientEnrolledInMentalHealthProgram,
                ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));

        Helper.saveReportDefinition(quarterlyRd);


        ReportDesign quarterlyDesign = Helper.createRowPerPatientXlsOverviewReportDesign(quarterlyRd,
                "MentalHealth_Indicator_Quarterly_Report.xls", "MentalHealth Indicator Quarterly Report (Excel)", null);
        Properties quarterlyProps = new Properties();
        quarterlyProps.put("repeatingSections", "sheet:1,dataset:Encounter Quarterly Data Set");
        quarterlyProps.put("sortWeight","5000");
        quarterlyDesign.setProperties(quarterlyProps);
        Helper.saveReportDesign(quarterlyDesign);

    }

    public void delete() {
        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rd : rs.getAllReportDesigns(false)) {
            if ("MentalHealth Indicator Quarterly Report (Excel)".equals(rd.getName())) {
                rs.purgeReportDesign(rd);
            }
        }
        Helper.purgeReportDefinition("MentalHealth Indicator Report-Quarterly");

    }

    //Create Quarterly Encounter Data set

    public LocationHierachyIndicatorDataSetDefinition createQuarterlyLocationDataSet() {

        LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
                createEncounterQuarterlyBaseDataSet());
        ldsd.addBaseDefinition(createQuarterlyBaseDataSet());
        ldsd.setName("Encounter Quarterly Data Set");
        ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));

        return ldsd;
    }
    private EncounterIndicatorDataSetDefinition createEncounterQuarterlyBaseDataSet() {

        EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();

        eidsd.setName("eidsd");
        eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        createQuarterlyIndicators(eidsd);
        return eidsd;
    }

    private void createQuarterlyIndicators(EncounterIndicatorDataSetDefinition dsd) {

        //=======================================================================
        //  A2: Total # of patient visits to DM clinic in the last quarter
        //==================================================================
        SqlEncounterQuery patientVisitsToMentalHealthClinic = new SqlEncounterQuery();

        patientVisitsToMentalHealthClinic
                .setQuery("select encounter_id from encounter where encounter_type="+mentalHealthEncounterType.getEncounterTypeId()+" and encounter_datetime>= :startDate and encounter_datetime<= :endDate and voided=0 group by encounter_datetime, patient_id");
        patientVisitsToMentalHealthClinic.setName("patientVisitsToMentalHealthClinic");
        patientVisitsToMentalHealthClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
        patientVisitsToMentalHealthClinic.addParameter(new Parameter("endDate", "endDate", Date.class));

        EncounterIndicator patientVisitsToMentalHealthClinicQuarterlyIndicator = new EncounterIndicator();
        patientVisitsToMentalHealthClinicQuarterlyIndicator.setName("patientVisitsToMentalHealthClinicQuarterlyIndicator");
        patientVisitsToMentalHealthClinicQuarterlyIndicator.setEncounterQuery(new Mapped<EncounterQuery>(
                patientVisitsToMentalHealthClinic, ParameterizableUtil
                .createParameterMappings("endDate=${endDate},startDate=${startDate}")));

        dsd.addColumn(patientVisitsToMentalHealthClinicQuarterlyIndicator);

    }

    // create quarterly cohort Data set
    private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setName("Quarterly Cohort Data Set");
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        createQuarterlyIndicators(dsd);
        return dsd;
    }

    private void createQuarterlyIndicators(CohortIndicatorDataSetDefinition dsd) {

        //=======================================================================
        //  A1: Total # of patients seen in the last quarter
        //==================================================================


        EncounterCohortDefinition patientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
                onOrAfterOnOrBefore, mentalHealthEncounterTypes);

        EncounterCohortDefinition patientWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
                onOrAfterOnOrBefore, InitialAndRoutineEncounters);

        CompositionCohortDefinition patientsSeenComposition = new CompositionCohortDefinition();
        patientsSeenComposition.setName("patientsSeenComposition");
        patientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        patientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        patientsSeenComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        patientsSeenComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        patientsSeenComposition.setCompositionString("1 OR 2");

        CohortIndicator patientsSeenQuarterIndicator = Indicators.newCountIndicator("patientsSeenMonthThreeIndicator",
                patientsSeenComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-3m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("A1N", "Total # of patients seen in the last quarter", new Mapped(patientsSeenQuarterIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=============================================================
        // A3: Active Patient(currently enrolled with an encounter)  //
        //=============================================================


        CompositionCohortDefinition activePatientComposition = new CompositionCohortDefinition();
        activePatientComposition.setName("activePatientComposition");
        activePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        activePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        activePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        activePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        activePatientComposition.setCompositionString("1 OR 2");

        CohortIndicator activePatientQuarterIndicator = Indicators.newCountIndicator("activePatientQuarterIndicator",
                activePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("ActiveAtEndDate", "Total # of active at end date", new Mapped(activePatientQuarterIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
        dsd.addColumn("ActiveAtStartDate", "Total # of active at start date", new Mapped(activePatientQuarterIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${startDate}")), "");

        //=============================================
        // B1: Active Patient With  Depression  //
        //=============================================

        CohortIndicator activePatientWithDepressionStatesIndicator = activeAndInstateInPeriod(DepressionList,patientWithDDB,patientSeen);
        dsd.addColumn("activePatientWithDepressionStates", "Total # of active patient with Depression", new Mapped(activePatientWithDepressionStatesIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //============================================================================
        // B2: Active Patient With  Somatoform Disorder or Trouble Somatoform        //
        //============================================================================

        CohortIndicator activePatientWithSDorTSStateIndicator = activeAndInstateInPeriod(SomatoformDisorderTroubleSomatoformList,patientWithDDB,patientSeen);
        dsd.addColumn("activePatientWithSDorTSState", "Total # of active patient with SD or TS", new Mapped(activePatientWithSDorTSStateIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
//
//        //=====================================================
//        // B3: Active Patient With  Psychosis/Schizophrenia  //
//        //=====================================================
//
//        CohortIndicator activePatientWithPsychosisorSchizophreniaStatesIndicator = activeAndInstateInPeriod(PsychosisorSchizophreniaList,patientWithDDB,patientSeen);
//        dsd.addColumn("activePatientWithPsychosisorSchizophreniaStates", "Total # of active patient with Psychosis or Schizophrenia", new Mapped(activePatientWithPsychosisorSchizophreniaStatesIndicator,
//                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

//
//        //=====================================================
//        // B3: Active Patient With  Psychosis  //
//        //=====================================================
//
        CohortIndicator activePatientWithPsychosisStatesIndicator = activeAndInstateInPeriod(PsychosisList,patientWithDDB,patientSeen);
        dsd.addColumn("activePatientWithPsychosisStates", "Total # of active patient with Psychosis ", new Mapped(activePatientWithPsychosisStatesIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

//
//        //=====================================================
//        // B4: Active Patient With  Schizophrenia  //
//        //=====================================================
//
        CohortIndicator activePatientWithSchizophreniaStatesIndicator = activeAndInstateInPeriod(SchizophreniaList,patientWithDDB,patientSeen);
        dsd.addColumn("activePatientWithSchizophreniaStates", "Total # of active patient with Schizophrenia", new Mapped(activePatientWithSchizophreniaStatesIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
//
//        //============================================================================
//        // B5: Active Patient With  EPILEPSY //
//        //============================================================================
//
        CohortIndicator activePatientWithEpilepsyStateIndicator = activeAndInstateInPeriod(EPILEPSYList,patientWithDDB,patientSeen);
        dsd.addColumn("activePatientWithEpilepsyState", "Total # of active patient with epilepsy", new Mapped(activePatientWithEpilepsyStateIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
//
//        //=============================================
//        // B6: Active Patient With  Bipolar Disorder //
//        //=============================================
//
        CohortIndicator activePatientWithBipolarDisorderStateIndicator = activeAndInstateInPeriod(BipolarDisorderList,patientWithDDB,patientSeen);
        dsd.addColumn("activePatientWithBipolarDisorderState", "Total # of active patient with Bipolar Disorder", new Mapped(activePatientWithBipolarDisorderStateIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //==========================//
        // C1: # of LTFU patients   //
        //==========================//

        SqlCohortDefinition programOutcomeLTFU=Cohorts.getPatientsWithCodedObservationsBetweenStartDateAndEndDate("LTFU",MHExitReasons,LostToFolloUpOutCome);


        CompositionCohortDefinition LTFUPatientComposition = new CompositionCohortDefinition();
        LTFUPatientComposition.setName("LTFUPatientComposition");
        LTFUPatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        LTFUPatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        LTFUPatientComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        LTFUPatientComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        LTFUPatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        LTFUPatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        LTFUPatientComposition.getSearches().put(
                "3",
                new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${endDate-12m+1d},onOrAfter=${startDate-12m+1d}")));
        LTFUPatientComposition.getSearches().put(
                "4",
                new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${endDate-12m+1d},onOrAfter=${startDate-12m+1d}")));
        LTFUPatientComposition.getSearches().put(
                "5",
                new Mapped<CohortDefinition>(programOutcomeLTFU, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}")));
        LTFUPatientComposition.setCompositionString("(NOT (1 OR 2) AND (3 OR 4)) OR 3");

        CohortIndicator LTFUPatientQuarterIndicator = Indicators.newCountIndicator("LTFUPatientQuarterIndicator",
                LTFUPatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("LTFUPatientInQuarter", "Total # of ever LFTU patients", new Mapped(LTFUPatientQuarterIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate},endDate=${endDate}")), "");


        //========================================================//
        // C2: # of patients ever encounter at that health center //
        //========================================================//

        EncounterCohortDefinition patientEverSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
                "onOrBefore", mentalHealthEncounterTypes);

        EncounterCohortDefinition patientEverWithDDB = Cohorts.createEncounterBasedOnForms("patientWithDDB",
                "onOrBefore", InitialAndRoutineEncounters);


        CompositionCohortDefinition patientEverEncounteredComposition = new CompositionCohortDefinition();
        patientEverEncounteredComposition.setName("patientEverEncounteredComposition");
        patientEverEncounteredComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        patientEverEncounteredComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientEverWithDDB, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore}")));
        patientEverEncounteredComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(patientEverSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore}")));
        patientEverEncounteredComposition.setCompositionString("1 OR 2");

        CohortIndicator patientEverEncounteredQuarterIndicator = Indicators.newCountIndicator("patientEverEncounteredQuarterIndicator",
                patientEverEncounteredComposition,
                ParameterizableUtil.createParameterMappings("onOrBefore=${endDate}"));
        dsd.addColumn("patientEverEncountered", "Total # of ever Encountered patients", new Mapped(patientEverEncounteredQuarterIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

    }

    private void setUpProperties() {

        mentalhealthProgram = gp.getProgram(GlobalPropertiesManagement.MENTAL_HEALTH_PROGRAM);
        mentalHealthPrograms.add(mentalhealthProgram);
        mentalHealthEncounterType = gp.getEncounterType(GlobalPropertiesManagement.MENTAL_HEALTH_VISIT);
        mentalHealthEncounterTypes.add(mentalHealthEncounterType);

        onOrAfterOnOrBefore.add("onOrAfter");
        onOrAfterOnOrBefore.add("onOrBefore");

        enrolledOnOrAfterOnOrBefore.add("enrolledOnOrAfter");
        enrolledOnOrAfterOnOrBefore.add("enrolledOnOrBefore");

        InitialAndRoutineEncounters=gp.getFormList(GlobalPropertiesManagement.MENTAL_HEALTH_NEXT_VISIT_FORMS);

        MHExitReasons=gp.getConcept(GlobalPropertiesManagement.MENTAL_HEALTH_EXIT_REASONS_CONCEPT);
        LostToFolloUpOutCome =gp.getConcept(GlobalPropertiesManagement.LOST_TO_FOLLOWUP_OUTCOME);

//        PrimaryDiagnosisConcept = gp.getConcept(GlobalPropertiesManagement.Primary_Diagnosis_Concept);

        SomatoformDisorderTroubleSomatoformConcept = gp.getConcept(GlobalPropertiesManagement.Somatoform_Disorder_or_Trouble_Somatoform_Concept);
        EPILEPSYConcept = gp.getConcept(GlobalPropertiesManagement.EPILEPSY_Concept);
        BipolarDisorderConcept = gp.getConcept(GlobalPropertiesManagement.Bipolar_Disorder_Concept);
        DepressionDueToOtherMedicalConditionConcept = gp.getConcept(GlobalPropertiesManagement.Depression_due_to_other_medical_condition_Concept);
        DepressionWithPsychoticFeaturesConcept = gp.getConcept(GlobalPropertiesManagement.Depression_with_Psychotic_Features_Concept);
        DepressionUnspecifiedConcept = gp.getConcept(GlobalPropertiesManagement.Depression_unspecified_Concept);
        MajorDepressiveDisorderConcept = gp.getConcept(GlobalPropertiesManagement.Major_Depressive_Disorder_Concept);
        PSYCHOSISConcept = gp.getConcept(GlobalPropertiesManagement.PSYCHOSIS_Concept);
        PsychosisDueToOtherMedicalConditionConcept = gp.getConcept(GlobalPropertiesManagement.Psychosis_due_to_other_medical_condition_Concept);
        SCHIZOPHRENIAConcept = gp.getConcept(GlobalPropertiesManagement.SCHIZOPHRENIA_Concept);

        //icd-10
        SOMATOFORMDISORDERSF45 =  gp.getConcept(GlobalPropertiesManagement.SomatoformdisordersF45);
        EpilepsyandrecurrentseizuresG40 = gp.getConcept(GlobalPropertiesManagement.EpilepsyandrecurrentseizuresG40);
        BipolardisorderF31 = gp.getConcept(GlobalPropertiesManagement.BipolardisorderF31);
        MajorDepressiveDisorderRecurrentSevereWithPsychoticsymptomsF333 = gp.getConcept(GlobalPropertiesManagement.MajorDepressiveDisorderRecurrentSevereWithPsychoticsymptomsF333);
        MajorDepressiveDisorderSingleEpisodeF32 = gp.getConcept(GlobalPropertiesManagement.MajorDepressiveDisorderSingleEpisodeF32);
        MajorDepressiveDisorderRecurrentF33 = gp.getConcept(GlobalPropertiesManagement.MajorDepressiveDisorderRecurrentF33);
        UnspecifiedPsychosisNotDueToaSubstanceOrKnownPsychologicalConditionF29 = gp.getConcept(GlobalPropertiesManagement.UnspecifiedPsychosisNotDueToaSubstanceOrKnownPsychologicalConditionF29);
        SchizophreniaF20 = gp.getConcept(GlobalPropertiesManagement.SchizophreniaF20);

        SomatoformDisorderTroubleSomatoformList.add(SomatoformDisorderTroubleSomatoformConcept);
        SomatoformDisorderTroubleSomatoformList.add(SOMATOFORMDISORDERSF45);
        EPILEPSYList.add(EPILEPSYConcept);
        EPILEPSYList.add(EpilepsyandrecurrentseizuresG40);
        BipolarDisorderList.add(BipolarDisorderConcept);
        BipolarDisorderList.add(BipolardisorderF31);
        DepressionList.add(DepressionDueToOtherMedicalConditionConcept);
        DepressionList.add(DepressionWithPsychoticFeaturesConcept);
        DepressionList.add(DepressionUnspecifiedConcept);
        DepressionList.add(MajorDepressiveDisorderConcept);
        DepressionList.add(MajorDepressiveDisorderRecurrentSevereWithPsychoticsymptomsF333);
        DepressionList.add(MajorDepressiveDisorderSingleEpisodeF32);
        DepressionList.add(MajorDepressiveDisorderRecurrentF33);
        PsychosisList.add(PSYCHOSISConcept);
        PsychosisList.add(PsychosisDueToOtherMedicalConditionConcept);
        PsychosisList.add(UnspecifiedPsychosisNotDueToaSubstanceOrKnownPsychologicalConditionF29);
        SchizophreniaList.add(SCHIZOPHRENIAConcept);
        SchizophreniaList.add(SchizophreniaF20);


        MentalHealthDiagnosisStoppingReasonConcept = gp.getConcept(GlobalPropertiesManagement.Mental_Health_Diagnosis_Stopping_Reason_Concept);
        mentalHealthDiagnosis = gp.getConcept(GlobalPropertiesManagement.MENTAL_HEALTH_DIAGNOSIS_CONCEPT);


//        SOMATOFORMDISORDERSF45 =  gp.getConcept(GlobalPropertiesManagement.SomatoformdisordersF45);
//        EpilepsyandrecurrentseizuresG40 = gp.getConcept(GlobalPropertiesManagement.EpilepsyandrecurrentseizuresG40);
//        BipolardisorderF31 = gp.getConcept(GlobalPropertiesManagement.BipolardisorderF31);
//        MajorDepressiveDisorderRecurrentSevereWithPsychoticsymptomsF333 = gp.getConcept(GlobalPropertiesManagement.MajorDepressiveDisorderRecurrentSevereWithPsychoticsymptomsF333);
//        MajorDepressiveDisorderSingleEpisodeF32 = gp.getConcept(GlobalPropertiesManagement.MajorDepressiveDisorderSingleEpisodeF32);
//        MajorDepressiveDisorderRecurrentF33 = gp.getConcept(GlobalPropertiesManagement.MajorDepressiveDisorderRecurrentF33);
//        UnspecifiedPsychosisNotDueToaSubstanceOrKnownPsychologicalConditionF29 = gp.getConcept(GlobalPropertiesManagement.UnspecifiedPsychosisNotDueToaSubstanceOrKnownPsychologicalConditionF29);
//        SchizophreniaF20 = gp.getConcept(GlobalPropertiesManagement.SchizophreniaF20);


    }

    private CohortIndicator activeAndInstateInPeriod(List<Concept> Diagnoses,EncounterCohortDefinition patientWithDDB,
                                                     EncounterCohortDefinition patientSeen ){


        SqlCohortDefinition patientsWithActiveDiagnosis = getPatientsWithDiagnosis("Patient has a diagnosis", mentalHealthDiagnosis,Diagnoses,MentalHealthDiagnosisStoppingReasonConcept);

        CompositionCohortDefinition activePatientWithStateComposition = new CompositionCohortDefinition();
        activePatientWithStateComposition.setName("activePatientWithStateComposition");
        activePatientWithStateComposition.addParameter(new Parameter("onDate", "onDate", Date.class));
        activePatientWithStateComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        activePatientWithStateComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        activePatientWithStateComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientWithDDB, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        activePatientWithStateComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(patientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        activePatientWithStateComposition.getSearches().put(
                "3",
                new Mapped<CohortDefinition>(patientsWithActiveDiagnosis, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        activePatientWithStateComposition.setCompositionString("(1 OR 2) AND 3");

        CohortIndicator activePatientWithStateQuarterIndicator = Indicators.newCountIndicator("activePatientWithStateQuarterIndicator",
                activePatientWithStateComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate},onDate=${endDate}"));


        return activePatientWithStateQuarterIndicator;
    }
    public static SqlCohortDefinition getPatientsWithDiagnosis(String name, Concept conceptQuestion,List<Concept> conceptAnswerList,
                                                               Concept stoppingReason) {
        SqlCohortDefinition obsBetweenStartDateAndEndDate = new SqlCohortDefinition();

        StringBuilder query = new StringBuilder("select distinct o.person_id from obs as o left join (select encounter_id from obs where concept_id= ");

        query.append(stoppingReason.getConceptId());

        query.append(" and voided=0 and obs_datetime>= :onOrAfter and obs_datetime<= :onOrBefore) as unwanted on unwanted.encounter_id = o.encounter_id where o.concept_id=");
        query.append(conceptQuestion.getConceptId()+" and o.voided=0 and (o.encounter_id != unwanted.encounter_id or unwanted.encounter_id is null) and o.value_coded in (");

        int i = 0;
        for (Concept c : conceptAnswerList) {
            if (i > 0) {
                query.append(",");
            }
            query.append(c.getId());

            i++;
        }
        query.append(") and o.obs_datetime>= :onOrAfter and o.obs_datetime<= :onOrBefore");

        obsBetweenStartDateAndEndDate.setQuery(query.toString());
        obsBetweenStartDateAndEndDate.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        obsBetweenStartDateAndEndDate.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        return obsBetweenStartDateAndEndDate;
    }

}

