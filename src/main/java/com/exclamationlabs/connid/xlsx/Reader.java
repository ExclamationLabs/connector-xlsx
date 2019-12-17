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

import org.identityconnectors.common.logging.Log;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.*;


public class Reader {

    private static final Log LOG = Log.getLog(Reader.class);

    private Workbook wb;

    private String filePath;

    public Reader(String filePath) throws IOException {
        this.filePath = filePath;
        LOG.info("Loading XLSX: {0}", this.filePath);

        FileInputStream inp = new FileInputStream(this.filePath);
        this.wb = new XSSFWorkbook(inp);
    }

    public void closeReader() throws IOException {
        this.wb.close();
    }

    public void openReader() throws IOException {
        FileInputStream inp = new FileInputStream(this.filePath);
        this.wb = new XSSFWorkbook(inp);
    }

    public Collection<Account> getAccounts(Configuration configuration) {
        Map<String, Account> accounts = new HashMap<>();
        DataFormatter dataFormatter = new DataFormatter();

        LOG.info("Begin Parsing");

        Sheet sheet = this.wb.getSheetAt(0);
        Cell cell;
        String cellValue;
        Account account = new Account();

        String[] heading = getHeading(configuration.getIncludesHeaderProperty());
        int identifierColumn = -1;
        int groupColumn = -1;
        Iterator<Row> rowIterator = sheet.rowIterator();

        if (!configuration.getIncludesHeaderProperty()) {
            identifierColumn = Integer.parseInt(configuration.getIdentifierProperty().substring(3));
            groupColumn = Integer.parseInt(configuration.getGroupIdentifierProperty().substring(3));
            LOG.error("{0}",identifierColumn);
            LOG.error("{0}",groupColumn);
        }

        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();

            // Find Identifier, first iteration
            if(row.getRowNum() == 0 && configuration.getIncludesHeaderProperty()) {
                while(cellIterator.hasNext() && (identifierColumn == -1 || groupColumn == -1)) {
                    cell = cellIterator.next();
                    cellValue = dataFormatter.formatCellValue(cell);
                    if (cellValue.equals(configuration.getIdentifierProperty())) {
                        identifierColumn = cell.getColumnIndex();
                    } else if (cellValue.equals(configuration.getGroupIdentifierProperty())) {
                        groupColumn = cell.getColumnIndex();
                    }
                }
            } else {
                String identifier = dataFormatter.formatCellValue(row.getCell(identifierColumn));

                //account already exists, get the group and move on
                if (accounts.containsKey(identifier)) {
                    account = accounts.get(identifier);
                    account.addGroup(dataFormatter.formatCellValue(row.getCell(groupColumn)));
                } else {

                    //new account
                    while (cellIterator.hasNext()) {
                        cell = cellIterator.next();
                        cellValue = dataFormatter.formatCellValue(cell);
                        if (cell.getColumnIndex() == identifierColumn) {
                            account.setIdentifier(cellValue);
                        } else if (cell.getColumnIndex() == groupColumn) {
                            account.addGroup(cellValue);
                        } else {
                            account.put(heading[cell.getColumnIndex()], cellValue);
                        }
                    }
                }

                accounts.put(identifier, account);
                account = new Account();
            }
        }
        LOG.info("End Parsing");
        return accounts.values();
    }

    public String[] getHeading(Boolean includesHeader) {
        Sheet sheet = this.wb.getSheetAt(0);
        Iterator<Cell> cellIterator = sheet.getRow(0).cellIterator();
        String[] header = new String[sheet.getRow(0).getPhysicalNumberOfCells()];

        if(includesHeader) {
            for (int i = 0; cellIterator.hasNext(); i++) {
                header[i] = cellIterator.next().getRichStringCellValue().toString();
            }
        } else {
            for (int i = 0; cellIterator.hasNext(); i++) {
                header[i] = new StringBuilder("col").append(cellIterator.next().getColumnIndex()).toString();
            }
        }

        return header;
    }

    public int colCount() {
        return this.wb.getSheetAt(0)
                .getRow(0)
                .getPhysicalNumberOfCells();
    }
}
