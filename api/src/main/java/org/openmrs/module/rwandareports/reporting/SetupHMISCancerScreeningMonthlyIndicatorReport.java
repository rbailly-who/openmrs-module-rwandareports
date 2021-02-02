package org.openmrs.module.rwandareports.reporting;

import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.*;
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


    }
}
