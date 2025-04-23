# <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Athletic%20Shoe.png" alt="Zapato Deportivo" width="35" /> Sistema de Gestión La Peruanita <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/High-Heeled%20Shoe.png" alt="Tacón" width="35" />

![Versión](https://img.shields.io/badge/versión-1.0.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![Frontend](https://img.shields.io/badge/Frontend-En%20desarrollo-orange.svg)
![Database](https://img.shields.io/badge/MySQL-8.0-4479A1.svg)

<div align="center">
  <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Hand%20gestures/Waving%20Hand.png" alt="Mano Saludando" width="100" />
  <h3>¡Bienvenido a La Peruanita - Gestión Integral de Calzado!</h3>
</div>

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Descripción General

La Peruanita es un sistema completo de ventas y gestión para calzado de hombres y mujeres, desarrollado con Spring Boot en el backend y MySQL como base de datos. Especializados en zapatillas deportivas como la marca Punto V, así como calzado femenino (tacones, zapatos de vestir y más), nuestro sistema ofrece una gestión de inventario intuitiva con una estructura jerárquica: productos → colores → tallas.

> **Nota:** El frontend está actualmente en desarrollo y se implementará próximamente con Angular 19.

<div align="center">
  <img src="https://i.imgur.com/TQEiZ3a.gif" alt="Desarrollo" width="280" />
  <p><i>Backend en funcionamiento - Frontend en construcción</i></p>
</div>

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Características Principales

- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Locked.png" alt="Bloqueado" width="20" /> **Autenticación Segura** - Sistema de control de acceso basado en roles
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Package.png" alt="Paquete" width="20" /> **Gestión de Productos** - Crear, actualizar y eliminar productos de calzado
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Artist%20Palette.png" alt="Paleta de Artista" width="20" /> **Variantes de Color** - Gestión de múltiples opciones de color por producto
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Straight%20Ruler.png" alt="Regla" width="20" /> **Gestión de Tallas** - Seguimiento de inventario por talla para cada variante de color
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Money%20Bag.png" alt="Bolsa de Dinero" width="20" /> **Procesamiento de Ventas** - Sistema completo de punto de venta con recibos
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Chart%20Increasing.png" alt="Gráfico Creciente" width="20" /> **Panel de Informes** - Análisis de ventas y perspectivas de inventario

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Flujo Lógico del Producto

<div align="center">
  <p>Estructura de Gestión de Calzado</p>
  
  <table align="center" border="0">
    <tr>
      <td align="center"><img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Athletic%20Shoe.png" alt="Zapatilla" width="50" /><br>Zapatillas Deportivas</td>
      <td align="center"><img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/High-Heeled%20Shoe.png" alt="Tacón" width="50" /><br>Tacones</td>
      <td align="center"><img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Man's%20Shoe.png" alt="Zapato de Vestir" width="50" /><br>Zapatos de Vestir</td>
    </tr>
  </table>
</div>

```
Producto (Ej: Zapatilla Punto V Running)
  ├── Color 1 (Negro)
  │     ├── Talla 38
  │     ├── Talla 39
  │     └── Talla 40
  │
  ├── Color 2 (Blanco)
  │     ├── Talla 38 
  │     ├── Talla 39
  │     └── Talla 41
  │
  └── Color 3 (Rojo)
        ├── Talla 37
        └── Talla 40
```

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Tecnologías

### Backend (Implementado)
- **Spring Boot** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **MySQL 8.0** - Base de datos relacional
- **Lombok** - Reducción de código repetitivo
- **Swagger/OpenAPI** - Documentación de API

### Frontend (En desarrollo)
- **Angular 19** - Framework frontend (Próximamente)

<div align="center">
  <img src="https://i.imgur.com/eU10C8A.gif" alt="Desarrollo Frontend" width="280" />
  <p><i>Frontend en proceso de desarrollo</i></p>
</div>

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Instalación y Configuración

### Prerrequisitos
- Java 17 o superior
- MySQL 8.0+
- Maven 3.8+

### Configuración del Backend
```bash
# Clonar el repositorio
git clone https://github.com/Emerson147/laperuanita.git

# Navegar al directorio del backend
cd laperuanita/backend

# Instalar dependencias
mvn install

# Ejecutar aplicación
mvn spring-boot:run
```

### Configuración de Base de Datos
```sql
-- Crear la base de datos
CREATE DATABASE laperuanita;

-- Usar la base de datos
USE laperuanita;

-- Las tablas se crearán automáticamente mediante JPA/Hibernate
```

La API estará disponible en `http://localhost:8080`

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Estructura del Proyecto

### Estructura del Backend
```
src/
├── main/
│   ├── java/com/laperuanita/
│   │   ├── config/        # Clases de configuración
│   │   ├── controller/    # Controladores REST
│   │   ├── dto/           # Objetos de Transferencia de Datos
│   │   ├── entity/        # Modelos de entidad
│   │   │   ├── Producto.java
│   │   │   ├── ColorVariante.java
│   │   │   ├── Talla.java
│   │   │   ├── Venta.java
│   │   │   └── Usuario.java
│   │   ├── exception/     # Excepciones personalizadas
│   │   ├── repository/    # Repositorios de datos
│   │   ├── service/       # Lógica de negocio
│   │   └── util/          # Clases utilitarias
│   └── resources/
│       ├── application.properties
│       └── data.sql       # Datos iniciales
└── test/                  # Pruebas unitarias e integración
```

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Modelo de Datos

<div align="center">
  <img src="https://i.imgur.com/XMzTKOA.png" alt="Diagrama ER" width="600" />
  <p><i>Diagrama de Entidad-Relación de La Peruanita</i></p>
</div>

### Estructura de Base de Datos (MySQL)

```sql
CREATE TABLE productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    marca VARCHAR(50) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    precio_base DECIMAL(10, 2) NOT NULL,
    descripcion TEXT,
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

CREATE TABLE color_variantes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    producto_id BIGINT NOT NULL,
    nombre_color VARCHAR(50) NOT NULL,
    codigo_hex VARCHAR(7) NOT NULL,
    imagen_principal VARCHAR(255),
    FOREIGN KEY (producto_id) REFERENCES productos(id)
);

CREATE TABLE tallas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    color_id BIGINT NOT NULL,
    talla_eu DECIMAL(4, 1) NOT NULL,
    talla_us DECIMAL(4, 1),
    cantidad INT NOT NULL DEFAULT 0,
    ajuste_precio DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (color_id) REFERENCES color_variantes(id)
);
```

### Ejemplo de Estructura JSON

```json
{
  "id": 1,
  "nombre": "Running Sport Punto V",
  "marca": "Punto V",
  "categoria": "Zapatilla Deportiva",
  "precioBase": 129.90,
  "descripcion": "Zapatillas deportivas con tecnología de amortiguación avanzada",
  "variantesColor": [
    {
      "id": 101,
      "nombreColor": "Negro",
      "codigoHex": "#000000",
      "imagenes": ["negro_frente.jpg", "negro_lateral.jpg"],
      "tallas": [
        {
          "id": 1001,
          "tallaEU": 40,
          "tallaUS": 7.5,
          "cantidad": 15,
          "ajustePrecio": 0.00
        },
        {
          "id": 1002,
          "tallaEU": 41,
          "tallaUS": 8,
          "cantidad": 8,
          "ajustePrecio": 0.00
        }
      ]
    },
    {
      "id": 102,
      "nombreColor": "Blanco",
      "codigoHex": "#FFFFFF",
      "imagenes": ["blanco_frente.jpg", "blanco_lateral.jpg"],
      "tallas": [
        {
          "id": 1003,
          "tallaEU": 40,
          "tallaUS": 7.5,
          "cantidad": 5,
          "ajustePrecio": 0.00
        }
      ]
    }
  ]
}
```

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> API REST

El backend proporciona una API RESTful completa para todas las operaciones del sistema:

<div align="center">
  <img src="https://i.imgur.com/8zH5LZ5.gif" alt="API Endpoints" width="400" />
  <p><i>Documentación API con Swagger</i></p>
</div>

### Endpoints Principales

- **Productos**: `/api/productos`
  - GET, POST, PUT, DELETE operaciones para gestionar productos
  
- **Colores**: `/api/productos/{productoId}/colores`
  - Gestión de variantes de color para cada producto
  
- **Tallas**: `/api/productos/{productoId}/colores/{colorId}/tallas`
  - Control de inventario por tallas
  
- **Ventas**: `/api/ventas`
  - Procesamiento de transacciones de ventas
  
- **Usuarios**: `/api/usuarios`
  - Gestión de usuarios y autenticación

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Planes para el Frontend

<div align="center">
  <img src="https://i.imgur.com/mDVZk4d.gif" alt="Frontend Concept" width="350" />
  <p><i>Diseño conceptual del futuro frontend</i></p>
</div>

El frontend se desarrollará próximamente con **Angular 19** y ofrecerá:

- Interfaz moderna y responsive para mostrar productos por categorías (Zapatillas Deportivas, Tacones, Zapatos de Vestir)
- Filtrado dinámico por marca (destacando Punto V)
- Gestión visual de inventario con códigos de color
- Panel de control para administración de ventas
- Estadísticas visuales de ventas por categoría y marca

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Mejoras Futuras

- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Travel%20and%20places/Globe%20with%20Meridians.png" alt="Globo" width="20" /> Tienda en línea para clientes
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Mobile%20Phone.png" alt="Teléfono Móvil" width="20" /> Aplicación móvil para vendedores
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Robot.png" alt="Robot" width="20" /> Sistema de recomendación inteligente
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Electric%20Plug.png" alt="Enchufe Eléctrico" width="20" /> Integración con software contable
- <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Objects/Receipt.png" alt="Recibo" width="20" /> Módulo de fidelización de clientes

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Contribuidores

- [Emerson147](https://github.com/Emerson147) - Desarrollador Principal

## <img src="https://media2.giphy.com/media/QssGEmpkyEOhBCb7e1/giphy.gif?cid=ecf05e47a0n3gi1bfqntqmob8g9aid1oyj2wr3ds3mg700bl&rid=giphy.gif" width="25" /> Licencia

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo LICENSE para más detalles.

---

<div align="center">
  <img src="https://raw.githubusercontent.com/Tarikul-Islam-Anik/Animated-Fluent-Emojis/master/Emojis/Smilies/Red%20Heart.png" alt="Corazón" width="60" />
  <p><i>Hecho con dedicación por La Peruanita</i></p>
  <p>© 2025 La Peruanita - Actualizado: 23/04/2025</p>
</div>
