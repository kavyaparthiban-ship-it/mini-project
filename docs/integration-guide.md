# Integration Guide

## Integration Points
1. **DB → Backend**: Spring Data JPA entities map to schema; `ddl-auto=validate`
2. **Backend → Frontend**: REST JSON over HTTP, CORS for local origins
3. **Member Modules**: All modules integrate through shared contracts (API + schema)

## Pre-integration Checklist
- MySQL running with schema+data loaded
- Backend builds and tests pass
- Frontend loads without console errors
- Environment variables set (or defaults used)

## Integration Steps
1. Load DB (schema.sql + data.sql)
2. Start backend (`mvn spring-boot:run`)
3. Verify `/api/health`
4. Start frontend (static server)
5. Run search in dashboard
6. Confirm rewards flow

## Common Issues
- Port conflicts (8080/5500) - adjust via env/CORS
- MySQL not started - backend fails fast
- CORS origin mismatch - update `CORS_ORIGINS`
- Credentials - set via env vars
