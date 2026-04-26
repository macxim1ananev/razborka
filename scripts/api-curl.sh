#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE:-http://localhost:8080}"

echo "=== GET /api/listings ==="
curl -sS "$BASE/api/listings?page=0&size=5" | head -c 2000
echo

echo "=== GET /api/search ==="
curl -sS "$BASE/api/search?page=0&size=5" | head -c 2000
echo

echo "=== GET /api/categories/tree ==="
curl -sS "$BASE/api/categories/tree" | head -c 2000
echo
