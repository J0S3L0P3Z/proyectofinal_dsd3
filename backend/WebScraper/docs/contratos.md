# Contratos de datos (F0.4)

> Los campos nulos se omiten en las respuestas JSON.

## Estados del job
`PENDING → SCRAPING → ANALYZING → GENERATING_PDF → SENDING_EMAIL → COMPLETED | COMPLETED_WITH_WARNINGS`  
Cualquier paso puede terminar en `FAILED` (con `errorCode` y `errorMessage`).

## Códigos de error del scraping
`INVALID_URL`, `URL_NOT_ALLOWED`, `PAGE_NOT_FOUND`, `ACCESS_FORBIDDEN`, `LOGIN_REQUIRED`, `BLOCKED_BY_SITE`, `HTTP_ERROR`, `SERVER_ERROR`, `TIMEOUT`, `NETWORK_ERROR`, `SSL_ERROR`, `EMPTY_CONTENT`, `AI_ALL_FAILED`, `PDF_FAILED`, `QUEUE_FULL`, `JOB_TIMEOUT`, `TOO_MANY_ACTIVE_JOBS`, `INTERNAL_ERROR`.  
Por IA: `AI_NOT_CONFIGURED`, `AI_QUOTA_EXCEEDED`, `AI_AUTH_FAILED`, `AI_UNAVAILABLE`, `AI_TIMEOUT`, `AI_INVALID_RESPONSE`.  
Los mensajes en español están en `ErrorCode.java`.

## Hechos del scraping (`facts`, calculados por código)
```json
{
  "requestedUrl": "https://books.toscrape.com/",
  "finalUrl": "https://books.toscrape.com/index.html",
  "httpStatus": 200,
  "loadTimeMs": 1840,
  "title": "All products | Books to Scrape",
  "language": "en",
  "metaDescription": null,
  "metaTags": { "viewport": "width=device-width" },
  "headings": ["h1: All products", "h3: A Light in the ..."],
  "htmlSizeChars": 52113,
  "textLength": 3120,
  "imageCount": 20,
  "imageSources": ["https://books.toscrape.com/media/cache/2c/da/....jpg"],
  "internalLinkCount": 94,
  "externalLinkCount": 0,
  "otherLinkCount": 0,
  "internalLinkSamples": ["..."],
  "externalLinkSamples": [],
  "formCount": 1,
  "scriptCount": 4,
  "scriptSources": ["https://books.toscrape.com/static/oscar/js/bootstrap/bootstrap.min.js"],
  "technologies": [
    { "name": "Bootstrap", "category": "Biblioteca", "evidence": "recurso que incluye \"bootstrap\"" },
    { "name": "nginx", "category": "Servidor", "evidence": "cabecera Server: nginx" }
  ]
}
```
`technologies` vacío = "no determinado" (nunca se adivina). Los campos nulos se omiten en las respuestas de la API.

## Salida de cada IA (esquema fijo)
```json
{
  "deQueTrata": "Una tienda en línea de ejemplo que vende libros…",
  "informacionPrincipal": ["Catálogo de 1000 libros", "Categorías en el menú lateral", "Paginación"],
  "personasMencionadas": [{ "nombre": "Ana Gómez", "contexto": "autora del libro destacado" }],
  "imagenes": { "cantidad": 20, "comentario": "Portadas de libros" },
  "tecnologia": { "nombre": "Bootstrap", "evidencia": "script bootstrap.min.js" },
  "observaciones": []
}
```
`imagenes.cantidad` siempre es el número calculado por el código, no el que diga la IA.

## API (resumen; el detalle con ejemplos está en Swagger: http://localhost:8080/)

**POST /auth/login** `{ "username": "admin", "password": "Admin#12345" }` (username puede ser el correo) → `200`
```json
{ "token": "eyJhbGciOiJIUzI1NiJ9...", "role": "ADMIN", "username": "admin" }
```
Se envía `Authorization: Bearer <token>`. Errores de `/auth` y `/user`: texto plano con el código HTTP (401, 403, 409, 400).

**POST /scrape/create** `{ "url": "https://example.com" }` → `202`
```json
{ "id": "6f1c2b7e-3a1d-4c55-9d52-0b6a1e8f7c10", "status": "PENDING", "requestedUrl": "https://example.com/" }
```
URL inválida o interna → `400`
```json
{ "timestamp": "2026-11-02T15:04:05Z", "status": 400, "code": "URL_NOT_ALLOWED",
  "message": "Esa dirección no está permitida: apunta a una red interna o privada y, por seguridad, no se puede analizar." }
```

**GET /scrape/getbyid/{id}** (mientras corre)
```json
{ "job": { "id": "6f1c…", "requestedUrl": "https://example.com/", "domain": "example.com",
           "status": "ANALYZING", "finished": false, "warnings": [], "createdAt": "2026-11-02T15:04:05Z" },
  "pdfAvailable": false, "contentTruncated": false, "analyses": [] }
```
Terminado: `finished: true`, `pdfAvailable: true`, `facts` y `analyses` (una entrada por IA):
```json
{ "provider": "gemini", "label": "Google Gemini", "model": "gemini-3.5-flash-lite", "ok": true,
  "response": { "…esquema de arriba…": "" }, "durationMs": 11840 }
```
IA fallida: `"ok": false, "errorCode": "AI_QUOTA_EXCEEDED", "errorMessage": "Esta IA alcanzó su límite de uso gratuito por ahora."`

**GET /scrape/getall?page=0&size=10** → página de Spring (`content`, `totalElements`, `totalPages`, `number`, `size`…) con el resumen de cada scraping en `content`.  
**GET /scrape/downloadpdf/{id}** → archivo PDF.

**GET /admin/ai/status** → `[{ "provider": "ollama", "label": "Ollama Cloud", "model": "gemma4:31b", "configured": false, "keyVariable": "AI_OLLAMA_API_KEY", ... }]`  
**POST /admin/ai/test** → `[{ "provider": "ollama", "ok": true, "durationMs": 2310, "reply": "OK" }, ...]`
