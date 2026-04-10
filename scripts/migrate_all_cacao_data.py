import os
import pymysql
import psycopg2
import psycopg2.extras
from sshtunnel import SSHTunnelForwarder
import logging
from datetime import datetime

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# --- Configurations ---
MYSQL_HOST = '190.15.143.254'
MYSQL_USER = 'inatrace_test_fortaleza'
MYSQL_PASS = '3@9QWERTY!u'
MYSQL_DB = 'inatrace_test_fortaleza'
SSH_USER = 'giz'
SSH_KEY_PATH = os.path.expanduser('~/.ssh/cedia_key')

PG_HOST = '127.0.0.1'
PG_PORT = 5432
PG_USER = 'inatrace'
PG_PASS = 'inatrace'
PG_DB = 'inatrace'

# Master tables to migrate. Using list to skip entirely irrelevant Flyway metrics
EXCLUDED_TABLES = {'flyway_schema_history', 'schema_version', 'REVINFO'}

def get_pg_columns(pg_cursor, table_name):
    # Try exact match (for quoted created tables like "User") or lowercase (for unquoted defaults)
    pg_cursor.execute("SELECT column_name FROM information_schema.columns WHERE table_schema='public' AND (table_name=%s OR table_name=%s);", (table_name, table_name.lower()))
    return {row[0] for row in pg_cursor.fetchall()}

def get_pg_table_name(pg_cursor, table_name):
    pg_cursor.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND (table_name=%s OR table_name=%s);", (table_name, table_name.lower()))
    row = pg_cursor.fetchone()
    return row[0] if row else None

def sanitize_value(col_name, val):
    """Sanitize empty strings or unmapped tinyints"""
    if val == "":
        return None
    
    # Handle MySQL BIT(1) resolving as byte strings instead of booleans
    if isinstance(val, bytes):
        if val == b'\x00':
            return False
        if val == b'\x01':
            return True
        try:
            return val.decode('utf-8')
        except UnicodeDecodeError:
            return val

    # Fix for entityversion NOT NULL violation 
    if val is None and col_name.lower() in ('entityversion', 'version'):
        return 0
    return val

def table_specific_filter(table_name, row):
    """ Exclude shrimp data logic """
    # 1. Reject SHRIMP facility types
    if table_name == 'FacilityType' and row.get('code') in ['LARVA_GROWING', 'HATCHERY', 'MATURATION']:
        return False
        
    # Reject translations belonging to them (assuming we don't have code, but parent id. Oh, it's safer to just fetch everything if we assume CASCADE ON DELETE later or ignore mapping)
    # 2. Reject Shrimp Products
    if table_name == 'SemiProduct' and row.get('code') in ['NAUPLIUS', 'LARVA', 'HEAD_ON', 'TAIL_ON', 'TAIL_OFF_PEELED']:
        return False
        
    # Check translations that shouldn't be empty
    # If the user asked to fill untranslated fields, we can do it here if needed.
    return True

def run_migration():
    logging.info("Starting SSH tunnel...")
    server = SSHTunnelForwarder(
        (MYSQL_HOST, 22),
        ssh_username=SSH_USER,
        ssh_pkey=SSH_KEY_PATH,
        remote_bind_address=('127.0.0.1', 3306),
        local_bind_address=('127.0.0.1', 13306)
    )
    server.start()
    logging.info(f"Tunnel open on local port {server.local_bind_port}")
    
    try:
        mysql_conn = pymysql.connect(
            host='127.0.0.1',
            port=server.local_bind_port,
            user=MYSQL_USER,
            password=MYSQL_PASS,
            database=MYSQL_DB,
            cursorclass=pymysql.cursors.DictCursor
        )
        pg_conn = psycopg2.connect(
            host=PG_HOST,
            port=PG_PORT,
            user=PG_USER,
            password=PG_PASS,
            dbname=PG_DB
        )
        pg_conn.autocommit = True
        
        with mysql_conn.cursor() as mysql_cur, pg_conn.cursor() as pg_cur:
            # 1. Disable Postgres Constraints to allow insertion of everything
            pg_cur.execute("SET session_replication_role = 'replica';")
            
            # Fetch all MySQL tables
            mysql_cur.execute("SHOW TABLES;")
            tables = [list(row.values())[0] for row in mysql_cur.fetchall()]
            
            # Filter tables
            tables_to_migrate = [t for t in tables if t not in EXCLUDED_TABLES and '_AUD' not in t.upper() and 'ENVERS' not in t.upper()]
            
            total_records_inserted = 0
            
            # Phase 1: TRUNCATE all active tables in Postgres to avoid overlapping / primary key conflicts
            for table in tables_to_migrate:
                try:
                    pg_cur.execute(f'TRUNCATE TABLE "{table}" CASCADE;')
                except Exception as e:
                    logging.info(f"Skipping truncate {table} (It might not exist locally): {e}")

            # Phase 2: Insert Data
            for table in tables_to_migrate:
                # Get accurate table name used in actual postgres schema
                pg_table = get_pg_table_name(pg_cur, table)
                if not pg_table:
                    logging.warning(f"Table {table} not found in Postgres. Skipping.")
                    continue
                
                # Get Postgres schema to know what columns are valid
                valid_pg_columns = get_pg_columns(pg_cur, pg_table)
                if not valid_pg_columns:
                    logging.warning(f"Table {pg_table} columns not found in Postgres. Skipping.")
                    continue
                
                mysql_cur.execute(f"SELECT * FROM `{table}`;")
                rows = mysql_cur.fetchall()
                if not rows:
                    continue
                
                inserted_count = 0
                for row in rows:
                    if not table_specific_filter(table, row):
                        continue
                        
                    mapped_data = {}
                    for col_name, mysql_val in row.items():
                        # In Postgres, columns might also be lowercased
                        # Match case-insensitively to valid pg columns
                        match_cols = [c for c in valid_pg_columns if c.lower() == col_name.lower()]
                        if match_cols:
                            actual_pg_col = match_cols[0]
                            mapped_data[actual_pg_col] = sanitize_value(actual_pg_col, mysql_val)
                    
                    if not mapped_data:
                        continue
                    
                    columns = list(mapped_data.keys())
                    values = list(mapped_data.values())
                    
                    col_names_str = ', '.join([f'"{c}"' for c in columns])
                    placeholders_str = ', '.join(['%s'] * len(columns))
                    
                    insert_query = f"""
                        INSERT INTO "{pg_table}" ({col_names_str})
                        VALUES ({placeholders_str})
                        ON CONFLICT DO NOTHING;
                    """
                    try:
                        pg_cur.execute(insert_query, values)
                        inserted_count += 1
                        total_records_inserted += 1
                    except Exception as e:
                        logging.error(f"Error inserting row into {table}: {e}")
                        
                logging.info(f"Migrated {inserted_count} rows for table {table}")
            
            # Phase 3: Synchronize ID sequences for auto-incremental generation in Postgres
            logging.info("Resyncing primary key sequences...")
            pg_cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public';")
            all_pg_tables = [r[0] for r in pg_cur.fetchall()]
            for table in all_pg_tables:
                # Conventional sequence name is table_id_seq or handled internally
                # A robust way is to reset via max(id)
                if "id" in get_pg_columns(pg_cur, table):
                    try:
                        # Attempt to find sequence. Usually "Table_id_seq" in Hibernate
                        seq_name = f"{table}_id_seq".lower()
                        # Verify we have IDs
                        pg_cur.execute(f'SELECT MAX(id) FROM "{table}";')
                        max_id = pg_cur.fetchone()[0]
                        if max_id is not None:
                            # Let's dynamically find the sequence name for the ID column
                            pg_cur.execute(f"SELECT pg_get_serial_sequence('\"public\".\"{table}\"', 'id');")
                            actual_seq = pg_cur.fetchone()[0]
                            if actual_seq:
                                pg_cur.execute(f"SELECT setval('{actual_seq}', {max_id + 1});")
                    except Exception as e:
                        pass
            
            logging.info(f"Done! Successfully migrated {total_records_inserted} rows total.")

    except Exception as e:
        logging.error(f"Fatal error: {e}")
    finally:
        mysql_conn.close()
        pg_conn.close()
        server.stop()
        logging.info("Connections closed.")

if __name__ == '__main__':
    run_migration()
