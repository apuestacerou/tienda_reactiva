# Tienda reactiva — visión técnica

Documento complementario al [README.md](README.md) principal. Aquí se resume **qué es el proyecto**, su **arquitectura**, el **modelo de datos** y **qué incluye** la solución de forma breve.

---

## 1. Qué es el proyecto

Es una **tienda en línea** full stack: catálogo público, carrito, registro e inicio de sesión, creación de pedidos y un **panel de administración** para gestionar productos (alta, edición, baja e imágenes). El backend expone una **API REST** consumida por una SPA en **React**; la persistencia es **PostgreSQL** (en la práctica [Neon](https://neon.tech)), sin base embebida ni H2.

El hilo conductor del backend es la **programación reactiva**: **Spring WebFlux**, **R2DBC** y tipos **Mono/Flux** (Project Reactor) para no bloquear hilos del servidor mientras se espera I/O (red, base de datos).

---

## 2. Stack tecnológico

| Capa | Tecnología |
|------|------------|
| API | Spring Boot, **Spring WebFlux** |
| Persistencia | **Spring Data R2DBC**, driver **r2dbc-postgresql** |
| Seguridad | Spring Security (reactive), JWT (JJWT) |
| Documentación API | **springdoc-openapi** (Swagger UI en `/swagger-ui.html`) |
| Cliente web | **React 18**, **TypeScript**, **Vite 5** |
| Base de datos | **PostgreSQL** (Neon en desarrollo/producción típica) |
| Build | Maven (`mvnw`), Node para el frontend |

---

## 3. Arquitectura (hexagonal / puertos y adaptadores)

El código Java se organiza por responsabilidades, separando el **núcleo de negocio** de los **detalles técnicos**:

```
com.tiendaenlinea.reactiva
├── domain/           # Modelo de dominio y contratos (puertos)
│   ├── model/        # Entidades de negocio (Product, Category, …)
│   └── port/         # Interfaces que el dominio exige (repositorios, almacenamiento de imágenes)
├── application/      # Casos de uso, DTOs, servicios de aplicación
│   ├── dto/          # Comandos, respuestas, requests de auth/pedidos
│   └── service/      # AuthService, ProductApplicationService, OrderCheckoutService, …
└── infrastructure/   # Adaptadores concretos
    ├── persistence/  # Entidades R2DBC, repositorios, adaptadores a los puertos
    ├── web/          # Controladores REST, configuración OpenAPI, manejo de errores
    ├── security/     # JWT, filtro Bearer, reglas por ruta y rol
    ├── storage/      # Guardado local de imágenes de producto
    └── config/       # Arranque, esquema SQL inicial, etc.
```

**Ideas clave:**

- **Dominio**: reglas y tipos independientes de HTTP y de la base.
- **Aplicación**: orquesta casos de uso (validar categoría, crear pedido, emitir JWT).
- **Infraestructura**: implementa los puertos (R2DBC, disco para imágenes, filtros de seguridad).

El frontend (`frontend/src`) sigue páginas por ruta (`/`, `/cart`, `/login`, `/admin`, …), contexto de **autenticación** y **carrito**, y un cliente HTTP centralizado en `api/client.ts`.

---

## 4. Programación reactiva en las llamadas al API

- Los controladores devuelven **`Mono`** o **`Flux`** en lugar de objetos bloqueantes.
- Los repositorios extienden **`ReactiveCrudRepository`** y las consultas devuelven **`Mono`/`Flux`**.
- Los servicios encadenan operaciones con **`flatMap`**, **`then`**, **`switchIfEmpty`**, etc., describiendo flujos asíncronos sin bloquear un hilo del pool en cada espera.

Eso encaja con **WebFlux** (Netty) y **R2DBC**: el modelo es **no bloqueante** respecto al uso eficiente de hilos bajo carga concurrente.

---

## 5. Seguridad y roles

- **Registro / login** (`/api/auth/register`, `/api/auth/login`) son públicos; la respuesta incluye un **JWT**.
- Rutas de lectura de catálogo y ficheros estáticos de imágenes suelen ir **sin token**.
- **Crear, actualizar o borrar productos** exige rol **`ADMINISTRADOR`** (claim `role` en el JWT).
- **Crear pedidos** (`POST /api/orders`) exige usuario **autenticado** (típicamente `CLIENTE` o también admin).

Los roles válidos en base de datos son **`CLIENTE`** y **`ADMINISTRADOR`**; el registro web crea usuarios `CLIENTE`. Promover un administrador se hace en SQL sobre la tabla `users` (ver README principal y `scripts/`).

---

## 6. Base de datos (PostgreSQL)

El esquema lógico está definido en `src/main/resources/schema.sql` (aplicado al arrancar vía inicializador R2DBC). Resumen:

| Tabla | Propósito |
|-------|-----------|
| **categories** | Categorías de producto (`id`, `name`, `slug` único). Seeds iniciales: *General*, *Electrónica*, *Ropa* (si no existen por `slug`). |
| **products** | Catálogo (`name`, `description`, `price`, `stock`, `image_path`, `category_id` → `categories`, restricciones de precio/stock ≥ 0). |
| **users** | Usuarios (`email` único, `password_hash`, `full_name`, `role`, marcas de tiempo). |
| **orders** | Pedidos (`user_id`, `status`, `total_amount`, fechas). Estados permitidos: `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`. |
| **order_items** | Líneas de pedido (`order_id`, `product_id`, `quantity`, `unit_price`); borrado en cascada si se elimina el pedido. |

**Relaciones principales:** `products.category_id` → `categories`; `orders.user_id` → `users`; `order_items` enlaza pedidos y productos.

Las imágenes de producto se guardan en **disco** (ruta configurable); en base solo se persiste la ruta o nombre de fichero según el modelo de entidad.

---

## 7. Qué incluye la solución (resumen funcional)

- API REST: productos, categorías, autenticación, pedidos.
- Tienda: listado, carrito, checkout con JWT.
- **Administración**: panel en `/#/admin` con accesos a **crear producto** y **listado con edición/eliminación**; formularios multipart para datos e imagen.
- **OpenAPI / Swagger UI** para probar endpoints desde el navegador (`/swagger-ui.html`).
- Colección **Postman** importable en `postman/Tienda-Reactiva.postman_collection.json` para pruebas manuales de los flujos principales.

Para **cómo ejecutar**, variables de entorno y detalles operativos, sigue el [README.md](README.md) principal.
