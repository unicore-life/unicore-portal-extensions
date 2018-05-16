/*
 * Copyright (c) 2014 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package pl.edu.icm.unicore.portal.openfoam.ui.job;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import eu.unicore.portal.core.User;
import eu.unicore.portal.core.i18n.MessageProvider;
import eu.unicore.portal.grid.ui.helpers.ProjectChooserComponent;
import pl.edu.icm.unicore.portal.openfoam.JobProfile;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMJobSpecification;
import pl.edu.icm.unicore.portal.openfoam.OpenFOAMProperties;

/**
 * Responsible for getting job's input.
 *
 * @author K. Benedyczak
 */
public class OpenFOAMInputPanel extends CustomComponent {
    private static final String LOCAL_L = "l";
    private static final String GRID_L = "g";

    private String inputId;
    private OpenFOAMProperties config;
    private Tab parent = null;
    private final MessageProvider msg;

    private ProjectChooserComponent project;
    private TextField jobName;
    private ComboBox execProfileChooser;
    private OptionGroup inputSourceChooser;

    private JobInputGridPanel gridInput;
    private JobInputUploadPanel uploadedInput;
    private User user;

    public OpenFOAMInputPanel(String inputId, OpenFOAMProperties config, MessageProvider msg, User user) {
        this.msg = msg;
        this.inputId = inputId;
        this.config = config;
        this.user = user;
        initUI();
    }

    private void initUI() {
        VerticalLayout main = new VerticalLayout();
        main.setMargin(true);
        setCompositionRoot(main);
        main.setSpacing(true);

        jobName = new TextField(msg.getMessage("OpenFOAM.OpenFOAMInput.jobName"));
        jobName.setRequired(true);
        jobName.setValue(msg.getMessage("OpenFOAM.OpenFOAMInput.defaultJobName"));
        jobName.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                if (parent != null)
                    parent.setCaption(jobName.getValue());
            }
        });
        jobName.setImmediate(true);
        String grantsAttribute = config.getValue(OpenFOAMProperties.GRANTS_ATTRIBUTE);
        project = new ProjectChooserComponent(grantsAttribute, msg.getMessage("OpenFOAM.OpenFOAMInput.grant"));

        uploadedInput = new JobInputUploadPanel(inputId, config, msg, user);
        gridInput = new JobInputGridPanel(msg);
        gridInput.setVisible(false);

        inputSourceChooser = new OptionGroup();
        inputSourceChooser.addItem(LOCAL_L);
        inputSourceChooser.setItemCaption(LOCAL_L, msg.getMessage("OpenFOAM.OpenFOAMInput.localInput"));
        inputSourceChooser.addItem(GRID_L);
        inputSourceChooser.setItemCaption(GRID_L, msg.getMessage("OpenFOAM.OpenFOAMInput.gridInput"));
        inputSourceChooser.setImmediate(true);
        inputSourceChooser.select(LOCAL_L);
        inputSourceChooser.addValueChangeListener(new ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                boolean localInput = LOCAL_L.equals(inputSourceChooser.getValue());
                uploadedInput.setVisible(localInput);
                gridInput.setVisible(!localInput);
            }
        });

        HorizontalLayout inputsLay = new HorizontalLayout();
        inputsLay.setSpacing(true);
        inputsLay.addComponents(uploadedInput, gridInput);

//        execProfileChooser = new ComboBox(msg.getMessage("OpenFOAM.OpenFOAMInput.profile"));
//        for (JobProfile p : JobProfile.values())
//            execProfileChooser.addItem(p.name());
//        execProfileChooser.select(JobProfile.normal.name());
//        execProfileChooser.setNullSelectionAllowed(false);

//        FormLayout params = new FormLayout(jobName, project, inputSourceChooser, inputsLay,
//                execProfileChooser);
        FormLayout params = new FormLayout(jobName, project, inputSourceChooser, inputsLay);

        main.addComponents(params);
    }

    public void setParent(Tab parent) {
        this.parent = parent;
    }

    public String getJobName() {
        return jobName.getValue();
    }

    public OpenFOAMJobSpecification getJobDescription() throws Exception {
        validate();
        boolean localInput = LOCAL_L.equals(inputSourceChooser.getValue());
        String grant = project.getProject();
        if (grant.trim().equals(""))
            grant = null;
        return new OpenFOAMJobSpecification(jobName.getValue(),
                grant,
                inputId,
                localInput ? null : gridInput.getGridInputFile().toString()
                );
    }

    private void validate() throws Exception {
        boolean error = false;
        if (jobName.getValue() == null || jobName.getValue().equals("")) {
            jobName.setComponentError(new UserError(msg.getMessage("OpenFOAM.OpenFOAMInput.errorRequired")));
            error = true;
        } else
            jobName.setComponentError(null);

        boolean localInput = LOCAL_L.equals(inputSourceChooser.getValue());
        if (localInput) {
            if (!uploadedInput.isUploadCompleted()) {
                uploadedInput.setComponentError(new UserError(
                        msg.getMessage("OpenFOAM.OpenFOAMInput.errorNoUploadedInput")));
                error = true;
            } else {
                uploadedInput.setComponentError(null);
            }
            gridInput.setComponentError(null);
        } else {
            if (gridInput.getGridInputFile() == null) {
                gridInput.setComponentError(new UserError(
                        msg.getMessage("OpenFOAM.OpenFOAMInput.errorNoGridInput")));
                error = true;
            } else
                gridInput.setComponentError(null);
            uploadedInput.setComponentError(null);
        }

        if (error)
            throw new IllegalStateException();
    }
}
