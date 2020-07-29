package org.openmrs.module.rwandareports.web.extension;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.Extension;
import org.openmrs.module.rwandareports.patientsummary.PatientSummaryManager;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PatientDashboardActions extends Extension {

	@Override
	public String getOverrideContent(String bodyContent) {
		Integer patientId = Integer.parseInt(getParameterMap().get("patientId"));
		Patient patient = Context.getPatientService().getPatient(patientId);

		Set<Program> currentlyEnrolledPrograms = new HashSet<Program>();
		Collection<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null, null, null, false);//Context.getProgramWorkflowService().getCurrentPrograms(patient, null);
		for (PatientProgram pp : patientPrograms) {
			if ( pp.getActive() ) {
				currentlyEnrolledPrograms.add(pp.getProgram());
			}
		}

		StringBuilder sb = new StringBuilder();
		List<PatientSummaryManager> patientSummaries = Context.getRegisteredComponents(PatientSummaryManager.class);

		if (patientSummaries != null) {
			sb.append("<tr><td>");
			for (PatientSummaryManager manager : patientSummaries) {
				if (manager.getRequiredPrograms() == null || CollectionUtils.containsAny(manager.getRequiredPrograms(), currentlyEnrolledPrograms)) {
					sb.append("<a href=\"javascript:window.open('");
					sb.append("module/rwandareports/patientSummary.form");
					sb.append("?patientId=").append(patientId);
					sb.append("&type="+manager.getClass().getName()).append("'");
					sb.append(", 'summaryWindow', 'toolbar=no,width=660,height=600,resizable=yes,scrollbars=yes').focus()\">");
					sb.append(manager.getName());
					sb.append("</a>&nbsp;&nbsp;&nbsp;&nbsp;");
				}
			}
			sb.append("</td></tr>");
		}
		return sb.toString();
	}

	@Override
	public MEDIA_TYPE getMediaType() {
		return MEDIA_TYPE.html;
	}
}
