<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<openmrs:htmlInclude file="/moduleResources/rwandareports/jquery.js" />
<!-- <script type="text/javascript">
	var $j = jQuery.noConflict();
</script> -->
<script type="text/javascript">
function msgreg(){
document.getElementById('msg').innerHTML="<div id='openmrs_msg'>Registering...</div>";
exit();
}
function msgrem(){
	document.getElementById('msg').innerHTML="<div id='openmrs_msg'>Removing...</div>";
	exit();
	}
</script>
<style>
table.reports{
border-collapse: collapse;
border: 1px solid blue;
width: 100%;
}
.reports td{
border-collapse: collapse;
border: 1px solid blue;
}
.reports .tableheaders{
font-weight: bold;
background-color: #B0C4DE;
}
.reports .tabletd{
font-weight: bold;
background-color: #EEE;
}

.reports .alt { background-color: #B0C4DE; }
.reports .altodd { background-color: #EEE; }
.reports .hover { background-color: #DED; }
.reports .althover { background-color: #EFE; }
</style>
<script type="text/javascript">
$(document).ready(function(){
	$('tr:even').addClass('alt');
	$('tr:even').hover(
			function(){$(this).addClass('hover')},
			function(){$(this).removeClass('hover')}
	);
	$('tr:odd').addClass('altodd');
	$('tr:odd').hover(
			function(){$(this).addClass('althover')},
			function(){$(this).removeClass('althover')}
	);
});
</script>
<div id="msg"></div>
<h2>Register Reports for Rwanda</h2>

<br />
<br />
<h3>Category Register/Remove Reports</h3>
<br /><br />

<table class="reports" style="width:100%;">
<tr><th>ALL</th><th>HIV</th><th>NCD</th><th>ONCOLOGY</th><th>CENTRAL</th><th>SITE</th><th>PRIMARY CARE</th><th>CHW</th><th>PDC</th><th>Mental Health</th></tr>

<tr>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allReports.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allHIVReports.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allNCDReports.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allOncologyReport.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allCentralReports.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allSiteReports.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allPCReport.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allCHWReport.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allPDCReport.form" onclick=msgreg(this)>(Re)Register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_allMHReport.form" onclick=msgreg(this)>(Re)Register</a></td>
</tr>
<tr>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_all.form" onclick=msgrem(this)>Remove All </a></td>
<td colspan="9"><center><b>The category remove is not yet available</b></center></td>
</tr>

</table>

<br /><br />


<h3>Individual Register/Remove Report</h3>
<br /><br />
<table class="reports" style="width:100%;">
<tr class="tableheaders">
<td>Categories</td>
<td>Report Name</td>
<td>Run</td>
<td colspan="2"><center>Action</center></td>
</tr>
<tr>
<td rowspan="3" class="tabletd">PIH Reports</td>
<td>PIH-Boston Indicators-Quarterly
</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_quarterlyCrossDistrictIndicator.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_quarterlyCrossDistrictIndicator.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PIH-Boston Viral Load Indicators-Quarterly</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_viralLoad.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_viralLoad.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PIH-Eligible For Viral Load</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_eligibleForViralLoadReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_eligibleForViralLoadReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<tr>
<!--<td rowspan="2" class="tabletd">Registers</td>
<td>Adult HIV Art Register report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adulthivartregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adulthivartregister.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>Pedi HIV Art Register report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pedihivartregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pedihivartregister.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>TRAC Mother-Infant Pair Follow-up Register report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctregister.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctregister.form" onclick=msgrem(this)>Remove</a></td>
</tr>-->
<tr>
<td rowspan="2" class="tabletd">Adult HIV Program</td>
<td>HIV-Adult Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adultHIVConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultHIVConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>HIV-CROWN Reports</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_CROWNReports.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_CROWNReports.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td class="tabletd">Pedi HIV Program</td>
<td>Pedi HIV Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pediHIVConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pediHIVConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td class="tabletd">TB Program</td>
<td>TB-Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_tbConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_tbConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<tr>
<td rowspan="5" class="tabletd">PMTCT Reports</td>
<td>HIV-PMTCT Combined Clinic Consultation sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_combinedHSCSPConsultation.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_combinedHSCSPConsultation.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<%-- <tr>
<td>HIV-PMTCT Food Package Distribution</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFoodDistributionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFoodDistributionSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>HIV-PMTCT Formula Package Distribution</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFormulaDistributionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFormulaDistributionSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr> --%>
<tr>
<td>HIV-PMTCT Pregnancy consultation sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctPregnancyConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctPregnancyConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<%-- <tr>
<td>DQ-HIV PMTCT Form Completion</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctFormCompletionSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctFormCompletionSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr> --%>
<tr>
<td>HIV-PMTCT Combined Clinic Mother Report-Monthly</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctCombinedClinicMotherMonthlyReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctCombinedClinicMotherMonthlyReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>HIV-PMTCT Exposed Infant Clinical Report-Monthly</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctCombinedClinicInfantReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctCombinedClinicInfantReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>HIV-PMTCT Pregnancy Report-Monthly</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pmtctPregMonthlyReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pmtctPregMonthlyReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="3" class="tabletd">Patient Follow-up</td>
<td>HIV-Adult Report-Monthly</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_adultLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_adultLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a></td>
</tr><tr>
<td>HIV-Pedi Report-Monthly</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pediatricLatevisitAndCD4.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pediatricLatevisitAndCD4.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<%-- <tr>
<td>HIV-ART Decline-Monthly</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_monthlyCD4Decline.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_monthlyCD4Decline.form" onclick=msgrem(this)>Remove</a></td>
</tr> --%>
<tr>
<td>HIV-Indicator Report-Quarterly</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_IDProgramQuarterlyIndicators.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_IDProgramQuarterlyIndicators.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td class="tabletd">Primary Care</td>
<td>PC-Rwanda Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_rwandaPrimaryCareReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_rwandaPrimaryCareReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="1" class="tabletd">Heart Failure</td>
<td>Heart Failure Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="2" class="tabletd">Data Quality</td>
<%-- <td>DQ-HIV CD4 Labs with Missing Data</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_missingCD4Report.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_missingCD4Report.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr> --%>
<td>DQ-Data Quality Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_dataQualityReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_dataQualityReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>DQ-Data Entry Delay Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_dataDelay.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_dataDelay.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="1" class="tabletd">Data Quantity</td>
<td>Data Quantity Report</td>
<td>central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_DataEntryQuantityReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_DataEntryQuantityReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="3" class="tabletd">Research</td>
<td>DQ-HIV Research Data Quality</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_hivResearchDataQuality.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_hivResearchDataQuality.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>Research-Extraction Data for HIV Research</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_hivResearchDataExtraction.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_hivResearchDataExtraction.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>Research-Primary Care Registration Data</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_PrimaryCareRegistrationData.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_PrimaryCareRegistrationData.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="1" class="tabletd">HMIS Report</td>
<td>HIV-HMIS Report</td>
<td>At site</td>
<td> <a href="${pageContext.request.contextPath}/module/rwandareports/register_hmisReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td> <a href="${pageContext.request.contextPath}/module/rwandareports/remove_hmisReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="1" class="tabletd">PBF Report</td>
<td>PBF Report</td>
<td>At site</td>
<td> <a href="${pageContext.request.contextPath}/module/rwandareports/register_PBFReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td> <a href="${pageContext.request.contextPath}/module/rwandareports/remove_PBFReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<tr>
<td rowspan="18" class="tabletd">NCD Reports</td>
<td>NCD-Diabetes Indicator Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_DiabetesQuarterlyAndMonthReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_DiabetesQuarterlyAndMonthReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD Consult Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_NCDConsult.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_NCDConsult.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD Late Visit and Lost to Follow Up</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_NCDlatevistAndLTFU.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_NCDlatevistAndLTFU.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Diabetes Consultation sheet and Late Visit</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_DiabetesConsultAndLTFU.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_DiabetesConsultAndLTFU.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Asthma Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_asthmaConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_asthmaConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Asthma Indicator Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_asthmaQuarterlyAndMonthReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_asthmaQuarterlyAndMonthReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Asthma Late Visit</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_asthmaLateVisitReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_asthmaLateVisitReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Hypertension Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_hypertensionConsultationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_hypertensionConsultationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Hypertension Late Visit</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_hypertensionLateVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_hypertensionLateVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Hypertension Indicator Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_hypertensionQuarterlyAndMonthlyReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_hypertensionQuarterlyAndMonthlyReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Epilepsy Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_epilepsyConsultSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_epilepsyConsultSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Epilepsy Late Visit</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_epilepsyLateVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_epilepsyLateVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Heart Failure Consultation Sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureConsultSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureConsultSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Heart Failure Late Visit</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureLateVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureLateVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-Heart Failure Indicator Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_heartFailureQuarterlyAndMonthlyReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_heartFailureQuarterlyAndMonthlyReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-CKD Consulation sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_CKDConsultSheetReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_CKDConsultSheetReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-CKD Late Visit</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_CKDMissedVisitReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_CKDMissedVisitReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>NCD-CKD Indicator Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_CKDIndicatorReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_CKDIndicatorReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="15" class="tabletd">Oncology</td>
<td>ONC-Chemotherapy Treatment Administration Plan</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_treatmentAdministrationPlan.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_treatmentAdministrationPlan.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Oncology Expected Patient List - Inpatient ward and Infusion center</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_chemotherapyPatientList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_chemotherapyPatientList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Oncology Expected Patient List - Outpatient Ward</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyOutpatientExpectedPatientList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyOutpatientExpectedPatientList.form" onclick=msgrem(this)>Remove</a></td>
</tr>

<tr>
<td>ONC-Oncology Missed Visit Patient List - Outpatient Ward</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyOutpatientClinicMissedVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyOutpatientClinicMissedVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr>

<tr>
<td>ONC-Oncology Missed Visit Patient List - Inpatient Ward</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyInpatientClinicMissedVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyInpatientClinicMissedVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Chemotherapy Daily Expected Patient List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_chemotherapyDailyPatientList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_chemotherapyDailyPatientList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Chemotherapy Missed Patient List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_missedChemotherapyPatientList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_missedChemotherapyPatientList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Chemotherapy Daily Drug List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_chemotherapyDailyDrugList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_chemotherapyDailyDrugList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Oncology Outpatient Clinic Patient List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyOutpatientClinicPatientList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyOutpatientClinicPatientList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<%-- <tr>
<td>ONC-Oncology Outpatient Clinic Missed Visit Patient List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyOutpatientClinicMissedVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyOutpatientClinicMissedVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr> --%>
<tr>
<td>ONC-Oncology Outpatient Clinic Appointment List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyOutpatientAppointment.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyOutpatientAppointment.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Biopsy Results/Tracking Contact List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyBiopsyList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyBiopsyList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-External Biopsy Results/Tracking Contact List</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyExternalBiopsyList.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyExternalBiopsyList.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Data extraction sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologydataextrationSheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologydataextrationSheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Indicator Report-Quarterly</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_oncologyquarterlyindicatorreport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_oncologyquarterlyindicatorreport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>ONC-Registry</td>
<td>central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_OncologyRegistry.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_OncologyRegistry.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<tr>
<td rowspan="5" class="tabletd">PDC Reports</td>
<td>PDC-Weekly and daily Consulation sheet</td>
<td>At Site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pdcWeeklySheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pdcWeeklySheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PDC-Monthly Consulation sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pdcMonthlySheet.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pdcMonthlySheet.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PDC-Indicator report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pdcIndicatorReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pdcIndicatorReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PDC Missed Visits report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pdcMissedVisitsReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pdcMissedVisitsReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>PDC-Monthly Lost to Follow-up report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_pdcMonthlyLTFUReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_pdcMonthlyLTFUReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td rowspan="3" class="tabletd">Generic Reports</td>
<td>Generic Encounter Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_EncounterAndObsReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_EncounterAndObsReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>Generic Patient by Program Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_PatientByProgramReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_PatientByProgramReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>Generic Drug Report</td>
<td>Central</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_DrugReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_DrugReport" onclick=msgrem(this)>Remove</a></td>
</tr>





<tr>
<td rowspan="3" class="tabletd">MOH Reports</td>
<td>MOH-HMIS Indicator report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_MOH-HMISIndicatorreport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_MOH-HMISIndicatorreport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>District Hospital Monthly HMIS Report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_HMISIndicatorMonthly.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_HMISIndicatorMonthlyReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td>-</td>
<td>-</td>
<td>-</td>
<td>-</td>
</tr>
<tr>
<td rowspan="3" class="tabletd"> Mental Health reports </td>
<td> Mental Health Consultation sheet</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_MentalHealthConsultationreport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_MentalHealthConsultationreport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td> Mental Health Late Visit</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_MentalHealthLateVisit.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_MentalHealthLateVisit.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr>
<td> Mental Health Indicator report</td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_MentalHealthIndicatorReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_MentalHealthIndicatorReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>

<tr >
<td class="tabletd"> Monthly Executive Dashboard Metrics Report </td>
<td> Monthly Executive Dashboard Metrics Report </td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_MonthlyExecutiveDashboardMetricsReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_MonthlyExecutiveDashboardMetricsReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>
<tr >
<td class="tabletd"> Lab Reports </td>
<td> Lab - Results Report </td>
<td>At site</td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/register_LabResultReport.form" onclick=msgreg(this)>(Re) register</a></td>
<td><a href="${pageContext.request.contextPath}/module/rwandareports/remove_LabResultReport.form" onclick=msgrem(this)>Remove</a></td>
</tr>



</table>
<%@ include file="/WEB-INF/template/footer.jsp"%>


