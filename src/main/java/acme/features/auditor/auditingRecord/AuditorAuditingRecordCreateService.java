
package acme.features.auditor.auditingRecord;

import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.components.AuxiliarService;
import acme.datatypes.Mark;
import acme.entities.Audit;
import acme.entities.AuditingRecord;
import acme.framework.components.accounts.Principal;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.helpers.MomentHelper;
import acme.framework.services.AbstractService;
import acme.roles.Auditor;

@Service
public class AuditorAuditingRecordCreateService extends AbstractService<Auditor, AuditingRecord> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected AuditorAuditingRecordRepository	repository;

	@Autowired
	protected AuxiliarService					auxiliarService;
	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		boolean status;
		status = super.getRequest().hasData("masterId", int.class);
		super.getResponse().setChecked(status);
	}

	@Override
	public void authorise() {
		Audit object;
		int masterId;
		masterId = super.getRequest().getData("masterId", int.class);
		object = this.repository.findAuditById(masterId);
		final Principal principal = super.getRequest().getPrincipal();
		final int userAccountId = principal.getAccountId();
		super.getResponse().setAuthorised(object.getAuditor().getUserAccount().getId() == userAccountId);
	}

	@Override
	public void load() {
		AuditingRecord object;
		object = new AuditingRecord();
		Audit audit;
		int masterId;
		masterId = super.getRequest().getData("masterId", int.class);
		audit = this.repository.findAuditById(masterId);
		object.setAssessment("");
		object.setSubject("");
		object.setExceptional(!audit.isDraftMode());
		object.setAudit(audit);
		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final AuditingRecord object) {
		assert object != null;
		super.bind(object, "subject", "assessment", "startPeriod", "endPeriod", "mark", "furtherInformationLink", "confirmation");
	}

	@Override
	public void validate(final AuditingRecord object) {
		boolean confirmation;

		confirmation = object.getAudit().isDraftMode() ? true : super.getRequest().getData("confirmation", boolean.class);
		super.state(confirmation, "confirmation", "javax.validation.constraints.AssertTrue.message");
		assert object != null;
		if (!super.getBuffer().getErrors().hasErrors("endPeriod") && !super.getBuffer().getErrors().hasErrors("startPeriod"))
			super.state(MomentHelper.isAfterOrEqual(object.getEndPeriod(), object.getStartPeriod()), "startPeriod", "auditor.auditing-record.form.error.post-date");
		if (!super.getBuffer().getErrors().hasErrors("endPeriod") && !super.getBuffer().getErrors().hasErrors("startPeriod"))
			super.state(MomentHelper.isAfterOrEqual(object.getEndPeriod(), MomentHelper.deltaFromMoment(object.getStartPeriod(), 1, ChronoUnit.HOURS)), "endPeriod", "auditor.auditing-record.form.error.not-enough-time");
		if (!super.getBuffer().getErrors().hasErrors("subject"))
			super.state(this.auxiliarService.validateTextImput(object.getSubject()), "subject", "auditor.auditing-record.form.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("assessment"))
			super.state(this.auxiliarService.validateTextImput(object.getSubject()), "assessment", "auditor.auditing-record.form.error.spam");

	}

	@Override
	public void perform(final AuditingRecord object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final AuditingRecord object) {
		assert object != null;
		Tuple tuple;
		tuple = super.unbind(object, "subject", "assessment", "startPeriod", "endPeriod", "mark", "furtherInformationLink");
		final SelectChoices choices;
		choices = SelectChoices.from(Mark.class, object.getMark());
		tuple.put("mark", choices.getSelected().getKey());
		tuple.put("marks", choices);
		tuple.put("masterId", super.getRequest().getData("masterId", int.class));
		tuple.put("draftMode", object.getAudit().isDraftMode());
		tuple.put("confirmation", false);
		super.getResponse().setData(tuple);
	}
}