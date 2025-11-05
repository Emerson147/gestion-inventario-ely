#!/bin/bash

# Script para probar la funcionalidad de la ticketera XPrinter XP-V320M
# Asegúrate de que la aplicación esté ejecutándose en el puerto 8080

BASE_URL="http://localhost:8080/api/comprobantes"
AUTH_TOKEN="" # Agregar el token JWT aquí si es necesario

echo "=== PRUEBAS DE TICKETERA XPRINTER XP-V320M ==="
echo "================================================"

# Función para hacer peticiones con curl
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    local description=$4

    echo ""
    echo "🔍 PRUEBA: $description"
    echo "-------------------------------------------"

    if [ "$method" = "GET" ]; then
        curl -s -X GET \
             -H "Content-Type: application/json" \
             ${AUTH_TOKEN:+-H "Authorization: Bearer $AUTH_TOKEN"} \
             "$BASE_URL$endpoint" | jq '.'
    elif [ "$method" = "POST" ]; then
        if [ -n "$data" ]; then
            curl -s -X POST \
                 -H "Content-Type: application/json" \
                 ${AUTH_TOKEN:+-H "Authorization: Bearer $AUTH_TOKEN"} \
                 -d "$data" \
                 "$BASE_URL$endpoint" | jq '.'
        else
            curl -s -X POST \
                 -H "Content-Type: application/json" \
                 ${AUTH_TOKEN:+-H "Authorization: Bearer $AUTH_TOKEN"} \
                 "$BASE_URL$endpoint" | jq '.'
        fi
    fi

    echo ""
    read -p "Presiona Enter para continuar..."
}

# 1. Verificar conexión con la ticketera
make_request "GET" "/verificar-conexion" "" "Verificar conexión con ticketera"

# 2. Obtener configuración de impresión
make_request "GET" "/configuracion-impresion" "" "Obtener configuración actual"

# 3. Obtener puertos disponibles
make_request "GET" "/puertos-disponibles" "" "Listar puertos disponibles"

# 4. Imprimir ticket de prueba
make_request "POST" "/imprimir-prueba" "" "Imprimir ticket de prueba"

# 5. Cortar papel
echo ""
echo "🔍 PRUEBA: Cortar papel"
echo "-------------------------------------------"
read -p "¿Deseas probar el corte de papel? (y/n): " respuesta
if [ "$respuesta" = "y" ] || [ "$respuesta" = "Y" ]; then
    make_request "POST" "/cortar-papel" "" "Cortar papel"
fi

# 6. Abrir cajón
echo ""
echo "🔍 PRUEBA: Abrir cajón de dinero"
echo "-------------------------------------------"
read -p "¿Deseas probar la apertura del cajón? (y/n): " respuesta
if [ "$respuesta" = "y" ] || [ "$respuesta" = "Y" ]; then
    make_request "POST" "/abrir-cajon" "" "Abrir cajón de dinero"
fi

# 7. Configurar puerto (opcional)
echo ""
echo "🔍 PRUEBA: Configurar puerto"
echo "-------------------------------------------"
read -p "¿Deseas cambiar el puerto de la ticketera? (y/n): " respuesta
if [ "$respuesta" = "y" ] || [ "$respuesta" = "Y" ]; then
    echo "Puertos comunes:"
    echo "- USB (por defecto)"
    echo "- COM1, COM2, COM3... (Windows)"
    echo "- /dev/ttyUSB0, /dev/ttyUSB1... (Linux)"
    echo "- XPrinter XP-V320M (nombre específico)"
    read -p "Ingresa el nuevo puerto: " nuevo_puerto
    make_request "POST" "/configurar-puerto" "{\"puerto\":\"$nuevo_puerto\"}" "Configurar puerto a $nuevo_puerto"
fi

# 8. Probar impresión de comprobante real (si existe)
echo ""
echo "🔍 PRUEBA: Imprimir comprobante real"
echo "-------------------------------------------"
read -p "¿Tienes un ID de comprobante para probar? (y/n): " respuesta
if [ "$respuesta" = "y" ] || [ "$respuesta" = "Y" ]; then
    read -p "Ingresa el ID del comprobante: " comprobante_id

    # Primero mostrar vista previa
    make_request "GET" "/$comprobante_id/vista-previa-ticket" "" "Vista previa del ticket"

    read -p "¿Deseas imprimir este comprobante? (y/n): " confirmar
    if [ "$confirmar" = "y" ] || [ "$confirmar" = "Y" ]; then
        make_request "POST" "/$comprobante_id/imprimir-ticket" "" "Imprimir comprobante $comprobante_id"
    fi
fi

echo ""
echo "=== PRUEBAS COMPLETADAS ==="
echo "============================"
echo ""
echo "📋 RESUMEN DE PRUEBAS:"
echo "1. ✓ Verificación de conexión"
echo "2. ✓ Configuración de impresión"
echo "3. ✓ Puertos disponibles"
echo "4. ✓ Ticket de prueba"
echo "5. ✓ Corte de papel (opcional)"
echo "6. ✓ Apertura de cajón (opcional)"
echo "7. ✓ Configuración de puerto (opcional)"
echo "8. ✓ Impresión de comprobante real (opcional)"
echo ""
echo "🎯 Si todas las pruebas fueron exitosas, tu ticketera XPrinter XP-V320M está funcionando correctamente!"
