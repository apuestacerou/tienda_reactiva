# Tienda en línea (reactiva)

Spring Boot **WebFlux** + **R2DBC** + **PostgreSQL (Neon)** + React (Vite). Arquitectura hexagonal. API REST con JWT, catálogo, carrito, registro/login y pedidos.

Repositorio: [github.com/apuestacerou/tienda_reactiva](https://github.com/apuestacerou/tienda_reactiva)

**No hay base en memoria:** solo **Neon** (PostgreSQL). Las credenciales van en **`application-local.yml`** (junto al `pom.xml`). Ese archivo **no** se sube a git; en el repo viene **`application-local.yml.example`** para copiarlo y editarlo.

La interfaz compilada está en **`src/main/resources/static/`** (generada con `npm run build` en `frontend/`). Así puedes abrir la tienda en **`http://localhost:8080`** con solo el backend en marcha. Si desarrollas el front, usa Vite en el puerto 5173 (ver más abajo).

---

## Cómo ejecutarlo si acabas de clonar el repo

1. **Requisitos:** JDK 21, Node.js 18+ (solo si vas a desarrollar el frontend o a regenerar la SPA), cuenta en [Neon](https://neon.tech) con un proyecto PostgreSQL.

2. **Clonar e ir a la raíz del backend** (donde está `pom.xml`):
   ```powershell
   git clone https://github.com/apuestacerou/tienda_reactiva.git
   cd tienda_reactiva
   ```

3. **Crear `application-local.yml`** (obligatorio para arrancar la API):
   ```powershell
   copy application-local.yml.example application-local.yml
   ```
   Edita `application-local.yml` y pon **URL R2DBC**, **usuario** y **contraseña** de Neon (host del pooler, base `neondb` o la que uses).

4. **Primera vez en Neon:** si la base ya existía sin categorías o sin columna `category_id` en `products`, ejecuta en el SQL Editor los scripts que apliquen en `scripts/` (por ejemplo `migrate-add-categories-products-fk.sql` y, si hace falta, `migrate-users-role-cliente-administrador.sql`).

5. **Levantar el backend:**
   ```powershell
   .\mvnw-local.cmd spring-boot:run
   ```
   - API: **http://localhost:8080**
   - Con la SPA incluida en el repo: abre **http://localhost:8080** en el navegador (misma app que en Vite, pero servida por Spring).

6. **Opcional — desarrollo del frontend con recarga rápida:** en otra terminal:
   ```powershell
   cd frontend
   npm install
   npm run dev
   ```
   Abre **http://localhost:5173/** (Vite envía `/api` al 8080).

---

## Requisitos

| Herramienta | Versión |
|-------------|---------|
| **JDK** | 21 (`JAVA_HOME` apuntando al JDK) |
| **Node.js** | 18 o superior (para `frontend/`: desarrollo o regenerar la SPA) |
| **Maven** | Incluido vía `mvnw` / `mvnw.cmd` |
| **Neon** | Proyecto PostgreSQL (pooler recomendado) |

---

## Configuración detallada de Neon (`application-local.yml`)

En la **misma carpeta que `pom.xml`**:

1. Copia la plantilla: `copy application-local.yml.example application-local.yml`
2. Edita **`application-local.yml`**: URL **R2DBC** `r2dbc:postgresql://HOST:5432/neondb?sslmode=require`, `username` y `password`.

Opcional: **`TIENDA_JWT_SECRET`** (≥ 32 caracteres) en servidores reales.

Al arrancar, `schema.sql` se aplica vía R2DBC (`CREATE TABLE IF NOT EXISTS`, …).

**Roles:** `CLIENTE` (por defecto al registrarse) o `ADMINISTRADOR`. Promover en Neon: `UPDATE users SET role = 'ADMINISTRADOR' WHERE email = '...';`. Si migras desde `CUSTOMER`/`ADMIN`, usa `scripts/migrate-users-role-cliente-administrador.sql`.

---

## Ejecutar solo el backend (API + UI embebida)

```powershell
.\mvnw-local.cmd spring-boot:run
```

`mvnw-local.cmd` exige **`application-local.yml`** antes de `spring-boot:run`. Para **tests** sin Neon: `.\mvnw.cmd test` (el test de contexto completo se omite si no existe `application-local.yml`).

---

## Regenerar la SPA en el JAR (tras cambiar `frontend/`)

```powershell
cd frontend
npm install
npm run build
cd ..
```

Eso vuelve a generar `src/main/resources/static/`. Luego `.\mvnw.cmd package` para empaquetar.

---

## Pruebas (Maven)

```powershell
.\mvnw.cmd test
```

---

## Empaquetar JAR (producción local)

```powershell
cd frontend
npm run build
cd ..
.\mvnw.cmd package -DskipTests
```

Si no tienes `application-local.yml` en la máquina de build, `-DskipTests` evita el test que levanta contexto contra Neon.

---

## Pendientes / operación

- Datos en Neon (`products`, `users`, categorías, pedidos).
- `TIENDA_JWT_SECRET` en despliegue.
- CORS si front y API van en dominios distintos.
- `tienda.uploads.dir` en servidor para imágenes.

---

## API (resumen)

| Método | Ruta | Notas |
|--------|------|--------|
| GET | `/api/products` | Catálogo |
| GET | `/api/products/{id}` | Detalle |
| GET | `/api/categories` | Categorías |
| POST | `/api/auth/register` | JSON `{ email, password, fullName }` |
| POST | `/api/auth/login` | JSON `{ email, password }` → `token`, `role` |
| POST | `/api/orders` | `Authorization: Bearer <token>` |
| POST/PUT/DELETE | `/api/products` | CRUD multipart (solo **ADMINISTRADOR**) |

Imágenes: `GET /api/files/{nombre}` — subidas en `uploads/productos/` (ignorado en git).

---

## Estructura (paquetes Java)

| Paquete | Rol |
|---------|-----|
| `domain` | Modelo y puertos |
| `application` | Servicios y DTOs |
| `infrastructure.persistence` | R2DBC |
| `infrastructure.web` | REST |
| `infrastructure.security` | JWT, Security WebFlux |

`frontend/` — React + TypeScript + Vite.
