package org.openmrs.module.rwandareports.reporting;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.query.encounter.definition.SqlEncounterQuery;
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

public class SetupHMISCancerScreeningMonthlyIndicatorReport {

    GlobalPropertiesManagement gp = new GlobalPropertiesManagement();

    // properties

    private Form oncologyBreastScreeningExamination;

    private Form oncologyCervicalScreeningExamination;

    private Form mUzimaBreastScreening;

    private Form mUzimaCervicalScreening;


    private Form oncologyScreeningLabResultsForm;
    private Form muzimaOncologyScreeningLabResults;

    private  List<Form> oncologyScreeningLabResultsForms =new ArrayList<Form>();

    private  List<Form> screeningExaminationForms =new ArrayList<Form>();

    private  List<String> parameterNames=new ArrayList<String>();

    private Concept screeningType;

    private Concept HPV;

    private Concept testResult;
    private Concept HPVpositive;
    private Concept HPVNegative;

    private  List<Concept> testResults =new ArrayList<Concept>();

    private  List<Concept> positiveTestResults =new ArrayList<Concept>();

    private Form mUzimaCervicalCancerScreeningFollowup;

    private Form OncologyCervicalScreeningFollowUp;

    private  List<Form> cervicalCancerScreeningFollowupAndExaminationForms=new ArrayList<Form>();;

    private Concept typeOfVIAPerformed;
    private Concept VIATriage;
    private  List<Concept> VIATriageInList=new ArrayList<Concept>();

    private Concept VIAResults;
    private Concept VIAAndEligibleForThermalAblation ;
    private Concept VIAAndEligibleForLEEP;

    private  List<Concept> VIAAndEligibleResults=new ArrayList<Concept>();

    private Form mUzimaBreastCancerScreening;
    private List<Form> breastCancerScreeningForms = new ArrayList<Form>();

    private Concept reasonForBreastExam;
    private Concept breastSymptoms;
    private Concept screening;
    private Concept breastExamination;
    private Concept ABNORMAL;

    private Concept nextStep;
    private Concept referredTo;
    private Concept furtherManagement;
    private Concept medicalImaging;
    private Concept BIOPSY;
    private Concept OTHERNONCODED;

    private Concept breastUltrasound;
    private Concept YES;
    private Concept solidMass;
    private Concept intermediate;
    private Concept highSuspiciousForMalignancy;
    private Concept proceduresDone;
    private Concept PAPANICOLAOUSMEAR;
    private Concept NORMAL;

    private Form muzimaOncologyScreeningDiagnosis;
    private Form oncologyScreeningDiagnosis;
    private List<Form> diagnosisScreeningForms = new ArrayList<Form>();

    private Concept CONFIRMEDDIAGNOSIS;
    private Concept confirmedCancerDiagnosis;
    private Concept cervicalCancer;
    private Concept breastCancer;

    private Form oncologyBreastCancerScreeningTransferIn;
    private List<Form> oncologyBreastCancerScreeningTransferInList = new ArrayList<Form>();


    private Concept VIANegative;
    private  List<Concept> VIANegativeInList=new ArrayList<Concept>();;

    private Concept VIAScreen;
    private  List<Concept> VIAScreenInList=new ArrayList<Concept>();

    private Concept typeOfTreatmentPerformed;
    private Concept thermalAblation;
    private Concept cryotherapy;
    private  List<Concept> thermalAblationAndCryotherapyList =new ArrayList<Concept>();

    private Concept reasonsForReferral;
    private Concept loopElectrosurgicalExcisionProcedure;
    private  List<Concept> loopElectrosurgicalExcisionProcedureInList=new ArrayList<Concept>();

    private  List<Concept> LEEPAndColposcopy=new ArrayList<Concept>();



    private Concept suspectedCancer;
    private  List<Concept> suspectedCancerInList=new ArrayList<Concept>();

    private Concept biopsyperformed;
    private Concept yes;
    private  List<Concept> yesInList=new ArrayList<Concept>();

    private Form oncologyCervicalCancerScreeningTransferIn;
    private  List<Form> oncologyCervicalCancerScreeningTransferInList=new ArrayList<Form>();

    public void setup() throws Exception {

        setUpProperties();

        Properties properties = new Properties();
        properties.setProperty("hierarchyFields", "countyDistrict:District");

        // Quarterly Report Definition: Start

        ReportDefinition monthlyRd = new ReportDefinition();
        monthlyRd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        monthlyRd.addParameter(new Parameter("endDate", "End Date", Date.class));


        monthlyRd.addParameter(new Parameter("location", "Location", AllLocation.class, properties));

        monthlyRd.setName("ONC - HMIS Cancer Screening Monthly Indicator Report");

        monthlyRd.addDataSetDefinition(createMonthlyLocationDataSet(),
                ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate},location=${location}"));

        // Monthly Report Definition: End

        EncounterCohortDefinition ScreeningExaminationEncounter=Cohorts.createEncounterBasedOnForms("ScreeningExaminationEncounter",parameterNames, screeningExaminationForms);

        monthlyRd.setBaseCohortDefinition(ScreeningExaminationEncounter,
                ParameterizableUtil.createParameterMappings("onOrBefore=${endDate},onOrAfter=${startDate}"));

        Helper.saveReportDefinition(monthlyRd);

        ReportDesign mothlyDesign = Helper.createRowPerPatientXlsOverviewReportDesign(monthlyRd,
                "ONC_HMIS_Cancer_Screening_Monthly.xls", "HMIS Cancer Screening Monthly Indicator Report (Excel)", null);
        Properties monthlyProps = new Properties();
        monthlyProps.put("repeatingSections", "sheet:1,dataset:Encounter HMIS Cancer Screening Data Set");
        monthlyProps.put("sortWeight","5000");
        mothlyDesign.setProperties(monthlyProps);
        Helper.saveReportDesign(mothlyDesign);

    }

    public void delete() {
        ReportService rs = Context.getService(ReportService.class);
        for (ReportDesign rd : rs.getAllReportDesigns(false)) {
            if ("HMIS Cancer Screening Monthly Indicator Report (Excel)".equals(rd.getName())) {
                rs.purgeReportDesign(rd);
            }
        }
        Helper.purgeReportDefinition("ONC - HMIS Cancer Screening Monthly Indicator Report");

    }



    //Create Monthly Encounter Data set

    public LocationHierachyIndicatorDataSetDefinition createMonthlyLocationDataSet() {

        LocationHierachyIndicatorDataSetDefinition ldsd = new LocationHierachyIndicatorDataSetDefinition(
                createEncounterMonthlyBaseDataSet());
        ldsd.addBaseDefinition(createMonthlyBaseDataSet());
        ldsd.setName("Encounter HMIS Cancer Screening Data Set");
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
        createMonthlyIndicators(eidsd);
        return eidsd;
    }

    private void createMonthlyIndicators(EncounterIndicatorDataSetDefinition dsd) {

    }

    // create monthly cohort Data set

    private CohortIndicatorDataSetDefinition createMonthlyBaseDataSet() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setName("Monthly Cohort Data Set");
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        createMonthlyIndicators(dsd);
        return dsd;
    }

    private void createMonthlyIndicators(CohortIndicatorDataSetDefinition dsd) {

SqlCohortDefinition screenedForCervicalCancerWithHPV=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithHPV",screeningExaminationForms,screeningType,HPV);

GenderCohortDefinition female=Cohorts.createFemaleCohortDefinition("female");

CompositionCohortDefinition femaleScreenedForCervicalCancerWithHPV=new CompositionCohortDefinition();
femaleScreenedForCervicalCancerWithHPV.setName("femaleScreenedForCervicalCancerWithHPV");
femaleScreenedForCervicalCancerWithHPV.addParameter(new Parameter("startDate", "startDate", Date.class));
femaleScreenedForCervicalCancerWithHPV.addParameter(new Parameter("endDate", "endDate", Date.class));
femaleScreenedForCervicalCancerWithHPV.getSearches().put("1",new Mapped<CohortDefinition>(screenedForCervicalCancerWithHPV, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
femaleScreenedForCervicalCancerWithHPV.getSearches().put("2",new Mapped<CohortDefinition>(female, null));
femaleScreenedForCervicalCancerWithHPV.setCompositionString("1 and 2");

CohortIndicator femaleScreenedForCervicalCancerWithHPVIndicator = Indicators.newCountIndicator("femaleScreenedForCervicalCancerWithHPVIndicator",
        femaleScreenedForCervicalCancerWithHPV, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


dsd.addColumn("C1", "Number of women  screened for cervical cancer with HPV", new Mapped(
        femaleScreenedForCervicalCancerWithHPVIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



SqlCohortDefinition screenedForCervicalCancerWithHPVResult=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithHPVResult",oncologyScreeningLabResultsForms,testResult,testResults);

        CompositionCohortDefinition femaleScreenedForCervicalCancerWithHPVResult=new CompositionCohortDefinition();
        femaleScreenedForCervicalCancerWithHPVResult.setName("femaleScreenedForCervicalCancerWithHPVResult");
        femaleScreenedForCervicalCancerWithHPVResult.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleScreenedForCervicalCancerWithHPVResult.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleScreenedForCervicalCancerWithHPVResult.getSearches().put("1",new Mapped<CohortDefinition>(screenedForCervicalCancerWithHPVResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithHPVResult.getSearches().put("2",new Mapped<CohortDefinition>(female, null));
        femaleScreenedForCervicalCancerWithHPVResult.setCompositionString("1 and 2");

        CohortIndicator femaleScreenedForCervicalCancerWithHPVResultIndicator = Indicators.newCountIndicator("femaleScreenedForCervicalCancerWithHPVResultIndicator",
                femaleScreenedForCervicalCancerWithHPVResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C2", "Number of women  screened for cervical cancer with HPV results available this month", new Mapped(
                femaleScreenedForCervicalCancerWithHPVResultIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        SqlCohortDefinition screenedForCervicalCancerWithPositiveHPVResult=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithHPVResult",oncologyScreeningLabResultsForms,testResult,positiveTestResults);
        CompositionCohortDefinition femaleScreenedForCervicalCancerWithPositiveHPVResult=new CompositionCohortDefinition();
        femaleScreenedForCervicalCancerWithPositiveHPVResult.setName("femaleScreenedForCervicalCancerWithPositiveHPVResult");
        femaleScreenedForCervicalCancerWithPositiveHPVResult.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleScreenedForCervicalCancerWithPositiveHPVResult.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleScreenedForCervicalCancerWithPositiveHPVResult.getSearches().put("1",new Mapped<CohortDefinition>(screenedForCervicalCancerWithPositiveHPVResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithPositiveHPVResult.getSearches().put("2",new Mapped<CohortDefinition>(female, null));
        femaleScreenedForCervicalCancerWithPositiveHPVResult.setCompositionString("1 and 2");

        CohortIndicator femaleScreenedForCervicalCancerWithPositiveHPVResultIndicator = Indicators.newCountIndicator("femaleScreenedForCervicalCancerWithPositiveHPVResultIndicator",
                femaleScreenedForCervicalCancerWithPositiveHPVResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C3", "Number of women  tested HPV positive", new Mapped(
                femaleScreenedForCervicalCancerWithPositiveHPVResultIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        SqlCohortDefinition screenedForCervicalCancerWithPositiveHPVResultWithVIATriage=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithPositiveHPVResultWithVIATriage",cervicalCancerScreeningFollowupAndExaminationForms,typeOfVIAPerformed,VIATriageInList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage.setName("femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage");
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage.getSearches().put("1",new Mapped<CohortDefinition>(femaleScreenedForCervicalCancerWithPositiveHPVResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithPositiveHPVResultWithVIATriage, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageIndicator",
                femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C4", "Number of women  tested HPV positive received  VIA Triage", new Mapped(
                femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        SqlCohortDefinition screenedForCervicalCancerWithVIATriageAndVIAAndElligibleResult=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult",cervicalCancerScreeningFollowupAndExaminationForms,VIAResults,VIAAndEligibleResults);

        CompositionCohortDefinition femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult=new CompositionCohortDefinition();
        femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult.setName("femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult");
        femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult.getSearches().put("1",new Mapped<CohortDefinition>(femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithVIATriageAndVIAAndElligibleResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult.setCompositionString("1 and 2");

        CohortIndicator femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResultIndicator = Indicators.newCountIndicator("femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResultIndicator",
                femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C5", "Number of women  tested HPV positive and VIA Triage positive: With Result VIA+ Eligible for Thermal ablation OR VIA+ Eligible for LEEP", new Mapped(
                femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResultIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        SqlCohortDefinition screenedForCervicalCancerWithVIANegativeResult=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult",cervicalCancerScreeningFollowupAndExaminationForms,VIAResults, VIANegativeInList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult.setName("femalesscreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIAAndElligibleResult");
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult.getSearches().put("1",new Mapped<CohortDefinition>(femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriage, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithVIANegativeResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResultIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResultIndicator",
                femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C6", "Number of women  tested HPV positive and VIA Triage negative: VIA-", new Mapped(
                femalescreenedForCervicalCancerWithPositiveHPVResultWithVIATriageAndVIANegativeResultIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        SqlCohortDefinition screenedForCervicalCancerWithVIAScreen=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithPositiveHPVResultWithVIATriage",cervicalCancerScreeningFollowupAndExaminationForms,typeOfVIAPerformed,VIAScreenInList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithVIAScreen=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithVIAScreen.setName("femalescreenedForCervicalCancerWithVIAScreen");
        femalescreenedForCervicalCancerWithVIAScreen.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithVIAScreen.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithVIAScreen.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithVIAScreen.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithVIAScreen, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithVIAScreen.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithVIAScreenIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithVIAScreenIndicator",
                femalescreenedForCervicalCancerWithVIAScreen, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C7", "Number of women screened for cervical cancer with VIA only", new Mapped(
                femalescreenedForCervicalCancerWithVIAScreenIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        CompositionCohortDefinition femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult.setName("femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult");
        femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult.getSearches().put("1",new Mapped<CohortDefinition>(femalescreenedForCervicalCancerWithVIAScreen, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithVIATriageAndVIAAndElligibleResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResultIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResultIndicator",
                femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C8", "Number of women screened for cervical cancer with VIA only: Type of VIA performed is VIA Screen and Result is VIA+ Eligible for Thermal ablation OR VIA+ Eligible for LEEP", new Mapped(
                femalescreenedForCervicalCancerWithVIAScreenAndVIAAndElligibleResultIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        CompositionCohortDefinition femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult.setName("femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult");
        femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult.getSearches().put("1",new Mapped<CohortDefinition>(femalescreenedForCervicalCancerWithVIAScreen, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithVIANegativeResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResultIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResultIndicator",
                femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResult, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C9", "Number of women screened for cervical cancer with VIA only: Type of VIA performed is VIA Screen and Result is VIA-", new Mapped(
                femalescreenedForCervicalCancerWithVIAScreenWithVIANegativeResultIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");



        SqlCohortDefinition screenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithThermalAblationAndLEEPTreatmentType",cervicalCancerScreeningFollowupAndExaminationForms,typeOfTreatmentPerformed, thermalAblationAndCryotherapyList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType.setName("femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType");
        femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentTypeIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentTypeIndicator",
                femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentType, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C10", "Number of  screened positive women treated with thermo ablation or Cryotherapy", new Mapped(
                femalescreenedForCervicalCancerWithThermalAblationAndCryotherapyTreatmentTypeIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        SqlCohortDefinition screenedForCervicalCancerWithLEEPAsReasonsForReferral=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithLEEPAsReasonsForReferral",cervicalCancerScreeningFollowupAndExaminationForms,reasonsForReferral, LEEPAndColposcopy);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral.setName("femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral");
        femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithLEEPAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithLEEPAsReasonsForReferralIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithLEEPAsReasonsForReferralIndicator",
                femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C11", "Number of screened positive women referred  for LEEP & Colposcopy", new Mapped(
                femalescreenedForCervicalCancerWithLEEPAsReasonsForReferralIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        SqlCohortDefinition screenedForCervicalCancerWithVIASuspectedCancer=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithVIASuspectedCancer",cervicalCancerScreeningFollowupAndExaminationForms,VIAResults, suspectedCancerInList);
        CompositionCohortDefinition femalescreenedForCervicalCancerWithVIASuspectedCancer=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithVIASuspectedCancer.setName("femalescreenedForCervicalCancerWithVIASuspectedCancer");
        femalescreenedForCervicalCancerWithVIASuspectedCancer.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithVIASuspectedCancer.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithVIASuspectedCancer.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithVIASuspectedCancer.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithVIASuspectedCancer, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithVIASuspectedCancer.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithVIASuspectedCancerIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithVIASuspectedCancerIndicator",
                femalescreenedForCervicalCancerWithVIASuspectedCancer, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C12", "Number of screened women with suspected cervical cancer", new Mapped(
                femalescreenedForCervicalCancerWithVIASuspectedCancerIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        SqlCohortDefinition screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithLEEPAsReasonsForReferral",cervicalCancerScreeningFollowupAndExaminationForms,reasonsForReferral, suspectedCancerInList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral.setName("femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral");
        femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralIndicator",
                femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C14", "Number of  women  with suspected   cervical cancer referred to other level", new Mapped(
                femalescreenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");




        SqlCohortDefinition screenedForCervicalCancerWithLEEPAsTreatmentPerformed=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithLEEPAsReasonsForReferral",cervicalCancerScreeningFollowupAndExaminationForms,typeOfTreatmentPerformed, loopElectrosurgicalExcisionProcedureInList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed.setName("femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed");
        femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithLEEPAsTreatmentPerformed, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformedIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformedIndicator",
                femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformed, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C18", "Number of  screened positive women treated with LEEP", new Mapped(
                femalescreenedForCervicalCancerWithLEEPAsTreatmentPerformedIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        SqlCohortDefinition screenedForCervicalCancerWithBiobsyPerformed=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithLEEPAsReasonsForReferral",cervicalCancerScreeningFollowupAndExaminationForms,biopsyperformed, yesInList);

        CompositionCohortDefinition femalescreenedForCervicalCancerWithBiobsyPerformed=new CompositionCohortDefinition();
        femalescreenedForCervicalCancerWithBiobsyPerformed.setName("femalescreenedForCervicalCancerWithBiobsyPerformed");
        femalescreenedForCervicalCancerWithBiobsyPerformed.addParameter(new Parameter("startDate", "startDate", Date.class));
        femalescreenedForCervicalCancerWithBiobsyPerformed.addParameter(new Parameter("endDate", "endDate", Date.class));
        femalescreenedForCervicalCancerWithBiobsyPerformed.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        femalescreenedForCervicalCancerWithBiobsyPerformed.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithBiobsyPerformed, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femalescreenedForCervicalCancerWithBiobsyPerformed.setCompositionString("1 and 2");

        CohortIndicator femalescreenedForCervicalCancerWithBiobsyPerformedIndicator = Indicators.newCountIndicator("femalescreenedForCervicalCancerWithBiobsyPerformedIndicator",
                femalescreenedForCervicalCancerWithBiobsyPerformed, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C19", "Number of women with cervical biopsy performed", new Mapped(
                femalescreenedForCervicalCancerWithBiobsyPerformedIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");





        SqlCohortDefinition screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferral=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithLEEPAsReasonsForReferral",oncologyCervicalCancerScreeningTransferInList,reasonsForReferral, LEEPAndColposcopy);


        CompositionCohortDefinition screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital=new CompositionCohortDefinition();
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.setName("femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral");
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.addParameter(new Parameter("startDate", "startDate", Date.class));
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.addParameter(new Parameter("endDate", "endDate", Date.class));
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithLEEPAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.getSearches().put("3",new Mapped<CohortDefinition>(screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital.setCompositionString("1 and 2 and 3");

        CohortIndicator screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospitalIndicator = Indicators.newCountIndicator("screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospitalIndicator",
                screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospital, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C16", "Number of women  referred from health centers  to Hospital  for LEEP & Colposcopy received by  the hospital", new Mapped(
                screenedForCervicalCancerWithLEEPAndColscopyAsReasonsForReferralRecevedAtHospitalIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        SqlCohortDefinition screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralInTransferIn=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithLEEPAsReasonsForReferral",oncologyCervicalCancerScreeningTransferInList,reasonsForReferral, suspectedCancerInList);


        CompositionCohortDefinition screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital=new CompositionCohortDefinition();
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.setName("femalescreenedForCervicalCancerWithLEEPAsReasonsForReferral");
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.addParameter(new Parameter("startDate", "startDate", Date.class));
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.addParameter(new Parameter("endDate", "endDate", Date.class));
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.getSearches().put("1",new Mapped<CohortDefinition>(female,null));
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.getSearches().put("3",new Mapped<CohortDefinition>(screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralInTransferIn, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital.setCompositionString("1 and 2 and 3");

        CohortIndicator screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospitalIndicator = Indicators.newCountIndicator("screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospitalIndicator",
                screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospital, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("C17", "Number of women  referred from health centers  to Hospital  for  cervical cancer suspicion  received by  the hospital", new Mapped(
                screenedForCervicalCancerWithSuspectedCancerAsReasonsForReferralRecevedAtHospitalIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");






        //=================================
        // Breast cancer early detection //
        //=================================

        // B1.Number of people reporting breast symptoms received  clinical breast exam


        SqlCohortDefinition screenedForBreastCancerWithReasonForBreastExamAsBreastSymptoms=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForBreastExamAsBreastSymptoms",breastCancerScreeningForms,reasonForBreastExam,breastSymptoms);

        CompositionCohortDefinition screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition=new CompositionCohortDefinition();
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition.setName("screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition");
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForBreastExamAsBreastSymptoms, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition.setCompositionString("1");

        CohortIndicator screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsIndicator = Indicators.newCountIndicator("screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsIndicator",
                screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B1", "Number of people reporting breast symptoms received  clinical breast exam", new Mapped(
                screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B2. Number of people  not reporting breast symptoms received screening with clinical breast exam


        SqlCohortDefinition screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptoms=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptoms",breastCancerScreeningForms,reasonForBreastExam,screening);

        CompositionCohortDefinition screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition=new CompositionCohortDefinition();
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition.setName("screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition");
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptoms, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition.setCompositionString("1");

        CohortIndicator screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsIndicator = Indicators.newCountIndicator("screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsIndicator",
                screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B2", "Number of people  not reporting breast symptoms received screening with clinical breast exam", new Mapped(
                screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B3. Number of people reporting symptoms whose clinical breast exam shows abnormal breast findings

        SqlCohortDefinition screenedForBreastCancerWithAbnormalBreastFindings=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithAbnormalBreastFindings",breastCancerScreeningForms,breastExamination,ABNORMAL);

        CompositionCohortDefinition screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition=new CompositionCohortDefinition();
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition.setName("screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition");
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForBreastExamAsBreastSymptoms, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition.getSearches().put("2",new Mapped<CohortDefinition>(screenedForBreastCancerWithAbnormalBreastFindings, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition.setCompositionString("1 and 2");

        CohortIndicator screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsIndicator = Indicators.newCountIndicator("screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsIndicator",
                screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B3", "Number of people reporting symptoms whose clinical breast exam shows abnormal breast findings", new Mapped(
                screenedForBreastCancerWithReasonForBreastExamAsBreastSymptomsAndAbnormalBreastFindingsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B4. Number of people not reporting breast  symptoms whose clinical breast exam shows abnormal breast findings


        CompositionCohortDefinition screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition=new CompositionCohortDefinition();
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition.setName("screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition");
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptoms, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition.getSearches().put("2",new Mapped<CohortDefinition>(screenedForBreastCancerWithAbnormalBreastFindings, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition.setCompositionString("1 and 2");

        CohortIndicator screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsIndicator = Indicators.newCountIndicator("screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsIndicator",
                screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B4", "Number of people not reporting breast  symptoms whose clinical breast exam shows abnormal breast findings", new Mapped(
                screenedForBreastCancerWithReasonForBreastExamAsScreeningNoSymptomsAndAbnormalBreastFindingsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B5. Number of people treated for breast concerns without requiring referral

        SqlCohortDefinition screenedForBreastCancerWithReferral=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithRefferal",breastCancerScreeningForms,nextStep,referredTo);
        SqlCohortDefinition screenedForBreastCancerWithReasonForReferralAsFurtherManagement=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForReferralAsFurtherManagement",breastCancerScreeningForms,reasonsForReferral,furtherManagement);
        SqlCohortDefinition screenedForBreastCancerWithReasonForReferralAsImaging=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForReferralAsImaging",breastCancerScreeningForms,reasonsForReferral,medicalImaging);
        SqlCohortDefinition screenedForBreastCancerWithReasonForReferralAsBiopsy=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForReferralAsBiopsy",breastCancerScreeningForms,reasonsForReferral,BIOPSY);
        SqlCohortDefinition screenedForBreastCancerWithOtherReason=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithOtherReason",breastCancerScreeningForms,reasonsForReferral,OTHERNONCODED);


        CompositionCohortDefinition numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition=new CompositionCohortDefinition();
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.setName("numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition");
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithAbnormalBreastFindings, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.getSearches().put("2",new Mapped<CohortDefinition>(screenedForBreastCancerWithReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.getSearches().put("3",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForReferralAsFurtherManagement, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.getSearches().put("4",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForReferralAsImaging, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.getSearches().put("5",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForReferralAsBiopsy, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.getSearches().put("6",new Mapped<CohortDefinition>(screenedForBreastCancerWithOtherReason, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition.setCompositionString("1 and ((not 2) and (not 3) and (not 4) and (not 5) and (not 6)) ");

        CohortIndicator numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralIndicator = Indicators.newCountIndicator("numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralIndicator",
                numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B5", "Number of people treated for breast concerns without requiring referral", new Mapped(
                numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B6. Number of people referred for further evaluation of abnormal clinical breast exam

        CompositionCohortDefinition numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition=new CompositionCohortDefinition();
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.setName("numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition");
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithAbnormalBreastFindings, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.getSearches().put("2",new Mapped<CohortDefinition>(screenedForBreastCancerWithReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.getSearches().put("3",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForReferralAsFurtherManagement, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.getSearches().put("4",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForReferralAsImaging, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.getSearches().put("5",new Mapped<CohortDefinition>(screenedForBreastCancerWithReasonForReferralAsBiopsy, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.getSearches().put("6",new Mapped<CohortDefinition>(screenedForBreastCancerWithOtherReason, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition.setCompositionString("1 and (2 or 3 or 4 or 5 or 6)");

        CohortIndicator numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastIndicator = Indicators.newCountIndicator("numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastIndicator",
                numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastExamComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B6", "Number of people referred for further evaluation of abnormal clinical breast exam", new Mapped(
                numberOfPeopleReferredForFurtherEvaluationOfAbnormalClinicalBreastIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //================================================
        // Additional indicators for Hospitals (Breast) //
        //================================================

        // B7. Number of people referred from Health center  to the hospital for abnormal breast findings received by the hospital

        SqlCohortDefinition tranferInForBreastCancerWithReasonForReferralAsFurtherManagement=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForReferralAsFurtherManagement",oncologyBreastCancerScreeningTransferInList,reasonsForReferral,furtherManagement);
        SqlCohortDefinition tranferInForBreastCancerWithReasonForReferralAsImaging=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForReferralAsImaging",oncologyBreastCancerScreeningTransferInList,reasonsForReferral,medicalImaging);
        SqlCohortDefinition tranferInForBreastCancerWithReasonForReferralAsBiopsy=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithReasonForReferralAsBiopsy",oncologyBreastCancerScreeningTransferInList,reasonsForReferral,BIOPSY);
        SqlCohortDefinition tranferInForBreastCancerWithOtherReason=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForBreastCancerWithOtherReason",oncologyBreastCancerScreeningTransferInList,reasonsForReferral,OTHERNONCODED);

        CompositionCohortDefinition transferedInForAbnormalBreastfindingsAndReachedTheHospital=new CompositionCohortDefinition();
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.setName("transferedInForAbnormalBreastfindingsAndReachedTheHospital");
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.addParameter(new Parameter("startDate", "startDate", Date.class));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.addParameter(new Parameter("endDate", "endDate", Date.class));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.getSearches().put("1",new Mapped<CohortDefinition>(screenedForBreastCancerWithAbnormalBreastFindings, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.getSearches().put("2",new Mapped<CohortDefinition>(screenedForBreastCancerWithReferral, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.getSearches().put("3",new Mapped<CohortDefinition>(tranferInForBreastCancerWithReasonForReferralAsFurtherManagement, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.getSearches().put("4",new Mapped<CohortDefinition>(tranferInForBreastCancerWithReasonForReferralAsImaging, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.getSearches().put("5",new Mapped<CohortDefinition>(tranferInForBreastCancerWithReasonForReferralAsBiopsy, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        transferedInForAbnormalBreastfindingsAndReachedTheHospital.getSearches().put("6",new Mapped<CohortDefinition>(tranferInForBreastCancerWithOtherReason, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        transferedInForAbnormalBreastfindingsAndReachedTheHospital.setCompositionString("1 and 2 and (3 or 4 or 5 or 6) ");

        CohortIndicator transferedInForAbnormalBreastfindingsAndReachedTheHospitalIndicator = Indicators.newCountIndicator("numberOfPeopleTreatedForBreastConcernsWithoutRequiringReferralIndicator",
                transferedInForAbnormalBreastfindingsAndReachedTheHospital, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B7", "Number people referred from Health center  to the hospital for abnormal breast findings received by the hospital", new Mapped(
                transferedInForAbnormalBreastfindingsAndReachedTheHospitalIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");


        // B8. Number of people receiving diagnostic with  breast ultrasound

        SqlCohortDefinition numberofPeopleReceivingDiagnosticWithBreastUltrasound=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberofPeopleReceivingDiagnosticWithBreastUltrasound",breastCancerScreeningForms,breastUltrasound,YES);


        CompositionCohortDefinition numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition=new CompositionCohortDefinition();
        numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition.setName("numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition");
        numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition.getSearches().put("1",new Mapped<CohortDefinition>(numberofPeopleReceivingDiagnosticWithBreastUltrasound, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition.setCompositionString("1");

        CohortIndicator numberofPeopleReceivingDiagnosticWithBreastUltrasoundIndicator = Indicators.newCountIndicator("numberofPeopleReceivingDiagnosticWithBreastUltrasoundIndicator",
                numberofPeopleReceivingDiagnosticWithBreastUltrasoundComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B8", "Number of people receiving diagnostic with  breast ultrasound", new Mapped(
                numberofPeopleReceivingDiagnosticWithBreastUltrasoundIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B9. Number of  screened people with suspected breast cancer at breast ultrasound

        SqlCohortDefinition numberOfScreenedPeopleWithIntermediateSolidMass=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberOfScreenedPeopleWithIntermediateSolidMass",breastCancerScreeningForms,solidMass,intermediate);
        SqlCohortDefinition numberOfScreenedPeopleWithHighSuspiciousForMalignancySolidMass=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberOfScreenedPeopleWithHighSuspiciousForMalignancySolidMass",breastCancerScreeningForms,solidMass,highSuspiciousForMalignancy);

        CompositionCohortDefinition numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition=new CompositionCohortDefinition();
        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.setName("numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition");
        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.getSearches().put("1",new Mapped<CohortDefinition>(numberofPeopleReceivingDiagnosticWithBreastUltrasound, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.getSearches().put("2",new Mapped<CohortDefinition>(numberOfScreenedPeopleWithIntermediateSolidMass, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.getSearches().put("3",new Mapped<CohortDefinition>(numberOfScreenedPeopleWithHighSuspiciousForMalignancySolidMass, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));

        numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition.setCompositionString("1 and (2 or 3)");

        CohortIndicator numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundIndicator = Indicators.newCountIndicator("numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundIndicator",
                numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B9", "Number of  screened people with suspected breast cancer at breast ultrasound", new Mapped(
                numberOfScreenedPeopleWithSuspectedBreastCancerAtBreastUltrasoundIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        // B10. Number of  women with breast biopsy performed

        SqlCohortDefinition numberOfPatientWithBreastBiopsyPerformed=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberOfWomenWithBreastBiopsyPerformed",breastCancerScreeningForms,proceduresDone,BIOPSY);

        CompositionCohortDefinition numberOfWomenWithBreastBiopsyPerformedComposition=new CompositionCohortDefinition();
        numberOfWomenWithBreastBiopsyPerformedComposition.setName("numberOfWomenWithBreastBiopsyPerformedComposition");
        numberOfWomenWithBreastBiopsyPerformedComposition.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberOfWomenWithBreastBiopsyPerformedComposition.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberOfWomenWithBreastBiopsyPerformedComposition.getSearches().put("1",new Mapped<CohortDefinition>(numberOfPatientWithBreastBiopsyPerformed, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfWomenWithBreastBiopsyPerformedComposition.getSearches().put("2",new Mapped<CohortDefinition>(female, null));

        numberOfWomenWithBreastBiopsyPerformedComposition.setCompositionString("1 and 2");

        CohortIndicator numberOfWomenWithBreastBiopsyPerformedCompositionIndicator = Indicators.newCountIndicator("numberOfWomenWithBreastBiopsyPerformedCompositionIndicator",
                numberOfWomenWithBreastBiopsyPerformedComposition, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B10", "Number of  women with breast biopsy performed", new Mapped(
                numberOfWomenWithBreastBiopsyPerformedCompositionIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //B11. Number of women with suspected  breast cancer referred to other levels  (skipped)


        //B12. Number of women screened for cervical cancer with  Papsmear/cytology

        SqlCohortDefinition screenedForCervicalCancerWithPapSmear=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithPapSmear",screeningExaminationForms,screeningType,PAPANICOLAOUSMEAR);

        CompositionCohortDefinition femaleScreenedForCervicalCancerWithPapSmear=new CompositionCohortDefinition();
        femaleScreenedForCervicalCancerWithPapSmear.setName("femaleScreenedForCervicalCancerWithPapSmear");
        femaleScreenedForCervicalCancerWithPapSmear.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleScreenedForCervicalCancerWithPapSmear.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleScreenedForCervicalCancerWithPapSmear.getSearches().put("1",new Mapped<CohortDefinition>(screenedForCervicalCancerWithPapSmear, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithPapSmear.getSearches().put("2",new Mapped<CohortDefinition>(female, null));
        femaleScreenedForCervicalCancerWithPapSmear.setCompositionString("1 and 2");

        CohortIndicator femaleScreenedForCervicalCancerWithPapSmearIndicator = Indicators.newCountIndicator("femaleScreenedForCervicalCancerWithPapSmearIndicator",
                femaleScreenedForCervicalCancerWithPapSmear, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B12", "Number of women screened for cervical cancer with  Papsmear/cytology", new Mapped(
                femaleScreenedForCervicalCancerWithPapSmearIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //B13. Number of women  screened  with Papsmear with abnomal papsmear result

        SqlCohortDefinition screenedForCervicalCancerWithAbnormalPapSmearResults=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithAbnormalPapSmearResults",screeningExaminationForms,PAPANICOLAOUSMEAR,ABNORMAL);

        CompositionCohortDefinition femaleScreenedForCervicalCancerWithAbnormalPapSmearResults=new CompositionCohortDefinition();
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.setName("femaleScreenedForCervicalCancerWithAbnormalPapSmearResults");
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.getSearches().put("1",new Mapped<CohortDefinition>(screenedForCervicalCancerWithPapSmear, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithAbnormalPapSmearResults, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.getSearches().put("3",new Mapped<CohortDefinition>(female, null));
        femaleScreenedForCervicalCancerWithAbnormalPapSmearResults.setCompositionString("1 and 2 and 3");

        CohortIndicator femaleScreenedForCervicalCancerWithAbnormalPapSmearResultsIndicator = Indicators.newCountIndicator("femaleScreenedForCervicalCancerWithAbnormalPapSmearResultsIndicator",
                femaleScreenedForCervicalCancerWithAbnormalPapSmearResults, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B13", "Number of women  screened  with Papsmear with abnomal papsmear result", new Mapped(
                femaleScreenedForCervicalCancerWithAbnormalPapSmearResultsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //B14. Number of women  screened  with Papsmear with normal papsmear result

        SqlCohortDefinition screenedForCervicalCancerWithNormalPapSmearResults=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("screenedForCervicalCancerWithNormalPapSmearResults",screeningExaminationForms,PAPANICOLAOUSMEAR,NORMAL);

        CompositionCohortDefinition femaleScreenedForCervicalCancerWithNormalPapSmearResults=new CompositionCohortDefinition();
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.setName("femaleScreenedForCervicalCancerWithNormalPapSmearResults");
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.addParameter(new Parameter("startDate", "startDate", Date.class));
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.addParameter(new Parameter("endDate", "endDate", Date.class));
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.getSearches().put("1",new Mapped<CohortDefinition>(screenedForCervicalCancerWithPapSmear, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.getSearches().put("2",new Mapped<CohortDefinition>(screenedForCervicalCancerWithNormalPapSmearResults, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.getSearches().put("3",new Mapped<CohortDefinition>(female, null));
        femaleScreenedForCervicalCancerWithNormalPapSmearResults.setCompositionString("1 and 2 and 3");

        CohortIndicator femaleScreenedForCervicalCancerWithNormalPapSmearResultsIndicator = Indicators.newCountIndicator("femaleScreenedForCervicalCancerWithNormalPapSmearResultsIndicator",
                femaleScreenedForCervicalCancerWithNormalPapSmearResults, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B14", "Number of women  screened  with Papsmear with normal papsmear result", new Mapped(
                femaleScreenedForCervicalCancerWithNormalPapSmearResultsIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //B15. Number of  biopsy confirmed cervical cancer

        SqlCohortDefinition numberOfConfirmedCancer=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberOfConfirmedCancer",diagnosisScreeningForms,CONFIRMEDDIAGNOSIS,YES);
        SqlCohortDefinition numberOfCervicalCancerDiagnised=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberOfCervicalCancerDiagnised",diagnosisScreeningForms,confirmedCancerDiagnosis,cervicalCancer);

        CompositionCohortDefinition numberOfBiopsyConfirmedCervicalCancer=new CompositionCohortDefinition();
        numberOfBiopsyConfirmedCervicalCancer.setName("numberOfBiopsyConfirmedCervicalCancer");
        numberOfBiopsyConfirmedCervicalCancer.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberOfBiopsyConfirmedCervicalCancer.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberOfBiopsyConfirmedCervicalCancer.getSearches().put("1",new Mapped<CohortDefinition>(numberOfConfirmedCancer, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfBiopsyConfirmedCervicalCancer.getSearches().put("2",new Mapped<CohortDefinition>(numberOfCervicalCancerDiagnised, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfBiopsyConfirmedCervicalCancer.setCompositionString("1 and 2");

        CohortIndicator numberOfBiopsyConfirmedCervicalCancerIndicator = Indicators.newCountIndicator("numberOfBiopsyConfirmedCervicalCancerIndicator",
                numberOfBiopsyConfirmedCervicalCancer, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B15", "Number of  biopsy confirmed cervical cancer", new Mapped(
                numberOfBiopsyConfirmedCervicalCancerIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");

        //B16. Number of people with biopsy confimed Breast cancer

        SqlCohortDefinition numberOfBreastCancerDiagnised=Cohorts.getPatientsWithObservationInFormBetweenStartAndEndDate("numberOfBreastCancerDiagnised",diagnosisScreeningForms,confirmedCancerDiagnosis,breastCancer);

        CompositionCohortDefinition numberOfBiopsyConfirmedBreastCancer=new CompositionCohortDefinition();
        numberOfBiopsyConfirmedBreastCancer.setName("numberOfBiopsyConfirmedBreastCancer");
        numberOfBiopsyConfirmedBreastCancer.addParameter(new Parameter("startDate", "startDate", Date.class));
        numberOfBiopsyConfirmedBreastCancer.addParameter(new Parameter("endDate", "endDate", Date.class));
        numberOfBiopsyConfirmedBreastCancer.getSearches().put("1",new Mapped<CohortDefinition>(numberOfConfirmedCancer, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfBiopsyConfirmedBreastCancer.getSearches().put("2",new Mapped<CohortDefinition>(numberOfBreastCancerDiagnised, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}")));
        numberOfBiopsyConfirmedBreastCancer.setCompositionString("1 and 2");

        CohortIndicator numberOfBiopsyConfirmedBreastCancerIndicator = Indicators.newCountIndicator("numberOfBiopsyConfirmedBreastCancerIndicator",
                numberOfBiopsyConfirmedBreastCancer, ParameterizableUtil.createParameterMappings("startDate=${startDate},endDate=${endDate}"));


        dsd.addColumn("B16", "Number of people with biopsy confimed Breast cancer", new Mapped(
                numberOfBiopsyConfirmedBreastCancerIndicator, ParameterizableUtil.createParameterMappings("endDate=${endDate},startDate=${startDate}")), "");




    }

    private void setUpProperties() {
        oncologyBreastScreeningExamination=gp.getForm(GlobalPropertiesManagement.ONCOLOGY_BREAST_SCREENING_EXAMINATION);
        oncologyCervicalScreeningExamination=gp.getForm(GlobalPropertiesManagement.ONCOLOGY_CERVICAL_SCREENING_EXAMINATION);

        mUzimaBreastScreening=Context.getFormService().getForm("mUzima Breast cancer screening");
        mUzimaCervicalScreening=Context.getFormService().getForm("mUzima Cervical cancer screening");

        parameterNames.add("onOrBefore");
        parameterNames.add("onOrAfter");

        screeningExaminationForms.add(oncologyBreastScreeningExamination);
        screeningExaminationForms.add(oncologyCervicalScreeningExamination);
        screeningExaminationForms.add(mUzimaBreastScreening);
        screeningExaminationForms.add(mUzimaCervicalScreening);
        screeningType=Context.getConceptService().getConceptByUuid("7e4e6554-d6c5-4ca3-b371-49806a754992");
        HPV=Context.getConceptService().getConceptByUuid("f7c2d59d-2043-42ce-b04d-08564d54b0c7");


        oncologyScreeningLabResultsForm=Context.getFormService().getFormByUuid("d7e4f3e6-2462-427d-83df-97d8488a53aa");
        muzimaOncologyScreeningLabResults=Context.getFormService().getFormByUuid("3a0e1a09-c88a-4412-99c6-cdbd7add50fd");

        oncologyScreeningLabResultsForms.add(oncologyScreeningLabResultsForm);
        oncologyScreeningLabResultsForms.add(muzimaOncologyScreeningLabResults);

        testResult=Context.getConceptService().getConceptByUuid("bfb3eb1e-db98-4846-9915-0168511c6298");
        HPVpositive=Context.getConceptService().getConceptByUuid("1b4a5f67-6106-4a4d-a389-2f430be543e4");
        HPVNegative =Context.getConceptService().getConceptByUuid("64c23192-54e4-4750-9155-2ed0b736a0db");
        testResults.add(HPVpositive);
        testResults.add(HPVNegative);
        positiveTestResults.add(HPVpositive);

        mUzimaCervicalCancerScreeningFollowup=Context.getFormService().getFormByUuid("94470633-8a84-4430-9910-10dcd628a0a2");
        OncologyCervicalScreeningFollowUp=Context.getFormService().getFormByUuid("9de98350-bc86-4012-a559-fcce13fc10c5");

        cervicalCancerScreeningFollowupAndExaminationForms.add(mUzimaCervicalCancerScreeningFollowup);
        cervicalCancerScreeningFollowupAndExaminationForms.add(OncologyCervicalScreeningFollowUp);
        cervicalCancerScreeningFollowupAndExaminationForms.add(oncologyCervicalScreeningExamination);
        cervicalCancerScreeningFollowupAndExaminationForms.add(mUzimaCervicalScreening);

        typeOfVIAPerformed=Context.getConceptService().getConceptByUuid("820b0e37-5d3e-46c6-9462-a8e7adaff954");
        VIATriage=Context.getConceptService().getConceptByUuid("69a0ca97-2fee-4c4a-9d84-4f2c25f70c93");
        VIATriageInList.add(VIATriage);

        VIAResults = Context.getConceptService().getConceptByUuid("a37a937a-a2a6-4c22-975f-986fb3599ea3");
        VIAAndEligibleForThermalAblation = Context.getConceptService().getConceptByUuid("3fe69559-cc82-48cb-926e-5d925aca088b");
        VIAAndEligibleForLEEP = Context.getConceptService().getConceptByUuid("402f3951-420e-4c09-9a9a-955bc0cff140");

        VIAAndEligibleResults.add(VIAAndEligibleForThermalAblation);
        VIAAndEligibleResults.add(VIAAndEligibleForLEEP);

        VIANegative = Context.getConceptService().getConceptByUuid("a7b08a37-0380-49dd-8f12-c2c2c76c8b13");
        VIANegativeInList.add(VIANegative);

        VIAScreen = Context.getConceptService().getConceptByUuid("690d8a13-a1ac-4fd7-97a4-f3964b97f049");
        VIAScreenInList.add(VIAScreen);


        typeOfTreatmentPerformed =  Context.getConceptService().getConceptByUuid("f7719cd4-e591-4a39-8bfe-9dd93d3cab89");
        thermalAblation =  Context.getConceptService().getConceptByUuid("ef4d5bd3-2c4e-4ac2-9b91-9d4ba12b44f8");
        cryotherapy = Context.getConceptService().getConceptByUuid("e48bc8db-8f6e-4556-9083-0a000a136e95");
        thermalAblationAndCryotherapyList.add(thermalAblation);
        thermalAblationAndCryotherapyList.add(cryotherapy);

        reasonsForReferral= Context.getConceptService().getConceptByUuid("1aa373f4-4db5-4b01-bce0-c10a636bb931");
        loopElectrosurgicalExcisionProcedure =  Context.getConceptService().getConceptByUuid("55040927-bae5-410c-80da-c79f7574167f");
        loopElectrosurgicalExcisionProcedureInList.add(loopElectrosurgicalExcisionProcedure);

        LEEPAndColposcopy.add(loopElectrosurgicalExcisionProcedure); // Colposcopy not available in Reason fo referral


        suspectedCancer= Context.getConceptService().getConceptByUuid("55040927-bae5-410c-80da-c79f7574167f");
        suspectedCancerInList.add(suspectedCancer);

        biopsyperformed = Context.getConceptService().getConceptByUuid("5563e827-9a1e-40eb-b05b-81d55981ce6d");
        yes = Context.getConceptService().getConceptByUuid("3cd6f600-26fe-102b-80cb-0017a47871b2");
        yesInList.add(yes);

        oncologyCervicalCancerScreeningTransferIn=Context.getFormService().getFormByUuid("f939311f-53ac-4587-9a9a-48d41ea1b38b");
        oncologyCervicalCancerScreeningTransferInList.add(oncologyCervicalCancerScreeningTransferIn);

        mUzimaBreastCancerScreening = Context.getFormService().getForm("mUzima Breast cancer screening");

        breastCancerScreeningForms.add(mUzimaBreastCancerScreening);
        breastCancerScreeningForms.add(oncologyBreastScreeningExamination);

        reasonForBreastExam = Context.getConceptService().getConceptByUuid("0483b8fa-b6d2-4551-ab4c-b141399897d7");
        breastSymptoms = Context.getConceptService().getConceptByUuid("b761e2c9-3d07-4a03-b274-c38282cc723c");
        screening = Context.getConceptService().getConceptByUuid("d9e56001-1c4e-479b-95f1-878e18d7ece8");
        breastExamination = Context.getConceptService().getConceptByUuid("f9576ba2-e96f-4b48-a178-42e72e6382ca");
        ABNORMAL = Context.getConceptService().getConceptByUuid("3cd75230-26fe-102b-80cb-0017a47871b2");
        nextStep = Context.getConceptService().getConceptByUuid("69b9671b-d8b1-461b-bb7d-adb151775a57");
        referredTo = Context.getConceptService().getConceptByUuid("25782f2c-074f-4834-b7d2-4668cd645a57");
        furtherManagement = Context.getConceptService().getConceptByUuid("de3a4342-b07e-48f8-ab80-202aab697756");
        medicalImaging = Context.getConceptService().getConceptByUuid("7f779262-de04-425b-97f7-9e5cc834eb55");
        BIOPSY = Context.getConceptService().getConceptByUuid("db64df50-1db1-4f80-abe5-b0307d7d4f9e");
        OTHERNONCODED = Context.getConceptService().getConceptByUuid("3cee7fb4-26fe-102b-80cb-0017a47871b2");
        breastUltrasound = Context.getConceptService().getConceptByUuid("58eacedf-cb05-4a8f-a98d-cc5717348d74");
        YES = Context.getConceptService().getConceptByUuid("3cd6f600-26fe-102b-80cb-0017a47871b2");
        solidMass = Context.getConceptService().getConceptByUuid("9ca1695a-f070-462e-81ce-d9778ee749b6");
        intermediate = Context.getConceptService().getConceptByUuid("636fc0a4-b82e-4a18-a97a-2cd43af8b852");
        highSuspiciousForMalignancy = Context.getConceptService().getConceptByUuid("f133b708-d7cf-47b3-81aa-17e618d47b2d");
        proceduresDone = Context.getConceptService().getConceptByUuid("e44f17a3-28bd-4fe1-a4e1-1bb94779a1fd");
        PAPANICOLAOUSMEAR = Context.getConceptService().getConceptByUuid("3cd4de2e-26fe-102b-80cb-0017a47871b2");
        NORMAL = Context.getConceptService().getConceptByUuid("3cd750a0-26fe-102b-80cb-0017a47871b2");

        muzimaOncologyScreeningDiagnosis = Context.getFormService().getForm("muzima oncology screening Diagnosis");
        oncologyScreeningDiagnosis = Context.getFormService().getForm("Oncology Screening Diagnosis");

        diagnosisScreeningForms.add(muzimaOncologyScreeningDiagnosis);
        diagnosisScreeningForms.add(oncologyScreeningDiagnosis);
        CONFIRMEDDIAGNOSIS = Context.getConceptService().getConceptByUuid("3d762b82-f951-4d13-b147-6aaba63b25d1");
        confirmedCancerDiagnosis = Context.getConceptService().getConceptByUuid("3dc2eb50-6981-43d3-b907-e3dd8b5ed620");
        cervicalCancer = Context.getConceptService().getConceptByUuid("36052b70-ba49-466f-a4eb-bc99581be7a2");
        breastCancer = Context.getConceptService().getConceptByUuid("e1bd83f4-e9fa-4564-b8aa-74a9b199aca8");

        oncologyBreastCancerScreeningTransferIn = Context.getFormService().getForm("Oncology Breast Cancer Screening transfer in");
        oncologyBreastCancerScreeningTransferInList.add(oncologyBreastCancerScreeningTransferIn);


    }
}
