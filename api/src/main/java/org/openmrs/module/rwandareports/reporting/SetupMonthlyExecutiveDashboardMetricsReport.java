package org.openmrs.module.rwandareports.reporting;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.rwandareports.dataset.EncounterIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.dataset.LocationHierachyIndicatorDataSetDefinition;
import org.openmrs.module.rwandareports.util.Cohorts;
import org.openmrs.module.rwandareports.util.GlobalPropertiesManagement;
import org.openmrs.module.rwandareports.util.Indicators;
import org.openmrs.module.rwandareports.widget.AllLocation;
import org.openmrs.module.rwandareports.widget.LocationHierarchy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by josua on 10/22/18.
 */
public class SetupMonthlyExecutiveDashboardMetricsReport {

    GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

    // properties
    private List<String> onOrAfterOnOrBefore = new ArrayList<String>();
    private List<String> startDateEndDate = new ArrayList<String>();

    private Program oncologyProgram;
    private List<Program> oncologyPrograms = new ArrayList<Program>();
    private Program hypertesionProgram;
    private Program heartFailureProgram;
    private Program asthmaProgram;
    private Program diabetesProgram;
    private Program CKDProgram;
    private Program epilepsyProgram;
    private List<Program> NCDPrograms = new ArrayList<Program>();
    private Program pdcProgram;
    private Program adulthivprogramname;
    private Program pmtctcombinedprogramname;
    private Program pedihivprogramname;
    private Program pmtctprogramname;
    private Program pmtctCombinedMotherProgramname;
    private List<Program> HIVPrograms = new ArrayList<Program>();


    private EncounterType outpatientOncEncounterType;
    private EncounterType inpatientOncologyEncounter;
    private List<EncounterType> oncologyEncounters = new ArrayList<EncounterType>();
    private List<Integer> EncounterTypesIds = new ArrayList<Integer>();
    private EncounterType hypertesionEncounter;
    private EncounterType heartFailureEncounter;
    private EncounterType asthmaEncounter;
    private EncounterType diabetesEncounter;
    private EncounterType CKDEncounter;
    private EncounterType epilepsyEncounter;
    private EncounterType HFHTNCKDEncounter;
    private EncounterType POSTCARDIACSURGERYVISIT;
    private List<EncounterType> NCDEncouterTypes = new ArrayList<EncounterType>();
    private EncounterType pdcEncounterType;
    private List<EncounterType> hivencounterTypeIds = new ArrayList<EncounterType>();


    private Concept oncologyprogramendreason;
    private Concept refferedOutForPalliationCareOnly;
    private Concept referredOutForPalliativeSystemicTherapy;
    private Concept ReferredOutForCurativeCancerCare;
    private Concept OtherReasonForReferral;
    private List<Concept> referredConcepts = new ArrayList<Concept>();
    private Concept reasonForExitingCare;
    private Concept patientDied;
    private Concept biopsyNegative;
    private Concept notCancerNoBiopsy;
    private Concept patientRefused;
    private Concept LostToFolloUp;
    private Concept patienttransferedoutstate;
    private Concept patientdefaulted;
    private Concept viralLoadConcept;
    private Concept hivonartstate;


    private Form exitform;
    private List<Form> exitforms = new ArrayList<Form>();


    public void setup() throws Exception {

        setUpProperties();

        Properties properties = new Properties();
        properties.setProperty("hierarchyFields", "countyDistrict:District");

        // monthly Report Definition: Start

        ReportDefinition monthlyRd = new ReportDefinition();
        monthlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        monthlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));


        monthlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));

        monthlyRd.setName("Monthly Executive Dashboard Metrics Report");

        monthlyRd.addDataSetDefinition(createMonthlyLocationDataSet(),
                ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));

        // Quarterly Report Definition: End

//        ProgramEnrollmentCohortDefinition patientEnrolledInDM = new ProgramEnrollmentCohortDefinition();
//        patientEnrolledInDM.addParameter(new Parameter("enrolledOnOrBefore", "enrolledOnOrBefore", Date.class));
//        patientEnrolledInDM.setPrograms(DMPrograms);

//        monthlyRd.setBaseCohortDefinition(patientEnrolledInDM,
//                ParameterizableUtil.createParameterMappings("enrolledOnOrBefore=${endDate}"));

        Helper.saveReportDefinition(monthlyRd);

        ReportDesign monthlyDesign = Helper.createRowPerPatientXlsOverviewReportDesign(monthlyRd,
                "MonthlyExecutiveDashboardMetricsReport.xls", "MonthlyExecutiveDashboardMetricsReport", null);
        Properties monthlyProps = new Properties();
        monthlyProps.put("repeatingSections", "sheet:1,dataset:Encounter monthly Data Set");
        monthlyProps.put("sortWeight", "5000");
        monthlyDesign.setProperties(monthlyProps);
        Helper.saveReportDesign(monthlyDesign);

    }

    public void delete() {
        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rd : rs.getAllReportDesigns(false)) {
            if ("MonthlyExecutiveDashboardMetricsReport".equals(rd.getName())) {
                rs.purgeReportDesign(rd);
            }
        }
        Helper.purgeReportDefinition("Monthly Executive Dashboard Metrics Report");

    }


    //Create Quarterly Encounter Data set

    public LocationHierachyIndicatorDataSetDefinition createMonthlyLocationDataSet() {

        LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
                createEncounterMonthlyBaseDataSet());
        ldsd.addBaseDefinition(createQuarterlyBaseDataSet());
        ldsd.setName("Encounter monthly Data Set");
        ldsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        ldsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        ldsd.addParameter(new Parameter("location", "District", LocationHierarchy.class));

        return ldsd;
    }

    private EncounterIndicatorDataSetDefinition createEncounterMonthlyBaseDataSet() {

        EncounterIndicatorDataSetDefinition eidsd = new EncounterIndicatorDataSetDefinition();
        eidsd.setName("eidsd");
        eidsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        eidsd.addParameter(new Parameter("endDate", "End Date", Date.class));
//        createMonthlyIndicators(eidsd);
        return eidsd;
    }

//    private void createMonthlyIndicators(EncounterIndicatorDataSetDefinition dsd) {
//        //=======================================================================
//        //  A1: Total # of patient visits to DM clinic in the last quarter
//        //==================================================================
//        SqlEncounterQuery patientVisitsToClinic = new SqlEncounterQuery();
//
//        patientVisitsToClinic.setQuery("select e.encounter_id from encounter e where e.encounter_type in ("
//                + EncounterTypesIds + ") and e.encounter_datetime>= :startDate and e.encounter_datetime<= :endDate and e.voided=0 group by encounter_datetime, patient_id");
//        patientVisitsToClinic.setName("patientVisitsClinic");
//        patientVisitsToClinic.addParameter(new Parameter("startDate", "startDate", Date.class));
//        patientVisitsToClinic.addParameter(new Parameter("endDate", "endDate", Date.class));
//
//        EncounterIndicator patientVisitsToClinicQuarterIndicator = new EncounterIndicator();
//        patientVisitsToClinicQuarterIndicator.setName("patientVisitsToClinicQuarterIndicator");
//        patientVisitsToClinicQuarterIndicator.setEncounterQuery(new Mapped<EncounterQuery>(patientVisitsToClinic,
//                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")));
//
//        dsd.addColumn(patientVisitsToClinicQuarterIndicator);
//
//    }



    // create Monthly cohort Data set
    private CohortIndicatorDataSetDefinition createQuarterlyBaseDataSet() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setName("Monthly Cohort Data Set");
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        createMonthlyIndicators(dsd);
        return dsd;
    }

    private void createMonthlyIndicators(CohortIndicatorDataSetDefinition dsd) {

        GenderCohortDefinition femaleCohort = Cohorts.createFemaleCohortDefinition("femalesDefinition");

        GenderCohortDefinition maleCohort = Cohorts.createMaleCohortDefinition("maleCohort");



        SqlCohortDefinition hospitalPatients=Cohorts.createHospitalPatientCohort("hospitalPatients");



        // ONCOLOGY
        //=========================================================================
        // A1: # of Oncology Active patients (With a visit within a year and not exited)  //
        //=========================================================================

        EncounterCohortDefinition oncologypatientSeen = Cohorts.createEncounterParameterizedByDate("Patients seen",
                onOrAfterOnOrBefore, oncologyEncounters);

        InProgramCohortDefinition inOncologyProgram = Cohorts.createInProgramParameterizableByStartEndDate("inOncologyProgram",
                oncologyProgram);

        //total number
        CompositionCohortDefinition oncologyActivePatientComposition = new CompositionCohortDefinition();
        oncologyActivePatientComposition.setName("oncologyActivePatientComposition");
        oncologyActivePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        oncologyActivePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        oncologyActivePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inOncologyProgram, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));
        oncologyActivePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(oncologypatientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        oncologyActivePatientComposition.setCompositionString("1 AND 2");

        CohortIndicator oncologyActivePatientMonthlyIndicator = Indicators.newCountIndicator("oncologyActivePatientMonthlyIndicator",
                oncologyActivePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("oncologyActivePatient", "# of oncology Active patients (With a visit within a year and not exited)", new Mapped(oncologyActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        // female number
        CompositionCohortDefinition femaleOncologyActivePatientComposition = new CompositionCohortDefinition();
        femaleOncologyActivePatientComposition.setName("femaleOncologyActivePatientComposition");
        femaleOncologyActivePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleOncologyActivePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleOncologyActivePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(oncologyActivePatientComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleOncologyActivePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        femaleOncologyActivePatientComposition.setCompositionString("1 AND 2");

        CohortIndicator femaleOncologyActivePatientMonthlyIndicator = Indicators.newCountIndicator("femaleOncologyActivePatientMonthlyIndicator",
                femaleOncologyActivePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleOncologyActivePatient", "# of oncology female Active patients (With a visit within a year and not exited)", new Mapped(femaleOncologyActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=========================================================================
        // A2: # of New cases  //
        //=========================================================================
        CompositionCohortDefinition patientEnrolledInThePeriod = Cohorts.createEnrolledInProgramDuringPeriod("patientEnrolledInThePeriod", oncologyProgram);

        CompositionCohortDefinition oncologyNewCases = new CompositionCohortDefinition();
        oncologyNewCases.setName("oncologyNewCases");
        oncologyNewCases.addParameter(new Parameter("endDate", "endDate", Date.class));
        oncologyNewCases.addParameter(new Parameter("startDate", "startDate", Date.class));
        oncologyNewCases.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientEnrolledInThePeriod, ParameterizableUtil
                        .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        oncologyNewCases.setCompositionString("1");

        CohortIndicator oncologyNewCasesMonthlyIndicator = Indicators.newCountIndicator("oncologyNewCasesMonthlyIndicator",
                oncologyNewCases,
                ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
        dsd.addColumn("oncologyNewCases", "# of oncology New Cases", new Mapped(oncologyNewCasesMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //female
        CompositionCohortDefinition femaleOncologyNewCases = new CompositionCohortDefinition();
        femaleOncologyNewCases.setName("femaleOncologyNewCases");
        femaleOncologyNewCases.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleOncologyNewCases.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleOncologyNewCases.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(patientEnrolledInThePeriod, ParameterizableUtil
                        .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleOncologyNewCases.getSearches().put("2", new Mapped<CohortDefinition>(femaleCohort, null));

        femaleOncologyNewCases.setCompositionString("1 AND 2");

        CohortIndicator femaleOncologyNewCasesMonthlyIndicator = Indicators.newCountIndicator("femaleOncologyNewCasesMonthlyIndicator",
                femaleOncologyNewCases,
                ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));
        dsd.addColumn("femaleOncologyNewCases", "# of female oncology New Cases", new Mapped(femaleOncologyNewCasesMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // A3: # of Referred                                                     //
        //=========================================================================

        SqlCohortDefinition patientWithOncologyReferredOutcomes = Cohorts.getPatientsWithOutcomeprogramEndReasons("patientWithOncologyReferredOutcomes", oncologyprogramendreason, referredConcepts);


//        CompositionCohortDefinition patientWithProgramReferredOutcome = new CompositionCohortDefinition();
//        patientWithProgramReferredOutcome.setName("patientWithProgramReferredOutcome");
//        patientWithProgramReferredOutcome.addParameter(new Parameter("onOrBefore","onOrBefore",Date .class));
//        patientWithProgramReferredOutcome.addParameter(new Parameter("onOrAfter","onOrAfter",Date .class));
//        patientWithProgramReferredOutcome.getSearches().put("1",new Mapped<CohortDefinition>(patientWithOncologyReferredOutcomes, ParameterizableUtil
//                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
//        patientWithProgramReferredOutcome.setCompositionString("1");
//        CohortIndicator patientWithProgramReferredOutcomeMonthlyIndicator = Indicators.newCountIndicator("patientWithProgramReferredOutcomeMonthlyIndicator",
//                patientWithProgramReferredOutcome,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));


        CompositionCohortDefinition OncologyPatientsReferred = new CompositionCohortDefinition();
        OncologyPatientsReferred.setName("OncologyPatientsReferred");
        OncologyPatientsReferred.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        OncologyPatientsReferred.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        OncologyPatientsReferred.getSearches().put("1",new Mapped<CohortDefinition>(patientWithOncologyReferredOutcomes, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        OncologyPatientsReferred.setCompositionString("1");

        CohortIndicator patientsWithProgramReferredOutcomeMonthlyIndicator = Indicators.newCountIndicator("patientsWithProgramReferredOutcomeMonthlyIndicator",
                OncologyPatientsReferred,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

        dsd.addColumn("oncologyPatientsReferred", "# of oncology Patients Referred", new Mapped(patientsWithProgramReferredOutcomeMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        //female


        CompositionCohortDefinition femaleOncologyPatientsReferred = new CompositionCohortDefinition();
        femaleOncologyPatientsReferred.setName("femaleOncologyPatientsReferred");
        femaleOncologyPatientsReferred.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleOncologyPatientsReferred.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleOncologyPatientsReferred.getSearches().put("1",new Mapped<CohortDefinition>(patientWithOncologyReferredOutcomes, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleOncologyPatientsReferred.getSearches().put("2", new Mapped<CohortDefinition>(femaleCohort, null));
        femaleOncologyPatientsReferred.setCompositionString("1 AND 2");

        CohortIndicator femalepatientsWithProgramReferredOutcomeMonthlyIndicator = Indicators.newCountIndicator("femalepatientsWithProgramReferredOutcomeMonthlyIndicator",
                femaleOncologyPatientsReferred,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));

        dsd.addColumn("femaleoncologyPatientsReferred", "# of oncology female Patients Referred", new Mapped(femalepatientsWithProgramReferredOutcomeMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // A4: # of Deceased                                                    //
        //=========================================================================

        dsd.addColumn("oncologyPatientsDied", "# of oncology Patients Died", new Mapped(patientExitReason(inOncologyProgram,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleoncologyPatientsDied", "# of oncology female Patients Died", new Mapped(femaleExitReason(inOncologyProgram,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // A4: # of Biopsy negative                                                    //
        //=========================================================================

        SqlCohortDefinition patientWithBiopsyNegativeOutcomes = Cohorts.getPatientsWithOutcomeprogramEndReasons("patientWithBiopsyNegativeOutcomes", oncologyprogramendreason, biopsyNegative);

        dsd.addColumn("oncologyBiopsyNegativePatients", "# of Biopsy negative ", new Mapped(patientsWithProgramOutcome(patientWithBiopsyNegativeOutcomes),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleoncologyBiopsyNegativePatients", "# of oncology female Patients Died", new Mapped(femalepatientsWithProgramOutcome(patientWithBiopsyNegativeOutcomes,femaleCohort),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // A6: # of No cancer no biopsy                                          //
        //=========================================================================

        SqlCohortDefinition patientWithOncologyNoCanceNoBiopsyOutcomes = Cohorts.getPatientsWithOutcomeprogramEndReasons("patientWithOncologyNoCanceNoBiopsyOutcomes", oncologyprogramendreason, notCancerNoBiopsy);

        dsd.addColumn("OncologyNoCanceNoBiopsyOutcomes", "# of oncology Patients Not Cancer no biopsy outcomes", new Mapped(patientsWithProgramOutcome(patientWithOncologyNoCanceNoBiopsyOutcomes),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleOncologyNoCanceNoBiopsyOutcomes", "# of oncology female Patients with Not Cancer no biopsy outcomes", new Mapped(femalepatientsWithProgramOutcome(patientWithOncologyNoCanceNoBiopsyOutcomes,femaleCohort),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        //=========================================================================
        // A7: # of Patient refused                                              //
        //=========================================================================

        SqlCohortDefinition patientWithOncologypatientRefusedOutcomes = Cohorts.getPatientsWithOutcomeprogramEndReasons("patientWithOncologypatientRefusedOutcomes", oncologyprogramendreason, patientRefused);

        dsd.addColumn("OncologypatientRefusedOutcomes", "# of oncology Patients patient refused outcomes", new Mapped(patientsWithProgramOutcome(patientWithOncologypatientRefusedOutcomes),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleOncologypatientRefusedOutcomes", "# of oncology female Patients with patient refused outcomes", new Mapped(femalepatientsWithProgramOutcome(patientWithOncologypatientRefusedOutcomes,femaleCohort),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // A8: # of Lost To FolloUp                                             //
        //=========================================================================

        SqlCohortDefinition patientWithOncologyLostToFolloUpOutcomes = Cohorts.getPatientsWithOutcomeprogramEndReasons("patientWithOncologyLostToFolloUpOutcomes", oncologyprogramendreason, LostToFolloUp);

        dsd.addColumn("patientWithOncologyLostToFolloUpOutcomes", "# of oncology Patients patient lost to followup outcomes", new Mapped(patientsWithProgramOutcome(patientWithOncologyLostToFolloUpOutcomes),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femalepatientWithOncologyLostToFolloUpOutcomes", "# of oncology female Patients with patient lost to followup outcomes", new Mapped(femalepatientsWithProgramOutcome(patientWithOncologyLostToFolloUpOutcomes,femaleCohort),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        //NCD HC

        //=============================================================================
        // B1: # of Active NCD patients (With a visit within a year and not exited)  //
        //============================================================================

        EncounterCohortDefinition NCDpatientSeen = Cohorts.createEncounterParameterizedByDate("NCD Patients seen",
                onOrAfterOnOrBefore, NCDEncouterTypes);

        InProgramCohortDefinition inNCDPrograms = Cohorts.createInProgramParameterizableByStartEndDate("inNCDPrograms",
                NCDPrograms);

        //total number
        CompositionCohortDefinition NCDActivePatientComposition = new CompositionCohortDefinition();
        NCDActivePatientComposition.setName("NCDActivePatientComposition");
        NCDActivePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        NCDActivePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        NCDActivePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inNCDPrograms, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));
        NCDActivePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(NCDpatientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        NCDActivePatientComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));
        NCDActivePatientComposition.setCompositionString("1 AND 2 AND (NOT 3)");

        CohortIndicator NCDActivePatientMonthlyIndicator = Indicators.newCountIndicator("NCDActivePatientMonthlyIndicator",
                NCDActivePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("NCDActivePatient", "# of NCD Active patients (With a visit within a year and not exited)", new Mapped(NCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        // female number
        CompositionCohortDefinition femaleNCDActivePatientComposition = new CompositionCohortDefinition();
        femaleNCDActivePatientComposition.setName("femaleNCDActivePatientComposition");
        femaleNCDActivePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleNCDActivePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleNCDActivePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(NCDActivePatientComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleNCDActivePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        femaleNCDActivePatientComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        femaleNCDActivePatientComposition.setCompositionString("1 AND 2 AND NOT 3");

        CohortIndicator femaleNCDActivePatientMonthlyIndicator = Indicators.newCountIndicator("femaleNCDActivePatientMonthlyIndicator",
                femaleNCDActivePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleNCDActivePatient", "# of NCD female Active patients (With a visit within a year and not exited)", new Mapped(femaleNCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //=========================================================================
        // B2: # of  Asthma patients who died                                    //
        //=========================================================================

        InProgramCohortDefinition inAsthmaProgram = Cohorts.createInProgramParameterizableByStartEndDate("inAsthmaProgram",
                asthmaProgram);

        dsd.addColumn("AsthmaPatientsDied", "# of Asthma Patients Died", new Mapped(patientExitReason(inAsthmaProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleAsthmaPatientsDied", "# of Asthma female Patients Died", new Mapped(femaleExitReason(inAsthmaProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
        //=========================================================================
        // B3: # of  Diabetes patients who died                                  //
        //=========================================================================

        InProgramCohortDefinition inDiabetesProgram = Cohorts.createInProgramParameterizableByStartEndDate("inDiabetesProgram",
                diabetesProgram);

        dsd.addColumn("DiabetesPatientsDied", "# of Diabetes Patients Died", new Mapped(patientExitReason(inDiabetesProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleDiabetesPatientsDied", "# of Diabetes female Patients Died", new Mapped(femaleExitReason(inDiabetesProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // B4: # of  Heart Failure patients who died                             //
        //=========================================================================

        InProgramCohortDefinition inHFProgram = Cohorts.createInProgramParameterizableByStartEndDate("inHFProgram",
                heartFailureProgram);

        dsd.addColumn("HeartFailurePatientsDied", "# of Heart Failure Patients Died", new Mapped(patientExitReason(inHFProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleHeartFailurePatientsDied", "# of Diabetes female Patients Died", new Mapped(femaleExitReason(inHFProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // B5: # of  Hypertension patients who died                              //
        //=========================================================================

        InProgramCohortDefinition inHypertensionProgram = Cohorts.createInProgramParameterizableByStartEndDate("inHypertensionProgram",
                hypertesionProgram);

        dsd.addColumn("HypertensionPatientsDied", "# of Hypertension Patients Died", new Mapped(patientExitReason(inHypertensionProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleHypertensionPatientsDied", "# of Hypertension female Patients Died", new Mapped(femaleExitReason(inHypertensionProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        //=========================================================================
        // B6: # of  Patients who have had a visit within the year               //
        //=========================================================================
        EncounterCohortDefinition NCDpatientsSeen = Cohorts.createEncounterParameterizedByDate("NCD Patients seen",
                onOrAfterOnOrBefore, NCDEncouterTypes);


        CompositionCohortDefinition NCDpatientsSeenComposition = new CompositionCohortDefinition();
        NCDpatientsSeenComposition.setName("NCDpatientsSeenComposition");
        NCDpatientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        NCDpatientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//
        NCDpatientsSeenComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(NCDpatientsSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        NCDpatientsSeenComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(inNCDPrograms, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        NCDpatientsSeenComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));
        NCDpatientsSeenComposition.setCompositionString("1 AND 2 AND NOT 3");

        CohortIndicator NCDpatientsSeenMonthlyIndicator = Indicators.newCountIndicator("NCDpatientsSeenMonthlyIndicator",
                NCDpatientsSeenComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("NCDpatientsSeenInaYear", "# of NCD patients Seen In a Year", new Mapped(NCDpatientsSeenMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // female number
        CompositionCohortDefinition femaleNCDpatientsSeenComposition = new CompositionCohortDefinition();
        femaleNCDpatientsSeenComposition.setName("femaleNCDpatientsSeenComposition");
        femaleNCDpatientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleNCDpatientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleNCDpatientsSeenComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(NCDpatientsSeenComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleNCDpatientsSeenComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        femaleNCDpatientsSeenComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        femaleNCDpatientsSeenComposition.setCompositionString("1 AND 2 AND NOT 3");

        CohortIndicator femaleNCDpatientsSeenMonthlyIndicator = Indicators.newCountIndicator("femaleNCDpatientsSeenMonthlyIndicator",
                femaleNCDpatientsSeenComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleNCDpatientsSeenInaYear", "# of female NCD patients Seen In a Year", new Mapped(femaleNCDpatientsSeenMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=========================================================================
        // B7: # of  Active Patients act the beginning of the period             //
        //=========================================================================


        dsd.addColumn("NCDActivePatientAtTheBeginningOfperiod", "# of NCD Active patients (With a visit within a year and not exited) At The Beginning Of period ", new Mapped(NCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${startDate}")), "");

        dsd.addColumn("femaleNCDActivePatientAtTheBeginningOfperiod", "# of NCD female Active patients (With a visit within a year and not exited) At The Beginning Of period", new Mapped(femaleNCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${startDate}")), "");


        //=========================================================================
        // B8: Of active asthma patients, % with no visit in 6 months            //
        //=========================================================================



//        EncounterCohortDefinition AsthmapatientsSeenInPeriod = Cohorts.createEncounterParameterizedByDate("Asthma Patients seen",
//                onOrAfterOnOrBefore, asthmaEncounter);



        CompositionCohortDefinition AsthmaActivePatientsComposition = activePatientOfADisease(NCDpatientSeen,hospitalPatients,inAsthmaProgram);
        CohortIndicator AsthmaActivePatientsIndicator = Indicators.newCountIndicator("AsthmaActivePatientsIndicator",
                AsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("asthmaActivePatients", "# of active asthma patients ", new Mapped(AsthmaActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition AsthmaActivePatientsNotSeenIn6MonthsComposition = ActivePatientsNotSeenIn6MonthsComposition(AsthmaActivePatientsComposition,hospitalPatients,NCDpatientSeen);

        CohortIndicator AsthmaActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("AsthmaActivePatientsNotSeenIn6MonthsIndicator",
                AsthmaActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        /*dsd.addColumn("asthmaactivePatientsNotSeenIn6Months", "# of active asthma patients with no visit in 6 months", new Mapped(AsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/
        CohortIndicator percentAsthmaActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("percentAsthmaActivePatientsNotSeenIn6MonthsIndicator",AsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),AsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("asthmaactivePatientsNotSeenIn6Months", "% of active asthma patients with no visit in 6 months", new Mapped(percentAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // female number
        CompositionCohortDefinition femaleAsthmaActivePatientsComposition = femaleComposition(AsthmaActivePatientsComposition,femaleCohort);

        CohortIndicator femaleAsthmaActivePatientsIndicator = Indicators.newCountIndicator("femaleAsthmaActivePatientsIndicator",
                femaleAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleasthmaActivePatients", "# of female active asthma patients with no visit in 6 months", new Mapped(femaleAsthmaActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition femaleAsthmaActivePatientsNotSeenIn6MonthsComposition = femaleComposition(AsthmaActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator femaleAsthmaActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("femaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",
                femaleAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleAsthmaactivePatientsNotSeenIn6Months", "# of female active asthma patients with no visit in 6 months", new Mapped(femaleAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator percentfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("percentfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",femaleAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),femaleAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("femaleAsthmaactivePatientsNotSeenIn6Months", "# of female active asthma patients with no visit in 6 months", new Mapped(percentfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // Male number
        CompositionCohortDefinition maleAsthmaActivePatientsComposition = femaleComposition(AsthmaActivePatientsComposition,maleCohort);
        CompositionCohortDefinition maleAsthmaActivePatientsNotSeenIn6MonthsComposition = femaleComposition(AsthmaActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator percentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("percentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",maleAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),maleAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("maleAsthmaactivePatientsNotSeenIn6Months", "# of male active asthma patients with no visit in 6 months", new Mapped(percentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=========================================================================
        // B9: Of active Diabetes patients, % with no visit in 6 months            //
        //=========================================================================



//        EncounterCohortDefinition diabetesPatientsSeenInPeriod = Cohorts.createEncounterParameterizedByDate("Diabetes Patients seen",
//                onOrAfterOnOrBefore, diabetesEncounter);



        CompositionCohortDefinition DiabetesActivePatientsComposition = activePatientOfADisease(NCDpatientSeen,hospitalPatients,inDiabetesProgram);
        CohortIndicator DiabetesActivePatientsIndicator = Indicators.newCountIndicator("DiabetesActivePatientsIndicator",
                DiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("diabetesActivePatients", "# of active Diabetes patients ", new Mapped(DiabetesActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition DiabetesActivePatientsNotSeenIn6MonthsComposition = ActivePatientsNotSeenIn6MonthsComposition(DiabetesActivePatientsComposition,hospitalPatients,NCDpatientSeen);

        CohortIndicator DiabetesActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("DiabetesActivePatientsNotSeenIn6MonthsIndicator",
                DiabetesActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        /*dsd.addColumn("activeDiabetesPatientsNotSeenIn6Months", "# of active Diabetes patients with no visit in 6 months", new Mapped(DiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator percentDiabetesActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("percentDiabetesActivePatientsNotSeenIn6MonthsIndicator",DiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),DiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("activeDiabetesPatientsNotSeenIn6Months", "% of active Diabetes patients with no visit in 6 months", new Mapped(percentDiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // female number
        CompositionCohortDefinition femaleDiabetesActivePatientsComposition = femaleComposition(DiabetesActivePatientsComposition,femaleCohort);

        CohortIndicator femaleDiabetesActivePatientsIndicator = Indicators.newCountIndicator("femaleDiabetesActivePatientsIndicator",
                femaleDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleDiabetesActivePatients", "# of female active Diabetes patients with no visit in 6 months", new Mapped(femaleDiabetesActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition femaleDiabetesActivePatientsNotSeenIn6MonthsComposition = femaleComposition(DiabetesActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator femaleDiabetesActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("femaleDiabetesActivePatientsNotSeenIn6MonthsIndicator",
                femaleDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleactiveDiabetesPatientsNotSeenIn6Months", "# of female active Diabetes patients with no visit in 6 months", new Mapped(femaleDiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator percentfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("percentfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator",femaleDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),femaleDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("femaleactiveDiabetesPatientsNotSeenIn6Months", "# of female active asthma patients with no visit in 6 months", new Mapped(percentfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // Male number
        CompositionCohortDefinition maleDiabetesActivePatientsComposition = femaleComposition(DiabetesActivePatientsComposition,maleCohort);
        CompositionCohortDefinition maleDiabetesActivePatientsNotSeenIn6MonthsComposition = femaleComposition(DiabetesActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator percentmaleDiabetesActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",maleDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),maleDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("maleDiabetesactivePatientsNotSeenIn6Months", "# of male active Diabetes patients with no visit in 6 months", new Mapped(percentmaleDiabetesActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        //=========================================================================
        // B10: Of active Heart failure patients, % with no visit in 6 months            //
        //=========================================================================



//        EncounterCohortDefinition heartFailurePatientsSeenInPeriod = Cohorts.createEncounterParameterizedByDate("Heart failure Patients seen",
//                onOrAfterOnOrBefore, heartFailureEncounter);



        CompositionCohortDefinition heartFailureActivePatientsComposition = activePatientOfADisease(NCDpatientSeen,hospitalPatients,inHFProgram);
        CohortIndicator heartFailureActivePatientsIndicator = Indicators.newCountIndicator("heartFailureActivePatientsIndicator",
                heartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("heartFailureActivePatients", "# of active heart Failure patients ", new Mapped(heartFailureActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition heartFailureActivePatientsNotSeenIn6MonthsComposition = ActivePatientsNotSeenIn6MonthsComposition(heartFailureActivePatientsComposition,hospitalPatients,NCDpatientSeen);

        CohortIndicator heartFailureActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("heartFailureActivePatientsNotSeenIn6MonthsIndicator",
                heartFailureActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("heartFailureactivePatientsNotSeenIn6Months", "# of active heart Failure patients with no visit in 6 months", new Mapped(heartFailureActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/


        CohortIndicator percentheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",heartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),heartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("heartFailureactivePatientsNotSeenIn6Months", "% of active Heart failure patients with no visit in 6 months", new Mapped(percentheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        // female number
        CompositionCohortDefinition femaleheartFailureActivePatientsComposition = femaleComposition(heartFailureActivePatientsComposition,femaleCohort);

        CohortIndicator femaleheartFailureActivePatientsIndicator = Indicators.newCountIndicator("femaleheartFailureActivePatientsIndicator",
                femaleheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleheartFailureActivePatients", "# of female active heart Failure patients with no visit in 6 months", new Mapped(femaleheartFailureActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition femaleheartFailureActivePatientsNotSeenIn6MonthsComposition = femaleComposition(heartFailureActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator femaleheartFailureActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("femaleheartFailureActivePatientsNotSeenIn6MonthsIndicator",
                femaleheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleheartFailureactivePatientsNotSeenIn6Months", "# of female active heart Failure patients with no visit in 6 months", new Mapped(femaleheartFailureActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");*/

        CohortIndicator percentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",femaleheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),femaleheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("femaleheartFailureactivePatientsNotSeenIn6Months", "% of female active Heart failure patients with no visit in 6 months", new Mapped(percentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // Male number
        CompositionCohortDefinition maleheartFailureActivePatientsComposition = femaleComposition(heartFailureActivePatientsComposition,maleCohort);
        CompositionCohortDefinition maleheartFailureActivePatientsNotSeenIn6MonthsComposition = femaleComposition(heartFailureActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator percentmalemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentmalemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",maleheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),maleheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("maleheartFailureactivePatientsNotSeenIn6Months", "% of male active Heart Failure patients with no visit in 6 months", new Mapped(percentmalemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        //=========================================================================
        // B11: Of active Hypertension patients, % with no visit in 6 months            //
        //=========================================================================



//        EncounterCohortDefinition HypertensionPatientsSeenInPeriod = Cohorts.createEncounterParameterizedByDate("Hypertension Patients seen",
//                onOrAfterOnOrBefore, hypertesionEncounter);



        CompositionCohortDefinition HypertensionActivePatientsComposition = activePatientOfADisease(NCDpatientSeen,hospitalPatients,inHypertensionProgram);
        CohortIndicator HypertensionActivePatientsIndicator = Indicators.newCountIndicator("HypertensionActivePatientsIndicator",
                HypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("HypertensionActivePatients", "# of active heart Failure patients ", new Mapped(HypertensionActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition HypertensionActivePatientsNotSeenIn6MonthsComposition = ActivePatientsNotSeenIn6MonthsComposition(HypertensionActivePatientsComposition,hospitalPatients,NCDpatientSeen);
        CohortIndicator HypertensionActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("HypertensionActivePatientsNotSeenIn6MonthsIndicator",
                HypertensionActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("HypertensionactivePatientsNotSeenIn6Months", "# of active heart Failure patients with no visit in 6 months", new Mapped(HypertensionActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator percentHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator",HypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),HypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("HypertensionactivePatientsNotSeenIn6Months", "% of active Hypertension patients with no visit in 6 months", new Mapped(percentHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // female number
        CompositionCohortDefinition femaleHypertensionActivePatientsComposition = femaleComposition(HypertensionActivePatientsComposition,femaleCohort);

        CohortIndicator femaleHypertensionActivePatientsIndicator = Indicators.newCountIndicator("femaleHypertensionActivePatientsIndicator",
                femaleHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleHypertensionActivePatients", "# of female active Hypertension patients with no visit in 6 months", new Mapped(femaleHypertensionActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition femaleHypertensionActivePatientsNotSeenIn6MonthsComposition = femaleComposition(HypertensionActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator femaleHypertensionActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("femaleHypertensionActivePatientsNotSeenIn6MonthsIndicator",
                femaleHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleHypertensionactivePatientsNotSeenIn6Months", "# of female active heart Failure patients with no visit in 6 months", new Mapped(femaleHypertensionActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/


        CohortIndicator percentfemaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",femaleHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),femaleHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("femaleHypertensionactivePatientsNotSeenIn6Months", "% of female active Hypertension patients with no visit in 6 months", new Mapped(percentfemaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        // Male number
        CompositionCohortDefinition maleHypertensionActivePatientsComposition = femaleComposition(HypertensionActivePatientsComposition,maleCohort);
        CompositionCohortDefinition maleHypertensionActivePatientsNotSeenIn6MonthsComposition = femaleComposition(HypertensionActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator percentmaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("percentmaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator",maleHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),maleHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("maleheartFailureactivePatientsNotSeenIn6Months", "% of male active Hypertension patients with no visit in 6 months", new Mapped(percentmaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        //NCD Hospital

        //=============================================================================
        // B1: # of Active NCD patients (With a visit within a year and not exited)  //
        //============================================================================

       //total number
        CompositionCohortDefinition hospitalNCDActivePatientComposition = new CompositionCohortDefinition();
        hospitalNCDActivePatientComposition.setName("NCDActivePatientComposition");
        hospitalNCDActivePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        hospitalNCDActivePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        hospitalNCDActivePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inNCDPrograms, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));
        hospitalNCDActivePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(NCDpatientSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        hospitalNCDActivePatientComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));
        hospitalNCDActivePatientComposition.setCompositionString("1 AND 2 AND 3");

        CohortIndicator hospitalNCDActivePatientMonthlyIndicator = Indicators.newCountIndicator("hospitalNCDActivePatientMonthlyIndicator",
                hospitalNCDActivePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalNCDActivePatient", "# of hospital NCD Active patients (With a visit within a year and not exited)", new Mapped(hospitalNCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        // female number
        CompositionCohortDefinition hospitalfemaleNCDActivePatientComposition = new CompositionCohortDefinition();
        hospitalfemaleNCDActivePatientComposition.setName("hospitalfemaleNCDActivePatientComposition");
        hospitalfemaleNCDActivePatientComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        hospitalfemaleNCDActivePatientComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        hospitalfemaleNCDActivePatientComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(NCDActivePatientComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        hospitalfemaleNCDActivePatientComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        hospitalfemaleNCDActivePatientComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        hospitalfemaleNCDActivePatientComposition.setCompositionString("1 AND 2 AND 3");

        CohortIndicator hospitalfemaleNCDActivePatientMonthlyIndicator = Indicators.newCountIndicator("hospitalfemaleNCDActivePatientMonthlyIndicator",
                hospitalfemaleNCDActivePatientComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalfemaleNCDActivePatient", "# of hospital NCD female Active patients (With a visit within a year and not exited)", new Mapped(hospitalfemaleNCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //=========================================================================
        // B2: # of  Asthma patients who died                                    //
        //=========================================================================

        dsd.addColumn("hospitalAsthmaPatientsDied", "# of Hospital Asthma Patients Died", new Mapped(hospitalPatientExitReason(inAsthmaProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("hospitalfemaleAsthmaPatientsDied", "# of Hospital Asthma female Patients Died", new Mapped(hospitalFemaleExitReason(inAsthmaProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");
        //=========================================================================
        // B3: # of  Diabetes patients who died                                  //
        //=========================================================================

       dsd.addColumn("hospitalDiabetesPatientsDied", "# of Hospital Diabetes Patients Died", new Mapped(hospitalPatientExitReason(inDiabetesProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("hospitalfemaleDiabetesPatientsDied", "# of Hospital Diabetes female Patients Died", new Mapped(hospitalFemaleExitReason(inDiabetesProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // B4: # of  Heart Failure patients who died                             //
        //=========================================================================

        dsd.addColumn("hospitalHeartFailurePatientsDied", "# of Hospital Heart Failure Patients Died", new Mapped(hospitalPatientExitReason(inHFProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("hospitalfemaleHeartFailurePatientsDied", "# of Hospital Diabetes female Patients Died", new Mapped(hospitalFemaleExitReason(inHFProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // B5: # of  Hypertension patients who died                              //
        //=========================================================================

       dsd.addColumn("hospitalHypertensionPatientsDied", "# of Hospital Hypertension Patients Died", new Mapped(hospitalPatientExitReason(inHypertensionProgram,hospitalPatients,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("hospitalfemaleHypertensionPatientsDied", "# of Hospital Hypertension female Patients Died", new Mapped(hospitalFemaleExitReason(inHypertensionProgram,hospitalPatients,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        //=========================================================================
        // B6: # of  Patients who have had a visit within the year               //
        //=========================================================================
        CompositionCohortDefinition hospitalNCDpatientsSeenComposition = new CompositionCohortDefinition();
        hospitalNCDpatientsSeenComposition.setName("hospitalNCDpatientsSeenComposition");
        hospitalNCDpatientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        hospitalNCDpatientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//
        hospitalNCDpatientsSeenComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(NCDpatientsSeen, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        hospitalNCDpatientsSeenComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(inNCDPrograms, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        hospitalNCDpatientsSeenComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));
        hospitalNCDpatientsSeenComposition.setCompositionString("1 AND 2 AND 3");

        CohortIndicator hospitalNCDpatientsSeenMonthlyIndicator = Indicators.newCountIndicator("hospitalNCDpatientsSeenMonthlyIndicator",
                hospitalNCDpatientsSeenComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hosptitalNCDpatientsSeenInaYear", "# of hospital NCD patients Seen In a Year", new Mapped(hospitalNCDpatientsSeenMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // female number
        CompositionCohortDefinition hospitalfemaleNCDpatientsSeenComposition = new CompositionCohortDefinition();
        hospitalfemaleNCDpatientsSeenComposition.setName("hospitalfemaleNCDpatientsSeenComposition");
        hospitalfemaleNCDpatientsSeenComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        hospitalfemaleNCDpatientsSeenComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        hospitalfemaleNCDpatientsSeenComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(NCDpatientsSeenComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        hospitalfemaleNCDpatientsSeenComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        hospitalfemaleNCDpatientsSeenComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        hospitalfemaleNCDpatientsSeenComposition.setCompositionString("1 AND 2 AND 3");

        CohortIndicator hospitalfemaleNCDpatientsSeenMonthlyIndicator = Indicators.newCountIndicator("hospitalfemaleNCDpatientsSeenMonthlyIndicator",
                hospitalfemaleNCDpatientsSeenComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalfemaleNCDpatientsSeenInaYear", "# of hospital female NCD patients Seen In a Year", new Mapped(hospitalfemaleNCDpatientsSeenMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=========================================================================
        // B7: # of  Active Patients act the beginning of the period             //
        //=========================================================================


        dsd.addColumn("hospitalNCDActivePatientAtTheBeginningOfperiod", "# of hospital NCD Active patients (With a visit within a year and not exited) At The Beginning Of period ", new Mapped(hospitalNCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${startDate}")), "");

        dsd.addColumn("hospitalfemaleNCDActivePatientAtTheBeginningOfperiod", "# of hospital NCD female Active patients (With a visit within a year and not exited) At The Beginning Of period", new Mapped(hospitalfemaleNCDActivePatientMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${startDate}")), "");


        //=========================================================================
        // B8: Of active asthma patients, % with no visit in 6 months            //
        //=========================================================================

        CompositionCohortDefinition hospitalAsthmaActivePatientsComposition = hospitalActivePatientOfADisease(NCDpatientSeen,hospitalPatients,inAsthmaProgram);
        CohortIndicator hospitalAsthmaActivePatientsIndicator = Indicators.newCountIndicator("hospitalAsthmaActivePatientsIndicator",
                hospitalAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalasthmaActivePatients", "# of hospital active asthma patients ", new Mapped(hospitalAsthmaActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition hospitalAsthmaActivePatientsNotSeenIn6MonthsComposition = hospitalActivePatientsNotSeenIn6MonthsComposition(AsthmaActivePatientsComposition,hospitalPatients,NCDpatientSeen);

        CohortIndicator hospitalAsthmaActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalAsthmaActivePatientsNotSeenIn6MonthsIndicator",
                hospitalAsthmaActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        /*dsd.addColumn("asthmaactivePatientsNotSeenIn6Months", "# of active asthma patients with no visit in 6 months", new Mapped(AsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/
        CohortIndicator hospitalpercentAsthmaActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("hospitalpercentAsthmaActivePatientsNotSeenIn6MonthsIndicator",hospitalAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalasthmaactivePatientsNotSeenIn6Months", "% of hospital active asthma patients with no visit in 6 months", new Mapped(hospitalpercentAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // female number
        CompositionCohortDefinition hospitalfemaleAsthmaActivePatientsComposition = femaleComposition(hospitalAsthmaActivePatientsComposition,femaleCohort);

        CohortIndicator hospitalfemaleAsthmaActivePatientsIndicator = Indicators.newCountIndicator("hospitalfemaleAsthmaActivePatientsIndicator",
                hospitalfemaleAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalfemaleasthmaActivePatients", "# of hospital female active asthma patients with no visit in 6 months", new Mapped(hospitalfemaleAsthmaActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition hospitalfemaleAsthmaActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalAsthmaActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator hospitalfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",
                hospitalfemaleAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleAsthmaactivePatientsNotSeenIn6Months", "# of female active asthma patients with no visit in 6 months", new Mapped(femaleAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator hospitalpercentfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("hospitalpercentfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",hospitalfemaleAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalfemaleAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalfemaleAsthmaactivePatientsNotSeenIn6Months", "# of hospital female active asthma patients with no visit in 6 months", new Mapped(hospitalpercentfemaleAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // Male number
        CompositionCohortDefinition hospitalmaleAsthmaActivePatientsComposition = femaleComposition(hospitalAsthmaActivePatientsComposition,maleCohort);
        CompositionCohortDefinition hospitalmaleAsthmaActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalAsthmaActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator hospitalpercentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("hospitalpercentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",hospitalmaleAsthmaActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalmaleAsthmaActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalmaleAsthmaactivePatientsNotSeenIn6Months", "# of hospital male active asthma patients with no visit in 6 months", new Mapped(hospitalpercentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=========================================================================
        // B9: Of active Diabetes patients, % with no visit in 6 months            //
        //=========================================================================

        CompositionCohortDefinition hospitalDiabetesActivePatientsComposition = hospitalActivePatientOfADisease(NCDpatientSeen,hospitalPatients,inDiabetesProgram);
        CohortIndicator hospitalDiabetesActivePatientsIndicator = Indicators.newCountIndicator("DiabetesActivePatientsIndicator",
                hospitalDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitaldiabetesActivePatients", "# of hospital active Diabetes patients ", new Mapped(hospitalDiabetesActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition hospitalDiabetesActivePatientsNotSeenIn6MonthsComposition = ActivePatientsNotSeenIn6MonthsComposition(hospitalDiabetesActivePatientsComposition,hospitalPatients,NCDpatientSeen);

        CohortIndicator hospitalDiabetesActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalDiabetesActivePatientsNotSeenIn6MonthsIndicator",
                hospitalDiabetesActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        /*dsd.addColumn("activeDiabetesPatientsNotSeenIn6Months", "# of active Diabetes patients with no visit in 6 months", new Mapped(DiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator hospitalpercentDiabetesActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("hosptalpercentDiabetesActivePatientsNotSeenIn6MonthsIndicator",hospitalDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalactiveDiabetesPatientsNotSeenIn6Months", "% of hospital active Diabetes patients with no visit in 6 months", new Mapped(hospitalpercentDiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // female number
        CompositionCohortDefinition hospitalfemaleDiabetesActivePatientsComposition = femaleComposition(hospitalDiabetesActivePatientsComposition,femaleCohort);

        CohortIndicator hospitalfemaleDiabetesActivePatientsIndicator = Indicators.newCountIndicator("hospitalfemaleDiabetesActivePatientsIndicator",
                hospitalfemaleDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalfemaleDiabetesActivePatients", "# of female active Diabetes patients with no visit in 6 months", new Mapped(hospitalfemaleDiabetesActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition hospitalfemaleDiabetesActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalDiabetesActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator hospitalfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator",
                hospitalfemaleDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleactiveDiabetesPatientsNotSeenIn6Months", "# of female active Diabetes patients with no visit in 6 months", new Mapped(femaleDiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator hospitalpercentfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator=Indicators.newFractionIndicator("hospitalpercentfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator",hospitalfemaleDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalfemaleDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalfemaleactiveDiabetesPatientsNotSeenIn6Months", "# of female active asthma patients with no visit in 6 months", new Mapped(hospitalpercentfemaleDiabetesActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // Male number
        CompositionCohortDefinition hospitalmaleDiabetesActivePatientsComposition = femaleComposition(hospitalDiabetesActivePatientsComposition,maleCohort);
        CompositionCohortDefinition hospitalmaleDiabetesActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalDiabetesActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator hospitalpercentmaleDiabetesActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitakpercentmaleAsthmaActivePatientsNotSeenIn6MonthsIndicator",hospitalmaleDiabetesActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalmaleDiabetesActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalmaleDiabetesactivePatientsNotSeenIn6Months", "# of hospital male active Diabetes patients with no visit in 6 months", new Mapped(hospitalpercentmaleDiabetesActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        //=========================================================================
        // B10: Of active Heart failure patients, % with no visit in 6 months            //
        //=========================================================================

        CompositionCohortDefinition hospitalheartFailureActivePatientsComposition = hospitalActivePatientOfADisease(NCDpatientSeen,hospitalPatients,inHFProgram);
        CohortIndicator hospitalheartFailureActivePatientsIndicator = Indicators.newCountIndicator("hospitalheartFailureActivePatientsIndicator",
                hospitalheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalheartFailureActivePatients", "# of hospital active heart Failure patients ", new Mapped(hospitalheartFailureActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition hospitalheartFailureActivePatientsNotSeenIn6MonthsComposition = hospitalActivePatientsNotSeenIn6MonthsComposition(heartFailureActivePatientsComposition,hospitalPatients,NCDpatientSeen);

        CohortIndicator hospitalheartFailureActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalheartFailureActivePatientsNotSeenIn6MonthsIndicator",
                hospitalheartFailureActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("heartFailureactivePatientsNotSeenIn6Months", "# of active heart Failure patients with no visit in 6 months", new Mapped(heartFailureActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/


        CohortIndicator hospitalpercentheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitalpercentheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",hospitalheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalheartFailureactivePatientsNotSeenIn6Months", "% of hospital active Heart failure patients with no visit in 6 months", new Mapped(hospitalpercentheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        // female number
        CompositionCohortDefinition hospitalfemaleheartFailureActivePatientsComposition = femaleComposition(hospitalheartFailureActivePatientsComposition,femaleCohort);

        CohortIndicator hospitalfemaleheartFailureActivePatientsIndicator = Indicators.newCountIndicator("hospitalfemaleheartFailureActivePatientsIndicator",
                hospitalfemaleheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalfemaleheartFailureActivePatients", "# of hospital female active heart Failure patients with no visit in 6 months", new Mapped(hospitalfemaleheartFailureActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition hospitalfemaleheartFailureActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalheartFailureActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator hospitalfemaleheartFailureActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalfemaleheartFailureActivePatientsNotSeenIn6MonthsIndicator",
                hospitalfemaleheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleheartFailureactivePatientsNotSeenIn6Months", "# of female active heart Failure patients with no visit in 6 months", new Mapped(femaleheartFailureActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");*/

        CohortIndicator hospitalpercentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitalpercentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",hospitalfemaleheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalfemaleheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalfemaleheartFailureactivePatientsNotSeenIn6Months", "% of female active Heart failure patients with no visit in 6 months", new Mapped(hospitalpercentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // Male number
        CompositionCohortDefinition hospitalmaleheartFailureActivePatientsComposition = femaleComposition(hospitalheartFailureActivePatientsComposition,maleCohort);
        CompositionCohortDefinition hospitalmaleheartFailureActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalheartFailureActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator hospitalpercentmalemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitalpercentmalemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",hospitalmaleheartFailureActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalmaleheartFailureActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalmaleheartFailureactivePatientsNotSeenIn6Months", "% of male hospital active Heart Failure patients with no visit in 6 months", new Mapped(hospitalpercentmalemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        //=========================================================================
        // B11: Of active Hypertension patients, % with no visit in 6 months            //
        //=========================================================================



//        EncounterCohortDefinition HypertensionPatientsSeenInPeriod = Cohorts.createEncounterParameterizedByDate("Hypertension Patients seen",
//                onOrAfterOnOrBefore, hypertesionEncounter);



        CompositionCohortDefinition hospitalHypertensionActivePatientsComposition = hospitalActivePatientOfADisease(NCDpatientSeen,hospitalPatients,inHypertensionProgram);
        CohortIndicator hospitalHypertensionActivePatientsIndicator = Indicators.newCountIndicator("HypertensionActivePatientsIndicator",
                hospitalHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("HypertensionActivePatients", "# of hospital active heart Failure patients ", new Mapped(hospitalHypertensionActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");




        CompositionCohortDefinition hospitalHypertensionActivePatientsNotSeenIn6MonthsComposition = hospitalActivePatientsNotSeenIn6MonthsComposition(HypertensionActivePatientsComposition,hospitalPatients,NCDpatientSeen);
        CohortIndicator hospitalHypertensionActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalHypertensionActivePatientsNotSeenIn6MonthsIndicator",
                hospitalHypertensionActivePatientsNotSeenIn6MonthsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("HypertensionactivePatientsNotSeenIn6Months", "# of active heart Failure patients with no visit in 6 months", new Mapped(HypertensionActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/

        CohortIndicator hospitalpercentHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitalpercentHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator",hospitalHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalHypertensionactivePatientsNotSeenIn6Months", "% of hospital active Hypertension patients with no visit in 6 months", new Mapped(hospitalpercentHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        // female number
        CompositionCohortDefinition hospitalfemaleHypertensionActivePatientsComposition = femaleComposition(hospitalHypertensionActivePatientsComposition,femaleCohort);

        CohortIndicator hospitalfemaleHypertensionActivePatientsIndicator = Indicators.newCountIndicator("hospitalfemaleHypertensionActivePatientsIndicator",
                hospitalfemaleHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("hospitalfemaleHypertensionActivePatients", "# of hospital female active Hypertension patients with no visit in 6 months", new Mapped(hospitalfemaleHypertensionActivePatientsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        CompositionCohortDefinition hospitalfemaleHypertensionActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalHypertensionActivePatientsNotSeenIn6MonthsComposition,femaleCohort);

        CohortIndicator hospitalfemaleHypertensionActivePatientsNotSeenIn6MonthsIndicator = Indicators.newCountIndicator("hospitalfemaleHypertensionActivePatientsNotSeenIn6MonthsIndicator",
                hospitalfemaleHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
       /* dsd.addColumn("femaleHypertensionactivePatientsNotSeenIn6Months", "# of female active heart Failure patients with no visit in 6 months", new Mapped(femaleHypertensionActivePatientsNotSeenIn6MonthsIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
*/


        CohortIndicator hospitalpercentfemaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitalpercentfemaleheartFailureActivePatientsNotSeenIn6MonthsCompositionIndicator",hospitalfemaleHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalfemaleHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalfemaleHypertensionactivePatientsNotSeenIn6Months", "% of hospital female active Hypertension patients with no visit in 6 months", new Mapped(hospitalpercentfemaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        // Male number
        CompositionCohortDefinition hospitalmaleHypertensionActivePatientsComposition = femaleComposition(hospitalHypertensionActivePatientsComposition,maleCohort);
        CompositionCohortDefinition hospitalmaleHypertensionActivePatientsNotSeenIn6MonthsComposition = femaleComposition(hospitalHypertensionActivePatientsNotSeenIn6MonthsComposition,maleCohort);

        CohortIndicator hospitalpercentmaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator=Indicators.newFractionIndicator("hospitalpercentmaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator",hospitalmaleHypertensionActivePatientsNotSeenIn6MonthsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"),hospitalmaleHypertensionActivePatientsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));

        dsd.addColumn("hospitalmaleheartFailureactivePatientsNotSeenIn6Months", "% of hospital male active Hypertension patients with no visit in 6 months", new Mapped(hospitalpercentmaleHypertensionActivePatientsNotSeenIn6MonthsCompositionIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");










        //PDC

        //=========================================================================
        // C1: # of PDC patients currently in care                               //
        //=========================================================================


        InProgramCohortDefinition inpdcProgram = Cohorts.createInProgramParameterizableByStartEndDate("inpdcProgram",
                pdcProgram);

        CompositionCohortDefinition inpdcProgramComposition = new CompositionCohortDefinition();
        inpdcProgramComposition.setName("inpdcProgramComposition");
        inpdcProgramComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        inpdcProgramComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//
        inpdcProgramComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inpdcProgram, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        inpdcProgramComposition.setCompositionString("1");

        CohortIndicator inpdcProgramMonthlyIndicator = Indicators.newCountIndicator("inpdcProgramMonthlyIndicator",
                inpdcProgramComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate},onOrBefore=${endDate}"));
        dsd.addColumn("NumberOfPatientsinPDCProgram", "# of PDC patients currently in care ", new Mapped(inpdcProgramMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");



        // female number
        CompositionCohortDefinition femaleinpdcProgramComposition = new CompositionCohortDefinition();
        femaleinpdcProgramComposition.setName("femaleinpdcProgramComposition");
        femaleinpdcProgramComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleinpdcProgramComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleinpdcProgramComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inpdcProgramComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleinpdcProgramComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        femaleinpdcProgramComposition.setCompositionString("1 AND 2");

        CohortIndicator femaleinpdcProgramMonthlyIndicator = Indicators.newCountIndicator("femaleinpdcProgramMonthlyIndicator",
                femaleinpdcProgramComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate},onOrBefore=${endDate}"));
        dsd.addColumn("femaleNumberOfPatientsinPDCProgram", "# of PDC female patients currently in care", new Mapped(femaleinpdcProgramMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //HIV

        //=========================================================================
        // D1: # of patients currently enrolled in HIV program                   //
        //=========================================================================


        InProgramCohortDefinition inHIVProgram = Cohorts.createInProgramParameterizableByStartEndDate("inHIVProgram",
                HIVPrograms);

        CompositionCohortDefinition inHIVProgramComposition = new CompositionCohortDefinition();
        inHIVProgramComposition.setName("inHIVProgramComposition");
        inHIVProgramComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        inHIVProgramComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//
        inHIVProgramComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inHIVProgram, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        inHIVProgramComposition.setCompositionString("1");

        CohortIndicator inHIVProgramMonthlyIndicator = Indicators.newCountIndicator("inHIVProgramMonthlyIndicator",
                inHIVProgramComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate},onOrBefore=${endDate}"));
        dsd.addColumn("NumberOfPatientsinHIVProgram", "# of patients currently enrolled in HIV program  ", new Mapped(inHIVProgramMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");
        //female
        CohortIndicator femaleInHIVProgramMonthlyIndicator = Indicators.newCountIndicator("femaleInHIVProgramMonthlyIndicator",
                femaleComposition(inHIVProgramComposition,femaleCohort),
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate},onOrBefore=${endDate}"));
        dsd.addColumn("femaleNumberOfPatientsinHIVProgram", "# of female patients currently enrolled in HIV program  ", new Mapped(femaleInHIVProgramMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //=========================================================================
        // D2: # of HIV patients enrolled with a visit                           //
        //=========================================================================

        EncounterCohortDefinition HIVnPatientsSeenInPeriod = Cohorts.createEncounterParameterizedByDate("HIV Patients seen",
                onOrAfterOnOrBefore, hivencounterTypeIds);

        CompositionCohortDefinition inHIVProgramAndVisitsComposition = new CompositionCohortDefinition();
        inHIVProgramAndVisitsComposition.setName("inHIVProgramAndVisitsComposition");
        inHIVProgramAndVisitsComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        inHIVProgramAndVisitsComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//
        inHIVProgramAndVisitsComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(inHIVProgram, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));
        inHIVProgramAndVisitsComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(HIVnPatientsSeenInPeriod, ParameterizableUtil

                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        inHIVProgramAndVisitsComposition.setCompositionString("1 AND 2");

        CohortIndicator inHIVProgramWithVisitsMonthlyIndicator = Indicators.newCountIndicator("inHIVProgramWithVisitsMonthlyIndicator",
                inHIVProgramAndVisitsComposition,
                ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
        dsd.addColumn("NumberOfPatientsinHIVProgramWithaVisit", "# of HIV patients enrolled with a visit ", new Mapped(inHIVProgramWithVisitsMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //female
        CohortIndicator femaleinHIVProgramWithVisitsMonthlyIndicator = Indicators.newCountIndicator("femaleinHIVProgramWithVisitsMonthlyIndicator",
                femaleComposition(inHIVProgramAndVisitsComposition,femaleCohort),
                ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
        dsd.addColumn("femaleNumberOfPatientsinHIVProgramWithaVisit", "# of female HIV patients enrolled with a visit  ", new Mapped(femaleinHIVProgramWithVisitsMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // D3: # of HIV patients enrolled but no visit                          //
        //=========================================================================

        CompositionCohortDefinition inHIVProgramAndNoVisitsComposition = new CompositionCohortDefinition();
        inHIVProgramAndNoVisitsComposition.setName("inHIVProgramAndNoVisitsComposition");
        inHIVProgramAndNoVisitsComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        inHIVProgramAndNoVisitsComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//
        inHIVProgramAndNoVisitsComposition.getSearches().put("1",new Mapped<CohortDefinition>(inHIVProgram, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));
        inHIVProgramAndNoVisitsComposition.getSearches().put("2",new Mapped<CohortDefinition>(HIVnPatientsSeenInPeriod, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        inHIVProgramAndNoVisitsComposition.setCompositionString("1 AND (NOT 2)");

        CohortIndicator inHIVProgramWithNoVisitsMonthlyIndicator = Indicators.newCountIndicator("inHIVProgramWithNoVisitsMonthlyIndicator",
                inHIVProgramAndNoVisitsComposition,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
        dsd.addColumn("NumberOfPatientsinHIVProgramWithNoVisit", "# of HIV patients enrolled with no visit ", new Mapped(inHIVProgramWithNoVisitsMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //female
        CohortIndicator femaleinHIVProgramWithNoVisitsMonthlyIndicator = Indicators.newCountIndicator("femaleinHIVProgramWithNoVisitsMonthlyIndicator",
                femaleComposition(inHIVProgramAndNoVisitsComposition,femaleCohort),
                ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate}"));
        dsd.addColumn("femaleNumberOfPatientsinHIVProgramWithNoVisit", "# of female HIV patients enrolled with no visit  ", new Mapped(femaleinHIVProgramWithNoVisitsMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // D4: # of HIV patients LTFU                                            //
        //=========================================================================

        dsd.addColumn("HIVLTFUPatients", "# of HIV LTFU Patients ", new Mapped(patientExitReason(inHIVProgram,patientdefaulted),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleHIVLTFUPatients", "# of HIV LTFU female Patients Transferred out", new Mapped(femaleExitReason(inHIVProgram,femaleCohort,patientdefaulted),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // D4: # of HIV patients transferred out                                 //
        //=========================================================================

        dsd.addColumn("HIVPatientsTransferredOut", "# of HIV Patients Transferred out", new Mapped(patientExitReason(inHIVProgram,patienttransferedoutstate),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleHIVPatientsTransferredOut", "# of HIV female Patients Transferred out", new Mapped(femaleExitReason(inHIVProgram,femaleCohort,patienttransferedoutstate),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=========================================================================
        // D4: # of HIV patients deceased                                        //
        //=========================================================================

        dsd.addColumn("HIVPatientsdeceased", "# of HIV Patients deceased", new Mapped(patientExitReason(inHIVProgram,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        dsd.addColumn("femaleHIVPatientsdeceased", "# of HIV female Patients deceased", new Mapped(femaleExitReason(inHIVProgram,femaleCohort,patientDied),
                ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //=============================================================================
        // D5: % of active eligible patients with a viral load results within a year //
        //=============================================================================
////        SqlCohortDefinition viralLoadGreaterThan1000InLast6Months = new SqlCohortDefinition("select vload.person_id from (select * from obs where concept_id="+viralLoad.getConceptId()+" and value_numeric<20 and obs_datetime> :beforeDate and obs_datetime<= :onDate order by obs_datetime desc) as vload group by vload.person_id");
//
//        SqlCohortDefinition viralLoadInPeriod = new SqlCohortDefinition("select vload.person_id from (select * from obs where concept_id="+viralLoadConcept.getConceptId()+" and obs_datetime> :onOrAfter and obs_datetime<= :onOrBefore order by obs_datetime desc) as vload group by vload.person_id");
//        viralLoadInPeriod.setName("viralLoadInPeriod");
//        viralLoadInPeriod.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
//        viralLoadInPeriod.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
//        viralLoadInPeriod.addParameter(new Parameter("location", "location", Location.class));


        CompositionCohortDefinition HIVPatientsOnART = new CompositionCohortDefinition();
        HIVPatientsOnART.setName("HIVPatientsOnART");
        HIVPatientsOnART.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        HIVPatientsOnART.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        HIVPatientsOnART.getSearches().put("1",new Mapped<CohortDefinition>(inHIVProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        HIVPatientsOnART.getSearches().put("2",new Mapped<CohortDefinition>(HIVnPatientsSeenInPeriod, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrAfter},onOrAfter=${onOrBefore}")));
        HIVPatientsOnART.getSearches().put("3",new Mapped<CohortDefinition>(OnArt(adulthivprogramname), ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        HIVPatientsOnART.getSearches().put("4",new Mapped<CohortDefinition>(OnArt(pedihivprogramname), ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
//        HIVPatientsOnART.getSearches().put("5",new Mapped<CohortDefinition>(OnArt(pmtctprogramname), ParameterizableUtil
//                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
//        HIVPatientsOnART.getSearches().put("6",new Mapped<CohortDefinition>(OnArt(pmtctCombinedMotherProgramname), ParameterizableUtil
//                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        HIVPatientsOnART.setCompositionString("1 AND 2 AND (3 OR 4)");

        CohortIndicator HIVPatientsOnARTMonthlyIndicator = Indicators.newCountIndicator("HIVPatientsOnARTMonthlyIndicator",
                HIVPatientsOnART,ParameterizableUtil.createParameterMappings("onOrBefore=${endDate-12m+1d},onOrAfter=${endDate}"));
        dsd.addColumn("HIVPatientsOnART", "# of Active patients  ", new Mapped(HIVPatientsOnARTMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //female
        CohortIndicator femaleHIVPatientsOnARTMonthlyIndicator = Indicators.newCountIndicator("femaleHIVPatientsOnARTMonthlyIndicator",
                femaleComposition(HIVPatientsOnART,femaleCohort),
                ParameterizableUtil.createParameterMappings("onOrBefore=${endDate-12m+1d},onOrAfter=${endDate}"));
        dsd.addColumn("femaleHIVPatientsOnART", "# of female Active patients  ", new Mapped(femaleHIVPatientsOnARTMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


//        SqlCohortDefinition viralLoadGreaterThan1000InLast6Months = new SqlCohortDefinition("select vload.person_id from (select * from obs where concept_id="+viralLoad.getConceptId()+" and value_numeric<20 and obs_datetime> :beforeDate and obs_datetime<= :onDate order by obs_datetime desc) as vload group by vload.person_id");

        SqlCohortDefinition viralLoadInPeriod = new SqlCohortDefinition("select vload.person_id from (select * from obs where concept_id="+viralLoadConcept.getConceptId()+" and obs_datetime>= :onOrAfter and obs_datetime<= :onOrBefore and voided = 0  order by obs_datetime desc) as vload group by vload.person_id");
        viralLoadInPeriod.setName("viralLoadInPeriod");
        viralLoadInPeriod.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        viralLoadInPeriod.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        //with vl
        CompositionCohortDefinition  ActivePatientWithVlInperiod= new CompositionCohortDefinition();
        ActivePatientWithVlInperiod.setName("ActivePatientWithVlInperiod");
        ActivePatientWithVlInperiod.addParameter(new Parameter("onOrBefore","onOrBefore",Date .class));
        ActivePatientWithVlInperiod.addParameter(new Parameter("onOrAfter","onOrAfter",Date .class));

        ActivePatientWithVlInperiod.getSearches().put("1",new Mapped<CohortDefinition>(HIVPatientsOnART, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrAfter},onOrAfter=${onOrBefore}")));
        ActivePatientWithVlInperiod.getSearches().put("2",new Mapped<CohortDefinition>(viralLoadInPeriod, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        ActivePatientWithVlInperiod.setCompositionString("1 AND 2");

        CohortIndicator ActivePatientWithVlInperiodMonthlyIndicator = Indicators.newCountIndicator("ActivePatientWithVlInperiodMonthlyIndicator",
                ActivePatientWithVlInperiod,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("ActivePatientWithVl", "# of active eligible patients with a viral load results within a year   ", new Mapped(ActivePatientWithVlInperiodMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //female
        CohortIndicator femaleActivePatientWithVlInperiodMonthlyIndicator = Indicators.newCountIndicator("femaleActivePatientWithVlInperiodMonthlyIndicator",
                femaleComposition(ActivePatientWithVlInperiod,femaleCohort),
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleActivePatientWithVl", "# of female active eligible patients with a viral load results within a year  ", new Mapped(femaleActivePatientWithVlInperiodMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");


        //=========================================================================================
        // D6: of active  patients with a viral load result within a year that is  less than 20) //
        //=========================================================================================


        SqlCohortDefinition viralLoadInPeriodLessthanTwenty = new SqlCohortDefinition("select vload.person_id from (select * from obs where concept_id="+viralLoadConcept.getConceptId()+" and value_numeric<20 and obs_datetime>= :onOrAfter and obs_datetime<= :onOrBefore and voided = 0 order by obs_datetime desc) as vload group by vload.person_id");
        viralLoadInPeriodLessthanTwenty.setName("viralLoadInPeriod");
        viralLoadInPeriodLessthanTwenty.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        viralLoadInPeriodLessthanTwenty.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        //with vl
        CompositionCohortDefinition  ActivePatientWithVlLessthanTwentyInperiod= new CompositionCohortDefinition();
        ActivePatientWithVlLessthanTwentyInperiod.setName("ActivePatientWithVlLessthanTwentyInperiod");
        ActivePatientWithVlLessthanTwentyInperiod.addParameter(new Parameter("onOrBefore","onOrBefore",Date .class));
        ActivePatientWithVlLessthanTwentyInperiod.addParameter(new Parameter("onOrAfter","onOrAfter",Date .class));

        ActivePatientWithVlLessthanTwentyInperiod.getSearches().put("1",new Mapped<CohortDefinition>(HIVPatientsOnART, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrAfter},onOrAfter=${onOrBefore}")));
        ActivePatientWithVlLessthanTwentyInperiod.getSearches().put("2",new Mapped<CohortDefinition>(viralLoadInPeriodLessthanTwenty, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        ActivePatientWithVlLessthanTwentyInperiod.setCompositionString("1 AND 2");

        CohortIndicator ActivePatientWithVlLessthanTwentyInperiodMonthlyIndicator = Indicators.newCountIndicator("ActivePatientWithVlLessthanTwentyInperiodMonthlyIndicator",
                ActivePatientWithVlLessthanTwentyInperiod,ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("ActivePatientWithVlLessthanTwenty", "# of active eligible patients with a viral load results Less than Twenty within a year   ", new Mapped(ActivePatientWithVlLessthanTwentyInperiodMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

        //female
        CohortIndicator femaleActivePatientWithVlLessthanTwentyInperiodMonthlyIndicator = Indicators.newCountIndicator("femaleActivePatientWithVlLessthanTwentyInperiodMonthlyIndicator",
                femaleComposition(ActivePatientWithVlLessthanTwentyInperiod,femaleCohort),
                ParameterizableUtil.createParameterMappings("onOrAfter=${endDate-12m+1d},onOrBefore=${endDate}"));
        dsd.addColumn("femaleActivePatientWithVlLessthanTwenty", "# of female active eligible patients with a viral load results Less than Twenty within a year  ", new Mapped(femaleActivePatientWithVlLessthanTwentyInperiodMonthlyIndicator,
                ParameterizableUtil.createParameterMappings("endDate=${endDate}")), "");

    }

    private CohortIndicator patientsWithProgramOutcome(SqlCohortDefinition patientWithProgramOutcomeCohortdefinition){

        CompositionCohortDefinition patientWithProgramOutcome = new CompositionCohortDefinition();
        patientWithProgramOutcome.setName("patientWithProgramOutcome");
        patientWithProgramOutcome.addParameter(new Parameter("startDate","startDate",Date .class));
        patientWithProgramOutcome.addParameter(new Parameter("endDate","endDate",Date .class));

        patientWithProgramOutcome.getSearches().put("1",new Mapped<CohortDefinition>(patientWithProgramOutcomeCohortdefinition, ParameterizableUtil
                            .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        patientWithProgramOutcome.setCompositionString("1");
        CohortIndicator patientWithProgramOutcomeMonthlyIndicator = Indicators.newCountIndicator("patientWithProgramOutcomeMonthlyIndicator",
                patientWithProgramOutcome,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        return patientWithProgramOutcomeMonthlyIndicator;
    }
    private CohortIndicator femalepatientsWithProgramOutcome(SqlCohortDefinition patientWithProgramOutcomeCohortdefinition,GenderCohortDefinition femaleCohort){

        CompositionCohortDefinition femaleOncologyPatientsWithOutcome = new CompositionCohortDefinition();
        femaleOncologyPatientsWithOutcome.setName("femaleOncologyPatientsWithOutcome");
        femaleOncologyPatientsWithOutcome.addParameter(new Parameter("startDate","startDate",Date .class));
        femaleOncologyPatientsWithOutcome.addParameter(new Parameter("endDate","endDate",Date .class));
        femaleOncologyPatientsWithOutcome.getSearches().put("1",new Mapped<CohortDefinition>(patientWithProgramOutcomeCohortdefinition, ParameterizableUtil
                .createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleOncologyPatientsWithOutcome.getSearches().put("2", new Mapped<CohortDefinition>(femaleCohort, null));
        femaleOncologyPatientsWithOutcome.setCompositionString("1 AND 2");

        CohortIndicator femalepatientsWithProgramOutcomeMonthlyIndicator = Indicators.newCountIndicator("femalepatientsWithProgramOutcomeMonthlyIndicator",
                femaleOncologyPatientsWithOutcome,ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));

        return femalepatientsWithProgramOutcomeMonthlyIndicator;

    }
    private CohortIndicator patientExitReason(InProgramCohortDefinition inProgram, Concept patientExitReason){

        CodedObsCohortDefinition exitedpatient = Cohorts.createCodedObsCohortDefinition("exitedpatient", onOrAfterOnOrBefore,reasonForExitingCare, patientExitReason, SetComparator.IN, BaseObsCohortDefinition.TimeModifier.LAST);

        CompositionCohortDefinition exitedfromcarewithreasons = new CompositionCohortDefinition();
        exitedfromcarewithreasons.setName("exitedfromcarewithreasons");
        exitedfromcarewithreasons.addParameter(new Parameter("startDate", "startDate", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("endDate", "endDate", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        exitedfromcarewithreasons.getSearches().put("1",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        exitedfromcarewithreasons.getSearches().put("2",new Mapped<CohortDefinition>(exitedpatient,
                ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        exitedfromcarewithreasons.setCompositionString("1 AND 2");
        CohortIndicator exitedfromcarewithreasonsMonthlyIndicator = Indicators.newCountIndicator("exitedfromcarewithreasonsMonthlyIndicator",
                exitedfromcarewithreasons,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        return exitedfromcarewithreasonsMonthlyIndicator;
    }
    private CohortIndicator patientExitReason(InProgramCohortDefinition inProgram, SqlCohortDefinition hospitalPatients, Concept patientExitReason){

        CodedObsCohortDefinition exitedpatient = Cohorts.createCodedObsCohortDefinition("exitedpatient", onOrAfterOnOrBefore,reasonForExitingCare, patientExitReason, SetComparator.IN, BaseObsCohortDefinition.TimeModifier.LAST);

        CompositionCohortDefinition exitedfromcarewithreasons = new CompositionCohortDefinition();
        exitedfromcarewithreasons.setName("exitedfromcarewithreasons");
        exitedfromcarewithreasons.addParameter(new Parameter("startDate", "startDate", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("endDate", "endDate", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        exitedfromcarewithreasons.getSearches().put("1",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        exitedfromcarewithreasons.getSearches().put("2",new Mapped<CohortDefinition>(exitedpatient,
                ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        exitedfromcarewithreasons.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));
        exitedfromcarewithreasons.setCompositionString("1 AND 2 AND NOT 3");
        CohortIndicator exitedfromcarewithreasonsMonthlyIndicator = Indicators.newCountIndicator("exitedfromcarewithreasonsMonthlyIndicator",
                exitedfromcarewithreasons,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        return exitedfromcarewithreasonsMonthlyIndicator;
    }
    private CohortIndicator hospitalPatientExitReason(InProgramCohortDefinition inProgram, SqlCohortDefinition hospitalPatients, Concept patientExitReason){

        CodedObsCohortDefinition exitedpatient = Cohorts.createCodedObsCohortDefinition("exitedpatient", onOrAfterOnOrBefore,reasonForExitingCare, patientExitReason, SetComparator.IN, BaseObsCohortDefinition.TimeModifier.LAST);

        CompositionCohortDefinition exitedfromcarewithreasons = new CompositionCohortDefinition();
        exitedfromcarewithreasons.setName("exitedfromcarewithreasons");
        exitedfromcarewithreasons.addParameter(new Parameter("startDate", "startDate", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("endDate", "endDate", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        exitedfromcarewithreasons.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        exitedfromcarewithreasons.getSearches().put("1",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        exitedfromcarewithreasons.getSearches().put("2",new Mapped<CohortDefinition>(exitedpatient,
                ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        exitedfromcarewithreasons.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));
        exitedfromcarewithreasons.setCompositionString("1 AND 2 AND 3");
        CohortIndicator exitedfromcarewithreasonsMonthlyIndicator = Indicators.newCountIndicator("exitedfromcarewithreasonsMonthlyIndicator",
                exitedfromcarewithreasons,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        return exitedfromcarewithreasonsMonthlyIndicator;
    }

    private CohortIndicator femaleExitReason(InProgramCohortDefinition inProgram,  GenderCohortDefinition femaleCohort, Concept patientExitReason){

        CodedObsCohortDefinition exitedpatient = Cohorts.createCodedObsCohortDefinition("exitedpatient", onOrAfterOnOrBefore,reasonForExitingCare, patientExitReason, SetComparator.IN, BaseObsCohortDefinition.TimeModifier.LAST);

        CompositionCohortDefinition femaleexitedfromcarewithreasons = new CompositionCohortDefinition();
        femaleexitedfromcarewithreasons.setName("femaleexitedfromcarewithreasons");
        femaleexitedfromcarewithreasons.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleexitedfromcarewithreasons.getSearches().put("1",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleexitedfromcarewithreasons.getSearches().put("2",new Mapped<CohortDefinition>(exitedpatient,
                ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleexitedfromcarewithreasons.getSearches().put("3", new Mapped<CohortDefinition>(femaleCohort, null));
        femaleexitedfromcarewithreasons.setCompositionString("1 AND 2 AND 3");
        CohortIndicator femaleExitedFromCareWithreasonsMonthlyIndicator = Indicators.newCountIndicator("femaleExitedFromCareWithreasonsMonthlyIndicator",
                femaleexitedfromcarewithreasons,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        return femaleExitedFromCareWithreasonsMonthlyIndicator;
    }

    private CohortIndicator femaleExitReason(InProgramCohortDefinition inProgram,SqlCohortDefinition hospitalPatients,  GenderCohortDefinition femaleCohort, Concept patientExitReason){

        CodedObsCohortDefinition exitedpatient = Cohorts.createCodedObsCohortDefinition("exitedpatient", onOrAfterOnOrBefore,reasonForExitingCare, patientExitReason, SetComparator.IN, BaseObsCohortDefinition.TimeModifier.LAST);

        CompositionCohortDefinition femaleexitedfromcarewithreasons = new CompositionCohortDefinition();
        femaleexitedfromcarewithreasons.setName("femaleexitedfromcarewithreasons");
        femaleexitedfromcarewithreasons.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleexitedfromcarewithreasons.getSearches().put("1",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleexitedfromcarewithreasons.getSearches().put("2",new Mapped<CohortDefinition>(exitedpatient,
                ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleexitedfromcarewithreasons.getSearches().put("3", new Mapped<CohortDefinition>(femaleCohort, null));
        femaleexitedfromcarewithreasons.getSearches().put("4",new Mapped<CohortDefinition>(hospitalPatients, null));

        femaleexitedfromcarewithreasons.setCompositionString("1 AND 2 AND 3 AND NOT 4");
        CohortIndicator femaleExitedFromCareWithreasonsMonthlyIndicator = Indicators.newCountIndicator("femaleExitedFromCareWithreasonsMonthlyIndicator",
                femaleexitedfromcarewithreasons,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        return femaleExitedFromCareWithreasonsMonthlyIndicator;
    }

    private CohortIndicator hospitalFemaleExitReason(InProgramCohortDefinition inProgram,SqlCohortDefinition hospitalPatients,  GenderCohortDefinition femaleCohort, Concept patientExitReason){

        CodedObsCohortDefinition exitedpatient = Cohorts.createCodedObsCohortDefinition("exitedpatient", onOrAfterOnOrBefore,reasonForExitingCare, patientExitReason, SetComparator.IN, BaseObsCohortDefinition.TimeModifier.LAST);

        CompositionCohortDefinition femaleexitedfromcarewithreasons = new CompositionCohortDefinition();
        femaleexitedfromcarewithreasons.setName("femaleexitedfromcarewithreasons");
        femaleexitedfromcarewithreasons.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleexitedfromcarewithreasons.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleexitedfromcarewithreasons.getSearches().put("1",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleexitedfromcarewithreasons.getSearches().put("2",new Mapped<CohortDefinition>(exitedpatient,
                ParameterizableUtil.createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleexitedfromcarewithreasons.getSearches().put("3", new Mapped<CohortDefinition>(femaleCohort, null));
        femaleexitedfromcarewithreasons.getSearches().put("4",new Mapped<CohortDefinition>(hospitalPatients, null));

        femaleexitedfromcarewithreasons.setCompositionString("1 AND 2 AND 3 AND 4");
        CohortIndicator femaleExitedFromCareWithreasonsMonthlyIndicator = Indicators.newCountIndicator("femaleExitedFromCareWithreasonsMonthlyIndicator",
                femaleexitedfromcarewithreasons,ParameterizableUtil.createParameterMappings("onOrAfter=${startDate},onOrBefore=${endDate},startDate=${startDate},endDate=${endDate}"));


        return femaleExitedFromCareWithreasonsMonthlyIndicator;
    }

    private CompositionCohortDefinition activePatientOfADisease(EncounterCohortDefinition diseasepatientsSeenInPeriod,SqlCohortDefinition hospitalPatients,InProgramCohortDefinition inProgram){
        CompositionCohortDefinition ActivePatientsComposition = new CompositionCohortDefinition();
        ActivePatientsComposition.setName("ActivePatientsComposition");
        ActivePatientsComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        ActivePatientsComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        ActivePatientsComposition.getSearches().put("1",new Mapped<CohortDefinition>(diseasepatientsSeenInPeriod, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

        ActivePatientsComposition.getSearches().put("2",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));

        ActivePatientsComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        ActivePatientsComposition.setCompositionString("1 AND 2 AND NOT 3");


        return  ActivePatientsComposition;
    }
    private CompositionCohortDefinition hospitalActivePatientOfADisease(EncounterCohortDefinition diseasepatientsSeenInPeriod,SqlCohortDefinition hospitalPatients,InProgramCohortDefinition inProgram){
        CompositionCohortDefinition ActivePatientsComposition = new CompositionCohortDefinition();
        ActivePatientsComposition.setName("ActivePatientsComposition");
        ActivePatientsComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        ActivePatientsComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        ActivePatientsComposition.getSearches().put("1",new Mapped<CohortDefinition>(diseasepatientsSeenInPeriod, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

        ActivePatientsComposition.getSearches().put("2",new Mapped<CohortDefinition>(inProgram, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore}")));

        ActivePatientsComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        ActivePatientsComposition.setCompositionString("1 AND 2 AND 3");


        return  ActivePatientsComposition;
    }

    private CompositionCohortDefinition ActivePatientsNotSeenIn6MonthsComposition(CompositionCohortDefinition ActivePatientsComposition,SqlCohortDefinition hospitalPatients,EncounterCohortDefinition patientsSeenInPeriod){
        CompositionCohortDefinition ActivePatientsNotSeenIn6MonthsComposition = new CompositionCohortDefinition();
        ActivePatientsNotSeenIn6MonthsComposition.setName("ActivePatientsNotSeenIn6MonthsComposition");
        ActivePatientsNotSeenIn6MonthsComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        ActivePatientsNotSeenIn6MonthsComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        ActivePatientsNotSeenIn6MonthsComposition.getSearches().put("1",new Mapped<CohortDefinition>(ActivePatientsComposition, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

        ActivePatientsNotSeenIn6MonthsComposition.getSearches().put("2",new Mapped<CohortDefinition>(patientsSeenInPeriod, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore-6m+1d}")));
        ActivePatientsNotSeenIn6MonthsComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        ActivePatientsNotSeenIn6MonthsComposition.setCompositionString("1 AND (NOT 2) AND (NOT 3)");

        return ActivePatientsNotSeenIn6MonthsComposition;
    }
    private CompositionCohortDefinition hospitalActivePatientsNotSeenIn6MonthsComposition(CompositionCohortDefinition ActivePatientsComposition,SqlCohortDefinition hospitalPatients,EncounterCohortDefinition patientsSeenInPeriod){
        CompositionCohortDefinition ActivePatientsNotSeenIn6MonthsComposition = new CompositionCohortDefinition();
        ActivePatientsNotSeenIn6MonthsComposition.setName("ActivePatientsNotSeenIn6MonthsComposition");
        ActivePatientsNotSeenIn6MonthsComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        ActivePatientsNotSeenIn6MonthsComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        ActivePatientsNotSeenIn6MonthsComposition.getSearches().put("1",new Mapped<CohortDefinition>(ActivePatientsComposition, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));

        ActivePatientsNotSeenIn6MonthsComposition.getSearches().put("2",new Mapped<CohortDefinition>(patientsSeenInPeriod, ParameterizableUtil
                .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrBefore-6m+1d}")));
        ActivePatientsNotSeenIn6MonthsComposition.getSearches().put("3",new Mapped<CohortDefinition>(hospitalPatients, null));

        ActivePatientsNotSeenIn6MonthsComposition.setCompositionString("1 AND (NOT 2) AND 3");

        return ActivePatientsNotSeenIn6MonthsComposition;
    }

    private CompositionCohortDefinition femaleComposition(CompositionCohortDefinition generalComposition,GenderCohortDefinition femaleCohort){
        CompositionCohortDefinition femaleComposition = new CompositionCohortDefinition();
        femaleComposition.setName("femaleComposition");
        femaleComposition.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        femaleComposition.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));
        femaleComposition.getSearches().put(
                "1",
                new Mapped<CohortDefinition>(generalComposition, ParameterizableUtil
                        .createParameterMappings("onOrBefore=${onOrBefore},onOrAfter=${onOrAfter}")));
        femaleComposition.getSearches().put(
                "2",
                new Mapped<CohortDefinition>(femaleCohort, null));
        femaleComposition.setCompositionString("1 AND 2");

        return femaleComposition;
    }

    private InStateCohortDefinition OnArt(Program HIVprogram){
        InStateCohortDefinition onART = new InStateCohortDefinition();
        onART.addState(HIVprogram.getWorkflowByName("TREATMENT STATUS").getState(hivonartstate));
        onART.addParameter(new Parameter("onOrAfter", "onOrAfter", Date.class));
        onART.addParameter(new Parameter("onOrBefore", "onOrBefore", Date.class));

        return onART;
    }


    private void setUpProperties() {

        oncologyProgram = gp.getProgram(GlobalPropertiesManagement.ONCOLOGY_PROGRAM);
        oncologyPrograms.add(oncologyProgram);


        hypertesionProgram =  gp.getProgram(GlobalPropertiesManagement.HYPERTENSION_PROGRAM);
        heartFailureProgram =  gp.getProgram(GlobalPropertiesManagement.HEART_FAILURE_PROGRAM_NAME);
        asthmaProgram =  gp.getProgram(GlobalPropertiesManagement.CRD_PROGRAM);
        diabetesProgram =  gp.getProgram(GlobalPropertiesManagement.DM_PROGRAM);
        CKDProgram =  gp.getProgram(GlobalPropertiesManagement.CKD_PROGRAM);
        epilepsyProgram =  gp.getProgram(GlobalPropertiesManagement.EPILEPSY_PROGRAM);
        pdcProgram =  gp.getProgram(GlobalPropertiesManagement.PDC_PROGRAM);
        adulthivprogramname =  gp.getProgram(GlobalPropertiesManagement.ADULT_HIV_PROGRAM);
//        pmtctcombinedprogramname = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_CLINIC_PROGRAM);
        pedihivprogramname = gp.getProgram(GlobalPropertiesManagement.PEDI_HIV_PROGRAM);
        pmtctprogramname = gp.getProgram(GlobalPropertiesManagement.PMTCT_PREGNANCY_PROGRAM);
        pmtctCombinedMotherProgramname = gp.getProgram(GlobalPropertiesManagement.PMTCT_COMBINED_MOTHER_PROGRAM);





        NCDPrograms.add(hypertesionProgram);
        NCDPrograms.add(heartFailureProgram);
        NCDPrograms.add(asthmaProgram);
        NCDPrograms.add(diabetesProgram);
        NCDPrograms.add(CKDProgram);
        NCDPrograms.add(epilepsyProgram);

        HIVPrograms.add(adulthivprogramname);
        HIVPrograms.add(pedihivprogramname);
        HIVPrograms.add(pmtctprogramname);
        HIVPrograms.add(pmtctCombinedMotherProgramname);

        //encounterTypes
        outpatientOncEncounterType=gp.getEncounterType(GlobalPropertiesManagement.OUTPATIENT_ONCOLOGY_ENCOUNTER);
        inpatientOncologyEncounter=gp.getEncounterType(GlobalPropertiesManagement.INPATIENT_ONCOLOGY_ENCOUNTER);
        oncologyEncounters.add(outpatientOncEncounterType);
        oncologyEncounters.add(inpatientOncologyEncounter);
        hypertesionEncounter =  gp.getEncounterType(GlobalPropertiesManagement.HYPERTENSION_ENCOUNTER);
        heartFailureEncounter =  gp.getEncounterType(GlobalPropertiesManagement.HEART_FAILURE_ENCOUNTER);
        asthmaEncounter =  gp.getEncounterType(GlobalPropertiesManagement.ASTHMA_VISIT);
        diabetesEncounter =  gp.getEncounterType(GlobalPropertiesManagement.DIABETES_VISIT);
        CKDEncounter =  gp.getEncounterType(GlobalPropertiesManagement.CKD_ENCOUNTER_TYPE);
        epilepsyEncounter =  gp.getEncounterType(GlobalPropertiesManagement.EPILEPSY_VISIT);
        HFHTNCKDEncounter =  gp.getEncounterType(GlobalPropertiesManagement.HF_HTN_CKD_ENCOUNTER_TYPE);
        POSTCARDIACSURGERYVISIT =  gp.getEncounterType(GlobalPropertiesManagement.POST_CARDIAC_SURGERY_VISIT);
        pdcEncounterType =  gp.getEncounterType(GlobalPropertiesManagement.PDC_VISIT);
        hivencounterTypeIds =  gp.getEncounterTypeList(GlobalPropertiesManagement.PEDIANDADULTHIV_ENCOUNTER_TYPES);





        NCDEncouterTypes.add(hypertesionEncounter);
        NCDEncouterTypes.add(heartFailureEncounter);
        NCDEncouterTypes.add(asthmaEncounter);
        NCDEncouterTypes.add(diabetesEncounter);
        NCDEncouterTypes.add(CKDEncounter);
        NCDEncouterTypes.add(epilepsyEncounter);
        NCDEncouterTypes.add(HFHTNCKDEncounter);
        NCDEncouterTypes.add(POSTCARDIACSURGERYVISIT);



        EncounterTypesIds.add(outpatientOncEncounterType.getEncounterTypeId());
        EncounterTypesIds.add(inpatientOncologyEncounter.getEncounterTypeId());
        EncounterTypesIds.add(inpatientOncologyEncounter.getEncounterTypeId());
        EncounterTypesIds.add(hypertesionEncounter.getEncounterTypeId());
        EncounterTypesIds.add(heartFailureEncounter.getEncounterTypeId());
        EncounterTypesIds.add(asthmaEncounter.getEncounterTypeId());
        EncounterTypesIds.add(diabetesEncounter.getEncounterTypeId());
        EncounterTypesIds.add(CKDEncounter.getEncounterTypeId());
        EncounterTypesIds.add(HFHTNCKDEncounter.getEncounterTypeId());
        EncounterTypesIds.add(epilepsyEncounter.getEncounterTypeId());
        EncounterTypesIds.add(pdcEncounterType.getEncounterTypeId());
        for(EncounterType enc : hivencounterTypeIds){
            EncounterTypesIds.add(enc.getEncounterTypeId());
        }





        //concepts
        oncologyprogramendreason = gp.getConcept(GlobalPropertiesManagement.ONCOLOGY_PROGRAM_END_REASON);
        refferedOutForPalliationCareOnly = gp.getConcept(GlobalPropertiesManagement.REFERRED_FOR_PALLIATIONONLY_CARE);
        referredOutForPalliativeSystemicTherapy = gp.getConcept(GlobalPropertiesManagement.REFERRED_OUT_FOR_PALLIATIVE_SYSTEMIC_THERAPY);
        ReferredOutForCurativeCancerCare = gp.getConcept(GlobalPropertiesManagement.REFERRED_OUT_FOR_CURATIVE_CANCER_CARE);
        OtherReasonForReferral = gp.getConcept(GlobalPropertiesManagement.OTHERREASONFORREFERRAL);
        referredConcepts.add(refferedOutForPalliationCareOnly);
        referredConcepts.add(referredOutForPalliativeSystemicTherapy);
        referredConcepts.add(ReferredOutForCurativeCancerCare);
        referredConcepts.add(OtherReasonForReferral);
        reasonForExitingCare= gp.getConcept(GlobalPropertiesManagement.REASON_FOR_EXITING_CARE);
        patientDied = gp.getConcept(GlobalPropertiesManagement.PATIENT_DIED);
        biopsyNegative = gp.getConcept(GlobalPropertiesManagement.BIOPSYNEGATIVE);

        notCancerNoBiopsy = gp.getConcept(GlobalPropertiesManagement.NOTCANCERNOBIOPSY);
        patientRefused = gp.getConcept(GlobalPropertiesManagement.PATIENTREFUSED);
        LostToFolloUp = gp.getConcept(GlobalPropertiesManagement.LOST_TO_FOLLOWUP_OUTCOME);
        patienttransferedoutstate = gp.getConcept(GlobalPropertiesManagement.TRANSFERRED_OUT_STATE);
        patientdefaulted = gp.getConcept(GlobalPropertiesManagement.PATIENT_DEFAULTED);
        viralLoadConcept = gp.getConcept(GlobalPropertiesManagement.VIRAL_LOAD_TEST);
        hivonartstate = gp.getConcept(GlobalPropertiesManagement.ON_ANTIRETROVIRALS_STATE);



//        exitform = gp.getForm(GlobalPropertiesManagement.ONCOLOGY_EXIT_FORM);
//        exitforms.add(exitform);

        onOrAfterOnOrBefore.add("onOrAfter");
        onOrAfterOnOrBefore.add("onOrBefore");
        startDateEndDate.add("startDate");
        startDateEndDate.add("endDate");

        //DMEncounterTypeId = Integer.parseInt(Context.getAdministrationService().getGlobalProperty(GlobalPropertiesManagement.DIABETES_VISIT));

    }

}

