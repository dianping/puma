package com.dianping.puma.core.util.sql;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class DDLParserTest {

	@Test
	public void parseDDLAlterTest() {
		String queryString;
		DDLResult expected;
		DDLResult result;

		// alter database.
		queryString = "ALTER DATABASE `DianPing` UPGRADE DATA DIRECTORY NAME";
		expected = new DDLResult(DDLType.ALTER_DATABASE, "DianPing", null);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter logfile group.
		queryString = "ALTER LOGFILE GROUP lg_3 ADD UNDOFILE 'undo_10.dat' INITIAL_SIZE=32M ENGINE=NDBCLUSTER;";
		expected = new DDLResult(DDLType.ALTER_LOGFILE_GROUP);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter function.
		queryString = "ALTER FUNCTION add SQL SECURITY INVOKER;";
		expected = new DDLResult(DDLType.ALTER_FUNCTION);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter procedure.
		queryString = "ALTER PROCEDURE procedure_name LANGUAGE SQL;";
		expected = new DDLResult(DDLType.ALTER_PROCEDURE);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter server.
		queryString = "ALTER SERVER server_name OPTIONS (USER 'user_name');";
		expected = new DDLResult(DDLType.ALTER_SERVER);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter table.
		queryString = "ALTER OFFLINE TABLE `schema_name`.`table_name` CHANGE c1 c1 TEXT CHARACTER SET utf8;";
		expected = new DDLResult(DDLType.ALTER_TABLE, "schema_name", "table_name");
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter tablespace.
		queryString = "ALTER TABLESPACE tablespace_name ADD DATAFILE `datafile_name`;";
		expected = new DDLResult(DDLType.ALTER_TABLESPACE);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));

		// alter view.
		queryString = "ALTER VIEW name;";
		expected = new DDLResult(DDLType.ALTER_VIEW);
		result = DDLParser.parse(queryString);
		Assert.assertTrue(EqualsBuilder.reflectionEquals(expected, result));
	}

	@Test
	public void testReplaceDdl() throws InvocationTargetException, IllegalAccessException {
		String sql = "ALTER TABLE Persons ALTER COLUMN DateOfBirth year";
		String expected = "ALTER TABLE `Puma`.`test` ALTER COLUMN DateOfBirth year";
		String result = DDLParser.replaceDdl(sql, "Puma", "test", DDLType.ALTER_TABLE);
		Assert.assertEquals(expected, result);
	}
}
