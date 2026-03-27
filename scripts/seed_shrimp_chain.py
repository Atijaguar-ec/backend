import psycopg2
import sys
import uuid
import datetime

# --- CONFIGURACIÓN DE BASE DE DATOS ---
DB_HOST = "localhost"
DB_PORT = "5432"
DB_NAME = "inatrace"
DB_USER = "inatrace"
DB_PASS = "inatrace"

def get_connection():
    try:
        conn = psycopg2.connect(
            host=DB_HOST,
            port=DB_PORT,
            dbname=DB_NAME,
            user=DB_USER,
            password=DB_PASS
        )
        return conn
    except Exception as e:
        print(f"Error conectando a PostgreSQL: {e}")
        sys.exit(1)

def clean_database(cursor):
    print("Iniciando TRUNCATE de tablas principales...")
    
    tables_to_truncate = [
        "valuechain",
        "facilitytype",
        "semiproduct",
        "processingaction",
        "company",
        '"User"'
    ]
    
    for table in tables_to_truncate:
        try:
            cursor.execute(f"TRUNCATE TABLE {table} CASCADE;")
            print(f"✅ Tabla {table} truncada exitosamente.")
        except Exception as e:
            print(f"⚠️ Aviso al truncar {table} (puede que no exista o falten roles): {e}")
            cursor.execute("ROLLBACK;")

def seed_shrimp_value_chain(cursor):
    print("\nInicializando datos Maestros para la cadena de Camarón...")
    
    # --- 0. PRERREQUISITOS BASE ---
    cursor.execute("INSERT INTO producttype (entityversion, name, code) VALUES (0, 'Camarón', 'SHRIMP') RETURNING id;")
    pt_id = cursor.fetchone()[0]
    
    # Admin del sistema
    cursor.execute("""
        INSERT INTO "User" (entityversion, language, status, email, name, role)
        VALUES (0, 'ES', 'ACTIVE', 'admin_sys@dufer.local', 'SysAdmin', 'SYSTEM_ADMIN') RETURNING id;
    """)
    sys_user_id = cursor.fetchone()[0]

    # --- 1. COMPAÑÍA DUFER ---
    cursor.execute("""
        INSERT INTO company (name, status, entityversion)
        VALUES ('Dufer Cia. Ltda.', 'ACTIVE', 0) RETURNING id;
    """)
    company_id = cursor.fetchone()[0]
    print(f"🏢 Compañía creada (ID: {company_id})")

    # --- 2. VALUE CHAIN ---
    cursor.execute("""
        INSERT INTO valuechain (name, description, entityversion, valuechainstatus, createdby_id, producttype_id)
        VALUES (%s, %s, %s, %s, %s, %s) RETURNING id;
    """, ("Camarón Dufer", "Cadena de valor para Procesamiento de Camarón (Fase 1)", 0, 'ENABLED', sys_user_id, pt_id))
    vc_id = cursor.fetchone()[0]
    
    cursor.execute("INSERT INTO companyvaluechain (company_id, valuechain_id) VALUES (%s, %s);", (company_id, vc_id))
    print(f"🦐 ValueChain creada y asignada a Dufer (ID: {vc_id})")
    
    # --- 3. USUARIOS OPERADORES ---
    operators = [
        ("recepcion@dufer.local", "Operador Recepción", "COMPANY_USER"),
        ("clasificacion@dufer.local", "Operador Clasificación", "COMPANY_USER"),
        ("jefe_planta@dufer.local", "Jefe de Planta", "COMPANY_ADMIN")
    ]
    for email, name, crole in operators:
        # 1. Crear usuario base (rol USER a nivel global)
        cursor.execute("""
            INSERT INTO "User" (entityversion, language, status, email, name, role)
            VALUES (0, 'ES', 'ACTIVE', %s, %s, 'USER') RETURNING id;
        """, (email, name))
        u_id = cursor.fetchone()[0]
        # 2. Vincular usuario a compañía con su rol específico
        cursor.execute("""
            INSERT INTO companyuser (entityversion, company_id, user_id, role)
            VALUES (0, %s, %s, %s);
        """, (company_id, u_id, crole))
    print(f"👥 {len(operators)} Usuarios operadores creados y asignados a la compañía.")

    # --- 4. FACILITY TYPES ---
    facilities = ["Recepción", "Clasificadora", "Túnel IQF", "Cámara Frigorífica"]
    import re
    for fac in facilities:
        fac_code = re.sub(r'[^A-Z]', '', fac.upper())
        cursor.execute("INSERT INTO facilitytype (label, code) VALUES (%s, %s) RETURNING id;", (fac, fac_code))
        fac_type_id = cursor.fetchone()[0]
        cursor.execute("INSERT INTO valuechainfacilitytype (valuechain_id, facilitytype_id) VALUES (%s, %s);", (vc_id, fac_type_id))
        
        # Opcional: Crear la facility física para esta compañía
        cursor.execute("""
            INSERT INTO facility (name, company_id, facilitytype_id, entityversion, iscollectionfacility, ispublic, displaytare, displaywomenonly)
            VALUES (%s, %s, %s, 0, false, false, false, false)
        """, (f"Planta Dufer - {fac}", company_id, fac_type_id))
    print(f"🏭 {len(facilities)} Plantas Físicas (Facilities) y Tipos creados.")
    
    # 3. Configurar SemiProducts (Tallas y Tipos)
    products = [
        "Entero", "Cola",
        "Talla 21/25", "Talla 26/30", "Talla 31/35", "Talla 36/40"
    ]
    for prod in products:
        cursor.execute("INSERT INTO semiproduct (name) VALUES (%s) RETURNING id;", (prod,))
        prod_id = cursor.fetchone()[0]
        cursor.execute("INSERT INTO valuechainsemiproduct (valuechain_id, semiproduct_id) VALUES (%s, %s);", (vc_id, prod_id))
        
    print(f"✅ {len(products)} SemiProducts creados y linkeados.")

    # 4. Configurar ProcessingActions (Acciones Base)
    actions = [
        ("Clasificación por Tallas", "PROCESSING"),
        ("Asignación IQF (-2)", "TRANSFER"),
        ("Rechazo a Descascarado", "PROCESSING")
    ]
    for name, a_type in actions:
        cursor.execute("""
            INSERT INTO processingaction (type, entityversion) 
            VALUES (%s, %s) RETURNING id;
        """, (a_type, 0))
        p_id = cursor.fetchone()[0]
        
        # Insertar el nombre en la tabla de traducciones
        cursor.execute("""
            INSERT INTO processingactiontranslation (processingaction_id, language, name)
            VALUES (%s, %s, %s);
        """, (p_id, 'ES', name))
    
    print("✅ ProcessingActions y Translations creados.")

if __name__ == "__main__":
    conn = get_connection()
    conn.autocommit = False # Asegurar transacción para toda la semilla
    cursor = conn.cursor()
    
    try:
        # Ejecutar operaciones
        clean_database(cursor)
        seed_shrimp_value_chain(cursor)
        
        # Confirmar
        conn.commit()
        print("\n🎉 ¡Inicialización de BD Camarón completada exitosamente!")
    except Exception as e:
        conn.rollback()
        print(f"\n❌ Error durante el seeding. Transacción revertida: {e}")
    finally:
        cursor.close()
        conn.close()
