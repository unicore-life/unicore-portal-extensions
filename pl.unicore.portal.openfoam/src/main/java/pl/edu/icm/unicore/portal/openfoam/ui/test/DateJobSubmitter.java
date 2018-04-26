package pl.edu.icm.unicore.portal.openfoam.ui.test;

import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import org.apache.log4j.Logger;
import pl.edu.icm.unicore.portal.openfoam.JobHandler;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMGridEnvironment;
import sun.security.action.OpenFileInputStreamAction;

/**
 * Additional class which proxy job submission to JobHandler class.
 */
public class DateJobSubmitter implements Button.ClickListener {
    private static final Logger log = Logger.getLogger(DateJobSubmitter.class);

    private final OpenFOAMGridEnvironment gridEnvironment;
    private final JobHandler jobHandler;

    public DateJobSubmitter(JobHandler jobHandler, OpenFOAMGridEnvironment gridEnvironment) {
        this.gridEnvironment = gridEnvironment;
        this.jobHandler = jobHandler;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Notification.show("Do not press this button again");
        log.info(gridEnvironment);

        jobHandler.submitDateJob(gridEnvironment);
        log.info("Date job submitted!");
    }
}
