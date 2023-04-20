
package acme.features.student.enrolment;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.components.AuxiliarService;
import acme.entities.Course;
import acme.entities.Enrolment;
import acme.framework.components.accounts.Principal;
import acme.framework.components.jsp.SelectChoices;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;
import acme.roles.Student;

@Service
public class StudentEnrolmentCreateService extends AbstractService<Student, Enrolment> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected StudentEnrolmentRepository	repository;

	@Autowired
	protected AuxiliarService				auxiliarService;

	// AbstractService interface ----------------------------------------------


	@Override
	public void check() {
		super.getResponse().setChecked(true);
	}

	@Override
	public void authorise() {
		super.getResponse().setAuthorised(true);
	}

	@Override
	public void load() {
		Enrolment object;
		object = new Enrolment();

		final Principal principal = super.getRequest().getPrincipal();
		final Student student = this.repository.findStudentById(principal.getActiveRoleId());

		object.setDraftMode(true);
		object.setStudent(student);
		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final Enrolment object) {
		assert object != null;

		int courseId;
		Course course;

		courseId = super.getRequest().getData("course", int.class);
		course = this.repository.findCourseById(courseId);

		super.bind(object, "code", "motivation", "goals");
		object.setCourse(course);
	}

	@Override
	public void validate(final Enrolment object) {
		assert object != null;
		if (!super.getBuffer().getErrors().hasErrors("motivation"))
			super.state(this.auxiliarService.validateTextImput(object.getMotivation()), "motivation", "student.enrolment.form.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("goals"))
			super.state(this.auxiliarService.validateTextImput(object.getGoals()), "goals", "student.enrolment.form.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("code")) {
			Enrolment existing;
			existing = this.repository.findEnrolmentByCode(object.getCode());
			super.state(existing == null, "code", "student.enrolment.form.error.code");
		}
	}

	@Override
	public void perform(final Enrolment object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Enrolment object) {
		assert object != null;

		Collection<Course> courses;
		SelectChoices choices;
		Tuple tuple;

		choices = new SelectChoices();
		courses = this.repository.findAllPublishedCourses();
		for (final Course c : courses) {
			if (c.getId() == object.getCourse().getId()) {
				choices.add(Integer.toString(c.getId()), c.getCode() + "-" + c.getTitle(), true);
				continue;
			}
			choices.add(Integer.toString(c.getId()), c.getCode() + "-" + c.getTitle(), false);
		}
		choices.add("0", "---", false);

		tuple = super.unbind(object, "code", "motivation", "goals");
		tuple.put("course", choices.getSelected().getKey());
		tuple.put("courses", choices);

		super.getResponse().setData(tuple);
	}
}
