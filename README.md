# Tienda en línea (reactiva)

Spring Boot **WebFlux** + **R2DBC** + React (Vite). Arquitectura hexagonal. API REST con JWT, catálogo, carrito, registro/login y pedidos.

Repositorio: [github.com/apuestacerou/tienda_reactiva](https://github.com/apuestacerou/tienda_reactiva)

---

## Requisitos

| Herramienta | Versión |
|--------------|---------|
| **JDK** | 21 (`JAVA_HOME` apuntando al JDK) |
| **Node.js** | 18 o superior (para el frontend) |
| **Maven** | Incluido vía `mvnw` / `mvnw.cmd` (no hace falta instalar Maven global) |

---

## 1. Clonar el proyecto

```powershell
git clone https://github.com/apuestacerou/tienda_reactiva.git
cd tienda_reactiva
```

Si trabajas en una carpeta distinta, entra siempre al directorio donde está el `pom.xml` (raíz del backend).

---

## 2. Ejecutar el backend (API)

Abre **PowerShell** en la raíz del proyecto (donde están `pom.xml` y `mvnw.cmd`).

### Si `JAVA_HOME` falla o Maven dice que no está bien definido

Usa el script local que detecta el JDK 21 en Windows:

```powershell
.\mvnw-local.cmd spring-boot:run
```

### Si `JAVA_HOME` ya está correcto

```powershell
.\mvnw.cmd spring-boot:run
```

- Por defecto la app usa **H2 en memoria** (no hace falta instalar una base).
- La API queda en: **`http://localhost:8080`**
- Comprueba: `http://localhost:8080/api/products` (puede devolver `[]` si no hay productos).

**No cierres esta terminal** mientras pruebas el frontend.

---

## 3. Ejecutar el frontend (interfaz web)

Abre **otra** terminal:

```powershell
cd frontend
npm install
npm run dev
```

- La web queda en: **`http://localhost:5173/`**
- Vite hace **proxy** de las peticiones `/api` hacia `http://localhost:8080`, así que el backend debe estar en marcha.

Rutas (HashRouter): `#/`, `#/cart`, `#/login`, `#/register`, `#/checkout`.

**Flujo:** cualquiera puede ver el catálogo y usar el carrito (se guarda en `localStorage`). Para **confirmar pedido** hay que **registrarse o iniciar sesión**; el checkout usa `POST /api/orders` con JWT.

---

## 4. Conectar **Neon** (PostgreSQL en la nube)

La configuración del perfil está en `src/main/resources/application-neon.yml` (sin contraseñas: solo placeholders).

1. En el [dashboard de Neon](https://neon.tech), copia el **host** del connection string (suele ser el host **pooler**), usuario, contraseña y nombre de base (p. ej. `neondb`).
2. En PowerShell, **antes** de arrancar el backend:

```powershell
$env:SPRING_PROFILES_ACTIVE="neon"
$env:NEON_R2DBC_URL="r2dbc:postgresql://TU_HOST_NEON:5432/neondb?sslmode=require"
$env:NEON_R2DBC_USERNAME="neondb_owner"
$env:NEON_R2DBC_PASSWORD="(contraseña del proyecto en Neon)"
.\mvnw-local.cmd spring-boot:run
```

- El formato es **R2DBC**, no el `postgresql://` JDBC del portapapeles: usa `r2dbc:postgresql://HOST:5432/neondb?sslmode=require` y usuario/clave **por separado**.
- **No subas** contraseñas al repositorio; usa variables de entorno o un `.env` local (está en `.gitignore`).
- Al arrancar, se ejecuta `schema.sql` sobre Neon (crea tablas si no existen). Si prefieres crear el esquema a mano, revisa `scripts/schema-products-neon.sql` y `scripts/schema-users-orders-neon.sql`.
- En producción, define también un **`TIENDA_JWT_SECRET`** largo y aleatorio (≥ 32 caracteres).

---

## 5. Empaquetar la SPA dentro del JAR (opcional)

Sirve para servir la misma UI desde el puerto 8080:

```powershell
cd frontend
npm run build
cd ..
.\mvnw.cmd package -DskipTests
```

Luego ejecuta el JAR generado en `target/`; la UI estática queda en `src/main/resources/static` tras el `npm run build`.

---

## 6. Pruebas (Maven)

```powershell
.\mvnw.cmd test
```

---

## 7. Qué puede seguir tu compañera / pendientes típicos

- **Neon:** verificar tablas y datos (`products`, usuarios de prueba); ajustar `NEON_R2DBC_*` si cambian host o base.
- **JWT:** `TIENDA_JWT_SECRET` en entorno para despliegues reales.
- **Catálogo:** cargar productos en Neon (API admin o SQL) si la lista sale vacía.
- **CORS / despliegue:** si el front y el back van en dominios distintos, revisar `WebConfig` / Security.
- **Imágenes:** en servidor, revisar `tienda.uploads.dir` y permisos de disco.

---

## API (resumen)

| Método | Ruta | Notas |
|--------|------|--------|
| GET | `/api/products` | Catálogo |
| GET | `/api/products/{id}` | Detalle |
| POST | `/api/auth/register` | JSON `{ email, password, fullName }` |
| POST | `/api/auth/login` | JSON `{ email, password }` → `token` |
| POST | `/api/orders` | Requiere `Authorization: Bearer <token>`; body `{ items: [{ productId, quantity }] }` |
| POST | `/api/products` | Multipart: nombre, precio, stock, imagen opcional |

Imágenes: `GET /api/files/{nombre}` — las subidas locales van a `uploads/productos/` (ignorado en git).

---

## Estructura del código (paquetes Java)

| Paquete | Rol |
|---------|-----|
| `domain` | Modelo y puertos |
| `application` | Servicios y DTOs |
| `infrastructure.persistence` | R2DBC, entidades |
| `infrastructure.web` | Controladores REST |
| `infrastructure.security` | JWT, Security WebFlux |

`frontend/` — React + TypeScript + Vite.
