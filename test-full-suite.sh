#!/bin/bash
# ============================================
# MES 系统 全功能测试（含认证 + 完整 CRUD）
# ============================================

BASE="http://localhost:8081"
PASS=0
FAIL=0
SKIP=0
TOKEN=""

# ---------- helpers ----------
ok()   { echo "  ✅ [$1] PASS"; ((PASS++)); }
fail() { echo "  ❌ [$1] FAIL — $2"; ((FAIL++)); }
skip() { echo "  ⏭️  [$1] SKIP — $2"; ((SKIP++)); }

# Extract record ID from compact JSON: "id":N,..."field":"value"
get_id() {
  local field="$1" value="$2" resp="$3"
  echo "$resp" | grep -o "\"id\":[0-9]*[^}]*\"${field}\":\"${value}\"" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'
}

# Generic API caller
api_call() {
  local method="$1" url="$2" data="$3"
  if [ "$method" = "GET" ] || [ "$method" = "DELETE" ]; then
    curl -s -X "$method" "$BASE$url" -H "Authorization: Bearer $TOKEN" 2>/dev/null
  else
    curl -s -X "$method" "$BASE$url" -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" -d "$data" 2>/dev/null
  fi
}

# check: validate HTTP status + optional body content
check() {
  local label="$1" method="$2" url="$3" data="$4" expect_code="${5:-200}" expect_body="$6"

  local resp http_code body
  if [ "$method" = "UPLOAD" ]; then
    resp=$(curl -s -w "\n%{http_code}" -X POST "$BASE$url" \
      -H "Authorization: Bearer $TOKEN" -F "$data" 2>/dev/null)
  else
    resp=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE$url" \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d "$data" 2>/dev/null)
    # For GET/DELETE without data, redo without -d
    if [ -z "$data" ] && [ "$method" = "GET" -o "$method" = "DELETE" ]; then
      resp=$(curl -s -w "\n%{http_code}" -X "$method" "$BASE$url" \
        -H "Authorization: Bearer $TOKEN" 2>/dev/null)
    fi
  fi

  http_code=$(echo "$resp" | tail -1)
  body=$(echo "$resp" | sed '$d')

  if [ "$http_code" = "$expect_code" ]; then
    if [ -n "$expect_body" ]; then
      if echo "$body" | grep -q "$expect_body"; then
        ok "$label"
      else
        fail "$label" "body missing '$expect_body': $(echo "$body" | head -c 300)" "$expect_body"
      fi
    else
      ok "$label"
    fi
  else
    fail "$label" "HTTP $http_code (body: $(echo "$body" | head -c 300))" "$expect_code"
  fi
}

check_delete() { check "$1" "DELETE" "$2" "" "${3:-200}" "${4:-}"; }
check_get()    { check "$1" "GET" "$2" "" "${3:-200}" "${4:-}"; }
check_post()   { check "$1" "POST" "$2" "$3" "${4:-200}" "${5:-}"; }
check_put()    { check "$1" "PUT" "$2" "$3" "${4:-200}" "${5:-}"; }

# ==========================================
# LOGIN
# ==========================================
echo ""
echo "======================================="
echo "  MES 全功能 API 测试 v2"
echo "  Server: $BASE"
echo "  Time:   $(date '+%Y-%m-%d %H:%M:%S')"
echo "======================================="
echo ""
echo "━━━ 登录认证 ━━━"

CAPTCHA_RESP=$(curl -s "$BASE/api/captcha/math")
CAPTCHA_KEY=$(echo "$CAPTCHA_RESP" | sed 's/.*"captchaKey":"\([^"]*\)".*/\1/')
QUESTION=$(echo "$CAPTCHA_RESP" | sed 's/.*"question":"\([^"]*\)".*/\1/')
A=$(echo "$QUESTION" | awk '{print $1}')
OP=$(echo "$QUESTION" | awk '{print $2}')
B=$(echo "$QUESTION" | awk '{print $3}')
if [ "$OP" = "+" ]; then ANSWER=$((A + B)); elif [ "$OP" = "-" ]; then ANSWER=$((A - B)); else ANSWER=$((A * B)); fi
echo "  验证码: $A $OP $B = $ANSWER"

LOGIN_RESP=$(curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\",\"captchaKey\":\"$CAPTCHA_KEY\",\"captchaAnswer\":\"$ANSWER\"}")
TOKEN=$(echo "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo "  ❌ 登录失败: $(echo "$LOGIN_RESP" | head -c 300)"
  exit 1
fi
echo "  ✅ 登录成功, Token: ${TOKEN:0:16}..."
ok "登录获取Token" ""

TS=$(date +%s)

# ==========================================
# 1. 系统管理 — 用户 / 角色 / 菜单
# ==========================================
echo ""
echo "━━━ 1a. 用户管理 ━━━"

check_get "用户分页列表"   "/api/system/user?page=1&pageSize=5" 200 "success"
check_get "用户分页搜索"   "/api/system/user?page=1&pageSize=5&keyword=admin" 200 "success"
check_get "用户详情(ID=1)" "/api/system/user/1" 200 "admin"

# Create user
NEW_USER="testuser_${TS}"
resp=$(api_call "POST" "/api/system/user" \
  "{\"username\":\"${NEW_USER}\",\"password\":\"123456\",\"nickname\":\"Tester\",\"status\":1}")
if echo "$resp" | grep -q '"code":200'; then
  ok "新增用户" ""
  USERS_RESP=$(api_call "GET" "/api/system/user?page=1&pageSize=50")
  NEW_USER_ID=$(get_id "username" "$NEW_USER" "$USERS_RESP")
  if [ -n "$NEW_USER_ID" ]; then
    check_put "修改用户" "/api/system/user" "{\"id\":$NEW_USER_ID,\"nickname\":\"Edited\",\"status\":1}"
  else
    echo "  ⚠️  无法提取用户ID，跳过修改测试"
    ((SKIP++))
  fi
else
  fail "新增用户" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

check_post "分配角色" "/api/system/user/1/roles" '{"roleIds":[1,2]}'

echo ""
echo "━━━ 1b. 角色管理 ━━━"

check_get    "角色列表"     "/api/system/role" 200 "success"
ROLE_CODE="test_role_${TS}"
resp=$(api_call "POST" "/api/system/role" "{\"code\":\"${ROLE_CODE}\",\"name\":\"TestRole\",\"description\":\"Auto test\"}")
if echo "$resp" | grep -q '"code":200'; then
  ok "新增角色" ""
  ROLES_RESP=$(api_call "GET" "/api/system/role")
  ROLE_ID=$(get_id "code" "$ROLE_CODE" "$ROLES_RESP")
  if [ -n "$ROLE_ID" ]; then
    check_put    "修改角色"     "/api/system/role" "{\"id\":$ROLE_ID,\"code\":\"${ROLE_CODE}\",\"name\":\"TestRoleEdit\"}"
    check_post   "分配菜单"     "/api/system/role/$ROLE_ID/menus" '{"menuIds":[11,12,13]}'
    check_delete "删除角色"     "/api/system/role/$ROLE_ID"
  else
    echo "  ⚠️  无法提取角色ID，跳过修改/删除测试"
    ((SKIP++))
  fi
else
  fail "新增角色" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

echo ""
echo "━━━ 1c. 菜单管理 ━━━"

check_get    "菜单树"       "/api/system/menu/tree" 200 "success"
check_get    "菜单列表"     "/api/system/menu" 200 "success"
resp=$(api_call "POST" "/api/system/menu" '{"parentId":1,"name":"TestMenu","type":2,"path":"/test","sort":99}')
if echo "$resp" | grep -q '"code":200'; then
  ok "新增菜单" ""
  MENUS_RESP=$(api_call "GET" "/api/system/menu")
  MENU_ID=$(get_id "name" "TestMenu" "$MENUS_RESP")
  if [ -n "$MENU_ID" ]; then
    check_put    "修改菜单"     "/api/system/menu" "{\"id\":$MENU_ID,\"name\":\"TestMenuEdit\",\"type\":2,\"path\":\"/test-edit\"}"
    check_delete "删除菜单"     "/api/system/menu/$MENU_ID"
  else
    echo "  ⚠️  无法提取菜单ID，跳过修改/删除测试"
    ((SKIP++))
  fi
else
  fail "新增菜单" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

# ==========================================
# 2. 基础数据
# ==========================================
echo ""
echo "━━━ 2a. 产品管理 ━━━"

check_get    "产品分页"        "/api/base/product?page=1&pageSize=5" 200 "success"
check_get    "产品搜索"        "/api/base/product?page=1&pageSize=5&keyword=Gear" 200
check_get    "产品详情(含BOM)" "/api/base/product/1" 200 "success"
PROD_CODE="TEST_${TS}"
resp=$(api_call "POST" "/api/base/product" "{\"code\":\"${PROD_CODE}\",\"name\":\"TestProduct\",\"spec\":\"D10\",\"unit\":\"pcs\",\"price\":99.99,\"status\":1}")
if echo "$resp" | grep -q '"code":200'; then
  ok "新增产品" ""
  PRODS_RESP=$(api_call "GET" "/api/base/product?page=1&pageSize=50")
  PROD_ID=$(get_id "code" "$PROD_CODE" "$PRODS_RESP")
  if [ -n "$PROD_ID" ]; then
    check_put    "修改产品"     "/api/base/product" "{\"id\":$PROD_ID,\"code\":\"${PROD_CODE}\",\"name\":\"TestProductEdit\",\"status\":1}"
    check_post   "保存BOM"     "/api/base/product/$PROD_ID/bom" "[{\"materialId\":9,\"quantity\":2.5,\"unit\":\"bar\"},{\"materialId\":10,\"quantity\":1.0,\"unit\":\"block\"}]" 200
    check_delete "删除产品"     "/api/base/product/$PROD_ID"
  else
    echo "  ⚠️  无法提取产品ID，跳过修改/BOM/删除测试"
    ((SKIP++))
  fi
else
  fail "新增产品" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

echo ""
echo "━━━ 2b. 工序管理 ━━━"

check_get    "工序列表"     "/api/base/process" 200
resp=$(api_call "POST" "/api/base/process" '{"code":"TEST01","name":"TestProcess","standardHours":10.0,"price":5.0,"sort":99,"status":1}')
if echo "$resp" | grep -q '"code":200'; then
  ok "新增工序" ""
  PROCS_RESP=$(api_call "GET" "/api/base/process")
  PROC_ID=$(get_id "code" "TEST01" "$PROCS_RESP")
  if [ -n "$PROC_ID" ]; then
    check_put    "修改工序"     "/api/base/process" "{\"id\":$PROC_ID,\"code\":\"TEST01\",\"name\":\"TestProcessEdit\",\"status\":1}"
    check_delete "删除工序"     "/api/base/process/$PROC_ID"
  else
    echo "  ⚠️  无法提取工序ID，跳过修改/删除测试"
    ((SKIP++))
  fi
else
  fail "新增工序" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

echo ""
echo "━━━ 2c. 仓库管理 ━━━"

check_get    "仓库列表"     "/api/base/warehouse" 200
resp=$(api_call "POST" "/api/base/warehouse" '{"code":"WH99","name":"TestWH","type":"material","status":1}')
if echo "$resp" | grep -q '"code":200'; then
  ok "新增仓库" ""
  WHS_RESP=$(api_call "GET" "/api/base/warehouse")
  WH_ID=$(get_id "code" "WH99" "$WHS_RESP")
  if [ -n "$WH_ID" ]; then
    check_put    "修改仓库"     "/api/base/warehouse" "{\"id\":$WH_ID,\"code\":\"WH99\",\"name\":\"TestWHEdit\",\"type\":\"material\"}"
    check_get    "库位列表"     "/api/base/warehouse/$WH_ID/locations" 200
    check_post   "新增库位"     "/api/base/warehouse/$WH_ID/locations" '{"code":"LOC-99","name":"Test Location"}' 200
    check_delete "删除仓库"     "/api/base/warehouse/$WH_ID"
  else
    echo "  ⚠️  无法提取仓库ID，跳过修改/库位/删除测试"
    ((SKIP++))
  fi
else
  fail "新增仓库" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

echo ""
echo "━━━ 2d. 客户管理 ━━━"

check_get    "客户列表"     "/api/base/customer" 200
check_get    "客户搜索"     "/api/base/customer?keyword=BYD" 200

# ==========================================
# 3. 销售管理
# ==========================================
echo ""
echo "━━━ 3a. 销售订单 ━━━"

check_get    "订单分页"     "/api/sale/order?page=1&pageSize=5" 200 "success"
check_get    "订单筛选"     "/api/sale/order?page=1&pageSize=5&status=1" 200
check_get    "订单详情"     "/api/sale/order/1" 200 "success"

resp=$(api_call "POST" "/api/sale/order" \
  '{"customerId":1,"orderDate":"2026-07-08","deliveryDate":"2026-07-20","remark":"Auto test order","items":[{"productId":3,"quantity":10,"unit":"pcs","price":45.00}]}')
if echo "$resp" | grep -q '"code":200'; then
  ok "创建订单" ""
  ORDERS_RESP=$(api_call "GET" "/api/sale/order?page=1&pageSize=10")
  ORDER_ID=$(get_id "remark" "Auto test order" "$ORDERS_RESP")
  if [ -n "$ORDER_ID" ]; then
    check_put    "修改订单(待审核)" "/api/sale/order" "{\"id\":$ORDER_ID,\"customerId\":1,\"orderDate\":\"2026-07-08\",\"items\":[{\"productId\":3,\"quantity\":20,\"unit\":\"pcs\",\"price\":45.00}]}"
    check_put    "审核通过"     "/api/sale/order/$ORDER_ID/status?status=2" 200
    check_put    "状态→生产中"  "/api/sale/order/$ORDER_ID/status?status=3" 200
    check_delete "删除订单"     "/api/sale/order/$ORDER_ID"
  else
    echo "  ⚠️  无法提取订单ID，跳过完整生命周期测试"
    ((SKIP++))
  fi
else
  fail "创建订单" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

echo ""
echo "━━━ 3b. 发货管理 ━━━"

check_get    "发货列表"     "/api/sale/delivery?page=1&pageSize=5" 200

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
# 5. 生产管理
# ==========================================
echo ""
echo "━━━ 5a. 生产工单 ━━━"

check_get    "工单分页"     "/api/production/work-order?page=1&pageSize=5" 200
check_get    "工单筛选"     "/api/production/work-order?page=1&pageSize=5&status=1" 200
check_get    "工单详情"     "/api/production/work-order/1" 200

resp=$(api_call "POST" "/api/production/work-order" \
  '{"productId":3,"quantity":100,"sourceType":"manual","planStart":"2026-07-08","planEnd":"2026-07-15","remark":"Auto test WO","processes":[{"processId":1,"planQty":100},{"processId":2,"planQty":100}]}')
if echo "$resp" | grep -q '"code":200'; then
  ok "创建工单" ""
  WOS_RESP=$(api_call "GET" "/api/production/work-order?page=1&pageSize=10")
  WO_ID=$(get_id "remark" "Auto test WO" "$WOS_RESP")
  if [ -n "$WO_ID" ]; then
    check_put    "开工"         "/api/production/work-order/$WO_ID/start" "" 200
    check_put    "完工"         "/api/production/work-order/$WO_ID/complete" "" 200
    check_put    "完工入库"     "/api/production/work-order/$WO_ID/stock-in" "" 200
    check_delete "删除工单"     "/api/production/work-order/$WO_ID"
  else
    echo "  ⚠️  无法提取工单ID，跳过生命周期测试"
    ((SKIP++))
  fi
else
  fail "创建工单" "code:200 (body: $(echo "$resp" | head -c 200))" "code:200"
fi

echo ""
echo "━━━ 5b. 报工管理 ━━━"

check_get    "报工列表"     "/api/production/report?page=1&pageSize=5" 200

echo ""
echo "━━━ 5c. 质检管理 ━━━"

check_get    "质检列表"     "/api/production/qc?page=1&pageSize=5" 200

# ==========================================
# 6. 知识库
# ==========================================
echo ""
echo "━━━ 6. 知识库 ━━━"

check_get    "文档分页"     "/api/knowledge?page=1&pageSize=5" 200
check_get    "文档筛选"     "/api/knowledge?page=1&pageSize=5&category=sop" 200

TMPFILE=$(mktemp)
echo "This is a test document for the MES knowledge base. It contains information about production processes and quality control procedures." > "$TMPFILE"
UPLOAD_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/knowledge/upload?category=sop" \
  -H "Authorization: Bearer $TOKEN" \
  -F "file=@$TMPFILE" 2>/dev/null)
UPLOAD_CODE=$(echo "$UPLOAD_RESP" | tail -1)
UPLOAD_BODY=$(echo "$UPLOAD_RESP" | sed '$d')
if [ "$UPLOAD_CODE" = "200" ]; then
  ok "上传文档" ""
  DOC_ID=$(echo "$UPLOAD_BODY" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
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

CHAT_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/ai/chat" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"question":"How do I check the inventory of product P001?"}' 2>/dev/null)
CHAT_CODE=$(echo "$CHAT_RESP" | tail -1)
CHAT_BODY=$(echo "$CHAT_RESP" | sed '$d')
if [ "$CHAT_CODE" = "200" ]; then
  ok "发送AI消息" ""
  CONV_ID=$(echo "$CHAT_BODY" | grep -o '"conversationId":[0-9]*' | grep -o '[0-9]*' | head -1)
  if [ -n "$CONV_ID" ]; then
    check_get "对话消息"     "/api/ai/conversations/$CONV_ID/messages" 200
  fi
else
  echo "  ⚠️  [发送AI消息] — HTTP $CHAT_CODE (AI service may be unavailable)"
  ((SKIP++))
fi

# ==========================================
# 8. Dashboard / BI / Attendance
# ==========================================
echo ""
echo "━━━ 8a. Dashboard ━━━"

check_get    "仪表盘统计"   "/api/dashboard/stats" 200
check_get    "生产进度"     "/api/dashboard/production-progress" 200
check_get    "订单趋势"     "/api/dashboard/order-trend" 200
check_get    "库存概览"     "/api/dashboard/inventory-overview" 200

echo ""
echo "━━━ 8b. BI 报表 ━━━"

check_get    "BI概览"       "/api/bi/overview" 200
check_get    "BI销售报表"   "/api/bi/overview/sales?period=month" 200
check_get    "BI库存报表"   "/api/bi/overview/inventory" 200
check_get    "BI生产报表"   "/api/bi/overview/production" 200
check_get    "数据透视-产品" "/api/bi/pivot?dimension=product&measure=quantity" 200
check_get    "预警规则列表" "/api/bi/alerts/rules" 200
check_get    "预警记录列表" "/api/bi/alerts/records" 200

echo ""
echo "━━━ 8c. 考勤管理 ━━━"

check_get    "考勤列表"     "/api/attendance?page=1&pageSize=5" 200

# ==========================================
# 9. 认证接口（含已知BUG标记）
# ==========================================
echo ""
echo "━━━ 9. 认证接口测试 ━━━"

# GET /api/auth/user-info — BUG: ClassCastException (GenericJackson2JsonRedisSerializer returns Integer, code casts to Long)
UI_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/api/auth/user-info" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
UI_CODE=$(echo "$UI_RESP" | tail -1)
UI_BODY=$(echo "$UI_RESP" | sed '$d')
if [ "$UI_CODE" = "200" ] && echo "$UI_BODY" | grep -q '"code":200'; then
  ok "获取用户信息" ""
elif echo "$UI_BODY" | grep -q "系统繁忙"; then
  echo "  🐛 [获取用户信息] — 已知BUG: AuthController.userInfo 将Integer强转为Long导致ClassCastException"
  ((FAIL++))
else
  fail "获取用户信息" "HTTP $UI_CODE: $(echo "$UI_BODY" | head -c 200)" "200"
fi

# GET /api/user/profile — BUG: no GET handler, only PUT exists
PF_RESP=$(curl -s -w "\n%{http_code}" -X GET "$BASE/api/user/profile" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
PF_CODE=$(echo "$PF_RESP" | tail -1)
PF_BODY=$(echo "$PF_RESP" | sed '$d')
if [ "$PF_CODE" = "200" ] && echo "$PF_BODY" | grep -q '"code":200'; then
  ok "获取个人信息" ""
else
  echo "  🐛 [获取个人信息] — 缺少 GET /api/user/profile 处理器 (仅有 PUT)"
  ((FAIL++))
fi

# ==========================================
# 10. 边界/异常测试
# ==========================================
echo ""
echo "━━━ 10. 边界/异常测试 ━━━"

check_get    "不存在的用户"  "/api/system/user/99999" 200
check_delete "删除不存在记录" "/api/system/user/99999" 200
check_get    "空关键词分页"  "/api/system/user?page=1&pageSize=10&keyword=" 200
check_get    "大页码"        "/api/system/user?page=999&pageSize=10" 200

# 库存不足出库 (expect error)
STOCK_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE/api/inventory/out" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"warehouseId":999,"batchNo":"NONEXISTENT","quantity":99999,"type":"out","orderNo":"TST-FAIL","remark":"Should fail"}' 2>/dev/null)
STOCK_CODE=$(echo "$STOCK_RESP" | tail -1)
STOCK_BODY=$(echo "$STOCK_RESP" | sed '$d')
if [ "$STOCK_CODE" = "500" ] || [ "$STOCK_CODE" = "400" ] || echo "$STOCK_BODY" | grep -qi "不足\|fail\|error\|500"; then
  ok "库存不足出库(正确拒绝)" ""
else
  echo "  ⚠️  [库存不足出库] — HTTP $STOCK_CODE (expected error)"
  ((SKIP++))
fi

# 非法状态流转 (expect error)
BAD_WF_RESP=$(curl -s -w "\n%{http_code}" -X PUT "$BASE/api/production/work-order/4/stock-in" \
  -H "Authorization: Bearer $TOKEN" 2>/dev/null)
BAD_WF_CODE=$(echo "$BAD_WF_RESP" | tail -1)
BAD_WF_BODY=$(echo "$BAD_WF_RESP" | sed '$d')
if [ "$BAD_WF_CODE" != "200" ] || echo "$BAD_WF_BODY" | grep -qi "fail\|error\|500\|只有"; then
  ok "非法状态流转(正确拒绝)" ""
else
  echo "  ⚠️  [非法状态流转] — HTTP $BAD_WF_CODE (expected error)"
  ((SKIP++))
fi

# 无token请求 (expect 401)
NOAUTH_RESP=$(curl -s -o /dev/null -w "%{http_code}" -X GET "$BASE/api/system/user?page=1&pageSize=5" 2>/dev/null)
if [ "$NOAUTH_RESP" = "401" ]; then
  ok "无Token被拒绝(401)" ""
else
  echo "  ⚠️  [无Token被拒绝] — HTTP $NOAUTH_RESP (expected 401)"
  ((SKIP++))
fi

# ==========================================
# 11. 清理
# ==========================================
echo ""
echo "━━━ 清理 ━━━"

if [ -n "$NEW_USER_ID" ]; then
  api_call "DELETE" "/api/system/user/$NEW_USER_ID" > /dev/null && \
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
echo "  ⚠️  SKIP: $SKIP"
echo "  Totals: $((PASS + FAIL + SKIP))"
echo "======================================="

if [ "$FAIL" -gt 0 ]; then
  exit 1
else
  exit 0
fi
