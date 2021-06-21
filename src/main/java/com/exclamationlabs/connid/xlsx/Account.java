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

import java.util.*;

@SuppressWarnings("serial")
public class Account {

    private String identifier;
    private HashMap<String, List<Object>> attributes = new HashMap<>();

    public Account(){}

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public HashMap<String, List<Object>> getAttributes() {
        return attributes;
    }

    public void addAttribute(String attribute, String value) {
        List<Object> existingValue = this.attributes.get(attribute);
        if(existingValue == null){
            this.attributes.put(attribute, new ArrayList<>(Arrays.asList(value)));
        }else {
            existingValue.add(value.trim());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;

        Account that = (Account) o;
        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        return attributes != null ? attributes.equals(that.attributes) : that.attributes == null;
    }

    @Override
    public int hashCode() {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Account:" +
                "identifier='" + identifier + '\'' +
                ", attributes='" + attributes + '\'';
    }
}
