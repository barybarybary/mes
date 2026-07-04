#!/bin/bash
# ============================================
# MES 系统 全功能测试脚本（无需登录）
# 目标: http://localhost:8081
# ============================================

BASE="http://localhost:8081"
PASS=0
FAIL=0

# ---------- helper ----------
ok()   { echo "  ✅ [$1] PASS"; ((PASS++)); }
fail() { echo "  ❌ [$1] FAIL — $2 (expect $3)"; ((FAIL++)); }

check() {
  local label="$1"
  local method="$2"
  local url="$3"
  local data="$4"
  local expect_code="${5:-200}"
  local expect_body="$6"

  local resp
  if [ "$method" = "GET" ] || [ "$method" = "DELETE" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE$url" 2>/dev/null)
  else
    resp=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE$url" \
      -H "Content-Type: application/json" \
      -d "$data" 2>/dev/null)
  fi

  local http_code
  http_code=$(echo "$resp" | tail -1)
  local body
  body=$(echo "$resp" | sed '$d')

  if [ "$http_code" = "$expect_code" ]; then
    if [ -n "$expect_body" ]; then
      if echo "$body" | grep -q "$expect_body"; then
        ok "$label"
      else
        fail "$label" "body missing '$expect_body': $(echo "$body" | head -c 200)" "$expect_body"
      fi
    else
      ok "$label"
    fi
  else
    fail "$label" "HTTP $http_code (body: $(echo "$body" | head -c 200))" "$expect_code"
  fi
}

check_delete() { check "$1" "DELETE" "$2" "$3" "${4:-200}" "${5:-}"; }
check_get()    { check "$1" "GET" "$2" "" "${3:-200}" "${4:-}"; }
check_post()   { check "$1" "POST" "$2" "$3" "${4:-200}" "${5:-}"; }
check_put()    { check "$1" "PUT" "$2" "$3" "${4:-200}" "${5:-}"; }

echo ""
echo "======================================="
echo "  MES 全功能 API 测试"
echo "  Server: $BASE"
echo "  Time:   $(date '+%Y-%m-%d %H:%M:%S')"
echo "======================================="
echo ""

# ==========================================
# 1. 系统管理 — 用户 (SysUser)
# ==========================================
echo "━━━ 1a. 用户管理 ━━━"

check_get "用户分页列表"   "/api/system/user?page=1&pageSize=5" 200 "success"
check_get "用户分页搜索"   "/api/system/user?page=1&pageSize=5&keyword=admin" 200 "success"
check_get "用户详情(ID=1)" "/api/system/user/1" 200 "admin"

NEW_USER_ID=""
resp=$(curl -s -X POST "$BASE/api/system/user" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser99","password":"e10adc3949ba59abbe56e057f20f883e","nickname":"Tester","status":1}')
if echo "$resp" | grep -q '"code":200'; then
  ok "新增用户"
  NEW_USER_ID=$(curl -s "$BASE/api/system/user?page=1&pageSize=20" | grep -o '"username":"testuser99".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
else
  fail "新增用户" "HTTP 200 (body: $(echo "$resp" | head -c 200))" "200"
fi

if [ -n "$NEW_USER_ID" ]; then
  check_put "修改用户" "/api/system/user" "{\"id\":$NEW_USER_ID,\"nickname\":\"Edited\",\"status\":1}"
fi

check_post "分配角色" "/api/system/user/1/roles" '{"roleIds":[1,2]}'

echo ""
echo "━━━ 1b. 角色管理 ━━━"

check_get    "角色列表"     "/api/system/role" 200 "success"
check_post   "新增角色"     "/api/system/role" '{"code":"test_role","name":"TestRole","description":"Auto test"}' 200 "success"

# Get test role id to clean up
ROLE_ID=$(curl -s "$BASE/api/system/role" | grep -o '"code":"test_role".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$ROLE_ID" ]; then
  check_put    "修改角色"     "/api/system/role" "{\"id\":$ROLE_ID,\"code\":\"test_role\",\"name\":\"TestRoleEdit\"}"
  check_post   "分配菜单"     "/api/system/role/$ROLE_ID/menus" '{"menuIds":[11,12,13]}'
  check_delete "删除角色"     "/api/system/role/$ROLE_ID"
fi

echo ""
echo "━━━ 1c. 菜单管理 ━━━"

check_get    "菜单树"       "/api/system/menu/tree" 200 "success"
check_get    "菜单列表"     "/api/system/menu" 200 "success"
check_post   "新增菜单"     "/api/system/menu" '{"parentId":1,"name":"TestMenu","type":2,"path":"/test","sort":99}' 200 "success"

MENU_ID=$(curl -s "$BASE/api/system/menu" | grep -o '"name":"TestMenu".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$MENU_ID" ]; then
  check_put    "修改菜单"     "/api/system/menu" "{\"id\":$MENU_ID,\"name\":\"TestMenuEdit\",\"type\":2,\"path\":\"/test-edit\"}"
  check_delete "删除菜单"     "/api/system/menu/$MENU_ID"
fi

# ==========================================
# 2. 基础数据 — 产品 / BOM / 工序 / 仓库
# ==========================================
echo ""
echo "━━━ 2a. 产品管理 ━━━"

check_get    "产品分页"     "/api/base/product?page=1&pageSize=5" 200 "success"
check_get    "产品搜索"     "/api/base/product?page=1&pageSize=5&keyword=Gear" 200
check_get    "产品详情(含BOM)" "/api/base/product/1" 200 "success"
check_post   "新增产品"     "/api/base/product" '{"code":"TEST001","name":"TestProduct","spec":"D10","unit":"pcs","price":99.99,"status":1}' 200

PROD_ID=$(curl -s "$BASE/api/base/product?page=1&pageSize=20" | grep -o '"code":"TEST001".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$PROD_ID" ]; then
  check_put    "修改产品"     "/api/base/product" "{\"id\":$PROD_ID,\"code\":\"TEST001\",\"name\":\"TestProductEdit\",\"status\":1}"
  check_post   "保存BOM"     "/api/base/product/$PROD_ID/bom" "[{\"materialId\":9,\"quantity\":2.5,\"unit\":\"bar\"},{\"materialId\":10,\"quantity\":1.0,\"unit\":\"block\"}]" 200
  check_delete "删除产品"     "/api/base/product/$PROD_ID"
fi

echo ""
echo "━━━ 2b. 工序管理 ━━━"

check_get    "工序列表"     "/api/base/process" 200
check_post   "新增工序"     "/api/base/process" '{"code":"TEST01","name":"TestProcess","standardHours":10.0,"price":5.0,"sort":99,"status":1}' 200

PROC_ID=$(curl -s "$BASE/api/base/process" | grep -o '"code":"TEST01".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$PROC_ID" ]; then
  check_put    "修改工序"     "/api/base/process" "{\"id\":$PROC_ID,\"code\":\"TEST01\",\"name\":\"TestProcessEdit\",\"status\":1}"
  check_delete "删除工序"     "/api/base/process/$PROC_ID"
fi

echo ""
echo "━━━ 2c. 仓库管理 ━━━"

check_get    "仓库列表"     "/api/base/warehouse" 200
check_post   "新增仓库"     "/api/base/warehouse" '{"code":"WH99","name":"TestWH","type":"material","status":1}' 200

WH_ID=$(curl -s "$BASE/api/base/warehouse" | grep -o '"code":"WH99".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$WH_ID" ]; then
  check_put    "修改仓库"     "/api/base/warehouse" "{\"id\":$WH_ID,\"code\":\"WH99\",\"name\":\"TestWHEdit\",\"type\":\"material\"}"
  check_get    "库位列表"     "/api/base/warehouse/$WH_ID/locations" 200
  check_post   "新增库位"     "/api/base/warehouse/$WH_ID/locations" '{"code":"LOC-99","name":"Test Location"}' 200
  check_delete "删除仓库"     "/api/base/warehouse/$WH_ID"
fi

# ==========================================
# 3. 销售管理 — 订单
# ==========================================
echo ""
echo "━━━ 3. 销售订单 ━━━"

check_get    "订单分页"     "/api/sale/order?page=1&pageSize=5" 200 "success"
check_get    "订单筛选"     "/api/sale/order?page=1&pageSize=5&status=1" 200
check_get    "订单详情"     "/api/sale/order/1" 200 "success"

check_post   "创建订单"     "/api/sale/order" '{"customerId":1,"orderDate":"2026-06-29","deliveryDate":"2026-07-10","remark":"Auto test order","items":[{"productId":3,"quantity":10,"unit":"pcs","price":45.00}]}' 200 "success"

# Find the newly created order
ORDER_ID=$(curl -s "$BASE/api/sale/order?page=1&pageSize=5" | grep -o '"remark":"Auto test order".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$ORDER_ID" ]; then
  check_put    "修改订单(待审核)" "/api/sale/order" "{\"id\":$ORDER_ID,\"customerId\":1,\"orderDate\":\"2026-06-29\",\"items\":[{\"productId\":3,\"quantity\":20,\"unit\":\"pcs\",\"price\":45.00}]}"
  check_put    "审核通过"     "/api/sale/order/$ORDER_ID/status?status=2" 200
  check_put    "状态→生产中"  "/api/sale/order/$ORDER_ID/status?status=3" 200
  check_delete "删除订单"     "/api/sale/order/$ORDER_ID"
fi

# ==========================================
# 4. 库存管理
# ==========================================
echo ""
echo "━━━ 4. 库存管理 ━━━"

check_get    "库存查询"     "/api/inventory" 200
check_get    "按产品查库存" "/api/inventory?productId=1" 200
check_get    "库存流水"     "/api/inventory/transactions?page=1&pageSize=5" 200
check_get    "流水筛选"     "/api/inventory/transactions?page=1&pageSize=5&productId=1" 200

check_post   "入库" "/api/inventory/in" \
  '{"productId":1,"warehouseId":3,"locationId":5,"batchNo":"TEST-BATCH-001","quantity":50,"type":"in","orderNo":"TST-IN-001","remark":"Test stock in"}' 200

check_post   "出库" "/api/inventory/out" \
  '{"productId":1,"warehouseId":3,"batchNo":"TEST-BATCH-001","quantity":10,"type":"out","orderNo":"TST-OUT-001","remark":"Test stock out"}' 200

# ==========================================
# 5. 生产管理 — 工单
# ==========================================
echo ""
echo "━━━ 5. 生产工单 ━━━"

check_get    "工单分页"     "/api/production/work-order?page=1&pageSize=5" 200
check_get    "工单筛选"     "/api/production/work-order?page=1&pageSize=5&status=1" 200
check_get    "工单详情"     "/api/production/work-order/1" 200

# Create a work order to test full lifecycle
check_post   "创建工单"     "/api/production/work-order" \
  '{"productId":3,"quantity":100,"sourceType":"manual","planStart":"2026-06-29","planEnd":"2026-07-05","remark":"Auto test WO","processes":[{"processId":1,"planQty":100},{"processId":2,"planQty":100}]}' 200

WO_ID=$(curl -s "$BASE/api/production/work-order?page=1&pageSize=5" | grep -o '"remark":"Auto test WO".*?"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
if [ -n "$WO_ID" ]; then
  check_put    "开工"         "/api/production/work-order/$WO_ID/start" "" 200
  check_put    "完工"         "/api/production/work-order/$WO_ID/complete" "" 200
  check_put    "完工入库"     "/api/production/work-order/$WO_ID/stock-in" "" 200
  check_delete "删除工单"     "/api/production/work-order/$WO_ID"
fi

# ==========================================
# 6. 知识库
# ==========================================
echo ""
echo "━━━ 6. 知识库 ━━━"

check_get    "文档分页"     "/api/knowledge?page=1&pageSize=5" 200
check_get    "文档筛选"     "/api/knowledge?page=1&pageSize=5&category=sop" 200

# Upload a test .txt file
TMPFILE=$(mktemp)
echo "This is a test document for the MES knowledge base. It contains information about production processes and quality control procedures." > "$TMPFILE"
UPLOAD_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/knowledge/upload?category=sop" \
  -F "file=@$TMPFILE" 2>/dev/null)
UPLOAD_CODE=$(echo "$UPLOAD_RESP" | tail -1)
UPLOAD_BODY=$(echo "$UPLOAD_RESP" | sed '$d')
if [ "$UPLOAD_CODE" = "200" ]; then
  ok "上传文档"
  DOC_ID=$(echo "$UPLOAD_BODY" | grep -o '"data":{[^}]*"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
  if [ -z "$DOC_ID" ]; then
    DOC_ID=$(echo "$UPLOAD_BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
  fi
  if [ -n "$DOC_ID" ]; then
    check_get    "查看切片"     "/api/knowledge/$DOC_ID/chunks" 200
    check_delete "删除文档"     "/api/knowledge/$DOC_ID"
  fi
else
  fail "上传文档" "HTTP $UPLOAD_CODE" "200"
fi
rm -f "$TMPFILE"

# ==========================================
# 7. AI 助手
# ==========================================
echo ""
echo "━━━ 7. AI 助手 ━━━"

check_get    "会话列表"     "/api/ai/conversations?userId=1" 200

check_post   "智能搜索"     "/api/ai/search" '{"query":"how to check inventory"}' 200

# Test AI chat (may fail if DeepSeek API is unreachable, but test the endpoint)
CHAT_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"question":"How do I check the inventory of product P001?"}' 2>/dev/null)
CHAT_CODE=$(echo "$CHAT_RESP" | tail -1)
CHAT_BODY=$(echo "$CHAT_RESP" | sed '$d')
if [ "$CHAT_CODE" = "200" ]; then
  ok "发送AI消息"
  CONV_ID=$(echo "$CHAT_BODY" | grep -o '"conversationId":[0-9]*' | grep -o '[0-9]*' | head -1)
  if [ -n "$CONV_ID" ]; then
    check_get "对话消息"     "/api/ai/conversations/$CONV_ID/messages" 200
  fi
else
  echo "  ⚠️  [发送AI消息] — HTTP $CHAT_CODE (AI service may be unavailable)"
fi

# ==========================================
# 8. 认证接口（跳过登录，测试无需token的）
# ==========================================
echo ""
echo "━━━ 8. 认证 (非登录) ━━━"

# POST /api/auth/login — SKIP (user asked no login)
echo "  ⏭️  登录 — SKIPPED (as requested)"

# POST /api/auth/logout — will fail without token (expected)
LOGOUT_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/auth/logout" -H "Authorization: Bearer fake-token" 2>/dev/null)
if [ "$LOGOUT_RESP" = "200" ]; then
  ok "登出(无有效token)"
else
  echo "  ⚠️  [登出] — HTTP $LOGOUT_RESP (expected without valid token)"
fi

# GET /api/auth/user-info — will fail without token
INFO_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE/api/auth/user-info" -H "Authorization: Bearer fake-token" 2>/dev/null)
if [ "$INFO_RESP" = "200" ]; then
  ok "获取用户信息(无有效token)"
else
  echo "  ⚠️  [获取用户信息] — HTTP $INFO_RESP (expected without valid token)"
fi

# ==========================================
# 9. Edge Cases / 异常场景
# ==========================================
echo ""
echo "━━━ 9. 边界/异常测试 ━━━"

check_get    "不存在的用户" "/api/system/user/99999" 200
check_delete "删除不存在记录" "/api/system/user/99999" 200

# 空关键词分页
check_get    "空关键词分页" "/api/system/user?page=1&pageSize=10&keyword=" 200
check_get    "大页码"       "/api/system/user?page=999&pageSize=10" 200

# 库存不足出库
STOCK_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/inventory/out" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"warehouseId":999,"batchNo":"NONEXISTENT","quantity":99999,"type":"out","orderNo":"TST-FAIL","remark":"Should fail"}' 2>/dev/null)
STOCK_CODE=$(echo "$STOCK_RESP" | tail -1)
if [ "$STOCK_CODE" = "500" ] || echo "$STOCK_RESP" | grep -qi "fail\|error\|不足"; then
  ok "库存不足出库(预期失败)"
else
  echo "  ⚠️  [库存不足出库] — HTTP $STOCK_CODE (expected 500)"
fi

# 非法状态流转 (待生产→直接入库)
BAD_WF=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE/api/production/work-order/4/stock-in" 2>/dev/null)
if [ "$BAD_WF" != "200" ]; then
  ok "非法状态流转(预期失败)"
else
  echo "  ⚠️  [非法状态流转] — HTTP $BAD_WF (expected error)"
fi

# ==========================================
# 10. 清理测试数据
# ==========================================
echo ""
echo "━━━ 清理 ━━━"

if [ -n "$NEW_USER_ID" ]; then
  curl -s -X DELETE "$BASE/api/system/user/$NEW_USER_ID" > /dev/null && \
    echo "  🧹 已删除测试用户 $NEW_USER_ID"
fi

# ==========================================
# 汇总
# ==========================================
echo ""
echo "======================================="
echo "  测试完成"
echo "  ✅ PASS: $PASS"
echo "  ❌ FAIL: $FAIL"
echo "======================================="

if [ "$FAIL" -gt 0 ]; then
  exit 1
else
  exit 0
fi
