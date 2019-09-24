package com.exclamationlabs.connid.xlsx;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import java.io.File;

public class Configuration extends AbstractConfiguration {

    private String directoryPathProperty;
    private String fileNameProperty;
    private String identifierProperty;
    private String groupIdentifierProperty;
    private Boolean includesHeaderProperty;

    @ConfigurationProperty(
            displayMessageKey = "File Name",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "The File Name matching rule for the export File",
            order = 1,
            confidential = false,
            required = true)
    public String getFileNameProperty() {
        return fileNameProperty;
    }

    public void setFileNameProperty(String fileNameProperty) {
        this.fileNameProperty = fileNameProperty;
    }

    @ConfigurationProperty(
            displayMessageKey = "Directory Path",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Set the directory path to the export File",
            order = 2,
            confidential = false,
            required = true)
    public String getDirectoryPathProperty() {
        return directoryPathProperty;
    }

    public void setDirectoryPathProperty(String directoryPathProperty) {
        this.directoryPathProperty = directoryPathProperty;
    }

    public String getFilePath() {

        File file = new File(this.directoryPathProperty);

        if(file.exists() && file.isDirectory()) {
            String[] files = file.list();

            for(String fileName : files) {
                if(fileName.startsWith(this.fileNameProperty)) {
                    return new File(this.directoryPathProperty, fileName).getPath();
                }
            }
        }
        throw new ConnectorIOException("Configured XLSX was not found.");
    }

    @ConfigurationProperty(
            displayMessageKey = "Includes Header",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Does the file contain a heading line",
            order = 3,
            confidential = false,
            required = false)
    public Boolean getIncludesHeaderProperty() {
        return includesHeaderProperty;
    }

    public void setIncludesHeaderProperty(Boolean includesHeaderProperty) {
        this.includesHeaderProperty = includesHeaderProperty;
    }

    @ConfigurationProperty(
            displayMessageKey = "Identifier Column Name",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Identifying column name, if file does not include a header this would be colX",
            order = 4,
            confidential = false,
            required = true)
    public String getIdentifierProperty() {
        return identifierProperty;
    }

    public void setIdentifierProperty(String identifierProperty) {
        this.identifierProperty = identifierProperty;
    }

    @ConfigurationProperty(
            displayMessageKey = "Group Column Name",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Group column name, if file does not include a header this would be colX",
            order = 5,
            confidential = false,
            required = true)
    public String getGroupIdentifierProperty() {
        return groupIdentifierProperty;
    }

    public void setGroupIdentifierProperty(String groupIdentifierProperty) {
        this.groupIdentifierProperty = groupIdentifierProperty;
    }


    @Override
    public void validate() {
        if (StringUtil.isBlank(directoryPathProperty + fileNameProperty)) {
            throw new ConfigurationException("Properties must not be blank!");
        }

        File file = new File(getFilePath());
        if (!file.canRead()
        ) {
            throw new ConfigurationException("Cannot read connector file specified");
        }
    }

}
