# Tienda en línea (reactiva)

Spring Boot **WebFlux** + **R2DBC** + **PostgreSQL (Neon)** + React (Vite). Arquitectura hexagonal. API REST con JWT, catálogo, carrito, registro/login y pedidos.

Repositorio: [github.com/apuestacerou/tienda_reactiva](https://github.com/apuestacerou/tienda_reactiva)

**No hay base en memoria:** la app solo se conecta a Neon. Las credenciales van en **`application-local.yml`** (junto al `pom.xml`), que **no** se sube a git.

---

## Requisitos

| Herramienta | Versión |
|--------------|---------|
| **JDK** | 21 (`JAVA_HOME` apuntando al JDK) |
| **Node.js** | 18 o superior (para el frontend) |
| **Maven** | Incluido vía `mvnw` / `mvnw.cmd` |
| **Neon** | Proyecto PostgreSQL con cadena de conexión (pooler recomendado) |

---

## 1. Clonar el proyecto

```powershell
git clone https://github.com/apuestacerou/tienda_reactiva.git
cd tienda_reactiva
```

Entra al directorio donde está el `pom.xml` (raíz del backend).

---

## 2. Configurar Neon (`application-local.yml`)

En la **misma carpeta que `pom.xml`**:

1. Copia la plantilla:
   ```powershell
   copy application-local.yml.example application-local.yml
   ```
2. Edita **`application-local.yml`** y pon tu host pooler de Neon, usuario y contraseña:
   - URL **R2DBC:** `r2dbc:postgresql://HOST:5432/neondb?sslmode=require` (no uses el `postgresql://` JDBC del portapapeles tal cual).
   - `username` / `password` aparte de la URL.

Ese archivo está en **`.gitignore`**: no se commitea.

Opcional: variable de entorno **`TIENDA_JWT_SECRET`** (≥ 32 caracteres) en servidores reales.

Al arrancar, `schema.sql` se aplica vía R2DBC (`CREATE TABLE IF NOT EXISTS`, …). También puedes usar los scripts en `scripts/`.

**Roles en `users.role`:** `CLIENTE` (por defecto al registrarse) o `ADMINISTRADOR`. Para promover a admin desde Neon: `UPDATE users SET role = 'ADMINISTRADOR' WHERE email = '...';`. Si tu BD ya tenía `CUSTOMER`/`ADMIN`, ejecuta una vez `scripts/migrate-users-role-cliente-administrador.sql` en el SQL Editor de Neon.

---

## 3. Ejecutar el backend (API)

```powershell
cd c:\ruta\al\proyecto
.\mvnw-local.cmd spring-boot:run
```

`mvnw-local.cmd` comprueba que exista **`application-local.yml`** antes de `spring-boot:run` (si falta, muestra cómo crearlo). Para **tests** puedes usar `.\mvnw.cmd test` sin ese archivo.

(O `.\mvnw.cmd spring-boot:run` si `JAVA_HOME` está bien y ya tienes `application-local.yml`; sin él la app no tendrá URL R2DBC.)

- API: **`http://localhost:8080`**
- En consola debería loguearse que R2DBC usa PostgreSQL (Neon).

**No cierres esta terminal** mientras pruebas el frontend.

---

## 4. Ejecutar el frontend

```powershell
cd frontend
npm install
npm run dev
```

- **`http://localhost:5173/`** — Vite hace proxy de `/api` a `http://localhost:8080`.

Rutas (HashRouter): `#/`, `#/cart`, `#/login`, `#/register`, `#/checkout`.

---

## 5. Pruebas (Maven)

```powershell
.\mvnw.cmd test
```

`ReactivaApplicationTests` (contexto completo con Neon) **solo se ejecuta** si existe **`application-local.yml`** en la raíz del módulo. Sin ese archivo (p. ej. en CI), se **omite**; el resto de tests siguen.

---

## 6. Empaquetar la SPA en el JAR (opcional)

```powershell
cd frontend
npm run build
cd ..
.\mvnw.cmd package
```

Si no tienes Neon en el entorno de build, usa `.\mvnw.cmd package -DskipTests`.

---

## Pendientes / operación

- Tablas y datos en Neon (`products`, `users`, pedidos).
- `TIENDA_JWT_SECRET` en despliegue.
- CORS si front y API van en dominios distintos.
- `tienda.uploads.dir` en servidor para imágenes.

---

## API (resumen)

| Método | Ruta | Notas |
|--------|------|--------|
| GET | `/api/products` | Catálogo |
| GET | `/api/products/{id}` | Detalle |
| POST | `/api/auth/register` | JSON `{ email, password, fullName }` |
| POST | `/api/auth/login` | JSON `{ email, password }` → `token` |
| POST | `/api/orders` | `Authorization: Bearer <token>`; `{ items: [{ productId, quantity }] }` |
| POST | `/api/products` | Multipart: nombre, precio, stock, imagen opcional |

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
