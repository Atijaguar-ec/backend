# Jenkins Pipeline Troubleshooting Guide
## Deploy-Backend Job Configuration Issues

### 🔴 Problema Actual

**Error:** `NoSuchMethodError: No such DSL method 'sshagent'`

**Causa Raíz:** El job Jenkins NO está leyendo `ci/Jenkinsfile` del repositorio. Está ejecutando un pipeline definido inline o desde otra ubicación.

---

## 🔍 Diagnóstico Paso a Paso

### 1. Verificar Configuración del Job

**Acceso:**
```
Jenkins → Deploy-Backend → Configure
```

**Verificar Sección "Pipeline":**

#### ✅ Configuración CORRECTA (debe verse así):
```
Pipeline Definition: Pipeline script from SCM
SCM: Git
  Repository URL: https://github.com/Atijaguar-ec/backend.git
  Credentials: github-pat (o el que uses)
  Branch Specifier: */main (o el que uses)
Script Path: ci/Jenkinsfile
Lightweight checkout: ☑ (recomendado)
```

#### ❌ Configuración INCORRECTA (si ves esto):
```
Pipeline Definition: Pipeline script
Script: [gran bloque de código Groovy aquí]
```

Si ves la configuración incorrecta, **este es tu problema**.

---

## ✅ Solución Definitiva

### Opción A: Reconfigurar Job Existente

**Paso 1: Backup de la configuración actual**
```bash
# En el servidor Jenkins
sudo cp -r /var/lib/jenkins/jobs/Deploy-Backend \
  /var/lib/jenkins/jobs/Deploy-Backend.backup.$(date +%Y%m%d_%H%M%S)
```

**Paso 2: Modificar configuración del job**

1. Jenkins → Deploy-Backend → **Configure**
2. Ir a sección **Pipeline**
3. Cambiar:
   - **Definition:** `Pipeline script from SCM`
   - **SCM:** `Git`
   - **Repository URL:** `https://github.com/Atijaguar-ec/backend.git`
   - **Credentials:** Seleccionar `github-pat` (o crear si no existe)
   - **Branch Specifier:** `*/main` (para producción) o `*/${BRANCH}` (si usas parámetro)
   - **Script Path:** `ci/Jenkinsfile`
   - ☑ **Lightweight checkout**
4. **Save**

**Paso 3: Validar configuración**
```bash
# Ejecutar build de prueba
# Jenkins → Deploy-Backend → Build with Parameters
# BRANCH: main
# SKIP_TESTS: true (para test rápido)
```

---

### Opción B: Crear Job Nuevo (Recomendado para Fresh Start)

**Paso 1: Exportar credenciales necesarias**

Credenciales requeridas:
- `github-pat` - Personal Access Token de GitHub
- `ghcr-credentials` - Username/Password para GitHub Container Registry
- `fortaleza-env-prod` - Secret File con `.env` de producción
- `fortaleza-env-staging` - Secret File con `.env` de staging
- `usuario-prod-ssh` - SSH Username with private key

**Paso 2: Crear nuevo Pipeline Job**

```groovy
// Jenkins → New Item → "Deploy-Backend-v2" → Pipeline

// En la configuración:
// 1. General
//    ☑ This project is parameterized
//    Copiar parámetros del job anterior o definir:
//    - Choice Parameter: BRANCH (staging, main)
//    - Boolean: SKIP_TESTS (false)
//    - Boolean: SKIP_DB_BACKUP (false)
//    - String: PROD_HOST_PRIMARY (190.15.143.192)
//    - String: PROD_USER_PRIMARY (administrador)
//    - String: PROD_TARGET_PRIMARY (/opt/inatrace/backend/prod/fortaleza)
//    - String: PROD_HEALTH_PORT_PRIMARY (8082)
//    - String: PROD_HEALTH_URL_PRIMARY (http://localhost:8082/actuator/health)
//    - String: PROD_DB_CONTAINER_NAME (inatrace-mysql-prod-fortaleza)
//    - String: STAGING_HEALTH_URL (https://testinatrace.espam.edu.ec/api/actuator/health)

// 2. Pipeline
//    Definition: Pipeline script from SCM
//    SCM: Git
//      Repository URL: https://github.com/Atijaguar-ec/backend.git
//      Credentials: github-pat
//      Branch Specifier: */${BRANCH}
//    Script Path: ci/Jenkinsfile
//    ☑ Lightweight checkout
```

**Paso 3: Validar nuevo job**
```bash
# Test 1: Build staging
Deploy-Backend-v2 → Build with Parameters
  BRANCH: staging
  SKIP_TESTS: true

# Test 2: Build producción (sin ejecutar)
Deploy-Backend-v2 → Build with Parameters
  BRANCH: main
  SKIP_TESTS: false
  # Abortar en approval gate
```

**Paso 4: Si todo funciona, renombrar jobs**
```bash
# En Jenkins UI:
Jenkins → Deploy-Backend → Rename → "Deploy-Backend-OLD"
Jenkins → Deploy-Backend-v2 → Rename → "Deploy-Backend"
```

---

## 🔐 Verificación de Credenciales

### GitHub PAT (github-pat)
```bash
# Jenkins → Manage Jenkins → Credentials → System → Global credentials

# Tipo: Username with password
# ID: github-pat
# Username: tu-usuario-github
# Password: ghp_xxxxxxxxxxxxxxxxxxxxx (token con permisos repo, read:packages)
```

### GHCR Credentials (ghcr-credentials)
```bash
# Tipo: Username with password
# ID: ghcr-credentials
# Username: tu-usuario-github
# Password: ghp_xxxxxxxxxxxxxxxxxxxxx (mismo token o diferente con write:packages)
```

### SSH Key Producción (usuario-prod-ssh)
```bash
# Tipo: SSH Username with private key
# ID: usuario-prod-ssh
# Username: administrador
# Private Key: Enter directly
#   -----BEGIN OPENSSH PRIVATE KEY-----
#   [contenido de la llave privada OpenSSH]
#   -----END OPENSSH PRIVATE KEY-----
# Passphrase: [si la llave tiene passphrase]
```

### Environment Files
```bash
# fortaleza-env-prod (Secret File)
# Upload: .env con configuración de producción

# fortaleza-env-staging (Secret File)
# Upload: .env con configuración de staging
```

---

## 🧪 Pruebas de Validación

### Test 1: Verificar que Jenkins lee el Jenkinsfile correcto
```bash
# Ejecutar build y revisar logs
# Debe aparecer:
# "Checking out Revision 44f9fb3b... (refs/remotes/origin/main)"
# NO debe aparecer error de sshagent
```

### Test 2: Verificar conectividad SSH
```bash
# En el servidor Jenkins
sudo -u jenkins ssh -i /path/to/key administrador@190.15.143.192 "echo OK"
# Debe retornar: OK
```

### Test 3: Build completo staging
```bash
# Deploy-Backend → Build with Parameters
# BRANCH: staging
# SKIP_TESTS: false
# Debe completar hasta "Deploy Fortaleza" sin errores
```

### Test 4: Build completo producción
```bash
# Deploy-Backend → Build with Parameters
# BRANCH: main
# SKIP_TESTS: false
# SKIP_DB_BACKUP: false
# Debe pausar en approval gate
# Aprobar y validar deploy exitoso
```

---

## 📊 Checklist de Validación Final

- [ ] Job configurado como "Pipeline script from SCM"
- [ ] Repository URL correcto: `https://github.com/Atijaguar-ec/backend.git`
- [ ] Branch specifier: `*/${BRANCH}` o `*/main`
- [ ] Script Path: `ci/Jenkinsfile`
- [ ] Credencial `github-pat` configurada y válida
- [ ] Credencial `ghcr-credentials` configurada y válida
- [ ] Credencial `usuario-prod-ssh` configurada con llave OpenSSH
- [ ] Secret Files `fortaleza-env-prod` y `fortaleza-env-staging` subidos
- [ ] Build de staging exitoso
- [ ] Build de producción llega hasta approval gate
- [ ] NO aparece error `NoSuchMethodError: sshagent`

---

## 🚨 Troubleshooting Adicional

### Problema: "Credentials not found"
```bash
# Verificar que el ID de la credencial coincide exactamente
# Jenkins → Credentials → verificar IDs:
# - github-pat
# - ghcr-credentials
# - usuario-prod-ssh
# - fortaleza-env-prod
# - fortaleza-env-staging
```

### Problema: "Permission denied (publickey)"
```bash
# La llave SSH NO es OpenSSH, convertir:
puttygen private-key.ppk -O private-openssh -o id_rsa_prod
# Luego copiar contenido de id_rsa_prod a Jenkins credential
```

### Problema: "Jenkinsfile not found"
```bash
# Verificar path exacto en el repo:
git ls-tree -r main --name-only | grep Jenkinsfile
# Debe retornar: ci/Jenkinsfile

# Ajustar "Script Path" en Jenkins si es diferente
```

---

## 📝 Documentación de Referencia

- **Pipeline Syntax:** https://www.jenkins.io/doc/book/pipeline/syntax/
- **SCM Checkout:** https://www.jenkins.io/doc/book/pipeline/getting-started/#defining-a-pipeline-in-scm
- **SSH Agent Plugin:** https://plugins.jenkins.io/ssh-agent/
- **withCredentials:** https://www.jenkins.io/doc/pipeline/steps/credentials-binding/

---

## ✅ Solución Implementada

Una vez completados todos los pasos:

1. ✅ Job configurado correctamente como "Pipeline from SCM"
2. ✅ Todas las credenciales verificadas y funcionando
3. ✅ Build de staging exitoso
4. ✅ Build de producción funcional hasta approval gate
5. ✅ Deploy SSH a producción funcionando sin errores

**Próximo paso:** Ejecutar deploy completo a producción con aprobación manual.
