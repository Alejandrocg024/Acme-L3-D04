
package acme.features.student.enrolment;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import acme.entities.Course;
import acme.entities.Enrolment;
import acme.framework.controllers.AbstractController;
import acme.roles.Lecturer;
import acme.roles.Student;

@Controller
public class StudentEnrolmentController extends AbstractController<Student, Enrolment> {

	@Autowired
	protected StudentEnrolmentListService	listAllService;

	@PostConstruct
	protected void initialise() {
		super.addBasicCommand("list", this.listAllService);
	}

}
