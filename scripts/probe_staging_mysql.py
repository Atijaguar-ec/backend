import os
import pymysql
from sshtunnel import SSHTunnelForwarder

remote_host = '190.15.143.254'
ssh_user = 'giz'
ssh_key_path = os.path.expanduser('~/.ssh/cedia_key')

db_user = 'inatrace_test_fortaleza'
db_password = '3@9QWERTY!u'
db_name = 'inatrace_test_fortaleza'

def test_connection():
    server = SSHTunnelForwarder(
        (remote_host, 22),
        ssh_username=ssh_user,
        ssh_pkey=ssh_key_path,
        remote_bind_address=('127.0.0.1', 3306),
        local_bind_address=('127.0.0.1', 13306)
    )
    server.start()
    
    try:
        conn = pymysql.connect(
            host='127.0.0.1',
            port=server.local_bind_port,
            user=db_user,
            password=db_password,
            database=db_name,
            cursorclass=pymysql.cursors.DictCursor
        )
        
        with conn.cursor() as cursor:
            cursor.execute("SHOW TABLES;")
            tables = [list(row.values())[0] for row in cursor.fetchall()]
            
            print("--- Table Rows ---")
            for table in tables:
                if 'flyway' in table.lower() or 'envers' in table.lower() or '_AUD' in table.upper():
                    continue
                cursor.execute(f"SELECT COUNT(*) as c FROM `{table}`;")
                count = cursor.fetchone()['c']
                if count > 0:
                    print(f"{table}: {count}")
    finally:
        conn.close()
        server.stop()

if __name__ == '__main__':
    test_connection()
