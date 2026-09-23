#!/usr/bin/env bash
# End-to-end smoke test of the openapi.yaml contract against a running gateway.
set -euo pipefail

BASE="${1:-http://localhost:4000}"

fail() {
  echo "FAIL: $1" >&2
  exit 1
}

check() {
  local description="$1" expected="$2" actual="$3"
  if [[ "$actual" != *"$expected"* ]]; then
    fail "$description (expected to contain '$expected', got: ${actual:0:400})"
  fi
  echo "ok - $description"
}

check "health" '"status":"ok"' "$(curl -fsS "$BASE/api/health")"

TOKEN=$(curl -fsS -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}' | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
[[ -n "$TOKEN" ]] || fail "login did not return a token"
echo "ok - login"

AUTH=(-H "Authorization: Bearer $TOKEN")

check "auth/me" '"username":"admin"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/auth/me")"
check "profile" '"restaurant"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/profile")"
check "dashboard" '"stats"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/dashboard")"
check "menu categories" 'Coffee' "$(curl -fsS "${AUTH[@]}" "$BASE/api/menu/categories")"
check "menu" '"available"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/menu")"
check "addons" '"linkedDishes"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/addons")"
check "tables" '"T-01"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/tables")"
check "orders" '"orderNo"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/orders")"
check "order history" '"rows"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/order-history")"
check "bill" 'KITCHEN ORDER TICKET' "$(curl -fsS "${AUTH[@]}" "$BASE/api/bills/%23041?format=kot")"
check "tax invoice" '"cgst"' "$(curl -fsS "${AUTH[@]}" "$BASE/api/bills/%23041?format=ca")"

GUEST=$(curl -fsS -X POST "$BASE/api/guest/orders" -H 'Content-Type: application/json' \
  -d '{"table":"T-12","items":[{"name":"Latte","quantity":1,"addons":["Oat Milk"]}],"customerName":"Smoke Test"}')
check "guest order" '"orderNo"' "$GUEST"

ORDER_ID=$(printf '%s' "$GUEST" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')
check "status update" '"kot-printed"' \
  "$(curl -fsS -X PATCH "${AUTH[@]}" -H 'Content-Type: application/json' \
    -d '{"status":"kot-printed"}' "$BASE/api/orders/$ORDER_ID/status")"

curl -fsS -X DELETE "${AUTH[@]}" "$BASE/api/orders/$ORDER_ID" >/dev/null
echo "ok - cancel order"

curl -fsS -X POST "$BASE/api/reset" >/dev/null
echo "ok - reset"

curl -fsS -X POST "${AUTH[@]}" "$BASE/api/auth/logout" >/dev/null
echo "ok - logout"

echo "All smoke checks passed against $BASE"
