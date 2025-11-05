#!/usr/bin/env python3
"""
Script para verificar comprobantes existentes y crear datos de prueba
"""

import requests
import json
from datetime import datetime

BASE_URL = "http://localhost:8080"

def check_existing_comprobantes():
    """Verifica qué comprobantes existen"""
    try:
        response = requests.get(f"{BASE_URL}/api/comprobantes")
        if response.status_code == 200:
            comprobantes = response.json()
            print(f"📄 Comprobantes encontrados: {len(comprobantes)}")

            if comprobantes:
                print("\n💼 Lista de comprobantes disponibles:")
                print("-" * 60)
                for comp in comprobantes:
                    print(f"ID: {comp['id']} | {comp['tipoDocumento']} {comp['serie']}-{comp['numero']} | Total: S/ {comp['total']} | Cliente: {comp['cliente']['nombres']} {comp['cliente']['apellidos']}")
                return comprobantes
            else:
                print("❌ No hay comprobantes en el sistema")
                return []
        else:
            print(f"❌ Error al obtener comprobantes: {response.status_code}")
            return []
    except Exception as e:
        print(f"💥 Error: {e}")
        return []

def check_existing_ventas():
    """Verifica qué ventas existen"""
    try:
        response = requests.get(f"{BASE_URL}/api/ventas")
        if response.status_code == 200:
            ventas = response.json()
            print(f"\n🛒 Ventas encontradas: {len(ventas)}")

            if ventas:
                print("\n💰 Lista de ventas disponibles:")
                print("-" * 60)
                for venta in ventas:
                    print(f"ID: {venta['id']} | #{venta['numeroVenta']} | Total: S/ {venta['total']} | Estado: {venta['estado']} | Cliente: {venta['cliente']['nombres']} {venta['cliente']['apellidos']}")
                return ventas
            else:
                print("❌ No hay ventas en el sistema")
                return []
        else:
            print(f"❌ Error al obtener ventas: {response.status_code}")
            return []
    except Exception as e:
        print(f"💥 Error: {e}")
        return []

def test_print_comprobante(comprobante_id):
    """Prueba imprimir un comprobante específico"""
    print(f"\n🖨️  Probando impresión del comprobante ID: {comprobante_id}")

    # Primero, obtener vista previa
    try:
        response = requests.get(f"{BASE_URL}/api/comprobantes/{comprobante_id}/vista-previa-ticket")
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                print("✅ Vista previa generada correctamente")
                print("\n📄 Contenido del ticket:")
                print("=" * 50)
                print(data['contenido'])
                print("=" * 50)

                # Preguntar si imprimir
                respuesta = input("\n¿Deseas imprimir este comprobante? (s/N): ").lower()
                if respuesta == 's':
                    print_response = requests.post(f"{BASE_URL}/api/comprobantes/{comprobante_id}/imprimir-ticket")
                    if print_response.status_code == 200:
                        print_data = print_response.json()
                        if print_data.get('success'):
                            print("✅ ¡Comprobante enviado a impresión exitosamente!")
                        else:
                            print(f"❌ Error al imprimir: {print_data.get('message')}")
                    else:
                        print(f"❌ Error HTTP al imprimir: {print_response.status_code}")
                else:
                    print("⏭️  Impresión cancelada")
            else:
                print(f"❌ Error en vista previa: {data.get('message')}")
        else:
            print(f"❌ Error HTTP en vista previa: {response.status_code}")
    except Exception as e:
        print(f"💥 Error: {e}")

def create_sample_data():
    """Crear datos de ejemplo si no existen"""
    print("\n🔧 ¿Deseas crear datos de ejemplo para pruebas?")
    print("Esto creará:")
    print("  • Un cliente de ejemplo")
    print("  • Una venta con productos")
    print("  • Un comprobante asociado")

    respuesta = input("¿Continuar? (s/N): ").lower()
    if respuesta != 's':
        return

    # Crear cliente
    cliente_data = {
        "nombres": "Juan Carlos",
        "apellidos": "Pérez González",
        "dni": "12345678",
        "telefono": "987654321",
        "email": "juan.perez@email.com",
        "direccion": "Av. Ejemplo 123",
        "estado": True
    }

    try:
        response = requests.post(f"{BASE_URL}/api/clientes", json=cliente_data)
        if response.status_code == 201:
            cliente = response.json()
            print(f"✅ Cliente creado: ID {cliente['id']}")

            # Ahora necesitarías crear productos, inventario, etc.
            # Por simplicidad, te mostraré cómo usar IDs existentes
            print("📝 Para crear una venta, necesitas:")
            print("  • ID de cliente (ya creado)")
            print("  • ID de usuario (debe existir)")
            print("  • IDs de inventario con productos")

        else:
            print(f"❌ Error al crear cliente: {response.status_code}")
    except Exception as e:
        print(f"💥 Error: {e}")

def main():
    print("🖨️  SISTEMA DE PRUEBAS CON COMPROBANTES REALES")
    print("=" * 60)

    # Verificar comprobantes existentes
    comprobantes = check_existing_comprobantes()

    # Verificar ventas existentes
    ventas = check_existing_ventas()

    if comprobantes:
        # Si hay comprobantes, permitir imprimir uno
        print(f"\n🎯 Puedes imprimir cualquiera de los {len(comprobantes)} comprobantes listados")

        try:
            comprobante_id = input("Ingresa el ID del comprobante a imprimir (Enter para salir): ").strip()
            if comprobante_id:
                test_print_comprobante(int(comprobante_id))
        except ValueError:
            print("❌ ID inválido")
        except KeyboardInterrupt:
            print("\n👋 ¡Hasta luego!")

    elif ventas:
        # Si hay ventas pero no comprobantes, ofrecer crear comprobante
        print("\n💡 Tienes ventas pero no comprobantes.")
        print("Puedes generar un comprobante desde una venta existente.")

        try:
            venta_id = input("Ingresa el ID de la venta para generar comprobante (Enter para salir): ").strip()
            if venta_id:
                # Generar comprobante
                comprobante_data = {
                    "ventaId": int(venta_id),
                    "tipoDocumento": "BOLETA",
                    "observaciones": "Comprobante generado para prueba de impresión"
                }

                response = requests.post(f"{BASE_URL}/api/comprobantes", json=comprobante_data)
                if response.status_code == 201:
                    comprobante = response.json()
                    print(f"✅ Comprobante creado: ID {comprobante['id']}")
                    test_print_comprobante(comprobante['id'])
                else:
                    print(f"❌ Error al crear comprobante: {response.status_code}")
        except ValueError:
            print("❌ ID inválido")
        except KeyboardInterrupt:
            print("\n👋 ¡Hasta luego!")

    else:
        # No hay datos, ofrecer crear datos de ejemplo
        create_sample_data()

if __name__ == "__main__":
    main()
