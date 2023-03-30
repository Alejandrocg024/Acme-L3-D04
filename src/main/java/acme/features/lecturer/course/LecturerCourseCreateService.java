
package acme.features.lecturer.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import SpamFilter.SpamFilter;
import acme.datatypes.Nature;
import acme.entities.Course;
import acme.entities.SystemConfiguration;
import acme.framework.components.models.Tuple;
import acme.framework.services.AbstractService;
import acme.roles.Lecturer;

@Service
public class LecturerCourseCreateService extends AbstractService<Lecturer, Course> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected LecturerCourseRepository repository;

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
		Course object;
		object = new Course();
		final Lecturer lecturer = this.repository.findOneLecturerByUserAccountId(super.getRequest().getPrincipal().getActiveRoleId());
		object.setLecturer(lecturer);
		object.setDraftMode(true);
		object.setCourseType(Nature.BALANCED);
		super.getBuffer().setData(object);
	}

	@Override
	public void bind(final Course object) {
		assert object != null;
		super.bind(object, "code", "title", "abstract$", "price", "furtherInformationLink");
	}

	@Override
	public void validate(final Course object) {
		assert object != null;
		final SystemConfiguration sc = this.repository.findSystemConfiguration();
		final SpamFilter spamFilter = new SpamFilter(sc.getSpamWords(), sc.getSpamThreshold());
		if (!super.getBuffer().getErrors().hasErrors("price"))
			super.state(object.getPrice().getAmount() > 0 && object.getPrice().getAmount() < 1000000, "price", "administrator.offer.form.error.price");
		if (!super.getBuffer().getErrors().hasErrors("title"))
			super.state(!spamFilter.isSpam(object.getTitle()), "title", "lecturer.course.form.error.spam");
		if (!super.getBuffer().getErrors().hasErrors("abstract$"))
			super.state(!spamFilter.isSpam(object.getAbstract$()), "abstract$", "lecturer.course.form.error.spam");
	}

	@Override
	public void perform(final Course object) {
		assert object != null;
		this.repository.save(object);
	}

	@Override
	public void unbind(final Course object) {
		assert object != null;
		Tuple tuple;
		tuple = super.unbind(object, "id", "code", "title", "abstract$", "price", "furtherInformationLink", "courseType", "draftMode", "lecturer");
		super.getResponse().setData(tuple);
	}
}
