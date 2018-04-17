/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam.ui.job;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import com.vaadin.ui.themes.ValoTheme;
import de.fzj.unicore.uas.client.JobClient;
import de.fzj.unicore.wsrflite.xmlbeans.WSUtilities;
import eu.unicore.griddisco.core.api.UserGridDiscovery;
import eu.unicore.griddisco.core.model.AtomicJob;
import eu.unicore.portal.core.PortalThreadPool;
import eu.unicore.portal.core.Session;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.core.threads.IProgressMonitor;
import eu.unicore.portal.grid.ui.helpers.JobMonitoring;
import eu.unicore.portal.grid.ui.helpers.JobMonitoringComponent;
import eu.unicore.portal.grid.ui.helpers.JobStatusListener;
import eu.unicore.portal.ui.BackgroundWorker;
import eu.unicore.portal.ui.IconUtil;
import eu.unicore.portal.ui.Styles;
import eu.unicore.portal.ui.icons.IconRepository;
import eu.unicore.util.httpclient.IClientConfiguration;
import org.apache.log4j.Logger;
import org.chemomentum.workassignment.xmlbeans.GetWorkAssignmentStatusResponseDocument.GetWorkAssignmentStatusResponse;
import org.unigrids.services.atomic.types.StatusType;
import org.unigrids.services.atomic.types.StatusType.Enum;
import pl.edu.icm.unicore.portal.openfoam.JobHandler;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMGridEnvironment;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMJSDLCreator;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMJobSpecification;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMProperties;

/**
 * Main panel of a single job.
 * In case job was not submitted the input preparation panel is editable.
 * After submission the input data becomes read-only and job status monitoring is shown.
 * After the job is finished its output is shown.
 *
 * @author K. Benedyczak
 */
public class OpenFOAMJobPanel extends CustomComponent {
    private static final Logger log = Logger.getLogger(OpenFOAMJobPanel.class);
    private final OpenFOAMProperties config;
    private final JobHandler jobHandler;
    private final MessageProvider msg;
    private final OpenFOAMGridEnvironment gridEnvironment;

    private OpenFOAMInputPanel inputPanel;
    private Button submitWA;
    //	private SinusInputROPanel inputROPanel;
//	private JobOutputPanel jobOutputPanel;
    private HorizontalLayout jobStatusPanel;
    private JobMonitoringComponent jobMonitoringComponent;
    private PortalThreadPool tpool;
    private UserGridDiscovery discovery;

    public OpenFOAMJobPanel(OpenFOAMProperties config, JobHandler jobHandler, MessageProvider msg,
                            PortalThreadPool tpool, UserGridDiscovery discovery, OpenFOAMGridEnvironment gridEnvironment) {
        this.config = config;
        this.jobHandler = jobHandler;
        this.msg = msg;
        this.tpool = tpool;
        this.discovery = discovery;
        this.gridEnvironment = gridEnvironment;
        init();
    }

    public OpenFOAMJobPanel(OpenFOAMProperties config, OpenFOAMJobSpecification existingJob, AtomicJob job,
                            JobHandler jobHandler, MessageProvider msg, PortalThreadPool tpool,
                            UserGridDiscovery discovery, OpenFOAMGridEnvironment gridEnvironment) {
        this(config, jobHandler, msg, tpool, discovery, gridEnvironment);
        JobMonitoring jobMonitoringEngine = new JobMonitoring(discovery, job, jobMonitoringComponent);

        try {
            jobMonitoringComponent.setCustomListener(new MyJobStatusListener(UI.getCurrent(),
                    Session.getCurrent().getUser().getCredentials()));
        } catch (Exception e) {
            Notification.show(msg.getMessage("OpenFOAM.OpenFOAMPanel.invalidGridState"),
                    Type.ERROR_MESSAGE);
            log.error("Can not open OpenFOAM job. See details of the exception.", e);
            return;
        }

        jobMonitoringEngine.start();
//		switchToJobMonitoring(existingJob);
    }

    @Override
    public String getCaption() {
        return inputPanel.getJobName();
    }

    private void init() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);
        String inputId = WSUtilities.newUniqueID();
        inputPanel = new OpenFOAMInputPanel(inputId, config, msg, Session.getCurrent().getUser());

        submitWA = new Button(msg.getMessage("OpenFOAM.OpenFOAMJobPanel.submit"));
        submitWA.setIcon(IconUtil.getIconFromTheme(IconRepository.ICON_ID_STARTWF));
        submitWA.addStyleName(Styles.MARGIN_TOP_BOTTOM_15);
        submitWA.addStyleName(Reindeer.BUTTON_LINK);
        submitWA.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                submitJob();
            }
        });

//		inputROPanel = new SinusInputROPanel(msg);
//		inputROPanel.setVisible(false);

        jobStatusPanel = new HorizontalLayout();
        jobStatusPanel.setSpacing(true);
        jobStatusPanel.setMargin(new MarginInfo(false, false, false, true));

        Label statusLabel = new Label(msg.getMessage("OpenFOAM.OpenFOAMJobPanel.status"));
        statusLabel.addStyleName(ValoTheme.LABEL_LARGE);

        jobMonitoringComponent = new JobMonitoringComponent(msg, tpool);


        jobStatusPanel.addComponents(statusLabel, jobMonitoringComponent);
        jobStatusPanel.setComponentAlignment(statusLabel, Alignment.MIDDLE_CENTER);
        jobStatusPanel.setVisible(false);

//		jobOutputPanel = new JobOutputPanel(msg, tpool);
//		jobOutputPanel.setVisible(false);

//		vl.addComponents(inputPanel, submitWA, inputROPanel, jobStatusPanel, jobOutputPanel);
        vl.addComponents(inputPanel, submitWA, jobStatusPanel);

        setCompositionRoot(vl);
    }

    public synchronized void setParent(Tab parent) {
        inputPanel.setParent(parent);
//        if (inputROPanel.isVisible())
//            parent.setCaption(inputROPanel.getCaption());
    }

    private synchronized void submitJob() {
        final OpenFOAMJobSpecification jobDesc;
        try {
            jobDesc = inputPanel.getJobDescription();
        } catch (Exception e) {
            Notification.show(msg.getMessage("OpenFOAM.OpenFOAMJobPanel.invalidInput"),
                    Type.ERROR_MESSAGE);
            return;
        }

        BackgroundWorker bgWorker = new BackgroundWorker("", tpool.getExecutor()) {
            private Exception saved;

            @Override
            protected void work(IProgressMonitor progress) {
                try {
                    doSubmitSynchro(jobDesc);
                } catch (Exception e) {
                    saved = e;
                }
            }

            @Override
            protected void updateUI() {
                if (saved != null) {
                    Notification.show(msg.getMessage("OpenFOAM.OpenFOAMJobPanel.invalidGridState"),
                            Type.ERROR_MESSAGE);
                    log.error("Can not submit OpenFOAM job, as job handler failed. "
                            + "See details of the exception.", saved);
                } else {
                    switchToJobMonitoring(jobDesc);
                }
            }
        };
        bgWorker.schedule();
    }

    private void doSubmitSynchro(OpenFOAMJobSpecification jobDesc) throws Exception {
        jobMonitoringComponent.setCustomListener(new MyJobStatusListener(UI.getCurrent(),
                Session.getCurrent().getUser().getCredentials()));
        String waId = jobHandler.submitJob(jobDesc, gridEnvironment);
        JobMonitoring jobMonitoring = new JobMonitoring(discovery,
                gridEnvironment.getBrokerService(), waId, jobMonitoringComponent);
        jobMonitoring.start();
    }

    private void switchToJobMonitoring(OpenFOAMJobSpecification jobDesc) {
        inputPanel.setVisible(false);
        submitWA.setVisible(false);

//        inputROPanel.setJob(jobDesc);
//        inputROPanel.setVisible(true);
        jobStatusPanel.setVisible(true);
    }

    /**
     * We want to get notified when the job is submitted. Then we update the information about the
     * job: grid storage being used, times, execution site.
     *
     * @author K. Benedyczak
     */
    private class MyJobStatusListener implements JobStatusListener {
        private final UI ui;
        private final IClientConfiguration clientCfg;

        public MyJobStatusListener(UI ui, IClientConfiguration clientCfg) {
            this.ui = ui;
            this.clientCfg = clientCfg;
        }

        @Override
        public void start() {
        }

        @Override
        public void atomicJobUpdated(Enum status, AtomicJob job, boolean isTerminal) {
            ui.getSession().lock();
            OpenFOAMJobSpecification extractedJob;
            try {
                extractedJob = OpenFOAMJSDLCreator.createJobSpecification(
                        job.getProperties());
//                inputROPanel.setJob(extractedJob);

                if (status == StatusType.SUCCESSFUL) {
//                    jobOutputPanel.setVisible(true);
                    JobClient jobClient = new JobClient(job.getAddress(), clientCfg);
//                    jobOutputPanel.setJob(jobHandler.getStorageUtil(),
//                            jobClient, extractedJob);
                }
            } catch (Exception e) {
                log.error("Can't fill the job's details panel", e);
            } finally {
                ui.getSession().unlock();
            }
        }

        @Override
        public void brokeredJobUpdated(GetWorkAssignmentStatusResponse waStat,
                                       boolean isTerminal) {
        }

        @Override
        public boolean monitoringFailure(Exception e) {
            return false;
        }
    }
}
