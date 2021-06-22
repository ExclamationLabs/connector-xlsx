package com.exclamationlabs.connid.xlsx;

import org.identityconnectors.common.logging.Log;
import org.apache.poi.ss.usermodel.*;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.ResultsHandler;

import java.io.FileInputStream;
import java.io.IOException;

import java.util.*;
import java.util.stream.Collectors;


public class ReadHandler {

    private static final Log LOG = Log.getLog(ReadHandler.class);

    private Workbook wb;

    private String filePath;

    public ReadHandler(String filePath) throws IOException {
        this.filePath = filePath;
        LOG.info("Loading XLSX: {0}", this.filePath);

        FileInputStream inp = new FileInputStream(this.filePath);
        this.wb = WorkbookFactory.create(inp);
    }

    public void closeReader() throws IOException {
        this.wb.close();
    }

    public void openReader() throws IOException {
        FileInputStream inp = new FileInputStream(this.filePath);
        this.wb = WorkbookFactory.create(inp);
    }

    public void getAccounts(Configuration configuration, ResultsHandler handler) {
        DataFormatter dataFormatter = new DataFormatter();
        Sheet sheet = this.wb.getSheetAt(0);
        Cell cell;
        String cellValue;
        HashMap<String, Account> accounts = new HashMap<>();
        Account account = new Account();
        String[] heading = getHeading(configuration.getIncludesHeaderProperty());
        int identifierColumn = -1;
        int ignoreColumn = -1;
        List<Integer> mergeColumns = new ArrayList<>();
        Iterator<Row> rowIterator = sheet.rowIterator();

        LOG.info("Begin Parsing");

        if (!configuration.getIncludesHeaderProperty()) {
            identifierColumn = Integer.parseInt(configuration.getIdentifierProperty().substring(3));
            ignoreColumn = Integer.parseInt(configuration.getIgnoreProperty().substring(3));
            mergeColumns = getMergeColumnIndex(configuration);
            LOG.info("{0}",identifierColumn);
            LOG.info("{0}",mergeColumns);
        }
        LOG.info(String.valueOf(rowIterator.hasNext()));
        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();

            // Find Identifier, first iteration
            if(row.getRowNum() == 0 && configuration.getIncludesHeaderProperty()) {
                while(cellIterator.hasNext()) {
                    cell = cellIterator.next();
                    cellValue = dataFormatter.formatCellValue(cell);
                    if (cellValue.equals(configuration.getIdentifierProperty())) {
                        identifierColumn = cell.getColumnIndex();
                    } else if (configuration.getMergeProperty() != null && configuration.getMergeProperty().contains(cellValue)) {
                        mergeColumns.add(cell.getColumnIndex());
                    } else if (configuration.getIgnoreProperty() != null && configuration.getIgnoreProperty().contains(cellValue)) {
                        ignoreColumn = cell.getColumnIndex();
                    }
                }
            }else {
                String identifier = dataFormatter.formatCellValue(row.getCell(identifierColumn));
                String ignore = (ignoreColumn != -1)?dataFormatter.formatCellValue(row.getCell(ignoreColumn)):"";
                if (identifier.isEmpty() || ignore.equals(configuration.getIgnoreValueProperty())){
                    //continue on null input and ignored row value
                    continue;
                }
                //account already exists, get the merge and move on
                if (accounts.containsKey(identifier)) {
                    account = accounts.get(identifier);
                    for(int col : mergeColumns){
                        account.addAttribute(heading[col], dataFormatter.formatCellValue(row.getCell(col)));
                    }
                } else {

                    if(account.getIdentifier() != null && configuration.isUidSortedProperty()){
                        handler.handle(
                            getAccountConnectorObjectFromAccount(account)
                        );
                    }
                    //new account
                    account = new Account();
                    while (cellIterator.hasNext()) {
                        cell = cellIterator.next();
                        cellValue = dataFormatter.formatCellValue(cell);
                        if (cell.getColumnIndex() == identifierColumn) {
                            account.setIdentifier(cellValue);
                        } else if(!cellValue.isEmpty()) {
                            account.addAttribute(heading[cell.getColumnIndex()], cellValue);
                        }
                    }
                }
                accounts.put(account.getIdentifier(), account);
            }
        }
        LOG.info("End Parsing");
        if(configuration.isUidSortedProperty()) {
            handler.handle(
                getAccountConnectorObjectFromAccount(account)
            );
        }else {
            for (Account acc : accounts.values()) {
                handler.handle(
                    getAccountConnectorObjectFromAccount(acc)
                );
            }
        }
    }

    private List<Integer> getMergeColumnIndex(Configuration configuration) {
        return Arrays.asList(configuration.getMergeProperty()
                .split(configuration.getMultivalueDelimiter())).stream()
                .map(c -> Integer.parseInt(c.substring(3)))
                .collect(Collectors.toList());
    }

    private ConnectorObject getAccountConnectorObjectFromAccount(Account account) {
        ConnectorObjectBuilder cob = new ConnectorObjectBuilder();
        cob.setName(account.getIdentifier());
        cob.setUid(account.getIdentifier());
        account.getAttributes().forEach(cob::addAttribute);

        return cob.build();
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
