package com.mpos.lottery.te.test.util;

import net.mpos.core.util.SecUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSet;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.CompositeDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.OracleDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseLoader {
    private static Log logger = LogFactory.getLog(DatabaseLoader.class);
    private String jdbcPropFile = "jdbc.properties";
    private IDatabaseConnection dbConnection;

    public DatabaseLoader() throws Exception {
        Properties jdbcProp = this.loadJdbcProp(jdbcPropFile);

        Class.forName(jdbcProp.getProperty("jdbc.driver"));
        Connection jdbcConnection = DriverManager.getConnection(jdbcProp.getProperty("jdbc.url"),
                SecUtil.decrypt(jdbcProp.getProperty("jdbc.user")),
                SecUtil.decrypt(jdbcProp.getProperty("jdbc.password")));
        /**
         * Must set schema to 'ramon', otherwise DBUnit will try to export many strange table, such as
         * 'DR$NUMBER_SEQUENCE', and throw out 'tabe or view doestn't exist' at final.
         */
        dbConnection = new DatabaseConnection(jdbcConnection, "ramon", true);
        dbConnection.getConfig().setProperty("http://www.dbunit.org/properties/datatypeFactory",
                new OracleDataTypeFactory());
        // dbConnection.getConfig().setProperty("http://www.dbunit.org/features/qualifiedTableNames",
        // true);
    }

    /**
     * Load data from multiple data files into database
     */
    public void load(String dataFileDir) throws Exception {
        File dir = new File(dataFileDir);
        String dataFiles[] = dir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }

        });

        IDataSet[] datasets = new IDataSet[dataFiles.length];
        for (int i = 0; i < dataFiles.length; i++) {
            IDataSet dataset = new FlatXmlDataSetBuilder().build(new File(dataFileDir, dataFiles[i]));
            datasets[i] = dataset;
            logger.info("Load data file " + dataFiles[i] + " successfully.");
        }
        CompositeDataSet composeDataSet = new CompositeDataSet(datasets);
        DatabaseOperation.CLEAN_INSERT.execute(dbConnection, composeDataSet); // Import your data
        dbConnection.close();

        logger.info("Load data files successfully.");
    }

    /**
     * Export the whole database in to flat XML data files(per data file per table).
     */
    public void export(String dataFileDir) throws Exception {
        // full database set, we can get all table names from this set
        DatabaseDataSet fullDataSet = new DatabaseDataSet(dbConnection, true);
        ITableIterator tableIt = fullDataSet.iterator();
        while (tableIt.next()) {
            ITable table = tableIt.getTable();
            if (table.getRowCount() > 0) {
                String tableName = table.getTableMetaData().getTableName();
                String dataFileName = dataFileDir + tableName + ".xml";
                // write a single data file per table
                QueryDataSet partialDataSet = new QueryDataSet(dbConnection);
                partialDataSet.addTable(tableName);
                FlatXmlDataSet.write(partialDataSet, new FileOutputStream(dataFileName));
                logger.info("Write table '" + tableName + "' to data file(" + dataFileName + ") successfully.");
            }
        }
        logger.info("Export database successfully.");
        dbConnection.close();
    }

    public static void main(String args[]) throws Exception {
        DatabaseLoader dbLoader = new DatabaseLoader();
        // dbLoader.export("f:/tmp/dbunit/");
        dbLoader.load("f:/tmp/dbunit/");
    }

    /**
     * Open a input stream from classpath of filepath.
     */
    private Properties loadJdbcProp(String propFile) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            // support for jre1.3 or below
            cl = this.getClass().getClassLoader();
        }
        InputStream is = cl.getResourceAsStream(jdbcPropFile);

        Properties prop = new Properties();
        prop.load(is);

        // release resource
        is.close();

        return prop;
    }
}
