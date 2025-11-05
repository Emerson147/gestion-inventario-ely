#!/usr/bin/env python3
"""
Script de pruebas para la ticketera XPrinter XP-V320M
Realiza pruebas completas de todos los endpoints disponibles
"""

import requests
import json
import time
import sys
from typing import Dict, Any, Optional

class TicketeraTester:
    def __init__(self, base_url: str = "http://localhost:8080", auth_token: Optional[str] = None):
        self.base_url = base_url
        self.headers = {
            "Content-Type": "application/json"
        }
        if auth_token:
            self.headers["Authorization"] = f"Bearer {auth_token}"

        self.results = []

    def print_header(self, title: str):
        """Imprime un encabezado formateado"""
        print("\n" + "=" * 60)
        print(f"🖨️  {title}")
        print("=" * 60)

    def print_test(self, test_name: str, success: bool, response: Dict[Any, Any]):
        """Imprime el resultado de una prueba"""
        status = "✅ ÉXITO" if success else "❌ ERROR"
        print(f"\n{status} - {test_name}")
        print("-" * 40)

        if success:
            if 'message' in response:
                print(f"📝 Mensaje: {response['message']}")
            if 'conectada' in response:
                print(f"🔌 Conectada: {'Sí' if response['conectada'] else 'No'}")
            if 'nombreImpresora' in response:
                print(f"🖨️  Impresora: {response['nombreImpresora']}")
            if 'puerto' in response:
                print(f"🔌 Puerto: {response['puerto']}")
        else:
            print(f"💥 Error: {response.get('message', 'Error desconocido')}")

        # Guardar resultado
        self.results.append({
            'test': test_name,
            'success': success,
            'response': response
        })

    def make_request(self, method: str, endpoint: str, data: Optional[Dict] = None) -> tuple[bool, Dict]:
        """Realiza una petición HTTP y retorna éxito y respuesta"""
        try:
            url = f"{self.base_url}/api/comprobantes{endpoint}"

            if method == "GET":
                response = requests.get(url, headers=self.headers, timeout=10)
            elif method == "POST":
                response = requests.post(url, headers=self.headers, json=data, timeout=10)
            else:
                return False, {"message": f"Método HTTP no soportado: {method}"}

            response_data = response.json() if response.content else {}

            if response.status_code == 200:
                return True, response_data
            else:
                return False, {
                    "message": f"HTTP {response.status_code}: {response_data.get('message', 'Error del servidor')}",
                    "status_code": response.status_code
                }

        except requests.exceptions.ConnectionError:
            return False, {"message": "No se pudo conectar al servidor. ¿Está ejecutándose la aplicación?"}
        except requests.exceptions.Timeout:
            return False, {"message": "Timeout: La petición tardó demasiado"}
        except Exception as e:
            return False, {"message": f"Error inesperado: {str(e)}"}

    def test_conexion(self):
        """Prueba la conexión con la ticketera"""
        success, response = self.make_request("GET", "/verificar-conexion")
        self.print_test("Verificar conexión con ticketera", success, response)
        return success

    def test_configuracion(self):
        """Obtiene la configuración actual"""
        success, response = self.make_request("GET", "/configuracion-impresion")
        self.print_test("Obtener configuración de impresión", success, response)

        if success and 'puertosDisponibles' in response:
            puertos = response['puertosDisponibles']
            print(f"📋 Puertos disponibles: {', '.join(puertos) if puertos else 'Ninguno'}")

        return success

    def test_puertos_disponibles(self):
        """Lista los puertos disponibles"""
        success, response = self.make_request("GET", "/puertos-disponibles")
        self.print_test("Listar puertos disponibles", success, response)

        if success and 'puertos' in response:
            puertos = response['puertos']
            print(f"🔌 Puertos encontrados: {len(puertos)}")
            for i, puerto in enumerate(puertos, 1):
                print(f"   {i}. {puerto}")

        return success

    def test_ticket_prueba(self):
        """Imprime un ticket de prueba"""
        print("\n⚠️  ATENCIÓN: Esta prueba enviará un ticket de prueba a la impresora")
        respuesta = input("¿Continuar? (s/N): ").lower()

        if respuesta != 's':
            print("⏭️  Prueba omitida por el usuario")
            return True

        success, response = self.make_request("POST", "/imprimir-prueba")
        self.print_test("Imprimir ticket de prueba", success, response)

        if success:
            print("📄 El ticket de prueba debería estar imprimiéndose ahora")

        return success

    def test_cortar_papel(self):
        """Prueba el corte de papel"""
        print("\n⚠️  ATENCIÓN: Esta prueba cortará el papel de la ticketera")
        respuesta = input("¿Continuar? (s/N): ").lower()

        if respuesta != 's':
            print("⏭️  Prueba omitida por el usuario")
            return True

        success, response = self.make_request("POST", "/cortar-papel")
        self.print_test("Cortar papel", success, response)
        return success

    def test_abrir_cajon(self):
        """Prueba la apertura del cajón"""
        print("\n⚠️  ATENCIÓN: Esta prueba abrirá el cajón de dinero")
        respuesta = input("¿Continuar? (s/N): ").lower()

        if respuesta != 's':
            print("⏭️  Prueba omitida por el usuario")
            return True

        success, response = self.make_request("POST", "/abrir-cajon")
        self.print_test("Abrir cajón de dinero", success, response)
        return success

    def test_configurar_puerto(self):
        """Prueba la configuración de puerto"""
        print("\n🔧 Configuración de puerto")
        print("Puertos comunes:")
        print("  • USB (por defecto)")
        print("  • XPrinter XP-V320M (nombre específico)")
        print("  • COM1, COM2, COM3... (Windows)")
        print("  • /dev/ttyUSB0, /dev/ttyUSB1... (Linux)")

        respuesta = input("¿Deseas cambiar el puerto? (s/N): ").lower()

        if respuesta != 's':
            print("⏭️  Configuración omitida por el usuario")
            return True

        nuevo_puerto = input("Ingresa el nuevo puerto: ").strip()
        if not nuevo_puerto:
            print("❌ Puerto vacío, omitiendo prueba")
            return True

        success, response = self.make_request("POST", "/configurar-puerto", {"puerto": nuevo_puerto})
        self.print_test(f"Configurar puerto a '{nuevo_puerto}'", success, response)
        return success

    def test_vista_previa_comprobante(self):
        """Prueba la vista previa de un comprobante"""
        comprobante_id = input("\n📄 Ingresa el ID de un comprobante para vista previa (Enter para omitir): ").strip()

        if not comprobante_id:
            print("⏭️  Vista previa omitida por el usuario")
            return True

        try:
            comprobante_id = int(comprobante_id)
        except ValueError:
            print("❌ ID inválido, debe ser un número")
            return False

        success, response = self.make_request("GET", f"/{comprobante_id}/vista-previa-ticket")
        self.print_test(f"Vista previa comprobante #{comprobante_id}", success, response)

        if success and 'contenido' in response:
            print("\n📄 Vista previa del ticket:")
            print("-" * 40)
            print(response['contenido'])
            print("-" * 40)

        return success

    def test_imprimir_comprobante(self):
        """Prueba la impresión de un comprobante real"""
        comprobante_id = input("\n🖨️  Ingresa el ID de un comprobante para imprimir (Enter para omitir): ").strip()

        if not comprobante_id:
            print("⏭️  Impresión omitida por el usuario")
            return True

        try:
            comprobante_id = int(comprobante_id)
        except ValueError:
            print("❌ ID inválido, debe ser un número")
            return False

        print(f"\n⚠️  ATENCIÓN: Esta prueba imprimirá el comprobante #{comprobante_id}")
        respuesta = input("¿Continuar? (s/N): ").lower()

        if respuesta != 's':
            print("⏭️  Impresión omitida por el usuario")
            return True

        success, response = self.make_request("POST", f"/{comprobante_id}/imprimir-ticket")
        self.print_test(f"Imprimir comprobante #{comprobante_id}", success, response)
        return success

    def run_all_tests(self):
        """Ejecuta todas las pruebas"""
        self.print_header("PRUEBAS DE TICKETERA XPRINTER XP-V320M")

        print(f"🌐 Servidor: {self.base_url}")
        print(f"🔑 Autenticación: {'Configurada' if 'Authorization' in self.headers else 'Sin configurar'}")

        # Lista de pruebas
        tests = [
            ("Verificar conexión", self.test_conexion),
            ("Configuración actual", self.test_configuracion),
            ("Puertos disponibles", self.test_puertos_disponibles),
            ("Ticket de prueba", self.test_ticket_prueba),
            ("Cortar papel", self.test_cortar_papel),
            ("Abrir cajón", self.test_abrir_cajon),
            ("Configurar puerto", self.test_configurar_puerto),
            ("Vista previa comprobante", self.test_vista_previa_comprobante),
            ("Imprimir comprobante", self.test_imprimir_comprobante),
        ]

        # Ejecutar pruebas
        for test_name, test_func in tests:
            try:
                test_func()
            except KeyboardInterrupt:
                print("\n\n⏹️  Pruebas interrumpidas por el usuario")
                break
            except Exception as e:
                print(f"\n💥 Error inesperado en {test_name}: {e}")

            time.sleep(1)  # Pausa entre pruebas

        # Resumen final
        self.print_summary()

    def print_summary(self):
        """Imprime un resumen de todas las pruebas"""
        self.print_header("RESUMEN DE PRUEBAS")

        total_tests = len(self.results)
        successful_tests = len([r for r in self.results if r['success']])
        failed_tests = total_tests - successful_tests

        print(f"📊 Total de pruebas: {total_tests}")
        print(f"✅ Exitosas: {successful_tests}")
        print(f"❌ Fallidas: {failed_tests}")
        print(f"📈 Porcentaje de éxito: {(successful_tests/total_tests*100):.1f}%" if total_tests > 0 else "N/A")

        if failed_tests > 0:
            print(f"\n❌ Pruebas fallidas:")
            for result in self.results:
                if not result['success']:
                    print(f"   • {result['test']}: {result['response'].get('message', 'Error desconocido')}")

        print(f"\n🎯 {'¡Ticketera funcionando correctamente!' if failed_tests == 0 else 'Hay problemas que resolver'}")

def main():
    """Función principal"""
    print("🖨️  SISTEMA DE PRUEBAS PARA TICKETERA XPRINTER XP-V320M")
    print("=" * 60)

    # Configuración
    base_url = input("URL del servidor (Enter para http://localhost:8080): ").strip()
    if not base_url:
        base_url = "http://localhost:8080"

    auth_token = input("Token JWT (Enter para omitir): ").strip()
    if not auth_token:
        auth_token = None

    # Crear instancia del tester
    tester = TicketeraTester(base_url, auth_token)

    # Ejecutar pruebas
    try:
        tester.run_all_tests()
    except KeyboardInterrupt:
        print("\n\n👋 ¡Hasta luego!")
        sys.exit(0)

if __name__ == "__main__":
    main()
