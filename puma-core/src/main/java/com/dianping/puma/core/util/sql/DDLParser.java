package com.dianping.puma.core.util.sql;

import com.dianping.puma.core.util.PatternUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;

public class DDLParser {

	// DDL statement patterns.
	private static final String ALTER_PATTERN = "^\\s*ALTER(.*)$";
	private static final String CREATE_PATTERN = "^\\s*CREATE(.*)$";
	private static final String DROP_PATTERN = "^\\s*DROP(.*)$";
	private static final String RENAME_PATTERN = "^\\s*RENAME(.*)$";
	private static final String TRUNCATE_PATTERN = "^\\s*RENAME(.*)$";

	// DDL statement database and table pattern.
	private static final String SCHEMA_TABLE_PATTERN = "^(IF\\s*NOT\\s*EXISTS\\s*)?(IF\\s*EXISTS\\s*)?(`?.+?`?[;\\(\\s]+?)?.*$";

	// DDL statement pattern components.
	private static final String ALGORITHM_PATTERN = "(ALGORITHM\\s*=\\s*(UNDEFINED|MERGE|TEMPTABLE))?";
	private static final String DEFINER_PATTERN = "(DEFINER\\s*=\\s*([^\\s]+|CURRENT_USER))?";
	private static final String SQL_SECURITY_PATTERN = "(SQL\\s*SECURITY\\s*(DEFINED|INVOKER))?";

	// DDL alter statement pattern.
	private static final String ALTER_DATABASE_PATTERN        = "^\\s*ALTER\\s*(DATABASE|SCHEMA)?\\s*(.*)$";
	private static final String ALTER_LOGFILE_GROUP_PATTERN   = "^\\s*ALTER\\*sLOGFILE\\*sGROUP\\s*(.*)$";
	private static final String ALTER_FUNCTION_PATTERN        = "^\\s*ALTER\\s*FUNCTION\\s*(.*)$";
	private static final String ALTER_PROCEDURE_PATTERN       = "^\\s*ALTER\\s*PROCEDURE\\s*(.*)$";
	private static final String ALTER_SERVER_PATTERN          = "^\\s*ALTER\\s*SERVER\\s*(.*)$";
	private static final String ALTER_TABLE_PATTERN           = "^\\s*ALTER\\*s(ONLINE|OFFLINE)\\s*(IGNORE)\\s*TABLE\\s*(.*)$";
	private static final String ALTER_TABLE_NAMESPACE_PATTERN = "^\\s*ALTER\\*sTABLESPACE\\s*(.*)$";
	private static final String ALTER_VIEW_PATTERN            = "^\\s*ALTER" + "\\s*" + ALGORITHM_PATTERN + "\\s*" + DEFINER_PATTERN + "\\s*" + SQL_SECURITY_PATTERN + "\\s*VIEW//s*(.*)$";

	// DDL create statement patterns.
	private static final String CREATE_TABLE_PATTERN = "^\\s*CREATE\\s*(TEMPORARY)?\\s*TABLE\\s*(.*)$";

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

	private static DDLResult parseDDLAlter(String queryString) {
		PatternMatcher matcher = new Perl5Matcher();

		// alter database.
		if (parseDDL(matcher, queryString, ALTER_DATABASE_PATTERN)) {
			String name = parseDBName(matcher.getMatch().group(2));
			return new DDLResult(DDLType.ALTER_DATABASE, name, null);
		}

		// alter logfile group.
		if (parseDDL(matcher, queryString, ALTER_LOGFILE_GROUP_PATTERN)) {
			return new DDLResult(DDLType.ALTER_LOGFILE_GROUP);
		}

		// alter function.
		if (parseDDL(matcher, queryString, ALTER_FUNCTION_PATTERN)) {
			return new DDLResult(DDLType.ALTER_FUNCTION);
		}

		// alter procedure.
		if (parseDDL(matcher, queryString, ALTER_PROCEDURE_PATTERN)) {
			return new DDLResult(DDLType.ALTER_PROCEDURE);
		}

		// alter server.
		if (parseDDL(matcher, queryString, ALTER_SERVER_PATTERN)) {
			return new DDLResult(DDLType.ALTER_SERVER);
		}

		// alter table.
		if (parseDDL(matcher, queryString, ALTER_TABLE_PATTERN)) {
			String names[] = parseDBTBName(matcher.getMatch().group(4));
			return new DDLResult(DDLType.ALTER_TABLE, names[0], names[1]);
		}

		// alter table namespace.
		if (parseDDL(matcher, queryString, ALTER_TABLE_NAMESPACE_PATTERN)) {
			return new DDLResult(DDLType.ALTER_TABLESPACE);
		}

		// alter view.
		if (parseDDL(matcher, queryString, ALTER_VIEW_PATTERN)) {
			return new DDLResult(DDLType.ALTER_VIEW);
		}

		return null;
	}

	private static DDLResult parseDDLCreate(String queryString) {
		return null;
	}

	private static DDLResult parseDDLDrop(String queryString) {
		return null;
	}

	private static DDLResult parseDDLRename(String queryString) {
		return null;
	}

	private static DDLResult parseDDLTruncate(String queryString) {
		return null;
	}

	private static boolean parseDDL(PatternMatcher matcher, String queryString, String queryPattern) {
		return matcher.matches(queryString, PatternUtils.getPattern(queryPattern));
	}

	// Returns database name, if not exists, return null.
	private static String parseDBName(String nameString) {
		String dbName = null;
		String name = parseName(nameString);
		if (name != null) {
			dbName = removeEscape(name);
		}
		return dbName;
	}

	// Returns database and table names, if not exists, return null.
	private static String[] parseDBTBName(String nameString) {
		String dbtbNames[] = {null, null};
		String name = parseName(nameString);
		if (name != null) {
			String names[] = StringUtils.split(name, ".");
			if (names != null) {
				if (names.length == 1) {
					dbtbNames[0] = removeEscape(names[0]);
				} else if (names.length >= 2) {
					dbtbNames[0] = removeEscape(names[0]);
					dbtbNames[1] = removeEscape(names[1]);
				}
			}
		}
		return dbtbNames;
	}

	private static String parseName(String nameString) {
		PatternMatcher matcher = new Perl5Matcher();
		if (matcher.matches(nameString, PatternUtils.getPattern(SCHEMA_TABLE_PATTERN))) {
			String name = matcher.getMatch().group(3);
			name = StringUtils.removeEnd(name, ";");
			name = StringUtils.removeEnd(name, "(");
			name = StringUtils.trim(name);
			return name;
		}
		return null;
	}

	private static String removeEscape(String str) {
		String result = StringUtils.removeEnd(str, "`");
		result = StringUtils.removeStart(result, "`");
		return result;
	}
}
