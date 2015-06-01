package com.dianping.puma.core.util.sql;

import com.dianping.puma.core.util.PatternUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

public class DDLParser {

	// DDL statements.
	private static final String ALTER_PATTERN    = "^\\s*ALTER(.*)$";
	private static final String CREATE_PATTERN   = "^\\s*CREATE(.*)$";
	private static final String DROP_PATTERN     = "^\\s*DROP(.*)$";
	private static final String RENAME_PATTERN   = "^\\s*RENAME(.*)$";
	private static final String TRUNCATE_PATTERN = "^\\s*RENAME(.*)$";

	// DDL statement begin and end.
	private static final String BEGIN         = "^";
	private static final String END           = "$";

	// ALTER, CREATE, DROP, RENAME, TRUNCATE.
	private static final String ALTER         = "(\\s*ALTER){1}";
	private static final String CREATE        = "(\\s*CREATE){1}";
	private static final String DROP          = "(\\s*DROP){1}";
	private static final String RENAME        = "(\\s*RENAME){1}";
	private static final String TRUNCATE      = "(\\s*TRUNCATE){1}";

	// DATABASE, TABLE, etc.
	private static final String DATABASE      = "(\\s+DATABASE|\\s+SCHEMA){1}";
	private static final String EVENT         = "(\\s+EVENT){1}";
	private static final String FUNCTION      = "(\\s+FUNCTION){1}";
	private static final String INDEX         = "(\\s+INDEX){1}";
	private static final String LOGFILE_GROUP = "(\\s+LOGFILE\\s+GROUP){1}";
	private static final String PROCEDURE     = "(\\s+PROCEDURE){1}";
	private static final String SERVER        = "(\\s+SERVER){1}";
	private static final String TABLE         = "(\\s+TABLE){1}";
	private static final String OMITTED_TABLE = "(\\s+TABLE){0,1}";
	private static final String TABLESPACE    = "(\\s+TABLESPACE){1}";
	private static final String TRIGGER       = "(\\s+TRIGGER){1}";
	private static final String VIEW          = "(\\s+VIEW){1}";

	// DDL statement qualifiers.
	private static final String IF_EXISTS     = "(\\s+IF\\s+EXISTS)?";
	private static final String IF_NOT_EXIST  = "(\\s+IF\\s+NOT\\s+EXIST)?";
	private static final String TEMPORARY     = "(\\s+TEMPORARY)?";
	private static final String DEFINER       = "(\\s+DEFINER\\s+=\\s+\\S+|\\s+DEFINER\\s+=\\s+CURRENT_USER)?";
	private static final String ONLINE        = "(\\s+ONLINE|\\s+OFFLINE)?";
	private static final String UNIQUE        = "(\\s+UNIQUE|\\s+FULLTEXT|\\s+SPATIAL)?";
	private static final String OR_REPLACE    = "(\\s+OR\\s+REPLACE)?";
	private static final String ALGORITHM     = "(\\s+ALGORITHM\\s+=\\s+UNDEFINED|\\s+ALGORITHM\\s+=\\s+MERGE|\\s+ALGORITHM\\s+=\\s+TEMPTABLE)?";
	private static final String SQL_SECURITY  = "(\\s+SQL\\s+SECURITY\\s+DEFINER|\\s+SQL\\s+SECURITY\\s+INVOKER)?";
	private static final String IGNORE        = "(\\s+IGNORE)?";

	// DDL statement names to extract.
	private static final String NAME          = "(\\s+[^;(\\s]+){1}";
	private static final String OMITTED_NAME  = "(\\s+[^;(\\s]+){0,1}";
	private static final String TO            = "(\\s+TO){1}";

	// DDL statement specification.
	private static final String SPECIFICATION = "(\\s+.+)?";

	// DDL statement semicolon.
	private static final String SEMICOLON     = "(;){0,1}";

	// DDL alter statements.
	private static final String ALTER_DATABASE_PATTERN       = BEGIN + ALTER + DATABASE + OMITTED_NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_EVENT_PATTERN          = BEGIN + ALTER + DEFINER + EVENT + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_LOGFILE_GROUP_PATTERN  = BEGIN + ALTER + LOGFILE_GROUP + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_FUNCTION_PATTERN       = BEGIN + ALTER + FUNCTION + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_PROCEDURE_PATTERN      = BEGIN + ALTER + PROCEDURE + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_SERVER_PATTERN         = BEGIN + ALTER + SERVER + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_TABLE_PATTERN          = BEGIN + ALTER + ONLINE + IGNORE + TABLE + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_TABLESPACE_PATTERN     = BEGIN + ALTER + TABLESPACE + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String ALTER_VIEW_PATTERN           = BEGIN + ALTER + ALGORITHM + DEFINER + SQL_SECURITY + VIEW + NAME + SPECIFICATION + SEMICOLON + END;

	// DDL create statements.
	private static final String CREATE_DATABASE_PATTERN      = BEGIN + CREATE + DATABASE + IF_NOT_EXIST + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_EVENT_PATTERN         = BEGIN + CREATE + DEFINER + EVENT + IF_NOT_EXIST + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_FUNCTION_PATTERN      = BEGIN + CREATE + DEFINER + FUNCTION + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_INDEX_PATTERN         = BEGIN + CREATE + ONLINE + UNIQUE + INDEX + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_LOGFILE_GROUP_PATTERN = BEGIN + CREATE + LOGFILE_GROUP + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_PROCEDURE_PATTERN     = BEGIN + CREATE + DEFINER + PROCEDURE + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_SERVER_PATTERN        = BEGIN + CREATE + SERVER + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_TABLE_PATTERN         = BEGIN + CREATE + TEMPORARY + TABLE + IF_NOT_EXIST + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_TABLESPACE_PATTERN    = BEGIN + CREATE + TABLESPACE + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_TRIGGER_PATTERN       = BEGIN + CREATE + DEFINER + TRIGGER + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String CREATE_VIEW_PATTERN          = BEGIN + CREATE + OR_REPLACE + ALGORITHM + DEFINER + SQL_SECURITY + VIEW + NAME + SPECIFICATION + SEMICOLON + END;

	// DDL drop statements.
	private static final String DROP_DATABASE_PATTERN        = BEGIN + DROP + DATABASE + IF_EXISTS + NAME + SEMICOLON + END;
	private static final String DROP_EVENT_PATTERN           = BEGIN + DROP + EVENT + IF_EXISTS + NAME + SEMICOLON + END;
	private static final String DROP_FUNCTION_PATTERN        = BEGIN + DROP + FUNCTION + IF_EXISTS + NAME + SEMICOLON + END;
	private static final String DROP_INDEX_PATTERN           = BEGIN + DROP + ONLINE + INDEX + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String DROP_LOGFILE_GROUP_PATTERN   = BEGIN + DROP + LOGFILE_GROUP + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String DROP_PROCEDURE_PATTERN       = BEGIN + DROP + PROCEDURE + IF_EXISTS + NAME + SEMICOLON + END;
	private static final String DROP_SERVER_PATTERN          = BEGIN + DROP + SERVER + IF_EXISTS + NAME + SEMICOLON + END;
	private static final String DROP_TABLE_PATTERN           = BEGIN + DROP + TEMPORARY + TABLE + IF_EXISTS + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String DROP_TABLESPACE_PATTERN      = BEGIN + DROP + TABLESPACE + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String DROP_TRIGGER_PATTERN         = BEGIN + DROP + TRIGGER + IF_EXISTS + NAME + SPECIFICATION + SEMICOLON + END;
	private static final String DROP_VIEW_PATTERN            = BEGIN + DROP + VIEW + IF_EXISTS + NAME + SPECIFICATION + SEMICOLON + END;

	// DDL rename statements.
	private static final String RENAME_DATABASE_PATTERN      = BEGIN + RENAME + DATABASE + NAME + TO + NAME + SEMICOLON + END;
	private static final String RENAME_TABLE_PATTERN         = BEGIN + RENAME + TABLE + NAME + TO + NAME + SPECIFICATION + SEMICOLON + END;

	// DDL truncate statements.
	private static final String TRUNCATE_TABLE_PATTERN       = BEGIN + TRUNCATE + OMITTED_TABLE + NAME + SEMICOLON + END;


	public static DDLResult parse(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		if (matcher.matches(queryString, PatternUtils.getPattern(ALTER_PATTERN))) {
			return parseDDLAlter(queryString);
		}

		if (matcher.matches(queryString, PatternUtils.getPattern(CREATE_PATTERN))) {
			return parseDDLCreate(queryString);
		}

		if (matcher.matches(queryString, PatternUtils.getPattern(DROP_PATTERN))) {
			return parseDDLDrop(queryString);
		}

		if (matcher.matches(queryString, PatternUtils.getPattern(RENAME_PATTERN))) {
			return parseDDLRename(queryString);
		}

		if (matcher.matches(queryString, PatternUtils.getPattern(TRUNCATE_PATTERN))) {
			return parseDDLTruncate(queryString);
		}

		return null;
	}

	public static String replaceDdl(String queryString, String schema, String table, DDLType ddlType) {
		PatternMatcher matcher = new Perl5Matcher();
		switch (ddlType) {
		case ALTER_TABLE:
			parseDDL(matcher, queryString, ALTER_TABLE_PATTERN);
			String name = matcher.getMatch().group(5);
			return queryString.replace(name, " " + "`" + schema + "`" + "." + "`" + table + "`");
		default:
			return null;
		}
	}

	// Parse ddl alter statements.
	private static DDLResult parseDDLAlter(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		// alter database statements.
		if (parseDDL(matcher, queryString, ALTER_DATABASE_PATTERN)) {
			String name = parseDBName(matcher.getMatch().group(3));
			return new DDLResult(DDLType.ALTER_DATABASE, name, null);
		}

		// alter event statements.
		if (parseDDL(matcher, queryString, ALTER_EVENT_PATTERN)) {
			return new DDLResult(DDLType.ALTER_EVENT);
		}

		// alter logfile group statements.
		if (parseDDL(matcher, queryString, ALTER_LOGFILE_GROUP_PATTERN)) {
			return new DDLResult(DDLType.ALTER_LOGFILE_GROUP);
		}

		// alter function statements.
		if (parseDDL(matcher, queryString, ALTER_FUNCTION_PATTERN)) {
			return new DDLResult(DDLType.ALTER_FUNCTION);
		}

		// alter procedure statements.
		if (parseDDL(matcher, queryString, ALTER_PROCEDURE_PATTERN)) {
			return new DDLResult(DDLType.ALTER_PROCEDURE);
		}

		// alter server statements.
		if (parseDDL(matcher, queryString, ALTER_SERVER_PATTERN)) {
			return new DDLResult(DDLType.ALTER_SERVER);
		}

		// alter table statements.
		if (parseDDL(matcher, queryString, ALTER_TABLE_PATTERN)) {
			String names[] = parseDBTBName(matcher.getMatch().group(5));
			return new DDLResult(DDLType.ALTER_TABLE, names[0], names[1]);
		}

		// alter tablespace statements.
		if (parseDDL(matcher, queryString, ALTER_TABLESPACE_PATTERN)) {
			return new DDLResult(DDLType.ALTER_TABLESPACE);
		}

		// alter view statements.
		if (parseDDL(matcher, queryString, ALTER_VIEW_PATTERN)) {
			return new DDLResult(DDLType.ALTER_VIEW);
		}

		return null;
	}

	private static DDLResult parseDDLCreate(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		// create database statements.
		if (parseDDL(matcher, queryString, CREATE_DATABASE_PATTERN)) {
			String name = parseDBName(matcher.getMatch().group(4));
			return new DDLResult(DDLType.CREATE_DATABASE, name, null);
		}

		// create event statements.
		if (parseDDL(matcher, queryString, CREATE_EVENT_PATTERN)) {
			return new DDLResult(DDLType.CREATE_EVENT);
		}

		// create function statements.
		if (parseDDL(matcher, queryString, CREATE_FUNCTION_PATTERN)) {
			return new DDLResult(DDLType.CREATE_FUNCTION);
		}

		// create index statements.
		if (parseDDL(matcher, queryString, CREATE_INDEX_PATTERN)) {
			return new DDLResult(DDLType.CREATE_INDEX);
		}

		// create logfile group statements.
		if (parseDDL(matcher, queryString, CREATE_LOGFILE_GROUP_PATTERN)) {
			return new DDLResult(DDLType.CREATE_LOGFILE_GROUP);
		}

		// create procedure statements.
		if (parseDDL(matcher, queryString, CREATE_PROCEDURE_PATTERN)) {
			return new DDLResult(DDLType.CREATE_PROCEDURE);
		}

		// create server statements.
		if (parseDDL(matcher, queryString, CREATE_SERVER_PATTERN)) {
			return new DDLResult(DDLType.CREATE_SERVER);
		}

		// create table statements.
		if (parseDDL(matcher, queryString, CREATE_TABLE_PATTERN)) {
			String names[] = parseDBTBName(matcher.getMatch().group(5));
			return new DDLResult(DDLType.CREATE_TABLE, names[0], names[1]);
		}

		// create tablespace statements.
		if (parseDDL(matcher, queryString, CREATE_TABLESPACE_PATTERN)) {
			return new DDLResult(DDLType.CREATE_TABLESPACE);
		}

		// create trigger statements.
		if (parseDDL(matcher, queryString, CREATE_TRIGGER_PATTERN)) {
			return new DDLResult(DDLType.CREATE_TRIGGER);
		}

		// create view statements.
		if (parseDDL(matcher, queryString, CREATE_VIEW_PATTERN)) {
			return new DDLResult(DDLType.CREATE_VIEW);
		}

		return null;
	}

	private static DDLResult parseDDLDrop(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		// drop database statements.
		if (parseDDL(matcher, queryString, DROP_DATABASE_PATTERN)) {
			String name = parseDBName(matcher.getMatch().group(4));
			return new DDLResult(DDLType.DROP_DATABASE, name, null);
		}

		// drop event statements.
		if (parseDDL(matcher, queryString, DROP_EVENT_PATTERN)) {
			return new DDLResult(DDLType.DROP_EVENT);
		}

		// drop function statements.
		if (parseDDL(matcher, queryString, DROP_FUNCTION_PATTERN)) {
			return new DDLResult(DDLType.DROP_FUNCTION);
		}

		// drop index statements.
		if (parseDDL(matcher, queryString, DROP_INDEX_PATTERN)) {
			return new DDLResult(DDLType.DROP_INDEX);
		}

		// drop logfile group statements.
		if (parseDDL(matcher, queryString, DROP_LOGFILE_GROUP_PATTERN)) {
			return new DDLResult(DDLType.DROP_LOGFILE_GROUP);
		}

		// drop procedure statements.
		if (parseDDL(matcher, queryString, DROP_PROCEDURE_PATTERN)) {
			return new DDLResult(DDLType.DROP_PROCEDURE);
		}

		// drop server statements.
		if (parseDDL(matcher, queryString, DROP_SERVER_PATTERN)) {
			return new DDLResult(DDLType.DROP_SERVER);
		}

		// drop table statements.
		if (parseDDL(matcher, queryString, DROP_TABLE_PATTERN)) {
			String names[] = parseDBTBName(matcher.getMatch().group(5));
			return new DDLResult(DDLType.DROP_TABLE, names[0], names[1]);
		}

		// drop tablespace statements.
		if (parseDDL(matcher, queryString, DROP_TABLESPACE_PATTERN)) {
			return new DDLResult(DDLType.DROP_TABLESPACE);
		}

		// drop trigger statements.
		if (parseDDL(matcher, queryString, DROP_TRIGGER_PATTERN)) {
			return new DDLResult(DDLType.DROP_TRIGGER);
		}

		// drop view statements.
		if (parseDDL(matcher, queryString, DROP_VIEW_PATTERN)) {
			return new DDLResult(DDLType.DROP_VIEW);
		}

		return null;
	}

	private static DDLResult parseDDLRename(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		// rename database statements.
		if (parseDDL(matcher, queryString, RENAME_DATABASE_PATTERN)) {
			String name = parseDBName(matcher.getMatch().group(3));
			String oriName = parseDBName(matcher.getMatch().group(5));
			return new DDLResult(DDLType.RENAME_DATABASE, name, null, oriName, null);
		}

		// rename table statements.
		if (parseDDL(matcher, queryString, RENAME_TABLE_PATTERN)) {
			String names[] = parseDBTBName(matcher.getMatch().group(3));
			String oriNames[] = parseDBTBName(matcher.getMatch().group(5));
			return new DDLResult(DDLType.RENAME_TABLE, names[0], names[1], oriNames[0], oriNames[1]);
		}

		return null;
	}

	private static DDLResult parseDDLTruncate(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		// truncate table statements.
		if (parseDDL(matcher, queryString, TRUNCATE_TABLE_PATTERN)) {
			String names[] = parseDBTBName(matcher.getMatch().group(3));
			return new DDLResult(DDLType.TRUNCATE_TABLE, names[0], names[1]);
		}

		return null;
	}

	private static boolean parseDDL(PatternMatcher matcher, String queryString, String queryPattern) {
		return matcher.matches(queryString, PatternUtils.getPattern(queryPattern));
	}

	private static String parseDBName(String nameString) {
		if (!StringUtils.isBlank(nameString)) {
			return normalizeName(nameString);
		}
		return null;
	}

	private static String[] parseDBTBName(String nameString) {
		if (!StringUtils.isBlank(nameString)) {
			String names[] = StringUtils.split(nameString, ".");
			if (names.length == 1) {
				return new String[]{null, normalizeName(nameString)};
			} else {
				return new String[]{normalizeName(names[0]), normalizeName(names[1])};
			}
		}

		return new String[]{null, null};
	}

	private static String normalizeName(String nameString) {
		String name = StringUtils.normalizeSpace(nameString);
		name = StringUtils.removeEnd(name, "`");
		name = StringUtils.removeStart(name, "`");
		return name;
	}
}
