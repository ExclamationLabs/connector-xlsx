/*
 * Copyright (c) 2019. Exclamation Labs https://www.exclamationlabs.com/
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
    private String mergeProperty = "";
    private String ignoreProperty;
    private String ignoreValueProperty;
    private String multivalueDelimiter = ";";
    private Boolean includesHeaderProperty = false;
    private Boolean uidSortedProperty = false;

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
            displayMessageKey = "Merge Property",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Properties to be merged if an account is listed multiple times, if file does not include a header this would be colX",
            order = 5,
            confidential = false,
            required = false)
    public String getMergeProperty() {
        return mergeProperty;
    }

    public void setMergeProperty(String mergeProperty) {
        this.mergeProperty = mergeProperty;
    }

    @ConfigurationProperty(
            displayMessageKey = "Uid Sorted",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "If items are sorted by Uid they will be handled individually, Default false",
            order = 6)
    public boolean isUidSortedProperty() {
        return uidSortedProperty;
    }

    public void setUidSortedProperty(boolean uidSortedProperty) {
        this.uidSortedProperty = uidSortedProperty;
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

    @ConfigurationProperty(
            displayMessageKey = "Multivalue Delimiter",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Character used to split multivalue attributes, default ;",
            order = 7,
            confidential = false,
            required = false)
    public String getMultivalueDelimiter() {
        return multivalueDelimiter;
    }

    public void setMultivalueDelimiter(String multivalueDelimiter) {
        this.multivalueDelimiter = multivalueDelimiter;
    }

    @ConfigurationProperty(
            displayMessageKey = "Ignore Column",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Set to Row name to exclude based on value",
            order = 8,
            confidential = false,
            required = false)
    public String getIgnoreProperty() {
        return ignoreProperty;
    }

    public void setIgnoreProperty(String ignoreProperty) {
        this.ignoreProperty = ignoreProperty;
    }

    @ConfigurationProperty(
            displayMessageKey = "Ignore Column Value",
            groupMessageKey ="XLSX Configuration",
            helpMessageKey = "Report value to ignore column",
            order = 9,
            confidential = false,
            required = false)
    public String getIgnoreValueProperty() {
        return ignoreValueProperty;
    }

    public void setIgnoreValueProperty(String ignoreValueProperty) {
        this.ignoreValueProperty = ignoreValueProperty;
    }
}
