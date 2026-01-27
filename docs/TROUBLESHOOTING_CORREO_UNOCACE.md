# Guía de Troubleshooting: Problemas de Envío de Correo - INATrace UNOCACE

---

## 🎯 Objetivo

Esta guía te ayudará a diagnosticar y resolver problemas relacionados con el envío de correos electrónicos en INATrace UNOCACE.

---

## 📧 ¿Cuándo se envían correos en INATrace?

El sistema envía correos en los siguientes casos:
1. **Registro de nuevo usuario** → Correo de confirmación de cuenta
2. **Recuperación de contraseña** → Correo con enlace para resetear contraseña
3. **Notificaciones del sistema** → Alertas y notificaciones a usuarios

---

## ⚙️ Configuración de Correo en UNOCACE

### Variables de Entorno Requeridas

El sistema usa las siguientes variables de entorno para configurar el correo:

```bash
# Servidor SMTP
SPRING_MAIL_HOST=mail.unocace.com          # Servidor SMTP
SPRING_MAIL_PORT=587                        # UNOCACE usa STARTTLS (puerto 587)
SPRING_MAIL_USERNAME=inatrace@unocace.com  # Usuario SMTP
SPRING_MAIL_PASSWORD=***********           # Contraseña SMTP

# Configuración SMTP
SPRING_MAIL_SMTP_AUTH=true                 # Autenticación requerida
SPRING_MAIL_SMTP_SSL_ENABLE=false          # SSL directo (puerto 465) deshabilitado
SPRING_MAIL_STARTTLS_ENABLE=true           # STARTTLS habilitado (puerto 587)

# Timeouts (milisegundos)
SPRING_MAIL_SMTP_CONNECTION_TIMEOUT=5000   # Timeout de conexión
SPRING_MAIL_SMTP_TIMEOUT=3000              # Timeout de lectura
SPRING_MAIL_SMTP_WRITETIMEOUT=5000         # Timeout de escritura

# Debug (solo para diagnóstico)
SPRING_MAIL_DEBUG=false                    # Logs detallados SMTP

# Configuración INATrace
INATRACE_MAIL_TEMPLATE_FROM=inatrace@unocace.com  # Remitente
INATRACE_MAIL_SENDING_ENABLED=true                # Envío habilitado
INATRACE_MAIL_REDIRECT=                           # Redirigir correos (vacío en prod)
```

---

## 🔍 Diagnóstico Paso a Paso

### Paso 1: Verificar que el envío de correo está habilitado

```bash
# Conectarse al servidor
ssh [usuario]@[servidor-unocace]

# Verificar variable INATRACE_MAIL_SENDING_ENABLED
docker exec inatrace-be-prod-unocace env | grep INATRACE_MAIL_SENDING_ENABLED

# Debe mostrar: INATRACE_MAIL_SENDING_ENABLED=true
```

**Si muestra `false`:**
```bash
cd /opt/inatrace/backend
nano .env
# Cambiar: INATRACE_MAIL_SENDING_ENABLED=true
# Guardar: Ctrl+O, Enter, Ctrl+X

# Reiniciar backend
docker compose restart backend
sleep 60
curl -s http://localhost:8080/actuator/health | jq
```

---

### Paso 2: Verificar configuración SMTP

```bash
# Ver todas las variables de correo
docker exec inatrace-be-prod-unocace env | grep SPRING_MAIL

# Verificar que NO estén vacías:
# - SPRING_MAIL_HOST
# - SPRING_MAIL_PORT
# - SPRING_MAIL_USERNAME
# - SPRING_MAIL_PASSWORD (aparecerá oculta)
```

**Variables críticas que NO deben estar vacías:**
- `SPRING_MAIL_HOST` → Servidor SMTP
- `SPRING_MAIL_PORT` → Puerto (465 o 587)
- `SPRING_MAIL_USERNAME` → Usuario
- `SPRING_MAIL_PASSWORD` → Contraseña

---

### Paso 3: Verificar conectividad al servidor SMTP

```bash
# Opción 1: Usar curl (disponible en el contenedor) - forzar IPv4
docker exec inatrace-be-prod-unocace curl -4 -v telnet://mail.unocace.com:587 --max-time 5

# Opción 2: Verificar desde el host (fuera del contenedor)
curl -4 -v telnet://mail.unocace.com:587 --max-time 5

# Opción 3: Verificar DNS (si nslookup no existe, usar getent)
docker exec inatrace-be-prod-unocace getent hosts mail.unocace.com

# Opción 4: Ping al servidor (solo verifica que existe)
docker exec inatrace-be-prod-unocace ping -c 3 mail.unocace.com
```

**Resultado esperado con curl:**
```
* Connected to mail.unocace.com (X.X.X.X) port 587 (#0)
220-gtxm1104.siteground.biz ESMTP
```

**Si curl muestra "Connection refused" o timeout:**
- Verificar firewall del servidor
- Verificar que el puerto SMTP esté abierto
- Verificar DNS (que resuelva mail.unocace.com)
- Contactar al administrador de correo de UNOCACE

---

## ✅ Caso Resuelto UNOCACE (SiteGround)

**Síntomas:** no se enviaban correos desde producción; el puerto 465 daba timeout.

**Diagnóstico real:**
- `mail.unocace.com:465` → timeout desde host y contenedor.
- `mail.unocace.com:587` → responde con `220-gtxm1104.siteground.biz ESMTP`.

**Solución aplicada:**
1. Cambiar a puerto 587 con STARTTLS.
2. Deshabilitar SSL directo.
3. **Recrear contenedor** para aplicar las variables actualizadas.

```bash
# Actualizar .env
SPRING_MAIL_PORT=587
SPRING_MAIL_SMTP_SSL_ENABLE=false
SPRING_MAIL_STARTTLS_ENABLE=true
SPRING_MAIL_DEBUG=true  # solo para diagnóstico

# Re-crear contenedor para aplicar variables
cd /opt/inatrace/backend
docker compose down
docker compose up -d

# Verificar que el contenedor usa 587
docker exec inatrace-be-prod-unocace env | grep SPRING_MAIL
```

**Nota importante:** `docker compose restart` **no aplica** nuevas variables si el contenedor ya está creado. Usa `down/up` o `up -d --force-recreate`.

---

### Paso 4: Habilitar logs de debug SMTP

Para ver exactamente qué está pasando con el servidor SMTP:

```bash
cd /opt/inatrace/backend
nano .env

# Agregar o cambiar:
SPRING_MAIL_DEBUG=true

# Guardar y reiniciar
docker compose restart backend
```

**Ver logs con debug habilitado:**
```bash
# Seguir logs en tiempo real
docker logs -f inatrace-be-prod-unocace

# Buscar errores SMTP
docker logs inatrace-be-prod-unocace --since 10m | grep -i "mail\|smtp"
```

**⚠️ IMPORTANTE:** Desactivar debug después de diagnosticar (genera muchos logs):
```bash
SPRING_MAIL_DEBUG=false
```

---

### Paso 5: Verificar logs del backend

```bash
# Ver errores relacionados con correo
docker logs inatrace-be-prod-unocace --since 1h | grep -i "mail\|smtp\|email" | tail -50

# Buscar excepciones de correo
docker logs inatrace-be-prod-unocace --since 1h | grep -A 10 "MailException\|MessagingException"
```

---

## 🚨 Errores Comunes y Soluciones

### Error 1: "Mail sending is disabled"

**Síntoma en logs:**
```
Mail sending is disabled, skipping email
```

**Causa:** `INATRACE_MAIL_SENDING_ENABLED=false`

**Solución:**
```bash
cd /opt/inatrace/backend
nano .env
# Cambiar: INATRACE_MAIL_SENDING_ENABLED=true
docker compose restart backend
```

---

### Error 2: "Could not connect to SMTP host"

**Síntoma en logs:**
```
javax.mail.MessagingException: Could not connect to SMTP host: mail.unocace.com, port: 465
```

**Causas posibles:**
1. Servidor SMTP no accesible
2. Puerto bloqueado por firewall
3. Credenciales incorrectas

**Diagnóstico:**
```bash
# Verificar conectividad con curl
docker exec inatrace-be-prod-unocace curl -v telnet://mail.unocace.com:465 --max-time 5

# Verificar DNS
docker exec inatrace-be-prod-unocace nslookup mail.unocace.com

# Verificar configuración
docker exec inatrace-be-prod-unocace env | grep SPRING_MAIL_HOST
docker exec inatrace-be-prod-unocace env | grep SPRING_MAIL_PORT
```

**Solución:**
1. Verificar que el servidor SMTP esté corriendo
2. Verificar firewall del servidor
3. Contactar al administrador de correo de UNOCACE

---

### Error 3: "Authentication failed"

**Síntoma en logs:**
```
javax.mail.AuthenticationFailedException: 535 Authentication failed
```

**Causa:** Usuario o contraseña incorrectos

**Solución:**
```bash
cd /opt/inatrace/backend
nano .env

# Verificar y corregir:
SPRING_MAIL_USERNAME=inatrace@unocace.com
SPRING_MAIL_PASSWORD=[contraseña correcta]

# Guardar y reiniciar
docker compose restart backend
```

**Verificar credenciales con el administrador de correo de UNOCACE**

---

### Error 4: "Connection timeout"

**Síntoma en logs:**
```
javax.mail.MessagingException: Connection timed out
```

**Causas posibles:**
1. Servidor SMTP lento o no responde
2. Timeouts muy cortos
3. Problemas de red

**Solución:**
```bash
cd /opt/inatrace/backend
nano .env

# Aumentar timeouts (valores en milisegundos)
SPRING_MAIL_SMTP_CONNECTION_TIMEOUT=10000
SPRING_MAIL_SMTP_TIMEOUT=10000
SPRING_MAIL_SMTP_WRITETIMEOUT=10000

# Guardar y reiniciar
docker compose restart backend
```

---

### Error 5: "SSL/TLS handshake failed"

**Síntoma en logs:**
```
javax.net.ssl.SSLHandshakeException: Received fatal alert: handshake_failure
```

**Causa:** Configuración incorrecta de SSL/TLS

**Solución según el puerto:**

**Para puerto 465 (SSL directo):**
```bash
SPRING_MAIL_PORT=465
SPRING_MAIL_SMTP_SSL_ENABLE=true
SPRING_MAIL_STARTTLS_ENABLE=false
```

**Para puerto 587 (STARTTLS):**
```bash
SPRING_MAIL_PORT=587
SPRING_MAIL_SMTP_SSL_ENABLE=false
SPRING_MAIL_STARTTLS_ENABLE=true
```

---

### Error 6: "Sender address rejected"

**Síntoma en logs:**
```
550 Sender address rejected
```

**Causa:** Dirección de remitente no autorizada

**Solución:**
```bash
cd /opt/inatrace/backend
nano .env

# Verificar que el remitente sea válido
INATRACE_MAIL_TEMPLATE_FROM=inatrace@unocace.com
SPRING_MAIL_USERNAME=inatrace@unocace.com

# Deben coincidir o ser del mismo dominio
```

---

## 🧪 Prueba Manual de Envío de Correo

### Opción 1: Probar desde la aplicación

1. Ir a la página de registro: `https://inatrace.unocace.com/register`
2. Registrar un nuevo usuario con tu email de prueba
3. Verificar que llegue el correo de confirmación

### Opción 2: Probar con telnet (avanzado)

```bash
# Conectarse al servidor SMTP
telnet mail.unocace.com 25

# Comandos SMTP (escribir uno por uno):
EHLO inatrace.unocace.com
MAIL FROM: inatrace@unocace.com
RCPT TO: tu_email@ejemplo.com
DATA
Subject: Test

Este es un correo de prueba.
.
QUIT
```

---

## 📋 Checklist de Verificación

Usa este checklist para diagnosticar problemas de correo:

- [ ] `INATRACE_MAIL_SENDING_ENABLED=true`
- [ ] `SPRING_MAIL_HOST` configurado (no vacío)
- [ ] `SPRING_MAIL_PORT` configurado (465 o 587)
- [ ] `SPRING_MAIL_USERNAME` configurado
- [ ] `SPRING_MAIL_PASSWORD` configurado
- [ ] Conectividad al servidor SMTP (ping/telnet)
- [ ] Puerto SMTP accesible (no bloqueado por firewall)
- [ ] Credenciales SMTP correctas
- [ ] Configuración SSL/TLS correcta según puerto
- [ ] Backend reiniciado después de cambios
- [ ] Logs revisados para errores específicos

---

## 📊 Comandos de Referencia Rápida

```bash
# Ver configuración de correo
docker exec inatrace-be-prod-unocace env | grep -E "MAIL|SMTP"

# Verificar conectividad SMTP
docker exec inatrace-be-prod-unocace curl -v telnet://mail.unocace.com:465 --max-time 5

# Ver logs de correo
docker logs inatrace-be-prod-unocace --since 1h | grep -i "mail\|smtp"

# Habilitar debug SMTP
cd /opt/inatrace/backend
echo "SPRING_MAIL_DEBUG=true" >> .env
docker compose restart backend

# Ver logs en tiempo real
docker logs -f inatrace-be-prod-unocace | grep -i mail

# Desactivar debug
cd /opt/inatrace/backend
sed -i 's/SPRING_MAIL_DEBUG=true/SPRING_MAIL_DEBUG=false/' .env
docker compose restart backend
```

---

## 🔧 Configuración Típica de UNOCACE

### Producción
```bash
SPRING_MAIL_HOST=mail.unocace.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=inatrace@unocace.com
SPRING_MAIL_PASSWORD=[proporcionado por admin]
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_SSL_ENABLE=true
SPRING_MAIL_STARTTLS_ENABLE=false
INATRACE_MAIL_TEMPLATE_FROM=inatrace@unocace.com
INATRACE_MAIL_SENDING_ENABLED=true
```

### Test
```bash
SPRING_MAIL_HOST=mail.unocace.com
SPRING_MAIL_PORT=465
SPRING_MAIL_USERNAME=inatrace@unocace.com
SPRING_MAIL_PASSWORD=[proporcionado por admin]
SPRING_MAIL_SMTP_AUTH=true
SPRING_MAIL_SMTP_SSL_ENABLE=true
SPRING_MAIL_STARTTLS_ENABLE=false
INATRACE_MAIL_TEMPLATE_FROM=inatrace@unocace.com
INATRACE_MAIL_SENDING_ENABLED=true
```

---

## 📞 Escalamiento

### Escalar al administrador de correo de UNOCACE si:
- No puedes conectarte al servidor SMTP
- Las credenciales no funcionan
- El servidor SMTP rechaza los correos
- Necesitas cambiar configuración del servidor de correo

### Escalar a desarrollo si:
- Los logs muestran errores de código Java
- El problema persiste después de verificar toda la configuración
- Necesitas modificar templates de correo
- Hay errores en la lógica de envío de correos

---

## 📝 Información de Contacto

**Administrador de Correo UNOCACE**: [Nombre] - [Email] - [Teléfono]  
**Técnico Helpdesk**: [Tu nombre] - [Email]  
**Desarrollo**: [Contacto desarrollo]

---

## 🔗 Documentación Relacionada

- **Plan de Capacitación**: `docs/PLAN_CAPACITACION_UNOCACE_HELPDESK.md`
- **Guía Rápida**: `docs/GUIA_RAPIDA_UNOCACE.md`
- **Troubleshooting General**: `TROUBLESHOOTING-es.md`
- **README**: `README-es.md`

---

**Versión**: 1.0  
**Última actualización**: Enero 2025  
**Autor**: Equipo DevOps INATrace UNOCACE
