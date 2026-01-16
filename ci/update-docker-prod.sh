#!/bin/bash
set -e

echo "==========================================="
echo "  Actualización de Docker en Producción"
echo "==========================================="
echo ""

# Verificar versión actual
echo "📋 Versión actual de Docker:"
docker --version || echo "Docker no encontrado"
docker compose version 2>/dev/null || docker-compose --version 2>/dev/null || echo "docker-compose no encontrado"
echo ""

# Confirmar actualización
read -p "⚠️  ¿Continuar con la actualización? (y/N): " -n 1 -r
echo ""
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    echo "❌ Actualización cancelada"
    exit 1
fi

echo "🔄 Iniciando actualización de Docker..."
echo ""

# Remover versiones antiguas
echo "1️⃣  Removiendo versiones antiguas de Docker..."
sudo apt-get remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true
echo "✅ Versiones antiguas removidas"
echo ""

# Actualizar índice de paquetes e instalar prerequisitos
echo "2️⃣  Instalando prerequisitos..."
sudo apt-get update
sudo apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
echo "✅ Prerequisitos instalados"
echo ""

# Agregar clave GPG de Docker
echo "3️⃣  Agregando clave GPG de Docker..."
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo "✅ Clave GPG agregada"
echo ""

# Configurar repositorio de Docker
echo "4️⃣  Configurando repositorio de Docker..."
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
echo "✅ Repositorio configurado"
echo ""

# Instalar Docker Engine
echo "5️⃣  Instalando Docker Engine (última versión)..."
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
echo "✅ Docker Engine instalado"
echo ""

# Verificar instalación
echo "6️⃣  Verificando instalación..."
docker --version
docker compose version
echo ""

# Asegurar que el servicio esté activo
echo "7️⃣  Configurando servicio Docker..."
sudo systemctl enable docker
sudo systemctl start docker
sudo systemctl status docker --no-pager | head -3
echo "✅ Servicio Docker activo"
echo ""

# Verificar que el usuario administrador esté en el grupo docker
echo "8️⃣  Configurando permisos de usuario..."
sudo usermod -aG docker administrador
echo "✅ Usuario 'administrador' agregado al grupo docker"
echo ""

# Verificar conectividad con daemon
echo "9️⃣  Verificando conectividad con Docker daemon..."
sudo docker ps > /dev/null 2>&1
echo "✅ Docker daemon respondiendo correctamente"
echo ""

echo "==========================================="
echo "  ✅ ACTUALIZACIÓN COMPLETADA"
echo "==========================================="
echo ""
echo "📋 Versiones instaladas:"
docker --version
docker compose version
echo ""
echo "⚠️  IMPORTANTE: Cierra sesión y vuelve a entrar para que los cambios de grupo tomen efecto:"
echo "    exit"
echo "    ssh administrador@10.10.102.26"
echo ""
echo "Después puedes verificar con:"
echo "    docker ps"
echo "    docker compose version"
echo ""
