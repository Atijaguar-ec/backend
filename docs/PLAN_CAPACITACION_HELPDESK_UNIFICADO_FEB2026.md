# Plan de Capacitación Unificado: Mantenimiento INATrace Backend/Frontend - UNOCACE
## Técnico Helpdesk e Infraestructura

---

## 📋 Información General

- **Cliente:** UNOCACE (Unión de Organizaciones Campesinas Cacaoteras del Ecuador)
- **Producto:** Cacao (COCOA)
- **Fechas:** 23 al 27 de febrero de 2026 (5 días)
- **Duración diaria:** 4 horas (total 20 horas)
- **Modalidad:** Práctica intensiva en ambientes UNOCACE
- **Nivel:** Técnico con nociones básicas de Linux y Docker

---

## 🎯 Objetivos de Aprendizaje
Al finalizar, el técnico podrá:
1. Verificar salud de frontend, backend y base de datos.
2. Reiniciar servicios de forma segura (frontend y backend).
3. Crear, verificar y restaurar respaldos de base de datos.
4. Consultar y filtrar logs para diagnóstico rápido.
5. Resolver problemas operativos frecuentes documentados.
6. Verificar conectividad frontend-backend y escalar con evidencia.

---

## 🌐 Infraestructura UNOCACE (referencia)
- **Ambientes y contenedores:**
  - Test: https://inatrace-test.unocace.com — `inatrace-fe-test-unocace`, `inatrace-be-test-unocace`, `inatrace-mysql`
  - Producción: https://inatrace.unocace.com — `inatrace-fe-prod-unocace`, `inatrace-be-prod-unocace`, `inatrace-mysql-prod-unocace`
- **Rutas clave:**
  - Backend: `/opt/inatrace/backend/` (env, docker-compose.yml)
  - Frontend: `/opt/inatrace/frontend/{test,prod}/unocace/`
  - Datos/respaldos: `/opt/inatrace/mysql/`, `/opt/inatrace/backups/mysql/`, `/opt/inatrace/uploads/`
- **Bases de datos:** `inatrace_test_unocace`, `inatrace_prod_unocace`

---

## 🗓️ Programa Detallado (23-27 febrero 2026)
### Día 1 (23 feb) — Introducción y verificación inicial (4h)
- Arquitectura INATrace (FE Angular + BE Spring Boot + MySQL) y despliegue con Docker / GitHub Actions.
- Accesos y recorrido de rutas en servidor.
- Healthchecks y estado de contenedores (`docker ps`, `curl http://localhost:8080/actuator/health`, `curl http://localhost:80/health`).
- Script de verificación diaria `check-unocace-daily.sh` (creación y ejecución).
- Ejercicio: correr verificación completa y documentar hallazgos.

### Día 2 (24 feb) — Operaciones y reinicios seguros (4h)
- Reinicio backend controlado (`docker compose restart backend`) y señales de éxito en logs.
- Reinicio completo (down/up) y consideraciones de downtime.
- Manejo de contenedores en estado *unhealthy* o *restarting* (`docker inspect`, recreación con `--force-recreate`).
- Monitoreo de recursos (`docker stats`, `df -h`, `docker system df -v`).
- Frontend: healthcheck, logs Nginx, reinicio de contenedor y verificación de proxy `/api`.

### Día 3 (25 feb) — Respaldos y restauración (4h)
- Respaldo manual de BD (mysqldump comprimido a `/opt/inatrace/backups/mysql/`).
- Verificación de respaldos automáticos (pre-deploy) y retención.
- Integridad de respaldo (`zcat | head`, conteo de `CREATE TABLE`/`INSERT`).
- Restauración guiada **solo en TEST**: backup de seguridad, stop backend, restore, start backend, validación de conteos.
- Buenas prácticas para producción: autorización previa, ventana de mantenimiento y backup previo.

### Día 4 (26 feb) — Troubleshooting (4h)
- Backend no responde: healthcheck, logs recientes, reinicio.
- Conexión MySQL: verificación de contenedor, ping desde backend, revisión de credenciales en `.env`.
- Migraciones Flyway: detección en logs, consulta de `schema_version`, criterios para escalar.
- Rendimiento: uso de CPU/memoria, disco, `SHOW PROCESSLIST`, limpieza de imágenes/logs.
- Subida de archivos y permisos en `/opt/inatrace/uploads/`.
- Frontend: errores 502/página en blanco, revisión de Nginx y conectividad con backend.

### Día 5 (27 feb) — Operaciones, CI/CD y evaluación (4h)
- Flujo de despliegue (GitHub Actions): qué verificar post-deploy (imagen, logs, healthchecks).
- Rollback rápido: selección de tag previo y recreación de backend.
- Mantenimiento preventivo: limpieza de imágenes, rotación de logs grandes, validación de espacio en disco, retención de respaldos.
- Procedimiento de escalamiento: información mínima (logs FE/BE, healthchecks, `docker ps`, uso de disco) y formato de reporte.
- Evaluación práctica: verificación matutina, reinicio controlado, creación y validación de respaldo manual, diagnóstico de incidente simulado.

---

## ✅ Checklist de preparación (antes del 23 feb)
- Acceso SSH y permisos Docker en ambientes de prueba y producción.
- Herramientas en servidor: `jq`, `curl`, `docker compose` disponibles.
- Variables sensibles validadas (.env sin credenciales expuestas en material de clase).
- Directorios de trabajo creados: `/home/usuario/check-unocace-daily.sh` y `/opt/inatrace/backups/mysql/`.

## 📦 Entregables al cierre (27 feb)
- Script de verificación diaria instalado y probado.
- Bitácora de mantenimiento iniciada para UNOCACE.
- Respaldo manual reciente generado y verificado en TEST.
- Checklist de finalización completado (accesos, reinicios, conectividad, respaldo, escalamiento).
- Formato de reporte de incidente listo para uso.

## 🔐 Contactos
- Técnico UNOCACE: [Nombre] - [Email] - [Teléfono]
- Desarrollo: [Contacto desarrollo]
- DevOps: [Contacto DevOps]
- Emergencias: [Teléfono de guardia]

---
**Versión:** 1.1 UNOCACE — Febrero 2026  
**Autor:** Equipo DevOps INATrace
