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
        return row[0]
    cur.execute("""
        INSERT INTO company (name, status, entityversion)
        VALUES (%s, 'ACTIVE', 0) RETURNING id;
    """, (name,))
    cid = cur.fetchone()[0]
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


def upsert_product_type(cur, name, code):
    """Insert product type or return existing id."""
    cur.execute("SELECT id FROM producttype WHERE code = %s;", (code,))
    row = cur.fetchone()
    if row:
        return row[0]
    cur.execute("""
        INSERT INTO producttype (entityversion, name, code)
        VALUES (0, %s, %s) RETURNING id;
    """, (name, code))
    return cur.fetchone()[0]


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

    # ── 5. Facility Types & Physical Facilities ──────────────────
    print("\n── Facilities ──")
    facilities = [
        ("Recepción",          "RECEPCION"),
        ("Clasificadora",      "CLASIFICADORA"),
        ("Túnel IQF",          "TUNEL_IQF"),
        ("Cámara Frigorífica", "CAMARA_FRIGORIFICA"),
    ]
    for fac_label, fac_code in facilities:
        cur.execute("SELECT id FROM facilitytype WHERE code = %s;", (fac_code,))
        ft_row = cur.fetchone()
        if ft_row:
            ft_id = ft_row[0]
        else:
            cur.execute("INSERT INTO facilitytype (label, code) VALUES (%s, %s) RETURNING id;", (fac_label, fac_code))
            ft_id = cur.fetchone()[0]

        # Link to value chain
        cur.execute("SELECT 1 FROM valuechainfacilitytype WHERE valuechain_id = %s AND facilitytype_id = %s;", (vc_id, ft_id))
        if not cur.fetchone():
            cur.execute("INSERT INTO valuechainfacilitytype (valuechain_id, facilitytype_id) VALUES (%s, %s);", (vc_id, ft_id))

        # Physical facility
        fac_name = f"Planta Dufer - {fac_label}"
        cur.execute("SELECT id FROM facility WHERE name = %s AND company_id = %s;", (fac_name, company_id))
        if not cur.fetchone():
            cur.execute("""
                INSERT INTO facility (name, company_id, facilitytype_id, entityversion,
                    iscollectionfacility, ispublic, displaytare, displaywomenonly)
                VALUES (%s, %s, %s, 0, false, false, false, false);
            """, (fac_name, company_id, ft_id))

    print(f"  ✅ {len(facilities)} facility types + physical facilities ensured")

    # ── 6. Semi-Products (Tallas y Tipos) ────────────────────────
    print("\n── Semi-Products ──")
    semi_products = [
        "Entero", "Cola",
        "Talla 21/25", "Talla 26/30", "Talla 31/35", "Talla 36/40",
    ]
    for sp_name in semi_products:
        cur.execute("SELECT id FROM semiproduct WHERE name = %s;", (sp_name,))
        sp_row = cur.fetchone()
        if sp_row:
            sp_id = sp_row[0]
        else:
            cur.execute("INSERT INTO semiproduct (name) VALUES (%s) RETURNING id;", (sp_name,))
            sp_id = cur.fetchone()[0]

        cur.execute("SELECT 1 FROM valuechainsemiproduct WHERE valuechain_id = %s AND semiproduct_id = %s;", (vc_id, sp_id))
        if not cur.fetchone():
            cur.execute("INSERT INTO valuechainsemiproduct (valuechain_id, semiproduct_id) VALUES (%s, %s);", (vc_id, sp_id))

    print(f"  ✅ {len(semi_products)} semi-products ensured")

    # ── 7. Processing Actions ────────────────────────────────────
    print("\n── Processing Actions ──")
    actions = [
        ("Clasificación por Tallas", "PROCESSING"),
        ("Asignación IQF (-2)",      "TRANSFER"),
        ("Rechazo a Descascarado",   "PROCESSING"),
    ]
    for action_name, action_type in actions:
        # Check by translation name to avoid duplicates
        cur.execute("""
            SELECT pa.id FROM processingaction pa
            JOIN processingactiontranslation pat ON pat.processingaction_id = pa.id
            WHERE pat.name = %s AND pat.language = 'ES';
        """, (action_name,))
        if cur.fetchone():
            continue
        cur.execute("""
            INSERT INTO processingaction (type, entityversion, company_id)
            VALUES (%s, 0, %s) RETURNING id;
        """, (action_type, company_id))
        pa_id = cur.fetchone()[0]
        cur.execute("""
            INSERT INTO processingactiontranslation (processingaction_id, language, name)
            VALUES (%s, 'ES', %s);
        """, (pa_id, action_name))

    print(f"  ✅ {len(actions)} processing actions ensured")

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
