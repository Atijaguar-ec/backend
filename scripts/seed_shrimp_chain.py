#!/usr/bin/env python3
"""
seed_shrimp_chain.py — Seed the INATrace DB for the Dufer shrimp value chain.

Usage:
    python seed_shrimp_chain.py              # Idempotent upsert (safe to re-run)
    python seed_shrimp_chain.py --clean      # TRUNCATE chain tables first, then seed
    python seed_shrimp_chain.py --dry-run    # Print SQL without executing

Requires: psycopg2  (pip install psycopg2-binary)
"""

import argparse
import csv
import os
import re
import sys

try:
    import psycopg2
except ImportError:
    print("❌ psycopg2 not found. Install with: pip install psycopg2-binary")
    sys.exit(1)

# ---------------------------------------------------------------------------
# Configuration (overridable via env vars)
# ---------------------------------------------------------------------------
DB_HOST = os.getenv("DB_HOST", "localhost")
DB_PORT = os.getenv("DB_PORT", "5432")
DB_NAME = os.getenv("DB_NAME", "inatrace")
DB_USER = os.getenv("DB_USER", "inatrace")
DB_PASS = os.getenv("DB_PASS", "inatrace")

# The Keycloak admin email — must match the JWT `email` claim
ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "alvarogeovani@gmail.com")

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def get_connection():
    try:
        conn = psycopg2.connect(
            host=DB_HOST, port=DB_PORT,
            dbname=DB_NAME, user=DB_USER, password=DB_PASS
        )
        return conn
    except Exception as e:
        print(f"❌ Error connecting to PostgreSQL: {e}")
        sys.exit(1)


def upsert_user(cur, email, name, surname, role, language="ES"):
    """Insert user or return existing id if email already exists."""
    cur.execute('SELECT id FROM "User" WHERE email = %s;', (email,))
    row = cur.fetchone()
    if row:
        print(f"  ↩ User '{email}' already exists (id={row[0]})")
        return row[0]
    cur.execute("""
        INSERT INTO "User" (entityversion, language, status, email, name, surname, role)
        VALUES (0, %s, 'ACTIVE', %s, %s, %s, %s) RETURNING id;
    """, (language, email, name, surname, role))
    uid = cur.fetchone()[0]
    print(f"  ✅ User '{email}' created (id={uid})")
    return uid


def upsert_company(cur, name):
    """Insert company or return existing id if name already exists."""
    cur.execute("SELECT id FROM company WHERE name = %s;", (name,))
    row = cur.fetchone()
    if row:
        print(f"  ↩ Company '{name}' already exists (id={row[0]})")
        upsert_translation(cur, "companytranslation", "company_id", row[0], name)
        return row[0]
    cur.execute("""
        INSERT INTO company (name, status, entityversion)
        VALUES (%s, 'ACTIVE', 0) RETURNING id;
    """, (name,))
    cid = cur.fetchone()[0]
    upsert_translation(cur, "companytranslation", "company_id", cid, name)
    print(f"  ✅ Company '{name}' created (id={cid})")
    return cid


def link_company_user(cur, company_id, user_id, role):
    """Link user to company with a role, skip if already linked."""
    cur.execute("""
        SELECT id FROM companyuser WHERE company_id = %s AND user_id = %s;
    """, (company_id, user_id))
    if cur.fetchone():
        print(f"  ↩ User {user_id} already linked to company {company_id}")
        return
    cur.execute("""
        INSERT INTO companyuser (entityversion, company_id, user_id, role)
        VALUES (0, %s, %s, %s);
    """, (company_id, user_id, role))
    print(f"  ✅ User {user_id} linked to company {company_id} as {role}")


def upsert_translation(cur, table, ref_col, ref_id, name, language="ES"):
    """Upsert translation for a given entity. Inserts both ES and EN to ensure
    the Core UI works regardless of the language header (defaults to EN)."""
    for lang in ["ES", "EN"]:
        cur.execute(f"SELECT 1 FROM {table} WHERE {ref_col} = %s AND language = %s;", (ref_id, lang))
        if not cur.fetchone():
            cur.execute(f"""
                INSERT INTO {table} ({ref_col}, language, name)
                VALUES (%s, %s, %s);
            """, (ref_id, lang, name))


def upsert_product_type(cur, name, code):
    """Insert product type or return existing id."""
    cur.execute("SELECT id FROM producttype WHERE code = %s;", (code,))
    row = cur.fetchone()
    if row:
        upsert_translation(cur, "producttypetranslation", "producttype_id", row[0], name)
        return row[0]
    cur.execute("""
        INSERT INTO producttype (entityversion, name, code)
        VALUES (0, %s, %s) RETURNING id;
    """, (name, code))
    pid = cur.fetchone()[0]
    upsert_translation(cur, "producttypetranslation", "producttype_id", pid, name)
    return pid


# ---------------------------------------------------------------------------
# Clean (optional)
# ---------------------------------------------------------------------------

TABLES_TO_CLEAN = [
    "processingactiontranslation",
    "processingactionvaluechain",
    "processingaction",
    "valuechainsemiproduct",
    "semiproduct",
    "valuechainfacilitytype",
    "facility",
    "facilitylocation",
    "facilitytype",
    "product",
    "companyvaluechain",
    "valuechain",
    "companyuser",
    "company",
    # NOTE: NOT truncating "User" — we preserve the admin user
]


def clean_database(cur):
    """TRUNCATE chain-related tables. Preserves Users."""
    print("\n🧹 Cleaning chain tables (preserving users)...")
    for table in TABLES_TO_CLEAN:
        try:
            cur.execute(f"TRUNCATE TABLE {table} CASCADE;")
            print(f"  ✅ {table} truncated")
        except Exception as e:
            print(f"  ⚠️  {table}: {e}")
            cur.connection.rollback()


# ---------------------------------------------------------------------------
# Seed
# ---------------------------------------------------------------------------

def seed(cur):
    print("\n🦐 Seeding Dufer shrimp value chain...\n")

    # ── 0. Admin user (your Keycloak user) ────────────────────────
    print("── Users ──")
    admin_id = upsert_user(cur, ADMIN_EMAIL, "SysAdmin", "", "SYSTEM_ADMIN")

    # ── 0. Countries (from Java migration V2020_03_27_15_00 that Flyway skipped) ─
    print("\n── Countries ──")
    cur.execute("SELECT COUNT(*) FROM country;")
    country_count = cur.fetchone()[0]
    if country_count == 0:
        csv_path = os.path.join(os.path.dirname(__file__), "..", "import", "countries.csv")
        if os.path.exists(csv_path):
            with open(csv_path, encoding="utf-8") as f:
                reader = csv.DictReader(f)
                inserted = 0
                for row in reader:
                    cur.execute("INSERT INTO country (code, name) VALUES (%s, %s);", (row["Code"], row["Name"]))
                    inserted += 1
            print(f"  ✅ {inserted} countries loaded from countries.csv")
        else:
            print(f"  ⚠️  countries.csv not found at {csv_path}, inserting Ecuador only")
            cur.execute("INSERT INTO country (code, name) VALUES ('EC', 'Ecuador');")
    else:
        print(f"  ↩ {country_count} countries already exist")

    # ── 1. Company ────────────────────────────────────────────────
    print("\n── Company ──")
    company_id = upsert_company(cur, "Dufer Cia. Ltda.")

    # Link admin as COMPANY_ADMIN
    link_company_user(cur, company_id, admin_id, "COMPANY_ADMIN")

    # ── 2. Operator users ────────────────────────────────────────
    print("\n── Operators ──")
    operators = [
        ("recepcion@dufer.local",     "Operador Recepción",     "", "COMPANY_USER"),
        ("clasificacion@dufer.local", "Operador Clasificación", "", "COMPANY_USER"),
        ("jefe_planta@dufer.local",   "Jefe de Planta",         "", "COMPANY_ADMIN"),
    ]
    for email, name, surname, crole in operators:
        uid = upsert_user(cur, email, name, surname, "USER")
        link_company_user(cur, company_id, uid, crole)

    # ── 3. Product Type ──────────────────────────────────────────
    print("\n── Product Type ──")
    pt_id = upsert_product_type(cur, "Camarón", "SHRIMP")
    print(f"  ✅ ProductType 'Camarón' (id={pt_id})")

    # ── 4. Value Chain ───────────────────────────────────────────
    print("\n── Value Chain ──")
    cur.execute("SELECT id FROM valuechain WHERE name = %s;", ("Camarón Dufer",))
    vc_row = cur.fetchone()
    if vc_row:
        vc_id = vc_row[0]
        print(f"  ↩ ValueChain 'Camarón Dufer' already exists (id={vc_id})")
    else:
        cur.execute("""
            INSERT INTO valuechain
                (name, description, entityversion, valuechainstatus, createdby_id, producttype_id)
            VALUES (%s, %s, 0, 'ENABLED', %s, %s) RETURNING id;
        """, ("Camarón Dufer", "Cadena de valor para Procesamiento de Camarón (Fase 1)", admin_id, pt_id))
        vc_id = cur.fetchone()[0]
        print(f"  ✅ ValueChain 'Camarón Dufer' created (id={vc_id})")

    # Link company ↔ value chain
    cur.execute("SELECT 1 FROM companyvaluechain WHERE company_id = %s AND valuechain_id = %s;", (company_id, vc_id))
    if not cur.fetchone():
        cur.execute("INSERT INTO companyvaluechain (company_id, valuechain_id) VALUES (%s, %s);", (company_id, vc_id))
        print(f"  ✅ Company linked to ValueChain")

    # ── 4b. Measurement Unit Types ───────────────────────────────
    print("\n── Measurement Units ──")
    unit_ids = {}
    units = [
        ("LBS", "Libras (lbs)", None),
        ("CAJETA", "Cajetas", None),
        ("KG", "Kilogramos (kg)", None),
    ]
    for code, label, weight in units:
        cur.execute("SELECT id FROM measureunittype WHERE code = %s;", (code,))
        row = cur.fetchone()
        if row:
            unit_ids[code] = row[0]
            print(f"  ↩ Unit '{code}' already exists (id={row[0]})")
        else:
            if weight is not None:
                cur.execute("INSERT INTO measureunittype (code, label, weight) VALUES (%s, %s, %s) RETURNING id;", (code, label, weight))
            else:
                cur.execute("INSERT INTO measureunittype (code, label) VALUES (%s, %s) RETURNING id;", (code, label))
            unit_ids[code] = cur.fetchone()[0]
            print(f"  ✅ Unit '{code}' created (id={unit_ids[code]})")

    # Link units to value chain
    for code in unit_ids:
        cur.execute("SELECT 1 FROM valuechainmeasureunittype WHERE valuechain_id = %s AND measureunittype_id = %s;", (vc_id, unit_ids[code]))
        if not cur.fetchone():
            cur.execute("INSERT INTO valuechainmeasureunittype (valuechain_id, measureunittype_id) VALUES (%s, %s);", (vc_id, unit_ids[code]))

    # ── 5. Facility Types & Physical Facilities ──────────────────
    print("\n── Facilities ──")
    facilities = [
        # (label, code, is_collection_facility)
        ("Recepción",          "RECEPCION",          True),
        ("Clasificadora",      "CLASIFICADORA",      False),
        ("Túnel IQF",          "TUNEL_IQF",          False),
        ("Cámara Frigorífica", "CAMARA_FRIGORIFICA", False),
    ]

    # Get Ecuador country ID (loaded in step 0)
    cur.execute("SELECT id FROM country WHERE code = 'EC';")
    ecuador_id = cur.fetchone()[0]

    facility_ids = {}  # code -> facility_id
    # Dufer plant location: Guayaquil, Ecuador
    plant_lat, plant_lng = -2.1894, -79.8891

    for fac_label, fac_code, is_collection in facilities:
        cur.execute("SELECT id FROM facilitytype WHERE code = %s;", (fac_code,))
        ft_row = cur.fetchone()
        if ft_row:
            ft_id = ft_row[0]
        else:
            cur.execute("INSERT INTO facilitytype (label, code) VALUES (%s, %s) RETURNING id;", (fac_label, fac_code))
            ft_id = cur.fetchone()[0]

        # Link to value chain (old way, now handled by facilityvaluechain)
        cur.execute("SELECT 1 FROM valuechainfacilitytype WHERE valuechain_id = %s AND facilitytype_id = %s;", (vc_id, ft_id))
        if not cur.fetchone():
            cur.execute("INSERT INTO valuechainfacilitytype (valuechain_id, facilitytype_id) VALUES (%s, %s);", (vc_id, ft_id))

        # Physical facility
        fac_name = f"Planta Dufer - {fac_label}"
        cur.execute("SELECT id FROM facility WHERE name = %s AND company_id = %s;", (fac_name, company_id))
        fac_row = cur.fetchone()
        if not fac_row:
            # Create FacilityLocation first
            cur.execute("""
                INSERT INTO facilitylocation
                    (entityversion, latitude, longitude, pinname,
                     address_address, address_city, address_state, address_country_id,
                     ispubliclyvisible, numberoffarmers)
                VALUES (0, %s, %s, %s, %s, %s, %s, %s, false, 0) RETURNING id;
            """, (plant_lat, plant_lng, fac_name,
                  "Km 10.5 Vía Daule", "Guayaquil", "Guayas", ecuador_id))
            loc_id = cur.fetchone()[0]

            cur.execute("""
                INSERT INTO facility (name, company_id, facilitytype_id, facilitylocation_id,
                    entityversion, iscollectionfacility, ispublic, displaytare, displaywomenonly)
                VALUES (%s, %s, %s, %s, 0, %s, false, false, false) RETURNING id;
            """, (fac_name, company_id, ft_id, loc_id, is_collection))
            fac_id = cur.fetchone()[0]
        else:
            fac_id = fac_row[0]

        facility_ids[fac_code] = fac_id
        upsert_translation(cur, "facilitytranslation", "facility_id", fac_id, fac_name)

        # ── 5b. Link facility ↔ value chain (facilityvaluechain) ──
        cur.execute("SELECT 1 FROM facilityvaluechain WHERE facility_id = %s AND valuechain_id = %s;", (fac_id, vc_id))
        if not cur.fetchone():
            cur.execute("INSERT INTO facilityvaluechain (facility_id, valuechain_id) VALUES (%s, %s);", (fac_id, vc_id))

    print(f"  ✅ {len(facilities)} facility types + physical facilities ensured")
    print(f"  ✅ All facilities linked to ValueChain (facilityvaluechain)")

    # ── 6. Semi-Products (Tallas y Tipos) ────────────────────────
    print("\n── Semi-Products ──")
    semi_products = [
        # (name, unit_code, buyable, is_sku)
        ("Entero",      "LBS",    True,  False),
        ("Cola",        "LBS",    True,  False),
        ("Talla 21/25", "LBS",    False, False),
        ("Talla 26/30", "LBS",    False, False),
        ("Talla 31/35", "LBS",    False, False),
        ("Talla 36/40", "LBS",    False, False),
    ]
    sp_ids = {}  # name -> id
    for sp_name, sp_unit, sp_buyable, sp_sku in semi_products:
        cur.execute("SELECT id FROM semiproduct WHERE name = %s;", (sp_name,))
        sp_row = cur.fetchone()
        if sp_row:
            sp_id = sp_row[0]
            # Update measurement unit if missing
            cur.execute("""
                UPDATE semiproduct SET measurementunittype_id = %s, isbuyable = %s, issku = %s
                WHERE id = %s AND (measurementunittype_id IS NULL OR isbuyable IS DISTINCT FROM %s OR issku IS DISTINCT FROM %s);
            """, (unit_ids[sp_unit], sp_buyable, sp_sku, sp_id, sp_buyable, sp_sku))
        else:
            cur.execute("""
                INSERT INTO semiproduct (name, measurementunittype_id, isbuyable, issku)
                VALUES (%s, %s, %s, %s) RETURNING id;
            """, (sp_name, unit_ids[sp_unit], sp_buyable, sp_sku))
            sp_id = cur.fetchone()[0]

        sp_ids[sp_name] = sp_id
        upsert_translation(cur, "semiproducttranslation", "semiproduct_id", sp_id, sp_name)

        cur.execute("SELECT 1 FROM valuechainsemiproduct WHERE valuechain_id = %s AND semiproduct_id = %s;", (vc_id, sp_id))
        if not cur.fetchone():
            cur.execute("INSERT INTO valuechainsemiproduct (valuechain_id, semiproduct_id) VALUES (%s, %s);", (vc_id, sp_id))

    print(f"  ✅ {len(semi_products)} semi-products ensured (with measurement units)")

    # ── 6b. Link facilities ↔ semi-products (facilitysemiproduct) ──
    print("\n── Facility ↔ Semi-Product Links ──")
    # Which semi-products each facility can handle
    fac_sp_map = {
        "RECEPCION":          ["Entero", "Cola"],
        "CLASIFICADORA":      ["Entero", "Cola", "Talla 21/25", "Talla 26/30", "Talla 31/35", "Talla 36/40"],
        "TUNEL_IQF":          ["Talla 21/25", "Talla 26/30", "Talla 31/35", "Talla 36/40"],
        "CAMARA_FRIGORIFICA": ["Talla 21/25", "Talla 26/30", "Talla 31/35", "Talla 36/40"],
    }
    for fac_code, sp_names in fac_sp_map.items():
        fac_id = facility_ids[fac_code]
        for sp_name in sp_names:
            sp_id = sp_ids[sp_name]
            cur.execute("SELECT 1 FROM facilitysemiproduct WHERE facility_id = %s AND semiproduct_id = %s;", (fac_id, sp_id))
            if not cur.fetchone():
                cur.execute("""
                    INSERT INTO facilitysemiproduct (facility_id, semiproduct_id, entityversion)
                    VALUES (%s, %s, 0);
                """, (fac_id, sp_id))
    print(f"  ✅ Facility ↔ Semi-Product links created")

    # ── 7. Processing Actions (with full relationships) ──────────
    print("\n── Processing Actions ──")

    def upsert_processing_action(cur, name, action_type, input_sp_name, prefix, company_id):
        """Create or fetch a processing action and return its ID."""
        cur.execute("""
            SELECT pa.id FROM processingaction pa
            JOIN processingactiontranslation pat ON pat.processingaction_id = pa.id
            WHERE pat.name = %s AND pat.language = 'ES';
        """, (name,))
        row = cur.fetchone()
        if row:
            pa_id = row[0]
            # Update fields that were missing before
            input_sp_id = sp_ids.get(input_sp_name) if input_sp_name else None
            cur.execute("""
                UPDATE processingaction SET inputsemiproduct_id = %s, prefix = %s, type = %s
                WHERE id = %s AND (inputsemiproduct_id IS DISTINCT FROM %s OR prefix IS DISTINCT FROM %s OR type IS DISTINCT FROM %s);
            """, (input_sp_id, prefix, action_type, pa_id, input_sp_id, prefix, action_type))
            print(f"  ↩ PA '{name}' updated (id={pa_id})")
            return pa_id
        else:
            input_sp_id = sp_ids.get(input_sp_name) if input_sp_name else None
            cur.execute("""
                INSERT INTO processingaction (type, entityversion, company_id, inputsemiproduct_id, prefix)
                VALUES (%s, 0, %s, %s, %s) RETURNING id;
            """, (action_type, company_id, input_sp_id, prefix))
            pa_id = cur.fetchone()[0]
            cur.execute("""
                INSERT INTO processingactiontranslation (processingaction_id, language, name)
                VALUES (%s, 'ES', %s);
            """, (pa_id, name))
            cur.execute("""
                INSERT INTO processingactiontranslation (processingaction_id, language, name)
                VALUES (%s, 'EN', %s);
            """, (pa_id, name))
            print(f"  ✅ PA '{name}' created (id={pa_id})")
            return pa_id

    # Processing Action definitions:
    # (name, type, input_semi_product, prefix, supported_facilities, output_semi_products)
    pa_defs = [
        {
            "name": "Clasificación por Tallas",
            "type": "PROCESSING",
            "input_sp": "Entero",
            "prefix": "CLAS",
            "facilities": ["CLASIFICADORA"],
            "output_sps": ["Talla 21/25", "Talla 26/30", "Talla 31/35", "Talla 36/40"],
        },
        {
            "name": "Asignación IQF (-2)",
            "type": "TRANSFER",
            "input_sp": "Talla 21/25",  # Any talla can be sent to IQF
            "prefix": "IQF",
            "facilities": ["CLASIFICADORA", "TUNEL_IQF"],
            "output_sps": [],
        },
        {
            "name": "Rechazo a Descascarado",
            "type": "PROCESSING",
            "input_sp": "Entero",
            "prefix": "RECH",
            "facilities": ["CLASIFICADORA"],
            "output_sps": ["Cola"],
        },
    ]

    for pa_def in pa_defs:
        pa_id = upsert_processing_action(
            cur, pa_def["name"], pa_def["type"],
            pa_def["input_sp"], pa_def["prefix"], company_id
        )

        # ── 7a. Link PA ↔ value chain (processingactionvaluechain) ──
        cur.execute("SELECT 1 FROM processingactionvaluechain WHERE processingaction_id = %s AND valuechain_id = %s;", (pa_id, vc_id))
        if not cur.fetchone():
            cur.execute("INSERT INTO processingactionvaluechain (processingaction_id, valuechain_id) VALUES (%s, %s);", (pa_id, vc_id))

        # ── 7b. Link PA ↔ facilities (processingactionfacility) ──
        for fac_code in pa_def["facilities"]:
            fac_id = facility_ids[fac_code]
            cur.execute("SELECT 1 FROM processingactionfacility WHERE processingaction_id = %s AND facility_id = %s;", (pa_id, fac_id))
            if not cur.fetchone():
                cur.execute("INSERT INTO processingactionfacility (processingaction_id, facility_id) VALUES (%s, %s);", (pa_id, fac_id))

        # ── 7c. Link PA ↔ output semi-products (processingactionoutputsemiproduct) ──
        for sp_name in pa_def["output_sps"]:
            sp_id = sp_ids[sp_name]
            cur.execute("SELECT 1 FROM processingactionoutputsemiproduct WHERE processingaction_id = %s AND outputsemiproduct_id = %s;", (pa_id, sp_id))
            if not cur.fetchone():
                cur.execute("""
                    INSERT INTO processingactionoutputsemiproduct
                        (processingaction_id, outputsemiproduct_id)
                    VALUES (%s, %s);
                """, (pa_id, sp_id))

    print(f"  ✅ Processing actions fully linked (VC, facilities, outputs)")

    # ── 8. Product (so the dashboard is not empty) ───────────────
    print("\n── Product ──")
    product_name = "Camarón Blanco Dufer"
    cur.execute("SELECT id FROM product WHERE name = %s AND company_id = %s;", (product_name, company_id))
    if cur.fetchone():
        print(f"  ↩ Product '{product_name}' already exists")
    else:
        cur.execute("""
            INSERT INTO product (name, description, entityversion, status, company_id, valuechain_id)
            VALUES (%s, %s, 0, 'ACTIVE', %s, %s) RETURNING id;
        """, (product_name, "Camarón blanco del Pacífico ecuatoriano – Litopenaeus vannamei", company_id, vc_id))
        prod_id = cur.fetchone()[0]
        print(f"  ✅ Product '{product_name}' created (id={prod_id})")

        # Link product to company via productcompany
        cur.execute("""
            INSERT INTO productcompany (type, company_id, product_id)
            VALUES ('OWNER', %s, %s);
        """, (company_id, prod_id))
        print(f"  ✅ Product linked to company as OWNER")

    # ── 9. Currency ─────────────────────────────────────────────
    print("\n── Currency ──")
    cur.execute("SELECT id FROM currencytype WHERE code = 'USD';")
    usd_row = cur.fetchone()
    if usd_row:
        usd_id = usd_row[0]
        print(f"  ↩ Currency 'USD' already exists (id={usd_id})")
    else:
        cur.execute("INSERT INTO currencytype (code, label, enabled) VALUES ('USD', 'Dólar Estadounidense', true) RETURNING id;")
        usd_id = cur.fetchone()[0]
        print(f"  ✅ Currency 'USD' created (id={usd_id})")

    # Link currency to company
    cur.execute("UPDATE company SET currency_id = %s WHERE id = %s AND currency_id IS NULL;", (usd_id, company_id))
    print(f"  ✅ Company currency set to USD")

    # ── 10. Sample Farmers / Proveedores Camaroneros ─────────────
    print("\n── Farmers (Proveedores) ──")

    # Get product ID for linking
    cur.execute("SELECT id FROM product WHERE name = %s AND company_id = %s;", ("Camarón Blanco Dufer", company_id))
    prod_row = cur.fetchone()
    product_id = prod_row[0] if prod_row else None

    farmers = [
        {
            "name": "Carlos", "surname": "Mendoza Reyes",
            "internal_id": "PROV-001", "phone": "0991234567",
            "gender": "MALE", "type": "FARMER",
            "location": "Isla Puná, Guayaquil",
        },
        {
            "name": "María", "surname": "Tomalá Suárez",
            "internal_id": "PROV-002", "phone": "0987654321",
            "gender": "FEMALE", "type": "FARMER",
            "location": "San Pablo, Santa Elena",
        },
        {
            "name": "José", "surname": "Borbor Lindao",
            "internal_id": "PROV-003", "phone": "0998765432",
            "gender": "MALE", "type": "FARMER",
            "location": "Chanduy, Santa Elena",
        },
        {
            "name": "Rosa", "surname": "Quimí Villón",
            "internal_id": "PROV-004", "phone": "0976543210",
            "gender": "FEMALE", "type": "FARMER",
            "location": "Engabao, Guayaquil",
        },
        {
            "name": "Pedro", "surname": "Acopiador Central",
            "internal_id": "ACOP-001", "phone": "0965432109",
            "gender": "MALE", "type": "COLLECTOR",
            "location": "Muisne, Esmeraldas",
        },
    ]

    for f in farmers:
        cur.execute("SELECT id FROM usercustomer WHERE farmercompanyinternalid = %s AND company_id = %s;",
                    (f["internal_id"], company_id))
        row = cur.fetchone()
        if row:
            print(f"  ↩ Farmer '{f['name']} {f['surname']}' already exists (id={row[0]})")
            continue
        cur.execute("""
            INSERT INTO usercustomer
                (name, surname, farmercompanyinternalid, phone, gender, type, location, company_id, product_id)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s) RETURNING id;
        """, (f["name"], f["surname"], f["internal_id"], f["phone"],
              f["gender"], f["type"], f["location"], company_id, product_id))
        farmer_id = cur.fetchone()[0]
        print(f"  ✅ Farmer '{f['name']} {f['surname']}' created (id={farmer_id})")

    print(f"  ✅ {len(farmers)} farmers/proveedores ensured")


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Seed INATrace DB for Dufer shrimp chain")
    parser.add_argument("--clean", action="store_true", help="TRUNCATE chain tables before seeding (preserves users)")
    parser.add_argument("--dry-run", action="store_true", help="Print actions without committing")
    args = parser.parse_args()

    conn = get_connection()
    conn.autocommit = False
    cur = conn.cursor()

    try:
        if args.clean:
            clean_database(cur)

        seed(cur)

        if args.dry_run:
            conn.rollback()
            print("\n🔍 Dry-run complete — no changes committed.")
        else:
            conn.commit()
            print("\n🎉 Seeding complete!")

    except Exception as e:
        conn.rollback()
        print(f"\n❌ Error during seeding. Transaction rolled back: {e}")
        import traceback
        traceback.print_exc()
    finally:
        cur.close()
        conn.close()
