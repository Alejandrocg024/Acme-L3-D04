
package acme.testing.auditor.auditingRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import acme.entities.AuditingRecord;
import acme.testing.TestHarness;

public class AuditorAuditingRecordPublishTest extends TestHarness {

	@Autowired
	AuditorAuditingRecordTestRepository repository;


	@Test
	public void test100Positive() {
		//Nos traemos de la base de datos los registros de auditoría sin
		//publicar y los publicamos
		super.signIn("auditor1", "auditor1");
		final Collection<AuditingRecord> ars = this.repository.findNonPublishedAuditingRecordsByAuditorUsername("auditor1");
		for (final AuditingRecord ar : ars) {
			final String param = String.format("id=%d", ar.getId());
			super.request("/auditor/auditing-record/show", param);
			super.clickOnSubmit("Publish");
			super.checkNotErrorsExist();
			super.request("/auditor/auditing-record/show", param);
			super.checkNotSubmitExists("Publish");
		}

		super.signOut();
	}

	@Test
	public void test200Negative() {
		//Con el procedimiento anterior intentamos publicar auditorías que no se pueden publicar
		//debido a que ya lo están
		super.signIn("auditor2", "auditor2");
		final Collection<AuditingRecord> ars = this.repository.findPublishedAuditingRecordsByAuditorUsername("auditor2");
		for (final AuditingRecord ar : ars) {
			final String param = String.format("id=%d", ar.getId());
			super.request("/auditor/auditing-record/show", param);
			super.checkNotButtonExists("Publish");
			super.request("/auditor/auditing-record/publish", param);
			super.checkPanicExists();

		}

		super.signOut();
	}

	@Test
	public void test300Hacking() {
		//Intentamos publicar un registro de auditoría sin ser los creadores de dicho registro

		final Collection<AuditingRecord> auditingRecords = this.repository.findNonPublishedAuditingRecordsByAuditorUsername("auditor2");
		final List<AuditingRecord> ls = new ArrayList<>(auditingRecords);
		final String param = String.format("id=%d", ls.get(0).getId());//Traemos un registro de auditoría sin publicar de otro auditor e intentamos publicarlo

		super.signIn("auditor1", "auditor1");
		super.request("/auditor/audit/publish", param);
		super.checkPanicExists();
		super.signOut();

		super.signIn("administrator", "administrator");
		super.request("/auditor/audit/publish", param);
		super.checkPanicExists();
		super.signOut();

		super.signIn("lecturer1", "lecturer1");
		super.request("/auditor/audit/publish", param);
		super.checkPanicExists();
		super.signOut();

	}

}