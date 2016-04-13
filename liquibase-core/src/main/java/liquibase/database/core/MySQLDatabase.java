package liquibase.database.core;

import java.math.BigInteger;
import java.util.*;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.PrimaryKey;
import liquibase.exception.DatabaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.RawSqlStatement;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractJdbcDatabase {
    public static final String PRODUCT_NAME = "MySQL";

    private static Set<String> reservedWords = new HashSet();

    public MySQLDatabase() {
        super.setCurrentDateTimeFunction("NOW()");
        // objects in mysql are always case sensitive
        super.quotingStartCharacter ="`";
        super.quotingEndCharacter="`";
    }

    @Override
    public String getShortName() {
        return "mysql";
    }


//todo: handle    @Override
//    public String getConnectionUsername() throws DatabaseException {
//        return super.getConnection().getConnectionUserName().replaceAll("\\@.*", "");
//    }

    @Override
    public String correctObjectName(String name, Class<? extends DatabaseObject> objectType) {
        if (objectType.equals(PrimaryKey.class) && name.equals("PRIMARY")) {
            return null;
        } else {
            name = super.correctObjectName(name, objectType);
            if (name == null) {
                return null;
            }
            if (!this.isCaseSensitive()) {
                return name.toLowerCase();
            }
            return name;
        }
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "MySQL";
    }

    @Override
    public Integer getDefaultPort() {
        return 3306;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return PRODUCT_NAME.equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return null;
    }


    @Override
    public boolean supportsSequences() {
        return false;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        return super.mustQuoteObjectName(objectName, objectType) || (!objectName.contains("(") && !objectName.matches("\\w+"));
    }

    @Override
    public String getLineComment() {
        return "-- ";
    }

    @Override
    protected String getAutoIncrementClause() {
        return "AUTO_INCREMENT";
    }

    @Override
    protected boolean generateAutoIncrementStartWith(final BigInteger startWith) {
    	// startWith not supported here. StartWith has to be set as table option.
        return false;
    }

    public String getTableOptionAutoIncrementStartWithClause(BigInteger startWith){
    	String startWithClause = String.format(getAutoIncrementStartWithClause(), (startWith == null) ? defaultAutoIncrementStartWith : startWith);
    	return getAutoIncrementClause() + startWithClause;
    }

    @Override
    protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
        // incrementBy not supported
        return false;
    }

    @Override
    protected String getAutoIncrementOpening() {
        return "";
    }

    @Override
    protected String getAutoIncrementClosing() {
        return "";
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
        return "=%d";
    }

    @Override
    public String getConcatSql(String... values) {
        StringBuffer returnString = new StringBuffer();
        returnString.append("CONCAT_WS(");
        for (String value : values) {
            returnString.append(value).append(", ");
        }

        return returnString.toString().replaceFirst(", $", ")");
    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        return escapeObjectName(indexName, Index.class);
    }

    @Override
    public boolean supportsForeignKeyDisable() {
        return true;
    }

    @Override
    public boolean disableForeignKeyChecks() throws DatabaseException {
        boolean enabled = ExecutorService.getInstance().getExecutor(this).queryForInt(new RawSqlStatement("SELECT @@FOREIGN_KEY_CHECKS")) == 1;
        ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("SET FOREIGN_KEY_CHECKS=0"));
        return enabled;
    }

    @Override
    public void enableForeignKeyChecks() throws DatabaseException {
        ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("SET FOREIGN_KEY_CHECKS=1"));
    }

    @Override
    public CatalogAndSchema getSchemaFromJdbcInfo(String rawCatalogName, String rawSchemaName) {
        return new CatalogAndSchema(rawCatalogName, null).customize(this);
    }

    @Override
    public String escapeStringForDatabase(String string) {
        string = super.escapeStringForDatabase(string);
        if (string == null) {
            return null;
        }
        return string.replace("\\", "\\\\");
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return true;
    }

    @Override
    public boolean isReservedWord(String string) {
        if (reservedWords.contains(string.toUpperCase())) {
            return true;
        }
        return super.isReservedWord(string);
    }

    public int getDatabasePatchVersion() throws DatabaseException {
        String versionStrings[] = this.getDatabaseProductVersion().split("\\.");
        try {
            return Integer.parseInt(versionStrings[2].replaceFirst("\\D.*", ""));
        } catch (NumberFormatException e) {
            return 0;
        }

    }

    {
        //list from http://dev.mysql.com/doc/refman/5.7/en/keywords.html
        reservedWords.addAll(Arrays.asList("ACCESSIBLE",
                "ACCOUNT",
                "ACTION",
                "ADD",
                "AFTER",
                "AGAINST",
                "AGGREGATE",
                "ALGORITHM",
                "ALL",
                "ALTER",
                "ALWAYS",
                "ANALYSE",
                "ANALYZE",
                "AND",
                "ANY",
                "AS",
                "ASC",
                "ASCII",
                "ASENSITIVE",
                "AT",
                "AUTOEXTEND_SIZE",
                "AUTO_INCREMENT",
                "AVG",
                "AVG_ROW_LENGTH",
                "BACKUP",
                "BEFORE",
                "BEGIN",
                "BETWEEN",
                "BIGINT",
                "BINARY",
                "BINLOG",
                "BIT",
                "BLOB",
                "BLOCK",
                "BOOL",
                "BOOLEAN",
                "BOTH",
                "BTREE",
                "BY",
                "BYTE",
                "CACHE",
                "CALL",
                "CASCADE",
                "CASCADED",
                "CASE",
                "CATALOG_NAME",
                "CHAIN",
                "CHANGE",
                "CHANGED",
                "CHANNEL",
                "CHAR",
                "CHARACTER",
                "CHARSET",
                "CHECK",
                "CHECKSUM",
                "CIPHER",
                "CLASS_ORIGIN",
                "CLIENT",
                "CLOSE",
                "COALESCE",
                "CODE",
                "COLLATE",
                "COLLATION",
                "COLUMN",
                "COLUMNS",
                "COLUMN_FORMAT",
                "COLUMN_NAME",
                "COMMENT",
                "COMMIT",
                "COMMITTED",
                "COMPACT",
                "COMPLETION",
                "COMPRESSED",
                "COMPRESSION",
                "CONCURRENT",
                "CONDITION",
                "CONNECTION",
                "CONSISTENT",
                "CONSTRAINT",
                "CONSTRAINT_CATALOG",
                "CONSTRAINT_NAME",
                "CONSTRAINT_SCHEMA",
                "CONTAINS",
                "CONTEXT",
                "CONTINUE",
                "CONVERT",
                "CPU",
                "CREATE",
                "CROSS",
                "CUBE",
                "CURRENT",
                "CURRENT_DATE",
                "CURRENT_TIME",
                "CURRENT_TIMESTAMP",
                "CURRENT_USER",
                "CURSOR",
                "CURSOR_NAME",
                "DATA",
                "DATABASE",
                "DATABASES",
                "DATAFILE",
                "DATE",
                "DATETIME",
                "DAY",
                "DAY_HOUR",
                "DAY_MICROSECOND",
                "DAY_MINUTE",
                "DAY_SECOND",
                "DEALLOCATE",
                "DEC",
                "DECIMAL",
                "DECLARE",
                "DEFAULT",
                "DEFAULT_AUTH",
                "DEFINER",
                "DELAYED",
                "DELAY_KEY_WRITE",
                "DELETE",
                "DESC",
                "DESCRIBE",
                "DES_KEY_FILE",
                "DETERMINISTIC",
                "DIAGNOSTICS",
                "DIRECTORY",
                "DISABLE",
                "DISCARD",
                "DISK",
                "DISTINCT",
                "DISTINCTROW",
                "DIV",
                "DO",
                "DOUBLE",
                "DROP",
                "DUAL",
                "DUMPFILE",
                "DUPLICATE",
                "DYNAMIC",
                "EACH",
                "ELSE",
                "ELSEIF",
                "ENABLE",
                "ENCLOSED",
                "ENCRYPTION",
                "END",
                "ENDS",
                "ENGINE",
                "ENGINES",
                "ENUM",
                "ERROR",
                "ERRORS",
                "ESCAPE",
                "ESCAPED",
                "EVENT",
                "EVENTS",
                "EVERY",
                "EXCHANGE",
                "EXECUTE",
                "EXISTS",
                "EXIT",
                "EXPANSION",
                "EXPIRE",
                "EXPLAIN",
                "EXPORT",
                "EXTENDED",
                "EXTENT_SIZE",
                "FALSE",
                "FAST",
                "FAULTS",
                "FETCH",
                "FIELDS",
                "FILE",
                "FILE_BLOCK_SIZE",
                "FILTER",
                "FIRST",
                "FIXED",
                "FLOAT",
                "FLOAT4",
                "FLOAT8",
                "FLUSH",
                "FOLLOWS",
                "FOR",
                "FORCE",
                "FOREIGN",
                "FORMAT",
                "FOUND",
                "FROM",
                "FULL",
                "FULLTEXT",
                "FUNCTION",
                "GENERAL",
                "GENERATED",
                "GEOMETRY",
                "GEOMETRYCOLLECTION",
                "GET",
                "GET_FORMAT",
                "GLOBAL",
                "GRANT",
                "GRANTS",
                "GROUP",
                "GROUP_REPLICATION",
                "HANDLER",
                "HASH",
                "HAVING",
                "HELP",
                "HIGH_PRIORITY",
                "HOST",
                "HOSTS",
                "HOUR",
                "HOUR_MICROSECOND",
                "HOUR_MINUTE",
                "HOUR_SECOND",
                "IDENTIFIED",
                "IF",
                "IGNORE",
                "IGNORE_SERVER_IDS",
                "IMPORT",
                "IN",
                "INDEX",
                "INDEXES",
                "INFILE",
                "INITIAL_SIZE",
                "INNER",
                "INOUT",
                "INSENSITIVE",
                "INSERT",
                "INSERT_METHOD",
                "INSTALL",
                "INSTANCE",
                "INT",
                "INT1",
                "INT2",
                "INT3",
                "INT4",
                "INT8",
                "INTEGER",
                "INTERVAL",
                "INTO",
                "INVOKER",
                "IO",
                "IO_AFTER_GTIDS",
                "IO_BEFORE_GTIDS",
                "IO_THREAD",
                "IPC",
                "IS",
                "ISOLATION",
                "ISSUER",
                "ITERATE",
                "JOIN",
                "JSON",
                "KEY",
                "KEYS",
                "KEY_BLOCK_SIZE",
                "KILL",
                "LANGUAGE",
                "LAST",
                "LEADING",
                "LEAVE",
                "LEAVES",
                "LEFT",
                "LESS",
                "LEVEL",
                "LIKE",
                "LIMIT",
                "LINEAR",
                "LINES",
                "LINESTRING",
                "LIST",
                "LOAD",
                "LOCAL",
                "LOCALTIME",
                "LOCALTIMESTAMP",
                "LOCK",
                "LOCKS",
                "LOGFILE",
                "LOGS",
                "LONG",
                "LONGBLOB",
                "LONGTEXT",
                "LOOP",
                "LOW_PRIORITY",
                "MASTER",
                "MASTER_AUTO_POSITION",
                "MASTER_BIND",
                "MASTER_CONNECT_RETRY",
                "MASTER_DELAY",
                "MASTER_HEARTBEAT_PERIOD",
                "MASTER_HOST",
                "MASTER_LOG_FILE",
                "MASTER_LOG_POS",
                "MASTER_PASSWORD",
                "MASTER_PORT",
                "MASTER_RETRY_COUNT",
                "MASTER_SERVER_ID",
                "MASTER_SSL",
                "MASTER_SSL_CA",
                "MASTER_SSL_CAPATH",
                "MASTER_SSL_CERT",
                "MASTER_SSL_CIPHER",
                "MASTER_SSL_CRL",
                "MASTER_SSL_CRLPATH",
                "MASTER_SSL_KEY",
                "MASTER_SSL_VERIFY_SERVER_CERT",
                "MASTER_TLS_VERSION",
                "MASTER_USER",
                "MATCH",
                "MAXVALUE",
                "MAX_CONNECTIONS_PER_HOUR",
                "MAX_QUERIES_PER_HOUR",
                "MAX_ROWS",
                "MAX_SIZE",
                "MAX_STATEMENT_TIME",
                "MAX_UPDATES_PER_HOUR",
                "MAX_USER_CONNECTIONS",
                "MEDIUM",
                "MEDIUMBLOB",
                "MEDIUMINT",
                "MEDIUMTEXT",
                "MEMORY",
                "MERGE",
                "MESSAGE_TEXT",
                "MICROSECOND",
                "MIDDLEINT",
                "MIGRATE",
                "MINUTE",
                "MINUTE_MICROSECOND",
                "MINUTE_SECOND",
                "MIN_ROWS",
                "MOD",
                "MODE",
                "MODIFIES",
                "MODIFY",
                "MONTH",
                "MULTILINESTRING",
                "MULTIPOINT",
                "MULTIPOLYGON",
                "MUTEX",
                "MYSQL_ERRNO",
                "NAME",
                "NAMES",
                "NATIONAL",
                "NATURAL",
                "NCHAR",
                "NDB",
                "NDBCLUSTER",
                "NEVER",
                "NEW",
                "NEXT",
                "NO",
                "NODEGROUP",
                "NONBLOCKING",
                "NONE",
                "NOT",
                "NO_WAIT",
                "NO_WRITE_TO_BINLOG",
                "NULL",
                "NUMBER",
                "NUMERIC",
                "NVARCHAR",
                "OFFSET",
                "OLD_PASSWORD",
                "ON",
                "ONE",
                "ONLY",
                "OPEN",
                "OPTIMIZE",
                "OPTIMIZER_COSTS",
                "OPTION",
                "OPTIONALLY",
                "OPTIONS",
                "OR",
                "ORDER",
                "OUT",
                "OUTER",
                "OUTFILE",
                "OWNER",
                "PACK_KEYS",
                "PAGE",
                "PARSER",
                "PARSE_GCOL_EXPR",
                "PARTIAL",
                "PARTITION",
                "PARTITIONING",
                "PARTITIONS",
                "PASSWORD",
                "PHASE",
                "PLUGIN",
                "PLUGINS",
                "PLUGIN_DIR",
                "POINT",
                "POLYGON",
                "PORT",
                "PRECEDES",
                "PRECISION",
                "PREPARE",
                "PRESERVE",
                "PREV",
                "PRIMARY",
                "PRIVILEGES",
                "PROCEDURE",
                "PROCESSLIST",
                "PROFILE",
                "PROFILES",
                "PROXY",
                "PURGE",
                "QUARTER",
                "QUERY",
                "QUICK",
                "RANGE",
                "READ",
                "READS",
                "READ_ONLY",
                "READ_WRITE",
                "REAL",
                "REBUILD",
                "RECOVER",
                "REDOFILE",
                "REDO_BUFFER_SIZE",
                "REDUNDANT",
                "REFERENCES",
                "REGEXP",
                "RELAY",
                "RELAYLOG",
                "RELAY_LOG_FILE",
                "RELAY_LOG_POS",
                "RELAY_THREAD",
                "RELEASE",
                "RELOAD",
                "REMOVE",
                "RENAME",
                "REORGANIZE",
                "REPAIR",
                "REPEAT",
                "REPEATABLE",
                "REPLACE",
                "REPLICATE_DO_DB",
                "REPLICATE_DO_TABLE",
                "REPLICATE_IGNORE_DB",
                "REPLICATE_IGNORE_TABLE",
                "REPLICATE_REWRITE_DB",
                "REPLICATE_WILD_DO_TABLE",
                "REPLICATE_WILD_IGNORE_TABLE",
                "REPLICATION",
                "REQUIRE",
                "RESET",
                "RESIGNAL",
                "RESTORE",
                "RESTRICT",
                "RESUME",
                "RETURN",
                "RETURNED_SQLSTATE",
                "RETURNS",
                "REVERSE",
                "REVOKE",
                "RIGHT",
                "RLIKE",
                "ROLLBACK",
                "ROLLUP",
                "ROTATE",
                "ROUTINE",
                "ROW",
                "ROWS",
                "ROW_COUNT",
                "ROW_FORMAT",
                "RTREE",
                "SAVEPOINT",
                "SCHEDULE",
                "SCHEMA",
                "SCHEMAS",
                "SCHEMA_NAME",
                "SECOND",
                "SECOND_MICROSECOND",
                "SECURITY",
                "SELECT",
                "SENSITIVE",
                "SEPARATOR",
                "SERIAL",
                "SERIALIZABLE",
                "SERVER",
                "SESSION",
                "SET",
                "SHARE",
                "SHOW",
                "SHUTDOWN",
                "SIGNAL",
                "SIGNED",
                "SIMPLE",
                "SLAVE",
                "SLOW",
                "SMALLINT",
                "SNAPSHOT",
                "SOCKET",
                "SOME",
                "SONAME",
                "SOUNDS",
                "SOURCE",
                "SPATIAL",
                "SPECIFIC",
                "SQL",
                "SQLEXCEPTION",
                "SQLSTATE",
                "SQLWARNING",
                "SQL_AFTER_GTIDS",
                "SQL_AFTER_MTS_GAPS",
                "SQL_BEFORE_GTIDS",
                "SQL_BIG_RESULT",
                "SQL_BUFFER_RESULT",
                "SQL_CACHE",
                "SQL_CALC_FOUND_ROWS",
                "SQL_NO_CACHE",
                "SQL_SMALL_RESULT",
                "SQL_THREAD",
                "SQL_TSI_DAY",
                "SQL_TSI_HOUR",
                "SQL_TSI_MINUTE",
                "SQL_TSI_MONTH",
                "SQL_TSI_QUARTER",
                "SQL_TSI_SECOND",
                "SQL_TSI_WEEK",
                "SQL_TSI_YEAR",
                "SSL",
                "STACKED",
                "START",
                "STARTING",
                "STARTS",
                "STATS_AUTO_RECALC",
                "STATS_PERSISTENT",
                "STATS_SAMPLE_PAGES",
                "STATUS",
                "STOP",
                "STORAGE",
                "STORED",
                "STRAIGHT_JOIN",
                "STRING",
                "SUBCLASS_ORIGIN",
                "SUBJECT",
                "SUBPARTITION",
                "SUBPARTITIONS",
                "SUPER",
                "SUSPEND",
                "SWAPS",
                "SWITCHES",
                "TABLE",
                "TABLES",
                "TABLESPACE",
                "TABLE_CHECKSUM",
                "TABLE_NAME",
                "TEMPORARY",
                "TEMPTABLE",
                "TERMINATED",
                "TEXT",
                "THAN",
                "THEN",
                "TIME",
                "TIMESTAMP",
                "TIMESTAMPADD",
                "TIMESTAMPDIFF",
                "TINYBLOB",
                "TINYINT",
                "TINYTEXT",
                "TO",
                "TRAILING",
                "TRANSACTION",
                "TRIGGER",
                "TRIGGERS",
                "TRUE",
                "TRUNCATE",
                "TYPE",
                "TYPES",
                "UNCOMMITTED",
                "UNDEFINED",
                "UNDO",
                "UNDOFILE",
                "UNDO_BUFFER_SIZE",
                "UNICODE",
                "UNINSTALL",
                "UNION",
                "UNIQUE",
                "UNKNOWN",
                "UNLOCK",
                "UNSIGNED",
                "UNTIL",
                "UPDATE",
                "UPGRADE",
                "USAGE",
                "USE",
                "USER",
                "USER_RESOURCES",
                "USE_FRM",
                "USING",
                "UTC_DATE",
                "UTC_TIME",
                "UTC_TIMESTAMP",
                "VALIDATION",
                "VALUE",
                "VALUES",
                "VARBINARY",
                "VARCHAR",
                "VARCHARACTER",
                "VARIABLES",
                "VARYING",
                "VIEW",
                "VIRTUAL",
                "WAIT",
                "WARNINGS",
                "WEEK",
                "WEIGHT_STRING",
                "WHEN",
                "WHERE",
                "WHILE",
                "WITH",
                "WITHOUT",
                "WORK",
                "WRAPPER",
                "WRITE",
                "X509",
                "XA",
                "XID",
                "XML",
                "XOR",
                "YEAR",
                "YEAR_MONTH",
                "ZEROFILL"));
    }}
